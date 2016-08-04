package com.lovedriver.hz;

import android.app.Application;

/**
 * Created by Administrator on 2016/8/3 0003.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
