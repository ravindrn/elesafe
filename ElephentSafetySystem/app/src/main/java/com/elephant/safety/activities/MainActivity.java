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
import com.elephant.safety.models.RiskPredictionRequest;
import com.elephant.safety.models.RiskPredictionResponse;
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

    // 1. The Updated Safety Status method
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

        // --- TRIGGER THE AI ---
        double distanceKm = minDistance / 1000.0;
        fetchAiPrediction(distanceKm, nearestZone);
    }

    // 2. The AI Network Call (LIVE GPS ENABLED)
    private void fetchAiPrediction(double distanceKm, DangerZone nearestZone) {
        String timeOfDay = getCurrentTimeOfDay();

        // We are still simulating weather for now, but GPS is REAL!
        String simulatedWeather = "Clear";

        // IMPORTANT: We are now passing currentLatitude and currentLongitude
        // directly from your phone's live GPS sensor!
        RiskPredictionRequest request = new RiskPredictionRequest(
                distanceKm,
                currentLatitude,
                currentLongitude,
                timeOfDay,
                simulatedWeather
        );

        apiService.checkCurrentRisk(request).enqueue(new Callback<RiskPredictionResponse>() {
            @Override
            public void onResponse(Call<RiskPredictionResponse> call, Response<RiskPredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiRiskLevel = response.body().predictedRisk;

                    runOnUiThread(() -> {
                        tvProximityLabel.setText(String.format("Nearest Zone: %.1f km away", distanceKm));

                        if ("CRITICAL".equals(aiRiskLevel)) {
                            setDangerStatus("⚠️ CRITICAL DANGER!", "AI detected severe risk! Reduce speed NOW!", "🔴", COLOR_CRITICAL);
                            tvWarningHint.setVisibility(android.view.View.VISIBLE);
                            tvWarningHint.setText("🚨 EMERGENCY: AI has flagged this area as highly active.");
                            tvWarningHint.setTextColor(Color.parseColor("#FFFFFF"));

                        } else if ("HIGH".equals(aiRiskLevel)) {
                            setDangerStatus("⚠️ HIGH RISK!", "AI predicts high elephant activity!", "🟠", COLOR_DANGER);
                            tvWarningHint.setVisibility(android.view.View.VISIBLE);
                            tvWarningHint.setText("⚠️ WARNING: High probability of elephant encounter.");

                        } else if ("MEDIUM".equals(aiRiskLevel)) {
                            setWarningStatus("⚠️ BE AWARE", "AI indicates moderate risk. Stay alert.", "🟡", COLOR_WARNING);
                            tvWarningHint.setVisibility(android.view.View.VISIBLE);
                            tvWarningHint.setText("⚠️ Elephant presence is possible.");

                        } else {
                            setSafeStatus("YOU ARE SAFE", "AI indicates low risk.", "🟢");
                            tvWarningHint.setVisibility(android.view.View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<RiskPredictionResponse> call, Throwable t) {
                Log.e("ML_ERROR", "Failed to connect to AI server: " + t.getMessage());
                setSafeStatus("CONNECTING...", "AI Server unreachable", "⚪");
            }
        });
    }


    // 3. The Time Helper
    private String getCurrentTimeOfDay() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 19) return "Dusk";
        return "Night";
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