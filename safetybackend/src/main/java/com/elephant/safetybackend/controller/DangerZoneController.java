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
    public ResponseEntity<List<DangerZone>> getAllZones() {
        List<DangerZone> zones = dangerZoneService.getAllActiveZones();
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DangerZone>> getNearbyZones(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") int radius) {
        List<DangerZone> zones = dangerZoneService.getNearbyZones(lat, lng, radius);
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<DangerZone>> getHighRiskZones() {
        List<DangerZone> zones = dangerZoneService.getHighRiskZones();
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/by-district/{district}")
    public ResponseEntity<List<DangerZone>> getZonesByDistrict(@PathVariable String district) {
        List<DangerZone> zones = dangerZoneService.getZonesByDistrict(district);
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

    @PostMapping("/admin")
    public ResponseEntity<DangerZone> addZone(@RequestBody DangerZone zone) {
        DangerZone saved = dangerZoneService.addZone(zone);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<DangerZone> updateZone(@PathVariable Long id, @RequestBody DangerZone zone) {
        zone.setId(id);
        DangerZone updated = dangerZoneService.updateZone(id, zone);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        dangerZoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getZoneStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", dangerZoneService.getAllActiveZones().size());
        stats.put("highRisk", dangerZoneService.getHighRiskZones().size());
        stats.put("byDistrict", dangerZoneService.getZonesByDistrictStats());
        return ResponseEntity.ok(stats);
    }
}