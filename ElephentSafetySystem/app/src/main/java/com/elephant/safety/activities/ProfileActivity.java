package com.elephant.safety.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.elephant.safety.R;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.utils.CustomToast;
import com.elephant.safety.utils.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    // Views
    private ImageView ivProfileImage;
    private TextView tvUserName, tvUserEmail, tvUserRole, tvStatsTotalReports, tvStatsApproved, tvStatsDangerZones;
    private TextView tvAccountName, tvAccountEmail, tvAccountPhone, tvMemberSinceDetail;
    private EditText etName, etEmail, etPhone;
    private TextInputLayout tilName, tilEmail, tilPhone;
    private Button btnEditProfile, btnChangePassword, btnLogout, btnSaveChanges, btnCancelEdit;
    private ProgressBar progressBar;
    private LinearLayout editContainer, viewContainer;
    private MaterialCardView cardProfile, cardStats;

    private ApiService apiService;
    private SharedPrefManager prefManager;
    private boolean isEditMode = false;

    private Uri profileImageUri;
    private String currentPhotoPath;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    uploadProfileImage(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);
        prefManager = SharedPrefManager.getInstance(this);

        initViews();
        loadUserData();
        loadUserStats();  // This will fetch real stats from backend
        setupClickListeners();
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvStatsTotalReports = findViewById(R.id.tvStatsTotalReports);
        tvStatsApproved = findViewById(R.id.tvStatsApproved);
        tvStatsDangerZones = findViewById(R.id.tvStatsDangerZones);

        // Account Details TextViews
        tvAccountName = findViewById(R.id.tvAccountName);
        tvAccountEmail = findViewById(R.id.tvAccountEmail);
        tvAccountPhone = findViewById(R.id.tvAccountPhone);
        tvMemberSinceDetail = findViewById(R.id.tvMemberSinceDetail);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        progressBar = findViewById(R.id.progressBar);
        editContainer = findViewById(R.id.editContainer);
        viewContainer = findViewById(R.id.viewContainer);
        cardProfile = findViewById(R.id.cardProfile);
        cardStats = findViewById(R.id.cardStats);

        setEditMode(false);
    }

    private void loadUserData() {
        String userName = prefManager.getUserName();
        String userEmail = prefManager.getUserEmail();
        String userRole = prefManager.getUserRole();
        String userPhone = prefManager.getUserPhone();

        // Profile header
        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        tvUserRole.setText(userRole.equals("ADMIN") ? "Administrator" : "Driver");

        // Account Details section
        tvAccountName.setText(userName);
        tvAccountEmail.setText(userEmail);
        tvAccountPhone.setText(userPhone != null && !userPhone.isEmpty() ? userPhone : "Not provided");
        tvMemberSinceDetail.setText(getMemberSinceDate());

        // Edit mode fields
        etName.setText(userName);
        etEmail.setText(userEmail);
        etPhone.setText(userPhone != null ? userPhone : "");

        loadProfileImage();
    }

    private void loadProfileImage() {
        String imageUrl = prefManager.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(ApiClient.BASE_URL + imageUrl)
                    .placeholder(R.drawable.ic_profile_default)
                    .error(R.drawable.ic_profile_default)
                    .circleCrop()
                    .into(ivProfileImage);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_default)
                    .circleCrop()
                    .into(ivProfileImage);
        }
    }

    private void loadUserStats() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getUserStats().enqueue(new Callback<ApiService.UserStats>() {
            @Override
            public void onResponse(Call<ApiService.UserStats> call, Response<ApiService.UserStats> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.UserStats stats = response.body();

                    // Update UI with real data
                    tvStatsTotalReports.setText(String.valueOf(stats.getTotalReports()));
                    tvStatsApproved.setText(String.valueOf(stats.getApprovedReports()));
                    tvStatsDangerZones.setText(String.valueOf(stats.getDangerZonesVisited()));

                    // Also update stats in SharedPref for caching
                    prefManager.saveUserStats(stats.getTotalReports(), stats.getApprovedReports(), stats.getDangerZonesVisited());
                } else {
                    // Try to load cached stats
                    loadCachedStats();

                    // Show error but don't crash
                    if (response.code() == 401) {
                        CustomToast.showWarning(ProfileActivity.this, "Please login again to see updated stats");
                    } else {
                        CustomToast.showWarning(ProfileActivity.this, "Could not load latest stats");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.UserStats> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

                // Load cached stats on network failure
                loadCachedStats();

                CustomToast.showWarning(ProfileActivity.this, "Network error. Showing cached stats.");
            }
        });
    }

    private void loadCachedStats() {
        // Load cached stats from SharedPref
        tvStatsTotalReports.setText(String.valueOf(prefManager.getCachedTotalReports()));
        tvStatsApproved.setText(String.valueOf(prefManager.getCachedApprovedReports()));
        tvStatsDangerZones.setText(String.valueOf(prefManager.getCachedDangerZonesVisited()));
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> setEditMode(true));
        btnCancelEdit.setOnClickListener(v -> {
            setEditMode(false);
            loadUserData();
        });

        btnSaveChanges.setOnClickListener(v -> updateProfile());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        ivProfileImage.setOnClickListener(v -> showImagePickerDialog());
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;

        viewContainer.setVisibility(editMode ? View.GONE : View.VISIBLE);
        editContainer.setVisibility(editMode ? View.VISIBLE : View.GONE);

        btnEditProfile.setVisibility(editMode ? View.GONE : View.VISIBLE);
        btnChangePassword.setVisibility(editMode ? View.GONE : View.VISIBLE);
        btnLogout.setVisibility(editMode ? View.GONE : View.VISIBLE);

        etName.setEnabled(editMode);
        etEmail.setEnabled(editMode);
        etPhone.setEnabled(editMode);
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ApiService.UpdateProfileRequest request = new ApiService.UpdateProfileRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);

        apiService.updateProfile(request).enqueue(new Callback<ApiService.UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<ApiService.UpdateProfileResponse> call, Response<ApiService.UpdateProfileResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    prefManager.updateUserInfo(name, email, phone);
                    CustomToast.showSuccess(ProfileActivity.this, "Profile updated successfully!");
                    setEditMode(false);
                    loadUserData();
                } else {
                    CustomToast.showError(ProfileActivity.this, "Failed to update profile");
                }
            }

            @Override
            public void onFailure(Call<ApiService.UpdateProfileResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                CustomToast.showError(ProfileActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String currentPwd = etCurrentPassword.getText().toString().trim();
                    String newPwd = etNewPassword.getText().toString().trim();
                    String confirmPwd = etConfirmPassword.getText().toString().trim();

                    if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                        CustomToast.showWarning(ProfileActivity.this, "Please fill all fields");
                        return;
                    }

                    if (!newPwd.equals(confirmPwd)) {
                        CustomToast.showWarning(ProfileActivity.this, "New passwords don't match");
                        return;
                    }

                    if (newPwd.length() < 6) {
                        CustomToast.showWarning(ProfileActivity.this, "Password must be at least 6 characters");
                        return;
                    }

                    changePassword(currentPwd, newPwd);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        progressBar.setVisibility(View.VISIBLE);

        ApiService.ChangePasswordRequest request = new ApiService.ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);

        apiService.changePassword(request).enqueue(new Callback<ApiService.ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ApiService.ChangePasswordResponse> call, Response<ApiService.ChangePasswordResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    CustomToast.showSuccess(ProfileActivity.this, "Password changed successfully!");
                } else {
                    CustomToast.showError(ProfileActivity.this, "Current password is incorrect");
                }
            }

            @Override
            public void onFailure(Call<ApiService.ChangePasswordResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                CustomToast.showError(ProfileActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissionAndOpen();
                            break;
                        case 1:
                            openGallery();
                            break;
                        case 2:
                            removeProfileImage();
                            break;
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                profileImageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, profileImageUri);
                startActivityForResult(takePictureIntent, 101);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadProfileImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytes(inputStream);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "profile.jpg", requestFile);

            apiService.uploadProfileImage(body).enqueue(new Callback<ApiService.ImageUploadResponse>() {
                @Override
                public void onResponse(Call<ApiService.ImageUploadResponse> call, Response<ApiService.ImageUploadResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body().getImageUrl();
                        prefManager.saveProfileImageUrl(imageUrl);
                        loadProfileImage();
                        CustomToast.showSuccess(ProfileActivity.this, "Profile picture updated!");
                    } else {
                        CustomToast.showError(ProfileActivity.this, "Failed to upload image");
                    }
                }

                @Override
                public void onFailure(Call<ApiService.ImageUploadResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    CustomToast.showError(ProfileActivity.this, "Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            CustomToast.showError(ProfileActivity.this, "Failed to process image");
        }
    }

    private void removeProfileImage() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.removeProfileImage().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    prefManager.saveProfileImageUrl(null);
                    Glide.with(ProfileActivity.this)
                            .load(R.drawable.ic_profile_default)
                            .circleCrop()
                            .into(ivProfileImage);
                    CustomToast.showSuccess(ProfileActivity.this, "Profile picture removed");
                } else {
                    CustomToast.showError(ProfileActivity.this, "Failed to remove image");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                CustomToast.showError(ProfileActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        prefManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getMemberSinceDate() {
        String createdAt = prefManager.getUserCreatedAt();
        if (createdAt != null && !createdAt.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                Date date = inputFormat.parse(createdAt);
                return outputFormat.format(date);
            } catch (Exception e) {
                return "April 2026";
            }
        }
        return "April 2026";
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}