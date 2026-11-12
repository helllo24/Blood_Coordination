package com.blooddonation.controller;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.Donor;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.util.Optional;

/**
 * Controller handling Donor Profile Management, CRUD operations,
 * availability toggling, and soft deactivation.
 */
@WebServlet(name = "DonorProfileServlet", urlPatterns = {"/donor-profile", "/donor/profile"})
public class DonorProfileServlet extends HttpServlet {

    private final DonorDAO donorDAO = new DonorDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20your%20donor%20profile.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser.getRole() != Role.DONOR && currentUser.getRole() != Role.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Access%20denied.%20Donor%20privileges%20required.");
            return;
        }

        // Support admin viewing specific donor by id, or default to current user's profile
        String donorIdParam = request.getParameter("donorId");
        Optional<Donor> donorOpt = Optional.empty();

        if (donorIdParam != null && !donorIdParam.isBlank() && currentUser.getRole() == Role.ADMIN) {
            try {
                Long dId = Long.parseLong(donorIdParam);
                donorOpt = donorDAO.findById(dId);
            } catch (NumberFormatException ignored) {}
        }

        if (donorOpt.isEmpty()) {
            donorOpt = donorDAO.findByUserId(currentUser.getId());
        }

        if (donorOpt.isPresent()) {
            Donor donor = donorOpt.get();
            request.setAttribute("donor", donor);
            // Fetch account user if viewing someone else as admin, or use currentUser
            if (currentUser.getId().equals(donor.getUserId())) {
                request.setAttribute("accountUser", currentUser);
            } else {
                userDAO.findById(donor.getUserId()).ifPresent(u -> request.setAttribute("accountUser", u));
            }
        } else {
            // Profile does not exist yet (or admin fallback)
            Donor emptyDonor = new Donor();
            emptyDonor.setUserId(currentUser.getId());
            emptyDonor.setBloodGroup("O+");
            emptyDonor.setAvailable(true);
            request.setAttribute("donor", emptyDonor);
            request.setAttribute("accountUser", currentUser);
        }

        request.getRequestDispatcher("/donor-profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Session%20expired.%20Please%20log%20in%20again.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser.getRole() != Role.DONOR && currentUser.getRole() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Insufficient permissions.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "updateProfile";
        }

        // 1. Availability Quick Toggle
        if ("toggleAvailability".equalsIgnoreCase(action)) {
            handleToggleAvailability(request, response, currentUser);
            return;
        }

        // 2. Soft Deactivation
        if ("deactivate".equalsIgnoreCase(action)) {
            handleDeactivateDonor(request, response, currentUser);
            return;
        }

        // 3. Profile Update
        if ("updateProfile".equalsIgnoreCase(action)) {
            handleUpdateProfile(request, response, currentUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/donor-profile.jsp");
    }

    private void handleToggleAvailability(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String donorIdStr = request.getParameter("donorId");
        String currentStatusStr = request.getParameter("currentStatus");

        if (donorIdStr == null || donorIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Invalid%20donor%20identifier.");
            return;
        }

        try {
            Long donorId = Long.parseLong(donorIdStr);
            Optional<Donor> donorOpt = donorDAO.findById(donorId);
            if (donorOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Donor%20profile%20not%20found.");
                return;
            }

            Donor donor = donorOpt.get();
            // Ownership validation: verify user is owner or admin
            if (!currentUser.getId().equals(donor.getUserId()) && currentUser.getRole() != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized action on donor profile.");
                return;
            }

            boolean currentStatus = Boolean.parseBoolean(currentStatusStr);
            boolean newStatus = !currentStatus;
            donorDAO.updateAvailability(donorId, newStatus);

            String redirectTarget = request.getParameter("fromProfile") != null ? "/donor-profile.jsp?statusUpdated=true" : "/donor-dashboard.jsp?statusUpdated=true";
            response.sendRedirect(request.getContextPath() + redirectTarget);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }

    private void handleDeactivateDonor(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String donorIdStr = request.getParameter("donorId");
        if (donorIdStr == null || donorIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Invalid%20donor%20identifier.");
            return;
        }

        try {
            Long donorId = Long.parseLong(donorIdStr);
            Optional<Donor> donorOpt = donorDAO.findById(donorId);
            if (donorOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Donor%20profile%20not%20found.");
                return;
            }

            Donor donor = donorOpt.get();
            // Ownership validation
            if (!currentUser.getId().equals(donor.getUserId()) && currentUser.getRole() != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized action on donor profile.");
                return;
            }

            donorDAO.deactivateDonor(donorId);
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?deactivated=true");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Invalid%20request%20format.");
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String donorIdStr = request.getParameter("donorId");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String bloodGroup = request.getParameter("bloodGroup");
        String ageStr = request.getParameter("age");
        String gender = request.getParameter("gender");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String pincode = request.getParameter("pincode");
        String address = request.getParameter("address");
        String lastDonationDateStr = request.getParameter("lastDonationDate");
        String isAvailableStr = request.getParameter("isAvailable");

        if (donorIdStr == null || donorIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Missing%20donor%20record%20ID.");
            return;
        }

        try {
            Long donorId = Long.parseLong(donorIdStr);
            Optional<Donor> donorOpt = donorDAO.findById(donorId);
            if (donorOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Donor%20record%20not%20found.");
                return;
            }

            Donor donor = donorOpt.get();

            // Ownership validation: Ensure requester owns this profile or is ADMIN
            if (!currentUser.getId().equals(donor.getUserId()) && currentUser.getRole() != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized to modify another donor's profile.");
                return;
            }

            // 1. Update personal details in users table
            if (fullName != null && !fullName.trim().isEmpty() && phone != null && !phone.trim().isEmpty()) {
                userDAO.updateProfile(donor.getUserId(), fullName.trim(), phone.trim());
                if (currentUser.getId().equals(donor.getUserId())) {
                    currentUser.setFullName(fullName.trim());
                    currentUser.setPhone(phone.trim());
                    request.getSession().setAttribute("userName", fullName.trim());
                    request.getSession().setAttribute("currentUser", currentUser);
                }
            }

            // 2. Update donor medical & location details
            if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
                donor.setBloodGroup(bloodGroup.trim());
            }
            if (ageStr != null && !ageStr.trim().isEmpty()) {
                donor.setAge(Integer.parseInt(ageStr.trim()));
            }
            if (gender != null && !gender.trim().isEmpty()) {
                donor.setGender(gender.trim());
            }
            if (city != null && !city.trim().isEmpty()) {
                donor.setCity(city.trim());
            }
            if (state != null && !state.trim().isEmpty()) {
                donor.setState(state.trim());
            }
            if (pincode != null && !pincode.trim().isEmpty()) {
                donor.setPincode(pincode.trim());
            }
            if (address != null && !address.trim().isEmpty()) {
                donor.setAddress(address.trim());
            }
            if (lastDonationDateStr != null && !lastDonationDateStr.trim().isEmpty()) {
                donor.setLastDonationDate(Date.valueOf(lastDonationDateStr.trim()));
            }
            if (isAvailableStr != null) {
                donor.setAvailable("true".equalsIgnoreCase(isAvailableStr) || "1".equals(isAvailableStr) || "on".equalsIgnoreCase(isAvailableStr));
            }

            boolean updated = donorDAO.update(donor);
            if (updated) {
                response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?updated=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Database%20update%20failed.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Invalid%20numerical%20input%20provided.");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=Invalid%20date%20format%20provided.");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/donor-profile.jsp?error=An%20unexpected%20error%20occurred.");
        }
    }
}
