package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.VerifiedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VerifiedReportRepository extends JpaRepository<VerifiedReport, Long> {

    List<VerifiedReport> findAllByOrderByVerifiedAtDesc();

    @Query("SELECT v FROM VerifiedReport v ORDER BY v.verifiedAt DESC LIMIT 20")
    List<VerifiedReport> findTop20ByOrderByVerifiedAtDesc();
}