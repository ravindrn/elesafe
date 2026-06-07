package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.EmergencyContact;
import com.elephant.safetybackend.repository.EmergencyContactRepository;
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
@RequestMapping("/admin/emergency-contacts")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    @GetMapping
    public String emergencyContactsPage(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", session.getAttribute("userName"));
        return "admin/emergency-contacts";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<EmergencyContact>> getAllEmergencyContacts(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(emergencyContactRepository.findAll());
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addEmergencyContact(@RequestBody EmergencyContact contact, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        try {
            contact.setCreatedAt(LocalDateTime.now());
            contact.setUpdatedAt(LocalDateTime.now());
            EmergencyContact saved = emergencyContactRepository.save(contact);
            return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateEmergencyContact(@PathVariable Long id, @RequestBody EmergencyContact contact, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        try {
            contact.setId(id);
            contact.setUpdatedAt(LocalDateTime.now());
            emergencyContactRepository.save(contact);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteEmergencyContact(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        emergencyContactRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}