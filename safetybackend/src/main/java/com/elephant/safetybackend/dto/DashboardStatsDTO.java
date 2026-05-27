package com.elephant.safetybackend.dto;

public class DashboardStatsDTO {
    private long totalReports;
    private long approvedReports;
    private long totalUsers;
    private long dangerZones;
    private long reportsThisWeek;
    private String topDistrict;

    // Constructors
    public DashboardStatsDTO() {}

    public DashboardStatsDTO(long totalReports, long approvedReports, long totalUsers,
                             long dangerZones, long reportsThisWeek, String topDistrict) {
        this.totalReports = totalReports;
        this.approvedReports = approvedReports;
        this.totalUsers = totalUsers;
        this.dangerZones = dangerZones;
        this.reportsThisWeek = reportsThisWeek;
        this.topDistrict = topDistrict;
    }

    // Getters and Setters
    public long getTotalReports() { return totalReports; }
    public void setTotalReports(long totalReports) { this.totalReports = totalReports; }

    public long getApprovedReports() { return approvedReports; }
    public void setApprovedReports(long approvedReports) { this.approvedReports = approvedReports; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getDangerZones() { return dangerZones; }
    public void setDangerZones(long dangerZones) { this.dangerZones = dangerZones; }

    public long getReportsThisWeek() { return reportsThisWeek; }
    public void setReportsThisWeek(long reportsThisWeek) { this.reportsThisWeek = reportsThisWeek; }

    public String getTopDistrict() { return topDistrict; }
    public void setTopDistrict(String topDistrict) { this.topDistrict = topDistrict; }
}