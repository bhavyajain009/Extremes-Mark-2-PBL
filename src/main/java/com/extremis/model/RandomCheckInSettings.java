package com.extremis.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RandomCheckInSettings {

    private boolean enabled = true;
    private boolean paused = false;
    private LocalTime earliestTime = LocalTime.of(9, 15);
    private LocalTime latestTime = LocalTime.of(15, 30);
    private int durationMinutes = 10;
    private int minIntervalMinutes = 45;
    private int maxIntervalMinutes = 90;
    private int challengesPerDay = 2;
    private String targetType = "ALL"; // "ALL" or "CLASS"
    private String targetValue = "ALL"; // "ALL" or class code like "AI-A"
    private String updatedAt;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public LocalTime getEarliestTime() {
        return earliestTime;
    }

    public void setEarliestTime(LocalTime earliestTime) {
        this.earliestTime = earliestTime;
    }

    public LocalTime getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(LocalTime latestTime) {
        this.latestTime = latestTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getMinIntervalMinutes() {
        return minIntervalMinutes;
    }

    public void setMinIntervalMinutes(int minIntervalMinutes) {
        this.minIntervalMinutes = minIntervalMinutes;
    }

    public int getMaxIntervalMinutes() {
        return maxIntervalMinutes;
    }

    public void setMaxIntervalMinutes(int maxIntervalMinutes) {
        this.maxIntervalMinutes = maxIntervalMinutes;
    }

    public int getChallengesPerDay() {
        return challengesPerDay;
    }

    public void setChallengesPerDay(int challengesPerDay) {
        this.challengesPerDay = challengesPerDay;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedEarliestTime() {
        return earliestTime != null ? earliestTime.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }

    public String getFormattedLatestTime() {
        return latestTime != null ? latestTime.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }
}
