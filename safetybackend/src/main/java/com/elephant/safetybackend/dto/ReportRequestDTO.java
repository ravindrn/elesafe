package com.elephant.safetybackend.dto;

public class ReportRequestDTO {
    private Long userId;
    private Double latitude;
    private Double longitude;
    private String note;
    private Integer elephantCount;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getElephantCount() {
        return elephantCount;
    }

    public void setElephantCount(Integer elephantCount) {
        this.elephantCount = elephantCount;
    }
}