<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.dao.BloodRequestDAO" %>
<%@ page import="com.blooddonation.dao.DonationDAO" %>
<%@ page import="com.blooddonation.dao.UserDAO" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.Role" %>
<%@ page import="java.util.Map" %>

<%
    // Session validation: Prevent unauthorized access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20administrative%20reports.");
        return;
    }
    if (currentUser.getRole() != Role.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Access%20denied.%20Administrator%20privileges%20required.");
        return;
    }

    // Direct access fallback if not forwarded by AdminReportServlet
    if (request.getAttribute("donorsByBloodGroup") == null) {
        DonorDAO donorDAO = new DonorDAO();
        BloodRequestDAO requestDAO = new BloodRequestDAO();
        DonationDAO donationDAO = new DonationDAO();
        UserDAO userDAO = new UserDAO();

        request.setAttribute("donorsByBloodGroup", donorDAO.getDonorsCountByBloodGroup());
        request.setAttribute("requestsByStatus", requestDAO.getRequestsCountByStatus());
        request.setAttribute("donationsByMonth", donationDAO.getDonationsByMonth());
        request.setAttribute("totalCompletedDonations", donationDAO.getTotalDonationsCount());
        int totalUnits = donationDAO.getTotalUnitsDonatedSystemWide();
        request.setAttribute("totalUnitsDonated", totalUnits);
        request.setAttribute("totalDonors", donorDAO.getTotalDonorsCount());
        request.setAttribute("totalRequests", requestDAO.getTotalRequestsCount());
        request.setAttribute("totalRequesters", userDAO.countByRole(Role.REQUESTER));
        request.setAttribute("livesSaved", totalUnits * 3);
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administrative Reports - BloodConnect</title>
    <jsp:include page="includes/header.jsp" />
    <!-- Chart.js CDN for client-side analytical visualizations -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .report-section-header {
            border-bottom: 2px solid var(--light-red);
            padding-bottom: 0.5rem;
            margin-bottom: 1.25rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .chart-box {
            position: relative;
            height: 280px;
            width: 100%;
        }
        @media print {
            .navbar, .footer, .btn, .no-print {
                display: none !important;
            }
            body {
                background: white !important;
            }
            .content-card {
                box-shadow: none !important;
                border: 1px solid #ccc !important;
            }
        }
    </style>
</head>
<body>

    <!-- Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Hero Header -->
    <section class="dashboard-hero">
        <div class="container">
            <div class="row align-items-center g-3">
                <div class="col-lg-8">
                    <span class="badge bg-danger text-white fw-semibold px-3 py-1 rounded-pill mb-2">
                        <i class="bi bi-graph-up-arrow me-1"></i> Clinical Registry Analytics
                    </span>
                    <h1 class="dashboard-title">
                        Platform Analytical Reports
                    </h1>
                    <p class="dashboard-subtitle">
                        Aggregated distribution reports across blood groups, requisition fulfillment rates, and monthly collection volumes.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end no-print">
                    <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="btn btn-outline-red me-2">
                        <i class="bi bi-arrow-left me-1"></i> Dashboard
                    </a>
                    <button type="button" onclick="window.print()" class="btn btn-primary-red">
                        <i class="bi bi-printer me-1"></i> Print Report
                    </button>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Content Container -->
    <main class="container mb-5">
        <!-- Overview Summary Stat Cards -->
        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-droplet-fill text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">${totalCompletedDonations}</div>
                        <div class="stat-metric-label">Completed Blood Donations</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-box2-heart text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">${totalUnitsDonated} Units</div>
                        <div class="stat-metric-label">Total Units Donated</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-people-fill"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number">${totalDonors}</div>
                        <div class="stat-metric-label">Registered Voluntary Donors</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-heart-pulse-fill text-success"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-success">~${livesSaved}</div>
                        <div class="stat-metric-label">Estimated Lives Saved</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- REPORT 1: Donors by Blood Group -->
        <div class="content-card mb-4">
            <div class="report-section-header">
                <h4 class="content-card-title text-danger mb-0">
                    <i class="bi bi-pie-chart-fill me-2"></i> Report 1: Voluntary Donors by Blood Group
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${totalDonors} Total Donors
                </span>
            </div>

            <div class="row g-4 align-items-center">
                <!-- Data Table -->
                <div class="col-lg-6">
                    <div class="table-responsive">
                        <table class="table table-custom mb-0">
                            <thead>
                                <tr>
                                    <th>Blood Group</th>
                                    <th>Registered Donors</th>
                                    <th>Proportion</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${donorsByBloodGroup}">
                                    <tr>
                                        <td>
                                            <span class="badge-blood-pill"><c:out value="${entry.key}" /></span>
                                        </td>
                                        <td><strong><c:out value="${entry.value}" /></strong> Donor(s)</td>
                                        <td>
                                            <div class="progress" style="height: 8px;">
                                                <div class="progress-bar bg-danger" role="progressbar" 
                                                     style="width: ${totalDonors > 0 ? (entry.value * 100 / totalDonors) : 0}%;" 
                                                     aria-valuenow="${entry.value}" aria-valuemin="0" aria-valuemax="${totalDonors}"></div>
                                            </div>
                                            <small class="text-muted">
                                                <c:choose>
                                                    <c:when test="${totalDonors > 0}">
                                                        ${String.format("%.1f", (entry.value * 100.0 / totalDonors))}%
                                                    </c:when>
                                                    <c:otherwise>0.0%</c:otherwise>
                                                </c:choose>
                                            </small>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Visual Chart Canvas -->
                <div class="col-lg-6">
                    <div class="p-3 bg-light rounded-3 text-center">
                        <h6 class="fw-semibold text-secondary mb-3">Blood Group Distribution</h6>
                        <div class="chart-box">
                            <canvas id="bloodGroupChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- REPORT 2: Blood Requests by Status -->
        <div class="content-card mb-4">
            <div class="report-section-header">
                <h4 class="content-card-title text-danger mb-0">
                    <i class="bi bi-clipboard2-pulse me-2"></i> Report 2: Emergency Blood Requisitions by Status
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${totalRequests} Total Requisitions
                </span>
            </div>

            <div class="row g-4 align-items-center">
                <!-- Data Table -->
                <div class="col-lg-6">
                    <div class="table-responsive">
                        <table class="table table-custom mb-0">
                            <thead>
                                <tr>
                                    <th>Status</th>
                                    <th>Total Requests</th>
                                    <th>Fulfillment Ratio</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${requestsByStatus}">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${entry.key eq 'PENDING'}">
                                                    <span class="badge-status-pending">PENDING</span>
                                                </c:when>
                                                <c:when test="${entry.key eq 'MATCHED'}">
                                                    <span class="badge-status-matched">MATCHED</span>
                                                </c:when>
                                                <c:when test="${entry.key eq 'FULFILLED'}">
                                                    <span class="badge-status-fulfilled">FULFILLED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-status-cancelled"><c:out value="${entry.key}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><strong><c:out value="${entry.value}" /></strong> Requisition(s)</td>
                                        <td>
                                            <small class="text-muted fw-semibold">
                                                <c:choose>
                                                    <c:when test="${totalRequests > 0}">
                                                        ${String.format("%.1f", (entry.value * 100.0 / totalRequests))}%
                                                    </c:when>
                                                    <c:otherwise>0.0%</c:otherwise>
                                                </c:choose>
                                            </small>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Visual Chart Canvas -->
                <div class="col-lg-6">
                    <div class="p-3 bg-light rounded-3 text-center">
                        <h6 class="fw-semibold text-secondary mb-3">Requisition Status Distribution</h6>
                        <div class="chart-box">
                            <canvas id="statusChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- REPORT 3: Donations by Month -->
        <div class="content-card">
            <div class="report-section-header">
                <h4 class="content-card-title text-danger mb-0">
                    <i class="bi bi-calendar3 me-2"></i> Report 3: Verified Blood Donations by Month
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    Monthly Audit Trail
                </span>
            </div>

            <div class="row g-4 align-items-center">
                <!-- Data Table -->
                <div class="col-lg-6">
                    <c:choose>
                        <c:when test="${empty donationsByMonth}">
                            <div class="text-center py-4 text-muted small">
                                No monthly completed donations recorded yet.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-custom mb-0">
                                    <thead>
                                        <tr>
                                            <th>Month (YYYY-MM)</th>
                                            <th>Completed Donations</th>
                                            <th>Total Units Donated</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="entry" items="${donationsByMonth}">
                                            <tr>
                                                <td><strong><c:out value="${entry.key}" /></strong></td>
                                                <td><c:out value="${entry.value[0]}" /> Record(s)</td>
                                                <td><span class="fw-bold text-danger"><c:out value="${entry.value[1]}" /> Unit(s)</span></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Visual Chart Canvas -->
                <div class="col-lg-6">
                    <div class="p-3 bg-light rounded-3 text-center">
                        <h6 class="fw-semibold text-secondary mb-3">Monthly Blood Units Collected</h6>
                        <div class="chart-box">
                            <canvas id="monthlyChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

    <!-- Chart.js Visualization Scripts -->
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            // 1. Blood Group Chart (Bar)
            const bgLabels = [];
            const bgData = [];
            <c:forEach var="entry" items="${donorsByBloodGroup}">
                bgLabels.push('${entry.key}');
                bgData.push(${entry.value});
            </c:forEach>

            const ctxBg = document.getElementById('bloodGroupChart').getContext('2d');
            new Chart(ctxBg, {
                type: 'bar',
                data: {
                    labels: bgLabels,
                    datasets: [{
                        label: 'Donors',
                        data: bgData,
                        backgroundColor: '#C62828',
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { precision: 0 }
                        }
                    }
                }
            });

            // 2. Request Status Chart (Doughnut)
            const statusLabels = [];
            const statusData = [];
            <c:forEach var="entry" items="${requestsByStatus}">
                statusLabels.push('${entry.key}');
                statusData.push(${entry.value});
            </c:forEach>

            const ctxStatus = document.getElementById('statusChart').getContext('2d');
            new Chart(ctxStatus, {
                type: 'doughnut',
                data: {
                    labels: statusLabels,
                    datasets: [{
                        data: statusData,
                        backgroundColor: ['#E65100', '#1565C0', '#2E7D32', '#78909C']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom' }
                    }
                }
            });

            // 3. Monthly Trend Chart (Line / Bar)
            const monthLabels = [];
            const monthUnits = [];
            <c:forEach var="entry" items="${donationsByMonth}">
                monthLabels.push('${entry.key}');
                monthUnits.push(${entry.value[1]});
            </c:forEach>

            const ctxMonth = document.getElementById('monthlyChart').getContext('2d');
            new Chart(ctxMonth, {
                type: 'line',
                data: {
                    labels: monthLabels.length ? monthLabels : ['No Data'],
                    datasets: [{
                        label: 'Units Donated',
                        data: monthUnits.length ? monthUnits : [0],
                        borderColor: '#E53935',
                        backgroundColor: 'rgba(229, 57, 53, 0.1)',
                        fill: true,
                        tension: 0.3,
                        pointBackgroundColor: '#C62828',
                        pointRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { precision: 0 }
                        }
                    }
                }
            });
        });
    </script>
</body>
</html>
