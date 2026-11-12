package com.blooddonation.controller;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.NotificationDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import com.blooddonation.model.Notification;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller handling the complete Blood Request Management Module:
 * - Create blood requests (REQUESTER role)
 * - View request details with requester data privacy
 * - Update pending requests (requester ownership enforced)
 * - Soft cancel pending requests
 * - Multi-criteria search and filter (ADMIN & Requesters)
 * - Administrative status updates (ADMIN only: PENDING, MATCHED, FULFILLED, CANCELLED)
 */
@WebServlet(name = "BloodRequestServlet", urlPatterns = {"/blood-request", "/blood-requests"})
public class BloodRequestServlet extends HttpServlet {

    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private static final List<String> VALID_BLOOD_GROUPS =
            Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private static final List<String> VALID_URGENCY_LEVELS =
            Arrays.asList("NORMAL", "URGENT", "CRITICAL");
    private static final List<String> VALID_STATUSES =
            Arrays.asList("PENDING", "MATCHED", "FULFILLED", "CANCELLED");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20access%20blood%20requests.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        String action = request.getParameter("action");

        // 1. Edit mode: Load request for editing if pending and owned by user
        if ("edit".equalsIgnoreCase(action)) {
            handleEditRequestGet(request, response, currentUser);
            return;
        }

        // 2. View mode: View individual request details with privacy access control
        if ("view".equalsIgnoreCase(action)) {
            handleViewRequestGet(request, response, currentUser);
            return;
        }

        // 3. Filter mode: Multi-criteria filtering for Admin dashboard
        if ("filter".equalsIgnoreCase(action) && currentUser.getRole() == Role.ADMIN) {
            handleFilterRequestsGet(request, response);
            return;
        }

