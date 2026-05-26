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
import com.google.android.gms.location.Priority;
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

    // Store current location
    private double currentLatitude = 0;
    private double currentLongitude = 0;

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
                    // Update current location
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    // Log the location for debugging
                    android.util.Log.d("LocationTracking", "Lat: " + currentLatitude + ", Lon: " + currentLongitude);

                    checkDangerZones(currentLatitude, currentLongitude);

                    // Broadcast location update
                    Intent intent = new Intent("LOCATION_UPDATE");
                    intent.putExtra("latitude", currentLatitude);
                    intent.putExtra("longitude", currentLongitude);
                    LocalBroadcastManager.getInstance(LocationTrackingService.this).sendBroadcast(intent);
                }
            }
        };
    }

    private void checkDangerZones(double userLat, double userLon) {
        if (dangerZones == null) return;

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
        return recentlyAlertedZones.contains(zoneId);
    }

    private void addToRecentlyAlerted(long zoneId) {
        recentlyAlertedZones.add(zoneId);
        new android.os.Handler().postDelayed(() ->
                recentlyAlertedZones.remove(zoneId), ALERT_COOLDOWN_MS);
    }

    private void triggerZoneAlert(DangerZone zone) {
        // Calculate distance to zone center
        double distanceToZone = HaversineFormula.calculateDistance(
                currentLatitude, currentLongitude,
                zone.getLatitude(), zone.getLongitude()
        );

        EmergencyAlertService alertService = new EmergencyAlertService(this);
        alertService.showDetailedAlert(
                zone.getZoneName(),
                zone.getRiskLevel(),
                distanceToZone / 1000 // Convert to km
        );
    }

    private void loadDangerZones() {
        dangerZones = SharedPrefManager.getInstance(this).getDangerZones();
        if (dangerZones == null) {
            dangerZones = new ArrayList<>();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        // Create location request with PRIORITY_HIGH_ACCURACY
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } else {
            android.util.Log.e("LocationTracking", "Location permission not granted");
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