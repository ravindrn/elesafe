package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByIsActiveTrueOrderByPriorityAsc();

    List<EmergencyContact> findByCategoryAndIsActiveTrueOrderByPriorityAsc(String category);

    List<EmergencyContact> findByIsActiveTrue();
}