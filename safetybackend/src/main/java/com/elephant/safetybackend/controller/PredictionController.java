package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.RiskPredictionRequest;
import com.elephant.safetybackend.dto.RiskPredictionResponse;
import com.elephant.safetybackend.service.AiPredictionService;
import com.elephant.safetybackend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/ml")
public class PredictionController {

    @Autowired
    private AiPredictionService aiPredictionService;

    @Autowired
    private ReportRepository reportRepository;

    @PostMapping("/check-risk")
    public ResponseEntity<RiskPredictionResponse> checkRisk(@RequestBody RiskPredictionRequest request) {

        // 1. Query MySQL for live active elephants within a 10km radius
        Integer recentElephantCount = reportRepository.getRecentElephantCountNearUser(
                request.getUserLatitude(),
                request.getUserLongitude(),
                10.0
        );
        int realElephantCount = (recentElephantCount != null) ? recentElephantCount : 0;

        System.out.println("Live Database Check: Found " + realElephantCount + " elephants near user.");

        // 2. Generate dynamic Time and Weather
        String dynamicTime = getCurrentTimeOfDay();
        String dynamicWeather = "Clear"; // Placeholder until we connect the Weather API

        // 3. Ask Python AI
        String riskLevel = aiPredictionService.getRiskPrediction(
                request.getDistance_to_Zone_km(),
                realElephantCount,
                dynamicTime,
                dynamicWeather
        );

        // =========================================================
        // 🚨 SAFETY NET 1: The "Permanent Zone" Override
        // =========================================================
        if ("LOW".equals(riskLevel) && request.getDistance_to_Zone_km() <= 2.0) {
            riskLevel = "MEDIUM";
            System.out.println("SAFETY NET 1 TRIGGERED: Upgraded LOW to MEDIUM due to zone proximity.");
        }

        // =========================================================
        // 🚨 SAFETY NET 2: The "Live Sighting" Override
        // =========================================================
        if (realElephantCount >= 5) {
            riskLevel = "CRITICAL";
            System.out.println("SAFETY NET 2 TRIGGERED: Forced CRITICAL due to a large herd nearby.");
        } else if (realElephantCount > 0 && "LOW".equals(riskLevel)) {
            riskLevel = "HIGH";
            System.out.println("SAFETY NET 2 TRIGGERED: Forced HIGH due to active live sightings.");
        }
        // =========================================================

        // 4. Respond to Android
        RiskPredictionResponse response = new RiskPredictionResponse();
        response.setStatus("success");
        response.setPredicted_risk(riskLevel);

        return ResponseEntity.ok(response);
    }

    /**
     * Helper Method: Extracts system time and converts it to match your ML Model classes
     */
    private String getCurrentTimeOfDay() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 19) return "Dusk";
        return "Night";
    }
}