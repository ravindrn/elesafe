package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.DangerZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DangerZoneRepository extends JpaRepository<DangerZone, Long> {

    List<DangerZone> findByStatus(String status);

    List<DangerZone> findByRiskLevel(String riskLevel);

    List<DangerZone> findByDistrict(String district);

    // Get top district with most danger zones
    @Query("SELECT d.district FROM DangerZone d WHERE d.district IS NOT NULL GROUP BY d.district ORDER BY COUNT(d) DESC LIMIT 1")
    String findTopDistrict();

    // Count danger zones by district
    @Query("SELECT d.district, COUNT(d) FROM DangerZone d WHERE d.district IS NOT NULL GROUP BY d.district ORDER BY COUNT(d) DESC")
    List<Object[]> countByDistrict();
}