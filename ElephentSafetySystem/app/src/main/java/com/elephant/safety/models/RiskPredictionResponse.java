package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;

public class RiskPredictionResponse {
    @SerializedName("status")
    public String status;

    @SerializedName("predicted_risk")
    public String predictedRisk;
}