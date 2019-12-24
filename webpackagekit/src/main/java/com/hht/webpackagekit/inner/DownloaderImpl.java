package com.hht.webpackagekit.inner;

import android.content.Context;
import android.util.Log;

import com.hht.webpackagekit.core.Constants;
import com.hht.webpackagekit.core.Downloader;
import com.hht.webpackagekit.core.PackageInfo;
import com.hht.webpackagekit.core.util.FileUtils;
import com.hht.webpackagekit.core.util.Logger;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

/**
 * 下载器实现类
 */
public class DownloaderImpl implements Downloader {
    private Context context;
    private String downloadUrl;

    public DownloaderImpl(Context context) {
        this.context = context;
    }

    //根据PackageInfo的downloadUrl下载离线包到PackageDownloadName: ${root}/${packageId}/${version}/download.zip
    @Override
    public void download(PackageInfo packageInfo, final DownloadCallback callback) {
        downloadUrl = Constants.BASE_URL + packageInfo.getDownloadUrl();
        Log.d("download", downloadUrl);

        BaseDownloadTask downloadTask = FileDownloader.getImpl()
            .create(downloadUrl)
            .setTag(packageInfo.getPackageId())
            .setPath(FileUtils.getPackageDownloadName(context, packageInfo.getPackageId(), packageInfo.getVersion()))
            .setListener(new FileDownloadSampleListener() {
                @Override
                protected void completed(BaseDownloadTask task) {
                    super.completed(task);
                    if (callback != null && task.getStatus() == FileDownloadStatus.completed) {
                        callback.onSuccess((String) task.getTag());
                    } else if (callback != null) {
                        callback.onFailure((String) task.getTag());
                    }
                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                    super.error(task, e);
                    Logger.e("packageResource download error: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure((String) task.getTag());
                    }
                }
            });
        downloadTask.start();
    }
}
