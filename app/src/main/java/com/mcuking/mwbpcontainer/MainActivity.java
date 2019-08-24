package com.mcuking.mwbpcontainer;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.view.KeyEvent;
import android.view.View;
import android.graphics.Color;

import com.mcuking.mwbpcontainer.network.JsApi;

import wendu.dsbridge.DWebView;

public class MainActivity extends AppCompatActivity {
    private DWebView mWebview;

    private WebSettings mWebSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Window window = getWindow();

        // Activity 全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity 顶端布局部分会被状态遮住
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        window.getDecorView().setSystemUiVisibility(option);

        window.setStatusBarColor(Color.TRANSPARENT);

        // 请求获取日历权限
        requestPermission();

        mWebview = (DWebView) findViewById(R.id.mWebview);

        // 向 js 环境注入 ds_bridge
        mWebview.addJavascriptObject(new JsApi(), null);

        // 可复写 WebviewClient 类
        mWebview.setWebViewClient((new WebViewClient()));

        // 可复写 WebviewChromeClient
        mWebview.setWebChromeClient(new WebChromeClient());

        mWebSettings = mWebview.getSettings();

        // 如果访问的页面中要与Javascript交互，则WebView必须设置支持Javascript
        mWebSettings.setJavaScriptEnabled(true);

        // 开启DOM缓存
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setDatabaseEnabled(true);

        // 使用WebView中缓存
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);


        // 修改 UA 以便区分 h5 环境
        mWebSettings.setUserAgentString(
                mWebSettings.getUserAgentString() + " " + getString(R.string.user_agent_suffix)
        );

        mWebview.loadUrl("https://mcuking.github.io/mobile-web-best-practice");
    }

    // 复写安卓返回事件 转为响应 h5 返回
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        } else {
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // 请求获取日历权限
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR
        }, 0);
    }
}
