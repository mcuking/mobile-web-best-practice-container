package com.hht.webpackagekit;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;

import com.google.gson.Gson;
import com.hht.webpackagekit.core.AssetResourceLoader;
import com.hht.webpackagekit.core.Constants;
import com.hht.webpackagekit.core.Downloader;
import com.hht.webpackagekit.core.PackageEntity;
import com.hht.webpackagekit.core.PackageInfo;
import com.hht.webpackagekit.core.PackageInstaller;
import com.hht.webpackagekit.core.PackageStatus;
import com.hht.webpackagekit.core.ResourceManager;
import com.hht.webpackagekit.core.util.FileUtils;
import com.hht.webpackagekit.core.util.GsonUtils;
import com.hht.webpackagekit.core.util.Logger;
import com.hht.webpackagekit.core.util.VersionUtils;
import com.hht.webpackagekit.inner.AssetResourceLoaderImpl;
import com.hht.webpackagekit.inner.DownloaderImpl;
import com.hht.webpackagekit.inner.PackageInstallerImpl;
import com.hht.webpackagekit.inner.ResourceManagerImpl;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 离线包管理器
 */
public class PackageManager {
    private static final int WHAT_DOWNLOAD_SUCCESS = 1;
    private static final int WHAT_DOWNLOAD_FAILURE = 2;
    private static final int WHAT_START_UPDATE = 3;
    private static final int WHAT_INIT_ASSETS = 4;

    private static final int STATUS_PACKAGE_CANUSE = 1;

    private volatile static PackageManager instance;

    private Context context;
    private ResourceManager resourceManager;
    private PackageInstaller packageInstaller;
    private AssetResourceLoader assetResourceLoader;
    private volatile boolean isUpdating = false;
    private Handler packageHandler;
    private HandlerThread packageThread;
    private PackageEntity localPackageEntity;
    //即将下载的离线包资源PackageInfo的集合
    private List<PackageInfo> willDownloadPackageInfoList;
    //需要更新的离线包资源PackageInfo的集合
    private List<PackageInfo> onlyUpdatePackageInfoList;
    private Lock resourceLock;
    private Map<String, Integer> packageStatusMap = new HashMap<>();
    private PackageConfig config = new PackageConfig();


    public static synchronized PackageManager getInstance(){
        if(instance == null){
            instance = new PackageManager();
        }
        return instance;
    }

    private PackageManager() {
        resourceLock = new ReentrantLock();
    }

    public void init(Context context) {

        Log.d("PackageManager","init");
        this.context = context;

        resourceManager = new ResourceManagerImpl(context);
        packageInstaller = new PackageInstallerImpl(context);
        FileDownloader.init(context);
        if (config.isEnableAssets() && !TextUtils.isEmpty(config.getAssetPath())) {
            assetResourceLoader = new AssetResourceLoaderImpl(context);
            ensurePackageThread();
            packageHandler.sendEmptyMessage(WHAT_INIT_ASSETS);
        }
    }

    //从服务端获取最新的packageIndex.json
    public void update(String packageIndexStr) {
        if (isUpdating) {
            return;
        }
        if (packageIndexStr == null) {
            packageIndexStr = "";
        }
        ensurePackageThread();
        Message message = Message.obtain();
        message.what = WHAT_START_UPDATE;
        message.obj = packageIndexStr;
        packageHandler.sendMessage(message);
    }

    private void ensurePackageThread() {
        if (packageThread == null) {
            packageThread = new HandlerThread("offline_package_thread");
            packageThread.start();
            packageHandler = new PackageHandler(packageThread.getLooper());
        }
    }

    //获取预置在assets中的离线包并解压到相应目录
    private void performLoadAssets() {
      if (assetResourceLoader == null) {
        return;
      }

      for (int i = 0; i < Constants.LOCAL_ASSET_LIST.length ; i++) {
        Log.d("PKGM1", Constants.LOCAL_ASSET_LIST[i]);
        PackageInfo packageInfo = assetResourceLoader.load(Constants.LOCAL_ASSET_LIST[i]);
        if (packageInfo == null) {
          return;
        }
        Log.d("PKGM2", packageInfo.getPackageId());
        installPackage(packageInfo.getPackageId(), packageInfo, true);
      }
    }

