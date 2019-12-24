package com.hht.webpackagekit.core;

/**
 * 离线包下载器
 */
public interface Downloader {
    /***
     * 离线包下载
     * */
    void  download(PackageInfo packageInfo, DownloadCallback callback);

    interface DownloadCallback {
        void onSuccess(String packageId);

        void onFailure(String packageID);
    }
}
