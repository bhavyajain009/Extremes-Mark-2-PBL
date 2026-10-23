package com.extremis.filter;

import com.extremis.dao.StudentDAO;
import com.extremis.model.Student;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter enforcing the single active session rule.
 * If another device logs in, the session token in the database changes,
 * invalidating any previous session.
 */
@WebFilter("/*")
public class SingleSessionFilter implements Filter {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Skip static resources and auth endpoints
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
            path.startsWith("/icons/") || path.startsWith("/uploads/") ||
            path.equals("/manifest.json") || path.equals("/sw.js") ||
            path.equals("/login") || path.equals("/logout") ||
            path.startsWith("/api/random-checkin/")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Student student = (Student) session.getAttribute("currentStudent");
            String sessionToken = (String) session.getAttribute("sessionToken");

            if (student != null && sessionToken != null) {
                // Fetch fresh student state from database
                Student freshStudent = studentDAO.findById(student.getId());
                if (freshStudent != null && freshStudent.getSessionToken() != null) {
                    if (!sessionToken.equals(freshStudent.getSessionToken())) {
                        // Invalidate old session and redirect with warning
                        session.invalidate();
                        HttpSession newSession = request.getSession(true);
                        newSession.setAttribute("flashError", "You were logged out because your account was opened on another device.");
                        response.sendRedirect(request.getContextPath() + "/login");
                        return;
                    }
                }
            }
        }

        chain.doFilter(req, res);
    }
}
