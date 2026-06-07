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
import java.time.format.DateTimeFormatter;
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

    // ========== ROOT REDIRECT TO LOGIN ==========
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    // ========== LOGIN PAGE ==========
    @GetMapping("/login")
    public String showLoginPage() {
        System.out.println("=== LOGIN PAGE LOADED ===");
        return "admin/login";
    }

    // ========== PROCESS LOGIN ==========
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            System.out.println("User not found!");
            model.addAttribute("error", "Invalid email or password");
            return "admin/login";
        }

        System.out.println("User found: " + user.getEmail());
        System.out.println("Stored hash: " + user.getPassword());

        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("Password matches: " + passwordMatches);

        if (passwordMatches) {
            System.out.println("Login SUCCESS!");
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole().toString());

            if (user.getRole().toString().equals("ADMIN")) {
                return "redirect:/admin/dashboard";
            }
        }

        System.out.println("Login FAILED!");
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

    // ========== CREATE FRESH ADMIN ==========
    @GetMapping("/create-fresh-admin")
    @ResponseBody
    public String createFreshAdmin() {
        userRepository.findByEmail("admin@newsystem.com").ifPresent(user -> userRepository.delete(user));

        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@newsystem.com");
        admin.setPassword(encodedPassword);
        admin.setPhone("0771234567");
        admin.setRole(User.Role.ADMIN);
        admin.setIsActive(true);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);

        return "<h3>✅ NEW ADMIN CREATED!</h3>" +
                "<b>Email:</b> admin@newsystem.com<br>" +
                "<b>Password:</b> " + rawPassword + "<br>" +
                "<b>Encoded Hash:</b> " + encodedPassword + "<br>" +
                "<b>Hash Length:</b> " + encodedPassword.length() + "<br><br>" +
                "<a href='/login'>Go to Login Page</a>";
    }

    // ========== CHECK LOGIN CREDENTIALS ==========
    @GetMapping("/check-login")
    @ResponseBody
    public String checkLogin(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return "❌ User not found with email: " + email;
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());

        return "<h3>Login Check Result</h3>" +
                "<b>Email:</b> " + email + "<br>" +
                "<b>User exists:</b> Yes<br>" +
                "<b>Password matches:</b> " + (matches ? "✅ YES" : "❌ NO") + "<br>" +
                "<b>Stored hash:</b> " + user.getPassword() + "<br>" +
                "<b>Hash length:</b> " + user.getPassword().length() + "<br>" +
                "<b>User Role:</b> " + user.getRole();
    }

    // ========== GENERATE HASH FOR A PASSWORD ==========
    @GetMapping("/generate-hash")
    @ResponseBody
    public String generateHash(@RequestParam String password) {
        String encodedPassword = passwordEncoder.encode(password);
        return "<h3>Hash Generated</h3>" +
                "<b>Password:</b> " + password + "<br>" +
                "<b>Encoded:</b> " + encodedPassword + "<br>" +
                "<b>Length:</b> " + encodedPassword.length();
    }

    // ========== DEBUG PASSWORD ENCODER ==========
    @GetMapping("/debug-password")
    @ResponseBody
    public String debugPassword() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>Password Encoder Debug</h3>");

        String testPassword = "admin123";
        String encoded = passwordEncoder.encode(testPassword);
        sb.append("<b>Test 1 - Encoding:</b><br>");
        sb.append("Raw password: ").append(testPassword).append("<br>");
        sb.append("Encoded: ").append(encoded).append("<br>");
        sb.append("Encoded length: ").append(encoded.length()).append("<br><br>");

        boolean matches = passwordEncoder.matches(testPassword, encoded);
        sb.append("<b>Test 2 - Verification:</b><br>");
        sb.append("Matches: ").append(matches).append("<br><br>");

        User admin = userRepository.findByEmail("admin@newsystem.com").orElse(null);
        if (admin != null) {
            sb.append("<b>Test 3 - Existing admin:</b><br>");
            sb.append("Email: ").append(admin.getEmail()).append("<br>");
            sb.append("Stored hash: ").append(admin.getPassword()).append("<br>");
            sb.append("Hash length: ").append(admin.getPassword().length()).append("<br>");
            boolean existingMatch = passwordEncoder.matches("admin123", admin.getPassword());
            sb.append("Matches 'admin123': ").append(existingMatch).append("<br>");
        } else {
            sb.append("<b>Test 3 - No admin found at admin@newsystem.com</b><br>");
            sb.append("Please visit <a href='/create-fresh-admin'>/create-fresh-admin</a> first.<br>");
        }

        sb.append("<br><a href='/login'>Go to Login Page</a>");
        return sb.toString();
    }

    // ========== LIST ALL USERS ==========
    @GetMapping("/list-users")
    @ResponseBody
    public String listAllUsers() {
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>All Users in Database</h3>");
        sb.append("<table border='1' cellpadding='5'>");
        sb.append("<tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Active</th><th>Hash Length</th></tr>");

        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>").append(user.getId()).append("</td>");
            sb.append("<td>").append(user.getName()).append("</td>");
            sb.append("<td>").append(user.getEmail()).append("</td>");
            sb.append("<td>").append(user.getRole()).append("</td>");
            sb.append("<td>").append(user.getIsActive()).append("</td>");
            sb.append("<td>").append(user.getPassword() != null ? user.getPassword().length() : 0).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append("<br><a href='/login'>Go to Login Page</a>");
        return sb.toString();
    }

    // ========== DELETE ALL USERS (Emergency Reset) ==========
    @GetMapping("/reset-users")
    @ResponseBody
    public String resetUsers() {
        userRepository.deleteAll();

        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@newsystem.com");
        admin.setPassword(encodedPassword);
        admin.setPhone("0771234567");
        admin.setRole(User.Role.ADMIN);
        admin.setIsActive(true);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);

        return "<h3>✅ DATABASE RESET!</h3>" +
                "All users deleted.<br>" +
                "New admin created:<br>" +
                "<b>Email:</b> admin@newsystem.com<br>" +
                "<b>Password:</b> " + rawPassword + "<br><br>" +
                "<a href='/login'>Go to Login Page</a>";
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
                .filter(r -> r.getStatus().equals(ElephantReport.ReportStatus.PENDING))
                .count();

        long usersInDangerZones = calculateUsersInDangerZones();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalDangerZones", dangerZoneRepository.count());
        stats.put("usersInDangerZones", usersInDangerZones);
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
                double zoneRadiusKm = zone.getRadius() != null ? zone.getRadius() / 1000.0 : 1.0;
                if (distance <= zoneRadiusKm) {
                    return true;
                }
            }
        }
        return false;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
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
    public ResponseEntity<List<Map<String, Object>>> getAllDangerZones(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).build();
        }

        List<DangerZone> zones = dangerZoneRepository.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            if (zone.getCreatedAt() != null) {
                zoneMap.put("createdAt", zone.getCreatedAt());
            }
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

            zone.setZoneName((String) zoneData.get("zoneName"));
            if (zone.getZoneName() == null) {
                zone.setZoneName((String) zoneData.get("locationName"));
            }

            zone.setLatitude(Double.parseDouble(zoneData.get("latitude").toString()));
            zone.setLongitude(Double.parseDouble(zoneData.get("longitude").toString()));

            if (zoneData.get("radius") != null) {
                zone.setRadius(Integer.parseInt(zoneData.get("radius").toString()));
            } else {
                zone.setRadius(500);
            }

            if (zoneData.get("district") != null) {
                zone.setDistrict((String) zoneData.get("district"));
            }
            if (zoneData.get("roadName") != null) {
                zone.setRoadName((String) zoneData.get("roadName"));
            }

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

            String statusStr = (String) zoneData.get("status");
            if ("ACTIVE".equalsIgnoreCase(statusStr)) {
                zone.setStatus(DangerZone.Status.ACTIVE);
            } else {
                zone.setStatus(DangerZone.Status.ACTIVE);
            }

            zone.setCreatedAt(LocalDateTime.now().toString());

            DangerZone saved = dangerZoneRepository.save(zone);

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

    // ========== API: UPDATE DANGER ZONE ==========
    @PutMapping("/api/admin/danger-zones/{id}")
    @ResponseBody
    public ResponseEntity<?> updateDangerZone(@PathVariable Long id, @RequestBody Map<String, Object> zoneData, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        DangerZone zone = dangerZoneRepository.findById(id).orElse(null);
        if (zone == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (zoneData.containsKey("zoneName")) {
                zone.setZoneName((String) zoneData.get("zoneName"));
            }
            if (zoneData.containsKey("latitude")) {
                zone.setLatitude(Double.parseDouble(zoneData.get("latitude").toString()));
            }
            if (zoneData.containsKey("longitude")) {
                zone.setLongitude(Double.parseDouble(zoneData.get("longitude").toString()));
            }
            if (zoneData.containsKey("radius")) {
                zone.setRadius(Integer.parseInt(zoneData.get("radius").toString()));
            }
            if (zoneData.containsKey("district")) {
                zone.setDistrict((String) zoneData.get("district"));
            }
            if (zoneData.containsKey("riskLevel")) {
                String riskLevelStr = (String) zoneData.get("riskLevel");
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
                }
            }

            dangerZoneRepository.save(zone);
            return ResponseEntity.ok(Map.of("message", "Zone updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update zone: " + e.getMessage()));
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
            report.setStatus(ElephantReport.ReportStatus.APPROVED);
            report.setResolvedAt(LocalDateTime.now());
        } else if ("REJECTED".equals(status)) {
            report.setStatus(ElephantReport.ReportStatus.REJECTED);
        } else if ("RESOLVED".equals(status)) {
            report.setStatus(ElephantReport.ReportStatus.RESOLVED);
            report.setResolvedAt(LocalDateTime.now());
        } else if ("PENDING".equals(status)) {
            report.setStatus(ElephantReport.ReportStatus.PENDING);
        }

        reportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Status updated successfully", "status", status));
    }

    // ========== API: GET ALL USERS ==========
    @GetMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).build();
        }

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userList = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("role", user.getRole().toString());
            userMap.put("isActive", user.getIsActive());
            userMap.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            userList.add(userMap);
        }

        return ResponseEntity.ok(userList);
    }

    // ========== API: UPDATE USER STATUS ==========
    @PutMapping("/api/admin/users/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Boolean isActive = payload.get("isActive");
        if (isActive != null) {
            user.setIsActive(isActive);
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("message", "User status updated successfully"));
    }
}