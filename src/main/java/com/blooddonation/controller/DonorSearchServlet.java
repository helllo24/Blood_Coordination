package com.blooddonation.controller;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.model.Donor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling Donor Search and Clinical Compatibility Filtering.
 * Supports searching by Blood Group, City, Availability status,
 * and Clinical Cross-Matching with blood bank confirmation disclaimers.
 */
@WebServlet(name = "DonorSearchServlet", urlPatterns = {"/donor-search", "/search-donors"})
public class DonorSearchServlet extends HttpServlet {

    private final DonorDAO donorDAO = new DonorDAO();

    public static final String CLINICAL_DISCLAIMER =
            "Compatibility results are preliminary software filtering only. " +
            "Actual donor eligibility and blood compatibility must be confirmed by qualified blood-bank professionals.";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bloodGroup = request.getParameter("bloodGroup");
        String city = request.getParameter("city");
        String compatibleParam = request.getParameter("compatible");
        String availableOnlyParam = request.getParameter("availableOnly");

        boolean isCompatible = "true".equalsIgnoreCase(compatibleParam);

        // Normalize blank inputs to null
        if (bloodGroup != null && bloodGroup.trim().isEmpty()) {
            bloodGroup = null;
        } else if (bloodGroup != null) {
            bloodGroup = bloodGroup.trim();
        }

        if (city != null && city.trim().isEmpty()) {
            city = null;
        } else if (city != null) {
            city = city.trim();
        }

        List<Donor> matchingDonors;

        if (bloodGroup != null && isCompatible) {
            // Clinical compatibility cross-match via compatibility matrix
            matchingDonors = donorDAO.findCompatibleDonors(bloodGroup, city);
        } else {
            Boolean availableOnly = null;
            if (availableOnlyParam != null && !availableOnlyParam.isBlank()) {
                availableOnly = Boolean.parseBoolean(availableOnlyParam);
            }
            matchingDonors = donorDAO.searchDonors(bloodGroup, city, availableOnly);
        }

        request.setAttribute("donorsList", matchingDonors);
        request.setAttribute("selectedBg", bloodGroup != null ? bloodGroup : "");
        request.setAttribute("selectedCity", city != null ? city : "");
        request.setAttribute("isCompatible", isCompatible);
        request.setAttribute("availableOnly", availableOnlyParam);
        request.setAttribute("disclaimer", CLINICAL_DISCLAIMER);

        request.getRequestDispatcher("/search-donors.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Delegate POST search requests to doGet for uniform query processing
        doGet(request, response);
    }
}
