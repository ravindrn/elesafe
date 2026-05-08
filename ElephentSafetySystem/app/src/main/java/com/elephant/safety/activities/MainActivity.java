package com.elephant.safety.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.elephant.safety.services.LocationTrackingService;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private BottomNavigationView bottomNav;
    private TextView tvWelcome;
    private Button btnReportSighting, btnSafetyInfo;
    private ApiService apiService;
    private List<DangerZone> dangerZones;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize API service
        apiService = ApiClient.getClient(this).create(ApiService.class);

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        btnReportSighting = findViewById(R.id.btnReportSighting);
        btnSafetyInfo = findViewById(R.id.btnSafetyInfo);
        bottomNav = findViewById(R.id.bottomNav);

        // Set welcome message
        String userName = SharedPrefManager.getInstance(this).getUserName();
        tvWelcome.setText("Welcome, " + userName + "! 🐘");

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set button click listeners
        btnReportSighting.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ReportSightingActivity.class)));

        btnSafetyInfo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SafetyInfoActivity.class)));

        setupBottomNavigation();
        checkLocationPermission();

        // Load danger zones from server
        loadDangerZonesFromServer();

        // Start location tracking service
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        startService(serviceIntent);
    }

    private void loadDangerZonesFromServer() {
        // Check if user is logged in
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            return;
        }

        apiService.getAllZones().enqueue(new Callback<List<DangerZone>>() {
            @Override
            public void onResponse(Call<List<DangerZone>> call, Response<List<DangerZone>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dangerZones = response.body();
                    // Save to SharedPreferences for offline use
                    SharedPrefManager.getInstance(MainActivity.this).saveDangerZones(dangerZones);
                    Toast.makeText(MainActivity.this,
                            "Loaded " + dangerZones.size() + " danger zones",
                            Toast.LENGTH_SHORT).show();

                    if (googleMap != null) {
                        addDangerZonesToMap();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "Failed to load danger zones",
                            Toast.LENGTH_SHORT).show();
                    loadDangerZonesFromStorage();
                }
            }

            @Override
            public void onFailure(Call<List<DangerZone>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                loadDangerZonesFromStorage();
            }
        });
    }

    private void loadDangerZonesFromStorage() {
        dangerZones = SharedPrefManager.getInstance(this).getDangerZones();
        if (dangerZones != null && !dangerZones.isEmpty() && googleMap != null) {
            addDangerZonesToMap();
        }
    }

    private void addDangerZonesToMap() {
        if (dangerZones == null || googleMap == null) return;

        for (DangerZone zone : dangerZones) {
            LatLng zoneLocation = new LatLng(zone.getLatitude(), zone.getLongitude());

            // Add marker with color based on risk level
            googleMap.addMarker(new MarkerOptions()
                    .position(zoneLocation)
                    .title(zone.getZoneName())
                    .snippet("Risk: " + zone.getRiskLevel() + " | " + zone.getDistrict())
                    .icon(BitmapDescriptorFactory.defaultMarker(zone.getMarkerHue())));

            // Add warning circle with color based on risk level
            googleMap.addCircle(new CircleOptions()
                    .center(zoneLocation)
                    .radius(zone.getRadius())
                    .strokeColor(zone.getWarningColor())
                    .fillColor(zone.getWarningColor() & 0x33FFFFFF)
                    .strokeWidth(3));
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
                return true;
            } else if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(MainActivity.this, AlertsActivity.class));
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // Center on Sri Lanka
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 7));

        // Load danger zones if available
        if (dangerZones != null && !dangerZones.isEmpty()) {
            addDangerZonesToMap();
        } else {
            loadDangerZonesFromServer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}