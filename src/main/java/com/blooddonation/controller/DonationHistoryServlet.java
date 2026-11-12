package com.blooddonation.controller;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonationDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.NotificationDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
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
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller handling Donation History Management,
 * donor-specific audit logs, administrative records creation,
 * status updates, and date filtering.
 */
@WebServlet(name = "DonationHistoryServlet", urlPatterns = {"/donation-history", "/donations", "/donation/history"})
public class DonationHistoryServlet extends HttpServlet {

    private final DonationDAO donationDAO = new DonationDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private static final List<String> VALID_STATUSES = Arrays.asList("COMPLETED", "SCHEDULED", "CANCELLED");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20donation%20history.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");

        // 1. Registered DONOR View: Isolated to their own donation records only
        if (currentUser.getRole() == Role.DONOR) {
            Optional<Donor> donorOpt = donorDAO.findByUserId(currentUser.getId());
            if (donorOpt.isPresent()) {
                Donor donor = donorOpt.get();
                List<Donation> myDonations = donationDAO.findByDonorId(donor.getId());
                int totalUnits = donationDAO.getTotalUnitsDonatedByDonor(donor.getId());

                request.setAttribute("donationsList", myDonations);
                request.setAttribute("donor", donor);
                request.setAttribute("totalDonations", donor.getTotalDonations());
                request.setAttribute("totalUnits", totalUnits);
                request.setAttribute("livesSaved", totalUnits * 3);
            } else {
                request.setAttribute("donationsList", List.of());
                request.setAttribute("totalDonations", 0);
                request.setAttribute("totalUnits", 0);
                request.setAttribute("livesSaved", 0);
            }
            request.setAttribute("isDonorView", true);
            request.getRequestDispatcher("/donation-history.jsp").forward(request, response);
            return;
        }

        // 2. ADMIN View: System-wide audit log with status and date filtering
        if (currentUser.getRole() == Role.ADMIN) {
            String status = request.getParameter("status");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");

            Date fromDate = null;
            Date toDate = null;

            try {
                if (fromDateStr != null && !fromDateStr.isBlank()) fromDate = Date.valueOf(fromDateStr.trim());
            } catch (IllegalArgumentException ignored) {}

            try {
                if (toDateStr != null && !toDateStr.isBlank()) toDate = Date.valueOf(toDateStr.trim());
            } catch (IllegalArgumentException ignored) {}

            List<Donation> filteredList = donationDAO.filterDonations(status, fromDate, toDate);
            int totalDonations = donationDAO.getTotalDonationsCount();
            int totalUnits = donationDAO.getTotalUnitsDonatedSystemWide();

            request.setAttribute("donationsList", filteredList);
            request.setAttribute("totalDonations", totalDonations);
            request.setAttribute("totalUnits", totalUnits);
            request.setAttribute("livesSaved", totalUnits * 3);
            request.setAttribute("allDonors", donorDAO.findAll());
            request.setAttribute("allRequests", requestDAO.findAll());
            request.setAttribute("selectedStatus", status != null ? status : "");
            request.setAttribute("selectedFromDate", fromDateStr != null ? fromDateStr : "");
            request.setAttribute("selectedToDate", toDateStr != null ? toDateStr : "");
            request.setAttribute("isAdminView", true);

            request.getRequestDispatcher("/donation-history.jsp").forward(request, response);
            return;
        }

        // Requesters do not have donor history
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

        // Role-based security check: Only authorized ADMIN can create or modify official donation records
        if (currentUser.getRole() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only authorized medical administrators can manage official donation records.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "createDonation";
        }

        // 1. Create Official Blood Donation Record
        if ("createDonation".equalsIgnoreCase(action)) {
            handleCreateDonation(request, response);
            return;
        }

        // 2. Update Status and Remarks
        if ("updateStatus".equalsIgnoreCase(action)) {
            handleUpdateStatus(request, response);
            return;
        }

