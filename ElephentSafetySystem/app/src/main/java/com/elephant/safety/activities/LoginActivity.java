package com.elephant.safety.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.elephant.safety.R;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.utils.CustomAlertDialog;
import com.elephant.safety.utils.CustomToast;
import com.elephant.safety.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient(this).create(ApiService.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            CustomToast.showWarning(this, "Please fill in all fields");
            return;
        }

        // Debug: Log the email being used for login
        android.util.Log.d("LoginActivity", "Attempting login with email: '" + email + "'");

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        ApiService.LoginRequest request = new ApiService.LoginRequest();
        request.email = email;
        request.password = password;

        apiService.login(request).enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call,
                                   Response<ApiService.LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.LoginResponse loginResponse = response.body();

                    // Debug: Log the response
                    android.util.Log.d("LoginActivity", "Login successful!");
                    android.util.Log.d("LoginActivity", "Token: " + (loginResponse.token != null ? loginResponse.token.substring(0, Math.min(50, loginResponse.token.length())) + "..." : "NULL"));
                    android.util.Log.d("LoginActivity", "User ID: " + (loginResponse.user != null ? loginResponse.user.id : "NULL"));
                    android.util.Log.d("LoginActivity", "User Email: " + (loginResponse.user != null ? loginResponse.user.email : "NULL"));
                    android.util.Log.d("LoginActivity", "User Name: " + (loginResponse.user != null ? loginResponse.user.name : "NULL"));
                    android.util.Log.d("LoginActivity", "User Role: " + (loginResponse.user != null ? loginResponse.user.role : "NULL"));

                    SharedPrefManager.getInstance(LoginActivity.this)
                            .saveUser(loginResponse.token, loginResponse.user);

                    // Verify what was saved
                    String savedToken = SharedPrefManager.getInstance(LoginActivity.this).getToken();
                    String savedEmail = SharedPrefManager.getInstance(LoginActivity.this).getUserEmail();
                    long savedUserId = SharedPrefManager.getInstance(LoginActivity.this).getUserId();

                    android.util.Log.d("LoginActivity", "Saved Token: " + (savedToken != null ? savedToken.substring(0, Math.min(50, savedToken.length())) + "..." : "NULL"));
                    android.util.Log.d("LoginActivity", "Saved Email: " + savedEmail);
                    android.util.Log.d("LoginActivity", "Saved User ID: " + savedUserId);

                    String userName = loginResponse.user != null ? loginResponse.user.name : "Driver";
                    CustomAlertDialog.showLoginSuccess(LoginActivity.this, userName);

                    // Delay navigation to show the dialog
                    new android.os.Handler().postDelayed(() -> {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }, 1500);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        android.util.Log.e("LoginActivity", "Login failed: " + response.code() + " - " + errorBody);
                        CustomToast.showError(LoginActivity.this, "Invalid email or password");
                    } catch (Exception e) {
                        CustomToast.showError(LoginActivity.this, "Invalid email or password");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");
                android.util.Log.e("LoginActivity", "Network error", t);
                CustomToast.showError(LoginActivity.this, "Network error: " + t.getMessage());
            }
        });
    }
}