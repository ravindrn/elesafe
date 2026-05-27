package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.EmergencyContactDTO;
import com.elephant.safetybackend.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
@CrossOrigin(origins = "*")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactService emergencyContactService;

    // Get all active emergency contacts (Public)
    @GetMapping("/active")
    public ResponseEntity<List<EmergencyContactDTO>> getAllActiveContacts() {
        return ResponseEntity.ok(emergencyContactService.getAllActiveContacts());
    }

    // Get categorized emergency contacts (Public)
    @GetMapping("/categorized")
    public ResponseEntity<EmergencyContactService.CategorizedContactsDTO> getCategorizedContacts() {
        return ResponseEntity.ok(emergencyContactService.getCategorizedContacts());
    }

    // Get contacts by category (Public)
    @GetMapping("/category/{category}")
    public ResponseEntity<List<EmergencyContactDTO>> getContactsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(emergencyContactService.getContactsByCategory(category));
    }

    // Create new emergency contact (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyContactDTO> createContact(@RequestBody EmergencyContactDTO dto) {
        return new ResponseEntity<>(emergencyContactService.createContact(dto), HttpStatus.CREATED);
    }

    // Update emergency contact (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyContactDTO> updateContact(@PathVariable Long id, @RequestBody EmergencyContactDTO dto) {
        return ResponseEntity.ok(emergencyContactService.updateContact(id, dto));
    }

    // Delete emergency contact (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        emergencyContactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    // Toggle contact status (Admin only)
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyContactDTO> toggleContactStatus(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyContactService.toggleContactStatus(id));
    }
}