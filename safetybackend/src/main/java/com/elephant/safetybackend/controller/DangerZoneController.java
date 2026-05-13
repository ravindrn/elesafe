package com.elephant.safetybackend.controller;

import com.elephant.safetybackend.model.DangerZone;
import com.elephant.safetybackend.service.DangerZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "*")
public class DangerZoneController {

    @Autowired
    private DangerZoneService dangerZoneService;

    @GetMapping
    public ResponseEntity<?> getAllZones() {
        try {
            List<DangerZone> zones = dangerZoneService.getAllActiveZones();
            System.out.println("Returning " + zones.size() + " danger zones");
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Backend is working!");
        response.put("zones_count", String.valueOf(dangerZoneService.getAllActiveZones().size()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DangerZone>> getNearbyZones(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") int radius) {
        List<DangerZone> zones = dangerZoneService.getNearbyZones(lat, lng, radius);
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DangerZone> getZoneById(@PathVariable Long id) {
        DangerZone zone = dangerZoneService.getZoneById(id);
        if (zone != null) {
            return ResponseEntity.ok(zone);
        }
        return ResponseEntity.notFound().build();
    }
}