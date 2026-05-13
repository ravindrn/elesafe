package com.elephant.safetybackend.service;

import com.elephant.safetybackend.dto.EmergencyContactDTO;
import com.elephant.safetybackend.model.EmergencyContact;
import com.elephant.safetybackend.repository.EmergencyContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyContactService {

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    // Get all active emergency contacts
    public List<EmergencyContactDTO> getAllActiveContacts() {
        return emergencyContactRepository.findByIsActiveTrueOrderByPriorityAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get contacts by category
    public List<EmergencyContactDTO> getContactsByCategory(String category) {
        return emergencyContactRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get categorized contacts
    public CategorizedContactsDTO getCategorizedContacts() {
        CategorizedContactsDTO categorized = new CategorizedContactsDTO();

        categorized.setPolice(emergencyContactRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("POLICE")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorized.setAmbulance(emergencyContactRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("AMBULANCE")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorized.setWildlife(emergencyContactRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("WILDLIFE")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorized.setHospital(emergencyContactRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("HOSPITAL")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        categorized.setForest(emergencyContactRepository.findByCategoryAndIsActiveTrueOrderByPriorityAsc("FOREST")
                .stream().map(this::convertToDTO).collect(Collectors.toList()));

        return categorized;
    }

    // Create new emergency contact (Admin only)
    public EmergencyContactDTO createContact(EmergencyContactDTO dto) {
        EmergencyContact contact = new EmergencyContact();
        contact.setName(dto.getName());
        contact.setPhoneNumber(dto.getPhoneNumber());
        contact.setDescription(dto.getDescription());
        contact.setCategory(dto.getCategory());
        contact.setPriority(dto.getPriority());
        contact.setIcon(dto.getIcon());
        contact.setIsActive(true);

        EmergencyContact saved = emergencyContactRepository.save(contact);
        return convertToDTO(saved);
    }

    // Update emergency contact (Admin only)
    public EmergencyContactDTO updateContact(Long id, EmergencyContactDTO dto) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found"));

        contact.setName(dto.getName());
        contact.setPhoneNumber(dto.getPhoneNumber());
        contact.setDescription(dto.getDescription());
        contact.setCategory(dto.getCategory());
        contact.setPriority(dto.getPriority());
        contact.setIcon(dto.getIcon());

        EmergencyContact updated = emergencyContactRepository.save(contact);
        return convertToDTO(updated);
    }

    // Delete emergency contact (Admin only)
    public void deleteContact(Long id) {
        emergencyContactRepository.deleteById(id);
    }

    // Toggle active status (Admin only)
    public EmergencyContactDTO toggleContactStatus(Long id) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found"));
        contact.setIsActive(!contact.getIsActive());
        return convertToDTO(emergencyContactRepository.save(contact));
    }

    private EmergencyContactDTO convertToDTO(EmergencyContact contact) {
        return new EmergencyContactDTO(
                contact.getId(),
                contact.getName(),
                contact.getPhoneNumber(),
                contact.getDescription(),
                contact.getCategory(),
                contact.getPriority(),
                contact.getIcon()
        );
    }

    // Inner class for categorized contacts
    public static class CategorizedContactsDTO {
        private List<EmergencyContactDTO> police;
        private List<EmergencyContactDTO> ambulance;
        private List<EmergencyContactDTO> wildlife;
        private List<EmergencyContactDTO> hospital;
        private List<EmergencyContactDTO> forest;

        // Getters and Setters
        public List<EmergencyContactDTO> getPolice() { return police; }
        public void setPolice(List<EmergencyContactDTO> police) { this.police = police; }

        public List<EmergencyContactDTO> getAmbulance() { return ambulance; }
        public void setAmbulance(List<EmergencyContactDTO> ambulance) { this.ambulance = ambulance; }

        public List<EmergencyContactDTO> getWildlife() { return wildlife; }
        public void setWildlife(List<EmergencyContactDTO> wildlife) { this.wildlife = wildlife; }

        public List<EmergencyContactDTO> getHospital() { return hospital; }
        public void setHospital(List<EmergencyContactDTO> hospital) { this.hospital = hospital; }

        public List<EmergencyContactDTO> getForest() { return forest; }
        public void setForest(List<EmergencyContactDTO> forest) { this.forest = forest; }
    }
}