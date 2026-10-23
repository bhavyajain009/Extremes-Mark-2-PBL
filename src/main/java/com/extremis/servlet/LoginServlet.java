package com.extremis.servlet;

import com.extremis.dao.AdminDAO;
import com.extremis.dao.DBConnection;
import com.extremis.dao.FacultyDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.model.Admin;
import com.extremis.model.Faculty;
import com.extremis.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            if (session.getAttribute("currentStudent") != null) {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            } else if (session.getAttribute("currentFaculty") != null) {
                resp.sendRedirect(req.getContextPath() + "/faculty/dashboard");
                return;
            } else if (session.getAttribute("currentAdmin") != null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = req.getParameter("role");
        String rollNo = req.getParameter("rollNo");
        String password = req.getParameter("password");

        if (role == null || role.trim().isEmpty()) {
            role = "student";
        }

        if (rollNo == null || rollNo.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("errorMessage", "Identifier and Password are required.");
            req.setAttribute("selectedRole", role);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        String inputHash = DBConnection.hashPassword(password);

        // 1. STUDENT LOGIN
        if ("student".equalsIgnoreCase(role)) {
            Student student = studentDAO.findByRollNo(rollNo.trim());

            if (student == null || !inputHash.equals(student.getPasswordHash())) {
                req.setAttribute("errorMessage", "Invalid student credentials. Please verify your Student ID and Password.");
                req.setAttribute("rollNo", rollNo);
                req.setAttribute("selectedRole", "student");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            // Requirement 2: "If any id is logged in 1 device then it can't be log in to another device."
            HttpSession currentSession = req.getSession(false);
            String existingToken = student.getSessionToken();
            if (existingToken != null && !existingToken.trim().isEmpty()) {
                String currentSessionToken = (currentSession != null) ? (String) currentSession.getAttribute("sessionToken") : null;
                // If attempting from a different session / device, block login!
                if (currentSessionToken == null || !currentSessionToken.equals(existingToken)) {
                    req.setAttribute("errorMessage", "This account is already logged in on another device. Multi-device login is restricted. Please log out from that device first or contact Admin to reset your device session.");
                    req.setAttribute("rollNo", rollNo);
                    req.setAttribute("selectedRole", "student");
                    req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                    return;
                }
            }

            // Fresh device or authorized device session
            String newSessionToken = UUID.randomUUID().toString();
            studentDAO.updateSessionToken(student.getId(), newSessionToken);
            student.setSessionToken(newSessionToken);

            HttpSession session = req.getSession(true);
            session.setAttribute("currentStudent", student);
            session.setAttribute("sessionToken", newSessionToken);
            session.setAttribute("role", "student");

            if (!student.isPermissionsAccepted()) {
                resp.sendRedirect(req.getContextPath() + "/permissions");
            } else {
                String redirect = req.getParameter("redirect");
                if (redirect != null && redirect.startsWith("/")) {
                    resp.sendRedirect(req.getContextPath() + redirect);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/dashboard");
                }
            }
            return;
        }

        // 2. FACULTY LOGIN
        if ("faculty".equalsIgnoreCase(role)) {
            Faculty faculty = facultyDAO.findByFacultyId(rollNo.trim());

            if (faculty == null || !inputHash.equals(faculty.getPasswordHash())) {
                req.setAttribute("errorMessage", "Invalid Faculty ID or Password.");
                req.setAttribute("rollNo", rollNo);
                req.setAttribute("selectedRole", "faculty");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            if (!faculty.isActive()) {
                req.setAttribute("errorMessage", "Your faculty account has been deactivated. Please contact the administrator.");
                req.setAttribute("rollNo", rollNo);
                req.setAttribute("selectedRole", "faculty");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            String newSessionToken = UUID.randomUUID().toString();
            facultyDAO.updateSessionToken(faculty.getId(), newSessionToken);
            faculty.setSessionToken(newSessionToken);

            HttpSession session = req.getSession(true);
            session.setAttribute("currentFaculty", faculty);
            session.setAttribute("sessionToken", newSessionToken);
            session.setAttribute("role", "faculty");

            resp.sendRedirect(req.getContextPath() + "/faculty/dashboard");
            return;
        }

        // 3. ADMIN LOGIN
        if ("admin".equalsIgnoreCase(role)) {
            Admin admin = adminDAO.findByUsername(rollNo.trim());

            if (admin == null || !inputHash.equals(admin.getPasswordHash())) {
                req.setAttribute("errorMessage", "Invalid Administrator Username or Password.");
                req.setAttribute("rollNo", rollNo);
                req.setAttribute("selectedRole", "admin");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            String newSessionToken = UUID.randomUUID().toString();
            adminDAO.updateSessionToken(admin.getId(), newSessionToken);
            admin.setSessionToken(newSessionToken);

            HttpSession session = req.getSession(true);
            session.setAttribute("currentAdmin", admin);
            session.setAttribute("sessionToken", newSessionToken);
            session.setAttribute("role", "admin");

            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        // Unknown role
        req.setAttribute("errorMessage", "Unknown login role selected.");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }
}
