package com.elephant.safetybackend.service;

import com.elephant.safetybackend.dto.SafetyTipDTO;
import com.elephant.safetybackend.dto.SafetyTipRequest;
import com.elephant.safetybackend.model.SafetyTip;
import com.elephant.safetybackend.repository.SafetyTipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SafetyTipService {

    @Autowired
    private SafetyTipRepository safetyTipRepository;

    // Get all active safety tips
    public List<SafetyTipDTO> getAllActiveTips() {
        return safetyTipRepository.findByIsActiveTrueOrderByPriorityAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get tips by category
    public List<SafetyTipDTO> getTipsByCategory(String category) {
        return safetyTipRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get categorized tips for home screen
    public CategorizedTipsDTO getCategorizedTips() {
        CategorizedTipsDTO categorizedTips = new CategorizedTipsDTO();

        categorizedTips.setDrivingTips(safetyTipRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("DRIVING")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorizedTips.setEncounterTips(safetyTipRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("ENCOUNTER")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorizedTips.setEmergencyTips(safetyTipRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("EMERGENCY")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorizedTips.setGeneralTips(safetyTipRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("GENERAL")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        return categorizedTips;
    }

    // Get single tip by id
    public SafetyTipDTO getTipById(Long id) {
        SafetyTip tip = safetyTipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Safety tip not found with id: " + id));
        return convertToDTO(tip);
    }

    // Create new safety tip (Admin only)
    public SafetyTipDTO createTip(SafetyTipRequest request) {
        SafetyTip tip = new SafetyTip();
        tip.setTitle(request.getTitle());
        tip.setDescription(request.getDescription());
        tip.setCategory(request.getCategory());
        tip.setIcon(request.getIcon());
        tip.setPriority(request.getPriority());
        tip.setIsActive(true);

        SafetyTip savedTip = safetyTipRepository.save(tip);
        return convertToDTO(savedTip);
    }

    // Update safety tip (Admin only)
    public SafetyTipDTO updateTip(Long id, SafetyTipRequest request) {
        SafetyTip tip = safetyTipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Safety tip not found with id: " + id));

        tip.setTitle(request.getTitle());
        tip.setDescription(request.getDescription());
        tip.setCategory(request.getCategory());
        tip.setIcon(request.getIcon());
        tip.setPriority(request.getPriority());

        SafetyTip updatedTip = safetyTipRepository.save(tip);
        return convertToDTO(updatedTip);
    }

    // Delete safety tip (Admin only)
    public void deleteTip(Long id) {
        SafetyTip tip = safetyTipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Safety tip not found with id: " + id));
        safetyTipRepository.delete(tip);
    }

    // Toggle active status (Admin only)
    public SafetyTipDTO toggleTipStatus(Long id) {
        SafetyTip tip = safetyTipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Safety tip not found with id: " + id));

        tip.setIsActive(!tip.getIsActive());
        SafetyTip updatedTip = safetyTipRepository.save(tip);
        return convertToDTO(updatedTip);
    }

    // Helper method to convert Entity to DTO
    private SafetyTipDTO convertToDTO(SafetyTip tip) {
        return new SafetyTipDTO(
                tip.getId(),
                tip.getTitle(),
                tip.getDescription(),
                tip.getCategory(),
                tip.getIcon(),
                tip.getPriority()
        );
    }

    // Inner class for categorized tips
    public static class CategorizedTipsDTO {
        private List<SafetyTipDTO> drivingTips;
        private List<SafetyTipDTO> encounterTips;
        private List<SafetyTipDTO> emergencyTips;
        private List<SafetyTipDTO> generalTips;

        // Getters and Setters
        public List<SafetyTipDTO> getDrivingTips() {
            return drivingTips;
        }

        public void setDrivingTips(List<SafetyTipDTO> drivingTips) {
            this.drivingTips = drivingTips;
        }

        public List<SafetyTipDTO> getEncounterTips() {
            return encounterTips;
        }

        public void setEncounterTips(List<SafetyTipDTO> encounterTips) {
            this.encounterTips = encounterTips;
        }

        public List<SafetyTipDTO> getEmergencyTips() {
            return emergencyTips;
        }

        public void setEmergencyTips(List<SafetyTipDTO> emergencyTips) {
            this.emergencyTips = emergencyTips;
        }

        public List<SafetyTipDTO> getGeneralTips() {
            return generalTips;
        }

        public void setGeneralTips(List<SafetyTipDTO> generalTips) {
            this.generalTips = generalTips;
        }
    }
}