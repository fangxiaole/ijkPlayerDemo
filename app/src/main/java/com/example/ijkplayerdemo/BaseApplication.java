package com.example.ijkplayerdemo;

import android.app.Application;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OKHttpUtil.init("https://cloudapi.qacloud.com.cn");
    }
}
