package com.elephant.safety.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.DangerZone;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {
    private static final String PREF_NAME = "ElephantSafetyPref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_DANGER_ZONES = "danger_zones";

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

    // Updated to use UserDTO with public fields
    public void saveUser(String token, ApiService.LoginResponse.UserDTO user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        if (user != null) {
            editor.putLong(KEY_USER_ID, user.id != null ? user.id : -1);
            editor.putString(KEY_USER_NAME, user.name);
            editor.putString(KEY_USER_EMAIL, user.email);
            editor.putString(KEY_USER_ROLE, user.role);
        }
        editor.apply();
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

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void saveDangerZones(List<DangerZone> zones) {
        String zonesJson = gson.toJson(zones);
        sharedPreferences.edit().putString(KEY_DANGER_ZONES, zonesJson).apply();
    }

    public List<DangerZone> getDangerZones() {
        String zonesJson = sharedPreferences.getString(KEY_DANGER_ZONES, null);
        if (zonesJson == null) return new ArrayList<>();
        Type type = new TypeToken<List<DangerZone>>(){}.getType();
        List<DangerZone> zones = gson.fromJson(zonesJson, type);
        return zones != null ? zones : new ArrayList<>();
    }
}