package com.elephant.safetybackend.dto;

public class RiskPredictionRequest {
    public double Distance_to_Zone_km;

    // Coordinates coming FROM Android
    public double userLatitude;
    public double userLongitude;

    // Elephant count going TO Python
    public int Recent_Elephant_Count;

    public String Time_of_Day;
    public String Weather;

    // Default constructor
    public RiskPredictionRequest() {}

    // Getters and Setters
    public double getDistance_to_Zone_km() { return Distance_to_Zone_km; }
    public void setDistance_to_Zone_km(double distance_to_Zone_km) { Distance_to_Zone_km = distance_to_Zone_km; }

    public double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(double userLatitude) { this.userLatitude = userLatitude; }

    public double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(double userLongitude) { this.userLongitude = userLongitude; }

    public int getRecent_Elephant_Count() { return Recent_Elephant_Count; }
    public void setRecent_Elephant_Count(int recent_Elephant_Count) { Recent_Elephant_Count = recent_Elephant_Count; }

    public String getTime_of_Day() { return Time_of_Day; }
    public void setTime_of_Day(String time_of_Day) { Time_of_Day = time_of_Day; }

    public String getWeather() { return Weather; }
    public void setWeather(String weather) { Weather = weather; }
}