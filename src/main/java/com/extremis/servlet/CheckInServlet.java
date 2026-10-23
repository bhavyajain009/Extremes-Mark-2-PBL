package com.extremis.servlet;

import com.extremis.model.AttendanceRecord;
import com.extremis.model.Student;
import com.extremis.service.AttendanceService;
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
import java.util.Base64;

@WebServlet("/checkin")
public class CheckInServlet extends HttpServlet {

    private final AttendanceService attendanceService = new AttendanceService();
    private final RandomCheckInService randomCheckInService = new RandomCheckInService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mode", "CHECK_IN");
        req.setAttribute("pageTitle", "Check In");
        req.setAttribute("submitButtonText", "Submit Check In");
        req.setAttribute("activeTab", "home");
        req.getRequestDispatcher("/WEB-INF/views/checkin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Student student = (Student) session.getAttribute("currentStudent");

        String latStr = req.getParameter("latitude");
        String lngStr = req.getParameter("longitude");
        String accStr = req.getParameter("accuracy");
        String photoBase64 = req.getParameter("photoBase64");

        Double lat = null;
        Double lng = null;
        Double accuracy = null;

        try {
            if (latStr != null) lat = Double.parseDouble(latStr);
            if (lngStr != null) lng = Double.parseDouble(lngStr);
            if (accStr != null) accuracy = Double.parseDouble(accStr);
        } catch (NumberFormatException ignored) {
        }

        String photoPath = null;
        if (photoBase64 != null && !photoBase64.trim().isEmpty()) {
            photoPath = saveBase64Photo(photoBase64, req);
        }

        String error = attendanceService.performCheckIn(student.getId(), lat, lng, accuracy, photoPath);

        if (error != null) {
            req.setAttribute("errorMessage", error);
            req.setAttribute("mode", "CHECK_IN");
            req.setAttribute("pageTitle", "Check In");
            req.setAttribute("submitButtonText", "Submit Check In");
            req.getRequestDispatcher("/WEB-INF/views/checkin.jsp").forward(req, resp);
        } else {
            // Schedule 4-5 random location verification events for this student for today
            try {
                AttendanceRecord att = attendanceService.getTodayAttendance(student.getId());
                Long attId = (att != null) ? att.getId() : null;
                randomCheckInService.scheduleStudentDailyVerifications(student.getId(), attendanceService.getEffectiveDate(), attId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            session.setAttribute("flashSuccess", "Check In recorded successfully!");
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

            String filename = "checkin_" + System.currentTimeMillis() + ".jpg";
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
