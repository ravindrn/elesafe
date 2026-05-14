package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.*;
import com.elephant.safetybackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // ========== PROCESS LOGIN (POST only) ==========
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        System.out.println("=== LOGIN ATTEMPT: " + username);

        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            System.out.println("USER NOT FOUND");
            model.addAttribute("error", "Invalid email or password");
            return "admin/login";
        }

        System.out.println("USER FOUND: " + user.getEmail());
        System.out.println("ROLE: " + user.getRole());

        if (user.getPassword().equals(password)) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userRole", user.getRole().toString());

            System.out.println("LOGIN SUCCESSFUL! Redirecting to /admin/dashboard");

            if (user.getRole().toString().equals("ADMIN")) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/home";
            }
        }

        System.out.println("PASSWORD MISMATCH");
        model.addAttribute("error", "Invalid email or password");
        return "admin/login";
    }

    // ========== ADMIN DASHBOARD API ==========
    @GetMapping("/api/admin/stats")
    @ResponseBody
    public ResponseEntity<?> getDashboardStats(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        long totalUsers = userRepository.count();
        long totalDangerZones = dangerZoneRepository.count();

        long pendingReports = 0;
        List<ElephantReport> allReports = reportRepository.findAll();
        for (ElephantReport report : allReports) {
            if (report.getStatus().toString().equals("PENDING")) {
                pendingReports++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", totalUsers);
        stats.put("totalDangerZones", totalDangerZones);
        stats.put("pendingReports", pendingReports);

        Object adminName = session.getAttribute("userName");
        stats.put("adminName", adminName != null ? adminName.toString() : "Admin");

        return ResponseEntity.ok(stats);
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
}