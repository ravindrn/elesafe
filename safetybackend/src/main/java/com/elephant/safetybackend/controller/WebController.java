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
    private AlertRepository alertRepository;

    @Autowired
    private ReportRepository reportRepository;

    // ============================================
    // LOGIN & AUTHENTICATION
    // ============================================

    @GetMapping("/web/login")
    public String showLoginPage() {
        return "admin/login";
    }

    @PostMapping("/web/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole().toString());

            if (user.getRole() == User.Role.ADMIN) {
                return "redirect:/web/admin/dashboard";
            } else {
                return "redirect:/web/user/home";
            }
        }

        model.addAttribute("error", "Invalid email or password");
        return "admin/login";
    }

    @GetMapping("/web/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/web/login";
    }

    // ============================================
    // ADMIN DASHBOARD
    // ============================================

    @GetMapping("/web/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/web/login";

        long totalUsers = userRepository.count();
        long activeUsers = totalUsers;
        long totalDangerZones = dangerZoneRepository.count();

        long pendingReports = 0;
        List<ElephantReport> allReports = reportRepository.findAll();
        for (ElephantReport report : allReports) {
            if (report.getStatus() == ElephantReport.Status.PENDING) {
                pendingReports++;
            }
        }

        long usersInDangerZones = 0;

        model.addAttribute("adminName", session.getAttribute("userName"));
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("totalDangerZones", totalDangerZones);
        model.addAttribute("pendingReports", pendingReports);
        model.addAttribute("usersInDangerZones", usersInDangerZones);

        return "admin/dashboard";
    }

    // ============================================
    // DANGER ZONES MANAGEMENT
    // ============================================

    @GetMapping("/web/admin/danger-zones")
    public String manageDangerZones(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/web/login";

        List<DangerZone> zones = dangerZoneRepository.findAll();
        model.addAttribute("dangerZones", zones);
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/danger-zones";
    }

    @PostMapping("/api/admin/danger-zones")
    @ResponseBody
    public ResponseEntity<?> addDangerZone(@RequestBody DangerZone zone, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Unauthorized");

        zone.setCreatedAt(LocalDateTime.now());
        DangerZone saved = dangerZoneRepository.save(zone);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/api/admin/danger-zones/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDangerZone(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Unauthorized");

        dangerZoneRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    // ============================================
    // REPORTS MANAGEMENT
    // ============================================

    @GetMapping("/web/admin/reports")
    public String manageReports(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/web/login";

        List<ElephantReport> reports = reportRepository.findAll();

        long pendingCount = 0;
        long resolvedCount = 0;
        for (ElephantReport report : reports) {
            if (report.getStatus() == ElephantReport.Status.PENDING) pendingCount++;
            if (report.getStatus() == ElephantReport.Status.RESOLVED) resolvedCount++;
        }

        model.addAttribute("reports", reports);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/reports";
    }

    @GetMapping("/api/admin/reports/{id}")
    @ResponseBody
    public ResponseEntity<?> getReportById(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        ElephantReport report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("id", report.getId());
        // getLocation removed - use area or coordinates instead
        reportData.put("area", "Report #" + report.getId());
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

    @PutMapping("/api/admin/reports/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateReportStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, HttpSession session) {
        if (!isAdmin(session)) {
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
        } else {
            return ResponseEntity.badRequest().body("Invalid status");
        }

        reportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    // ============================================
    // USER WEB VIEW
    // ============================================

    @GetMapping("/web/user/home")
    public String userHome(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/web/login";
        }
        model.addAttribute("userName", session.getAttribute("userName"));
        return "user/home";
    }

    // ============================================
    // HELPER METHOD
    // ============================================

    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("userRole") != null &&
                "ADMIN".equals(session.getAttribute("userRole"));
    }
}