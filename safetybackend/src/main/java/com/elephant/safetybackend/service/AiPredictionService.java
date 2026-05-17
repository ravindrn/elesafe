package com.elephant.safetybackend.service;

import com.elephant.safetybackend.dto.RiskPredictionRequest;
import com.elephant.safetybackend.dto.RiskPredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class AiPredictionService {

    private final RestTemplate restTemplate;

    // The URL where your Python Flask server is running locally.
    private final String FLASK_API_URL = "http://localhost:5000/predict_risk";

    public AiPredictionService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calls the Python Random Forest model to predict the current elephant risk level.
     */
    public String getRiskPrediction(double distance, int elephantCount, String timeOfDay, String weather) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RiskPredictionRequest requestData = new RiskPredictionRequest();
            requestData.setDistance_to_Zone_km(distance);
            requestData.setRecent_Elephant_Count(elephantCount);
            requestData.setTime_of_Day(timeOfDay);
            requestData.setWeather(weather);

            HttpEntity<RiskPredictionRequest> requestEntity = new HttpEntity<>(requestData, headers);

            ResponseEntity<RiskPredictionResponse> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    requestEntity,
                    RiskPredictionResponse.class
            );

            if (response.getBody() != null && "success".equals(response.getBody().getStatus())) {
                return response.getBody().getPredicted_risk();
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to reach Python AI API. Is Flask running? Details: " + e.getMessage());
        }

        return "UNKNOWN";
    }
}