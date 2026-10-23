package com.extremis.dao;

import com.extremis.model.AttendanceTimerSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AttendanceTimerDAO {

    private static final DateTimeFormatter TIME_PARSE_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public AttendanceTimerSettings getSettings() {
        String sql = "SELECT * FROM attendance_timer_settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                AttendanceTimerSettings s = new AttendanceTimerSettings();
                s.setCheckinTimerEnabled(rs.getInt("checkin_timer_enabled") == 1);
                s.setCheckinStartTime(parseTime(rs.getString("checkin_start_time"), LocalTime.of(8, 50)));
                s.setCheckinEndTime(parseTime(rs.getString("checkin_end_time"), LocalTime.of(9, 5)));
                s.setCheckinDurationMinutes(rs.getInt("checkin_duration_minutes"));

                s.setCheckoutTimerEnabled(rs.getInt("checkout_timer_enabled") == 1);
                s.setCheckoutStartTime(parseTime(rs.getString("checkout_start_time"), LocalTime.of(15, 40)));
                s.setCheckoutEndTime(parseTime(rs.getString("checkout_end_time"), LocalTime.of(18, 0)));
                s.setCheckoutDurationMinutes(rs.getInt("checkout_duration_minutes"));
                s.setUpdatedAt(rs.getString("updated_at"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new AttendanceTimerSettings();
    }

    public boolean saveSettings(AttendanceTimerSettings s) {
        String sql = "INSERT INTO attendance_timer_settings " +
                "(id, checkin_timer_enabled, checkin_start_time, checkin_end_time, checkin_duration_minutes, " +
                "checkout_timer_enabled, checkout_start_time, checkout_end_time, checkout_duration_minutes, updated_at) " +
                "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now')) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "checkin_timer_enabled = excluded.checkin_timer_enabled, " +
                "checkin_start_time = excluded.checkin_start_time, " +
                "checkin_end_time = excluded.checkin_end_time, " +
                "checkin_duration_minutes = excluded.checkin_duration_minutes, " +
                "checkout_timer_enabled = excluded.checkout_timer_enabled, " +
                "checkout_start_time = excluded.checkout_start_time, " +
                "checkout_end_time = excluded.checkout_end_time, " +
                "checkout_duration_minutes = excluded.checkout_duration_minutes, " +
                "updated_at = datetime('now')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.isCheckinTimerEnabled() ? 1 : 0);
            ps.setString(2, formatTime(s.getCheckinStartTime()));
            ps.setString(3, formatTime(s.getCheckinEndTime()));
            ps.setInt(4, s.getCheckinDurationMinutes());

            ps.setInt(5, s.isCheckoutTimerEnabled() ? 1 : 0);
            ps.setString(6, formatTime(s.getCheckoutStartTime()));
            ps.setString(7, formatTime(s.getCheckoutEndTime()));
            ps.setInt(8, s.getCheckoutDurationMinutes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setCheckinTimerEnabled(boolean enabled) {
        String sql = "UPDATE attendance_timer_settings SET checkin_timer_enabled = ?, updated_at = datetime('now') WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                AttendanceTimerSettings s = getSettings();
                s.setCheckinTimerEnabled(enabled);
                return saveSettings(s);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setCheckoutTimerEnabled(boolean enabled) {
        String sql = "UPDATE attendance_timer_settings SET checkout_timer_enabled = ?, updated_at = datetime('now') WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                AttendanceTimerSettings s = getSettings();
                s.setCheckoutTimerEnabled(enabled);
                return saveSettings(s);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private LocalTime parseTime(String val, LocalTime fallback) {
        if (val == null || val.trim().isEmpty()) return fallback;
        try {
            val = val.trim();
            if (val.length() > 5) val = val.substring(0, 5);
            return LocalTime.parse(val, TIME_PARSE_FMT);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String formatTime(LocalTime lt) {
        if (lt == null) return "08:50";
        return lt.format(TIME_PARSE_FMT);
    }
}
