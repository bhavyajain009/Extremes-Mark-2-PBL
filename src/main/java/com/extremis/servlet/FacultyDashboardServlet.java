package com.extremis.servlet;

import com.extremis.dao.AttendanceDAO;
import com.extremis.dao.ClassDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.dao.TeacherAssignmentDAO;
import com.extremis.dto.ClassAnalyticsDTO;
import com.extremis.dto.StudentAttendanceSummaryDTO;
import com.extremis.dto.StudentSessionHistoryDTO;
import com.extremis.model.AttendanceOverrideAudit;
import com.extremis.model.AttendanceRecord;
import com.extremis.model.BatchEntity;
import com.extremis.model.ClassEntity;
import com.extremis.model.Faculty;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.Student;
import com.extremis.model.TeacherAssignment;
import com.extremis.service.RandomCheckInService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for the Comprehensive Faculty Attendance Analytics & Management Dashboard.
 */
@WebServlet("/faculty/dashboard")
public class FacultyDashboardServlet extends HttpServlet {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final TeacherAssignmentDAO assignmentDAO = new TeacherAssignmentDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final RandomCheckInService randomCheckInService = new RandomCheckInService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Faculty faculty = (session != null) ? (Faculty) session.getAttribute("currentFaculty") : null;

        if (faculty == null || !faculty.isActive()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Check for AJAX endpoint: Individual Student Session History Drawer
        String action = req.getParameter("action");
        if ("student_history".equalsIgnoreCase(action)) {
            handleStudentHistoryAjax(req, resp, faculty);
            return;
        }

        // 1. Fetch assigned classes and batches for authenticated faculty
        List<TeacherAssignment> assignments = assignmentDAO.getAssignmentsByFacultyId(faculty.getId());
        List<ClassEntity> assignedClasses = new ArrayList<>();
        List<BatchEntity> assignedBatches = new ArrayList<>();

        Set<Long> classIds = new HashSet<>();
        for (TeacherAssignment ta : assignments) {
            if (ta.getClassId() != null && classIds.add(ta.getClassId())) {
                ClassEntity ce = classDAO.getClassById(ta.getClassId());
                if (ce != null) assignedClasses.add(ce);
            }
            if (ta.getBatchId() != null) {
                BatchEntity be = classDAO.getBatchById(ta.getBatchId());
                if (be != null) assignedBatches.add(be);
            }
        }

        // Fallback for legacy assignment mapping if teacher_assignments has no explicit link
        if (assignedClasses.isEmpty()) {
            List<ClassEntity> allClasses = classDAO.getAllClasses();
            for (ClassEntity ce : allClasses) {
                if (ce.getDepartment().equalsIgnoreCase(faculty.getBranch()) ||
                    ce.getCode().toUpperCase().contains(faculty.getBranch().toUpperCase())) {
                    assignedClasses.add(ce);
                    List<BatchEntity> bList = classDAO.getBatchesByClassId(ce.getId());
                    assignedBatches.addAll(bList);
                    break;
                }
            }
        }

        // 2. Parse selection parameters
        String classIdParam = req.getParameter("classId");
        String batchIdParam = req.getParameter("batchId");
        String rangePreset = req.getParameter("rangePreset");
        String startParam = req.getParameter("startDate");
        String endParam = req.getParameter("endDate");
        String dailyDateParam = req.getParameter("dailyDate");
        String searchParam = req.getParameter("search");
        String tierFilterParam = req.getParameter("tierFilter");
        String dailyStatusFilter = req.getParameter("status");
        String tabParam = req.getParameter("tab");
        String sortParam = req.getParameter("sort");
        String dirParam = req.getParameter("dir");

        Long selectedClassId = null;
        if (classIdParam != null && !classIdParam.trim().isEmpty()) {
            try { selectedClassId = Long.parseLong(classIdParam.trim()); } catch (Exception ignored) {}
        } else if (!assignedClasses.isEmpty()) {
            selectedClassId = assignedClasses.get(0).getId();
        }

        Long selectedBatchId = null;
        if (batchIdParam != null && !batchIdParam.trim().isEmpty()) {
            try { selectedBatchId = Long.parseLong(batchIdParam.trim()); } catch (Exception ignored) {}
        }

        // Determine Reporting Date Range
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 12);

