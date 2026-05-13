package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.*;
import com.elephant.safetybackend.service.InsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "*")
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    // Get verified reports (Public)
    @GetMapping("/verified-reports")
    public ResponseEntity<List<VerifiedReportDTO>> getVerifiedReports() {
        return ResponseEntity.ok(insightsService.getVerifiedReports());
    }

    // Get all news (Public)
    @GetMapping("/news")
    public ResponseEntity<List<NewsItemDTO>> getAllNews() {
        return ResponseEntity.ok(insightsService.getAllNews());
    }

    // Get recent accidents (Public)
    @GetMapping("/recent-accidents")
    public ResponseEntity<List<NewsItemDTO>> getRecentAccidents() {
        return ResponseEntity.ok(insightsService.getRecentAccidents());
    }

    // Get dashboard statistics (Public)
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(insightsService.getDashboardStats());
    }
}