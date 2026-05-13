package com.elephant.safetybackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "danger_zones")
public class DangerZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Integer radius = 500;

    private String district;

    @Column(name = "road_name")
    private String roadName;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    public DangerZone() {}

    // Getters
    public Long getId() { return id; }
    public String getZoneName() { return zoneName; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getRadius() { return radius; }
    public String getDistrict() { return district; }
    public String getRoadName() { return roadName; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public Status getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setRadius(Integer radius) { this.radius = radius; }
    public void setDistrict(String district) { this.district = district; }
    public void setRoadName(String roadName) { this.roadName = roadName; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public void setStatus(Status status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}