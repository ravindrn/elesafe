package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.*;
import com.elephant.safetybackend.repository.*;
import com.elephant.safetybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private DangerZoneRepository dangerZoneRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get user stats (total reports, approved reports, danger zones visited)
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Get user's reports
        List<ElephantReport> userReports = reportRepository.findByUser(user);
        long totalReports = userReports.size();
        long approvedReports = userReports.stream()
                .filter(r -> r.getStatus() == ElephantReport.ReportStatus.APPROVED)
                .count();

        // Calculate danger zones visited (based on user's report locations)
        long dangerZonesVisited = calculateDangerZonesVisited(userReports);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", totalReports);
        stats.put("approvedReports", approvedReports);
        stats.put("dangerZonesVisited", dangerZonesVisited);

        return ResponseEntity.ok(stats);
    }

    // Calculate unique danger zones visited by user
    private long calculateDangerZonesVisited(List<ElephantReport> reports) {
        if (reports.isEmpty()) {
            return 0;
        }

        List<DangerZone> allZones = dangerZoneRepository.findAll();
        if (allZones.isEmpty()) {
            return 0;
        }

        long visitedZones = 0;
        for (DangerZone zone : allZones) {
            boolean visited = false;
            for (ElephantReport report : reports) {
                double distance = calculateDistance(
                        report.getLatitude(), report.getLongitude(),
                        zone.getLatitude(), zone.getLongitude()
                );
                double zoneRadiusKm = zone.getRadius() != null ? zone.getRadius() / 1000.0 : 1.0;
                if (distance <= zoneRadiusKm) {
                    visited = true;
                    break;
                }
            }
            if (visited) {
                visitedZones++;
            }
        }
        return visitedZones;
    }

    // Calculate distance between two coordinates using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Update user profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Update fields
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check if email is already taken by another user
            if (!request.getEmail().equals(email) && userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", user.getRole().toString()
        ));

        return ResponseEntity.ok(response);
    }

    // Change password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // Inner classes
    public static class UpdateProfileRequest {
        private String name;
        private String email;
        private String phone;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}