package com.elephant.safety.services;

import android.content.Context;

public abstract class AlertService implements AlertInterface {
    protected Context context;

    public AlertService(Context context) {
        this.context = context;
    }

    public abstract void logAlertToServer(long userId, long zoneId, String alertType);

    public final void processAlert(String zoneName, String riskLevel) {
        String message = generateAlertMessage(zoneName, riskLevel);
        triggerFullAlert(message);
        logAlertToServer(0, 0, "ENTERING_ZONE");
    }

    protected abstract String generateAlertMessage(String zoneName, String riskLevel);
}