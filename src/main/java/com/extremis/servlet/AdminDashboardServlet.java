package com.extremis.servlet;

import com.extremis.dao.AttendanceDAO;
import com.extremis.dao.ClassDAO;
import com.extremis.dao.FacultyDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.dao.TeacherAssignmentDAO;
import com.extremis.model.Admin;
import com.extremis.model.ClassEntity;
import com.extremis.model.Faculty;
import com.extremis.model.RandomChallenge;
import com.extremis.model.RandomCheckInSettings;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.Student;
import com.extremis.model.StudentRandomChallenge;
import com.extremis.service.RandomCheckInService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final TeacherAssignmentDAO assignmentDAO = new TeacherAssignmentDAO();
    private final RandomCheckInService randomCheckInService = new RandomCheckInService();
    private final com.extremis.service.AttendanceService attendanceService = new com.extremis.service.AttendanceService();
    private final com.extremis.dao.AttendanceTimerDAO timerDAO = new com.extremis.dao.AttendanceTimerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Admin admin = (Admin) session.getAttribute("currentAdmin");
        List<Student> allStudents = studentDAO.getAllStudents();
        List<Faculty> allFaculties = facultyDAO.getAllFaculties();
        List<ClassEntity> allClasses = classDAO.getAllClasses();

        int activeSessions = 0;
        for (Student s : allStudents) {
            if (s.getSessionToken() != null && !s.getSessionToken().trim().isEmpty()) {
                activeSessions++;
            }
        }

        int todayCheckIns = attendanceDAO.countTodayCheckIns(LocalDate.now());
        int totalClasses = allClasses.size();
        int totalBatches = classDAO.getAllBatches().size();
        int totalAssignments = assignmentDAO.getAllAssignments().size();
        int totalOverrides = attendanceDAO.getOverrideAuditHistory(500).size();

        // Randomizer information for Admin Dashboard
        RandomCheckInSettings randomizerSettings = randomCheckInService.getSettings();
        RandomChallenge activeChallenge = randomCheckInService.getCurrentActiveChallenge();
        RandomChallenge nextChallenge = randomCheckInService.getNextScheduledChallenge();
        List<RandomChallenge> todayChallenges = randomCheckInService.getChallengesForDate(randomCheckInService.getEffectiveDate());
        List<RandomChallenge> challengeHistory = randomCheckInService.getAllChallenges(20);

        // Student responses for inspection (either selected by query param, or active challenge, or latest today)
        Long inspectedChallengeId = null;
        String inspectParam = req.getParameter("inspectId");
        if (inspectParam != null && !inspectParam.trim().isEmpty()) {
            try {
                inspectedChallengeId = Long.parseLong(inspectParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (inspectedChallengeId == null && activeChallenge != null) {
            inspectedChallengeId = activeChallenge.getId();
        } else if (inspectedChallengeId == null && !todayChallenges.isEmpty()) {
            inspectedChallengeId = todayChallenges.get(0).getId();
        } else if (inspectedChallengeId == null && !challengeHistory.isEmpty()) {
            inspectedChallengeId = challengeHistory.get(0).getId();
        }

        RandomChallenge inspectedChallenge = null;
        List<StudentRandomChallenge> inspectedResponses = null;
        if (inspectedChallengeId != null) {
            inspectedChallenge = randomCheckInService.getChallengeById(inspectedChallengeId);
            inspectedResponses = randomCheckInService.getStudentResponses(inspectedChallengeId);
        }

        req.setAttribute("admin", admin);
        req.setAttribute("students", allStudents);
        req.setAttribute("faculties", allFaculties);
        req.setAttribute("classes", allClasses);
        req.setAttribute("totalStudents", allStudents.size());
        req.setAttribute("totalFaculties", allFaculties.size());
        req.setAttribute("activeSessions", activeSessions);
        req.setAttribute("todayCheckIns", todayCheckIns);
        req.setAttribute("totalClasses", totalClasses);
        req.setAttribute("totalBatches", totalBatches);
        req.setAttribute("totalAssignments", totalAssignments);
        req.setAttribute("totalOverrides", totalOverrides);

        // Attendance Timers & Slots
        com.extremis.model.AttendanceTimerSettings timerSettings = attendanceService.getTimerSettings();
        req.setAttribute("timerSettings", timerSettings);

        // Randomizer attributes
        req.setAttribute("randomizerSettings", randomizerSettings);
        req.setAttribute("activeChallenge", activeChallenge);
        req.setAttribute("nextChallenge", nextChallenge);
        req.setAttribute("todayChallenges", todayChallenges);
        req.setAttribute("challengeHistory", challengeHistory);
        req.setAttribute("inspectedChallenge", inspectedChallenge);
        req.setAttribute("inspectedResponses", inspectedResponses);
        // Random Location Verification Checks (Requirements Section 2 & 3)
        List<RandomLocationCheck> locationChecks = randomCheckInService.getLocationChecksForDate(randomCheckInService.getEffectiveDate());
        if (locationChecks.isEmpty()) {
            locationChecks = randomCheckInService.getRecentLocationChecks(50);
        }

        int verifiedCampusCount = 0;
        int outsideGeofenceCount = 0;
        int gpsFailuresCount = 0;
        int pendingChecksCount = 0;

        for (RandomLocationCheck rlc : locationChecks) {
            String res = rlc.getVerificationResult();
            String st = rlc.getStatus();
            if ("VERIFIED_CAMPUS".equalsIgnoreCase(res)) {
                verifiedCampusCount++;
            } else if ("OUTSIDE_GEOFENCE".equalsIgnoreCase(res) || rlc.isFlaggedForReview()) {
                outsideGeofenceCount++;
            } else if ("GPS_UNAVAILABLE".equalsIgnoreCase(res) || "ACCURACY_POOR".equalsIgnoreCase(res) || "PERMISSION_DENIED".equalsIgnoreCase(res) || "EXPIRED_UNREACHABLE".equalsIgnoreCase(res) || "FAILED".equalsIgnoreCase(st)) {
                gpsFailuresCount++;
            } else if ("SCHEDULED".equalsIgnoreCase(st) || "ACTIVE".equalsIgnoreCase(st)) {
                pendingChecksCount++;
            }
        }

        req.setAttribute("locationChecks", locationChecks);
        req.setAttribute("totalLocationChecks", locationChecks.size());
        req.setAttribute("verifiedCampusCount", verifiedCampusCount);
        req.setAttribute("outsideGeofenceCount", outsideGeofenceCount);
        req.setAttribute("gpsFailuresCount", gpsFailuresCount);
        req.setAttribute("pendingChecksCount", pendingChecksCount);

        req.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Admin admin = (Admin) session.getAttribute("currentAdmin");
        String action = req.getParameter("action");

        if ("reset_session".equalsIgnoreCase(action)) {
            String studentIdStr = req.getParameter("studentId");
            if (studentIdStr != null) {
                try {
                    Long studentId = Long.parseLong(studentIdStr);
                    studentDAO.clearSessionToken(studentId);
                    req.getSession().setAttribute("flashSuccess", "Device session lock cleared successfully! The student can now log in on a new device.");
                } catch (NumberFormatException ignored) {}
            }
        } else if ("update_randomizer_settings".equalsIgnoreCase(action)) {
            try {
                boolean enabled = "on".equalsIgnoreCase(req.getParameter("enabled")) || "true".equalsIgnoreCase(req.getParameter("enabled"));
                String earliestStr = req.getParameter("earliestTime");
                String latestStr = req.getParameter("latestTime");
                int duration = Integer.parseInt(req.getParameter("durationMinutes"));
                int minInterval = Integer.parseInt(req.getParameter("minIntervalMinutes"));
                int maxInterval = Integer.parseInt(req.getParameter("maxIntervalMinutes"));
                int challengesPerDay = Integer.parseInt(req.getParameter("challengesPerDay"));
                String targetType = req.getParameter("targetType");
                String targetValue = req.getParameter("targetValue");

                LocalTime earliest = LocalTime.parse(earliestStr);
                LocalTime latest = LocalTime.parse(latestStr);

                // Server-side validations
                if (earliest.isBefore(RandomCheckInService.REGULAR_CHECKIN_END)) {
                    session.setAttribute("flashError", "Earliest random check-in time cannot be before morning check-in window closes at 9:05 AM.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }
                if (latest.isAfter(RandomCheckInService.REGULAR_CHECKOUT_START)) {
                    session.setAttribute("flashError", "Latest random check-in time cannot be after checkout window begins at 3:40 PM.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }
                if (duration < 1 || duration > 30) {
                    session.setAttribute("flashError", "Challenge duration must be between 1 and 30 minutes.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }
                if (minInterval < 10) {
                    session.setAttribute("flashError", "Minimum interval between challenges must be at least 10 minutes.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }
                if (maxInterval < minInterval) {
                    session.setAttribute("flashError", "Maximum interval must be greater than or equal to minimum interval.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }

                RandomCheckInSettings s = randomCheckInService.getSettings();
                s.setEnabled(enabled);
                s.setEarliestTime(earliest);
                s.setLatestTime(latest);
                s.setDurationMinutes(duration);
                s.setMinIntervalMinutes(minInterval);
                s.setMaxIntervalMinutes(maxInterval);
                s.setChallengesPerDay(challengesPerDay);
                s.setTargetType(targetType != null ? targetType : "ALL");
                s.setTargetValue(targetValue != null ? targetValue : "ALL");

                randomCheckInService.saveSettings(s);
                session.setAttribute("flashSuccess", "Randomizer settings updated and persisted successfully!");
            } catch (Exception e) {
                session.setAttribute("flashError", "Failed to update randomizer settings: " + e.getMessage());
            }
        } else if ("toggle_randomizer".equalsIgnoreCase(action) || "toggle_random_verification".equalsIgnoreCase(action)) {
            String enableParam = req.getParameter("enable");
            RandomCheckInSettings s = randomCheckInService.getSettings();
            boolean targetState = (enableParam != null) ? ("1".equals(enableParam) || "true".equalsIgnoreCase(enableParam)) : !s.isEnabled();
            randomCheckInService.toggleRandomizer(targetState);
            session.setAttribute("flashSuccess", "Random Location Verification has been turned " + (targetState ? "ON (ENABLED)" : "OFF (DISABLED)") + ".");
        } else if ("toggle_pause".equalsIgnoreCase(action)) {
            RandomCheckInSettings s = randomCheckInService.getSettings();
            if (s.isPaused()) {
                randomCheckInService.resumeRandomizer();
                session.setAttribute("flashSuccess", "Attendance Randomizer has been RESUMED.");
            } else {
                randomCheckInService.pauseRandomizer();
                session.setAttribute("flashSuccess", "Attendance Randomizer has been PAUSED. No automatic challenges will be scheduled or activated while paused.");
            }
        } else if ("manual_trigger".equalsIgnoreCase(action)) {
            try {
                int duration = Integer.parseInt(req.getParameter("durationMinutes"));
                String targetType = req.getParameter("targetType");
                String targetValue = req.getParameter("targetValue");

                String error = randomCheckInService.triggerManualChallenge(duration, targetType, targetValue, admin.getName() + " (" + admin.getUsername() + ")");
                if (error != null) {
                    session.setAttribute("flashError", error);
                } else {
                    session.setAttribute("flashSuccess", "Random Attendance Check-In challenge triggered immediately! Eligible students have been notified.");
                }
            } catch (Exception e) {
                session.setAttribute("flashError", "Manual trigger failed: " + e.getMessage());
            }
        } else if ("cancel_challenge".equalsIgnoreCase(action)) {
            String challengeIdStr = req.getParameter("challengeId");
            if (challengeIdStr != null) {
                try {
                    Long cid = Long.parseLong(challengeIdStr);
                    randomCheckInService.cancelChallenge(cid);
                    session.setAttribute("flashSuccess", "Challenge #" + cid + " has been cancelled.");
                } catch (NumberFormatException ignored) {}
            }
        } else if ("toggle_checkin_timer".equalsIgnoreCase(action)) {
            String enableParam = req.getParameter("enable");
            com.extremis.model.AttendanceTimerSettings ts = attendanceService.getTimerSettings();
            boolean target = (enableParam != null) ? ("1".equals(enableParam) || "true".equalsIgnoreCase(enableParam)) : !ts.isCheckinTimerEnabled();
            timerDAO.setCheckinTimerEnabled(target);
            session.setAttribute("flashSuccess", "Check-In Timer has been turned " + (target ? "ON (Window Enforced)" : "OFF (Open Check-In / Anytime)") + ".");
        } else if ("toggle_checkout_timer".equalsIgnoreCase(action)) {
            String enableParam = req.getParameter("enable");
            com.extremis.model.AttendanceTimerSettings ts = attendanceService.getTimerSettings();
            boolean target = (enableParam != null) ? ("1".equals(enableParam) || "true".equalsIgnoreCase(enableParam)) : !ts.isCheckoutTimerEnabled();
            timerDAO.setCheckoutTimerEnabled(target);
            session.setAttribute("flashSuccess", "Check-Out Timer has been turned " + (target ? "ON (Window Enforced)" : "OFF (Open Check-Out / Anytime)") + ".");
        } else if ("update_attendance_timers".equalsIgnoreCase(action)) {
            try {
                boolean cinEnabled = "on".equalsIgnoreCase(req.getParameter("checkinEnabled")) || "true".equalsIgnoreCase(req.getParameter("checkinEnabled")) || "1".equals(req.getParameter("checkinEnabled"));
                boolean coutEnabled = "on".equalsIgnoreCase(req.getParameter("checkoutEnabled")) || "true".equalsIgnoreCase(req.getParameter("checkoutEnabled")) || "1".equals(req.getParameter("checkoutEnabled"));

                String cinStartStr = req.getParameter("checkinStartTime");
                String cinEndStr = req.getParameter("checkinEndTime");
                int cinDuration = Integer.parseInt(req.getParameter("checkinDurationMinutes"));

                String coutStartStr = req.getParameter("checkoutStartTime");
                String coutEndStr = req.getParameter("checkoutEndTime");
                int coutDuration = Integer.parseInt(req.getParameter("checkoutDurationMinutes"));

                LocalTime cinStart = LocalTime.parse(cinStartStr);
                LocalTime cinEnd = LocalTime.parse(cinEndStr);
                LocalTime coutStart = LocalTime.parse(coutStartStr);
                LocalTime coutEnd = LocalTime.parse(coutEndStr);

                if (cinEnd.isBefore(cinStart)) {
                    session.setAttribute("flashError", "Check-In End Time cannot be earlier than Check-In Start Time.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }
                if (coutEnd.isBefore(coutStart)) {
                    session.setAttribute("flashError", "Check-Out End Time cannot be earlier than Check-Out Start Time.");
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                }

                com.extremis.model.AttendanceTimerSettings s = attendanceService.getTimerSettings();
                s.setCheckinTimerEnabled(cinEnabled);
                s.setCheckinStartTime(cinStart);
                s.setCheckinEndTime(cinEnd);
                s.setCheckinDurationMinutes(cinDuration);

                s.setCheckoutTimerEnabled(coutEnabled);
                s.setCheckoutStartTime(coutStart);
                s.setCheckoutEndTime(coutEnd);
                s.setCheckoutDurationMinutes(coutDuration);

                timerDAO.saveSettings(s);
                session.setAttribute("flashSuccess", "Check-In and Check-Out timer slots and durations saved successfully!");
            } catch (Exception e) {
                session.setAttribute("flashError", "Failed to update attendance timers: " + e.getMessage());
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }
}
