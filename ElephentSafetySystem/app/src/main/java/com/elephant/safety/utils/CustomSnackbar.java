package com.elephant.safety.utils;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.elephant.safety.R;

public class CustomSnackbar {

    public static void showSuccess(View view, String message) {
        showSnackbar(view, message, R.drawable.ic_success, "#4CAF50");
    }

    public static void showError(View view, String message) {
        showSnackbar(view, message, R.drawable.ic_error, "#F44336");
    }

    public static void showWarning(View view, String message) {
        showSnackbar(view, message, R.drawable.ic_warning_alert, "#FF9800");
    }

    public static void showInfo(View view, String message) {
        showSnackbar(view, message, R.drawable.ic_info, "#2196F3");
    }

    private static void showSnackbar(View view, String message, int iconRes, String colorHex) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(android.graphics.Color.parseColor(colorHex + "1A"));

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(android.graphics.Color.parseColor(colorHex));
        textView.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        textView.setCompoundDrawablePadding(16);

        snackbar.show();
    }
}