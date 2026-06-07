package com.elephant.safetybackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiPredictionService {

    private final RestTemplate restTemplate;

    // IMPORTANT: Make sure this URL matches your Flask @app.route exactly!
    // I am using your old URL here (/predict_risk) assuming that is what your Python code uses.
    private final String FLASK_API_URL = "http://localhost:5000/predict_risk";

    public AiPredictionService() {
        // initialized only ONCE for high performance!
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calls the Python Random Forest model to predict the current elephant risk level.
     */
    public String getRiskPrediction(double distance, int elephantCount, String timeOfDay, String weather) {

        try {
            // 1. Set explicit headers (From your Old Code)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. Package ONLY the exact 4 features Python is expecting (From the New Code)
            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("Distance_to_Zone_km", distance);
            pythonRequest.put("Recent_Elephant_Count", elephantCount);
            pythonRequest.put("Time_of_Day", timeOfDay);
            pythonRequest.put("Weather", weather);

            // 3. Wrap headers and data together
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(pythonRequest, headers);

            // 4. Send to Python Flask server
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    requestEntity,
                    Map.class
            );

            // 5. Extract the predicted risk
            if (response.getBody() != null && response.getBody().containsKey("predicted_risk")) {
                return response.getBody().get("predicted_risk").toString();
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to reach Python AI API. Is Flask running? Details: " + e.getMessage());
        }

        // Failsafe if Python is offline
        return "LOW";
    }
}