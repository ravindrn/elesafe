package com.elephant.safetybackend.dto;

public class RiskPredictionResponse {
    public String status;
    public String predicted_risk;

    // Default constructor
    public RiskPredictionResponse() {}

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPredicted_risk() { return predicted_risk; }
    public void setPredicted_risk(String predicted_risk) { this.predicted_risk = predicted_risk; }
}