package com.elephant.safety.services;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;

public class EmergencyAlertService extends AlertService {
    private Vibrator vibrator;
    private ApiService apiService;
    private ToneGenerator toneGenerator;

    public EmergencyAlertService(Context context) {
        super(context);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.apiService = ApiClient.getClient().create(ApiService.class);

        try {
            // Fix: Use AudioManager.STREAM_ALARM instead
            this.toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showSoundAlert() {
        if (toneGenerator != null) {
            try {
                // Play a loud alarm sound
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showVisualAlert(String message) {
        new AlertDialog.Builder(context)
                .setTitle("⚠️ DANGER ZONE ALERT ⚠️")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("ACKNOWLEDGE", (dialog, which) -> {
                    stopSound();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
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
        // TODO: Send alert log to backend
    }

    @Override
    protected String generateAlertMessage(String zoneName, String riskLevel) {
        return String.format(
                "⚠️ ELEPHANT CROSSING ZONE ⚠️\n\n" +
                        "Zone: %s\n" +
                        "Risk Level: %s\n\n" +
                        "Instructions:\n" +
                        "• Reduce speed immediately\n" +
                        "• Stay alert for elephants\n" +
                        "• Do not use horn\n" +
                        "• Watch both sides of road\n" +
                        "• Be prepared to stop",
                zoneName, riskLevel
        );
    }

    public void stopSound() {
        if (toneGenerator != null) {
            toneGenerator.stopTone();
        }
    }
}