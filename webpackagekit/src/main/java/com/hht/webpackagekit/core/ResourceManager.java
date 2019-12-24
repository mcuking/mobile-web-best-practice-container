package com.hht.webpackagekit.core;

import android.webkit.WebResourceResponse;

/**
 * 资源管理器
 */
public interface ResourceManager {
    WebResourceResponse getResource(String url);

    boolean updateResource(String packageId, String version);

    void setResourceValidator(ResoureceValidator validator);

    String getPackageId(String url);
}
