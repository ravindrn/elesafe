package com.elephant.safety.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.elephant.safety.models.DangerZone;
import com.elephant.safety.utils.HaversineFormula;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class LocationTrackingService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private List<DangerZone> dangerZones;
    private List<Long> recentlyAlertedZones;
    private static final long ALERT_COOLDOWN_MS = 300000; // 5 minutes

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dangerZones = new ArrayList<>();
        recentlyAlertedZones = new ArrayList<>();
        initLocationCallback();
        loadDangerZones();
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    checkDangerZones(location.getLatitude(), location.getLongitude());

                    // Broadcast location update
                    Intent intent = new Intent("LOCATION_UPDATE");
                    intent.putExtra("latitude", location.getLatitude());
                    intent.putExtra("longitude", location.getLongitude());
                    LocalBroadcastManager.getInstance(LocationTrackingService.this).sendBroadcast(intent);
                }
            }
        };
    }

    private void checkDangerZones(double userLat, double userLon) {
        for (DangerZone zone : dangerZones) {
            boolean insideZone = HaversineFormula.isInsideZone(
                    userLat, userLon, zone.getLatitude(), zone.getLongitude(), zone.getRadius()
            );

            if (insideZone && !isRecentlyAlerted(zone.getId())) {
                triggerZoneAlert(zone);
                addToRecentlyAlerted(zone.getId());
            }
        }
    }

    private boolean isRecentlyAlerted(long zoneId) {
        // Check if zone was alerted within cooldown period
        return recentlyAlertedZones.contains(zoneId);
    }

    private void addToRecentlyAlerted(long zoneId) {
        recentlyAlertedZones.add(zoneId);
        // Remove after cooldown
        new android.os.Handler().postDelayed(() ->
                recentlyAlertedZones.remove(zoneId), ALERT_COOLDOWN_MS);
    }

    private void triggerZoneAlert(DangerZone zone) {
        EmergencyAlertService alertService = new EmergencyAlertService(this);
        alertService.triggerFullAlert(
                String.format("Warning! Entering %s zone. Risk level: %s",
                        zone.getZoneName(), zone.getRiskLevel())
        );
    }

    private void loadDangerZones() {
        // Load danger zones from API
        dangerZones = SharedPrefManager.getInstance(this).getDangerZones();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000) // 5 seconds
                .setFastestInterval(2000) // 2 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}