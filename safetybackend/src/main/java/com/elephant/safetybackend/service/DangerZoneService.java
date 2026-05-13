package com.elephant.safetybackend.service;

import com.elephant.safetybackend.model.DangerZone;
import com.elephant.safetybackend.repository.DangerZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DangerZoneService {

    @Autowired
    private DangerZoneRepository dangerZoneRepository;

    public List<DangerZone> getAllActiveZones() {
        try {
            List<DangerZone> zones = dangerZoneRepository.findAll();
            System.out.println("Found " + zones.size() + " zones in database");
            return zones;
        } catch (Exception e) {
            System.err.println("Error fetching zones: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<DangerZone> getNearbyZones(double lat, double lng, int radiusMeters) {
        // Return all zones for now
        return getAllActiveZones();
    }

    public List<DangerZone> getHighRiskZones() {
        List<DangerZone> allZones = getAllActiveZones();
        List<DangerZone> highRiskZones = new ArrayList<>();
        for (DangerZone zone : allZones) {
            if (zone.getRiskLevel() == DangerZone.RiskLevel.HIGH ||
                    zone.getRiskLevel() == DangerZone.RiskLevel.CRITICAL) {
                highRiskZones.add(zone);
            }
        }
        return highRiskZones;
    }

    public List<DangerZone> getZonesByDistrict(String district) {
        List<DangerZone> allZones = getAllActiveZones();
        List<DangerZone> districtZones = new ArrayList<>();
        for (DangerZone zone : allZones) {
            if (district.equals(zone.getDistrict())) {
                districtZones.add(zone);
            }
        }
        return districtZones;
    }

    public Map<String, Long> getZonesByDistrictStats() {
        List<DangerZone> zones = getAllActiveZones();
        Map<String, Long> stats = new HashMap<>();
        for (DangerZone zone : zones) {
            String district = zone.getDistrict();
            if (district != null) {
                stats.put(district, stats.getOrDefault(district, 0L) + 1);
            }
        }
        return stats;
    }

    public DangerZone addZone(DangerZone zone) {
        return dangerZoneRepository.save(zone);
    }

    public DangerZone updateZone(Long id, DangerZone zone) {
        zone.setId(id);
        return dangerZoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        dangerZoneRepository.deleteById(id);
    }

    public DangerZone getZoneById(Long id) {
        return dangerZoneRepository.findById(id).orElse(null);
    }
}