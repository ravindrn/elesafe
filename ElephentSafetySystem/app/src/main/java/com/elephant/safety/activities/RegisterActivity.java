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

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getClient(this).create(ApiService.class);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            CustomToast.showWarning(this, "Please fill all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            CustomToast.showError(this, "Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            CustomToast.showWarning(this, "Password must be at least 6 characters");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        ApiService.RegisterRequest request = new ApiService.RegisterRequest();
        request.name = name;
        request.email = email;
        request.password = password;
        request.phone = "";

        apiService.register(request).enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call,
                                   Response<ApiService.LoginResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("REGISTER");

                if (response.isSuccessful() && response.body() != null) {
                    CustomAlertDialog.showRegistrationSuccess(RegisterActivity.this);
                } else {
                    CustomToast.showError(RegisterActivity.this, "Email already exists or invalid data");
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("REGISTER");
                CustomToast.showError(RegisterActivity.this, "Network error: " + t.getMessage());
            }
        });
    }
}