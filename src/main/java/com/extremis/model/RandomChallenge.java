package com.extremis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RandomChallenge {

    private Long id;
    private String challengeCode;
    private LocalDateTime scheduledTime;
    private LocalDateTime deadlineTime;
    private int durationMinutes;
    private String status; // "SCHEDULED", "ACTIVE", "COMPLETED", "CANCELLED"
    private String targetType; // "ALL" or "CLASS"
    private String targetValue; // "ALL" or "AI-A"
    private boolean manual;
    private LocalDateTime createdAt;
    private String createdBy;

    // Aggregated stats for dashboards
    private int totalAssigned;
    private int respondedCount;
    private int missedCount;
    private int pendingCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChallengeCode() {
        return challengeCode;
    }

    public void setChallengeCode(String challengeCode) {
        this.challengeCode = challengeCode;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(LocalDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getTotalAssigned() {
        return totalAssigned;
    }

    public void setTotalAssigned(int totalAssigned) {
        this.totalAssigned = totalAssigned;
    }

    public int getRespondedCount() {
        return respondedCount;
    }

    public void setRespondedCount(int respondedCount) {
        this.respondedCount = respondedCount;
    }

    public int getMissedCount() {
        return missedCount;
    }

    public void setMissedCount(int missedCount) {
        this.missedCount = missedCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public String getFormattedScheduledTime() {
        return scheduledTime != null ? scheduledTime.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }

    public String getFormattedDeadlineTime() {
        return deadlineTime != null ? deadlineTime.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }

    public String getFormattedDateTime() {
        return scheduledTime != null ? scheduledTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "";
    }
}
