package com.elephant.safetybackend.service;

import com.elephant.safetybackend.dto.ReportRequestDTO;
import com.elephant.safetybackend.dto.ReportResponseDTO;
import com.elephant.safetybackend.model.ElephantReport;
import com.elephant.safetybackend.model.User;
import com.elephant.safetybackend.repository.ReportRepository;
import com.elephant.safetybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    // Submit a new report
    @Transactional
    public ReportResponseDTO submitReport(ReportRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ElephantReport report = new ElephantReport();
        report.setUser(user);
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setNote(request.getNote());
        report.setElephantCount(request.getElephantCount() != null ? request.getElephantCount() : 1);
        report.setStatus(ElephantReport.ReportStatus.PENDING);  // Use enum, not String
        report.setCreatedAt(LocalDateTime.now());

        ElephantReport savedReport = reportRepository.save(report);
        return convertToDTO(savedReport);
    }

    // Get all reports (Admin only)
    public List<ReportResponseDTO> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get reports by status (Admin only)
    public List<ReportResponseDTO> getReportsByStatus(String status) {
        ElephantReport.ReportStatus reportStatus;
        try {
            reportStatus = ElephantReport.ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
        return reportRepository.findByStatusOrderByCreatedAtDesc(reportStatus)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get current user's reports
    public List<ReportResponseDTO> getMyReports(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reportRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get report by ID (Admin only)
    public ReportResponseDTO getReportById(Long id) {
        ElephantReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        return convertToDTO(report);
    }

    // Approve report (Admin only)
    @Transactional
    public ReportResponseDTO approveReport(Long id) {
        ElephantReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        report.setStatus(ElephantReport.ReportStatus.APPROVED);
        return convertToDTO(reportRepository.save(report));
    }

    // Reject report (Admin only)
    @Transactional
    public ReportResponseDTO rejectReport(Long id) {
        ElephantReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        report.setStatus(ElephantReport.ReportStatus.REJECTED);
        return convertToDTO(reportRepository.save(report));
    }

    // Resolve report (Admin only)
    @Transactional
    public ReportResponseDTO resolveReport(Long id) {
        ElephantReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        report.setStatus(ElephantReport.ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        return convertToDTO(reportRepository.save(report));
    }

    // Delete report (Admin only)
    @Transactional
    public void deleteReport(Long id) {
        ElephantReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        reportRepository.delete(report);
    }

    // Get report statistics (Admin only)
    public ReportStatsDTO getReportStats() {
        ReportStatsDTO stats = new ReportStatsDTO();
        stats.setTotal(reportRepository.count());
        stats.setPending(reportRepository.countByStatus(ElephantReport.ReportStatus.PENDING));
        stats.setApproved(reportRepository.countByStatus(ElephantReport.ReportStatus.APPROVED));
        stats.setRejected(reportRepository.countByStatus(ElephantReport.ReportStatus.REJECTED));
        stats.setResolved(reportRepository.countByStatus(ElephantReport.ReportStatus.RESOLVED));
        return stats;
    }

    // Convert Entity to DTO
    private ReportResponseDTO convertToDTO(ElephantReport report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setUserId(report.getUser().getId());
        dto.setUserName(report.getUser().getName());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setNote(report.getNote());
        dto.setElephantCount(report.getElephantCount());
        dto.setStatus(report.getStatus().toString());  // Convert enum to String
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }

    // Inner class for stats
    public static class ReportStatsDTO {
        private long total;
        private long pending;
        private long approved;
        private long rejected;
        private long resolved;

        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }

        public long getApproved() { return approved; }
        public void setApproved(long approved) { this.approved = approved; }

        public long getRejected() { return rejected; }
        public void setRejected(long rejected) { this.rejected = rejected; }

        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }
    }
}