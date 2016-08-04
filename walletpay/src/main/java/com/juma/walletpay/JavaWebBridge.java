package com.juma.walletpay;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.juma.walletpay.script.WalletJsInterface;
import com.juma.walletpay.web.WebviewHelper;

/**
 * Created by Administrator on 2016/8/3 0003.
 */

public class JavaWebBridge {
    private WebView mWebView;

    public JavaWebBridge(WebView webView) {
        this.mWebView = webView;
        //初始化webView基本属性
        WebviewHelper.configWebView(webView);
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void init() {

            }

        }, "JavaWebBridge");
    }

    /**
     * 向JS注入Java方法
     * @param tag
     * @param obj
     */
    public void addJsInterface(String tag, Object obj) {
        this.mWebView.addJavascriptInterface(new WalletJsInterface(this, obj), tag);
    }

    /**
     * 取消向JS注入的Java方法
     * @param tag
     */
    public void removeJsInterface(String tag) {
        this.mWebView.removeJavascriptInterface(tag);
    }

    public WebView getWebView() {
        return mWebView;
    }


}

