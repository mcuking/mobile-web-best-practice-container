package com.hht.webpackagekit;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OfflineWebViewClient extends WebViewClient {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        final String url = request.getUrl().toString();
        WebResourceResponse resourceResponse = getWebResourceResponse(url);
        if (resourceResponse == null) {
            Log.d("WebViewClient", "request from remote , " + url);
            return super.shouldInterceptRequest(view, request);
        }
        Log.d("WebViewClient", "request from local , " + url);
        return resourceResponse;
    }

    /**
     * 从本地命中并返回资源
     * @param url 资源地址
     */
    private WebResourceResponse getWebResourceResponse(String url) {
        try {
            WebResourceResponse resourceResponse = PackageManager.getInstance().getResource(url);
            return resourceResponse;
        } catch (Exception e) {
            Log.e("Error", e.toString());
            e.printStackTrace();
        }
        return null;
    }
}
