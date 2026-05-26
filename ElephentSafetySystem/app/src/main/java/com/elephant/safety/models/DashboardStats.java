package com.elephant.safety.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DashboardStats implements Serializable {

    @SerializedName("totalReports")
    private long totalReports;

    @SerializedName("approvedReports")
    private long approvedReports;

    @SerializedName("totalUsers")
    private long totalUsers;

    @SerializedName("dangerZones")
    private long dangerZones;

    @SerializedName("reportsThisWeek")
    private long reportsThisWeek;

    @SerializedName("topDistrict")
    private String topDistrict;

    public DashboardStats() {}

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