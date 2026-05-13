package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.ReportRequestDTO;
import com.elephant.safetybackend.dto.ReportResponseDTO;
import com.elephant.safetybackend.model.User;
import com.elephant.safetybackend.repository.UserRepository;
import com.elephant.safetybackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequestDTO request) {
        System.out.println("=== REPORT SUBMISSION DEBUG ===");
        System.out.println("Request received: lat=" + request.getLatitude() +
                ", lng=" + request.getLongitude() +
                ", note=" + request.getNote() +
                ", count=" + request.getElephantCount());

        try {
            // Get authentication from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + auth);
            System.out.println("Is authenticated: " + (auth != null ? auth.isAuthenticated() : false));

            if (auth == null || !auth.isAuthenticated()) {
                System.out.println("No authentication found!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            String email = auth.getName();
            System.out.println("Email from security context: '" + email + "'");

            // Find user by email
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("User NOT found for email: '" + email + "'");
                // List all users for debugging
                System.out.println("All users in database:");
                userRepository.findAll().forEach(u -> System.out.println("  - '" + u.getEmail() + "' (ID: " + u.getId() + ")"));

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "User not found with email: " + email));
            }

            System.out.println("User found: ID=" + user.getId() +
                    ", Email='" + user.getEmail() +
                    "', Name='" + user.getName() + "'");

            ReportResponseDTO response = reportService.submitReport(request, email);

            Map<String, Object> result = new HashMap<>();
            result.put("id", response.getId());
            result.put("message", "Report submitted successfully");
            result.put("status", response.getStatus());

            System.out.println("Report submitted successfully with ID: " + response.getId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("Error submitting report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}