package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class VerifiedReport implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("userName")
    private String userName;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("note")
    private String note;

    @SerializedName("elephantCount")
    private int elephantCount;

    @SerializedName("createdAt")
    private String createdAt;

    public VerifiedReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getElephantCount() { return elephantCount; }
    public void setElephantCount(int elephantCount) { this.elephantCount = elephantCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}