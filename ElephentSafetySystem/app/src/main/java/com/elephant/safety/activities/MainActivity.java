package com.elephant.safety.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.elephant.safety.R;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.DangerZone;
import com.elephant.safety.services.LocationForegroundService;
import com.elephant.safety.utils.CustomToast;
import com.elephant.safety.utils.HaversineFormula;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private BottomNavigationView bottomNav;
    private TextView tvWelcome, tvSafetyStatus, tvSafetyMessage, tvSafetyIcon, tvSpeed, tvProximityLabel, tvWarningHint;
    private ProgressBar progressDanger;
    private LinearLayout safetyStatusBar;
    private Button btnReportSighting, btnSafetyInfo;
    private ApiService apiService;
    private List<DangerZone> dangerZones;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Safety status colors
    private static final int COLOR_SAFE = 0xFF4CAF50;
    private static final int COLOR_WARNING = 0xFFFF9800;
    private static final int COLOR_DANGER = 0xFFF44336;
    private static final int COLOR_CRITICAL = 0xFFD32F2F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSafetyStatus = findViewById(R.id.tvSafetyStatus);
        tvSafetyMessage = findViewById(R.id.tvSafetyMessage);
        tvSafetyIcon = findViewById(R.id.tvSafetyIcon);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvProximityLabel = findViewById(R.id.tvProximityLabel);
        tvWarningHint = findViewById(R.id.tvWarningHint);
        progressDanger = findViewById(R.id.progressDanger);
        safetyStatusBar = findViewById(R.id.safetyStatusBar);
        btnReportSighting = findViewById(R.id.btnReportSighting);
        btnSafetyInfo = findViewById(R.id.btnSafetyInfo);
        bottomNav = findViewById(R.id.bottomNav);

        String userName = SharedPrefManager.getInstance(this).getUserName();
        tvWelcome.setText("Welcome, " + userName + "! 🐘");

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnReportSighting.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ReportSightingActivity.class)));

        btnSafetyInfo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SafetyInfoActivity.class)));

        setupBottomNavigation();
        checkLocationPermission();
        loadDangerZonesFromServer();
        startLocationUpdates();

        // Start background location service
        startBackgroundLocationService();
    }

    private void startBackgroundLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, LocationForegroundService.class));
        } else {
            startService(new Intent(this, LocationForegroundService.class));
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    updateSafetyStatus();

                    if (location.hasSpeed()) {
                        float speedKmh = location.getSpeed() * 3.6f;
                        tvSpeed.setText(String.valueOf(Math.round(speedKmh)));
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void updateSafetyStatus() {
        if (dangerZones == null || dangerZones.isEmpty()) {
            setSafeStatus("Loading zones...", "Fetching danger zone data", "🟡");
            return;
        }

        DangerZone nearestZone = null;
        double minDistance = Double.MAX_VALUE;

        for (DangerZone zone : dangerZones) {
            double distance = HaversineFormula.calculateDistance(
                    currentLatitude, currentLongitude,
                    zone.getLatitude(), zone.getLongitude()
            ) - zone.getRadius();

            if (distance < 0) distance = 0;

            if (distance < minDistance) {
                minDistance = distance;
                nearestZone = zone;
            }
        }

        if (nearestZone == null) {
            setSafeStatus("YOU ARE SAFE", "No danger zones detected", "🟢");
            return;
        }

        double distanceKm = minDistance / 1000;
        String riskLevel = nearestZone.getRiskLevel();

        if (minDistance < 100) {
            tvProximityLabel.setText("⚠️ ENTERING DANGER ZONE!");
        } else if (minDistance < 1000) {
            tvProximityLabel.setText(String.format("Nearest Danger Zone: %.0f meters - %s Risk", minDistance, riskLevel));
        } else {
            tvProximityLabel.setText(String.format("Nearest Danger Zone: %.1f km away - %s Risk", distanceKm, riskLevel));
        }

        int progress;
        if (minDistance < 0) {
            progress = 100;
        } else if (minDistance > 5000) {
            progress = 0;
        } else {
            progress = (int)((1 - (minDistance / 5000)) * 100);
        }
        progressDanger.setProgress(progress);

        if (minDistance < 0) {
            if (riskLevel.equals("CRITICAL")) {
                setDangerStatus("⚠️ CRITICAL DANGER!", "You are in a CRITICAL elephant zone! Immediate action required!", "🔴", COLOR_CRITICAL);
                tvWarningHint.setVisibility(android.view.View.VISIBLE);
                tvWarningHint.setText("🚨 EMERGENCY: You are inside a CRITICAL danger zone! Reduce speed NOW!");
                tvWarningHint.setTextColor(Color.parseColor("#FF0000"));
            } else if (riskLevel.equals("HIGH")) {
                setDangerStatus("⚠️ HIGH RISK!", "You are in a HIGH RISK elephant zone! Stay alert!", "🟠", COLOR_DANGER);
                tvWarningHint.setVisibility(android.view.View.VISIBLE);
                tvWarningHint.setText("⚠️ WARNING: You are in a HIGH RISK elephant danger zone!");
            } else {
                setWarningStatus("⚠️ DANGER ZONE!", "You are in an elephant danger zone. Stay alert!", "🟡", COLOR_WARNING);
                tvWarningHint.setVisibility(android.view.View.VISIBLE);
                tvWarningHint.setText("⚠️ You are in an elephant danger zone - Stay alert!");
            }
        } else if (minDistance < 500) {
            setDangerStatus("⚠️ APPROACHING DANGER!", String.format("%.0f meters to danger zone. Be prepared!", minDistance), "🔴", COLOR_DANGER);
            tvWarningHint.setVisibility(android.view.View.VISIBLE);
            tvWarningHint.setText(String.format("⚠️ Approaching %s risk zone - %.0f meters ahead!", riskLevel, minDistance));
        } else if (minDistance < 1000) {
            setWarningStatus("⚠️ CAUTION!", String.format("Danger zone %.0f meters ahead. Stay vigilant!", minDistance), "🟡", COLOR_WARNING);
            tvWarningHint.setVisibility(android.view.View.VISIBLE);
            tvWarningHint.setText(String.format("⚠️ %s risk zone ahead - %.0f meters", riskLevel, minDistance));
        } else if (minDistance < 2000) {
            setInfoStatus("⚠️ BE AWARE", String.format("Danger zone %.1f km ahead. Stay alert!", distanceKm), "🟠", COLOR_WARNING);
            tvWarningHint.setVisibility(android.view.View.GONE);
        } else {
            setSafeStatus("YOU ARE SAFE", String.format("Nearest danger zone is %.1f km away", distanceKm), "🟢");
            tvWarningHint.setVisibility(android.view.View.GONE);
        }
    }

    private void setSafeStatus(String status, String message, String icon) {
        safetyStatusBar.setBackgroundColor(COLOR_SAFE);
        tvSafetyStatus.setText(status);
        tvSafetyMessage.setText(message);
        tvSafetyIcon.setText(icon);
        progressDanger.setProgressTintList(android.content.res.ColorStateList.valueOf(COLOR_SAFE));
    }

    private void setWarningStatus(String status, String message, String icon, int color) {
        safetyStatusBar.setBackgroundColor(color);
        tvSafetyStatus.setText(status);
        tvSafetyMessage.setText(message);
        tvSafetyIcon.setText(icon);
        progressDanger.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void setDangerStatus(String status, String message, String icon, int color) {
        safetyStatusBar.setBackgroundColor(color);
        tvSafetyStatus.setText(status);
        tvSafetyMessage.setText(message);
        tvSafetyIcon.setText(icon);
        progressDanger.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void setInfoStatus(String status, String message, String icon, int color) {
        safetyStatusBar.setBackgroundColor(color);
        tvSafetyStatus.setText(status);
        tvSafetyMessage.setText(message);
        tvSafetyIcon.setText(icon);
        progressDanger.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
                return true;
            } else if (itemId == R.id.nav_insights) {
                startActivity(new Intent(MainActivity.this, InsightsActivity.class));
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(MainActivity.this, ReportSightingActivity.class));
                return true;
            } else if (itemId == R.id.nav_safety) {
                startActivity(new Intent(MainActivity.this, SafetyInfoActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void checkLocationPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void loadDangerZonesFromServer() {
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            return;
        }

        apiService.getAllZones().enqueue(new Callback<List<DangerZone>>() {
            @Override
            public void onResponse(Call<List<DangerZone>> call, Response<List<DangerZone>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dangerZones = response.body();
                    SharedPrefManager.getInstance(MainActivity.this).saveDangerZones(dangerZones);
                    CustomToast.showSuccess(MainActivity.this, "Loaded " + dangerZones.size() + " danger zones");
                    if (googleMap != null) {
                        addDangerZonesToMap();
                    }
                    updateSafetyStatus();
                }
            }

            @Override
            public void onFailure(Call<List<DangerZone>> call, Throwable t) {
                loadDangerZonesFromStorage();
            }
        });
    }

    private void loadDangerZonesFromStorage() {
        dangerZones = SharedPrefManager.getInstance(this).getDangerZones();
        if (dangerZones != null && !dangerZones.isEmpty() && googleMap != null) {
            addDangerZonesToMap();
            updateSafetyStatus();
        }
    }

    private void addDangerZonesToMap() {
        if (dangerZones == null || googleMap == null) return;
        for (DangerZone zone : dangerZones) {
            LatLng zoneLocation = new LatLng(zone.getLatitude(), zone.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(zoneLocation)
                    .title(zone.getZoneName())
                    .snippet("Risk: " + zone.getRiskLevel())
                    .icon(BitmapDescriptorFactory.defaultMarker(zone.getMarkerHue())));
            googleMap.addCircle(new CircleOptions()
                    .center(zoneLocation)
                    .radius(zone.getRadius())
                    .strokeColor(zone.getWarningColor())
                    .fillColor(zone.getWarningColor() & 0x33FFFFFF)
                    .strokeWidth(3));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 7));
        if (dangerZones != null && !dangerZones.isEmpty()) {
            addDangerZonesToMap();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}