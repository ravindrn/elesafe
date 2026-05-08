package com.elephant.safety.models;

public class DangerZone {
    private long id;
    private String zoneName;
    private double latitude;
    private double longitude;
    private int radius;
    private String district;
    private String roadName;
    private String riskLevel;
    private String status;
    private String createdAt;

    // No-arg constructor (REQUIRED for Retrofit)
    public DangerZone() {}

    // Constructor with fields
    public DangerZone(long id, String zoneName, double latitude, double longitude,
                      int radius, String district, String riskLevel) {
        this.id = id;
        this.zoneName = zoneName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.district = district;
        this.riskLevel = riskLevel;
        this.status = "ACTIVE";
    }

    // Getters
    public long getId() { return id; }
    public String getZoneName() { return zoneName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getRadius() { return radius; }
    public String getDistrict() { return district; }
    public String getRoadName() { return roadName; }
    public String getRiskLevel() { return riskLevel; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setRadius(int radius) { this.radius = radius; }
    public void setDistrict(String district) { this.district = district; }
    public void setRoadName(String roadName) { this.roadName = roadName; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public int getWarningColor() {
        if (riskLevel == null) return 0xFF00FF00;
        switch(riskLevel) {
            case "CRITICAL": return 0xFFFF0000; // Red
            case "HIGH": return 0xFFFF6600; // Orange
            case "MEDIUM": return 0xFFFFFF00; // Yellow
            default: return 0xFF00FF00; // Green
        }
    }

    public float getMarkerHue() {
        if (riskLevel == null) return 120;
        switch(riskLevel) {
            case "CRITICAL": return 0; // Red
            case "HIGH": return 30; // Orange
            case "MEDIUM": return 60; // Yellow
            default: return 120; // Green
        }
    }
}