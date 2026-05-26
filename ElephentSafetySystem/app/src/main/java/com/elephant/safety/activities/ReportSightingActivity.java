package com.elephant.safety.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.elephant.safety.utils.CustomAlertDialog;
import com.elephant.safety.utils.CustomToast;
import com.elephant.safety.utils.SharedPrefManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportSightingActivity extends AppCompatActivity {

    private EditText etNote;
    private TextView tvElephantCount, tvLocationStatus;
    private Button btnSubmitReport, btnGetLocation, btnDecrement, btnIncrement, btnUploadPhoto;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiService apiService;

    private int elephantCount = 1;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private boolean hasLocation = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_sighting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report Elephant Sighting");
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etNote = findViewById(R.id.etNote);
        tvElephantCount = findViewById(R.id.tvElephantCount);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnDecrement = findViewById(R.id.btnDecrement);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);

        tvElephantCount.setText(String.valueOf(elephantCount));

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
        btnSubmitReport.setOnClickListener(v -> submitReport());
        btnUploadPhoto.setOnClickListener(v ->
                CustomToast.showInfo(this, "Camera feature coming soon!"));

        // Auto-get location on start
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

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                hasLocation = true;
                tvLocationStatus.setText(String.format("📍 Location: %.4f, %.4f",
                        currentLatitude, currentLongitude));
                btnSubmitReport.setEnabled(true);
                CustomToast.showSuccess(this, "Location detected successfully");
            } else {
                tvLocationStatus.setText("⚠️ Could not get location. Please try again.");
                btnSubmitReport.setEnabled(false);
                CustomToast.showWarning(this, "Unable to detect location. Please try again.");
            }
        }).addOnFailureListener(e -> {
            tvLocationStatus.setText("⚠️ Location error: " + e.getMessage());
            btnSubmitReport.setEnabled(false);
            CustomToast.showError(this, "Failed to get location: " + e.getMessage());
        });
    }

    private void submitReport() {
        if (!hasLocation) {
            CustomToast.showWarning(this, "Please get location first");
            return;
        }

        String note = etNote.getText().toString().trim();
        if (note.isEmpty()) {
            note = "Elephant sighting reported";
        }

        // Check if user is logged in
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            CustomToast.showError(this, "Please login to submit a report");
            return;
        }

        // IMPORTANT: Use ReportRequest, NOT ElephantReport
        // Do NOT include userId, createdAt, id, status - backend gets these from token and generates them
        ApiService.ReportRequest request = new ApiService.ReportRequest(
                currentLatitude,
                currentLongitude,
                note,
                elephantCount
        );

        btnSubmitReport.setText("Submitting...");
        btnSubmitReport.setEnabled(false);

        // Debug: Log what we're sending
        android.util.Log.d("ReportSighting", "Submitting report: lat=" + currentLatitude + ", lng=" + currentLongitude + ", note=" + note + ", count=" + elephantCount);

        apiService.submitReport(request).enqueue(new Callback<ApiService.ReportResponse>() {
            @Override
            public void onResponse(Call<ApiService.ReportResponse> call, Response<ApiService.ReportResponse> response) {
                btnSubmitReport.setText("Submit Report");
                btnSubmitReport.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ReportResponse reportResponse = response.body();
                    CustomToast.showSuccess(ReportSightingActivity.this,
                            "Report submitted! Status: " + reportResponse.getStatus());
                    CustomAlertDialog.showReportSubmitted(ReportSightingActivity.this);
                    new android.os.Handler().postDelayed(() -> finish(), 2000);
                } else {
                    // Handle different error codes
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        android.util.Log.e("ReportSighting", "Error response: " + response.code() + " - " + errorBody);

                        if (response.code() == 401) {
                            CustomToast.showError(ReportSightingActivity.this, "Session expired. Please login again.");
                            SharedPrefManager.getInstance(ReportSightingActivity.this).logout();
                            finish();
                        } else if (response.code() == 500) {
                            CustomToast.showError(ReportSightingActivity.this, "Server error. Please try again later.");
                        } else {
                            CustomToast.showError(ReportSightingActivity.this, "Failed to submit report: " + response.code());
                        }
                    } catch (IOException e) {
                        CustomToast.showError(ReportSightingActivity.this, "Failed to submit report");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ReportResponse> call, Throwable t) {
                btnSubmitReport.setText("Submit Report");
                btnSubmitReport.setEnabled(true);
                CustomToast.showError(ReportSightingActivity.this, "Network error: " + t.getMessage());
                android.util.Log.e("ReportSighting", "Network error", t);
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
            tvLocationStatus.setText("⚠️ Location permission required");
            CustomToast.showError(this, "Location permission is required to report sightings");
        }
    }
}