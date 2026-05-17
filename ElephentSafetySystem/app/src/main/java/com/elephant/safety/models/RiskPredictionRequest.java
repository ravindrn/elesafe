package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;

public class RiskPredictionRequest {

    @SerializedName("Distance_to_Zone_km")
    public double distanceToZoneKm;

    @SerializedName("userLatitude")
    public double userLatitude;   // We added this!

    @SerializedName("userLongitude")
    public double userLongitude;  // We added this!

    @SerializedName("Time_of_Day")
    public String timeOfDay;

    @SerializedName("Weather")
    public String weather;

    // Notice how this constructor now perfectly matches the 5 items you are sending!
    public RiskPredictionRequest(double distance, double lat, double lng, String time, String weather) {
        this.distanceToZoneKm = distance;
        this.userLatitude = lat;
        this.userLongitude = lng;
        this.timeOfDay = time;
        this.weather = weather;
    }
}