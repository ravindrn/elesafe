package com.elephant.safety.api;

import com.elephant.safety.models.DangerZone;
import com.elephant.safety.models.ElephantReport;
import com.elephant.safety.models.SafetyTip;
import com.elephant.safety.models.EmergencyContact;
import com.elephant.safety.models.VerifiedReport;
import com.elephant.safety.models.NewsItem;
import com.elephant.safety.models.DashboardStats;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
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
    Call<ReportResponse> submitReport(@Body ReportRequest report);

    @GET("api/reports")
    Call<List<ElephantReport>> getReports(@Query("status") String status);

    @GET("api/reports/my-reports")
    Call<List<ElephantReport>> getMyReports();

    // ========== ALERT ENDPOINTS ==========

    @POST("api/alerts/log")
    Call<Void> logAlert(@Body AlertLog alertLog);

    // ========== SAFETY TIPS ENDPOINTS ==========

    @GET("api/safety-tips/active")
    Call<List<SafetyTip>> getActiveSafetyTips();

    @GET("api/safety-tips/categorized")
    Call<CategorizedSafetyTips> getCategorizedSafetyTips();

    @GET("api/safety-tips/category/{category}")
    Call<List<SafetyTip>> getSafetyTipsByCategory(@Path("category") String category);

    @GET("api/safety-tips/{id}")
    Call<SafetyTip> getSafetyTipById(@Path("id") Long id);

    // ========== EMERGENCY CONTACT ENDPOINTS ==========

    @GET("api/emergency-contacts/active")
    Call<List<EmergencyContact>> getAllEmergencyContacts();

    @GET("api/emergency-contacts/categorized")
    Call<CategorizedEmergencyContacts> getCategorizedEmergencyContacts();

    @GET("api/emergency-contacts/category/{category}")
    Call<List<EmergencyContact>> getEmergencyContactsByCategory(@Path("category") String category);

    // ========== INSIGHTS ENDPOINTS ==========

    @GET("api/insights/verified-reports")
    Call<List<VerifiedReport>> getVerifiedReports();

    @GET("api/insights/news")
    Call<List<NewsItem>> getNews();

    @GET("api/insights/stats")
    Call<DashboardStats> getDashboardStats();

    @GET("api/insights/recent-accidents")
    Call<List<NewsItem>> getRecentAccidents();

    // ========== PROFILE ENDPOINTS ==========

    @GET("api/users/stats")
    Call<UserStats> getUserStats();

    @PUT("api/users/profile")
    Call<UpdateProfileResponse> updateProfile(@Body UpdateProfileRequest request);

    @PUT("api/users/password")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    @POST("api/users/profile-image")
    Call<ImageUploadResponse> uploadProfileImage(@Part MultipartBody.Part image);

    @DELETE("api/users/profile-image")
    Call<Void> removeProfileImage();

    // ========== INNER CLASSES WITH PUBLIC FIELDS ==========

    /**
     * Login Request Class
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
     * Report Request Class
     */
    public static class ReportRequest {
        private double latitude;
        private double longitude;
        private String note;
        private int elephantCount;

        public ReportRequest() {}

        public ReportRequest(double latitude, double longitude, String note, int elephantCount) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.note = note;
            this.elephantCount = elephantCount;
        }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public int getElephantCount() { return elephantCount; }
        public void setElephantCount(int elephantCount) { this.elephantCount = elephantCount; }
    }

    /**
     * Report Response Class
     */
    public static class ReportResponse {
        private Long id;
        private String message;
        private String status;

        public ReportResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
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

    /**
     * Categorized Safety Tips Class
     */
    public static class CategorizedSafetyTips {
        public List<SafetyTip> drivingTips;
        public List<SafetyTip> encounterTips;
        public List<SafetyTip> emergencyTips;
        public List<SafetyTip> generalTips;

        public CategorizedSafetyTips() {}

        public List<SafetyTip> getDrivingTips() { return drivingTips; }
        public void setDrivingTips(List<SafetyTip> drivingTips) { this.drivingTips = drivingTips; }
        public List<SafetyTip> getEncounterTips() { return encounterTips; }
        public void setEncounterTips(List<SafetyTip> encounterTips) { this.encounterTips = encounterTips; }
        public List<SafetyTip> getEmergencyTips() { return emergencyTips; }
        public void setEmergencyTips(List<SafetyTip> emergencyTips) { this.emergencyTips = emergencyTips; }
        public List<SafetyTip> getGeneralTips() { return generalTips; }
        public void setGeneralTips(List<SafetyTip> generalTips) { this.generalTips = generalTips; }
    }

    /**
     * Categorized Emergency Contacts Class
     */
    public static class CategorizedEmergencyContacts {
        public List<EmergencyContact> police;
        public List<EmergencyContact> ambulance;
        public List<EmergencyContact> wildlife;
        public List<EmergencyContact> hospital;
        public List<EmergencyContact> forest;

        public CategorizedEmergencyContacts() {}

        public List<EmergencyContact> getPolice() { return police; }
        public void setPolice(List<EmergencyContact> police) { this.police = police; }
        public List<EmergencyContact> getAmbulance() { return ambulance; }
        public void setAmbulance(List<EmergencyContact> ambulance) { this.ambulance = ambulance; }
        public List<EmergencyContact> getWildlife() { return wildlife; }
        public void setWildlife(List<EmergencyContact> wildlife) { this.wildlife = wildlife; }
        public List<EmergencyContact> getHospital() { return hospital; }
        public void setHospital(List<EmergencyContact> hospital) { this.hospital = hospital; }
        public List<EmergencyContact> getForest() { return forest; }
        public void setForest(List<EmergencyContact> forest) { this.forest = forest; }
    }

    // ========== PROFILE INNER CLASSES ==========

    /**
     * User Statistics Class
     */
    public static class UserStats {
        private int totalReports;
        private int approvedReports;
        private int dangerZonesVisited;
        private int totalAlerts;
        private int savedReports;

        public UserStats() {}

        public int getTotalReports() { return totalReports; }
        public void setTotalReports(int totalReports) { this.totalReports = totalReports; }

        public int getApprovedReports() { return approvedReports; }
        public void setApprovedReports(int approvedReports) { this.approvedReports = approvedReports; }

        public int getDangerZonesVisited() { return dangerZonesVisited; }
        public void setDangerZonesVisited(int dangerZonesVisited) { this.dangerZonesVisited = dangerZonesVisited; }

        public int getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(int totalAlerts) { this.totalAlerts = totalAlerts; }

        public int getSavedReports() { return savedReports; }
        public void setSavedReports(int savedReports) { this.savedReports = savedReports; }
    }

    /**
     * Update Profile Request Class
     */
    public static class UpdateProfileRequest {
        private String name;
        private String email;
        private String phone;

        public UpdateProfileRequest() {}

        public UpdateProfileRequest(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * Update Profile Response Class
     */
    public static class UpdateProfileResponse {
        private String message;
        private UserDTO user;

        public UpdateProfileResponse() {}

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public UserDTO getUser() { return user; }
        public void setUser(UserDTO user) { this.user = user; }

        public static class UserDTO {
            private Long id;
            private String name;
            private String email;
            private String phone;
            private String role;

            public UserDTO() {}

            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public String getPhone() { return phone; }
            public void setPhone(String phone) { this.phone = phone; }

            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
        }
    }

    /**
     * Change Password Request Class
     */
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public ChangePasswordRequest() {}

        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Change Password Response Class
     */
    public static class ChangePasswordResponse {
        private String message;
        private boolean success;

        public ChangePasswordResponse() {}

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    /**
     * Image Upload Response Class
     */
    public static class ImageUploadResponse {
        private String imageUrl;
        private String message;

        public ImageUploadResponse() {}

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}