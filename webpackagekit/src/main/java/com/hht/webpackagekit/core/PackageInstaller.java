package com.hht.webpackagekit.core;

/**
 * 离线包安装器
 * 1、解压离线包到update下，如果存在update包，重命名update为update_temp，然后解压到update,如果解压失败，再讲update_temp重新命名为update,最后删除update_temp
 * 2、更新current包
 * 3、提取index.json中的内容到ResourceManager中
 */
public interface PackageInstaller {
    boolean install(PackageInfo packageInfo, boolean isAssets);
}
