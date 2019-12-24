package com.hht.webpackagekit.inner;

import android.content.Context;
import android.text.TextUtils;

import com.hht.webpackagekit.core.AssetResourceLoader;
import com.hht.webpackagekit.core.PackageInfo;
import com.hht.webpackagekit.core.PackageStatus;
import com.hht.webpackagekit.core.ResourceInfoEntity;
import com.hht.webpackagekit.core.util.FileUtils;
import com.hht.webpackagekit.core.util.GsonUtils;
import com.hht.webpackagekit.core.util.MD5Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * asset 资源加载
 */
public class AssetResourceLoaderImpl implements AssetResourceLoader {
    private Context context;

    public AssetResourceLoaderImpl(Context context) {
        this.context = context;
    }

    //基于预置的离线包生成PackageInfo
    @Override
    public PackageInfo load(String path) {
        InputStream inputStream = null;

        //读取assets目录下的离线包
        inputStream = openAssetInputStream(path);
        if (inputStream == null) {
            return null;
        }

        String indexInfo = FileUtils.getStringForZip(inputStream);

        if (TextUtils.isEmpty(indexInfo)) {
            return null;
        }

        //基于assets下某个离线包生成ResourceInfoEntity实例，可获取离线包的version、packageId和ResourceInfo（具体每个远程资源对应一个本地资源）数组
        ResourceInfoEntity assetEntity = GsonUtils.fromJsonIgnoreException(indexInfo, ResourceInfoEntity.class);
        if (assetEntity == null) {
            return null;
        }

        //读取update.zip文件流
        File file =
            new File(FileUtils.getPackageUpdateName(context, assetEntity.getPackageId(), assetEntity.getVersion()));
        ResourceInfoEntity localEntity = null;
        FileInputStream fileInputStream = null;
        if (file.exists()) {
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {

            }
        }
        String local = null;
        if (fileInputStream != null) {
            local = FileUtils.getStringForZip(fileInputStream);
        }
        //基于update.zip生成ResourceInfoEntity实例
        if (!TextUtils.isEmpty(local)) {
            localEntity = GsonUtils.fromJsonIgnoreException(local, ResourceInfoEntity.class);
        }
        //比对update.zip和assets生成的ResourceInfoEntity实例的版本version，如果assets版本小于等于update.zip则返回null

//        if (localEntity != null
//            && VersionUtils.compareVersion(assetEntity.getVersion(), localEntity.getVersion()) <= 0) {
//            return null;
//        }

        //将assets目录下某个资源包拷贝到设定的目录下PackageAssetsName: ${root}/assets/${packageId}/${version}/package.zip
        String assetPath =
            FileUtils.getPackageAssetsName(context, assetEntity.getPackageId(), assetEntity.getVersion());

        inputStream = openAssetInputStream(path);
        if (inputStream == null) {
            return null;
        }
        boolean isSuccess = FileUtils.copyFile(inputStream, assetPath);
        if (!isSuccess) {
            return null;
        }
        FileUtils.safeCloseFile(inputStream);

        String md5 = MD5Utils.calculateMD5(new File(assetPath));
        if (TextUtils.isEmpty(md5)) {
            return null;
        }

        //将assets文件下某个压缩资源包的信息转化成PackageInfo实例的属性
        PackageInfo info = new PackageInfo();
        info.setPackageId(assetEntity.getPackageId());
        info.setStatus(PackageStatus.onLine);
        info.setVersion(assetEntity.getVersion());
//        info.setMd5(md5);
        return info;
    }

    private InputStream openAssetInputStream(String path) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
        } catch (IOException e) {
        }
        if (inputStream == null) {
            return null;
        }
        return inputStream;
    }
}