        if ("today".equalsIgnoreCase(rangePreset)) {
            startDate = endDate;
        } else if ("last_7_days".equalsIgnoreCase(rangePreset)) {
            startDate = endDate.minusDays(6);
        } else if ("this_month".equalsIgnoreCase(rangePreset)) {
            startDate = LocalDate.of(2026, 10, 1);
            endDate = LocalDate.of(2026, 10, 31);
        } else if ("all_time".equalsIgnoreCase(rangePreset)) {
            startDate = LocalDate.of(2026, 1, 1);
            endDate = LocalDate.of(2026, 12, 31);
        } else {
            // Custom or default
            if (startParam != null && !startParam.trim().isEmpty()) {
                try { startDate = LocalDate.parse(startParam.trim()); } catch (Exception ignored) {}
            }
            if (endParam != null && !endParam.trim().isEmpty()) {
                try { endDate = LocalDate.parse(endParam.trim()); } catch (Exception ignored) {}
            }
            if (rangePreset == null || rangePreset.isEmpty()) {
                rangePreset = "this_month";
            }
        }

        // Daily Roster Date
        LocalDate selectedDailyDate = LocalDate.of(2026, 10, 12);
        if (dailyDateParam != null && !dailyDateParam.trim().isEmpty()) {
            try { selectedDailyDate = LocalDate.parse(dailyDateParam.trim()); } catch (Exception ignored) {}
        }

        // Active Tab
        String activeTab = (tabParam != null && !tabParam.trim().isEmpty()) ? tabParam.trim().toLowerCase() : "overview";

        // 3. Query Real Database Data & Analytics
        ClassAnalyticsDTO analytics = attendanceDAO.getClassAnalytics(
                faculty.getId(), selectedClassId, selectedBatchId, startDate, endDate);

        List<StudentAttendanceSummaryDTO> studentSummaries = attendanceDAO.getStudentSummaries(
                faculty.getId(), selectedClassId, selectedBatchId, startDate, endDate, searchParam, tierFilterParam);

        // In-memory sorting for student summaries
        applySorting(studentSummaries, sortParam, dirParam);

        // Daily Attendance Records for the calendar/daily roster tab
        List<AttendanceRecord> dailyAttendance = attendanceDAO.getAttendanceForFaculty(
                faculty.getId(), selectedClassId, selectedBatchId, selectedDailyDate, dailyStatusFilter);

        int dailyPresent = 0;
        int dailyAbsent = 0;
        int dailyLate = 0;
        int dailyExcused = 0;
        for (AttendanceRecord r : dailyAttendance) {
            String s = r.getStatus();
            if ("PRESENT".equalsIgnoreCase(s)) dailyPresent++;
            else if ("LATE".equalsIgnoreCase(s)) dailyLate++;
            else if ("EXCUSED".equalsIgnoreCase(s) || "OD".equalsIgnoreCase(s)) dailyExcused++;
            else dailyAbsent++;
        }

        // Recent Overrides Audit Log for Reports tab
        List<AttendanceOverrideAudit> recentOverrides = attendanceDAO.getOverrideAuditHistory(faculty.getId(), 25);

        // Batches for selected class dropdown
        List<BatchEntity> classBatches = (selectedClassId != null) ? classDAO.getBatchesByClassId(selectedClassId) : assignedBatches;

        // Current Class details
        ClassEntity currentClass = null;
        if (selectedClassId != null) {
            currentClass = classDAO.getClassById(selectedClassId);
        }

        // 4. Set View Model Attributes
        req.setAttribute("faculty", faculty);
        req.setAttribute("assignments", assignments);
        req.setAttribute("assignedClasses", assignedClasses);
        req.setAttribute("classBatches", classBatches);
        req.setAttribute("currentClass", currentClass);

