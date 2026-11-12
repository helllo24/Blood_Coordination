package com.blooddonation.controller;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.DonorResponseDAO;
import com.blooddonation.dao.NotificationDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import com.blooddonation.model.DonorResponse;
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
import java.util.List;
import java.util.Optional;

/**
 * Controller handling Donor Responses to Emergency Blood Requests:
 * - Accept a blood request (with optional message)
 * - Reject a blood request
 * - Duplicate response prevention
 * - Role-based authorization
 * - Retrieval of response history for Donors, Requesters, and Admin
 */
@WebServlet(name = "DonorResponseServlet", urlPatterns = {"/donor-response", "/donor/response"})
public class DonorResponseServlet extends HttpServlet {

    private final DonorResponseDAO responseDAO = new DonorResponseDAO();
    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20responses.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.ADMIN) {
            List<DonorResponse> allResponses = responseDAO.findAll();
            request.setAttribute("allResponses", allResponses);
            request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
            return;
        }

        if (currentUser.getRole() == Role.DONOR) {
            Optional<Donor> donorOpt = donorDAO.findByUserId(currentUser.getId());
            if (donorOpt.isPresent()) {
                List<DonorResponse> myResponses = responseDAO.findByDonorId(donorOpt.get().getId());
                request.setAttribute("myResponses", myResponses);
            }
            request.getRequestDispatcher("/donor-dashboard.jsp").forward(request, response);
            return;
        }

        // REQUESTER role
        response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp");
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

        // Role check: Only registered DONOR (or ADMIN) can respond to emergency blood requests
        if (currentUser.getRole() != Role.DONOR && currentUser.getRole() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only registered voluntary donors can submit responses.");
            return;
        }

        String requestIdStr = request.getParameter("requestId");
        String action = request.getParameter("action");
        String status = request.getParameter("status");
        String message = request.getParameter("message");

        if (requestIdStr == null || requestIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Missing%20request%20identifier.");
            return;
        }

        try {
            Long requestId = Long.parseLong(requestIdStr);
            Optional<BloodRequest> requestOpt = requestDAO.findById(requestId);
            if (requestOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Target%20blood%20request%20not%20found.");
                return;
            }

            BloodRequest bloodRequest = requestOpt.get();

            // Resolve authenticated donor profile
            Optional<Donor> donorOpt = donorDAO.findByUserId(currentUser.getId());
            Long donorId;
            if (donorOpt.isPresent()) {
                donorId = donorOpt.get().getId();
            } else {
                // Fallback for Admin testing if explicit donorId is supplied
                String fallbackDonorIdStr = request.getParameter("donorId");
                if (currentUser.getRole() == Role.ADMIN && fallbackDonorIdStr != null && !fallbackDonorIdStr.isBlank()) {
                    donorId = Long.parseLong(fallbackDonorIdStr);
                } else {
                    response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=No%20donor%20profile%20associated%20with%20this%20account.");
                    return;
                }
            }

            // Duplicate prevention check
            if (responseDAO.hasResponded(requestId, donorId)) {
                response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=You%20have%20already%20submitted%20a%20response%20for%20this%20request.");
                return;
            }

            // Determine status (ACCEPTED or REJECTED)
            String finalStatus = "ACCEPTED";
            if ("reject".equalsIgnoreCase(action) || "REJECTED".equalsIgnoreCase(status)) {
                finalStatus = "REJECTED";
            }

            DonorResponse resp = new DonorResponse();
            resp.setRequestId(requestId);
            resp.setDonorId(donorId);
            resp.setStatus(finalStatus);
            if (message != null && !message.trim().isEmpty()) {
                resp.setMessage(message.trim());
            } else {
                resp.setMessage("ACCEPTED".equals(finalStatus) ? "Available to donate blood for this requirement." : "Unavailable to donate at this time.");
            }

            Long savedId = responseDAO.save(resp);
            if (savedId != null) {
                // Advance request status to MATCHED if accepted and was PENDING
                if ("ACCEPTED".equals(finalStatus) && "PENDING".equalsIgnoreCase(bloodRequest.getStatus())) {
                    requestDAO.updateStatus(requestId, "MATCHED");
                }

                // Notify the blood request owner of the donor response
                if ("ACCEPTED".equals(finalStatus)) {
                    String dName = donorOpt.map(Donor::getFullName).orElse("A voluntary donor");
                    String dPhone = donorOpt.map(Donor::getPhone).orElse("Available on file");
                    String dGroup = donorOpt.map(Donor::getBloodGroup).orElse(bloodRequest.getBloodGroup());

                    Notification acceptNotif = new Notification(
                        bloodRequest.getRequesterId(),
                        "Donor Accepted Blood Request",
                        "Donor " + dName + " (" + dGroup + ", Contact: " + dPhone + ") has accepted your blood request for " +
                            bloodRequest.getPatientName() + ". Message: " + resp.getMessage(),
                        "DONOR_RESPONSE"
                    );
                    notificationDAO.save(acceptNotif);

                    response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?responseAccepted=true");
                } else {
                    Notification rejectNotif = new Notification(
                        bloodRequest.getRequesterId(),
                        "Donor Response Update",
                        "A voluntary donor was unable to fulfill your blood request for " + bloodRequest.getPatientName() + " at this time.",
                        "DONOR_RESPONSE"
                    );
                    notificationDAO.save(rejectNotif);

                    response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?responseRejected=true");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Failed%20to%20record%20response.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donor-dashboard.jsp?error=Invalid%20request%20format.");
        }
    }
}
