package com.elephant.safety.models;

import java.util.Date;

public class ElephantReport {
    private long id;
    private long userId;
    private double latitude;
    private double longitude;
    private String note;
    private String photoUrl;
    private int elephantCount;
    private String status;
    private Date createdAt;

    // ⭐ ADD THIS - No-arg constructor (REQUIRED for Retrofit/Gson)
    public ElephantReport() {}

    // Constructor with fields
    public ElephantReport(long userId, double latitude, double longitude, String note, int elephantCount) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.elephantCount = elephantCount;
        this.status = "PENDING";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public int getElephantCount() { return elephantCount; }
    public void setElephantCount(int elephantCount) { this.elephantCount = elephantCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}