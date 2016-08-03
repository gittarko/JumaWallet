package com.juma.walletpay.script;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.juma.walletpay.PaymentTask;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Administrator on 2016/8/3 0003.
 */

public class WalletJsInterface {
    private static final int ARG_PAY_CALL = 0x10;
    private static final int ARG_CHANNELS_CALL = 0x11;

    private WebView mWebView;
    private PayHandler mHandler = null;

    //Java回调JS支付的方法名
    private String mPaymentCallBack = null;
    //Java回调JS支付渠道的方法名
    private String mChannelsCallBack = null;

    public WalletJsInterface(WebView webView) {
        this.mWebView = webView;
        mHandler = new PayHandler(Looper.getMainLooper());
    }

    //java回调JS支付结果
    public void execPaymentJavaScript(final String jsString) {
        Message.obtain(mHandler, ARG_PAY_CALL, jsString)
                .sendToTarget();
    }

    //java回调js支付渠道结果
    protected void execChannelsJavascript(final String jsString) {
        Message.obtain(mHandler, ARG_CHANNELS_CALL, jsString)
                .sendToTarget();
    }

    /**
     * @param url  订单接口
     * @param jsonData  订单参数
     */
    @JavascriptInterface
    public void doPayment(String url, String jsonData) {
        new PaymentTask(mWebView.getContext()).execute(url, jsonData);
    }

    @JavascriptInterface
    public void paymentCallBackFunc(String func) {
        mPaymentCallBack = func;
    }

    /**
     * JS获取支付渠道
     * @param url  完整的获取支付渠道接口地址
     */
    @JavascriptInterface
    public void getPayChannels(String url) {
        //创建OkHttpClient对象，用于稍后发起请求
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        //根据Request对象发起Get异步Http请求，并添加请求回调
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, Throwable throwable) {
                //请求失败
                execChannelsJavascript("");
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                String result = response.body().string();
                execChannelsJavascript(result);
            }
        });
    }

    /**
     * java回调JS支付渠道结果
     * @param func  JS方法名
     */
    @JavascriptInterface
    public void payChannelsCallBackFunc(String func) {
        this.mChannelsCallBack = func;
    }

    private  class PayHandler extends Handler {
        public PayHandler(Looper loop) {
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
            int arg = msg.what;
            String result = msg.obj.toString();
            switch (arg) {
                case ARG_CHANNELS_CALL:
                    try {
                        mWebView.loadUrl("javascript:" + mChannelsCallBack + "('" + result + "');");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ARG_PAY_CALL:
                    try {
                        if(TextUtils.isEmpty(mPaymentCallBack))
                            mPaymentCallBack = "paymentCallBack";
                        mWebView.loadUrl("javascript:" + mPaymentCallBack + "('" + result + "');");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
