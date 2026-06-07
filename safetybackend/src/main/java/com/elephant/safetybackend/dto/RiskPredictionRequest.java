package com.elephant.safetybackend.dto;

public class RiskPredictionRequest {

    private double userLatitude;
    private double userLongitude;
    private double distance_to_Zone_km;

    // --- Getters and Setters (Mandatory for Spring Boot JSON parsing) ---

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public double getDistance_to_Zone_km() {
        return distance_to_Zone_km;
    }

    public void setDistance_to_Zone_km(double distance_to_Zone_km) {
        this.distance_to_Zone_km = distance_to_Zone_km;
    }
}