package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.RiskPredictionRequest;
import com.elephant.safetybackend.dto.RiskPredictionResponse;
import com.elephant.safetybackend.service.AiPredictionService;
import com.elephant.safetybackend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ml")
public class PredictionController {

    @Autowired
    private AiPredictionService aiPredictionService;

    // Inject your repository so we can query the database!
    @Autowired
    private ReportRepository reportRepository;

    @PostMapping("/check-risk")
    public ResponseEntity<RiskPredictionResponse> checkRisk(@RequestBody RiskPredictionRequest request) {

        // 1. Query the database for REAL active elephants within a 10km radius of the user
        Integer recentElephantCount = reportRepository.getRecentElephantCountNearUser(
                request.getUserLatitude(),
                request.getUserLongitude(),
                10.0 // Searching within a 10 km radius
        );

        // SQL might return null if there are absolutely no reports, so we safely convert it to 0
        int realElephantCount = (recentElephantCount != null) ? recentElephantCount : 0;

        System.out.println("Live Database Check: Found " + realElephantCount + " elephants near user.");

        // 2. Pass the REAL data to the Python AI Model
        String riskLevel = aiPredictionService.getRiskPrediction(
                request.getDistance_to_Zone_km(),
                realElephantCount,
                request.getTime_of_Day(),
                request.getWeather()
        );

        // =========================================================
        // 🚨 THE SAFETY NET OVERRIDE 🚨
        // If the AI thinks it's "LOW" risk, but the user is physically
        // inside or very close to a known permanent danger zone (< 2.0 km),
        // we override the AI and force a "MEDIUM" baseline warning.
        // =========================================================
        if ("LOW".equals(riskLevel) && request.getDistance_to_Zone_km() <= 2.0) {
            riskLevel = "MEDIUM";
            System.out.println("SAFETY NET TRIGGERED: Upgraded LOW to MEDIUM because user is near a Danger Zone.");
        }
        // =========================================================

        // 3. Package the prediction and send it back to Android
        RiskPredictionResponse response = new RiskPredictionResponse();
        response.setStatus("success");
        response.setPredicted_risk(riskLevel);

        return ResponseEntity.ok(response);
    }
}