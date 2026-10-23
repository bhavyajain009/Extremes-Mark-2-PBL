package com.extremis.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing dynamic Admin-controlled timer settings for Check-In and Check-Out.
 */
public class AttendanceTimerSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private boolean checkinTimerEnabled = true;
    private LocalTime checkinStartTime = LocalTime.of(8, 50);
    private LocalTime checkinEndTime = LocalTime.of(9, 5);
    private int checkinDurationMinutes = 15;

    private boolean checkoutTimerEnabled = true;
    private LocalTime checkoutStartTime = LocalTime.of(15, 40);
    private LocalTime checkoutEndTime = LocalTime.of(18, 0);
    private int checkoutDurationMinutes = 140;

    private String updatedAt;

    public AttendanceTimerSettings() {
    }

    public boolean isCheckinTimerEnabled() {
        return checkinTimerEnabled;
    }

    public void setCheckinTimerEnabled(boolean checkinTimerEnabled) {
        this.checkinTimerEnabled = checkinTimerEnabled;
    }

    public LocalTime getCheckinStartTime() {
        return checkinStartTime;
    }

    public void setCheckinStartTime(LocalTime checkinStartTime) {
        this.checkinStartTime = checkinStartTime;
    }

    public LocalTime getCheckinEndTime() {
        return checkinEndTime;
    }

    public void setCheckinEndTime(LocalTime checkinEndTime) {
        this.checkinEndTime = checkinEndTime;
    }

    public int getCheckinDurationMinutes() {
        return checkinDurationMinutes;
    }

    public void setCheckinDurationMinutes(int checkinDurationMinutes) {
        this.checkinDurationMinutes = checkinDurationMinutes;
    }

    public boolean isCheckoutTimerEnabled() {
        return checkoutTimerEnabled;
    }

    public void setCheckoutTimerEnabled(boolean checkoutTimerEnabled) {
        this.checkoutTimerEnabled = checkoutTimerEnabled;
    }

    public LocalTime getCheckoutStartTime() {
        return checkoutStartTime;
    }

    public void setCheckoutStartTime(LocalTime checkoutStartTime) {
        this.checkoutStartTime = checkoutStartTime;
    }

    public LocalTime getCheckoutEndTime() {
        return checkoutEndTime;
    }

    public void setCheckoutEndTime(LocalTime checkoutEndTime) {
        this.checkoutEndTime = checkoutEndTime;
    }

    public int getCheckoutDurationMinutes() {
        return checkoutDurationMinutes;
    }

    public void setCheckoutDurationMinutes(int checkoutDurationMinutes) {
        this.checkoutDurationMinutes = checkoutDurationMinutes;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper formatting methods
    public String getFormattedCheckinStart() {
        return checkinStartTime != null ? checkinStartTime.format(TIME_FMT) : "08:50 AM";
    }

    public String getFormattedCheckinEnd() {
        return checkinEndTime != null ? checkinEndTime.format(TIME_FMT) : "09:05 AM";
    }

    public String getFormattedCheckinWindow() {
        return getFormattedCheckinStart() + " – " + getFormattedCheckinEnd();
    }

    public String getFormattedCheckoutStart() {
        return checkoutStartTime != null ? checkoutStartTime.format(TIME_FMT) : "03:40 PM";
    }

    public String getFormattedCheckoutEnd() {
        return checkoutEndTime != null ? checkoutEndTime.format(TIME_FMT) : "06:00 PM";
    }

    public String getFormattedCheckoutWindow() {
        return getFormattedCheckoutStart() + " – " + getFormattedCheckoutEnd();
    }

    /**
     * Checks if current effective time is within the configured checkin window.
     */
    public boolean isCheckinWindowActive(LocalTime now) {
        if (!checkinTimerEnabled) {
            // When timer is off, window is always open
            return true;
        }
        if (checkinStartTime == null || checkinEndTime == null) return true;
        return !now.isBefore(checkinStartTime) && !now.isAfter(checkinEndTime);
    }

    /**
     * Checks if current effective time is valid for checkout.
     */
    public boolean isCheckoutWindowActive(LocalTime now) {
        if (!checkoutTimerEnabled) {
            // When timer is off, window is always open
            return true;
        }
        if (checkoutStartTime == null) return true;
        if (now.isBefore(checkoutStartTime)) return false;
        if (checkoutEndTime != null && now.isAfter(checkoutEndTime)) return false;
        return true;
    }
}
