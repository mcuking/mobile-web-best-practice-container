package com.mcuking.mwbpcontainer;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.view.KeyEvent;
import android.view.View;
import android.graphics.Color;
import com.hht.webpackagekit.OfflineWebViewClient;

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

        // 复写 WebviewClient 类
        mWebview.setWebViewClient(new OfflineWebViewClient());

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

        // 获取 app 版本
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo = null;
        try {
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            packInfo = packageManager.getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersion = packInfo.versionName;

        // 获取系统版本
        String systemVersion = android.os.Build.VERSION.RELEASE;

        mWebSettings.setUserAgentString(
                mWebSettings.getUserAgentString() + " DSBRIDGE_"  + appVersion + "_" + systemVersion + "_android"
        );

        mWebview.loadUrl("http://www.mcuking.club/");
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
