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

    List<ElephantReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    // Add this method for user-specific reports
    List<ElephantReport> findByUser(User user);

    List<ElephantReport> findByUserOrderByCreatedAtDesc(User user);

    long countByStatus(ReportStatus status);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT r FROM ElephantReport r WHERE r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<ElephantReport> findTop20ApprovedReports();
}