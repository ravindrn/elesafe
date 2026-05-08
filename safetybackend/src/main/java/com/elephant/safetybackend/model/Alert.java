package com.elephant.safetybackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private DangerZone dangerZone;

    private String alertType;

    private Double userLatitude;

    private Double userLongitude;

    private Double distanceToZone;

    private Boolean acknowledged = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Alert() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public DangerZone getDangerZone() { return dangerZone; }
    public void setDangerZone(DangerZone dangerZone) { this.dangerZone = dangerZone; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public Double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(Double userLatitude) { this.userLatitude = userLatitude; }

    public Double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(Double userLongitude) { this.userLongitude = userLongitude; }

    public Double getDistanceToZone() { return distanceToZone; }
    public void setDistanceToZone(Double distanceToZone) { this.distanceToZone = distanceToZone; }

    public Boolean getAcknowledged() { return acknowledged; }
    public void setAcknowledged(Boolean acknowledged) { this.acknowledged = acknowledged; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}