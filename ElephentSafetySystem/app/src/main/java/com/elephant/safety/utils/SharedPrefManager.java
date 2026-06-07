package com.elephant.safety.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.DangerZone;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SharedPrefManager {
    private static final String PREF_NAME = "ElephantSafetyPref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_USER_CREATED_AT = "user_created_at";
    private static final String KEY_DANGER_ZONES = "danger_zones";

    // Stats caching keys
    private static final String KEY_CACHED_TOTAL_REPORTS = "cached_total_reports";
    private static final String KEY_CACHED_APPROVED_REPORTS = "cached_approved_reports";
    private static final String KEY_CACHED_DANGER_ZONES_VISITED = "cached_danger_zones_visited";

    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(String token, ApiService.LoginResponse.UserDTO user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        if (user != null) {
            editor.putLong(KEY_USER_ID, user.id != null ? user.id : -1);
            editor.putString(KEY_USER_NAME, user.name);
            editor.putString(KEY_USER_EMAIL, user.email);
            editor.putString(KEY_USER_ROLE, user.role);
            editor.putString(KEY_USER_PHONE, "");
            editor.putString(KEY_USER_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        }
        editor.apply();

        Log.d("SharedPrefManager", "User saved - ID: " + (user != null ? user.id : -1) +
                ", Email: " + (user != null ? user.email : "null") +
                ", Created At: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    }

    public void updateUserInfo(String name, String email, String phone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();

        Log.d("SharedPrefManager", "User info updated - Name: " + name + ", Email: " + email + ", Phone: " + phone);
    }

    // Save user stats to cache
    public void saveUserStats(int totalReports, int approvedReports, int dangerZonesVisited) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_CACHED_TOTAL_REPORTS, totalReports);
        editor.putInt(KEY_CACHED_APPROVED_REPORTS, approvedReports);
        editor.putInt(KEY_CACHED_DANGER_ZONES_VISITED, dangerZonesVisited);
        editor.apply();

        Log.d("SharedPrefManager", "User stats saved - Total: " + totalReports +
                ", Approved: " + approvedReports +
                ", Danger Zones: " + dangerZonesVisited);
    }

    // Get cached total reports
    public int getCachedTotalReports() {
        return sharedPreferences.getInt(KEY_CACHED_TOTAL_REPORTS, 0);
    }

    // Get cached approved reports
    public int getCachedApprovedReports() {
        return sharedPreferences.getInt(KEY_CACHED_APPROVED_REPORTS, 0);
    }

    // Get cached danger zones visited
    public int getCachedDangerZonesVisited() {
        return sharedPreferences.getInt(KEY_CACHED_DANGER_ZONES_VISITED, 0);
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "Driver");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "USER");
    }

    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }

    public String getProfileImageUrl() {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE, null);
    }

    public void saveProfileImageUrl(String url) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_IMAGE, url);
        editor.apply();

        Log.d("SharedPrefManager", "Profile image URL saved: " + url);
    }

    public String getUserCreatedAt() {
        return sharedPreferences.getString(KEY_USER_CREATED_AT, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Log.d("SharedPrefManager", "User logged out, preferences cleared");
    }

    public void saveDangerZones(List<DangerZone> zones) {
        String zonesJson = gson.toJson(zones);
        sharedPreferences.edit().putString(KEY_DANGER_ZONES, zonesJson).apply();
        Log.d("SharedPrefManager", "Saved " + (zones != null ? zones.size() : 0) + " danger zones");
    }

    public List<DangerZone> getDangerZones() {
        String zonesJson = sharedPreferences.getString(KEY_DANGER_ZONES, null);
        if (zonesJson == null) return new ArrayList<>();
        Type type = new TypeToken<List<DangerZone>>(){}.getType();
        List<DangerZone> zones = gson.fromJson(zonesJson, type);
        return zones != null ? zones : new ArrayList<>();
    }
}