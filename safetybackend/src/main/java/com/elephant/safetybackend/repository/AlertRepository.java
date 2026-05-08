package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM Alert a WHERE DATE(a.createdAt) = CURRENT_DATE")
    long countAlertsToday();

    List<Alert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}