package com.extremis.servlet;

import com.extremis.dao.AttendanceDAO;
import com.extremis.dao.ClassDAO;
import com.extremis.dao.DBConnection;
import com.extremis.dao.StudentDAO;
import com.extremis.model.Admin;
import com.extremis.model.AttendanceRecord;
import com.extremis.model.BatchEntity;
import com.extremis.model.ClassEntity;
import com.extremis.model.Student;
import com.extremis.service.StudentImportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminStudentsServlet", urlPatterns = {"/admin/students", "/admin/students/template"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 20   // 20MB
)
public class AdminStudentsServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentImportService importService = new StudentImportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Admin admin = (Admin) session.getAttribute("currentAdmin");
        String uri = req.getRequestURI();
        String action = req.getParameter("action");

        // Handle template downloads
        if (uri.endsWith("/template") || "download_template".equalsIgnoreCase(action)) {
            String format = req.getParameter("format");
            if ("csv".equalsIgnoreCase(format)) {
                byte[] csv = importService.generateCsvTemplate();
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"extremis_students_template.csv\"");
                resp.setContentLength(csv.length);
                try (OutputStream os = resp.getOutputStream()) {
                    os.write(csv);
                    os.flush();
                }
            } else {
                byte[] xlsx = importService.generateExcelTemplate();
                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                resp.setHeader("Content-Disposition", "attachment; filename=\"extremis_students_template.xlsx\"");
                resp.setContentLength(xlsx.length);
                try (OutputStream os = resp.getOutputStream()) {
                    os.write(xlsx);
                    os.flush();
                }
            }
            return;
        }

        // Filters: classId, batchId, search
        Long selectedClassId = null;
        String classIdParam = req.getParameter("classId");
        if (classIdParam != null && !classIdParam.trim().isEmpty() && !"all".equalsIgnoreCase(classIdParam.trim())) {
            try {
                selectedClassId = Long.parseLong(classIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Long selectedBatchId = null;
        String batchIdParam = req.getParameter("batchId");
        if (batchIdParam != null && !batchIdParam.trim().isEmpty() && !"all".equalsIgnoreCase(batchIdParam.trim())) {
            try {
                selectedBatchId = Long.parseLong(batchIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        String search = req.getParameter("search");
        if (search != null) search = search.trim();

        List<Student> students;
        if (selectedClassId != null) {
            students = studentDAO.getStudentsByClassId(selectedClassId);
        } else {
            students = studentDAO.getAllStudents();
        }

        // Apply batch and search filters in memory if needed
        List<Student> filteredStudents = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Map<Long, AttendanceRecord> todayAttendanceMap = new HashMap<>();

        int presentCount = 0;
        int lockedCount = 0;

        for (Student s : students) {
            if (selectedBatchId != null && (s.getBatchId() == null || !s.getBatchId().equals(selectedBatchId))) {
                continue;
            }
            if (search != null && !search.isEmpty()) {
                String q = search.toLowerCase();
                boolean matchRoll = s.getRollNo() != null && s.getRollNo().toLowerCase().contains(q);
                boolean matchName = s.getName() != null && s.getName().toLowerCase().contains(q);
                boolean matchEmail = s.getEmail() != null && s.getEmail().toLowerCase().contains(q);
                if (!matchRoll && !matchName && !matchEmail) {
                    continue;
                }
            }

            filteredStudents.add(s);

            // Today's attendance
            AttendanceRecord att = attendanceDAO.findByStudentAndDate(s.getId(), today);
            if (att != null) {
                todayAttendanceMap.put(s.getId(), att);
                if (att.getCheckInTime() != null) {
                    presentCount++;
                }
            }

            if (s.getSessionToken() != null && !s.getSessionToken().trim().isEmpty()) {
                lockedCount++;
            }
        }

        List<ClassEntity> allClasses = classDAO.getAllClasses();
        List<BatchEntity> allBatches = classDAO.getAllBatches();
        List<BatchEntity> classBatches = (selectedClassId != null) ?
                classDAO.getBatchesByClassId(selectedClassId) : allBatches;

        int allStudentsCount = 0;
        for (ClassEntity ce : allClasses) {
            allStudentsCount += ce.getStudentCount();
        }

        ClassEntity selectedClass = null;
        if (selectedClassId != null) {
            selectedClass = classDAO.getClassById(selectedClassId);
        }

        req.setAttribute("admin", admin);
        req.setAttribute("students", filteredStudents);
        req.setAttribute("totalStudents", filteredStudents.size());
        req.setAttribute("allStudentsCount", allStudentsCount);
        req.setAttribute("presentCount", presentCount);
        req.setAttribute("lockedCount", lockedCount);
        req.setAttribute("todayAttendanceMap", todayAttendanceMap);

        req.setAttribute("allClasses", allClasses);
        req.setAttribute("allBatches", allBatches);
        req.setAttribute("classBatches", classBatches);

        req.setAttribute("selectedClassId", selectedClassId);
        req.setAttribute("selectedBatchId", selectedBatchId);
        req.setAttribute("selectedClass", selectedClass);
        req.setAttribute("searchQuery", search);

        req.getRequestDispatcher("/WEB-INF/views/admin-students.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        String returnClassId = req.getParameter("returnClassId");
        String redirectUrl = req.getContextPath() + "/admin/students" +
                (returnClassId != null && !returnClassId.isEmpty() ? "?classId=" + returnClassId : "");

        if ("add_student".equalsIgnoreCase(action)) {
            String rollNo = req.getParameter("rollNo");
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String classIdStr = req.getParameter("classId");
            String batchIdStr = req.getParameter("batchId");
            String password = req.getParameter("password");

            if (rollNo == null || rollNo.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                session.setAttribute("flashError", "Student Roll Number and Full Name are required.");
                resp.sendRedirect(redirectUrl);
                return;
            }

            rollNo = rollNo.trim().toUpperCase();
            if (studentDAO.rollNoExists(rollNo)) {
                session.setAttribute("flashError", "A student with Roll Number '" + rollNo + "' already exists in the database.");
                resp.sendRedirect(redirectUrl);
                return;
            }

            Long classId = null;
            String branch = "AI";
            String section = "Section A";
            if (classIdStr != null && !classIdStr.trim().isEmpty()) {
                try {
                    classId = Long.parseLong(classIdStr.trim());
                    ClassEntity ce = classDAO.getClassById(classId);
                    if (ce != null) {
                        branch = ce.getDepartment();
                        section = ce.getName();
                    }
                } catch (NumberFormatException ignored) {}
            }

            Long batchId = null;
            if (batchIdStr != null && !batchIdStr.trim().isEmpty()) {
                try {
                    batchId = Long.parseLong(batchIdStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            if (email == null || email.trim().isEmpty()) {
                email = rollNo.toLowerCase() + "@aryacollege.edu.in";
            } else {
                email = email.trim();
            }

            String passHash = (password != null && !password.trim().isEmpty()) ?
                    DBConnection.hashPassword(password.trim()) :
                    DBConnection.hashPassword("StudentPassword123!");

            Student s = new Student();
            s.setRollNo(rollNo);
            s.setName(name.trim());
            s.setEmail(email);
            s.setBranch(branch);
            s.setSection(section);
            s.setClassId(classId);
            s.setBatchId(batchId);
            s.setPasswordHash(passHash);
            s.setPermissionsAccepted(true);

            boolean ok = studentDAO.insertStudent(s);
            if (ok) {
                session.setAttribute("flashSuccess", "Student " + s.getName() + " (" + s.getRollNo() + ") successfully added to the database!");
            } else {
                session.setAttribute("flashError", "Failed to add student due to a database error.");
            }

        } else if ("import_excel".equalsIgnoreCase(action)) {
            try {
                Part filePart = req.getPart("file");
                if (filePart == null || filePart.getSize() == 0) {
                    session.setAttribute("flashError", "Please select a valid Excel (.xlsx, .xls) or CSV file to upload.");
                    resp.sendRedirect(redirectUrl);
                    return;
                }

                String submittedFileName = filePart.getSubmittedFileName();
                StudentImportService.ImportResult result = importService.importStudents(filePart.getInputStream(), submittedFileName);

                if (result.getImportedCount() > 0 || result.getUpdatedCount() > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Mass import completed! ")
                      .append(result.getImportedCount()).append(" new students added, ")
                      .append(result.getUpdatedCount()).append(" students updated.");
                    if (result.getFailedCount() > 0) {
                        sb.append(" (").append(result.getFailedCount()).append(" rows skipped/failed)");
                    }
                    session.setAttribute("flashSuccess", sb.toString());
                }

                if (!result.getErrors().isEmpty()) {
                    String errSummary = String.join("<br>&bull; ", result.getErrors().subList(0, Math.min(result.getErrors().size(), 5)));
                    if (result.getErrors().size() > 5) {
                        errSummary += "<br>... and " + (result.getErrors().size() - 5) + " more warnings.";
                    }
                    session.setAttribute("flashError", "Import Notes / Warnings:<br>&bull; " + errSummary);
                }
            } catch (Exception e) {
                session.setAttribute("flashError", "Mass import failed: " + e.getMessage());
            }

        } else if ("delete_student".equalsIgnoreCase(action)) {
            String studentIdStr = req.getParameter("studentId");
            if (studentIdStr != null) {
                try {
                    Long studentId = Long.parseLong(studentIdStr.trim());
                    Student s = studentDAO.findById(studentId);
                    String name = (s != null) ? s.getName() + " (" + s.getRollNo() + ")" : "Student #" + studentId;
                    boolean ok = studentDAO.deleteStudent(studentId);
                    if (ok) {
                        session.setAttribute("flashSuccess", name + " was deleted from the database.");
                    } else {
                        session.setAttribute("flashError", "Could not delete student.");
                    }
                } catch (NumberFormatException ignored) {}
            }

        } else if ("reset_session".equalsIgnoreCase(action)) {
            String studentIdStr = req.getParameter("studentId");
            if (studentIdStr != null) {
                try {
                    Long studentId = Long.parseLong(studentIdStr.trim());
                    studentDAO.clearSessionToken(studentId);
                    session.setAttribute("flashSuccess", "Device session lock cleared successfully! The student can now log in on a new device.");
                } catch (NumberFormatException ignored) {}
            }
        }

        resp.sendRedirect(redirectUrl);
    }
}
