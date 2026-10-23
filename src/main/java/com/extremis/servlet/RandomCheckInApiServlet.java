package com.extremis.servlet;

import com.extremis.model.RandomChallenge;
import com.extremis.model.RandomCheckInSettings;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.Student;
import com.extremis.service.RandomCheckInService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;

@WebServlet("/api/random-checkin/*")
public class RandomCheckInApiServlet extends HttpServlet {

    private final RandomCheckInService randomCheckInService = new RandomCheckInService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");

        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession(false);
        Student student = (session != null) ? (Student) session.getAttribute("currentStudent") : null;

        if (student == null) {
            out.print("{\"authenticated\":false,\"enabled\":false,\"checkDue\":false,\"hasActiveChallenge\":false}");
            out.flush();
            return;
        }

        String pathInfo = req.getPathInfo();
        RandomCheckInSettings settings = randomCheckInService.getSettings();

        if (pathInfo == null || "/poll".equalsIgnoreCase(pathInfo) || "/pending".equalsIgnoreCase(pathInfo)) {
            // Server-controlled random location verification poll
            if (!settings.isEnabled() || settings.isPaused()) {
                out.print("{\"authenticated\":true,\"enabled\":false,\"checkDue\":false}");
                out.flush();
                return;
            }

            RandomLocationCheck pending = randomCheckInService.getPendingLocationCheckForStudent(student.getId());
            if (pending != null) {
                LocalDateTime now = randomCheckInService.getEffectiveDateTime();
                long remainingSec = Math.max(0, Duration.between(now, pending.getWindowEndTime()).getSeconds());

                out.print("{" +
                        "\"authenticated\":true," +
                        "\"enabled\":true," +
                        "\"checkDue\":true," +
                        "\"checkId\":" + pending.getId() + "," +
                        "\"windowExpiresInSeconds\":" + remainingSec + "," +
                        "\"serverTime\":\"" + now.toString() + "\"" +
                        "}");
            } else {
                // Students cannot predict future check times (Rule 4)
                out.print("{\"authenticated\":true,\"enabled\":true,\"checkDue\":false}");
            }
            out.flush();

        } else if ("/active".equalsIgnoreCase(pathInfo)) {
            // Backward compatibility endpoint for legacy challenge tests
            RandomChallenge active = randomCheckInService.getActiveChallengeForStudent(student.getId());
            if (active != null) {
                LocalDateTime now = randomCheckInService.getEffectiveDateTime();
                long remainingSec = Math.max(0, Duration.between(now, active.getDeadlineTime()).getSeconds());

                out.print("{" +
                        "\"authenticated\":true," +
                        "\"hasActiveChallenge\":true," +
                        "\"challengeId\":" + active.getId() + "," +
                        "\"challengeCode\":\"" + active.getChallengeCode() + "\"," +
                        "\"durationMinutes\":" + active.getDurationMinutes() + "," +
                        "\"deadlineTime\":\"" + active.getDeadlineTime().toString() + "\"," +
                        "\"remainingSeconds\":" + remainingSec + "," +
                        "\"serverTime\":\"" + now.toString() + "\"" +
                        "}");
            } else {
                out.print("{\"authenticated\":true,\"hasActiveChallenge\":false}");
            }
            out.flush();

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Endpoint not found\"}");
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        Student student = (session != null) ? (Student) session.getAttribute("currentStudent") : null;

        if (student == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"Authentication required\"}");
            out.flush();
            return;
        }

        String pathInfo = req.getPathInfo();
        if ("/verify".equalsIgnoreCase(pathInfo) || "/submit".equalsIgnoreCase(pathInfo)) {
            // Parse parameters from query/form or JSON body
            String checkIdStr = req.getParameter("checkId");
            String latStr = req.getParameter("latitude");
            String lngStr = req.getParameter("longitude");
            String accStr = req.getParameter("accuracy");
            String error = req.getParameter("error");
            String clientTimestamp = req.getParameter("clientTimestamp");

            // Also check for JSON body if empty
            if (checkIdStr == null) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                String jsonBody = sb.toString();
                if (!jsonBody.isEmpty()) {
                    checkIdStr = extractJsonField(jsonBody, "checkId");
                    latStr = extractJsonField(jsonBody, "latitude");
                    lngStr = extractJsonField(jsonBody, "longitude");
                    accStr = extractJsonField(jsonBody, "accuracy");
                    error = extractJsonField(jsonBody, "error");
                    clientTimestamp = extractJsonField(jsonBody, "clientTimestamp");
                }
            }

            Long checkId = null;
            Double lat = null;
            Double lng = null;
            Double accuracy = null;

            try {
                if (checkIdStr != null && !checkIdStr.trim().isEmpty()) {
                    checkId = Long.parseLong(checkIdStr.trim());
                }
                if (latStr != null && !latStr.trim().isEmpty()) {
                    lat = Double.parseDouble(latStr.trim());
                }
                if (lngStr != null && !lngStr.trim().isEmpty()) {
                    lng = Double.parseDouble(lngStr.trim());
                }
                if (accStr != null && !accStr.trim().isEmpty()) {
                    accuracy = Double.parseDouble(accStr.trim());
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid coordinate format\"}");
                out.flush();
                return;
            }

            if (checkId == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Missing checkId\"}");
                out.flush();
                return;
            }

            String validationError = randomCheckInService.verifyLocationCheck(
                    checkId, student.getId(), lat, lng, accuracy, error, clientTimestamp
            );

            if (validationError != null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"" + escapeJson(validationError) + "\"}");
            } else {
                out.print("{\"success\":true,\"message\":\"Verification check recorded successfully\"}");
            }
            out.flush();

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Endpoint not found\"}");
            out.flush();
        }
    }

    private String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*";
        int idx = json.indexOf(pattern);
        if (idx == -1) {
            pattern = "\"" + field + "\":";
            idx = json.indexOf(pattern);
        }
        if (idx == -1) return null;

        int start = idx + pattern.length();
        if (start >= json.length()) return null;

        char c = json.charAt(start);
        if (c == '"') {
            int end = json.indexOf('"', start + 1);
            if (end != -1) return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                end++;
            }
            String val = json.substring(start, end).trim();
            return "null".equals(val) ? null : val;
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
