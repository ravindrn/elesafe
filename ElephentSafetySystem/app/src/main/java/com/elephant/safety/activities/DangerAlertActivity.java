package com.elephant.safety.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.elephant.safety.R;

public class DangerAlertActivity extends AppCompatActivity {

    private TextView tvZoneName, tvRiskLevel, tvDistance, tvInstructions;
    private Button btnAcknowledge;
    private View blinkView;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Handler handler = new Handler();
    private String zoneName;
    private String riskLevel;
    private String distance;
    private long zoneId;
    private static final int AUTO_DISMISS_SECONDS = 15;
    private boolean isFinishingFlag = false;

    // Broadcast receiver to stop alert when service says user exited
    private final BroadcastReceiver stopAlertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("STOP_ALERT".equals(intent.getAction())) {
                long receivedZoneId = intent.getLongExtra("zone_id", -1);
                android.util.Log.e("DangerAlert", "Received STOP_ALERT for zone: " + receivedZoneId + ", current zone: " + zoneId);

                if (receivedZoneId == -1 || receivedZoneId == zoneId) {
                    android.util.Log.e("DangerAlert", "Stopping alert - User exited danger zone!");
                    stopAll();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_danger_alert);

        // Get data from intent
        zoneName = getIntent().getStringExtra("zone_name");
        riskLevel = getIntent().getStringExtra("risk_level");
        distance = getIntent().getStringExtra("distance");
        zoneId = getIntent().getLongExtra("zone_id", -1);

        // Initialize views
        tvZoneName = findViewById(R.id.tvZoneName);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvDistance = findViewById(R.id.tvDistance);
        tvInstructions = findViewById(R.id.tvInstructions);
        btnAcknowledge = findViewById(R.id.btnAcknowledge);
        blinkView = findViewById(R.id.blinkView);

        // Set data
        tvZoneName.setText(zoneName != null ? zoneName : "Unknown Danger Zone");
        tvRiskLevel.setText(riskLevel != null ? riskLevel : "HIGH");
        tvDistance.setText(distance != null ? distance : "Approaching");

        // Set instructions based on risk level
        String instructionsText;
        if (riskLevel != null && riskLevel.equals("CRITICAL")) {
            instructionsText = "🚨 CRITICAL EMERGENCY 🚨\n\n" +
                    "• STOP IMMEDIATELY if safe\n" +
                    "• DO NOT PROCEED\n" +
                    "• Wait for elephant to pass\n" +
                    "• Turn off engine\n" +
                    "• Stay inside vehicle\n" +
                    "• Call emergency if needed";
        } else if (riskLevel != null && riskLevel.equals("HIGH")) {
            instructionsText = "⚠️ HIGH RISK ZONE ⚠️\n\n" +
                    "• REDUCE SPEED IMMEDIATELY\n" +
                    "• STAY EXTREMELY ALERT\n" +
                    "• WATCH FOR ELEPHANTS CROSSING\n" +
                    "• DO NOT USE HORN\n" +
                    "• DO NOT FLASH HEADLIGHTS\n" +
                    "• MAINTAIN SAFE DISTANCE\n" +
                    "• BE PREPARED TO STOP";
        } else {
            instructionsText = "⚠️ DANGER ZONE ⚠️\n\n" +
                    "• REDUCE SPEED\n" +
                    "• STAY ALERT\n" +
                    "• WATCH FOR ELEPHANTS\n" +
                    "• DO NOT USE HORN\n" +
                    "• BE PREPARED TO STOP";
        }
        tvInstructions.setText(instructionsText);

        // Set risk level color
        int riskColor;
        switch (riskLevel != null ? riskLevel : "HIGH") {
            case "CRITICAL":
                riskColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
                break;
            case "HIGH":
                riskColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
                break;
            case "MEDIUM":
                riskColor = ContextCompat.getColor(this, android.R.color.holo_orange_light);
                break;
            default:
                riskColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        tvRiskLevel.setTextColor(riskColor);

        // Register broadcast receiver with proper flags for Android 14+
        IntentFilter filter = new IntentFilter("STOP_ALERT");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires RECEIVER_NOT_EXPORTED for internal broadcasts
            registerReceiver(stopAlertReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stopAlertReceiver, filter);
        }

        android.util.Log.e("DangerAlert", "STOP_ALERT receiver registered");

        // Start animations and alerts
        startBlinkingAnimation();
        playWarningSound();
        startVibration();

        // Acknowledge button
        btnAcknowledge.setOnClickListener(v -> {
            stopAll();

            // Send broadcast that user acknowledged the alert
            Intent stopIntent = new Intent("STOP_ALERT");
            stopIntent.putExtra("zone_id", zoneId);
            sendBroadcast(stopIntent);
            android.util.Log.e("DangerAlert", "User acknowledged alert for zone: " + zoneId);

            // Send broadcast to reset service state
            Intent resetIntent = new Intent("RESET_ZONE_STATUS");
            sendBroadcast(resetIntent);

            // Go to main activity
            Intent intent = new Intent(DangerAlertActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Auto dismiss after 15 seconds
        handler.postDelayed(() -> {
            if (!isFinishing() && !isFinishingFlag) {
                stopAll();

                Intent stopIntent = new Intent("STOP_ALERT");
                stopIntent.putExtra("zone_id", zoneId);
                sendBroadcast(stopIntent);

                Intent intent = new Intent(DangerAlertActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }, AUTO_DISMISS_SECONDS * 1000);
    }

    @Override
    public void onBackPressed() {
        stopAll();

        Intent stopIntent = new Intent("STOP_ALERT");
        stopIntent.putExtra("zone_id", zoneId);
        sendBroadcast(stopIntent);

        Intent intent = new Intent(DangerAlertActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private void startBlinkingAnimation() {
        if (blinkView != null) {
            ObjectAnimator colorAnim = ObjectAnimator.ofObject(blinkView, "backgroundColor",
                    new ArgbEvaluator(),
                    ContextCompat.getColor(this, android.R.color.holo_red_dark),
                    ContextCompat.getColor(this, android.R.color.holo_red_light),
                    ContextCompat.getColor(this, android.R.color.holo_red_dark));
            colorAnim.setDuration(500);
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.start();
        }
    }

    private void playWarningSound() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
            } else {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            android.util.Log.e("DangerAlert", "Error playing sound: " + e.getMessage());
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 800, 500, 800, 500, 800, 500, 1000};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                long[] pattern = {0, 800, 500, 800, 500, 800, 500, 1000};
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopAll() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }

        handler.removeCallbacksAndMessages(null);
        isFinishingFlag = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        try {
            unregisterReceiver(stopAlertReceiver);
            android.util.Log.e("DangerAlert", "STOP_ALERT receiver unregistered");
        } catch (Exception e) {
            android.util.Log.e("DangerAlert", "Error unregistering receiver: " + e.getMessage());
        }
        stopAll();
    }
}