    //基于从服务端拉取的packageIndex.json，决定升级哪些离线包
    private void performUpdate(String packageIndexStr) {
        Log.d("download", "performupdate");
        //读取本地packageIndex.json文件
        String localPackageIndexFileName = FileUtils.getPackageIndexFileName(context);
        File localPackageIndexFile = new File(localPackageIndexFileName);

        //是否是第一次加载离线包
        boolean isFirstLoadPackage = false;
        if (!localPackageIndexFile.exists()) {
            isFirstLoadPackage = true;
        }

        //将从服务端拉取的packageIndex.json中离线包数组信息转化成willDownloadPackageInfoList数组
        PackageEntity packageEntity = null;
        packageEntity = GsonUtils.fromJsonIgnoreException(packageIndexStr, PackageEntity.class);
        willDownloadPackageInfoList = new ArrayList<>(2);
        if (packageEntity != null && packageEntity.getItems() != null) {
            willDownloadPackageInfoList.addAll(packageEntity.getItems());
        }

        //不是第一次load package，则要比对本地和服务端拉取的packageIndex.json信息，决定哪些离线包需要加载
        if (!isFirstLoadPackage) {
            initLocalEntity(localPackageIndexFile);
        }
        List<PackageInfo> packageInfoList = new ArrayList<>(willDownloadPackageInfoList.size());
        for (PackageInfo packageInfo : willDownloadPackageInfoList) {
            if (packageInfo.getStatus() == PackageStatus.offLine) {
                continue;
            }
            packageInfoList.add(packageInfo);
        }
        willDownloadPackageInfoList.clear();
        willDownloadPackageInfoList.addAll(packageInfoList);

        //遍历处理好的willDownloadPackageInfoList，下载相应离线包，下载成功后调用PackageInstaller.install将包与之前的离线包合并或替换，并解压到指定目录
        for (PackageInfo packageInfo : willDownloadPackageInfoList) {
            Downloader downloader = new DownloaderImpl(context);
            downloader.download(packageInfo, new DownloadCallback(this));
        }

        if (onlyUpdatePackageInfoList != null && onlyUpdatePackageInfoList.size() > 0) {
            for (PackageInfo packageInfo : onlyUpdatePackageInfoList) {
                resourceManager.updateResource(packageInfo.getPackageId(), packageInfo.getVersion());
                synchronized (packageStatusMap) {
                    packageStatusMap.put(packageInfo.getPackageId(), STATUS_PACKAGE_CANUSE);
                }
            }
        }
    }

    //比对willDownloadPackageInfoList中离线包packageInfo和本地离线包localPackageInfo版本，决定是否下载
    private void initLocalEntity(File localPackageIndexFile) {
        FileInputStream localIndexFis = null;
        try {
            localIndexFis = new FileInputStream(localPackageIndexFile);
        } catch (FileNotFoundException e) {

        }
        if (localIndexFis == null) {
            return;
        }
        localPackageEntity = GsonUtils.fromJsonIgnoreException(localIndexFis, PackageEntity.class);
        if (localPackageEntity == null || localPackageEntity.getItems() == null) {
            return;
        }
        int index = 0;
        for (PackageInfo localPackageInfo : localPackageEntity.getItems()) {
            // 如果本地 packageIndex 的某个包 localPackageInfo 不在从服务器拉下来的 packageIndex 中，则跳出本次循环
            if ((index = willDownloadPackageInfoList.indexOf(localPackageInfo)) < 0) {
                continue;
            }
            //当本地离线包localPackageInfo版本大于等于从服务端拉取的最新的packageIndex.json的packageInfo版本
            //则从willDownloadPackageInfoList中移除离线包packageInfo，移入onlyUpdatePackageInfoList，即只需更新packageInfo，无需下载
            //否则更新本地离线包localPackageInfo的版本，然后等待下载
            PackageInfo packageInfo = willDownloadPackageInfoList.get(index);
            Log.d("PKGM","pkgId:"+packageInfo.getPackageId() + "|" +"remote version:" + packageInfo.getVersion() + "|" +"local version:" + localPackageInfo.getVersion());
            if (VersionUtils.compareVersion(packageInfo.getVersion(), localPackageInfo.getVersion()) <= 0) {
                if (!checkResourceFileValid(packageInfo.getPackageId(), packageInfo.getVersion())) {
                    return;
                }
                willDownloadPackageInfoList.remove(index);
                if (onlyUpdatePackageInfoList == null) {
                    onlyUpdatePackageInfoList = new ArrayList<>(2);
                }
                if (packageInfo.getStatus() == PackageStatus.onLine) {
                    onlyUpdatePackageInfoList.add(localPackageInfo);
                }
                localPackageInfo.setStatus(packageInfo.getStatus());
            } else {
                localPackageInfo.setStatus(packageInfo.getStatus());
                localPackageInfo.setVersion(packageInfo.getVersion());
                if ((Integer.parseInt(packageInfo.getVersion()) - Integer.parseInt(localPackageInfo.getVersion()) > 1)) {
                  packageInfo.setDownloadUrl(packageInfo.getOrigin_file_path());
                  packageInfo.setIsPatch(false);
                  packageInfo.setMd5(packageInfo.getOrigin_file_md5());
                } else {
                  packageInfo.setDownloadUrl(packageInfo.getPatch_file_path());
                  packageInfo.setIsPatch(true);
                  packageInfo.setMd5(packageInfo.getPatch_file_md5());
                }
            }
        }
    }

