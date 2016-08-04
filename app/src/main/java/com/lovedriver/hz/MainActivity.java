package com.lovedriver.hz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.juma.walletpay.script.WalletJsInterface;
import com.juma.walletpay.web.HDWebChromeClient;
import com.juma.walletpay.web.HDWebViewClient;
import com.juma.walletpay.web.WebviewHelper;
import com.pingplusplus.android.Pingpp;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String JS_INTERFACE = "WalletPay";

    private EditText etUrl;
    private Button btnLoad;
    private WebView webView;

    private WalletJsInterface mJsInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = (EditText)findViewById(R.id.et_url);
        etUrl.setText("http://10.101.0.105:8080/forward/customer/order/order.detail.html?waybillId=256&status=4&scrollTop=0&statusId=&page=1");
        btnLoad = (Button)findViewById(R.id.btn_load);

        btnLoad.setOnClickListener(this);

        webView = (WebView)findViewById(R.id.webView);
        initWebView(webView);
    }

    private void initWebView(WebView webView) {
        if(Build.VERSION.SDK_INT >= 19) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new HDWebViewClient(this) {
            @Override
            public void showErrorView() {
                Toast.makeText(MainActivity.this, "发生错误", Toast.LENGTH_SHORT).show();
            }
        });
        webView.setWebChromeClient(new HDWebChromeClient(MainActivity.this));
        //设置webview属性
        WebviewHelper.configWebView(webView);
        //添加JS调用JAVA对象
        mJsInterface = new WalletJsInterface(webView);
        webView.addJavascriptInterface(mJsInterface, JS_INTERFACE);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //支付页面返回处理
        if (requestCode == Pingpp.REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, "支付回调", Toast.LENGTH_SHORT).show();
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息

                //回调JS，将支付结果通知给web;传入js方法名和结果参数
                if(TextUtils.isEmpty(errorMsg) && TextUtils.isEmpty(extraMsg)) {
                    Log.d("Charge", "payment info:" + result);
                    //API调用成功
                    mJsInterface.execPaymentJavaScript(result);
                }else {
                    Log.d("Charge", "payment info:" + errorMsg + ", " + extraMsg);
                    //API调用失败
                    mJsInterface.execPaymentJavaScript("fail");
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
