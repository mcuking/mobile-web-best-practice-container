package com.hht.webpackagekit.core;

/**
 * 所有的常量信息都放在此处
 */
public class Constants {
   public static String[] LOCAL_ASSET_LIST = {"main.zip"};

   // 服务器地址
   public static final String BASE_URL = "http://122.51.132.117:5002";

    // package.json请求地址
    public static final String BASE_PACKAGE_INDEX = BASE_URL + "/getPackageIndex?appName=mwbp";

   /***
     * 所有离线包的根目录
     * */
    public static final String PACKAGE_FILE_ROOT_PATH = "offlinepackage";

    /***
     * 配置信息
     * */
    public static final String PACKAGE_FILE_INDEX = "packageIndex.json";
    /***
     * 每个离线包的索引信息文件
     * */
    public static final String RESOURCE_INDEX_NAME = "index.json";

    /**
     * 工作目录
     */
    public static final String PACKAGE_WORK = "work";

    /***
     *
     * 更新临时目录
     * */
    public static final String PACKAGE_UPDATE_TEMP = "update_tmp.zip";

    /***
     *
     * 更新目录
     * */
    public static final String PACKAGE_UPDATE = "update.zip";

    /**
     * 下载文件名称
     */
    public static final String PACKAGE_DOWNLOAD = "download.zip";

    /**
     * merge路径
     */
    public static final String PACKAGE_MERGE = "merge.zip";

    /**
     * 中间路径
     */
    public static final String RESOURCE_MIDDLE_PATH = "package";

    /**
     * asstes文件名称
     */
    public static final String PACKAGE_ASSETS= "package.zip";
}
