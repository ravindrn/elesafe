package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.*;
import com.elephant.safetybackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DangerZoneRepository dangerZoneRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== LOGIN PAGE ==========
    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }

    // ========== PROCESS LOGIN ==========
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Invalid email or password");
            return "admin/login";
        }

        if (user.getPassword().equals(password)) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole().toString());

            if (user.getRole().toString().equals("ADMIN")) {
                return "redirect:/admin/dashboard";
            }
        }

        model.addAttribute("error", "Invalid email or password");
        return "admin/login";
    }

    // ========== ADMIN DASHBOARD PAGE ==========
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/dashboard";
    }

    // ========== DANGER ZONES PAGE ==========
    @GetMapping("/admin/danger-zones")
    public String dangerZonesPage(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/danger-zones";
    }

    // ========== REPORTS PAGE ==========
    @GetMapping("/admin/reports")
    public String reportsPage(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/reports";
    }

    // ========== CONTACT US PAGE ==========
    @GetMapping("/admin/contact-us")
    public String contactUs(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/contact-us";
    }

    // ========== LOGOUT ==========
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    // ========== ADMIN DASHBOARD API ==========
    @GetMapping("/api/admin/stats")
    @ResponseBody
    public ResponseEntity<?> getDashboardStats(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getIsActive() != null && u.getIsActive())
                .count();
        long pendingReports = reportRepository.findAll().stream()
                .filter(r -> r.getStatus().toString().equals("PENDING"))
                .count();
        
        // Calculate users in danger zones instead of danger zone count
        long usersInDangerZones = calculateUsersInDangerZones();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalDangerZones", usersInDangerZones);  // Now shows users in danger
        stats.put("pendingReports", pendingReports);
        stats.put("adminName", session.getAttribute("userName") != null ?
                session.getAttribute("userName").toString() : "Admin");

        return ResponseEntity.ok(stats);
    }

    // ========== HELPER METHODS FOR USERS IN DANGER ZONES ==========
    
    private long calculateUsersInDangerZones() {
        List<User> users = userRepository.findAll();
        List<DangerZone> dangerZones = dangerZoneRepository.findAll();
        
        if (dangerZones.isEmpty()) {
            return 0;
        }
        
        long count = 0;
        for (User user : users) {
            // Check if user is active and has location data
            if (user.getIsActive() != null && user.getIsActive() &&
                user.getLatitude() != null && user.getLongitude() != null) {
                
                if (isUserInAnyDangerZone(user, dangerZones)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isUserInAnyDangerZone(User user, List<DangerZone> dangerZones) {
        for (DangerZone zone : dangerZones) {
            if (zone.getLatitude() != null && zone.getLongitude() != null) {
                double distance = calculateDistance(
                    user.getLatitude(), user.getLongitude(),
                    zone.getLatitude(), zone.getLongitude()
                );
                // Convert radius from meters to km and compare
                double zoneRadiusKm = zone.getRadius() != null ? zone.getRadius() / 1000.0 : 1.0;
                if (distance <= zoneRadiusKm) {
                    return true;
                }
            }
        }
        return false;
    }

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

    // ========== TEST ENDPOINT: Set User Location (For Testing) ==========
    @PostMapping("/api/admin/set-location/{userId}")
    @ResponseBody
    public ResponseEntity<?> setUserLocation(@PathVariable Long userId,
                                             @RequestParam double lat,
                                             @RequestParam double lng,
                                             HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        user.setLatitude(lat);
        user.setLongitude(lng);
        user.setLastLocationUpdate(LocalDateTime.now());
        userRepository.save(user);
        
        // Check if in danger zone
        List<DangerZone> dangerZones = dangerZoneRepository.findAll();
        boolean inDanger = false;
        String dangerZoneName = null;
        
        for (DangerZone zone : dangerZones) {
            if (zone.getLatitude() != null && zone.getLongitude() != null) {
                double distance = calculateDistance(lat, lng, zone.getLatitude(), zone.getLongitude());
                double zoneRadiusKm = zone.getRadius() != null ? zone.getRadius() / 1000.0 : 1.0;
                if (distance <= zoneRadiusKm) {
                    inDanger = true;
                    dangerZoneName = zone.getZoneName();
                    break;
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("userName", user.getName());
        response.put("latitude", lat);
        response.put("longitude", lng);
        response.put("inDangerZone", inDanger);
        response.put("dangerZoneName", dangerZoneName);
        
        return ResponseEntity.ok(response);
    }

    // ========== API: GET ALL USERS WITH LOCATIONS (For Admin Monitoring) ==========
    @GetMapping("/api/admin/user-locations")
    @ResponseBody
    public ResponseEntity<?> getUserLocations(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> locationData = new ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> userLoc = new HashMap<>();
            userLoc.put("id", user.getId());
            userLoc.put("name", user.getName());
            userLoc.put("email", user.getEmail());
            userLoc.put("latitude", user.getLatitude());
            userLoc.put("longitude", user.getLongitude());
            userLoc.put("lastLocationUpdate", user.getLastLocationUpdate());
            userLoc.put("isActive", user.getIsActive());
            locationData.add(userLoc);
        }
        
        return ResponseEntity.ok(locationData);
    }

    // ========== API: GET ALL DANGER ZONES ==========
    @GetMapping("/api/admin/danger-zones")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllDangerZones(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).build();
        }

        List<DangerZone> zones = dangerZoneRepository.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (DangerZone zone : zones) {
            Map<String, Object> zoneMap = new HashMap<>();
            zoneMap.put("id", zone.getId());
            zoneMap.put("zoneName", zone.getZoneName());
            zoneMap.put("latitude", zone.getLatitude());
            zoneMap.put("longitude", zone.getLongitude());
            zoneMap.put("radius", zone.getRadius());
            zoneMap.put("district", zone.getDistrict());
            zoneMap.put("roadName", zone.getRoadName());
            zoneMap.put("riskLevel", zone.getRiskLevel().toString());
            zoneMap.put("status", zone.getStatus().toString());
            zoneMap.put("createdAt", zone.getCreatedAt());
            responseList.add(zoneMap);
        }

        return ResponseEntity.ok(responseList);
    }

    // ========== API: ADD DANGER ZONE ==========
    @PostMapping("/api/admin/danger-zones")
    @ResponseBody
    public ResponseEntity<?> addDangerZone(@RequestBody Map<String, Object> zoneData, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        try {
            DangerZone zone = new DangerZone();

            // Set basic fields
            zone.setZoneName((String) zoneData.get("zoneName"));
            if (zone.getZoneName() == null) {
                zone.setZoneName((String) zoneData.get("locationName")); // Fallback for frontend
            }

            zone.setLatitude(Double.parseDouble(zoneData.get("latitude").toString()));
            zone.setLongitude(Double.parseDouble(zoneData.get("longitude").toString()));

            // Set radius
            if (zoneData.get("radius") != null) {
                zone.setRadius(Integer.parseInt(zoneData.get("radius").toString()));
            } else {
                zone.setRadius(500);
            }

            // Set optional fields
            if (zoneData.get("district") != null) {
                zone.setDistrict((String) zoneData.get("district"));
            }
            if (zoneData.get("roadName") != null) {
                zone.setRoadName((String) zoneData.get("roadName"));
            }

            // Set RiskLevel Enum
            String riskLevelStr = (String) zoneData.get("riskLevel");
            if (riskLevelStr != null) {
                switch (riskLevelStr.toUpperCase()) {
                    case "CRITICAL":
                        zone.setRiskLevel(DangerZone.RiskLevel.CRITICAL);
                        break;
                    case "HIGH":
                        zone.setRiskLevel(DangerZone.RiskLevel.HIGH);
                        break;
                    case "MEDIUM":
                        zone.setRiskLevel(DangerZone.RiskLevel.MEDIUM);
                        break;
                    case "LOW":
                        zone.setRiskLevel(DangerZone.RiskLevel.LOW);
                        break;
                    default:
                        zone.setRiskLevel(DangerZone.RiskLevel.MEDIUM);
                }
            } else {
                zone.setRiskLevel(DangerZone.RiskLevel.MEDIUM);
            }

            // Set Status Enum
            String statusStr = (String) zoneData.get("status");
            if ("ACTIVE".equalsIgnoreCase(statusStr)) {
                zone.setStatus(DangerZone.Status.ACTIVE);
            } else {
                zone.setStatus(DangerZone.Status.INACTIVE);
            }

            zone.setCreatedAt(LocalDateTime.now());

            DangerZone saved = dangerZoneRepository.save(zone);

            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("zoneName", saved.getZoneName());
            response.put("latitude", saved.getLatitude());
            response.put("longitude", saved.getLongitude());
            response.put("riskLevel", saved.getRiskLevel().toString());
            response.put("status", saved.getStatus().toString());
            response.put("radius", saved.getRadius());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to add zone: " + e.getMessage()));
        }
    }

    // ========== API: DELETE DANGER ZONE ==========
    @DeleteMapping("/api/admin/danger-zones/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteDangerZone(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        if (!dangerZoneRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        dangerZoneRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    // ========== API: GET ALL REPORTS ==========
    @GetMapping("/api/admin/reports")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllReports(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        List<ElephantReport> reports = reportRepository.findAll();

        long pendingCount = 0;
        long resolvedCount = 0;
        List<Map<String, Object>> reportList = new ArrayList<>();

        for (ElephantReport report : reports) {
            String status = report.getStatus().toString();

            if (status.equals("PENDING")) {
                pendingCount++;
            } else {
                resolvedCount++;
            }

            Map<String, Object> reportMap = new HashMap<>();
            reportMap.put("id", report.getId());
            reportMap.put("latitude", report.getLatitude());
            reportMap.put("longitude", report.getLongitude());
            reportMap.put("elephantCount", report.getElephantCount());
            reportMap.put("status", status);
            reportMap.put("createdAt", report.getCreatedAt().toString());

            String userName = "Anonymous";
            if (report.getUser() != null) {
                userName = report.getUser().getName();
            }
            reportMap.put("userName", userName);
            reportList.add(reportMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reports", reportList);
        response.put("pendingCount", pendingCount);
        response.put("resolvedCount", resolvedCount);

        return ResponseEntity.ok(response);
    }

    // ========== API: GET SINGLE REPORT ==========
    @GetMapping("/api/admin/reports/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReportById(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        ElephantReport report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("id", report.getId());
        reportData.put("latitude", report.getLatitude());
        reportData.put("longitude", report.getLongitude());
        reportData.put("elephantCount", report.getElephantCount());
        reportData.put("status", report.getStatus().toString());
        reportData.put("createdAt", report.getCreatedAt().toString());

        String userName = "Anonymous";
        if (report.getUser() != null) {
            userName = report.getUser().getName();
        }
        reportData.put("userName", userName);

        return ResponseEntity.ok(reportData);
    }

    // ========== API: UPDATE REPORT STATUS (Approve/Reject) ==========
    @PutMapping("/api/admin/reports/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateReportStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        ElephantReport report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        String status = payload.get("status");

        if ("APPROVED".equals(status)) {
            report.setStatus(ElephantReport.Status.APPROVED);
            report.setResolvedAt(LocalDateTime.now());
        } else if ("REJECTED".equals(status)) {
            report.setStatus(ElephantReport.Status.REJECTED);
        } else if ("RESOLVED".equals(status)) {
            report.setStatus(ElephantReport.Status.APPROVED);
            report.setResolvedAt(LocalDateTime.now());
        } else if ("PENDING".equals(status)) {
            report.setStatus(ElephantReport.Status.PENDING);
        }

        reportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Status updated successfully", "status", status));
    }
}