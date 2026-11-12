package com.blooddonation.controller;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonationDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Controller generating administrative reports and analytics:
 * 1. Donors by Blood Group distribution
 * 2. Blood Requests by Status
 * 3. Monthly donation volume and units collected
 * 4. Total completed donations
 * 5. Total units donated and lives saved
 */
@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports", "/reports"})
public class AdminReportServlet extends HttpServlet {

    private final DonorDAO donorDAO = new DonorDAO();
    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final DonationDAO donationDAO = new DonationDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20administrative%20reports.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser.getRole() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Administrative reports are restricted to platform administrators.");
            return;
        }

        // 1. Donors by Blood Group
        Map<String, Integer> donorsByBloodGroup = donorDAO.getDonorsCountByBloodGroup();

        // 2. Blood Requests by Status
        Map<String, Integer> requestsByStatus = requestDAO.getRequestsCountByStatus();

        // 3. Donations by Month
        Map<String, int[]> donationsByMonth = donationDAO.getDonationsByMonth();

        // 4. Aggregate metrics
        int totalCompletedDonations = donationDAO.getTotalDonationsCount();
        int totalUnitsDonated = donationDAO.getTotalUnitsDonatedSystemWide();
        int totalDonors = donorDAO.getTotalDonorsCount();
        int totalRequests = requestDAO.getTotalRequestsCount();
        int totalRequesters = userDAO.countByRole(Role.REQUESTER);
        int livesSaved = totalUnitsDonated * 3;

        request.setAttribute("donorsByBloodGroup", donorsByBloodGroup);
        request.setAttribute("requestsByStatus", requestsByStatus);
        request.setAttribute("donationsByMonth", donationsByMonth);
        request.setAttribute("totalCompletedDonations", totalCompletedDonations);
        request.setAttribute("totalUnitsDonated", totalUnitsDonated);
        request.setAttribute("totalDonors", totalDonors);
        request.setAttribute("totalRequests", totalRequests);
        request.setAttribute("totalRequesters", totalRequesters);
        request.setAttribute("livesSaved", livesSaved);

        request.getRequestDispatcher("/admin-reports.jsp").forward(request, response);
    }
}
