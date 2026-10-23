package com.extremis.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing aggregated analytics and performance distributions
 * for an academic class or batch.
 */
public class ClassAnalyticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long classId;
    private String classCode;
    private String className;
    private Long batchId;
    private String batchCode;
    private String batchName;

    private int totalEnrolled;
    private int conductedSessions;
    private double overallAttendancePercentage;

    private int totalPresent;
    private int totalLate;
    private int totalAbsent;
    private int totalExcused;
    private int totalOverrides;

    private int safeCount;       // >= 75%
    private int warningCount;    // 60% - 74.9%
    private int criticalCount;   // < 60%

    private List<DailyTrendDTO> dailyTrends = new ArrayList<>();
    private List<StudentAttendanceSummaryDTO> atRiskStudents = new ArrayList<>();

    public ClassAnalyticsDTO() {
    }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public int getTotalEnrolled() { return totalEnrolled; }
    public void setTotalEnrolled(int totalEnrolled) { this.totalEnrolled = totalEnrolled; }

    public int getConductedSessions() { return conductedSessions; }
    public void setConductedSessions(int conductedSessions) { this.conductedSessions = conductedSessions; }

    public double getOverallAttendancePercentage() { return overallAttendancePercentage; }
    public void setOverallAttendancePercentage(double overallAttendancePercentage) {
        this.overallAttendancePercentage = Math.round(overallAttendancePercentage * 10.0) / 10.0;
    }

    public int getTotalPresent() { return totalPresent; }
    public void setTotalPresent(int totalPresent) { this.totalPresent = totalPresent; }

    public int getTotalLate() { return totalLate; }
    public void setTotalLate(int totalLate) { this.totalLate = totalLate; }

    public int getTotalAbsent() { return totalAbsent; }
    public void setTotalAbsent(int totalAbsent) { this.totalAbsent = totalAbsent; }

    public int getTotalExcused() { return totalExcused; }
    public void setTotalExcused(int totalExcused) { this.totalExcused = totalExcused; }

    public int getTotalOverrides() { return totalOverrides; }
    public void setTotalOverrides(int totalOverrides) { this.totalOverrides = totalOverrides; }

    public int getSafeCount() { return safeCount; }
    public void setSafeCount(int safeCount) { this.safeCount = safeCount; }

    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }

    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }

    public List<DailyTrendDTO> getDailyTrends() { return dailyTrends; }
    public void setDailyTrends(List<DailyTrendDTO> dailyTrends) { this.dailyTrends = dailyTrends; }

    public List<StudentAttendanceSummaryDTO> getAtRiskStudents() { return atRiskStudents; }
    public void setAtRiskStudents(List<StudentAttendanceSummaryDTO> atRiskStudents) { this.atRiskStudents = atRiskStudents; }

    public int getDailyTrendsCount() {
        return dailyTrends != null ? dailyTrends.size() : 0;
    }

    public int getAtRiskCount() {
        return atRiskStudents != null ? atRiskStudents.size() : 0;
    }

    public int getPresentRate() {
        int total = totalPresent + totalLate + totalAbsent + totalExcused;
        return total > 0 ? (int) Math.round(totalPresent * 100.0 / total) : 0;
    }

    public int getLateRate() {
        int total = totalPresent + totalLate + totalAbsent + totalExcused;
        return total > 0 ? (int) Math.round(totalLate * 100.0 / total) : 0;
    }

    public int getAbsentRate() {
        int total = totalPresent + totalLate + totalAbsent + totalExcused;
        return total > 0 ? (int) Math.round(totalAbsent * 100.0 / total) : 0;
    }

    public int getExcusedRate() {
        int total = totalPresent + totalLate + totalAbsent + totalExcused;
        return total > 0 ? (int) Math.round(totalExcused * 100.0 / total) : 0;
    }

    public String getFormattedOverallPercentage() {
        return String.format("%.1f%%", overallAttendancePercentage);
    }
}
