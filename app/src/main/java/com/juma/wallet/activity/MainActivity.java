package com.juma.wallet.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.juma.wallet.BuildConfig;
import com.juma.wallet.R;
import com.pingplusplus.android.Pingpp;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String JS_INTERFACE = "WalletPay";

    private EditText etUrl;
    private Button btnLoad;
    private WebView webView;

    private JSInterface mJsInterface;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = (EditText)findViewById(R.id.et_url);
        btnLoad = (Button)findViewById(R.id.btn_load);

        btnLoad.setOnClickListener(this);

        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(true);

        if(Build.VERSION.SDK_INT >= 19) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        //添加JS调用JAVA对象
        mJsInterface = new JSInterface(webView);
        webView.addJavascriptInterface(mJsInterface, JS_INTERFACE);
        webView.setWebChromeClient(new WebChromeClient());
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.btn_load) {
            String WEB_URL = getResources().getString(R.string.baseUrl);
            WEB_URL = etUrl.getText().toString().trim();
            if(TextUtils.isEmpty(WEB_URL)) {
                //使用本地测试地址
                WEB_URL = "file:///android_asset/test.html";
            }else {
            }

            webView.loadUrl(WEB_URL);
        }
    }

    private class JSInterface {
        private WebView mWebView;

        private String mPaymentCallBack = null;

        public JSInterface(WebView webView) {
            this.mWebView = webView;
        }

        //java调用js
        protected void execJavaScript(final String jsString) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(TextUtils.isEmpty(mPaymentCallBack))
                            mPaymentCallBack = "paymentCallBack";
                        mWebView.loadUrl("javascript:" + mPaymentCallBack + "('" + jsString + "');");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        //调用支付
        @JavascriptInterface
        public void doPayment(String data) {
            if(BuildConfig.DEBUG) {
                Log.d("charge", "order info:" + data);
            }

            if(data == null) {
                showMsg("请求出错", "请检查URL", "URL无法获取charge信息");
            }else {
                //执行支付
                Pingpp.createPayment(MainActivity.this, data);
            }

//            new PaymentTask(url).execute(new PaymentRequest(channel, amount));
        }

        @JavascriptInterface
        public void paymentCallBackFunc(String func) {
            mPaymentCallBack = func;
        }

    }


    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        private String url = null;

        public PaymentTask(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(PaymentRequest... pr) {

            PaymentRequest paymentRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(paymentRequest);
            try {
                //向Your Ping++ Server SDK请求数据
                data = postJson(url, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        /**
         * 获得服务端的charge，调用ping++ sdk。
         */
        @Override
        protected void onPostExecute(String data) {
            if(null == data){
                showMsg("请求出错", "请检查URL", "URL无法获取charge");
                return;
            }
            Log.d("charge", data);
            Pingpp.createPayment(MainActivity.this, data);
        }

    }

    class PaymentRequest {
        String channel;
        double amount;

        public PaymentRequest(String channel, double amount) {
            this.channel = channel;
            this.amount = amount;
        }
    }

    //获取charge信息数据
    private static String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).post(body).build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //支付页面返回处理
        if (requestCode == Pingpp.REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息

                showMsg(result, errorMsg, extraMsg);
                //回调JS，将支付结果通知给web;传入js方法名和结果参数
                if(TextUtils.isEmpty(errorMsg) && TextUtils.isEmpty(extraMsg)) {
                    Log.d("Charge", "payment info:" + result);
                    //API调用成功
                    mJsInterface.execJavaScript(result);
                }else {
                    Log.d("Charge", "payment info:" + errorMsg + ", " + extraMsg);
                    //API调用失败
                    mJsInterface.execJavaScript("fail");
                }
            }
        }
    }

    //信息提示
    public void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null !=msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null !=msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(str);
        builder.setTitle("APP提示");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.getSettings().setJavaScriptEnabled(false);
            webView.loadDataWithBaseURL("about:blank", "<html></html>", "text/html", "UTF-8", null);
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