        req.setAttribute("selectedClassId", selectedClassId);
        req.setAttribute("selectedBatchId", selectedBatchId);
        req.setAttribute("startDate", startDate.toString());
        req.setAttribute("endDate", endDate.toString());
        req.setAttribute("rangePreset", rangePreset);
        req.setAttribute("selectedDailyDate", selectedDailyDate.toString());
        req.setAttribute("searchQuery", searchParam != null ? searchParam : "");
        req.setAttribute("tierFilter", tierFilterParam != null ? tierFilterParam : "ALL");
        req.setAttribute("dailyStatusFilter", dailyStatusFilter != null ? dailyStatusFilter : "ALL");
        req.setAttribute("activeTab", activeTab);
        req.setAttribute("sortCol", sortParam != null ? sortParam : "roll");
        req.setAttribute("sortDir", dirParam != null ? dirParam : "asc");

        req.setAttribute("analytics", analytics);
        req.setAttribute("studentSummaries", studentSummaries);
        req.setAttribute("totalEnrolled", studentSummaries.size());

        req.setAttribute("dailyAttendance", dailyAttendance);
        req.setAttribute("dailyTotal", dailyAttendance.size());
        req.setAttribute("dailyPresent", dailyPresent);
        req.setAttribute("dailyAbsent", dailyAbsent);
        req.setAttribute("dailyLate", dailyLate);
        req.setAttribute("dailyExcused", dailyExcused);

        req.setAttribute("recentOverrides", recentOverrides);

        // 5. Random Location Verification Records for Faculty Students
        LocalDate effectiveToday = randomCheckInService.getEffectiveDate();
        LocalDate dateForLocation = (dailyDateParam != null && !dailyDateParam.trim().isEmpty()) ? selectedDailyDate : effectiveToday;
        List<RandomLocationCheck> facultyLocationChecks = randomCheckInService.getLocationChecksForFaculty(faculty.getId(), dateForLocation);
        if (facultyLocationChecks.isEmpty() && !dateForLocation.equals(effectiveToday)) {
            facultyLocationChecks = randomCheckInService.getLocationChecksForFaculty(faculty.getId(), effectiveToday);
        }
        if (facultyLocationChecks.isEmpty()) {
            facultyLocationChecks = randomCheckInService.getRecentLocationChecks(40);
        }

        int locOutsideGeofenceCount = 0;
        int locVerifiedCampusCount = 0;
        int locGpsFailuresCount = 0;
        int locPendingCount = 0;
        for (RandomLocationCheck rlc : facultyLocationChecks) {
            if (rlc.isPendingFacultyReview() || "OUTSIDE_GEOFENCE".equalsIgnoreCase(rlc.getVerificationResult())) {
                locOutsideGeofenceCount++;
            } else if ("VERIFIED_CAMPUS".equalsIgnoreCase(rlc.getVerificationResult())) {
                locVerifiedCampusCount++;
            } else if ("FAILED".equalsIgnoreCase(rlc.getStatus()) || "ACCURACY_POOR".equalsIgnoreCase(rlc.getVerificationResult())) {
                locGpsFailuresCount++;
            } else if ("SCHEDULED".equalsIgnoreCase(rlc.getStatus()) || "ACTIVE".equalsIgnoreCase(rlc.getStatus())) {
                locPendingCount++;
            }
        }

        req.setAttribute("facultyLocationChecks", facultyLocationChecks);
        req.setAttribute("locTotalChecks", facultyLocationChecks.size());
        req.setAttribute("locOutsideGeofence", locOutsideGeofenceCount);
        req.setAttribute("locVerifiedCampus", locVerifiedCampusCount);
        req.setAttribute("locGpsFailures", locGpsFailuresCount);
        req.setAttribute("locPending", locPendingCount);

