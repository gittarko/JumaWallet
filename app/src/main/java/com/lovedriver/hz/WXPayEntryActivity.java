package com.lovedriver.hz;

import android.app.Activity;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * Created by Administrator on 2016/8/3 0003.
 */

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    @Override
    public void onReq(BaseReq baseReq) {
        Toast.makeText(WXPayEntryActivity.this, "请求微信支付", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Toast.makeText(WXPayEntryActivity.this, "微信支付结果:" + baseResp.errCode, Toast.LENGTH_SHORT).show();
    }
}
