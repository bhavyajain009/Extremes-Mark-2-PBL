package com.extremis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity representing an individual student's random location verification event.
 * Server-scheduled 4-5 times per academic day (9:05 AM - 3:40 PM, excluding 12:20 PM - 1:10 PM).
 */
public class RandomLocationCheck {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private Long studentId;
    private Long attendanceId;
    private LocalDate checkDate;
    private LocalDateTime scheduledTime;
    private LocalDateTime windowEndTime;
    private String status; // "SCHEDULED", "ACTIVE", "VERIFIED", "FAILED", "EXPIRED", "CANCELLED"
    private String verificationResult; // "VERIFIED_CAMPUS", "OUTSIDE_GEOFENCE", "ACCURACY_POOR", "GPS_UNAVAILABLE", "PERMISSION_DENIED", "EXPIRED_UNREACHABLE"
    private Double lat;
    private Double lng;
    private Double accuracy;
    private Double distanceMeters;
    private LocalDateTime serverTimestamp;
    private String clientTimestamp;
    private String failureReason;
    private boolean flaggedForReview;
    private String facultyReviewStatus; // "PENDING", "APPROVED_VALID_REASON", "REJECTED_ABSENT"
    private String facultyReviewNotes;
    private String facultyReviewedBy;
    private LocalDateTime facultyReviewedAt;
    private LocalDateTime createdAt;

    // Student profile details for presentation in Admin/Faculty views
    private String studentName;
    private String studentRollNo;
    private String studentBranch;
    private String studentSection;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public LocalDate getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDate checkDate) {
        this.checkDate = checkDate;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getWindowEndTime() {
        return windowEndTime;
    }

    public void setWindowEndTime(LocalDateTime windowEndTime) {
        this.windowEndTime = windowEndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerificationResult() {
        return verificationResult;
    }

    public void setVerificationResult(String verificationResult) {
        this.verificationResult = verificationResult;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public LocalDateTime getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(LocalDateTime serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public void setFlaggedForReview(boolean flaggedForReview) {
        this.flaggedForReview = flaggedForReview;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentBranch() {
        return studentBranch;
    }

    public void setStudentBranch(String studentBranch) {
        this.studentBranch = studentBranch;
    }

    public String getStudentSection() {
        return studentSection;
    }

    public void setStudentSection(String studentSection) {
        this.studentSection = studentSection;
    }

    public String getFormattedScheduledTime() {
        return (scheduledTime != null) ? scheduledTime.format(TIME_FMT) : "—";
    }

    public String getFormattedServerTimestamp() {
        return (serverTimestamp != null) ? serverTimestamp.format(TIME_FMT) : "—";
    }

    public String getFacultyReviewStatus() {
        return facultyReviewStatus;
    }

    public void setFacultyReviewStatus(String facultyReviewStatus) {
        this.facultyReviewStatus = facultyReviewStatus;
    }

    public String getFacultyReviewNotes() {
        return facultyReviewNotes;
    }

    public void setFacultyReviewNotes(String facultyReviewNotes) {
        this.facultyReviewNotes = facultyReviewNotes;
    }

    public String getFacultyReviewedBy() {
        return facultyReviewedBy;
    }

    public void setFacultyReviewedBy(String facultyReviewedBy) {
        this.facultyReviewedBy = facultyReviewedBy;
    }

    public LocalDateTime getFacultyReviewedAt() {
        return facultyReviewedAt;
    }

    public void setFacultyReviewedAt(LocalDateTime facultyReviewedAt) {
        this.facultyReviewedAt = facultyReviewedAt;
    }

    public boolean isPendingFacultyReview() {
        return flaggedForReview || ("OUTSIDE_GEOFENCE".equalsIgnoreCase(verificationResult) &&
                (facultyReviewStatus == null || "PENDING".equalsIgnoreCase(facultyReviewStatus)));
    }

    public String getFormattedFacultyReviewedAt() {
        return (facultyReviewedAt != null) ? facultyReviewedAt.format(DATE_TIME_FMT) : "—";
    }
}
