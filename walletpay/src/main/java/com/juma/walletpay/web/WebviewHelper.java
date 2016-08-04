package com.juma.walletpay.web;

import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/5/25 0025.
 */
public final class WebviewHelper {

    private static final String USER_AGENT = "Juma";

    public static void configWebView(WebView webView) {
        if (webView == null) {
            return;
        }
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setSaveFormData(true);
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setDomStorageEnabled(true);
//        webSettings.setSupportMultipleWindows(false);

        //启用数据库
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.setDatabasePath("/data/data/" + webView.getContext().getPackageName() + "/databases/");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Class<?> clazz = webView.getSettings().getClass();
            try {
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(webView.getSettings(), true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //webview localstorage
        webSettings.setDomStorageEnabled(true);

        //cache
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(webView.getContext().getCacheDir().getAbsolutePath());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 10);
        webSettings.setAllowFileAccess(true);

        //agent
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + " " + USER_AGENT);

        //位置
        String dir = webView.getContext().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationDatabasePath(dir);

        webSettings.setGeolocationEnabled(true);

        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(true);
    }
}
