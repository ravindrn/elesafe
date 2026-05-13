package com.elephant.safety.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.elephant.safety.R;
import com.elephant.safety.services.LocationForegroundService;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsList = new ArrayList<>();

        // Always needed
        permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsList.add(Manifest.permission.VIBRATE);

        // BACKGROUND LOCATION - CRITICAL for when app is closed!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Foreground service location (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsList.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
        }

        // Full screen intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsList.add(Manifest.permission.USE_FULL_SCREEN_INTENT);
        }

        String[] permissions = permissionsList.toArray(new String[0]);

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            proceedToNextActivity();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if location permissions are granted
            boolean locationGranted = false;
            boolean backgroundLocationGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationGranted = true;
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    backgroundLocationGranted = true;
                }
            }

            if (locationGranted) {
                // If background location not granted, request again with explanation
                if (!backgroundLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Show explanation and request again
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Background Location Required")
                            .setMessage("This app needs background location access to alert you even when the app is closed. Please allow 'Allow all the time' location access.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Request background location again
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_CODE + 1);
                            })
                            .setNegativeButton("Skip", (dialog, which) -> {
                                proceedToNextActivity();
                            })
                            .show();
                } else {
                    proceedToNextActivity();
                }
            } else {
                android.widget.Toast.makeText(this,
                        "Location permission is required for elephant danger alerts",
                        android.widget.Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void proceedToNextActivity() {
        new Handler().postDelayed(() -> {
            if (SharedPrefManager.getInstance(this).isLoggedIn()) {
                // Start background service
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

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}