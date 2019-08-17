package com.mcuking.mwbpcontainer;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.KeyEvent;
import android.view.View;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {
    private WebView myWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();

            // Activity 全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity 顶端布局部分会被状态遮住
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(option);

            window.setStatusBarColor(Color.TRANSPARENT);
        }

        myWebview = (WebView) findViewById(R.id.myWebview);

        myWebview.setWebViewClient((new WebViewClient()));
        myWebview.setWebChromeClient(new WebChromeClient());

        myWebview.getSettings().setJavaScriptEnabled(true);

        myWebview.loadUrl("https://mcuking.github.io/mobile-web-best-practice/");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebview.canGoBack()) {
            myWebview.goBack();
            return true;
        } else {
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
