package com.elephant.safetybackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // This will load login.html from templates/admin/
    }
    
    @GetMapping("/admin/dashboard")
    public String showDashboard() {
        return "dashboard";  // This will load dashboard.html
    }
    
    @GetMapping("/admin/users")
    public String showUserManagement() {
        return "users";
    }
    
    @GetMapping("/admin/danger-zones")
    public String showDangerZones() {
        return "danger-zones";
    }
    
    @GetMapping("/admin/reports")
    public String showReports() {
        return "reports";
    }
    
    @GetMapping("/admin/contact-us")
    public String showContactUs() {
        return "contact-us";
    }
}