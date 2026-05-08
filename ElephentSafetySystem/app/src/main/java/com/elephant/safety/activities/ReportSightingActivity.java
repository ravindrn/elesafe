package com.elephant.safety.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.elephant.safety.R;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.ElephantReport;
import com.elephant.safety.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportSightingActivity extends AppCompatActivity {

    private EditText etNote;
    private TextView tvElephantCount, tvLocationStatus, tvPhotoStatus;
    private Button btnSubmitReport, btnGetLocation, btnDecrement, btnIncrement, btnUploadPhoto;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiService apiService;

    private int elephantCount = 1;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private boolean hasLocation = false;
    private boolean hasPhoto = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_sighting);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        etNote = findViewById(R.id.etNote);
        tvElephantCount = findViewById(R.id.tvElephantCount);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvPhotoStatus = findViewById(R.id.tvPhotoStatus);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnDecrement = findViewById(R.id.btnDecrement);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);

        // Set initial values
        tvElephantCount.setText(String.valueOf(elephantCount));

        // Setup click listeners
        btnDecrement.setOnClickListener(v -> {
            if (elephantCount > 1) {
                elephantCount--;
                tvElephantCount.setText(String.valueOf(elephantCount));
            }
        });

        btnIncrement.setOnClickListener(v -> {
            elephantCount++;
            tvElephantCount.setText(String.valueOf(elephantCount));
        });

        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        btnUploadPhoto.setOnClickListener(v -> {
            // For now, just simulate photo selection
            hasPhoto = true;
            tvPhotoStatus.setText("📷 Photo selected");
            tvPhotoStatus.setTextColor(getColor(R.color.primary));
        });

        btnSubmitReport.setOnClickListener(v -> submitReport());

        // Auto get location on start
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        tvLocationStatus.setText("📍 Getting location...");
        tvLocationStatus.setTextColor(getColor(R.color.warning));

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                hasLocation = true;
                tvLocationStatus.setText(String.format("📍 Location: %.4f, %.4f",
                        currentLatitude, currentLongitude));
                tvLocationStatus.setTextColor(getColor(R.color.safe));
                btnSubmitReport.setEnabled(true);
            } else {
                tvLocationStatus.setText("⚠️ Could not get location. Please try again.");
                tvLocationStatus.setTextColor(getColor(R.color.danger));
                btnSubmitReport.setEnabled(false);
            }
        });
    }

    private void submitReport() {
        if (!hasLocation) {
            Toast.makeText(this, "Please get location first", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote.getText().toString().trim();
        long userId = SharedPrefManager.getInstance(this).getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        ElephantReport report = new ElephantReport(userId, currentLatitude, currentLongitude, note, elephantCount);

        btnSubmitReport.setText("Submitting...");
        btnSubmitReport.setEnabled(false);

        apiService.submitReport(report).enqueue(new Callback<ElephantReport>() {
            @Override
            public void onResponse(Call<ElephantReport> call, Response<ElephantReport> response) {
                btnSubmitReport.setText("Submit Report");
                btnSubmitReport.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(ReportSightingActivity.this,
                            "✅ Report submitted successfully!\nThank you for keeping roads safe.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ReportSightingActivity.this,
                            "❌ Failed to submit report. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ElephantReport> call, Throwable t) {
                btnSubmitReport.setText("Submit Report");
                btnSubmitReport.setEnabled(true);
                Toast.makeText(ReportSightingActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            tvLocationStatus.setText("⚠️ Location permission required to report sightings");
            tvLocationStatus.setTextColor(getColor(R.color.danger));
        }
    }
}