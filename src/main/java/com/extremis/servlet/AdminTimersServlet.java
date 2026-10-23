package com.extremis.servlet;

import com.extremis.dao.AttendanceTimerDAO;
import com.extremis.model.Admin;
import com.extremis.model.AttendanceTimerSettings;
import com.extremis.service.AttendanceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalTime;

@WebServlet("/admin/timers")
public class AdminTimersServlet extends HttpServlet {

    private final AttendanceService attendanceService = new AttendanceService();
    private final AttendanceTimerDAO timerDAO = new AttendanceTimerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Admin admin = (Admin) session.getAttribute("currentAdmin");
        AttendanceTimerSettings timerSettings = attendanceService.getTimerSettings();
        LocalTime effectiveTime = attendanceService.getEffectiveTime();

        req.setAttribute("admin", admin);
        req.setAttribute("timerSettings", timerSettings);
        req.setAttribute("effectiveTime", effectiveTime);
        req.getRequestDispatcher("/WEB-INF/views/admin-timers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        if ("toggle_checkin_timer".equalsIgnoreCase(action)) {
            String enableParam = req.getParameter("enable");
            AttendanceTimerSettings ts = attendanceService.getTimerSettings();
            boolean target = (enableParam != null) ? ("1".equals(enableParam) || "true".equalsIgnoreCase(enableParam)) : !ts.isCheckinTimerEnabled();
            timerDAO.setCheckinTimerEnabled(target);
            session.setAttribute("flashSuccess", "Check-In Timer has been turned " + (target ? "ON (Window Enforced)" : "OFF (Open Check-In / Anytime)") + ".");
        } else if ("toggle_checkout_timer".equalsIgnoreCase(action)) {
            String enableParam = req.getParameter("enable");
            AttendanceTimerSettings ts = attendanceService.getTimerSettings();
            boolean target = (enableParam != null) ? ("1".equals(enableParam) || "true".equalsIgnoreCase(enableParam)) : !ts.isCheckoutTimerEnabled();
            timerDAO.setCheckoutTimerEnabled(target);
            session.setAttribute("flashSuccess", "Check-Out Timer has been turned " + (target ? "ON (Window Enforced)" : "OFF (Open Check-Out / Anytime)") + ".");
        } else if ("update_attendance_timers".equalsIgnoreCase(action)) {
            try {
                boolean cinEnabled = "on".equalsIgnoreCase(req.getParameter("checkinEnabled")) || "true".equalsIgnoreCase(req.getParameter("checkinEnabled")) || "1".equals(req.getParameter("checkinEnabled"));
                boolean coutEnabled = "on".equalsIgnoreCase(req.getParameter("checkoutEnabled")) || "true".equalsIgnoreCase(req.getParameter("checkoutEnabled")) || "1".equals(req.getParameter("checkoutEnabled"));

                String cinStartStr = req.getParameter("checkinStartTime");
                String cinEndStr = req.getParameter("checkinEndTime");
                String cinDurStr = req.getParameter("checkinDurationMinutes");
                int cinDuration = (cinDurStr != null && !cinDurStr.trim().isEmpty()) ? Integer.parseInt(cinDurStr.trim()) : 15;

                String coutStartStr = req.getParameter("checkoutStartTime");
                String coutEndStr = req.getParameter("checkoutEndTime");
                String coutDurStr = req.getParameter("checkoutDurationMinutes");
                int coutDuration = (coutDurStr != null && !coutDurStr.trim().isEmpty()) ? Integer.parseInt(coutDurStr.trim()) : 140;

                LocalTime cinStart = LocalTime.parse(cinStartStr);
                LocalTime cinEnd = LocalTime.parse(cinEndStr);
                LocalTime coutStart = LocalTime.parse(coutStartStr);
                LocalTime coutEnd = LocalTime.parse(coutEndStr);

                if (cinEnd.isBefore(cinStart)) {
                    session.setAttribute("flashError", "Check-In End Time cannot be earlier than Check-In Start Time.");
                    resp.sendRedirect(req.getContextPath() + "/admin/timers");
                    return;
                }
                if (coutEnd.isBefore(coutStart)) {
                    session.setAttribute("flashError", "Check-Out End Time cannot be earlier than Check-Out Start Time.");
                    resp.sendRedirect(req.getContextPath() + "/admin/timers");
                    return;
                }

                AttendanceTimerSettings s = attendanceService.getTimerSettings();
                s.setCheckinTimerEnabled(cinEnabled);
                s.setCheckinStartTime(cinStart);
                s.setCheckinEndTime(cinEnd);
                s.setCheckinDurationMinutes(cinDuration);

                s.setCheckoutTimerEnabled(coutEnabled);
                s.setCheckoutStartTime(coutStart);
                s.setCheckoutEndTime(coutEnd);
                s.setCheckoutDurationMinutes(coutDuration);

                timerDAO.saveSettings(s);
                session.setAttribute("flashSuccess", "Attendance Timers and Time Slots saved successfully!");
            } catch (Exception e) {
                session.setAttribute("flashError", "Failed to update attendance timers: " + e.getMessage());
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/timers");
    }
}
