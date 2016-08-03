package com.juma.walletpay.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Administrator on 2016/6/7 0007.
 */
public abstract class HDWebViewClient extends WebViewClient {

    private Context context;

    public HDWebViewClient(Context context) {
        this.context = context;
    }

    public abstract void showErrorView();

    /**
     * 拦截web发起的请求
     * @param view
     * @param url
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            String schema = uri.getScheme();
            if (TextUtils.equals(schema, "mailto") || TextUtils.equals(schema, "geo") || TextUtils.equals(schema, "tel")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
                return true;
            }
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        showErrorView();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        showErrorView();
    }
}
