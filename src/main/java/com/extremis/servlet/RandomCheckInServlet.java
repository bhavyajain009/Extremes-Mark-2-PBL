package com.extremis.servlet;

import com.extremis.dao.RandomCheckInDAO;
import com.extremis.model.RandomChallenge;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@WebServlet("/random-checkin")
public class RandomCheckInServlet extends HttpServlet {

    private final RandomCheckInService randomCheckInService = new RandomCheckInService();
    private final RandomCheckInDAO randomCheckInDAO = new RandomCheckInDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Student student = (session != null) ? (Student) session.getAttribute("currentStudent") : null;

        if (student == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/random-checkin");
            return;
        }

        RandomChallenge active = randomCheckInService.getActiveChallengeForStudent(student.getId());
        LocalDateTime now = randomCheckInService.getEffectiveDateTime();

        if (active != null) {
            long remainingSeconds = Math.max(0, Duration.between(now, active.getDeadlineTime()).getSeconds());
            req.setAttribute("hasActiveChallenge", true);
            req.setAttribute("challenge", active);
            req.setAttribute("remainingSeconds", remainingSeconds);
        } else {
            req.setAttribute("hasActiveChallenge", false);
            // Check if there was a challenge ID passed in query param to display completed/missed status
            String cidStr = req.getParameter("challengeId");
            if (cidStr != null) {
                try {
                    Long cid = Long.parseLong(cidStr);
                    StudentRandomChallenge sc = randomCheckInDAO.getStudentChallenge(cid, student.getId());
                    if (sc != null) {
                        req.setAttribute("pastChallengeStatus", sc.getStatus());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        List<RandomLocationCheck> studentChecks = randomCheckInService.getLocationChecksForStudent(
                student.getId(), randomCheckInService.getEffectiveDate()
        );
        req.setAttribute("studentChecks", studentChecks);
        req.setAttribute("randomizerSettings", randomCheckInService.getSettings());

        req.setAttribute("pageTitle", "Random Attendance Check-In");
        req.setAttribute("activeTab", "home");
        req.getRequestDispatcher("/WEB-INF/views/random-checkin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Student student = (session != null) ? (Student) session.getAttribute("currentStudent") : null;

        if (student == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/random-checkin");
            return;
        }

        String challengeIdStr = req.getParameter("challengeId");
        String latStr = req.getParameter("latitude");
        String lngStr = req.getParameter("longitude");
        String accStr = req.getParameter("accuracy");
        String photoBase64 = req.getParameter("photoBase64");

        Long challengeId = null;
        Double lat = null;
        Double lng = null;
        Double accuracy = null;

        try {
            if (challengeIdStr != null) challengeId = Long.parseLong(challengeIdStr);
            if (latStr != null) lat = Double.parseDouble(latStr);
            if (lngStr != null) lng = Double.parseDouble(lngStr);
            if (accStr != null) accuracy = Double.parseDouble(accStr);
        } catch (NumberFormatException ignored) {}

        if (challengeId == null) {
            req.setAttribute("errorMessage", "Invalid challenge reference.");
            doGet(req, resp);
            return;
        }

        String photoPath = null;
        if (photoBase64 != null && !photoBase64.trim().isEmpty()) {
            photoPath = saveBase64Photo(photoBase64, req);
        }

        String error = randomCheckInService.submitStudentResponse(student.getId(), challengeId, lat, lng, accuracy, photoPath);

        if (error != null) {
            req.setAttribute("errorMessage", error);
            doGet(req, resp);
        } else {
            session.setAttribute("flashSuccess", "Random Attendance Check-In verified successfully! Presence confirmed.");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    private String saveBase64Photo(String base64Data, HttpServletRequest req) {
        try {
            String clean = base64Data;
            if (clean.contains(",")) {
                clean = clean.split(",")[1];
            }
            byte[] imageBytes = Base64.getDecoder().decode(clean);

            String uploadDir = req.getServletContext().getRealPath("/uploads");
            if (uploadDir == null) {
                uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
            }
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = "random_checkin_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(dir, filename);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(imageBytes);
            }
            return "/uploads/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return "/uploads/default.jpg";
        }
    }
}
