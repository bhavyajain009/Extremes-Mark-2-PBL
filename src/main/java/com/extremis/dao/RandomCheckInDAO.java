package com.extremis.dao;

import com.extremis.model.RandomChallenge;
import com.extremis.model.RandomCheckInSettings;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.StudentRandomChallenge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RandomCheckInDAO {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public RandomCheckInSettings getSettings() {
        String sql = "SELECT * FROM random_checkin_settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                RandomCheckInSettings s = new RandomCheckInSettings();
                s.setEnabled(rs.getInt("is_enabled") == 1);
                s.setPaused(rs.getInt("is_paused") == 1);
                s.setEarliestTime(LocalTime.parse(rs.getString("earliest_time")));
                s.setLatestTime(LocalTime.parse(rs.getString("latest_time")));
                s.setDurationMinutes(rs.getInt("duration_minutes"));
                s.setMinIntervalMinutes(rs.getInt("min_interval_minutes"));
                s.setMaxIntervalMinutes(rs.getInt("max_interval_minutes"));
                s.setChallengesPerDay(rs.getInt("challenges_per_day"));
                s.setTargetType(rs.getString("target_type"));
                s.setTargetValue(rs.getString("target_value"));
                s.setUpdatedAt(rs.getString("updated_at"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new RandomCheckInSettings();
    }

    public boolean saveSettings(RandomCheckInSettings s) {
        String sql = "INSERT INTO random_checkin_settings (id, is_enabled, is_paused, earliest_time, latest_time, " +
                "duration_minutes, min_interval_minutes, max_interval_minutes, challenges_per_day, target_type, target_value, updated_at) " +
                "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now')) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "is_enabled = excluded.is_enabled, " +
                "is_paused = excluded.is_paused, " +
                "earliest_time = excluded.earliest_time, " +
                "latest_time = excluded.latest_time, " +
                "duration_minutes = excluded.duration_minutes, " +
                "min_interval_minutes = excluded.min_interval_minutes, " +
                "max_interval_minutes = excluded.max_interval_minutes, " +
                "challenges_per_day = excluded.challenges_per_day, " +
                "target_type = excluded.target_type, " +
                "target_value = excluded.target_value, " +
                "updated_at = datetime('now')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.isEnabled() ? 1 : 0);
            ps.setInt(2, s.isPaused() ? 1 : 0);
            ps.setString(3, s.getEarliestTime().toString().substring(0, 5));
            ps.setString(4, s.getLatestTime().toString().substring(0, 5));
            ps.setInt(5, s.getDurationMinutes());
            ps.setInt(6, s.getMinIntervalMinutes());
            ps.setInt(7, s.getMaxIntervalMinutes());
            ps.setInt(8, s.getChallengesPerDay());
            ps.setString(9, s.getTargetType() != null ? s.getTargetType() : "ALL");
            ps.setString(10, s.getTargetValue() != null ? s.getTargetValue() : "ALL");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setPaused(boolean paused) {
        String sql = "UPDATE random_checkin_settings SET is_paused = ?, updated_at = datetime('now') WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paused ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setEnabled(boolean enabled) {
        String sql = "UPDATE random_checkin_settings SET is_enabled = ?, updated_at = datetime('now') WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Long createChallenge(RandomChallenge c, List<Long> studentIds) {
        String insertChallenge = "INSERT INTO random_challenges (challenge_code, scheduled_time, deadline_time, duration_minutes, status, target_type, target_value, is_manual, created_at, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertStudent = "INSERT OR IGNORE INTO student_random_challenges (challenge_id, student_id, status) VALUES (?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            Long challengeId = null;

            try (PreparedStatement ps = conn.prepareStatement(insertChallenge, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getChallengeCode());
                ps.setString(2, c.getScheduledTime().format(ISO_FORMATTER));
                ps.setString(3, c.getDeadlineTime().format(ISO_FORMATTER));
                ps.setInt(4, c.getDurationMinutes());
                ps.setString(5, c.getStatus());
                ps.setString(6, c.getTargetType());
                ps.setString(7, c.getTargetValue());
                ps.setInt(8, c.isManual() ? 1 : 0);
                ps.setString(9, c.getCreatedAt().format(ISO_FORMATTER));
                ps.setString(10, c.getCreatedBy());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        challengeId = rs.getLong(1);
                    }
                }
            }

            if (challengeId != null && studentIds != null && !studentIds.isEmpty()) {
                try (PreparedStatement psStu = conn.prepareStatement(insertStudent)) {
                    for (Long sId : studentIds) {
                        psStu.setLong(1, challengeId);
                        psStu.setLong(2, sId);
                        psStu.addBatch();
                    }
                    psStu.executeBatch();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return challengeId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public RandomChallenge getChallengeById(Long id) {
        String sql = "SELECT * FROM random_challenges WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChallengeRow(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RandomChallenge getChallengeByCode(String code) {
        String sql = "SELECT * FROM random_challenges WHERE challenge_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChallengeRow(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RandomChallenge getActiveChallengeForStudent(Long studentId, LocalDateTime now) {
        String sql = "SELECT c.* FROM random_challenges c " +
                "JOIN student_random_challenges sc ON c.id = sc.challenge_id " +
                "WHERE sc.student_id = ? " +
                "AND c.status = 'ACTIVE' " +
                "AND sc.status = 'PENDING' " +
                "AND datetime(c.deadline_time) >= datetime(?) " +
                "ORDER BY c.scheduled_time ASC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChallengeRow(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StudentRandomChallenge getStudentChallenge(Long challengeId, Long studentId) {
        String sql = "SELECT sc.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section, " +
                "c.challenge_code, c.deadline_time " +
                "FROM student_random_challenges sc " +
                "JOIN students s ON sc.student_id = s.id " +
                "JOIN random_challenges c ON sc.challenge_id = c.id " +
                "WHERE sc.challenge_id = ? AND sc.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, challengeId);
            ps.setLong(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudentChallengeRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStudentResponse(Long challengeId, Long studentId, Double lat, Double lng, Double acc, String photoPath, LocalDateTime respondedAt) {
        String sql = "UPDATE student_random_challenges SET status = 'RESPONDED', responded_at = ?, lat = ?, lng = ?, accuracy = ?, photo_path = ? " +
                "WHERE challenge_id = ? AND student_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, respondedAt.format(ISO_FORMATTER));
            if (lat != null) ps.setDouble(2, lat); else ps.setNull(2, java.sql.Types.REAL);
            if (lng != null) ps.setDouble(3, lng); else ps.setNull(3, java.sql.Types.REAL);
            if (acc != null) ps.setDouble(4, acc); else ps.setNull(4, java.sql.Types.REAL);
            ps.setString(5, photoPath);
            ps.setLong(6, challengeId);
            ps.setLong(7, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RandomChallenge> getScheduledChallengesDue(LocalDateTime now) {
        List<RandomChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM random_challenges WHERE status = 'SCHEDULED' AND datetime(scheduled_time) <= datetime(?) ORDER BY scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChallengeRow(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markChallengeActive(Long challengeId) {
        String sql = "UPDATE random_challenges SET status = 'ACTIVE' WHERE id = ? AND status = 'SCHEDULED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, challengeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RandomChallenge> getExpiredActiveChallenges(LocalDateTime now) {
        List<RandomChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM random_challenges WHERE status = 'ACTIVE' AND datetime(deadline_time) < datetime(?) ORDER BY deadline_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChallengeRow(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markChallengeCompleted(Long challengeId) {
        String sql = "UPDATE random_challenges SET status = 'COMPLETED' WHERE id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, challengeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Long> markPendingStudentsMissed(Long challengeId) {
        List<Long> missedStudentIds = new ArrayList<>();
        String queryPending = "SELECT student_id FROM student_random_challenges WHERE challenge_id = ? AND status = 'PENDING'";
        String updateMissed = "UPDATE student_random_challenges SET status = 'MISSED' WHERE challenge_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psQuery = conn.prepareStatement(queryPending)) {
                psQuery.setLong(1, challengeId);
                try (ResultSet rs = psQuery.executeQuery()) {
                    while (rs.next()) {
                        missedStudentIds.add(rs.getLong("student_id"));
                    }
                }
            }

            if (!missedStudentIds.isEmpty()) {
                try (PreparedStatement psUp = conn.prepareStatement(updateMissed)) {
                    psUp.setLong(1, challengeId);
                    psUp.executeUpdate();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missedStudentIds;
    }

    public RandomChallenge getCurrentActiveChallenge(LocalDateTime now) {
        String sql = "SELECT * FROM random_challenges WHERE status = 'ACTIVE' AND datetime(deadline_time) >= datetime(?) ORDER BY scheduled_time DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChallengeRow(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RandomChallenge getNextScheduledChallenge(LocalDate date, LocalDateTime now) {
        String sql = "SELECT * FROM random_challenges WHERE status = 'SCHEDULED' " +
                "AND date(scheduled_time) = date(?) " +
                "AND datetime(scheduled_time) > datetime(?) " +
                "ORDER BY scheduled_time ASC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setString(2, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChallengeRow(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RandomChallenge> getChallengesForDate(LocalDate date) {
        List<RandomChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM random_challenges WHERE date(scheduled_time) = date(?) ORDER BY scheduled_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChallengeRow(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RandomChallenge> getAllChallenges(int limit) {
        List<RandomChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM random_challenges ORDER BY scheduled_time DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChallengeRow(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<StudentRandomChallenge> getStudentResponsesForChallenge(Long challengeId) {
        List<StudentRandomChallenge> list = new ArrayList<>();
        String sql = "SELECT sc.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section, " +
                "c.challenge_code, c.deadline_time " +
                "FROM student_random_challenges sc " +
                "JOIN students s ON sc.student_id = s.id " +
                "JOIN random_challenges c ON sc.challenge_id = c.id " +
                "WHERE sc.challenge_id = ? " +
                "ORDER BY sc.status DESC, s.roll_no ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, challengeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStudentChallengeRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean hasChallengesScheduledForDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM random_challenges WHERE date(scheduled_time) = date(?) AND is_manual = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelChallenge(Long challengeId) {
        String sql = "UPDATE random_challenges SET status = 'CANCELLED' WHERE id = ? AND status IN ('SCHEDULED', 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, challengeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RandomChallenge mapChallengeRow(ResultSet rs, Connection conn) throws SQLException {
        RandomChallenge c = new RandomChallenge();
        c.setId(rs.getLong("id"));
        c.setChallengeCode(rs.getString("challenge_code"));
        c.setScheduledTime(LocalDateTime.parse(rs.getString("scheduled_time")));
        c.setDeadlineTime(LocalDateTime.parse(rs.getString("deadline_time")));
        c.setDurationMinutes(rs.getInt("duration_minutes"));
        c.setStatus(rs.getString("status"));
        c.setTargetType(rs.getString("target_type"));
        c.setTargetValue(rs.getString("target_value"));
        c.setManual(rs.getInt("is_manual") == 1);
        c.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        c.setCreatedBy(rs.getString("created_by"));

        // Load stats
        String statsSql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'RESPONDED' THEN 1 ELSE 0 END) as responded, " +
                "SUM(CASE WHEN status = 'MISSED' THEN 1 ELSE 0 END) as missed, " +
                "SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending " +
                "FROM student_random_challenges WHERE challenge_id = ?";

        try (PreparedStatement psStats = conn.prepareStatement(statsSql)) {
            psStats.setLong(1, c.getId());
            try (ResultSet rsStats = psStats.executeQuery()) {
                if (rsStats.next()) {
                    c.setTotalAssigned(rsStats.getInt("total"));
                    c.setRespondedCount(rsStats.getInt("responded"));
                    c.setMissedCount(rsStats.getInt("missed"));
                    c.setPendingCount(rsStats.getInt("pending"));
                }
            }
        } catch (SQLException ignored) {}

        return c;
    }

    private StudentRandomChallenge mapStudentChallengeRow(ResultSet rs) throws SQLException {
        StudentRandomChallenge sc = new StudentRandomChallenge();
        sc.setId(rs.getLong("id"));
        sc.setChallengeId(rs.getLong("challenge_id"));
        sc.setStudentId(rs.getLong("student_id"));
        sc.setStatus(rs.getString("status"));

        String respAt = rs.getString("responded_at");
        if (respAt != null && !respAt.isEmpty()) {
            sc.setRespondedAt(LocalDateTime.parse(respAt));
        }

        double lat = rs.getDouble("lat");
        if (!rs.wasNull()) sc.setLat(lat);
        double lng = rs.getDouble("lng");
        if (!rs.wasNull()) sc.setLng(lng);
        double acc = rs.getDouble("accuracy");
        if (!rs.wasNull()) sc.setAccuracy(acc);

        sc.setPhotoPath(rs.getString("photo_path"));
        sc.setStudentName(rs.getString("student_name"));
        sc.setStudentRollNo(rs.getString("student_roll"));
        sc.setStudentBranch(rs.getString("student_branch"));
        sc.setStudentSection(rs.getString("student_section"));
        sc.setChallengeCode(rs.getString("challenge_code"));

        String dl = rs.getString("deadline_time");
        if (dl != null && !dl.isEmpty()) {
            sc.setDeadlineTime(LocalDateTime.parse(dl));
        }

        return sc;
    }

    // =========================================================================
    // Individual Random Location Verification Checks (Requirements Section 2 & 3)
    // =========================================================================

    public boolean createLocationChecks(List<RandomLocationCheck> checks) {
        if (checks == null || checks.isEmpty()) return true;
        String sql = "INSERT INTO random_location_checks (student_id, attendance_id, check_date, scheduled_time, window_end_time, " +
                "status, verification_result, lat, lng, accuracy, distance_meters, server_timestamp, client_timestamp, failure_reason, flagged_for_review, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (RandomLocationCheck c : checks) {
                    ps.setLong(1, c.getStudentId());
                    if (c.getAttendanceId() != null) ps.setLong(2, c.getAttendanceId()); else ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setString(3, c.getCheckDate().toString());
                    ps.setString(4, c.getScheduledTime().format(ISO_FORMATTER));
                    ps.setString(5, c.getWindowEndTime().format(ISO_FORMATTER));
                    ps.setString(6, c.getStatus());
                    ps.setString(7, c.getVerificationResult());
                    if (c.getLat() != null) ps.setDouble(8, c.getLat()); else ps.setNull(8, java.sql.Types.REAL);
                    if (c.getLng() != null) ps.setDouble(9, c.getLng()); else ps.setNull(9, java.sql.Types.REAL);
                    if (c.getAccuracy() != null) ps.setDouble(10, c.getAccuracy()); else ps.setNull(10, java.sql.Types.REAL);
                    if (c.getDistanceMeters() != null) ps.setDouble(11, c.getDistanceMeters()); else ps.setNull(11, java.sql.Types.REAL);
                    if (c.getServerTimestamp() != null) ps.setString(12, c.getServerTimestamp().format(ISO_FORMATTER)); else ps.setNull(12, java.sql.Types.VARCHAR);
                    ps.setString(13, c.getClientTimestamp());
                    ps.setString(14, c.getFailureReason());
                    ps.setInt(15, c.isFlaggedForReview() ? 1 : 0);
                    ps.setString(16, c.getCreatedAt().format(ISO_FORMATTER));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Long createSingleLocationCheck(RandomLocationCheck c) {
        String sql = "INSERT INTO random_location_checks (student_id, attendance_id, check_date, scheduled_time, " +
                "window_end_time, status, verification_result, lat, lng, accuracy, distance_meters, " +
                "server_timestamp, client_timestamp, failure_reason, flagged_for_review, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, c.getStudentId());
            if (c.getAttendanceId() != null) ps.setLong(2, c.getAttendanceId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, c.getCheckDate().toString());
            ps.setString(4, c.getScheduledTime().format(ISO_FORMATTER));
            ps.setString(5, c.getWindowEndTime().format(ISO_FORMATTER));
            ps.setString(6, c.getStatus());
            ps.setString(7, c.getVerificationResult());
            if (c.getLat() != null) ps.setDouble(8, c.getLat()); else ps.setNull(8, java.sql.Types.REAL);
            if (c.getLng() != null) ps.setDouble(9, c.getLng()); else ps.setNull(9, java.sql.Types.REAL);
            if (c.getAccuracy() != null) ps.setDouble(10, c.getAccuracy()); else ps.setNull(10, java.sql.Types.REAL);
            if (c.getDistanceMeters() != null) ps.setDouble(11, c.getDistanceMeters()); else ps.setNull(11, java.sql.Types.REAL);
            if (c.getServerTimestamp() != null) ps.setString(12, c.getServerTimestamp().format(ISO_FORMATTER)); else ps.setNull(12, java.sql.Types.VARCHAR);
            ps.setString(13, c.getClientTimestamp());
            ps.setString(14, c.getFailureReason());
            ps.setInt(15, c.isFlaggedForReview() ? 1 : 0);
            ps.setString(16, c.getCreatedAt().format(ISO_FORMATTER));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    c.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteLocationChecksForStudentAndDate(Long studentId, LocalDate date) {
        String sql = "DELETE FROM random_location_checks WHERE student_id = ? AND check_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, date.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasStudentLocationChecks(Long studentId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM random_location_checks WHERE student_id = ? AND check_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public RandomLocationCheck getPendingLocationCheckForStudent(Long studentId, LocalDateTime now) {
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.student_id = ? " +
                "AND r.status IN ('SCHEDULED', 'ACTIVE') " +
                "AND datetime(r.scheduled_time) <= datetime(?) " +
                "AND datetime(r.window_end_time) >= datetime(?) " +
                "ORDER BY r.scheduled_time ASC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, now.format(ISO_FORMATTER));
            ps.setString(3, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRandomLocationCheckRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RandomLocationCheck getLocationCheckById(Long id) {
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRandomLocationCheckRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateLocationCheckResponse(Long checkId, Long studentId, String status, String verificationResult,
                                              Double lat, Double lng, Double accuracy, Double dist,
                                              LocalDateTime serverTimestamp, String clientTimestamp,
                                              String failureReason, boolean flaggedForReview) {
        String sql = "UPDATE random_location_checks SET status = ?, verification_result = ?, " +
                "lat = ?, lng = ?, accuracy = ?, distance_meters = ?, server_timestamp = ?, " +
                "client_timestamp = ?, failure_reason = ?, flagged_for_review = ? " +
                "WHERE id = ? AND student_id = ? AND status IN ('SCHEDULED', 'ACTIVE')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, verificationResult);
            if (lat != null) ps.setDouble(3, lat); else ps.setNull(3, java.sql.Types.REAL);
            if (lng != null) ps.setDouble(4, lng); else ps.setNull(4, java.sql.Types.REAL);
            if (accuracy != null) ps.setDouble(5, accuracy); else ps.setNull(5, java.sql.Types.REAL);
            if (dist != null) ps.setDouble(6, dist); else ps.setNull(6, java.sql.Types.REAL);
            ps.setString(7, serverTimestamp.format(ISO_FORMATTER));
            ps.setString(8, clientTimestamp);
            ps.setString(9, failureReason);
            ps.setInt(10, flaggedForReview ? 1 : 0);
            ps.setLong(11, checkId);
            ps.setLong(12, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RandomLocationCheck> getActiveLocationChecksDue(LocalDateTime now) {
        List<RandomLocationCheck> list = new ArrayList<>();
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.status = 'SCHEDULED' AND datetime(r.scheduled_time) <= datetime(?) " +
                "ORDER BY r.scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRandomLocationCheckRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markLocationCheckActive(Long checkId) {
        String sql = "UPDATE random_location_checks SET status = 'ACTIVE' WHERE id = ? AND status = 'SCHEDULED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, checkId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RandomLocationCheck> getExpiredActiveLocationChecks(LocalDateTime now) {
        List<RandomLocationCheck> list = new ArrayList<>();
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.status IN ('SCHEDULED', 'ACTIVE') AND datetime(r.window_end_time) < datetime(?) " +
                "ORDER BY r.window_end_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now.format(ISO_FORMATTER));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRandomLocationCheckRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markLocationCheckExpired(Long checkId, LocalDateTime serverTime) {
        String sql = "UPDATE random_location_checks SET status = 'EXPIRED', verification_result = 'EXPIRED_UNREACHABLE', " +
                "failure_reason = 'Verification window expired without device response (phone offline, app closed, or unreachable)', " +
                "server_timestamp = ? WHERE id = ? AND status IN ('SCHEDULED', 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serverTime.format(ISO_FORMATTER));
            ps.setLong(2, checkId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelAllPendingLocationChecksToday(LocalDate date) {
        String sql = "UPDATE random_location_checks SET status = 'CANCELLED' WHERE check_date = ? AND status IN ('SCHEDULED', 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RandomLocationCheck> getRecentLocationChecks(int limit) {
        List<RandomLocationCheck> list = new ArrayList<>();
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "ORDER BY r.scheduled_time DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRandomLocationCheckRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RandomLocationCheck> getLocationChecksForDate(LocalDate date) {
        List<RandomLocationCheck> list = new ArrayList<>();
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.check_date = ? " +
                "ORDER BY r.scheduled_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRandomLocationCheckRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RandomLocationCheck> getLocationChecksForStudent(Long studentId, LocalDate date) {
        List<RandomLocationCheck> list = new ArrayList<>();
        String sql = "SELECT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "WHERE r.student_id = ? AND r.check_date = ? " +
                "ORDER BY r.scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRandomLocationCheckRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RandomLocationCheck> getLocationChecksForFaculty(Long facultyId, LocalDate date) {
        List<RandomLocationCheck> list = new ArrayList<>();
        String sql = "SELECT DISTINCT r.*, s.name as student_name, s.roll_no as student_roll, s.branch as student_branch, s.section as student_section " +
                "FROM random_location_checks r " +
                "JOIN students s ON r.student_id = s.id " +
                "LEFT JOIN teacher_assignments ta ON (ta.class_id = s.class_id OR (ta.class_id IS NULL AND s.branch = ta.subject)) " +
                "WHERE (ta.faculty_id = ? OR s.branch = (SELECT branch FROM faculties WHERE id = ?)) " +
                (date != null ? "AND r.check_date = ? " : "") +
                "ORDER BY r.scheduled_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, facultyId);
            ps.setLong(2, facultyId);
            if (date != null) {
                ps.setString(3, date.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRandomLocationCheckRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean resolveLocationCheckReview(Long checkId, String reviewStatus, String reviewNotes, String reviewedBy, LocalDateTime reviewedAt) {
        String sql = "UPDATE random_location_checks SET faculty_review_status = ?, faculty_review_notes = ?, faculty_reviewed_by = ?, faculty_reviewed_at = ?, flagged_for_review = 0 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewStatus);
            ps.setString(2, reviewNotes);
            ps.setString(3, reviewedBy);
            ps.setString(4, reviewedAt != null ? reviewedAt.format(ISO_FORMATTER) : null);
            ps.setLong(5, checkId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RandomLocationCheck mapRandomLocationCheckRow(ResultSet rs) throws SQLException {
        RandomLocationCheck c = new RandomLocationCheck();
        c.setId(rs.getLong("id"));
        c.setStudentId(rs.getLong("student_id"));
        long attId = rs.getLong("attendance_id");
        if (!rs.wasNull()) c.setAttendanceId(attId);
        c.setCheckDate(LocalDate.parse(rs.getString("check_date")));
        c.setScheduledTime(LocalDateTime.parse(rs.getString("scheduled_time")));
        c.setWindowEndTime(LocalDateTime.parse(rs.getString("window_end_time")));
        c.setStatus(rs.getString("status"));
        c.setVerificationResult(rs.getString("verification_result"));

        double lat = rs.getDouble("lat");
        if (!rs.wasNull()) c.setLat(lat);
        double lng = rs.getDouble("lng");
        if (!rs.wasNull()) c.setLng(lng);
        double acc = rs.getDouble("accuracy");
        if (!rs.wasNull()) c.setAccuracy(acc);
        double dist = rs.getDouble("distance_meters");
        if (!rs.wasNull()) c.setDistanceMeters(dist);

        String serverTs = rs.getString("server_timestamp");
        if (serverTs != null && !serverTs.isEmpty()) {
            c.setServerTimestamp(LocalDateTime.parse(serverTs));
        }
        c.setClientTimestamp(rs.getString("client_timestamp"));
        c.setFailureReason(rs.getString("failure_reason"));
        c.setFlaggedForReview(rs.getInt("flagged_for_review") == 1);
        c.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));

        try {
            c.setStudentName(rs.getString("student_name"));
            c.setStudentRollNo(rs.getString("student_roll"));
            c.setStudentBranch(rs.getString("student_branch"));
            c.setStudentSection(rs.getString("student_section"));
        } catch (SQLException ignored) {}

        try {
            c.setFacultyReviewStatus(rs.getString("faculty_review_status"));
            c.setFacultyReviewNotes(rs.getString("faculty_review_notes"));
            c.setFacultyReviewedBy(rs.getString("faculty_reviewed_by"));
            String revAt = rs.getString("faculty_reviewed_at");
            if (revAt != null && !revAt.isEmpty()) {
                c.setFacultyReviewedAt(LocalDateTime.parse(revAt));
            }
        } catch (SQLException ignored) {}

        return c;
    }
}