        req.getRequestDispatcher("/WEB-INF/views/faculty-dashboard.jsp").forward(req, resp);
    }

    private void handleStudentHistoryAjax(HttpServletRequest req, HttpServletResponse resp, Faculty faculty) throws IOException {
        String studentIdParam = req.getParameter("studentId");
        if (studentIdParam == null || studentIdParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Student ID is required\"}");
            return;
        }

        Long studentId;
        try {
            studentId = Long.parseLong(studentIdParam.trim());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid Student ID\"}");
            return;
        }

        // Authorization check
        boolean authorized = assignmentDAO.isFacultyAuthorizedForStudent(faculty.getId(), studentId);
        if (!authorized) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Unauthorized to access this student's records\"}");
            return;
        }

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\":false,\"message\":\"Student not found\"}");
            return;
        }

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 12);
        String sParam = req.getParameter("startDate");
        String eParam = req.getParameter("endDate");
        if (sParam != null && !sParam.trim().isEmpty()) {
            try { startDate = LocalDate.parse(sParam.trim()); } catch (Exception ignored) {}
        }
        if (eParam != null && !eParam.trim().isEmpty()) {
            try { endDate = LocalDate.parse(eParam.trim()); } catch (Exception ignored) {}
        }

        boolean newestFirst = !"asc".equalsIgnoreCase(req.getParameter("sortOrder"));

        List<StudentSessionHistoryDTO> sessions = attendanceDAO.getStudentSessionHistory(studentId, startDate, endDate, newestFirst);
        List<StudentAttendanceSummaryDTO> summaries = attendanceDAO.getStudentSummaries(faculty.getId(), student.getClassId(), null, startDate, endDate, null, null);

        StudentAttendanceSummaryDTO summary = null;
        for (StudentAttendanceSummaryDTO s : summaries) {
            if (s.getStudentId().equals(studentId)) {
                summary = s;
                break;
            }
        }

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":true,");
        json.append("\"student\":{");
        json.append("\"id\":").append(student.getId()).append(",");
        json.append("\"rollNo\":\"").append(escapeJson(student.getRollNo())).append("\",");
        json.append("\"name\":\"").append(escapeJson(student.getName())).append("\",");
        json.append("\"email\":\"").append(escapeJson(student.getEmail())).append("\",");
        json.append("\"branch\":\"").append(escapeJson(student.getBranch())).append("\",");
        json.append("\"section\":\"").append(escapeJson(student.getSection())).append("\",");
        json.append("\"batchName\":\"").append(escapeJson(student.getBatchName() != null ? student.getBatchName() : "General")).append("\"");
        json.append("},");

        json.append("\"summary\":{");
        if (summary != null) {
            json.append("\"totalSessions\":").append(summary.getTotalSessions()).append(",");
            json.append("\"presentCount\":").append(summary.getPresentCount()).append(",");
            json.append("\"lateCount\":").append(summary.getLateCount()).append(",");
            json.append("\"absentCount\":").append(summary.getAbsentCount()).append(",");
            json.append("\"excusedCount\":").append(summary.getExcusedCount()).append(",");
            json.append("\"attendancePercentage\":").append(summary.getAttendancePercentage()).append(",");
            json.append("\"performanceTier\":\"").append(escapeJson(summary.getPerformanceTier())).append("\",");
            json.append("\"lastDate\":\"").append(escapeJson(summary.getLastDate())).append("\",");
            json.append("\"lastStatus\":\"").append(escapeJson(summary.getLastStatus())).append("\"");
        } else {
            json.append("\"totalSessions\":0,\"presentCount\":0,\"lateCount\":0,\"absentCount\":0,\"excusedCount\":0,\"attendancePercentage\":0.0,\"performanceTier\":\"SAFE\",\"lastDate\":\"—\",\"lastStatus\":\"NOT_RECORDED\"");
        }
        json.append("},");

        json.append("\"sessions\":[");
        for (int i = 0; i < sessions.size(); i++) {
            StudentSessionHistoryDTO sess = sessions.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"attendanceId\":").append(sess.getAttendanceId() != null ? sess.getAttendanceId() : "null").append(",");
            json.append("\"date\":\"").append(escapeJson(sess.getDate())).append("\",");
            json.append("\"subject\":\"").append(escapeJson(sess.getSubject())).append("\",");
            json.append("\"sessionTime\":\"").append(escapeJson(sess.getSessionTime())).append("\",");
            json.append("\"status\":\"").append(escapeJson(sess.getStatus())).append("\",");
            json.append("\"checkInTime\":\"").append(escapeJson(sess.getCheckInTime())).append("\",");
            json.append("\"checkOutTime\":\"").append(escapeJson(sess.getCheckOutTime())).append("\",");
            json.append("\"isOverride\":").append(sess.isOverride()).append(",");
            json.append("\"overrideReason\":\"").append(escapeJson(sess.getOverrideReason() != null ? sess.getOverrideReason() : "")).append("\",");
            json.append("\"overriddenBy\":\"").append(escapeJson(sess.getOverriddenBy() != null ? sess.getOverriddenBy() : "")).append("\",");
            json.append("\"recordingMethod\":\"").append(escapeJson(sess.getRecordingMethod())).append("\",");
            json.append("\"gpsCoordinates\":\"").append(escapeJson(sess.getGpsCoordinates() != null ? sess.getGpsCoordinates() : "")).append("\"");
            json.append("}");
        }
        json.append("]");

        json.append("}");
        out.write(json.toString());
    }

    private void applySorting(List<StudentAttendanceSummaryDTO> list, String sort, String dir) {
        if (sort == null || sort.trim().isEmpty()) return;
        boolean asc = !"desc".equalsIgnoreCase(dir);

        Comparator<StudentAttendanceSummaryDTO> comp = null;
        switch (sort.toLowerCase()) {
            case "name":
                comp = Comparator.comparing(StudentAttendanceSummaryDTO::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "roll":
                comp = Comparator.comparing(StudentAttendanceSummaryDTO::getRollNo, String.CASE_INSENSITIVE_ORDER);
                break;
            case "percentage":
                comp = Comparator.comparingDouble(StudentAttendanceSummaryDTO::getAttendancePercentage);
                break;
            case "present":
                comp = Comparator.comparingInt(StudentAttendanceSummaryDTO::getPresentCount);
                break;
            case "absent":
                comp = Comparator.comparingInt(StudentAttendanceSummaryDTO::getAbsentCount);
                break;
            case "late":
                comp = Comparator.comparingInt(StudentAttendanceSummaryDTO::getLateCount);
                break;
            case "sessions":
                comp = Comparator.comparingInt(StudentAttendanceSummaryDTO::getTotalSessions);
                break;
            case "lastdate":
                comp = Comparator.comparing(StudentAttendanceSummaryDTO::getLastDate);
                break;
            default:
                comp = Comparator.comparing(StudentAttendanceSummaryDTO::getRollNo);
                break;
        }

        if (comp != null) {
            if (!asc) comp = comp.reversed();
            list.sort(comp);
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Faculty faculty = (session != null) ? (Faculty) session.getAttribute("currentFaculty") : null;

        if (faculty == null || !faculty.isActive()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("verify_location".equalsIgnoreCase(action)) {
            String checkIdStr = req.getParameter("checkId");
            String decision = req.getParameter("decision"); // "APPROVE_PRESENT" or "REJECT_ABSENT"
            String notes = req.getParameter("reasonNotes");

            if (checkIdStr != null && decision != null) {
                try {
                    Long checkId = Long.parseLong(checkIdStr.trim());
                    boolean ok = randomCheckInService.reviewStudentLocationCheck(checkId, decision, notes, faculty);
                    if (ok) {
                        if ("APPROVE_PRESENT".equalsIgnoreCase(decision)) {
                            session.setAttribute("flashSuccess", "Location verified: Student attendance approved as PRESENT (Valid reason confirmed).");
                        } else {
                            session.setAttribute("flashSuccess", "Location verified: Student attendance confirmed as ABSENT (Unauthorized outside campus).");
                        }
                    } else {
                        session.setAttribute("flashError", "Failed to update location verification review in database.");
                    }
                } catch (Exception e) {
                    session.setAttribute("flashError", "Error processing verification review: " + e.getMessage());
                }
            }
            resp.sendRedirect(req.getContextPath() + "/faculty/dashboard?tab=location");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/faculty/dashboard");
    }
}
