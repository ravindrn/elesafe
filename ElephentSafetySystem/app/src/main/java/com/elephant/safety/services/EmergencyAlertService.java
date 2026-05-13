package com.elephant.safety.services;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Toast;

import com.elephant.safety.R;
import com.elephant.safety.activities.DangerAlertActivity;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;

public class EmergencyAlertService extends AlertService {
    private Vibrator vibrator;
    private ApiService apiService;
    private MediaPlayer mediaPlayer;

    public EmergencyAlertService(Context context) {
        super(context);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
    }

    @Override
    public void showSoundAlert() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
            }
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showVisualAlert(String message) {
        // For danger zone alerts, launch full-screen activity
        // The message contains zone info, but we'll extract it or just show the full screen alert

        // Launch the full-screen danger alert activity
        Intent intent = new Intent(context, DangerAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Parse message to extract zone info if needed
        // For now, we'll let the activity handle it or pass generic info
        intent.putExtra("zone_name", "Elephant Danger Zone");
        intent.putExtra("risk_level", "HIGH");
        intent.putExtra("distance", "Approaching");

        context.startActivity(intent);
    }

    public void showDetailedAlert(String zoneName, String riskLevel, double distanceKm) {
        Intent intent = new Intent(context, DangerAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("zone_name", zoneName);
        intent.putExtra("risk_level", riskLevel);

        String distanceText;
        if (distanceKm < 1) {
            distanceText = String.format("%.0f meters", distanceKm * 1000);
        } else {
            distanceText = String.format("%.2f km", distanceKm);
        }
        intent.putExtra("distance", distanceText);

        String instructions = "• REDUCE SPEED IMMEDIATELY\n" +
                "• STAY EXTREMELY ALERT\n" +
                "• WATCH FOR ELEPHANTS CROSSING\n" +
                "• DO NOT USE HORN\n" +
                "• DO NOT FLASH HEADLIGHTS\n" +
                "• MAINTAIN SAFE DISTANCE\n" +
                "• BE PREPARED TO STOP";
        intent.putExtra("instructions", instructions);

        context.startActivity(intent);
    }

    @Override
    public void showVibrationAlert() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 500, 1000, 500, 2000};
            vibrator.vibrate(pattern, -1);
        }
    }

    @Override
    public void sendPushNotification(String title, String message) {
        Toast.makeText(context, title + ": " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFullAlert(String message) {
        showSoundAlert();
        showVisualAlert(message);
        showVibrationAlert();
        sendPushNotification("Elephant Safety Alert", message);
    }

    @Override
    public void logAlertToServer(long userId, long zoneId, String alertType) {
        // Send alert log to backend
    }

    @Override
    protected String generateAlertMessage(String zoneName, String riskLevel) {
        return String.format(
                "Zone: %s\nRisk Level: %s\n\nInstructions:\n• Reduce speed immediately\n• Stay alert for elephants\n• Do not use horn\n• Watch both sides of road\n• Be prepared to stop",
                zoneName, riskLevel
        );
    }

    public void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}