    private boolean checkResourceFileValid(String packageId, String version) {
        File indexFile = FileUtils.getResourceIndexFile(context, packageId, version);
        return indexFile.exists() && indexFile.isFile();
    }

    //更新packageIndex.json中packageId对应的离线包版本
    private void updatePackageIndexFile(String packageId, String version) {
        Log.d("PKGM", "updatePackageIndexFile");
        String packageIndexFileName = FileUtils.getPackageIndexFileName(context);
        File packageIndexFile = new File(packageIndexFileName);
        //若不存在packageIndex.json，则创建一个packageIndex.json
        if (!packageIndexFile.exists()) {
            boolean isSuccess = true;
            try {
                isSuccess = packageIndexFile.createNewFile();
            } catch (IOException e) {
                isSuccess = false;
            }
            if (!isSuccess) {
                return;
            }
        }
        if (localPackageEntity == null) {
            FileInputStream indexFis = null;
            try {
                indexFis = new FileInputStream(packageIndexFile);
            } catch (FileNotFoundException e) {

            }
            if (indexFis == null) {
                return;
            }
            localPackageEntity = GsonUtils.fromJsonIgnoreException(indexFis, PackageEntity.class);
        }
        if (localPackageEntity == null) {
            localPackageEntity = new PackageEntity();
        }

        //获取localPackageEntity下所有的离线包packageInfo，如果存在packageId对应的包，则更新版本状态等，没有则加进去
        List<PackageInfo> packageInfoList = new ArrayList<>(2);
        if (localPackageEntity.getItems() != null) {
            packageInfoList.addAll(localPackageEntity.getItems());
        }
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setPackageId(packageId);
        int index = 0;
        if ((index = packageInfoList.indexOf(packageInfo)) >= 0) {
            packageInfoList.get(index).setVersion(version);
        } else {
            packageInfo.setStatus(PackageStatus.onLine);
            packageInfo.setVersion(version);
            packageInfoList.add(packageInfo);
        }
        localPackageEntity.setItems(packageInfoList);
        if (localPackageEntity == null || localPackageEntity.getItems() == null
            || localPackageEntity.getItems().size() == 0) {
            return;
        }

        //将最新的数据写入到packageIndex.json文件中
        String updateStr = new Gson().toJson(localPackageEntity);
        try {
            FileOutputStream outputStream = new FileOutputStream(packageIndexFile);
            try {
                outputStream.write(updateStr.getBytes());
            } catch (IOException ignore) {
                Logger.e("write packageIndex file error");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ignore) {
            Logger.e("read packageIndex file error");
        }
    }

    public WebResourceResponse getResource(String url) {
        synchronized (packageStatusMap) {

            String packageId = resourceManager.getPackageId(url);
            Integer status = packageStatusMap.get(packageId);
            Log.d("WebResourceResponse", status + " | " + url + " | " + packageId +"| packageStatusMap size:"+packageStatusMap.size());
            if (status == null) {
                return null;
            }
            if (status != STATUS_PACKAGE_CANUSE) {
                return null;
            }
        }
        WebResourceResponse resourceResponse = null;

         synchronized (resourceManager) {
           resourceResponse = resourceManager.getResource(url);
         }
        return resourceResponse;
    }

    private void downloadSuccess(String packageId) {
        Log.d("download", "success");
        if (packageHandler == null) {
            return;
        }
        Message message = Message.obtain();
        message.what = WHAT_DOWNLOAD_SUCCESS;
        message.obj = packageId;
        packageHandler.sendMessage(message);
    }

    private void downloadFailure(String packageId) {
        Log.d("download", "failure");
        if (packageHandler == null) {
            return;
        }
        Message message = Message.obtain();
        message.what = WHAT_DOWNLOAD_FAILURE;
        message.obj = packageId;
        packageHandler.sendMessage(message);
    }

    //将预置在assets或刚下载的离线包解压到指定目录
    private void installPackage(String packageId, PackageInfo packageInfo, boolean isAssets) {
        if (packageInfo != null) {
            //暂时关闭对下载文件的MD5校验
//            boolean isValid = (isAssets && assetValidator.validate(packageInfo)) || validator.validate(packageInfo);
//            if (isValid) {
                resourceLock.lock();
                boolean isSuccess = packageInstaller.install(packageInfo, isAssets);
                resourceLock.unlock();
                //安装失败情况下，不做任何处理，因为资源既然资源需要最新资源，失败了，就没有必要再用缓存了
                if (isSuccess) {
                    resourceManager.updateResource(packageInfo.getPackageId(), packageInfo.getVersion());
                    //更新安装成功的离线包版本到packageIndex.json
                    updatePackageIndexFile(packageInfo.getPackageId(), packageInfo.getVersion());
                    synchronized (packageStatusMap) {
                        packageStatusMap.put(packageId, STATUS_PACKAGE_CANUSE);
                    }
                }
//            }
        }
    }

    //某个离线包下载成功后，则从willDownloadPackageInfoLis移除，并解压该离线包到相应文件
    private void performDownloadSuccess(String packageId) {
        if (willDownloadPackageInfoList == null) {
            return;
        }
        PackageInfo packageInfo = null;
        PackageInfo tmp = new PackageInfo();
        tmp.setPackageId(packageId);
        int pos = willDownloadPackageInfoList.indexOf(tmp);
        if (pos >= 0) {
            packageInfo = willDownloadPackageInfoList.remove(pos);
        }
        allResouceUpdateFinished();
        installPackage(packageId, packageInfo, false);
    }

    //某个离线包下载失败后，则从willDownloadPackageInfoLis移除
    private void performDownloadFailure(String packageId) {
        if (willDownloadPackageInfoList == null) {
            return;
        }
        PackageInfo packageInfo = null;
        PackageInfo tmp = new PackageInfo();
        tmp.setPackageId(packageId);
        int pos = willDownloadPackageInfoList.indexOf(tmp);
        if (pos >= 0) {
            willDownloadPackageInfoList.remove(pos);
        }
        allResouceUpdateFinished();
    }

    private void allResouceUpdateFinished() {
        if (willDownloadPackageInfoList.size() == 0) {
            isUpdating = false;
        }
    }

    //离线包handler处理器
    class PackageHandler extends Handler {
        public PackageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DOWNLOAD_SUCCESS:
                    performDownloadSuccess((String) msg.obj);
                    break;
                case WHAT_DOWNLOAD_FAILURE:
                    performDownloadFailure((String) msg.obj);
                    break;
                case WHAT_START_UPDATE:
                    performUpdate((String) msg.obj);
                    break;
                case WHAT_INIT_ASSETS:
                    performLoadAssets();
                    break;
                default:
                    break;
            }
        }
    }

    static class DownloadCallback implements Downloader.DownloadCallback {
        private PackageManager packageManager;

        public DownloadCallback(PackageManager packageManager) {
            this.packageManager = packageManager;
        }

        @Override
        public void onSuccess(String packageId) {
            packageManager.downloadSuccess(packageId);
        }

        @Override
        public void onFailure(String packageId) {
            packageManager.downloadFailure(packageId);
        }
    }

}
