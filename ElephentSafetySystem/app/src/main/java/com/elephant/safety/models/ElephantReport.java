package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ElephantReport implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("note")
    private String note;

    @SerializedName("elephantCount")
    private int elephantCount;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    // Default constructor (required for Retrofit)
    public ElephantReport() {}

    // Constructor for creating new reports
    public ElephantReport(long userId, double latitude, double longitude, String note, int elephantCount) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.elephantCount = elephantCount;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getElephantCount() {
        return elephantCount;
    }

    public void setElephantCount(int elephantCount) {
        this.elephantCount = elephantCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}