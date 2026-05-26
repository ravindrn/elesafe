package com.elephant.safety.utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.safety.R;

public class CustomToast {

    public static void showSuccess(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_success, "#4CAF50");
    }

    public static void showError(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_error, "#F44336");
    }

    public static void showWarning(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_warning_alert, "#FF9800");
    }

    public static void showInfo(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_info, "#2196F3");
    }

    public static void showAlert(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_alert_bell, "#FF5722");
    }

    private static void showCustomToast(Context context, String message, int iconRes, String colorHex) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        View icon = layout.findViewById(R.id.toast_icon);
        icon.setBackgroundResource(iconRes);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(32);
        shape.setColor(android.graphics.Color.parseColor(colorHex + "1A")); // 10% opacity
        shape.setStroke(2, android.graphics.Color.parseColor(colorHex));
        layout.setBackground(shape);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();

        // Auto dismiss after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(toast::cancel, 3000);
    }
}