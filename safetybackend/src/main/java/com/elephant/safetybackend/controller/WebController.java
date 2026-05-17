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
        return ResponseEntity.status(403).body("Unauthorized");
    }

    long totalUsers = userRepository.count();
    
    // Active users (users with location updated in last 5 minutes)
    long activeUsers = userRepository.findAll().stream()
            .filter(u -> u.getIsActive() != null && u.getIsActive())
            .count();
    
    // Users in danger zones (based on their current location)
    long usersInDanger = calculateUsersInDangerZones();
    
    // Pending reports
    long pendingReports = reportRepository.findAll().stream()
            .filter(r -> r.getStatus().toString().equals("PENDING"))
            .count();

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalUsers", totalUsers);
    stats.put("activeUsers", activeUsers);
    stats.put("totalDangerZones", usersInDanger);
    stats.put("pendingReports", pendingReports);
    stats.put("adminName", session.getAttribute("userName") != null ? 
               session.getAttribute("userName").toString() : "Admin");

    return ResponseEntity.ok(stats);
}

// Helper method to calculate users in danger zones
private long calculateUsersInDangerZones() {
    List<User> users = userRepository.findAll();
    List<DangerZone> dangerZones = dangerZoneRepository.findAll();
    
    if (dangerZones.isEmpty()) {
        return 0;
    }
    
    long count = 0;
    for (User user : users) {
        if (user.getLatitude() != null && user.getLongitude() != null && 
            user.getIsActive() != null && user.getIsActive()) {
            if (isUserInDangerZone(user, dangerZones)) {
                count++;
            }
        }
    }
    return count;
}

private boolean isUserInDangerZone(User user, List<DangerZone> dangerZones) {
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

    // ========== API: GET ALL DANGER ZONES ==========
    @GetMapping("/api/admin/danger-zones")
    @ResponseBody
    public ResponseEntity<?> getAllDangerZones(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        List<DangerZone> zones = dangerZoneRepository.findAll();
        return ResponseEntity.ok(zones);
    }

    // ========== API: ADD DANGER ZONE ==========
    @PostMapping("/api/admin/danger-zones")
    @ResponseBody
    public ResponseEntity<?> addDangerZone(@RequestBody DangerZone zone, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        zone.setCreatedAt(LocalDateTime.now());
        DangerZone saved = dangerZoneRepository.save(zone);
        return ResponseEntity.ok(saved);
    }

    // ========== API: DELETE DANGER ZONE ==========
    @DeleteMapping("/api/admin/danger-zones/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDangerZone(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        dangerZoneRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    // ========== API: GET ALL REPORTS ==========
    @GetMapping("/api/admin/reports")
    @ResponseBody
    public ResponseEntity<?> getAllReports(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        List<ElephantReport> reports = reportRepository.findAll();

        long pendingCount = 0;
        long resolvedCount = 0;
        for (ElephantReport report : reports) {
            if (report.getStatus().toString().equals("PENDING")) pendingCount++;
            if (report.getStatus().toString().equals("RESOLVED")) resolvedCount++;
        }

        List<Map<String, Object>> reportList = new java.util.ArrayList<>();
        for (ElephantReport report : reports) {
            Map<String, Object> reportMap = new HashMap<>();
            reportMap.put("id", report.getId());
            reportMap.put("latitude", report.getLatitude());
            reportMap.put("longitude", report.getLongitude());
            reportMap.put("elephantCount", report.getElephantCount());
            reportMap.put("status", report.getStatus().toString());
            reportMap.put("createdAt", report.getCreatedAt());

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
    public ResponseEntity<?> getReportById(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
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
        reportData.put("createdAt", report.getCreatedAt());

        String userName = "Anonymous";
        if (report.getUser() != null) {
            userName = report.getUser().getName();
        }
        reportData.put("userName", userName);

        return ResponseEntity.ok(reportData);
    }

    // ========== API: UPDATE REPORT STATUS ==========
    @PutMapping("/api/admin/reports/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateReportStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        ElephantReport report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        String status = payload.get("status");
        if ("RESOLVED".equals(status)) {
            report.setStatus(ElephantReport.Status.RESOLVED);
        } else if ("PENDING".equals(status)) {
            report.setStatus(ElephantReport.Status.PENDING);
        }
        reportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    @GetMapping("/api/admin/users-in-danger")
@ResponseBody
public ResponseEntity<?> getUsersInDangerZones(HttpSession session) {
    if (session.getAttribute("userId") == null) {
        return ResponseEntity.status(403).body("Unauthorized");
    }
    
    List<DangerZone> dangerZones = dangerZoneRepository.findAll();
    List<Map<String, Object>> usersInDanger = new java.util.ArrayList<>();
    
    List<User> users = userRepository.findAll();
    for (User user : users) {
        if (user.getLatitude() != null && user.getLongitude() != null && 
            user.getIsActive() != null && user.getIsActive()) {
            for (DangerZone zone : dangerZones) {
                double distance = calculateDistance(
                    user.getLatitude(), user.getLongitude(),
                    zone.getLatitude(), zone.getLongitude()
                );
                double zoneRadiusKm = zone.getRadius() != null ? zone.getRadius() / 1000.0 : 1.0;
                if (distance <= zoneRadiusKm) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("name", user.getName());
                    userData.put("email", user.getEmail());
                    userData.put("phone", user.getPhone());
                    userData.put("latitude", user.getLatitude());
                    userData.put("longitude", user.getLongitude());
                    userData.put("dangerZone", zone.getZoneName());
                    userData.put("distance", Math.round(distance * 100) / 100.0);
                    usersInDanger.add(userData);
                    break;
                }
            }
        }
    }
    
    return ResponseEntity.ok(usersInDanger);
}
}