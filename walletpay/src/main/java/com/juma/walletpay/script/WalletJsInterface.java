package com.juma.walletpay.script;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.juma.walletpay.JavaWebBridge;
import com.juma.walletpay.PaymentTask;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

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

    private JavaWebBridge mBridge;
    private Object mJsInterface;

    public WalletJsInterface(JavaWebBridge bridge, Object jsInterface) {
        this.mBridge = bridge;
        this.mJsInterface = jsInterface;
    }

    public WalletJsInterface(WebView webView) {
        this.mWebView = webView;
        mHandler = new PayHandler(Looper.getMainLooper());
    }

    /**
     * JS调用Java方法
     * @param methodName    java方法名
     * @param jsonArgs      js传递的参数
     */
    @JavascriptInterface
    public void call(final String methodName, final String jsonArgs) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callJava(methodName, jsonArgs);
            }
        });
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
     * @param jsonData  订单详情信息
     */
    @JavascriptInterface
    public void doPayment(String url, String jsonData) {
        Toast.makeText(mWebView.getContext(), "APP发起请求支付", Toast.LENGTH_SHORT).show();
        new PaymentTask(mWebView.getContext()).execute(url, jsonData);
    }

    @JavascriptInterface
    public void doPayment(String jsonData) {
        doPayment(null, jsonData);
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
     * 调用Java方法
     * @param methodName
     * @param jsonArgs
     * @return
     */
    private Object callJava(String methodName, String jsonArgs) {
        ArgumentsHelper args = new ArgumentsHelper(jsonArgs);
        Method method = getMethodByName(methodName, args);
        Object[] convertedArgs = convertArgs(args, method.getParameterTypes());
        try {
            //反射调用
            return method.invoke(mJsInterface, convertedArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Method call error", e);
        }

    }

    private Object[] convertArgs(ArgumentsHelper args, Class<?>[] parameterTypes) {
        Object[] argsObjects = args.getArgs();
        Object[] convertedArgs = new Object[argsObjects.length];
        for (int i = 0; i < argsObjects.length; i++) {
            convertedArgs[i] = toObject(String.valueOf(argsObjects[i]), parameterTypes[i]);
        }
        return convertedArgs;
    }

    public Method getMethodByName(String methodName, ArgumentsHelper args) {
        ArrayList<Method> methodsForSearch = new ArrayList<>();
        Method[] declaredMethods = mJsInterface.getClass().getDeclaredMethods();

        //获取interface类中的所有方法
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals(methodName)) {
                methodsForSearch.add(declaredMethod);
            }
        }

        //匹配方法参数个数
        for (Iterator<Method> iterator = methodsForSearch.iterator(); iterator.hasNext(); ) {
            Method method = iterator.next();
            if (method.getParameterTypes().length != args.getArgs().length) {
                iterator.remove();
            }
        }

        if (methodsForSearch.size() == 1) {
            return methodsForSearch.get(0);
        } else {
            throw new RuntimeException(String.format("Method '%s' parse error!", methodName));
        }

    }

    public <T> T toObject(String json, Class<T> type) {
        try {
            return new Gson().fromJson(json, type);
        } catch (IllegalStateException e) {
            throw new RuntimeException(String.format("Invalid JSON object: %s", json), e);
        }
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
                    //向JS回调支付结果
                    try {
                        mWebView.loadUrl("javascript:" + mChannelsCallBack + "('" + result + "');");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ARG_PAY_CALL:
                    //向JS回调支付渠道结果
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
