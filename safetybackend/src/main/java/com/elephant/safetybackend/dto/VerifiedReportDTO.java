package com.elephant.safetybackend.dto;

import java.time.LocalDateTime;

public class VerifiedReportDTO {
    private Long id;
    private String userName;
    private Double latitude;
    private Double longitude;
    private String note;
    private Integer elephantCount;
    private LocalDateTime createdAt;

    // Constructors
    public VerifiedReportDTO() {}

    public VerifiedReportDTO(Long id, String userName, Double latitude, Double longitude,
                             String note, Integer elephantCount, LocalDateTime createdAt) {
        this.id = id;
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.elephantCount = elephantCount;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getElephantCount() { return elephantCount; }
    public void setElephantCount(Integer elephantCount) { this.elephantCount = elephantCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}