        // Default GET: Forward to blood request creation form
        request.getRequestDispatcher("/blood-request.jsp").forward(request, response);
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
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "createRequest";
        }

        // 1. Create Blood Request
        if ("createRequest".equalsIgnoreCase(action)) {
            handleCreateRequest(request, response, currentUser);
            return;
        }

        // 2. Update Pending Blood Request
        if ("updateRequest".equalsIgnoreCase(action)) {
            handleUpdateRequest(request, response, currentUser);
            return;
        }

        // 3. Soft Cancel Pending Blood Request
        if ("cancelRequest".equalsIgnoreCase(action)) {
            handleCancelRequest(request, response, currentUser);
            return;
        }

        // 4. Admin Status Update
        if ("updateStatus".equalsIgnoreCase(action)) {
            handleAdminUpdateStatus(request, response, currentUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp");
    }

    private void handleEditRequestGet(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String requestIdStr = request.getParameter("id");
        if (requestIdStr == null || requestIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Invalid%20request%20ID.");
            return;
        }

        try {
            Long reqId = Long.parseLong(requestIdStr);
            Optional<BloodRequest> reqOpt = requestDAO.findById(reqId);
            if (reqOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Blood%20request%20not%20found.");
                return;
            }

            BloodRequest req = reqOpt.get();

            // Authorization: Requester can only edit their own request unless Admin
            if (!currentUser.getId().equals(req.getRequesterId()) && currentUser.getRole() != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized: You cannot edit another requester's requisition.");
                return;
            }

            // Status check: Prevent editing non-pending requests
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Cannot%20modify%20a%20request%20that%20is%20already%20" + req.getStatus() + ".");
                return;
            }

            request.setAttribute("editRequest", req);
            request.getRequestDispatcher("/blood-request.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }

    private void handleViewRequestGet(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String requestIdStr = request.getParameter("id");
        if (requestIdStr == null || requestIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Invalid%20request%20ID.");
            return;
        }

        try {
            Long reqId = Long.parseLong(requestIdStr);
            Optional<BloodRequest> reqOpt = requestDAO.findById(reqId);
            if (reqOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Request%20not%20found.");
                return;
            }

            BloodRequest req = reqOpt.get();

            // Privacy check: Non-admin requesters can only view their own requests
            if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DONOR && !currentUser.getId().equals(req.getRequesterId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Private requisition details.");
                return;
            }

            request.setAttribute("viewRequest", req);
            request.getRequestDispatcher("/blood-request.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }

    private void handleFilterRequestsGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bloodGroup = request.getParameter("bloodGroup");
        String urgency = request.getParameter("urgency");
        String status = request.getParameter("status");
        String city = request.getParameter("city");

        List<BloodRequest> filtered = requestDAO.searchAndFilter(bloodGroup, urgency, status, city);
        request.setAttribute("filteredRequests", filtered);
        request.setAttribute("selectedBg", bloodGroup != null ? bloodGroup : "");
        request.setAttribute("selectedUrgency", urgency != null ? urgency : "");
        request.setAttribute("selectedStatus", status != null ? status : "");
        request.setAttribute("selectedCity", city != null ? city : "");

        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }

    private void handleCreateRequest(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        // Role check: Only REQUESTER or ADMIN can create requests
        if (currentUser.getRole() != Role.REQUESTER && currentUser.getRole() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only registered Requesters can submit blood requisitions.");
            return;
        }

        String patientName = request.getParameter("patientName");
        String bloodGroup = request.getParameter("bloodGroup");
        String unitsNeededStr = request.getParameter("unitsNeeded");
        String hospitalName = request.getParameter("hospitalName");
        String hospitalCity = request.getParameter("hospitalCity");
        String hospitalAddress = request.getParameter("hospitalAddress");
        String contactPerson = request.getParameter("contactPerson");
        String contactPhone = request.getParameter("contactPhone");
        String urgencyLevel = request.getParameter("urgencyLevel");
        String requiredDateStr = request.getParameter("requiredDate");
        String additionalNotes = request.getParameter("additionalNotes");

        // Server-side validation
        String validationError = validateRequestFields(patientName, bloodGroup, unitsNeededStr, hospitalName,
                hospitalCity, contactPerson, contactPhone, urgencyLevel, requiredDateStr);

        if (validationError != null) {
            String encoded = URLEncoder.encode(validationError, StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/blood-request.jsp?error=" + encoded);
            return;
        }

        int units = Integer.parseInt(unitsNeededStr.trim());
        Date reqDate = Date.valueOf(requiredDateStr.trim());

        BloodRequest newReq = new BloodRequest();
        newReq.setRequesterId(currentUser.getId()); // Automatically link to logged-in requester
        newReq.setPatientName(patientName.trim());
        newReq.setBloodGroup(bloodGroup.trim());
        newReq.setUnitsNeeded(units);
        newReq.setHospitalName(hospitalName.trim());
        newReq.setHospitalCity(hospitalCity.trim());
        newReq.setHospitalAddress(hospitalAddress != null ? hospitalAddress.trim() : "");
        newReq.setContactPerson(contactPerson.trim());
        newReq.setContactPhone(contactPhone.trim());
        newReq.setUrgencyLevel(urgencyLevel.trim());
        newReq.setStatus("PENDING");
        newReq.setRequiredDate(reqDate);
        newReq.setAdditionalNotes(additionalNotes != null ? additionalNotes.trim() : "");

        Long createdId = requestDAO.save(newReq);
        if (createdId != null) {
            newReq.setId(createdId);

            // 1. Notify the submitting requester
            Notification reqNotif = new Notification(
                currentUser.getId(),
                "Requisition Submitted",
                "Your blood request for " + newReq.getPatientName() + " (" + newReq.getBloodGroup() + ", " + newReq.getUnitsNeeded() + " unit(s)) at " + newReq.getHospitalName() + " is active. Compatible donors are being matched.",
                "EMERGENCY_REQUEST"
            );
            notificationDAO.save(reqNotif);

            // 2. Broadcast emergency alert to compatible registered donors in the hospital's city
            try {
                List<Donor> compatibleDonors = donorDAO.findCompatibleDonors(newReq.getBloodGroup(), newReq.getHospitalCity());
                for (Donor d : compatibleDonors) {
                    if (d.getUserId() != null && !d.getUserId().equals(currentUser.getId())) {
                        Notification donorAlert = new Notification(
                            d.getUserId(),
                            "Emergency Blood Need in " + newReq.getHospitalCity(),
                            "Urgent requisition: " + newReq.getUnitsNeeded() + " unit(s) of " + newReq.getBloodGroup() + " needed for " + newReq.getPatientName() + " at " + newReq.getHospitalName() + " by " + newReq.getRequiredDate() + ".",
                            "EMERGENCY_REQUEST"
                        );
                        notificationDAO.save(donorAlert);
                    }
                }
            } catch (Exception e) {
                System.err.println("[BloodRequestServlet] Warning: Donor notification broadcast failed: " + e.getMessage());
            }

            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?requestCreated=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/blood-request.jsp?error=Database%20error%20saving%20blood%20request.");
        }
    }

    private void handleUpdateRequest(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String requestIdStr = request.getParameter("requestId");
        if (requestIdStr == null || requestIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Missing%20request%20ID.");
            return;
        }

        try {
            Long reqId = Long.parseLong(requestIdStr);
            Optional<BloodRequest> reqOpt = requestDAO.findById(reqId);
            if (reqOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Request%20not%20found.");
                return;
            }

            BloodRequest existingReq = reqOpt.get();

            // Authorization: Requester can only update their own request unless Admin
            if (!currentUser.getId().equals(existingReq.getRequesterId()) && currentUser.getRole() != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized: You cannot modify another requester's requisition.");
                return;
            }

            // Status check: Prevent modifying completed, fulfilled, or cancelled requests
            if (!"PENDING".equalsIgnoreCase(existingReq.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Cannot%20modify%20requests%20with%20status%20" + existingReq.getStatus() + ".");
                return;
            }

            String patientName = request.getParameter("patientName");
            String bloodGroup = request.getParameter("bloodGroup");
            String unitsNeededStr = request.getParameter("unitsNeeded");
            String hospitalName = request.getParameter("hospitalName");
            String hospitalCity = request.getParameter("hospitalCity");
            String hospitalAddress = request.getParameter("hospitalAddress");
            String contactPerson = request.getParameter("contactPerson");
            String contactPhone = request.getParameter("contactPhone");
            String urgencyLevel = request.getParameter("urgencyLevel");
            String requiredDateStr = request.getParameter("requiredDate");
            String additionalNotes = request.getParameter("additionalNotes");

            // Server-side validation
            String validationError = validateRequestFields(patientName, bloodGroup, unitsNeededStr, hospitalName,
                    hospitalCity, contactPerson, contactPhone, urgencyLevel, requiredDateStr);

            if (validationError != null) {
                String encoded = URLEncoder.encode(validationError, StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/blood-request?action=edit&id=" + reqId + "&error=" + encoded);
                return;
            }

            existingReq.setPatientName(patientName.trim());
            existingReq.setBloodGroup(bloodGroup.trim());
            existingReq.setUnitsNeeded(Integer.parseInt(unitsNeededStr.trim()));
            existingReq.setHospitalName(hospitalName.trim());
            existingReq.setHospitalCity(hospitalCity.trim());
            existingReq.setHospitalAddress(hospitalAddress != null ? hospitalAddress.trim() : "");
            existingReq.setContactPerson(contactPerson.trim());
            existingReq.setContactPhone(contactPhone.trim());
            existingReq.setUrgencyLevel(urgencyLevel.trim());
            existingReq.setRequiredDate(Date.valueOf(requiredDateStr.trim()));
            existingReq.setAdditionalNotes(additionalNotes != null ? additionalNotes.trim() : "");

            boolean updated = requestDAO.update(existingReq);
            if (updated) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?requestUpdated=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Failed%20to%20update%20request.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }

    private void handleCancelRequest(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String requestIdStr = request.getParameter("requestId");
        if (requestIdStr == null || requestIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Missing%20request%20ID.");
            return;
        }

        try {
            Long reqId = Long.parseLong(requestIdStr);
            Optional<BloodRequest> reqOpt = requestDAO.findById(reqId);
            if (reqOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Request%20not%20found.");
                return;
            }

            BloodRequest req = reqOpt.get();

            // Authorization check
            if (!currentUser.getId().equals(req.getRequesterId()) && currentUser.getRole() != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized: You cannot cancel another requester's requisition.");
                return;
            }

            // Status check: Only pending requests can be cancelled
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Only%20pending%20requests%20can%20be%20cancelled.");
                return;
            }

            boolean cancelled = requestDAO.cancelPendingRequest(reqId);
            if (cancelled) {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?requestCancelled=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Failed%20to%20cancel%20request.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }

    private void handleAdminUpdateStatus(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        // Role check: ONLY ADMIN
        if (currentUser.getRole() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only system administrators can update requisition statuses.");
            return;
        }

        String requestIdStr = request.getParameter("requestId");
        String newStatus = request.getParameter("newStatus");

        if (requestIdStr == null || newStatus == null || !VALID_STATUSES.contains(newStatus.toUpperCase())) {
            response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp?error=Invalid%20status%20transition.");
            return;
        }

        try {
            Long reqId = Long.parseLong(requestIdStr);
            Optional<BloodRequest> existingOpt = requestDAO.findById(reqId);

            boolean updated = requestDAO.updateStatus(reqId, newStatus.toUpperCase());
            if (updated) {
                // Notify the requisition owner regarding administrative status change
                if (existingOpt.isPresent()) {
                    BloodRequest bReq = existingOpt.get();
                    Notification statusNotif = new Notification(
                        bReq.getRequesterId(),
                        "Requisition Status Updated",
                        "Your blood requisition #REQ-" + bReq.getId() + " for patient " + bReq.getPatientName() + " has been updated to status: " + newStatus.toUpperCase() + ".",
                        "STATUS_UPDATE"
                    );
                    notificationDAO.save(statusNotif);
                }

                response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp?statusUpdated=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp?error=Failed%20to%20update%20status.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }

    private String validateRequestFields(String patientName, String bloodGroup, String unitsNeededStr,
                                        String hospitalName, String hospitalCity, String contactPerson,
                                        String contactPhone, String urgencyLevel, String requiredDateStr) {
        if (patientName == null || patientName.trim().isEmpty() || patientName.trim().length() > 150) {
            return "Please enter a valid Patient Name (max 150 characters).";
        }
        if (bloodGroup == null || !VALID_BLOOD_GROUPS.contains(bloodGroup.trim())) {
            return "Please select a valid Blood Group (e.g. O+, A+, B+, AB+).";
        }
        if (unitsNeededStr == null || unitsNeededStr.trim().isEmpty()) {
            return "Please specify the number of blood units required.";
        }
        try {
            int units = Integer.parseInt(unitsNeededStr.trim());
            if (units < 1 || units > 20) {
                return "Units required must be between 1 and 20 bottles.";
            }
        } catch (NumberFormatException e) {
            return "Units required must be a valid number.";
        }
        if (hospitalName == null || hospitalName.trim().isEmpty() || hospitalName.trim().length() > 255) {
            return "Please enter a valid Hospital Name.";
        }
        if (hospitalCity == null || hospitalCity.trim().isEmpty() || hospitalCity.trim().length() > 100) {
            return "Please enter the Hospital City.";
        }
        if (contactPerson == null || contactPerson.trim().isEmpty() || contactPerson.trim().length() > 150) {
            return "Please enter an Emergency Contact Person name.";
        }
        if (contactPhone == null || contactPhone.trim().isEmpty() || contactPhone.trim().length() < 7 || contactPhone.trim().length() > 20) {
            return "Please provide a valid Contact Phone number (7 - 20 digits).";
        }
        if (urgencyLevel == null || !VALID_URGENCY_LEVELS.contains(urgencyLevel.trim())) {
            return "Please select a valid Urgency Priority (NORMAL, URGENT, CRITICAL).";
        }
        if (requiredDateStr == null || requiredDateStr.trim().isEmpty()) {
            return "Please select a Required By Date.";
        }
        try {
            Date.valueOf(requiredDateStr.trim());
        } catch (IllegalArgumentException e) {
            return "Please enter a valid date in YYYY-MM-DD format.";
        }
        return null;
    }
}
