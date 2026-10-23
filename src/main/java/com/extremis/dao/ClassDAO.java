package com.extremis.dao;

import com.extremis.model.BatchEntity;
import com.extremis.model.ClassEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Academic Classes and Batches.
 */
public class ClassDAO {

    public List<ClassEntity> getAllClasses() {
        List<ClassEntity> list = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) as student_count, " +
                     "(SELECT COUNT(DISTINCT ta.faculty_id) FROM teacher_assignments ta WHERE ta.class_id = c.id) as teacher_count, " +
                     "(SELECT COUNT(*) FROM batches b WHERE b.class_id = c.id) as batch_count " +
                     "FROM classes c ORDER BY c.code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClassEntity ce = new ClassEntity();
                ce.setId(rs.getLong("id"));
                ce.setCode(rs.getString("code"));
                ce.setName(rs.getString("name"));
                ce.setDepartment(rs.getString("department"));
                ce.setSemester(rs.getString("semester"));
                ce.setActive(rs.getInt("is_active") == 1);
                ce.setStudentCount(rs.getInt("student_count"));
                ce.setTeacherCount(rs.getInt("teacher_count"));
                ce.setBatchCount(rs.getInt("batch_count"));
                list.add(ce);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassEntity> getActiveClasses() {
        List<ClassEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE is_active = 1 ORDER BY code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClassEntity ce = new ClassEntity();
                ce.setId(rs.getLong("id"));
                ce.setCode(rs.getString("code"));
                ce.setName(rs.getString("name"));
                ce.setDepartment(rs.getString("department"));
                ce.setSemester(rs.getString("semester"));
                ce.setActive(rs.getInt("is_active") == 1);
                list.add(ce);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ClassEntity getClassById(Long id) {
        if (id == null) return null;
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) as student_count, " +
                     "(SELECT COUNT(DISTINCT ta.faculty_id) FROM teacher_assignments ta WHERE ta.class_id = c.id) as teacher_count, " +
                     "(SELECT COUNT(*) FROM batches b WHERE b.class_id = c.id) as batch_count " +
                     "FROM classes c WHERE c.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClassEntity ce = new ClassEntity();
                    ce.setId(rs.getLong("id"));
                    ce.setCode(rs.getString("code"));
                    ce.setName(rs.getString("name"));
                    ce.setDepartment(rs.getString("department"));
                    ce.setSemester(rs.getString("semester"));
                    ce.setActive(rs.getInt("is_active") == 1);
                    ce.setStudentCount(rs.getInt("student_count"));
                    ce.setTeacherCount(rs.getInt("teacher_count"));
                    ce.setBatchCount(rs.getInt("batch_count"));
                    return ce;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClassEntity getClassByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        String sql = "SELECT * FROM classes WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClassEntity ce = new ClassEntity();
                    ce.setId(rs.getLong("id"));
                    ce.setCode(rs.getString("code"));
                    ce.setName(rs.getString("name"));
                    ce.setDepartment(rs.getString("department"));
                    ce.setSemester(rs.getString("semester"));
                    ce.setActive(rs.getInt("is_active") == 1);
                    return ce;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BatchEntity getBatchByClassAndCode(Long classId, String batchCode) {
        if (classId == null || batchCode == null || batchCode.trim().isEmpty()) return null;
        String sql = "SELECT b.*, c.name as class_name, c.code as class_code FROM batches b " +
                     "JOIN classes c ON b.class_id = c.id " +
                     "WHERE b.class_id = ? AND UPPER(b.code) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            ps.setString(2, batchCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BatchEntity be = new BatchEntity();
                    be.setId(rs.getLong("id"));
                    be.setClassId(rs.getLong("class_id"));
                    be.setCode(rs.getString("code"));
                    be.setName(rs.getString("name"));
                    be.setActive(rs.getInt("is_active") == 1);
                    be.setClassName(rs.getString("class_name"));
                    be.setClassCode(rs.getString("class_code"));
                    return be;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createClass(ClassEntity ce) {
        String sql = "INSERT INTO classes (code, name, department, semester, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ce.getCode().trim().toUpperCase());
            ps.setString(2, ce.getName().trim());
            ps.setString(3, ce.getDepartment().trim());
            ps.setString(4, ce.getSemester() != null ? ce.getSemester().trim() : "");
            ps.setInt(5, ce.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateClass(ClassEntity ce) {
        String sql = "UPDATE classes SET code = ?, name = ?, department = ?, semester = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ce.getCode().trim().toUpperCase());
            ps.setString(2, ce.getName().trim());
            ps.setString(3, ce.getDepartment().trim());
            ps.setString(4, ce.getSemester() != null ? ce.getSemester().trim() : "");
            ps.setInt(5, ce.isActive() ? 1 : 0);
            ps.setLong(6, ce.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleClassStatus(Long id, boolean active) {
        String sql = "UPDATE classes SET is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BatchEntity> getAllBatches() {
        List<BatchEntity> list = new ArrayList<>();
        String sql = "SELECT b.*, c.name as class_name, c.code as class_code, " +
                     "(SELECT COUNT(*) FROM students s WHERE s.batch_id = b.id) as student_count, " +
                     "(SELECT COUNT(DISTINCT ta.faculty_id) FROM teacher_assignments ta WHERE ta.batch_id = b.id) as teacher_count " +
                     "FROM batches b " +
                     "JOIN classes c ON b.class_id = c.id " +
                     "ORDER BY c.code ASC, b.code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BatchEntity be = new BatchEntity();
                be.setId(rs.getLong("id"));
                be.setClassId(rs.getLong("class_id"));
                be.setCode(rs.getString("code"));
                be.setName(rs.getString("name"));
                be.setActive(rs.getInt("is_active") == 1);
                be.setClassName(rs.getString("class_name"));
                be.setClassCode(rs.getString("class_code"));
                be.setStudentCount(rs.getInt("student_count"));
                be.setTeacherCount(rs.getInt("teacher_count"));
                list.add(be);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BatchEntity> getBatchesByClassId(Long classId) {
        List<BatchEntity> list = new ArrayList<>();
        if (classId == null) return list;
        String sql = "SELECT b.*, c.name as class_name, c.code as class_code, " +
                     "(SELECT COUNT(*) FROM students s WHERE s.batch_id = b.id) as student_count, " +
                     "(SELECT COUNT(DISTINCT ta.faculty_id) FROM teacher_assignments ta WHERE ta.batch_id = b.id) as teacher_count " +
                     "FROM batches b " +
                     "JOIN classes c ON b.class_id = c.id " +
                     "WHERE b.class_id = ? AND b.is_active = 1 " +
                     "ORDER BY b.code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BatchEntity be = new BatchEntity();
                    be.setId(rs.getLong("id"));
                    be.setClassId(rs.getLong("class_id"));
                    be.setCode(rs.getString("code"));
                    be.setName(rs.getString("name"));
                    be.setActive(rs.getInt("is_active") == 1);
                    be.setClassName(rs.getString("class_name"));
                    be.setClassCode(rs.getString("class_code"));
                    be.setStudentCount(rs.getInt("student_count"));
                    be.setTeacherCount(rs.getInt("teacher_count"));
                    list.add(be);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public BatchEntity getBatchById(Long id) {
        if (id == null) return null;
        String sql = "SELECT b.*, c.name as class_name, c.code as class_code, " +
                     "(SELECT COUNT(*) FROM students s WHERE s.batch_id = b.id) as student_count, " +
                     "(SELECT COUNT(DISTINCT ta.faculty_id) FROM teacher_assignments ta WHERE ta.batch_id = b.id) as teacher_count " +
                     "FROM batches b " +
                     "JOIN classes c ON b.class_id = c.id " +
                     "WHERE b.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BatchEntity be = new BatchEntity();
                    be.setId(rs.getLong("id"));
                    be.setClassId(rs.getLong("class_id"));
                    be.setCode(rs.getString("code"));
                    be.setName(rs.getString("name"));
                    be.setActive(rs.getInt("is_active") == 1);
                    be.setClassName(rs.getString("class_name"));
                    be.setClassCode(rs.getString("class_code"));
                    be.setStudentCount(rs.getInt("student_count"));
                    be.setTeacherCount(rs.getInt("teacher_count"));
                    return be;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createBatch(BatchEntity be) {
        String sql = "INSERT INTO batches (class_id, code, name, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, be.getClassId());
            ps.setString(2, be.getCode().trim().toUpperCase());
            ps.setString(3, be.getName().trim());
            ps.setInt(4, be.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBatch(BatchEntity be) {
        String sql = "UPDATE batches SET class_id = ?, code = ?, name = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, be.getClassId());
            ps.setString(2, be.getCode().trim().toUpperCase());
            ps.setString(3, be.getName().trim());
            ps.setInt(4, be.isActive() ? 1 : 0);
            ps.setLong(5, be.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleBatchStatus(Long id, boolean active) {
        String sql = "UPDATE batches SET is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
