package com.hht.webpackagekit.core;

/**
 * asset资源加载器
 */
public interface AssetResourceLoader {
    /**
     * asset资源路径信息
     * @param path
     */
    PackageInfo load(String path);
}
