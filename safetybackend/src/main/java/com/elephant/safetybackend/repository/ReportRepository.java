package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.ElephantReport;
import com.elephant.safetybackend.model.ElephantReport.ReportStatus;
import com.elephant.safetybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}