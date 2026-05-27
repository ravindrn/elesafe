package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.SafetyTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SafetyTipRepository extends JpaRepository<SafetyTip, Long> {

    List<SafetyTip> findByIsActiveTrueOrderByPriorityAsc();

    List<SafetyTip> findByCategoryAndIsActiveTrueOrderByPriorityAsc(String category);

    List<SafetyTip> findByIsActiveTrueAndCategoryIn(List<String> categories);
}