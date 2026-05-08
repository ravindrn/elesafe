package com.elephant.safety.api;

import com.elephant.safety.models.DangerZone;
import com.elephant.safety.models.ElephantReport;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ========== AUTH ENDPOINTS ==========

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    // ========== DANGER ZONE ENDPOINTS ==========

    @GET("api/zones")
    Call<List<DangerZone>> getAllZones();

    @GET("api/zones/nearby")
    Call<List<DangerZone>> getNearbyZones(@Query("lat") double lat,
                                          @Query("lng") double lng,
                                          @Query("radius") int radius);

    @GET("api/zones/high-risk")
    Call<List<DangerZone>> getHighRiskZones();

    @GET("api/zones/stats")
    Call<ZoneStats> getZoneStats();

    // ========== REPORT ENDPOINTS ==========

    @POST("api/reports")
    Call<ElephantReport> submitReport(@Body ElephantReport report);

    @GET("api/reports")
    Call<List<ElephantReport>> getReports(@Query("status") String status);

    // ========== ALERT ENDPOINTS ==========

    @POST("api/alerts/log")
    Call<Void> logAlert(@Body AlertLog alertLog);

    // ========== INNER CLASSES WITH PUBLIC FIELDS ==========

    /**
     * Login Request Class - Using public fields for simplicity
     */
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    /**
     * Register Request Class
     */
    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public String phone;

        public RegisterRequest() {}

        public RegisterRequest(String name, String email, String password, String phone) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.phone = phone;
        }
    }

    /**
     * Login/Register Response Class
     */
    public static class LoginResponse {
        public String token;
        public UserDTO user;

        public LoginResponse() {}

        public LoginResponse(String token, UserDTO user) {
            this.token = token;
            this.user = user;
        }

        // UserDTO inner class
        public static class UserDTO {
            public Long id;
            public String name;
            public String email;
            public String role;

            public UserDTO() {}

            public UserDTO(Long id, String name, String email, String role) {
                this.id = id;
                this.name = name;
                this.email = email;
                this.role = role;
            }
        }
    }

    /**
     * Alert Log Class
     */
    public static class AlertLog {
        public long userId;
        public long zoneId;
        public String alertType;
        public double latitude;
        public double longitude;

        public AlertLog() {}

        public AlertLog(long userId, long zoneId, String alertType, double latitude, double longitude) {
            this.userId = userId;
            this.zoneId = zoneId;
            this.alertType = alertType;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Zone Statistics Class
     */
    public static class ZoneStats {
        public int total;
        public int highRisk;
        public Map<String, Long> byDistrict;

        public ZoneStats() {}
    }
}