package com.elephant.safetybackend.service;

import com.elephant.safetybackend.model.DangerZone;
import com.elephant.safetybackend.repository.DangerZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DangerZoneService {

    @Autowired
    private DangerZoneRepository dangerZoneRepository;

    public List<DangerZone> getAllActiveZones() {
        return dangerZoneRepository.findByStatus(DangerZone.Status.ACTIVE);
    }

    public List<DangerZone> getNearbyZones(double lat, double lng, int radiusMeters) {
        double radiusKm = radiusMeters / 1000.0;
        return dangerZoneRepository.findNearbyZones(lat, lng, radiusKm);
    }

    public List<DangerZone> getHighRiskZones() {
        return dangerZoneRepository.findByRiskLevelAndStatus(
                DangerZone.RiskLevel.HIGH, DangerZone.Status.ACTIVE);
    }

    public List<DangerZone> getZonesByDistrict(String district) {
        return dangerZoneRepository.findByDistrictAndStatus(district, DangerZone.Status.ACTIVE);
    }

    public Map<String, Long> getZonesByDistrictStats() {
        List<DangerZone> zones = getAllActiveZones();
        return zones.stream()
                .collect(Collectors.groupingBy(
                        DangerZone::getDistrict,
                        Collectors.counting()
                ));
    }

    public DangerZone addZone(DangerZone zone) {
        zone.setCreatedAt(LocalDateTime.now());
        zone.setStatus(DangerZone.Status.ACTIVE);
        return dangerZoneRepository.save(zone);
    }

    public DangerZone updateZone(Long id, DangerZone zone) {
        zone.setId(id);
        zone.setUpdatedAt(LocalDateTime.now());
        return dangerZoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        dangerZoneRepository.deleteById(id);
    }

    public DangerZone getZoneById(Long id) {
        return dangerZoneRepository.findById(id).orElse(null);
    }
}