package com.elephant.safety.services;

public interface AlertInterface {
    void showSoundAlert();
    void showVisualAlert(String message);
    void showVibrationAlert();
    void sendPushNotification(String title, String message);
    void triggerFullAlert(String message);
}