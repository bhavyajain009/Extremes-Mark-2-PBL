package com.extremis.dao;

import com.extremis.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public Student findByRollNo(String rollNo) {
        String sql = "SELECT s.*, b.name as batch_name, b.code as batch_code " +
                     "FROM students s " +
                     "LEFT JOIN batches b ON s.batch_id = b.id " +
                     "WHERE UPPER(s.roll_no) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim());
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

    public Student findById(Long id) {
        String sql = "SELECT s.*, b.name as batch_name, b.code as batch_code " +
                     "FROM students s " +
                     "LEFT JOIN batches b ON s.batch_id = b.id " +
                     "WHERE s.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
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

    public boolean updateSessionToken(Long id, String token) {
        String sql = "UPDATE students SET session_token = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePermissions(Long id, boolean accepted) {
        String sql = "UPDATE students SET permissions_accepted = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accepted ? 1 : 0);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearSessionToken(Long id) {
        String sql = "UPDATE students SET session_token = NULL WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Student> getStudentsByClass(String branch, String section) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.name as batch_name, b.code as batch_code " +
                     "FROM students s " +
                     "LEFT JOIN batches b ON s.batch_id = b.id " +
                     "WHERE UPPER(s.branch) = UPPER(?) AND UPPER(s.section) = UPPER(?) " +
                     "ORDER BY s.roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branch.trim());
            ps.setString(2, section.trim());
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

    public List<Student> getStudentsByClassId(Long classId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.name as batch_name, b.code as batch_code " +
                     "FROM students s " +
                     "LEFT JOIN batches b ON s.batch_id = b.id " +
                     "WHERE s.class_id = ? " +
                     "ORDER BY s.roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
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

    public List<Student> getStudentsByBatchId(Long batchId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.name as batch_name, b.code as batch_code " +
                     "FROM students s " +
                     "LEFT JOIN batches b ON s.batch_id = b.id " +
                     "WHERE s.batch_id = ? " +
                     "ORDER BY s.roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, batchId);
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

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.name as batch_name, b.code as batch_code " +
                     "FROM students s " +
                     "LEFT JOIN batches b ON s.batch_id = b.id " +
                     "ORDER BY s.branch ASC, s.section ASC, s.roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean assignStudentToClassAndBatch(Long studentId, Long classId, Long batchId) {
        String sql = "UPDATE students SET class_id = ?, batch_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (classId != null) ps.setLong(1, classId); else ps.setNull(1, java.sql.Types.INTEGER);
            if (batchId != null) ps.setLong(2, batchId); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setLong(3, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rollNoExists(String rollNo) {
        if (rollNo == null || rollNo.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM students WHERE UPPER(roll_no) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim());
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

    public boolean insertStudent(Student s) {
        String sql = "INSERT INTO students (roll_no, name, email, branch, section, password_hash, permissions_accepted, class_id, batch_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getRollNo().trim().toUpperCase());
            ps.setString(2, s.getName().trim());
            ps.setString(3, s.getEmail().trim());
            ps.setString(4, s.getBranch() != null ? s.getBranch().trim() : "General");
            ps.setString(5, s.getSection() != null ? s.getSection().trim() : "Section A");
            ps.setString(6, s.getPasswordHash() != null ? s.getPasswordHash() : DBConnection.hashPassword("StudentPassword123!"));
            if (s.getClassId() != null) ps.setLong(7, s.getClassId()); else ps.setNull(7, java.sql.Types.INTEGER);
            if (s.getBatchId() != null) ps.setLong(8, s.getBatchId()); else ps.setNull(8, java.sql.Types.INTEGER);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        s.setId(gk.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStudent(Student s) {
        String sql = "UPDATE students SET name = ?, email = ?, branch = ?, section = ?, class_id = ?, batch_id = ? " +
                     (s.getPasswordHash() != null ? ", password_hash = ? " : " ") +
                     "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName().trim());
            ps.setString(2, s.getEmail().trim());
            ps.setString(3, s.getBranch() != null ? s.getBranch().trim() : "General");
            ps.setString(4, s.getSection() != null ? s.getSection().trim() : "Section A");
            if (s.getClassId() != null) ps.setLong(5, s.getClassId()); else ps.setNull(5, java.sql.Types.INTEGER);
            if (s.getBatchId() != null) ps.setLong(6, s.getBatchId()); else ps.setNull(6, java.sql.Types.INTEGER);
            if (s.getPasswordHash() != null) {
                ps.setString(7, s.getPasswordHash());
                ps.setLong(8, s.getId());
            } else {
                ps.setLong(7, s.getId());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStudent(Long id) {
        if (id == null) return false;
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setRollNo(rs.getString("roll_no"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setBranch(rs.getString("branch"));
        s.setSection(rs.getString("section"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setSessionToken(rs.getString("session_token"));

        try {
            s.setPermissionsAccepted(rs.getInt("permissions_accepted") == 1);
        } catch (SQLException ignored) {}

        try {
            long cid = rs.getLong("class_id");
            if (!rs.wasNull()) s.setClassId(cid);
        } catch (SQLException ignored) {}

        try {
            long bid = rs.getLong("batch_id");
            if (!rs.wasNull()) s.setBatchId(bid);
        } catch (SQLException ignored) {}

        try {
            s.setBatchName(rs.getString("batch_name"));
            s.setBatchCode(rs.getString("batch_code"));
        } catch (SQLException ignored) {}

        return s;
    }
}
