package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.ElephantReport;
import com.elephant.safetybackend.model.ElephantReport.ReportStatus;
import com.elephant.safetybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Added this import for the new query!
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ElephantReport, Long> {

    List<ElephantReport> findAllByOrderByCreatedAtDesc();

    // For Enum type
    List<ElephantReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    // For String type - convert to Enum
    default List<ElephantReport> findByStatusOrderByCreatedAtDesc(String status) {
        try {
            ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
            return findByStatusOrderByCreatedAtDesc(reportStatus);
        } catch (IllegalArgumentException e) {
            return new java.util.ArrayList<>();
        }
    }

    List<ElephantReport> findByUserOrderByCreatedAtDesc(User user);

    // For Enum type
    long countByStatus(ReportStatus status);

    // For String type
    default long countByStatus(String status) {
        try {
            ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
            return countByStatus(reportStatus);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    long countByCreatedAtAfter(LocalDateTime date);

    // Native query for approved reports
    @Query("SELECT r FROM ElephantReport r WHERE r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<ElephantReport> findTop20ApprovedReports();

    // ====================================================================================
    // --- NEW AI ML HELPER METHOD ---
    // Calculates the total number of elephants reported within a specific radius
    // in the last 24 hours using the Haversine formula directly inside MySQL!
    // ====================================================================================
    @Query(value = "SELECT COALESCE(SUM(elephant_count), 0) FROM reports " +
            "WHERE status = 'APPROVED' " +
            "AND created_at >= NOW() - INTERVAL 3 HOUR " +
            "AND (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:userLng)) + " +
            "sin(radians(:userLat)) * sin(radians(latitude)))) < :radiusKm",
            nativeQuery = true)
    Integer getRecentElephantCountNearUser(
            @Param("userLat") double userLat,
            @Param("userLng") double userLng,
            @Param("radiusKm") double radiusKm
    );
}