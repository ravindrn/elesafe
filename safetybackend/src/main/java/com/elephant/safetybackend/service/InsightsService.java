package com.elephant.safetybackend.service;

import com.elephant.safetybackend.dto.*;
import com.elephant.safetybackend.model.ElephantReport;
import com.elephant.safetybackend.model.ElephantReport.ReportStatus;
import com.elephant.safetybackend.model.NewsItem;
import com.elephant.safetybackend.model.User;
import com.elephant.safetybackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DangerZoneRepository dangerZoneRepository;

    @Autowired
    private NewsRepository newsRepository;

    // Get verified reports (APPROVED status from reports table)
    public List<VerifiedReportDTO> getVerifiedReports() {
        List<ElephantReport> approvedReports = reportRepository.findTop20ApprovedReports();
        return approvedReports.stream()
                .map(this::convertToVerifiedReportDTO)
                .collect(Collectors.toList());
    }

    // Get all news
    public List<NewsItemDTO> getAllNews() {
        return newsRepository.findByIsActiveTrueOrderByPublishedDateDesc()
                .stream()
                .map(this::convertToNewsItemDTO)
                .collect(Collectors.toList());
    }

    // Get recent accidents
    public List<NewsItemDTO> getRecentAccidents() {
        return newsRepository.findByTypeAndIsActiveTrueOrderByPublishedDateDesc("ACCIDENT")
                .stream()
                .map(this::convertToNewsItemDTO)
                .collect(Collectors.toList());
    }

    // Get dashboard statistics
    public DashboardStatsDTO getDashboardStats() {
        long totalReports = reportRepository.count();
        long approvedReports = reportRepository.countByStatus(ReportStatus.APPROVED);
        long totalUsers = userRepository.count();
        long dangerZones = dangerZoneRepository.count();

        // Get reports from last 7 days
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        long reportsThisWeek = reportRepository.countByCreatedAtAfter(oneWeekAgo);

        // Get top district with most danger zones
        String topDistrict = dangerZoneRepository.findTopDistrict();
        if (topDistrict == null || topDistrict.isEmpty()) {
            topDistrict = "Polonnaruwa";
        }

        return new DashboardStatsDTO(totalReports, approvedReports, totalUsers,
                dangerZones, reportsThisWeek, topDistrict);
    }

    // Create news item (Admin only)
    public NewsItem createNewsItem(NewsItem newsItem) {
        newsItem.setCreatedAt(LocalDateTime.now());
        newsItem.setPublishedDate(LocalDateTime.now());
        newsItem.setIsActive(true);
        return newsRepository.save(newsItem);
    }

    // Helper method to convert Report to VerifiedReportDTO
    private VerifiedReportDTO convertToVerifiedReportDTO(ElephantReport report) {
        VerifiedReportDTO dto = new VerifiedReportDTO();
        dto.setId(report.getId());
        dto.setUserName(report.getUser().getName());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setNote(report.getNote());
        dto.setElephantCount(report.getElephantCount());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }

    // Helper method to convert NewsItem to NewsItemDTO
    private NewsItemDTO convertToNewsItemDTO(NewsItem news) {
        NewsItemDTO dto = new NewsItemDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setSource(news.getSource());
        dto.setImageUrl(news.getImageUrl());
        dto.setType(news.getType());
        dto.setDate(formatDate(news.getPublishedDate()));
        return dto;
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return date.format(formatter);
    }
}