package com.elephant.safety.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.elephant.safety.R;
import com.elephant.safety.activities.MainActivity;
import com.elephant.safety.models.DangerZone;
import com.elephant.safety.utils.HaversineFormula;
import com.elephant.safety.utils.NotificationHelper;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationForegroundService extends Service {

    private static final String CHANNEL_ID = "location_foreground_channel";
    private static final int NOTIFICATION_ID = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private List<DangerZone> dangerZones;

    // Track alert states for each zone
    private Map<Long, Boolean> userInsideZone;      // Current status (true = inside zone)
    private Map<Long, Long> lastAlertTime;          // Last alert time for cooldown
    private Map<Long, Boolean> activeAlertForZone;  // Is alert currently playing for this zone

    private static final long ALERT_COOLDOWN_MS = 30000; // 30 seconds cooldown
    private boolean wasInAnyDangerZone = false;
    private boolean alertInProgress = false;
    private Long currentAlertZoneId = null;

    // Store last known location to detect changes
    private double lastLat = 0;
    private double lastLon = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        android.util.Log.e("LocationService", "=== SERVICE CREATED ===");

        createNotificationChannel();

        try {
            startForeground(NOTIFICATION_ID, getNotification("🛡️ ElephAlert Active"));
            android.util.Log.e("LocationService", "Foreground service started");
        } catch (Exception e) {
            android.util.Log.e("LocationService", "Failed to start foreground: " + e.getMessage());
            stopSelf();
            return;
        }

        // Register broadcast receivers
        IntentFilter resetFilter = new IntentFilter("RESET_ZONE_STATUS");
        IntentFilter stopAlertFilter = new IntentFilter("STOP_ALERT");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(resetReceiver, resetFilter, Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(stopAlertReceiver, stopAlertFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(resetReceiver, resetFilter);
            registerReceiver(stopAlertReceiver, stopAlertFilter);
        }

        android.util.Log.e("LocationService", "Broadcast receivers registered");

        initializeService();
    }

    private void initializeService() {
        android.util.Log.e("LocationService", "=== INITIALIZING SERVICE ===");

        // Initialize tracking maps
        userInsideZone = new HashMap<>();
        lastAlertTime = new HashMap<>();
        activeAlertForZone = new HashMap<>();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("LocationService", "LOCATION PERMISSION NOT GRANTED!");
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dangerZones = new ArrayList<>();

        loadDangerZones();
        initLocationCallback();
        startLocationUpdates();

        android.util.Log.e("LocationService", "Service initialized successfully");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ElephAlert Protection",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Real-time monitoring of elephant danger zones");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification getNotification(String content) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🐘 ElephAlert Active")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_elephant)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void loadDangerZones() {
        dangerZones = SharedPrefManager.getInstance(this).getDangerZones();
        if (dangerZones == null || dangerZones.isEmpty()) {
            android.util.Log.e("LocationService", "No danger zones loaded! Open the app first to cache zones.");
        } else {
            android.util.Log.e("LocationService", "Loaded " + dangerZones.size() + " danger zones");

            for (DangerZone zone : dangerZones) {
                userInsideZone.put(zone.getId(), false);
                lastAlertTime.put(zone.getId(), 0L);
                activeAlertForZone.put(zone.getId(), false);
            }
        }
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    double newLat = location.getLatitude();
                    double newLon = location.getLongitude();

                    double distance = HaversineFormula.calculateDistance(lastLat, lastLon, newLat, newLon);
                    if (distance > 5 || lastLat == 0) {
                        lastLat = newLat;
                        lastLon = newLon;
                        android.util.Log.e("LocationService", "📍 Location update: " + newLat + ", " + newLon);
                        checkAllDangerZones(newLat, newLon);
                    }
                }
            }
        };
    }

    private void cancelDangerNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Cancel all danger notifications (IDs from NotificationHelper)
        manager.cancel(2001);
        manager.cancel(2002);
        manager.cancel(2003);
        android.util.Log.e("LocationService", "Danger notifications cancelled");
    }

    private void checkAllDangerZones(double userLat, double userLon) {
        if (dangerZones == null || dangerZones.isEmpty()) {
            return;
        }

        boolean currentlyInAnyZone = false;
        long currentTime = System.currentTimeMillis();

        for (DangerZone zone : dangerZones) {
            double distance = HaversineFormula.calculateDistance(
                    userLat, userLon,
                    zone.getLatitude(), zone.getLongitude()
            );

            boolean isInside = distance <= zone.getRadius();
            Long zoneId = zone.getId();

            if (isInside) {
                currentlyInAnyZone = true;

                Boolean wasInside = userInsideZone.get(zoneId);
                boolean wasPreviouslyOutside = (wasInside == null || !wasInside);

                if (wasPreviouslyOutside) {
                    android.util.Log.e("LocationService", "🚨 NEW ENTRY to zone: " + zone.getZoneName());

                    long lastAlert = lastAlertTime.getOrDefault(zoneId, 0L);
                    long timeSinceLastAlert = currentTime - lastAlert;

                    if (timeSinceLastAlert > ALERT_COOLDOWN_MS) {
                        sendInstantAlert(zone, distance);
                        lastAlertTime.put(zoneId, currentTime);
                        activeAlertForZone.put(zoneId, true);
                        currentAlertZoneId = zoneId;
                        android.util.Log.e("LocationService", "✅ Alert sent for: " + zone.getZoneName());
                    } else {
                        long secondsRemaining = (ALERT_COOLDOWN_MS - timeSinceLastAlert) / 1000;
                        android.util.Log.e("LocationService", "⏰ Cooldown: " + secondsRemaining + "s remaining");
                    }

                    userInsideZone.put(zoneId, true);
                }
            } else {
                Boolean wasInside = userInsideZone.get(zoneId);
                boolean wasPreviouslyInside = (wasInside != null && wasInside);

                if (wasPreviouslyInside) {
                    android.util.Log.e("LocationService", "🚪 EXITED ZONE: " + zone.getZoneName());
                    userInsideZone.put(zoneId, false);

                    // CANCEL NOTIFICATION WHEN USER EXITS
                    cancelDangerNotification();

                    if (activeAlertForZone.getOrDefault(zoneId, false)) {
                        android.util.Log.e("LocationService", "🔴 STOPPING ALERT - User exited danger zone!");
                        stopAlertForZone(zoneId);
                    }
                }
            }
        }

        if (wasInAnyDangerZone != currentlyInAnyZone) {
            if (currentlyInAnyZone) {
                android.util.Log.e("LocationService", "⚠️ ENTERED DANGER AREA");
                updateNotification("⚠️ DANGER ZONE - Stay Alert!");
            } else {
                android.util.Log.e("LocationService", "✅ EXITED ALL DANGER ZONES");
                updateNotification("🛡️ Safe - Monitoring");
            }
            wasInAnyDangerZone = currentlyInAnyZone;
        }
    }

    private void stopAlertForZone(Long zoneId) {
        // Send broadcast to stop the alert activity
        Intent stopIntent = new Intent("STOP_ALERT");
        stopIntent.putExtra("zone_id", zoneId);
        sendBroadcast(stopIntent);

        // CANCEL THE NOTIFICATION
        cancelDangerNotification();

        activeAlertForZone.put(zoneId, false);
        if (currentAlertZoneId != null && currentAlertZoneId.equals(zoneId)) {
            currentAlertZoneId = null;
        }
        alertInProgress = false;

        android.util.Log.e("LocationService", "Alert stopped for zone: " + zoneId);
    }

    private final BroadcastReceiver stopAlertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("STOP_ALERT".equals(intent.getAction())) {
                long zoneId = intent.getLongExtra("zone_id", -1);
                if (zoneId != -1) {
                    android.util.Log.e("LocationService", "User acknowledged alert for zone: " + zoneId);
                    activeAlertForZone.put(zoneId, false);
                    if (currentAlertZoneId != null && currentAlertZoneId.equals(zoneId)) {
                        currentAlertZoneId = null;
                    }
                    alertInProgress = false;
                    // Also cancel notification when user acknowledges
                    cancelDangerNotification();
                }
            }
        }
    };

    private final BroadcastReceiver resetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("RESET_ZONE_STATUS".equals(intent.getAction())) {
                android.util.Log.e("LocationService", "Received reset command from alert dialog");
                resetAllZoneStatuses();
            }
        }
    };

    private void resetAllZoneStatuses() {
        if (dangerZones != null) {
            for (DangerZone zone : dangerZones) {
                userInsideZone.put(zone.getId(), false);
                lastAlertTime.put(zone.getId(), 0L);
                activeAlertForZone.put(zone.getId(), false);
            }
        }
        wasInAnyDangerZone = false;
        alertInProgress = false;
        currentAlertZoneId = null;
        android.util.Log.e("LocationService", "Reset all zone statuses for re-entry");
    }

    private void updateNotification(String status) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, getNotification(status));
    }

    private void sendInstantAlert(DangerZone zone, double distance) {
        android.util.Log.e("LocationService", "🚨 SENDING ALERT!");
        alertInProgress = true;
        try {
            if (zone != null) {
                NotificationHelper.showEmergencyAlert(this, zone.getZoneName(), zone.getRiskLevel(), distance, zone.getId());
            }
        } catch (Exception e) {
            android.util.Log.e("LocationService", "Failed to send notification: " + e.getMessage());
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(resetReceiver);
            unregisterReceiver(stopAlertReceiver);
        } catch (Exception e) {
            android.util.Log.e("LocationService", "Error unregistering receivers: " + e.getMessage());
        }

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}