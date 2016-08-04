package com.juma.walletpay;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pingplusplus.android.Pingpp;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 2016/8/3 0003.
 */

public class PaymentTask extends AsyncTask<String, Void, String> {
    private Context context;

    public PaymentTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String[] pr) {
        String data = null;
        String url = pr[0];
        String json = pr[1];

        if(TextUtils.isEmpty(url)) {
            //没有地址的支付直接调用ping__ sdk发起支付
            data = json;
        }else {
            try {
                //向Your Ping++ Server SDK请求数据
                data = postJson(url, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String data) {
        if(null == data){
            Toast.makeText(context, "请求出错,请检查Url", Toast.LENGTH_SHORT).show();
            return;
        }

        //调用ping++ sdk发起支付
        Pingpp.createPayment((Activity)context, data);

    }

    //获取charge信息数据
    private static String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).post(body).build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        try {
            JSONObject jsonObject = new JSONObject(result);
            //取出data信息
            return jsonObject.getString("data");
        }catch(JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
