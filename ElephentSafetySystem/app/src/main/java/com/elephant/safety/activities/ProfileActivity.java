package com.elephant.safety.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.elephant.safety.R;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize SharedPreferences
        prefs = getSharedPreferences("ElephantAlertPrefs", MODE_PRIVATE);

        // Load user info (for demo - replace with real auth later)
        String userName = prefs.getString("userName", "Pramitha Sahan");
        String userEmail = prefs.getString("userEmail", "pramitha@example.com");

        // Setup buttons
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🚪 Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear user data
                    prefs.edit().clear().apply();

                    // Show success message
                    Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

                    // Close profile and go back to main
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}