package com.elephant.safety;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.elephant.safety.api.ApiClient;
import com.elephant.safety.services.LocationForegroundService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);

        // Delay service start slightly to allow permissions to be granted
        new android.os.Handler().postDelayed(() -> {
            startBackgroundService();
        }, 1000);
    }

    private void startBackgroundService() {
        try {
            Intent serviceIntent = new Intent(this, LocationForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}