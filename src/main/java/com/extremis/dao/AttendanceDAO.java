package com.extremis.dao;

import com.extremis.dto.ClassAnalyticsDTO;
import com.extremis.dto.DailyTrendDTO;
import com.extremis.dto.StudentAttendanceSummaryDTO;
import com.extremis.dto.StudentSessionHistoryDTO;
import com.extremis.model.AttendanceOverrideAudit;
import com.extremis.model.AttendanceRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public AttendanceRecord findByStudentAndDate(Long studentId, LocalDate date) {
        String sql = "SELECT * FROM attendance WHERE student_id = ? AND date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertCheckIn(AttendanceRecord r) {
        String sql = "INSERT INTO attendance (student_id, date, check_in_time, checkin_lat, checkin_lng, checkin_accuracy, checkin_photo_path, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getStudentId());
            ps.setString(2, r.getDate().toString());
            ps.setString(3, r.getCheckInTime().toString());
            if (r.getCheckinLat() != null) ps.setDouble(4, r.getCheckinLat()); else ps.setNull(4, java.sql.Types.REAL);
            if (r.getCheckinLng() != null) ps.setDouble(5, r.getCheckinLng()); else ps.setNull(5, java.sql.Types.REAL);
            if (r.getCheckinAccuracy() != null) ps.setDouble(6, r.getCheckinAccuracy()); else ps.setNull(6, java.sql.Types.REAL);
            ps.setString(7, r.getCheckinPhotoPath());
            ps.setString(8, "PRESENT");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCheckOut(AttendanceRecord r) {
        String sql = "UPDATE attendance SET check_out_time = ?, checkout_lat = ?, checkout_lng = ?, checkout_photo_path = ? " +
                     "WHERE student_id = ? AND date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCheckOutTime().toString());
            if (r.getCheckoutLat() != null) ps.setDouble(2, r.getCheckoutLat()); else ps.setNull(2, java.sql.Types.REAL);
            if (r.getCheckoutLng() != null) ps.setDouble(3, r.getCheckoutLng()); else ps.setNull(3, java.sql.Types.REAL);
            ps.setNull(4, java.sql.Types.REAL);
            ps.setLong(5, r.getStudentId());
            ps.setString(6, r.getDate().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AttendanceRecord> getRecordsByStudentAndMonth(Long studentId, int year, int month, String statusFilter) {
        List<AttendanceRecord> list = new ArrayList<>();
        String prefix = String.format("%04d-%02d", year, month);

        StringBuilder sb = new StringBuilder("SELECT * FROM attendance WHERE student_id = ? AND date LIKE ?");
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sb.append(" AND status = ?");
        }
        sb.append(" ORDER BY date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setLong(1, studentId);
            ps.setString(2, prefix + "%");
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
                ps.setString(3, statusFilter.toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countByStatus(Long studentId, int year, int month, String status) {
        String prefix = String.format("%04d-%02d", year, month);
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND date LIKE ? AND status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, prefix + "%");
            ps.setString(3, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countTotalMonthClasses(Long studentId, int year, int month) {
        String prefix = String.format("%04d-%02d", year, month);
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND date LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Retrieves attendance list for faculty dashboard with filtering options.
     */
    public List<AttendanceRecord> getAttendanceForFaculty(Long facultyId, Long classId, Long batchId, LocalDate date, String statusFilter) {
        List<AttendanceRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT s.id as s_id, s.name as student_name, s.roll_no as student_roll, s.branch, s.section, ")
          .append("b.name as batch_name, ")
          .append("a.id as att_id, a.date as att_date, a.status as att_status, a.check_in_time, a.check_out_time, ")
          .append("a.is_override, a.override_reason, a.overridden_by ")
          .append("FROM students s ")
          .append("LEFT JOIN batches b ON s.batch_id = b.id ")
          .append("LEFT JOIN attendance a ON s.id = a.student_id AND a.date = ? ")
          .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        params.add(date.toString());

        // Authorization constraint or filter
        if (classId != null) {
            sb.append(" AND s.class_id = ? ");
            params.add(classId);
        } else if (facultyId != null) {
            // Only students in classes/batches assigned to this faculty
            sb.append(" AND (s.class_id IN (SELECT ta.class_id FROM teacher_assignments ta WHERE ta.faculty_id = ?) ")
              .append("      OR UPPER(s.branch) IN (SELECT UPPER(c.department) FROM classes c JOIN teacher_assignments ta ON ta.class_id = c.id WHERE ta.faculty_id = ?)) ");
            params.add(facultyId);
            params.add(facultyId);
        }

        if (batchId != null) {
            sb.append(" AND s.batch_id = ? ");
            params.add(batchId);
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            if ("NOT_MARKED".equalsIgnoreCase(statusFilter) || "ABSENT".equalsIgnoreCase(statusFilter)) {
                sb.append(" AND (a.status IS NULL OR UPPER(a.status) = 'ABSENT' OR UPPER(a.status) = 'NOT_MARKED') ");
            } else {
                sb.append(" AND UPPER(a.status) = ? ");
                params.add(statusFilter.toUpperCase().trim());
            }
        }

        sb.append(" ORDER BY s.roll_no ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = new AttendanceRecord();
                    long attId = rs.getLong("att_id");
                    if (!rs.wasNull()) {
                        r.setId(attId);
                    }
                    r.setStudentId(rs.getLong("s_id"));
                    r.setStudentName(rs.getString("student_name"));
                    r.setStudentRoll(rs.getString("student_roll"));
                    r.setBranch(rs.getString("branch"));
                    r.setSection(rs.getString("section"));
                    r.setBatchName(rs.getString("batch_name"));
                    r.setDate(date);

                    String rawStatus = rs.getString("att_status");
                    r.setStatus(rawStatus != null && !rawStatus.isEmpty() ? rawStatus : "ABSENT");

                    String cin = rs.getString("check_in_time");
                    if (cin != null && !cin.isEmpty()) {
                        try { r.setCheckInTime(LocalTime.parse(cin.length() > 8 ? cin.substring(0, 8) : cin)); } catch (Exception ignored) {}
                    }
                    String cout = rs.getString("check_out_time");
                    if (cout != null && !cout.isEmpty()) {
                        try { r.setCheckOutTime(LocalTime.parse(cout.length() > 8 ? cout.substring(0, 8) : cout)); } catch (Exception ignored) {}
                    }

                    r.setOverride(rs.getInt("is_override") == 1);
                    r.setOverrideReason(rs.getString("override_reason"));
                    r.setOverriddenBy(rs.getString("overridden_by"));

                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AttendanceRecord> getTodayAttendanceForClass(String branch, String section, LocalDate date) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT s.id as s_id, s.name as student_name, s.roll_no as student_roll, s.branch, s.section, " +
                     "a.id as att_id, a.date as att_date, a.status as att_status, a.check_in_time, a.check_out_time, " +
                     "a.is_override, a.override_reason, a.overridden_by " +
                     "FROM students s " +
                     "LEFT JOIN attendance a ON s.id = a.student_id AND a.date = ? " +
                     "WHERE UPPER(s.branch) = UPPER(?) AND UPPER(s.section) = UPPER(?) " +
                     "ORDER BY s.roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setString(2, branch.trim());
            ps.setString(3, section.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = new AttendanceRecord();
                    long attId = rs.getLong("att_id");
                    if (!rs.wasNull()) {
                        r.setId(attId);
                    }
                    r.setStudentId(rs.getLong("s_id"));
                    r.setStudentName(rs.getString("student_name"));
                    r.setStudentRoll(rs.getString("student_roll"));
                    r.setBranch(rs.getString("branch"));
                    r.setSection(rs.getString("section"));
                    r.setDate(date);

                    String rawStatus = rs.getString("att_status");
                    r.setStatus(rawStatus != null && !rawStatus.isEmpty() ? rawStatus : "ABSENT");

                    String cin = rs.getString("check_in_time");
                    if (cin != null && !cin.isEmpty()) {
                        try { r.setCheckInTime(LocalTime.parse(cin.length() > 8 ? cin.substring(0, 8) : cin)); } catch (Exception ignored) {}
                    }
                    String cout = rs.getString("check_out_time");
                    if (cout != null && !cout.isEmpty()) {
                        try { r.setCheckOutTime(LocalTime.parse(cout.length() > 8 ? cout.substring(0, 8) : cout)); } catch (Exception ignored) {}
                    }

                    r.setOverride(rs.getInt("is_override") == 1);
                    r.setOverrideReason(rs.getString("override_reason"));
                    r.setOverriddenBy(rs.getString("overridden_by"));

                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Executes an attendance override with transactional integrity and audit logging.
     */
    public boolean recordOverride(Long studentId, LocalDate date, String newStatus, String reason, String facultyIdentifier, Long facultyId) {
        if (studentId == null || date == null || newStatus == null || reason == null || reason.trim().isEmpty()) {
            return false;
        }

        String checkSql = "SELECT id, status, check_in_time FROM attendance WHERE student_id = ? AND date = ?";
        String updateSql = "UPDATE attendance SET status = ?, is_override = 1, override_reason = ?, overridden_by = ?, " +
                           "check_in_time = COALESCE(check_in_time, ?) WHERE id = ?";
        String insertSql = "INSERT INTO attendance (student_id, date, status, is_override, override_reason, overridden_by, check_in_time) " +
                           "VALUES (?, ?, ?, 1, ?, ?, ?)";
        String auditSql = "INSERT INTO attendance_overrides (attendance_id, student_id, faculty_id, original_status, updated_status, reason, attendance_date, override_time) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))";

        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long attId = null;
                String originalStatus = "ABSENT";

                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setLong(1, studentId);
                    checkPs.setString(2, date.toString());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next()) {
                            attId = rs.getLong("id");
                            originalStatus = rs.getString("status");
                        }
                    }
                }

                if (attId != null) {
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, newStatus.toUpperCase().trim());
                        updatePs.setString(2, reason.trim());
                        updatePs.setString(3, facultyIdentifier);
                        updatePs.setString(4, nowTime);
                        updatePs.setLong(5, attId);
                        updatePs.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertPs.setLong(1, studentId);
                        insertPs.setString(2, date.toString());
                        insertPs.setString(3, newStatus.toUpperCase().trim());
                        insertPs.setString(4, reason.trim());
                        insertPs.setString(5, facultyIdentifier);
                        insertPs.setString(6, nowTime);
                        insertPs.executeUpdate();

                        try (ResultSet generatedKeys = insertPs.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                attId = generatedKeys.getLong(1);
                            }
                        }
                    }
                }

                // Record audit log
                try (PreparedStatement auditPs = conn.prepareStatement(auditSql)) {
                    if (attId != null) auditPs.setLong(1, attId); else auditPs.setNull(1, java.sql.Types.INTEGER);
                    auditPs.setLong(2, studentId);
                    auditPs.setLong(3, facultyId != null ? facultyId : 0L);
                    auditPs.setString(4, originalStatus);
                    auditPs.setString(5, newStatus.toUpperCase().trim());
                    auditPs.setString(6, reason.trim());
                    auditPs.setString(7, date.toString());
                    auditPs.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves audit history of overrides for admin and faculty review.
     */
    public List<AttendanceOverrideAudit> getOverrideAuditHistory(int limit) {
        return getOverrideAuditHistory(null, limit);
    }

    public List<AttendanceOverrideAudit> getOverrideAuditHistory(Long facultyId, int limit) {
        List<AttendanceOverrideAudit> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ao.*, s.name as student_name, s.roll_no as student_roll, ")
          .append("f.name as faculty_name, f.faculty_id as faculty_code ")
          .append("FROM attendance_overrides ao ")
          .append("JOIN students s ON ao.student_id = s.id ")
          .append("LEFT JOIN faculties f ON ao.faculty_id = f.id ");
        if (facultyId != null) {
            sb.append("WHERE ao.faculty_id = ? ")
              .append("   OR s.class_id IN (SELECT ta.class_id FROM teacher_assignments ta WHERE ta.faculty_id = ?) ");
        }
        sb.append("ORDER BY ao.id DESC LIMIT ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int p = 1;
            if (facultyId != null) {
                ps.setLong(p++, facultyId);
                ps.setLong(p++, facultyId);
            }
            ps.setInt(p, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceOverrideAudit a = new AttendanceOverrideAudit();
                    a.setId(rs.getLong("id"));
                    a.setAttendanceId(rs.getLong("attendance_id"));
                    a.setStudentId(rs.getLong("student_id"));
                    a.setFacultyId(rs.getLong("faculty_id"));
                    a.setOriginalStatus(rs.getString("original_status"));
                    a.setUpdatedStatus(rs.getString("updated_status"));
                    a.setReason(rs.getString("reason"));
                    a.setAttendanceDate(rs.getString("attendance_date"));
                    a.setOverrideTime(rs.getString("override_time"));
                    a.setStudentName(rs.getString("student_name"));
                    a.setStudentRoll(rs.getString("student_roll"));
                    a.setFacultyName(rs.getString("faculty_name"));
                    a.setFacultyCode(rs.getString("faculty_code"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countTodayCheckIns(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE date = ? AND check_in_time IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getLong("id"));
        r.setStudentId(rs.getLong("student_id"));
        r.setDate(LocalDate.parse(rs.getString("date")));

        String cin = rs.getString("check_in_time");
        if (cin != null && !cin.isEmpty()) {
            try { r.setCheckInTime(LocalTime.parse(cin.length() > 8 ? cin.substring(0, 8) : cin)); } catch (Exception ignored) {}
        }

        String cout = rs.getString("check_out_time");
        if (cout != null && !cout.isEmpty()) {
            try { r.setCheckOutTime(LocalTime.parse(cout.length() > 8 ? cout.substring(0, 8) : cout)); } catch (Exception ignored) {}
        }

        try { r.setCheckinLat(rs.getDouble("checkin_lat")); } catch (SQLException ignored) {}
        try { r.setCheckinLng(rs.getDouble("checkin_lng")); } catch (SQLException ignored) {}
        try { r.setCheckinAccuracy(rs.getDouble("checkin_accuracy")); } catch (SQLException ignored) {}
        try { r.setCheckinPhotoPath(rs.getString("checkin_photo_path")); } catch (SQLException ignored) {}
        r.setStatus(rs.getString("status"));

        try { r.setOverride(rs.getInt("is_override") == 1); } catch (SQLException ignored) {}
        try { r.setOverrideReason(rs.getString("override_reason")); } catch (SQLException ignored) {}
        try { r.setOverriddenBy(rs.getString("overridden_by")); } catch (SQLException ignored) {}

        return r;
    }

    /**
     * Retrieves aggregated student attendance performance summaries for faculty analytics.
     */
    public List<StudentAttendanceSummaryDTO> getStudentSummaries(
            Long facultyId, Long classId, Long batchId, LocalDate startDate, LocalDate endDate, String search, String tierFilter) {
        List<StudentAttendanceSummaryDTO> list = new ArrayList<>();

        String startStr = (startDate != null) ? startDate.toString() : "2000-01-01";
        String endStr = (endDate != null) ? endDate.toString() : "2099-12-31";

        // 1. Calculate conducted sessions (distinct dates marked for students in this class/batch)
        int conductedSessions = 0;
        StringBuilder condSql = new StringBuilder();
        condSql.append("SELECT COUNT(DISTINCT a.date) FROM attendance a ")
               .append("JOIN students s ON a.student_id = s.id ")
               .append("WHERE a.date >= ? AND a.date <= ? ");
        List<Object> condParams = new ArrayList<>();
        condParams.add(startStr);
        condParams.add(endStr);

        if (classId != null) {
            condSql.append(" AND s.class_id = ? ");
            condParams.add(classId);
        } else if (facultyId != null) {
            condSql.append(" AND (s.class_id IN (SELECT ta.class_id FROM teacher_assignments ta WHERE ta.faculty_id = ?) ")
                   .append("      OR UPPER(s.branch) IN (SELECT UPPER(c.department) FROM classes c JOIN teacher_assignments ta ON ta.class_id = c.id WHERE ta.faculty_id = ?)) ");
            condParams.add(facultyId);
            condParams.add(facultyId);
        }
        if (batchId != null) {
            condSql.append(" AND s.batch_id = ? ");
            condParams.add(batchId);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(condSql.toString())) {
            for (int i = 0; i < condParams.size(); i++) {
                ps.setObject(i + 1, condParams.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    conductedSessions = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Query student aggregations
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT s.id as s_id, s.roll_no, s.name, s.branch, s.section, ")
          .append("       s.class_id, c.code as class_code, c.name as class_name, ")
          .append("       s.batch_id, b.code as batch_code, b.name as batch_name, ")
          .append("       COALESCE(SUM(CASE WHEN UPPER(a.status) = 'PRESENT' THEN 1 ELSE 0 END), 0) as present_count, ")
          .append("       COALESCE(SUM(CASE WHEN UPPER(a.status) = 'LATE' THEN 1 ELSE 0 END), 0) as late_count, ")
          .append("       COALESCE(SUM(CASE WHEN UPPER(a.status) = 'ABSENT' THEN 1 ELSE 0 END), 0) as absent_count, ")
          .append("       COALESCE(SUM(CASE WHEN UPPER(a.status) IN ('EXCUSED', 'OD') THEN 1 ELSE 0 END), 0) as excused_count, ")
          .append("       COALESCE(SUM(CASE WHEN a.is_override = 1 THEN 1 ELSE 0 END), 0) as override_count, ")
          .append("       MAX(a.date) as last_date, ")
          .append("       (SELECT a2.status FROM attendance a2 WHERE a2.student_id = s.id AND a2.date >= ? AND a2.date <= ? ORDER BY a2.date DESC LIMIT 1) as last_status ")
          .append("FROM students s ")
          .append("LEFT JOIN classes c ON s.class_id = c.id ")
          .append("LEFT JOIN batches b ON s.batch_id = b.id ")
          .append("LEFT JOIN attendance a ON s.id = a.student_id AND a.date >= ? AND a.date <= ? ")
          .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        params.add(startStr);
        params.add(endStr);
        params.add(startStr);
        params.add(endStr);

        if (classId != null) {
            sb.append(" AND s.class_id = ? ");
            params.add(classId);
        } else if (facultyId != null) {
            sb.append(" AND (s.class_id IN (SELECT ta.class_id FROM teacher_assignments ta WHERE ta.faculty_id = ?) ")
              .append("      OR UPPER(s.branch) IN (SELECT UPPER(c.department) FROM classes c JOIN teacher_assignments ta ON ta.class_id = c.id WHERE ta.faculty_id = ?)) ");
            params.add(facultyId);
            params.add(facultyId);
        }

        if (batchId != null) {
            sb.append(" AND s.batch_id = ? ");
            params.add(batchId);
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = "%" + search.trim().toLowerCase() + "%";
            sb.append(" AND (LOWER(s.name) LIKE ? OR LOWER(s.roll_no) LIKE ?) ");
            params.add(q);
            params.add(q);
        }

        sb.append("GROUP BY s.id ORDER BY s.roll_no ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentAttendanceSummaryDTO dto = new StudentAttendanceSummaryDTO();
                    dto.setStudentId(rs.getLong("s_id"));
                    dto.setRollNo(rs.getString("roll_no"));
                    dto.setName(rs.getString("name"));
                    dto.setBranch(rs.getString("branch"));
                    dto.setSection(rs.getString("section"));
                    dto.setClassId(rs.getLong("class_id"));
                    dto.setClassCode(rs.getString("class_code"));
                    dto.setClassName(rs.getString("class_name"));
                    dto.setBatchId(rs.getLong("batch_id"));
                    dto.setBatchCode(rs.getString("batch_code"));
                    dto.setBatchName(rs.getString("batch_name"));

                    int pCount = rs.getInt("present_count");
                    int lCount = rs.getInt("late_count");
                    int aCount = rs.getInt("absent_count");
                    int eCount = rs.getInt("excused_count");
                    int oCount = rs.getInt("override_count");
                    String lDate = rs.getString("last_date");
                    String lStatus = rs.getString("last_status");

                    dto.setPresentCount(pCount);
                    dto.setLateCount(lCount);
                    dto.setAbsentCount(aCount);
                    dto.setExcusedCount(eCount);
                    dto.setOverriddenCount(oCount);
                    dto.setLastDate(lDate);
                    dto.setLastStatus(lStatus != null ? lStatus : "NOT_RECORDED");

                    int totalSessionsForStudent = Math.max(conductedSessions, (pCount + lCount + aCount + eCount));
                    dto.setTotalSessions(totalSessionsForStudent);

                    int attended = pCount + lCount;
                    int denominator = Math.max(0, totalSessionsForStudent - eCount);
                    double pct = 0.0;
                    if (denominator > 0) {
                        pct = (attended * 100.0) / denominator;
                        pct = Math.min(100.0, Math.max(0.0, pct));
                    }
                    dto.setAttendancePercentage(pct);

                    String tier = "SAFE";
                    if (pct < 60.0) {
                        tier = "CRITICAL";
                    } else if (pct < 75.0) {
                        tier = "WARNING";
                    }
                    dto.setPerformanceTier(tier);

                    // Filter by tier if specified
                    if (tierFilter != null && !tierFilter.trim().isEmpty() && !tierFilter.equalsIgnoreCase("ALL")) {
                        String tf = tierFilter.trim().toUpperCase();
                        if ("SHORTAGE".equals(tf) || "<75%".equals(tf) || "BELOW_75".equals(tf)) {
                            if (pct >= 75.0) continue;
                        } else if (!tier.equalsIgnoreCase(tf)) {
                            continue;
                        }
                    }

                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Retrieves overall class/cohort KPI analytics, distribution statistics, and daily trends.
     */
    public ClassAnalyticsDTO getClassAnalytics(Long facultyId, Long classId, Long batchId, LocalDate startDate, LocalDate endDate) {
        ClassAnalyticsDTO analytics = new ClassAnalyticsDTO();
        analytics.setClassId(classId);
        analytics.setBatchId(batchId);

        // Fetch all student summaries without tier/search filters to compute true cohort statistics
        List<StudentAttendanceSummaryDTO> allStudents = getStudentSummaries(facultyId, classId, batchId, startDate, endDate, null, null);
        analytics.setTotalEnrolled(allStudents.size());

        int conducted = 0;
        int totPresent = 0;
        int totLate = 0;
        int totAbsent = 0;
        int totExcused = 0;
        int totOverrides = 0;
        int safe = 0;
        int warn = 0;
        int crit = 0;
        List<StudentAttendanceSummaryDTO> atRisk = new ArrayList<>();

        for (StudentAttendanceSummaryDTO s : allStudents) {
            if (s.getTotalSessions() > conducted) {
                conducted = s.getTotalSessions();
            }
            totPresent += s.getPresentCount();
            totLate += s.getLateCount();
            totAbsent += s.getAbsentCount();
            totExcused += s.getExcusedCount();
            totOverrides += s.getOverriddenCount();

            if ("CRITICAL".equalsIgnoreCase(s.getPerformanceTier())) {
                crit++;
                atRisk.add(s);
            } else if ("WARNING".equalsIgnoreCase(s.getPerformanceTier())) {
                warn++;
                atRisk.add(s);
            } else {
                safe++;
            }
        }

        analytics.setConductedSessions(conducted);
        analytics.setTotalPresent(totPresent);
        analytics.setTotalLate(totLate);
        analytics.setTotalAbsent(totAbsent);
        analytics.setTotalExcused(totExcused);
        analytics.setTotalOverrides(totOverrides);
        analytics.setSafeCount(safe);
        analytics.setWarningCount(warn);
        analytics.setCriticalCount(crit);
        analytics.setAtRiskStudents(atRisk);

        int totalAttended = totPresent + totLate;
        int maxPossible = conducted * allStudents.size();
        int denominator = Math.max(1, maxPossible - totExcused);
        double overallPct = (allStudents.isEmpty() || conducted == 0) ? 0.0 : ((totalAttended * 100.0) / denominator);
        overallPct = Math.min(100.0, Math.max(0.0, overallPct));
        analytics.setOverallAttendancePercentage(overallPct);

        // Query daily trend records for the conducted dates
        String startStr = (startDate != null) ? startDate.toString() : "2000-01-01";
        String endStr = (endDate != null) ? endDate.toString() : "2099-12-31";

        StringBuilder dtSql = new StringBuilder();
        dtSql.append("SELECT a.date, ")
             .append("       COUNT(a.id) as marked_count, ")
             .append("       SUM(CASE WHEN UPPER(a.status) = 'PRESENT' THEN 1 ELSE 0 END) as p_count, ")
             .append("       SUM(CASE WHEN UPPER(a.status) = 'LATE' THEN 1 ELSE 0 END) as l_count, ")
             .append("       SUM(CASE WHEN UPPER(a.status) = 'ABSENT' THEN 1 ELSE 0 END) as a_count, ")
             .append("       SUM(CASE WHEN UPPER(a.status) IN ('EXCUSED', 'OD') THEN 1 ELSE 0 END) as e_count ")
             .append("FROM attendance a ")
             .append("JOIN students s ON a.student_id = s.id ")
             .append("WHERE a.date >= ? AND a.date <= ? ");

        List<Object> dtParams = new ArrayList<>();
        dtParams.add(startStr);
        dtParams.add(endStr);

        if (classId != null) {
            dtSql.append(" AND s.class_id = ? ");
            dtParams.add(classId);
        } else if (facultyId != null) {
            dtSql.append(" AND (s.class_id IN (SELECT ta.class_id FROM teacher_assignments ta WHERE ta.faculty_id = ?) ")
                 .append("      OR UPPER(s.branch) IN (SELECT UPPER(c.department) FROM classes c JOIN teacher_assignments ta ON ta.class_id = c.id WHERE ta.faculty_id = ?)) ");
            dtParams.add(facultyId);
            dtParams.add(facultyId);
        }
        if (batchId != null) {
            dtSql.append(" AND s.batch_id = ? ");
            dtParams.add(batchId);
        }

        dtSql.append("GROUP BY a.date ORDER BY a.date ASC");

        List<DailyTrendDTO> dailyTrends = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(dtSql.toString())) {
            for (int i = 0; i < dtParams.size(); i++) {
                ps.setObject(i + 1, dtParams.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String d = rs.getString("date");
                    int p = rs.getInt("p_count");
                    int l = rs.getInt("l_count");
                    int a = rs.getInt("a_count");
                    int e = rs.getInt("e_count");
                    int enr = allStudents.size();
                    dailyTrends.add(new DailyTrendDTO(d, enr, p, l, a, e));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        analytics.setDailyTrends(dailyTrends);

        return analytics;
    }

    /**
     * Retrieves date-by-date session attendance records for a specific student.
     */
    public List<StudentSessionHistoryDTO> getStudentSessionHistory(Long studentId, LocalDate startDate, LocalDate endDate, boolean newestFirst) {
        List<StudentSessionHistoryDTO> list = new ArrayList<>();
        if (studentId == null) return list;

        String startStr = (startDate != null) ? startDate.toString() : "2000-01-01";
        String endStr = (endDate != null) ? endDate.toString() : "2099-12-31";

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT a.*, ")
          .append("       ta.subject as assigned_subject ")
          .append("FROM attendance a ")
          .append("JOIN students s ON a.student_id = s.id ")
          .append("LEFT JOIN teacher_assignments ta ON ta.class_id = s.class_id AND (ta.batch_id IS NULL OR ta.batch_id = s.batch_id) ")
          .append("WHERE a.student_id = ? AND a.date >= ? AND a.date <= ? ")
          .append("ORDER BY a.date ").append(newestFirst ? "DESC" : "ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setLong(1, studentId);
            ps.setString(2, startStr);
            ps.setString(3, endStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentSessionHistoryDTO h = new StudentSessionHistoryDTO();
                    h.setAttendanceId(rs.getLong("id"));
                    h.setStudentId(rs.getLong("student_id"));
                    h.setDate(rs.getString("date"));
                    h.setSubject(rs.getString("assigned_subject"));
                    h.setSessionTime("09:00 AM - 10:30 AM");

                    String st = rs.getString("status");
                    h.setStatus(st != null && !st.isEmpty() ? st : "ABSENT");

                    h.setCheckInTime(rs.getString("check_in_time"));
                    h.setCheckOutTime(rs.getString("check_out_time"));

                    int isOv = rs.getInt("is_override");
                    h.setOverride(isOv == 1);
                    h.setOverrideReason(rs.getString("override_reason"));
                    h.setOverriddenBy(rs.getString("overridden_by"));

                    double lat = rs.getDouble("checkin_lat");
                    boolean hasGps = !rs.wasNull();
                    double lng = rs.getDouble("checkin_lng");
                    if (hasGps) {
                        h.setGpsCoordinates(String.format("%.4f, %.4f", lat, lng));
                    }
                    h.setPhotoPath(rs.getString("checkin_photo_path"));

                    if (isOv == 1) {
                        h.setRecordingMethod("Faculty Manual Override");
                    } else if (hasGps) {
                        h.setRecordingMethod("Automatic GPS Mobile Check-in");
                    } else {
                        h.setRecordingMethod("System Check-in");
                    }

                    list.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
