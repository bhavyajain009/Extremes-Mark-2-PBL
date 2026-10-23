package com.extremis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StudentRandomChallenge {

    private Long id;
    private Long challengeId;
    private Long studentId;
    private String status; // "PENDING", "RESPONDED", "MISSED"
    private LocalDateTime respondedAt;
    private Double lat;
    private Double lng;
    private Double accuracy;
    private String photoPath;

    // Student profile details for presentation
    private String studentName;
    private String studentRollNo;
    private String studentBranch;
    private String studentSection;
    private String challengeCode;
    private LocalDateTime deadlineTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
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

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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

    public String getChallengeCode() {
        return challengeCode;
    }

    public void setChallengeCode(String challengeCode) {
        this.challengeCode = challengeCode;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(LocalDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public String getFormattedRespondedAt() {
        return respondedAt != null ? respondedAt.format(DateTimeFormatter.ofPattern("hh:mm:ss a")) : "";
    }
}
