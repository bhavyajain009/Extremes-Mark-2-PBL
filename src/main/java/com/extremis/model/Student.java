package com.extremis.model;

import java.io.Serializable;

/**
 * Model representing a Student entity.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String rollNo;
    private String name;
    private String email;
    private String branch;
    private String section;
    private String passwordHash;
    private String sessionToken;
    private boolean permissionsAccepted;

    // Academic class & batch association
    private Long classId;
    private Long batchId;
    private String batchCode;
    private String batchName;

    public Student() {
    }

    public Student(Long id, String rollNo, String name, String email, String branch, String section,
            String passwordHash, String sessionToken) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.branch = branch;
        this.section = section;
        this.passwordHash = passwordHash;
        this.sessionToken = sessionToken;
        this.permissionsAccepted = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isPermissionsAccepted() {
        return permissionsAccepted;
    }

    public void setPermissionsAccepted(boolean permissionsAccepted) {
        this.permissionsAccepted = permissionsAccepted;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getDisplayBatch() {
        if (batchCode != null && !batchCode.trim().isEmpty()) {
            return "Batch " + batchCode;
        }
        return "Batch A1";
    }
}
