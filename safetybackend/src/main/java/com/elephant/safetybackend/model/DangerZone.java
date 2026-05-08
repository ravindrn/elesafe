package com.elephant.safetybackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "danger_zones")
public class DangerZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String zoneName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Integer radius = 500;

    private String district;

    private String roadName;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    // Constructors
    public DangerZone() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getRadius() { return radius; }
    public void setRadius(Integer radius) { this.radius = radius; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getRoadName() { return roadName; }
    public void setRoadName(String roadName) { this.roadName = roadName; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper method for getting color based on risk level
    public int getWarningColor() {
        switch(riskLevel) {
            case CRITICAL: return 0xFFFF0000; // Red
            case HIGH: return 0xFFFF6600; // Orange
            case MEDIUM: return 0xFFFFFF00; // Yellow
            default: return 0xFF00FF00; // Green
        }
    }

    public float getMarkerColor() {
        switch(riskLevel) {
            case CRITICAL: return 0; // Red
            case HIGH: return 0; // Orange - need custom marker
            case MEDIUM: return 60; // Yellow
            default: return 120; // Green
        }
    }
}