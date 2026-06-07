package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;

public class RiskPredictionRequest {

    // 1. The exact live latitude
    @SerializedName("userLatitude")
    public double userLatitude;

    // 2. The exact live longitude
    @SerializedName("userLongitude")
    public double userLongitude;

    // 3. The distance to the nearest permanent zone
    @SerializedName("distance_to_Zone_km")
    public double distanceToZoneKm;

    // Constructor
    public RiskPredictionRequest(double userLatitude, double userLongitude, double distanceToZoneKm) {
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distanceToZoneKm = distanceToZoneKm;
    }

    // Getters
    public double getUserLatitude() {
        return userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public double getDistanceToZoneKm() {
        return distanceToZoneKm;
    }
}