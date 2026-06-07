package com.elephant.safety.api;

import com.elephant.safety.models.RiskPredictionRequest;
import com.elephant.safety.models.RiskPredictionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ElephantApiService {

    // This matches the URL of your Spring Boot controller!
    @POST("/api/ml/check-risk")
    Call<RiskPredictionResponse> checkCurrentRisk(@Body RiskPredictionRequest request);
}