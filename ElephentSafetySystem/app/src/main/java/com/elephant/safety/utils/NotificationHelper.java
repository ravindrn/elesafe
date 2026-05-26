package com.elephant.safety.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.elephant.safety.R;
import com.elephant.safety.activities.DangerAlertActivity;

public class NotificationHelper {

    private static final String DANGER_CHANNEL_ID = "danger_alerts_channel";
    private static final String DANGER_CHANNEL_NAME = "Elephant Danger Alerts";
    private static final int DANGER_NOTIFICATION_ID = 2001;

    public static void createDangerNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    DANGER_CHANNEL_ID,
                    DANGER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Critical alerts for elephant danger zones");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000, 500, 2000});
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarmSound == null) {
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
                channel.setSound(alarmSound, audioAttributes);
            }

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Original method - 4 parameters
    public static void showEmergencyAlert(Context context, String zoneName, String riskLevel, double distance) {
        showEmergencyAlert(context, zoneName, riskLevel, distance, -1);
    }

    // New method - 5 parameters (with zoneId)
    public static void showEmergencyAlert(Context context, String zoneName, String riskLevel, double distance, long zoneId) {
        createDangerNotificationChannel(context);

        String distanceText;
        if (distance < 1000) {
            distanceText = String.format("%.0f meters", distance);
        } else {
            distanceText = String.format("%.2f km", distance / 1000);
        }

        String title = "🚨 DANGER ZONE! 🚨";
        String message = String.format("Entering: %s\nRisk: %s\nDistance: %s\n\nTap for immediate instructions!",
                zoneName, riskLevel, distanceText);

        Intent intent = new Intent(context, DangerAlertActivity.class);
        intent.putExtra("zone_name", zoneName);
        intent.putExtra("risk_level", riskLevel);
        intent.putExtra("distance", distanceText);
        intent.putExtra("zone_id", zoneId);
        intent.putExtra("from_notification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) zoneId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DANGER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_danger_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
                .setVibrate(new long[]{0, 1000, 500, 1000, 500, 2000});

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(DANGER_NOTIFICATION_ID + 2, builder.build());

        vibrate(context);
    }

    public static void showDangerNotification(Context context, String title, String message, PendingIntent intent) {
        createDangerNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DANGER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_danger_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setFullScreenIntent(intent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(DANGER_NOTIFICATION_ID, builder.build());

        vibrate(context);
    }

    public static void showDangerNotificationWithSound(Context context, String title, String message) {
        createDangerNotificationChannel(context);

        Intent intent = new Intent(context, DangerAlertActivity.class);
        intent.putExtra("from_notification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DANGER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_danger_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(DANGER_NOTIFICATION_ID + 1, builder.build());

        vibrate(context);
    }

    private static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                        new long[]{0, 1000, 500, 1000, 500, 2000}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 1000, 500, 1000, 500, 2000}, 0);
            }
        }
    }
}