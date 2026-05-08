package com.elephant.safety;

import android.app.Application;

import com.elephant.safety.api.ApiClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
}