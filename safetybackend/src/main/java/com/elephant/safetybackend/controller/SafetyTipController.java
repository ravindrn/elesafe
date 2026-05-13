package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.dto.SafetyTipDTO;
import com.elephant.safetybackend.dto.SafetyTipRequest;
import com.elephant.safetybackend.service.SafetyTipService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/safety-tips")
@CrossOrigin(origins = "*")
public class SafetyTipController {

    @Autowired
    private SafetyTipService safetyTipService;

    // Get all active safety tips (Public access)
    @GetMapping("/active")
    public ResponseEntity<List<SafetyTipDTO>> getAllActiveTips() {
        return ResponseEntity.ok(safetyTipService.getAllActiveTips());
    }

    // Get categorized safety tips (Public access)
    @GetMapping("/categorized")
    public ResponseEntity<SafetyTipService.CategorizedTipsDTO> getCategorizedTips() {
        return ResponseEntity.ok(safetyTipService.getCategorizedTips());
    }

    // Get tips by category (Public access)
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SafetyTipDTO>> getTipsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(safetyTipService.getTipsByCategory(category));
    }

    // Get single tip by id (Public access)
    @GetMapping("/{id}")
    public ResponseEntity<SafetyTipDTO> getTipById(@PathVariable Long id) {
        return ResponseEntity.ok(safetyTipService.getTipById(id));
    }

    // Create new safety tip (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SafetyTipDTO> createTip(@Valid @RequestBody SafetyTipRequest request) {
        SafetyTipDTO createdTip = safetyTipService.createTip(request);
        return new ResponseEntity<>(createdTip, HttpStatus.CREATED);
    }

    // Update safety tip (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SafetyTipDTO> updateTip(@PathVariable Long id, @Valid @RequestBody SafetyTipRequest request) {
        SafetyTipDTO updatedTip = safetyTipService.updateTip(id, request);
        return ResponseEntity.ok(updatedTip);
    }

    // Delete safety tip (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTip(@PathVariable Long id) {
        safetyTipService.deleteTip(id);
        return ResponseEntity.noContent().build();
    }

    // Toggle tip active status (Admin only)
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SafetyTipDTO> toggleTipStatus(@PathVariable Long id) {
        SafetyTipDTO updatedTip = safetyTipService.toggleTipStatus(id);
        return ResponseEntity.ok(updatedTip);
    }
}