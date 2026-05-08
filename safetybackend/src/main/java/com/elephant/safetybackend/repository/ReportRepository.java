package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.ElephantReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ElephantReport, Long> {
    List<ElephantReport> findByStatus(ElephantReport.Status status);
    List<ElephantReport> findByUserId(Long userId);
}