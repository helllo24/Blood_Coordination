package com.blooddonation.controller;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.NotificationDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.Donor;
import com.blooddonation.model.Notification;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;

/**
 * Controller handling user registration for voluntary Donors and Requesters.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final List<String> VALID_BLOOD_GROUPS = Arrays.asList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String roleStr = request.getParameter("role");

        // 1. Validate common required fields
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            redirectWithError(request, response, "Please fill in all required personal information fields.");
            return;
        }

        fullName = fullName.trim();
        email = email.trim().toLowerCase();
        password = password.trim();
        phone = phone.trim();

        // 2. Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectWithError(request, response, "Please provide a valid email address.");
            return;
        }

        // 3. Validate password length and confirmation
        if (password.length() < 6) {
            redirectWithError(request, response, "Password must be at least 6 characters long.");
            return;
        }

        if (confirmPassword != null && !password.equals(confirmPassword.trim())) {
            redirectWithError(request, response, "Passwords do not match. Please re-enter.");
            return;
        }

        // 4. Validate phone number (minimum 10 characters)
        if (phone.length() < 10) {
            redirectWithError(request, response, "Please provide a valid mobile number (minimum 10 digits).");
            return;
        }

        // 5. Determine Role
        Role role;
        try {
            role = Role.valueOf(roleStr != null ? roleStr.toUpperCase() : "DONOR");
            if (role == Role.ADMIN) {
                // Public registration cannot self-grant ADMIN privileges
                role = Role.REQUESTER;
            }
        } catch (IllegalArgumentException e) {
            role = Role.DONOR;
        }

        // 6. Check for duplicate email in database
        if (userDAO.findByEmail(email).isPresent()) {
            redirectWithError(request, response, "This email address is already registered. Please sign in instead.");
            return;
        }

        // 7. Role-specific validation (for DONOR profile)
        String bloodGroup = null;
        int age = 25;
        String gender = "MALE";
        String city = "Bangalore";
        String state = "Karnataka";
        String pincode = "560001";
        String address = "";
        Date lastDonationDate = null;
        boolean isAvailable = true;

        if (role == Role.DONOR) {
            bloodGroup = request.getParameter("bloodGroup");
            String ageStr = request.getParameter("age");
            gender = request.getParameter("gender");
            city = request.getParameter("city");
            state = request.getParameter("state");
            pincode = request.getParameter("pincode");
            address = request.getParameter("address");
            String lastDonationDateStr = request.getParameter("lastDonationDate");
            String isAvailableStr = request.getParameter("isAvailable");

            // Validate Blood Group
            if (bloodGroup == null || !VALID_BLOOD_GROUPS.contains(bloodGroup.trim())) {
                redirectWithError(request, response, "Please select a valid blood group (A+, A-, B+, B-, AB+, AB-, O+, O-).");
                return;
            }
            bloodGroup = bloodGroup.trim();

            // Validate Age
            if (ageStr != null && !ageStr.trim().isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr.trim());
                    if (age < 18 || age > 65) {
                        redirectWithError(request, response, "Donor eligibility requires age to be between 18 and 65 years.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    redirectWithError(request, response, "Invalid age specified.");
                    return;
                }
            }

            // Location validation
            if (city == null || city.trim().isEmpty() || state == null || state.trim().isEmpty()) {
                redirectWithError(request, response, "Please provide your city and state for emergency donor matching.");
                return;
            }
            city = city.trim();
            state = state.trim();
            pincode = pincode != null ? pincode.trim() : "560001";
            address = address != null ? address.trim() : "";
            isAvailable = isAvailableStr != null;

            // Optional last donation date
            if (lastDonationDateStr != null && !lastDonationDateStr.trim().isEmpty()) {
                try {
                    lastDonationDate = Date.valueOf(lastDonationDateStr.trim());
                } catch (IllegalArgumentException ignored) {
                }
            }
        } else {
            // Requester location fields if provided
            String reqCity = request.getParameter("city");
            String reqState = request.getParameter("state");
            String reqPincode = request.getParameter("pincode");
            String reqAddress = request.getParameter("address");

            city = (reqCity != null && !reqCity.trim().isEmpty()) ? reqCity.trim() : "Bangalore";
            state = (reqState != null && !reqState.trim().isEmpty()) ? reqState.trim() : "Karnataka";
            pincode = (reqPincode != null && !reqPincode.trim().isEmpty()) ? reqPincode.trim() : "560001";
            address = (reqAddress != null) ? reqAddress.trim() : "";
        }

        // 8. Hash password and persist user
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(PasswordUtil.hashPassword(password));
        newUser.setPhone(phone);
        newUser.setRole(role);
        newUser.setStatus("ACTIVE");

        Long userId = userDAO.save(newUser);
        if (userId == null) {
            redirectWithError(request, response, "Registration failed due to a database error. Please try again.");
            return;
        }

        // 9. If Donor, persist the donor profile
        if (role == Role.DONOR) {
            Donor donor = new Donor();
            donor.setUserId(userId);
            donor.setBloodGroup(bloodGroup);
            donor.setAge(age);
            donor.setGender(gender != null ? gender : "MALE");
            donor.setCity(city);
            donor.setState(state);
            donor.setPincode(pincode);
            donor.setAddress(address);
            donor.setLastDonationDate(lastDonationDate);
            donor.setAvailable(isAvailable);
            donor.setTotalDonations(0);

            donorDAO.save(donor);

            // Trigger notification for successful donor registration
            Notification welcomeNotif = new Notification(
                userId,
                "Donor Registration Confirmed",
                "Your voluntary donor profile (" + bloodGroup + ") in " + city + " has been successfully registered. Thank you for your commitment to saving lives.",
                "GENERAL"
            );
            notificationDAO.save(welcomeNotif);
        } else {
            // Requester welcome notification
            Notification welcomeNotif = new Notification(
                userId,
                "Requester Account Activated",
                "Welcome to BloodConnect. You can now submit emergency blood requests and connect with verified voluntary donors.",
                "GENERAL"
            );
            notificationDAO.save(welcomeNotif);
        }

        // 10. Redirect to login page with registration success indicator
        response.sendRedirect(request.getContextPath() + "/login.jsp?registered=true&role=" + role.name());
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + encoded);
    }
}
