package com.elephant.safety.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.elephant.safety.R;

public class CustomAlertDialog {

    public static void showSuccess(Activity activity, String title, String message) {
        showCustomDialog(activity, title, message, R.drawable.ic_success_circle, "#4CAF50");
    }

    public static void showError(Activity activity, String title, String message) {
        showCustomDialog(activity, title, message, R.drawable.ic_error_circle, "#F44336");
    }

    public static void showWarning(Activity activity, String title, String message) {
        showCustomDialog(activity, title, message, R.drawable.ic_warning_circle, "#FF9800");
    }

    public static void showInfo(Activity activity, String title, String message) {
        showCustomDialog(activity, title, message, R.drawable.ic_info_circle, "#2196F3");
    }

    public static void showDangerZoneAlert(Activity activity, String zoneName, String riskLevel, String distance) {
        String title = "⚠️ DANGER ZONE!";
        String message = String.format("Entering: %s\nRisk: %s\nDistance: %s\n\nReduce speed and stay alert!",
                zoneName, riskLevel, distance);
        showCustomDialog(activity, title, message, R.drawable.ic_danger_alert, "#FF4B4B");
    }

    public static void showLoginSuccess(Activity activity, String userName) {
        showCustomDialog(activity,
                "Welcome " + userName + "! 🐘",
                "Successfully logged in.\nStay safe on the roads!",
                R.drawable.ic_success_circle, "#4CAF50");
    }

    public static void showRegistrationSuccess(Activity activity) {
        showCustomDialog(activity,
                "Registration Successful! 🎉",
                "Your account has been created.\nPlease login to continue.",
                R.drawable.ic_success_circle, "#4CAF50");
    }

    public static void showReportSubmitted(Activity activity) {
        showCustomDialog(activity,
                "Report Submitted! 🙏",
                "Thank you for reporting.\nYour report will help other drivers stay safe.",
                R.drawable.ic_success_circle, "#4CAF50");
    }

    private static void showCustomDialog(Activity activity, String title, String message,
                                         int iconRes, String colorHex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);

        ImageView iconView = dialogView.findViewById(R.id.dialog_icon);
        iconView.setImageResource(iconRes);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText(title);
        titleView.setTextColor(android.graphics.Color.parseColor(colorHex));

        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        messageView.setText(message);

        Button actionButton = dialogView.findViewById(R.id.dialog_button);
        actionButton.setText("OK");
        actionButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(colorHex)));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialogView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.dialog_slide_in));

        actionButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.button_click));
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void showDangerZoneAlert(Activity activity, String zoneName, String riskLevel, double distanceKm) {
        String distanceText;
        if (distanceKm < 1) {
            distanceText = String.format("%.0f meters", distanceKm * 1000);
        } else {
            distanceText = String.format("%.2f km", distanceKm);
        }

        String title = "⚠️ DANGER ZONE ENTERED! ⚠️";
        String message = String.format(
                "You are entering:\n📍 %s\n⚠️ Risk Level: %s\n📏 Distance to zone: %s\n\n🚨 IMMEDIATE ACTION REQUIRED:\n• Reduce speed immediately\n• Stay extremely alert\n• Watch for elephants crossing\n• Do not use horn\n• Be ready to stop\n• Keep headlights on",
                zoneName, riskLevel, distanceText
        );

        showCustomDialog(activity, title, message, R.drawable.ic_danger_alert, "#FF4B4B");
    }
}