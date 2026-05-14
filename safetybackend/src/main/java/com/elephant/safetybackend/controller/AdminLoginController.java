package com.elephant.safetybackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String showUserManagement() {
        return "admin/users";
    }

    @GetMapping("/admin/danger-zones")
    public String showDangerZones() {
        return "admin/danger-zones";
    }

    @GetMapping("/admin/reports")
    public String showReports() {
        return "admin/reports";
    }

    @GetMapping("/admin/contact-us")
    public String showContactUs() {
        return "admin/contact-us";
    }
}