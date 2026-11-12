package com.blooddonation.controller;

import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Controller handling user authentication, credential validation, and role-based session routing.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String roleStr = request.getParameter("role");

        // 1. Validate required input parameters
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectWithError(request, response, "Please enter both your email address and password.");
            return;
        }

        email = email.trim().toLowerCase();
        password = password.trim();

        // 2. Parse selected role
        Role selectedRole;
        try {
            selectedRole = Role.valueOf(roleStr != null ? roleStr.toUpperCase() : "DONOR");
        } catch (IllegalArgumentException e) {
            selectedRole = Role.DONOR;
        }

        // 3. Look up user by email
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            redirectWithError(request, response, "No registered account found with email: " + email + ". Please register first.");
            return;
        }

        User user = userOpt.get();

        // 4. Verify password hash using PasswordUtil
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            redirectWithError(request, response, "Invalid password. Please check your credentials and try again.");
            return;
        }

        // 5. Verify role match
        if (user.getRole() != selectedRole) {
            redirectWithError(request, response, "Invalid role selected. This account is registered as " + user.getRole().name() + ".");
            return;
        }

        // 6. Verify account status
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            redirectWithError(request, response, "Your account is currently " + user.getStatus() + ". Please contact the administrator.");
            return;
        }

        // 7. Successful Authentication: Initialize Session
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate(); // Prevent session fixation
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("userEmail", user.getEmail());

        // 8. Role-based redirect to respective dashboard
        switch (user.getRole()) {
            case ADMIN:
                response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp");
                break;
            case DONOR:
                response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp");
                break;
            case REQUESTER:
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/index.jsp?loginSuccess=true");
                break;
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + encoded);
    }
}