        // 3. Soft Cancel Record
        if ("cancelDonation".equalsIgnoreCase(action)) {
            handleCancelDonation(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/donation-history");
    }

    private void handleCreateDonation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String donorIdStr = request.getParameter("donorId");
        String requestIdStr = request.getParameter("requestId");
        String unitsDonatedStr = request.getParameter("unitsDonated");
        String donationDateStr = request.getParameter("donationDate");
        String hospitalOrCamp = request.getParameter("hospitalOrCamp");
        String status = request.getParameter("status");
        String remarks = request.getParameter("remarks");

        if (donorIdStr == null || donorIdStr.isBlank() ||
            donationDateStr == null || donationDateStr.isBlank() ||
            hospitalOrCamp == null || hospitalOrCamp.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Please%20provide%20all%20required%20donation%20fields.");
            return;
        }

        try {
            Long donorId = Long.parseLong(donorIdStr.trim());
            Optional<Donor> donorOpt = donorDAO.findById(donorId);
            if (donorOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/donation-history?error=Selected%20donor%20does%20not%20exist.");
                return;
            }

            Long requestId = null;
            if (requestIdStr != null && !requestIdStr.isBlank()) {
                try {
                    requestId = Long.parseLong(requestIdStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            int units = 1;
            if (unitsDonatedStr != null && !unitsDonatedStr.isBlank()) {
                units = Integer.parseInt(unitsDonatedStr.trim());
                if (units < 1 || units > 5) {
                    response.sendRedirect(request.getContextPath() + "/donation-history?error=Donation%20units%20must%20be%20between%201%20and%205.");
                    return;
                }
            }

            Date donDate = Date.valueOf(donationDateStr.trim());
            String finalStatus = (status != null && VALID_STATUSES.contains(status.trim().toUpperCase()))
                    ? status.trim().toUpperCase() : "COMPLETED";

            Donation don = new Donation();
            don.setDonorId(donorId);
            don.setRequestId(requestId);
            don.setUnitsDonated(units);
            don.setDonationDate(donDate);
            don.setHospitalOrCamp(hospitalOrCamp.trim());
            don.setStatus(finalStatus);
            don.setRemarks(remarks != null ? remarks.trim() : "Official clinical audit verified.");

            Long savedId = donationDAO.save(don);
            if (savedId != null) {
                // If linked to a request and marked COMPLETED, advance blood request to FULFILLED
                if (requestId != null && "COMPLETED".equalsIgnoreCase(finalStatus)) {
                    requestDAO.updateStatus(requestId, "FULFILLED");
                }

                // 1. Send notification to the voluntary donor
                if ("COMPLETED".equalsIgnoreCase(finalStatus)) {
                    Donor donorEntity = donorOpt.get();
                    Notification donorNotif = new Notification(
                        donorEntity.getUserId(),
                        "Donation Verified and Recorded",
                        "Thank you! Your donation of " + units + " unit(s) at " + hospitalOrCamp.trim() + " on " + donDate +
                            " has been officially recorded in the national clinical audit. Your contribution saves lives.",
                        "GENERAL"
                    );
                    notificationDAO.save(donorNotif);

                    // 2. If tied to a blood requisition, notify the requester
                    if (requestId != null) {
                        Optional<BloodRequest> reqOpt = requestDAO.findById(requestId);
                        if (reqOpt.isPresent()) {
                            BloodRequest bReq = reqOpt.get();
                            Notification reqNotif = new Notification(
                                bReq.getRequesterId(),
                                "Requisition Fulfilled by Donation",
                                "Great news! A verified donation of " + units + " unit(s) for patient " + bReq.getPatientName() +
                                    " was completed at " + hospitalOrCamp.trim() + ".",
                                "STATUS_UPDATE"
                            );
                            notificationDAO.save(reqNotif);
                        }
                    }
                }

                response.sendRedirect(request.getContextPath() + "/donation-history?donationLogged=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/donation-history?error=Failed%20to%20save%20donation%20record.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Invalid%20numerical%20input.");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Invalid%20date%20format%20(YYYY-MM-DD%20required).");
        }
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String donationIdStr = request.getParameter("donationId");
        String newStatus = request.getParameter("newStatus");
        String remarks = request.getParameter("remarks");

        if (donationIdStr == null || newStatus == null || !VALID_STATUSES.contains(newStatus.trim().toUpperCase())) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Invalid%20status%20transition.");
            return;
        }

        try {
            Long donationId = Long.parseLong(donationIdStr.trim());
            boolean updated = donationDAO.updateStatus(donationId, newStatus.trim().toUpperCase(),
                    remarks != null ? remarks.trim() : "Updated by administrator.");

            if (updated) {
                if ("COMPLETED".equalsIgnoreCase(newStatus.trim())) {
                    donationDAO.findById(donationId).ifPresent(d -> {
                        donorDAO.findById(d.getDonorId()).ifPresent(dnr -> {
                            Notification donorNotif = new Notification(
                                dnr.getUserId(),
                                "Donation Marked as Completed",
                                "Your donation record #DON-" + d.getId() + " at " + d.getHospitalOrCamp() + " has been officially verified as COMPLETED in the clinical registry.",
                                "GENERAL"
                            );
                            notificationDAO.save(donorNotif);
                        });
                    });
                }
                response.sendRedirect(request.getContextPath() + "/donation-history?statusUpdated=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/donation-history?error=Failed%20to%20update%20donation%20status.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Invalid%20donation%20identifier.");
        }
    }

    private void handleCancelDonation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String donationIdStr = request.getParameter("donationId");
        if (donationIdStr == null || donationIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Missing%20donation%20ID.");
            return;
        }

        try {
            Long donationId = Long.parseLong(donationIdStr.trim());
            boolean cancelled = donationDAO.cancelDonation(donationId);
            if (cancelled) {
                response.sendRedirect(request.getContextPath() + "/donation-history?statusUpdated=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/donation-history?error=Failed%20to%20cancel%20donation.");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/donation-history?error=Invalid%20donation%20identifier.");
        }
    }
}
