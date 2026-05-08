package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.DangerZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DangerZoneRepository extends JpaRepository<DangerZone, Long> {

    List<DangerZone> findByStatus(DangerZone.Status status);

    List<DangerZone> findByRiskLevelAndStatus(DangerZone.RiskLevel riskLevel, DangerZone.Status status);

    List<DangerZone> findByDistrictAndStatus(String district, DangerZone.Status status);

    @Query(value = "SELECT * FROM danger_zones WHERE status = 'ACTIVE' AND " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(latitude)))) <= :radiusKm", nativeQuery = true)
    List<DangerZone> findNearbyZones(@Param("lat") double lat,
                                     @Param("lng") double lng,
                                     @Param("radiusKm") double radiusKm);
}