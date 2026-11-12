<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.dao.BloodRequestDAO" %>
<%@ page import="com.blooddonation.dao.DonorResponseDAO" %>
<%@ page import="com.blooddonation.dao.DonationDAO" %>
<%@ page import="com.blooddonation.dao.UserDAO" %>
<%@ page import="com.blooddonation.model.Donor" %>
<%@ page import="com.blooddonation.model.BloodRequest" %>
<%@ page import="com.blooddonation.model.DonorResponse" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.Donation" %>
<%@ page import="java.util.List" %>

<%
    // Session validation: Prevent unauthorized access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20access%20the%20administrator%20portal.");
        return;
    }
    if (currentUser.getRole() != com.blooddonation.model.Role.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Access%20denied.%20Administrator%20privileges%20required.");
        return;
    }

    DonorDAO donorDAO = new DonorDAO();
    BloodRequestDAO requestDAO = new BloodRequestDAO();
    DonorResponseDAO responseDAO = new DonorResponseDAO();
    DonationDAO donationDAO = new DonationDAO();
    UserDAO userDAO = new UserDAO();

    // 1. Dynamic Admin Statistics (Retrieved dynamically from MySQL)
    int totalDonors = donorDAO.getTotalDonorsCount();
    int totalRequesters = userDAO.countByRole(com.blooddonation.model.Role.REQUESTER);
    int totalBloodRequests = requestDAO.getTotalRequestsCount();
    int pendingRequests = requestDAO.countByStatus("PENDING");
    int matchedRequests = requestDAO.countByStatus("MATCHED");
    int completedDonations = donationDAO.getTotalDonationsCount();
    int totalUnitsDonated = donationDAO.getTotalUnitsDonatedSystemWide();

    // 2. Recent Activities (Retrieved dynamically from MySQL)
    List<Donor> recentDonors = donorDAO.findRecentDonors(5);
    List<BloodRequest> recentRequests = requestDAO.findRecentRequests(5);
    List<DonorResponse> recentResponses = responseDAO.findRecentResponses(5);
    List<Donation> recentCompletedDonations = donationDAO.findRecentDonations(5);

    List<Donor> allDonors = donorDAO.findAll();
    
    // Support filtering for requests
    String bloodGroup = request.getParameter("bloodGroup");
    String urgency = request.getParameter("urgency");
    String status = request.getParameter("status");
    String city = request.getParameter("city");

    List<BloodRequest> allRequests;
    if ((bloodGroup != null && !bloodGroup.isBlank()) ||
        (urgency != null && !urgency.isBlank()) ||
        (status != null && !status.isBlank()) ||
        (city != null && !city.isBlank())) {
        allRequests = requestDAO.searchAndFilter(bloodGroup, urgency, status, city);
    } else {
        allRequests = requestDAO.findAll();
    }

    List<User> allUsers = userDAO.findAll();
    List<Donation> recentDonations = donationDAO.findAll();

    // Donor Response Metrics
    List<DonorResponse> allResponses = responseDAO.findAll();
    int acceptedResponses = responseDAO.countByStatus("ACCEPTED");
    int rejectedResponses = responseDAO.countByStatus("REJECTED");
    int pendingResponses = responseDAO.countByStatus("PENDING");

    request.setAttribute("totalDonors", totalDonors);
    request.setAttribute("totalRequesters", totalRequesters);
    request.setAttribute("totalBloodRequests", totalBloodRequests);
    request.setAttribute("pendingRequests", pendingRequests);
    request.setAttribute("matchedRequests", matchedRequests);
    request.setAttribute("completedDonations", completedDonations);
    request.setAttribute("totalUnitsDonated", totalUnitsDonated);

    request.setAttribute("recentDonors", recentDonors);
    request.setAttribute("recentRequests", recentRequests);
    request.setAttribute("recentResponses", recentResponses);
    request.setAttribute("recentCompletedDonations", recentCompletedDonations);

    request.setAttribute("allDonors", allDonors);
    request.setAttribute("allRequests", allRequests);
    request.setAttribute("recentDonations", recentDonations);
    request.setAttribute("allUsers", allUsers);
    request.setAttribute("allResponses", allResponses);
    request.setAttribute("acceptedResponses", acceptedResponses);
    request.setAttribute("rejectedResponses", rejectedResponses);
    request.setAttribute("pendingResponses", pendingResponses);
    request.setAttribute("selectedBg", bloodGroup != null ? bloodGroup : "");
    request.setAttribute("selectedUrgency", urgency != null ? urgency : "");
    request.setAttribute("selectedStatus", status != null ? status : "");
    request.setAttribute("selectedCity", city != null ? city : "");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - BloodConnect</title>
    <jsp:include page="includes/header.jsp" />
</head>
<body>

    <!-- Professional Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Dashboard Hero Header -->
    <section class="dashboard-hero">
        <div class="container">
            <div class="row align-items-center g-3">
                <div class="col-lg-8">
                    <span class="badge bg-danger text-white fw-semibold px-3 py-1 rounded-pill mb-2">
                        <i class="bi bi-shield-lock-fill me-1"></i> System Administration Control Panel
                    </span>
                    <h1 class="dashboard-title">
                        Platform Administration Overview
                    </h1>
                    <p class="dashboard-subtitle">
                        Monitor donor registries, blood requisitions, matched donor responses, and coordination audit trails.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline-red me-2">
                        <i class="bi bi-graph-up me-1"></i> Reports
                    </a>
                    <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-outline-red me-2">
                        <i class="bi bi-plus-circle me-1"></i> New Request
                    </a>
                    <a href="${pageContext.request.contextPath}/search-donors.jsp" class="btn btn-primary-red">
                        <i class="bi bi-search me-1"></i> Search Registry
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Dashboard Container -->
    <main class="container mb-5">
        <!-- Status Feedback Alerts -->
        <c:if test="${not empty param.statusUpdated}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Requisition status updated successfully in the national registry.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show custom-alert shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5 text-danger"></i>
                <c:out value="${param.error}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 1. Dynamic Admin Statistics (MySQL) -->
        <div class="row g-3 mb-3">
            <!-- 1. Total Registered Donors -->
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-people-fill"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number">${totalDonors}</div>
                        <div class="stat-metric-label">Total Registered Donors</div>
                    </div>
                </div>
            </div>
            <!-- 2. Total Requesters -->
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-person-heart text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number">${totalRequesters}</div>
                        <div class="stat-metric-label">Total Requesters</div>
                    </div>
                </div>
            </div>
            <!-- 3. Total Blood Requests -->
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-clipboard2-pulse text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">${totalBloodRequests}</div>
                        <div class="stat-metric-label">Total Blood Requests</div>
                    </div>
                </div>
            </div>
            <!-- 6. Completed Donations & 7. Total Units -->
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-droplet-fill text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">${completedDonations} <small class="fs-6 text-muted">(${totalUnitsDonated} Units)</small></div>
                        <div class="stat-metric-label">Completed Blood Donations</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mb-4">
            <!-- 4. Pending Blood Requests -->
            <div class="col-md-4">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-hourglass-split text-warning"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-warning">${pendingRequests}</div>
                        <div class="stat-metric-label">Pending Blood Requests</div>
                    </div>
                </div>
            </div>
            <!-- 5. Matched Blood Requests -->
            <div class="col-md-4">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-check2-circle text-primary"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-primary">${matchedRequests}</div>
                        <div class="stat-metric-label">Matched Blood Requests</div>
                    </div>
                </div>
            </div>
            <!-- 7. Total Units Donated System-Wide -->
            <div class="col-md-4">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-heart-pulse-fill text-success"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-success">${totalUnitsDonated} Units <small class="fs-6 text-muted">(~${totalUnitsDonated * 3} Lives)</small></div>
                        <div class="stat-metric-label">Total Units Donated</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 2. Recent Activities Section -->
        <div class="content-card mb-4">
            <div class="content-card-header d-flex justify-content-between align-items-center">
                <h4 class="content-card-title text-danger mb-0">
                    <i class="bi bi-activity me-2"></i> Recent Platform Activities
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    Live Activity Stream
                </span>
            </div>

            <div class="p-3">
                <ul class="nav nav-tabs custom-tabs mb-3" id="recentActivityTabs" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active fw-semibold" id="recent-donors-tab" data-bs-toggle="tab" 
                                data-bs-target="#recent-donors-pane" type="button" role="tab">
                            <i class="bi bi-person-plus me-1"></i> Recent Donors
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="recent-requests-tab" data-bs-toggle="tab" 
                                data-bs-target="#recent-requests-pane" type="button" role="tab">
                            <i class="bi bi-clipboard2-pulse me-1"></i> Recent Requests
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="recent-responses-tab" data-bs-toggle="tab" 
                                data-bs-target="#recent-responses-pane" type="button" role="tab">
                            <i class="bi bi-chat-heart me-1"></i> Latest Responses
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="recent-donations-tab" data-bs-toggle="tab" 
                                data-bs-target="#recent-donations-pane" type="button" role="tab">
                            <i class="bi bi-droplet-half me-1"></i> Recent Donations
                        </button>
                    </li>
                </ul>

                <div class="tab-content" id="recentActivityTabContent">
                    <!-- Tab 1: Recent Donors -->
                    <div class="tab-pane fade show active" id="recent-donors-pane" role="tabpanel">
                        <c:choose>
                            <c:when test="${empty recentDonors}">
                                <div class="text-center py-4 text-muted small">No recently registered donors found.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-custom mb-0">
                                        <thead>
                                            <tr>
                                                <th>Donor Name &amp; Contact</th>
                                                <th>Blood Group</th>
                                                <th>Demographics</th>
                                                <th>Location</th>
                                                <th>Availability</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="rd" items="${recentDonors}">
                                                <tr>
                                                    <td>
                                                        <div class="fw-semibold text-dark"><c:out value="${rd.fullName}" /></div>
                                                        <small class="text-muted"><c:out value="${rd.phone}" /></small>
                                                    </td>
                                                    <td><span class="badge-blood-pill"><c:out value="${rd.bloodGroup}" /></span></td>
                                                    <td><c:out value="${rd.age}" /> yrs / <c:out value="${rd.gender}" /></td>
                                                    <td><i class="bi bi-geo-alt text-danger me-1"></i><c:out value="${rd.city}" />, <c:out value="${rd.state}" /></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${rd.available}">
                                                                <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">Available</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-secondary-subtle text-muted border px-2 py-1 rounded-pill">Inactive</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Tab 2: Recent Requests -->
                    <div class="tab-pane fade" id="recent-requests-pane" role="tabpanel">
                        <c:choose>
                            <c:when test="${empty recentRequests}">
                                <div class="text-center py-4 text-muted small">No recently submitted blood requests found.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-custom mb-0">
                                        <thead>
                                            <tr>
                                                <th>Patient Name</th>
                                                <th>Blood Group</th>
                                                <th>Units</th>
                                                <th>Hospital &amp; City</th>
                                                <th>Urgency</th>
                                                <th>Status</th>
                                                <th>Required Date</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="rr" items="${recentRequests}">
                                                <tr>
                                                    <td><strong><c:out value="${rr.patientName}" /></strong></td>
                                                    <td><span class="badge-blood-pill"><c:out value="${rr.bloodGroup}" /></span></td>
                                                    <td><c:out value="${rr.unitsNeeded}" /> Unit(s)</td>
                                                    <td><c:out value="${rr.hospitalName}" /> (<c:out value="${rr.hospitalCity}" />)</td>
                                                    <td>
                                                        <span class="${rr.urgencyLevel eq 'CRITICAL' ? 'badge-urgency-critical' : rr.urgencyLevel eq 'URGENT' ? 'badge-urgency-urgent' : 'badge-urgency-normal'}">
                                                            <c:out value="${rr.urgencyLevel}" />
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <span class="${rr.status eq 'PENDING' ? 'badge-status-pending' : rr.status eq 'MATCHED' ? 'badge-status-matched' : rr.status eq 'FULFILLED' ? 'badge-status-fulfilled' : 'badge-status-cancelled'}">
                                                            <c:out value="${rr.status}" />
                                                        </span>
                                                    </td>
                                                    <td><small class="text-muted"><c:out value="${rr.requiredDate}" /></small></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Tab 3: Latest Responses -->
                    <div class="tab-pane fade" id="recent-responses-pane" role="tabpanel">
                        <c:choose>
                            <c:when test="${empty recentResponses}">
                                <div class="text-center py-4 text-muted small">No recent donor responses found.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-custom mb-0">
                                        <thead>
                                            <tr>
                                                <th>Donor Name</th>
                                                <th>Donor Group</th>
                                                <th>Patient / Hospital</th>
                                                <th>Status</th>
                                                <th>Message</th>
                                                <th>Responded At</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="rp" items="${recentResponses}">
                                                <tr>
                                                    <td><strong><c:out value="${rp.donorName}" /></strong></td>
                                                    <td><span class="badge-blood-pill"><c:out value="${rp.donorBloodGroup}" /></span></td>
                                                    <td><c:out value="${rp.patientName}" /> (<c:out value="${rp.hospitalName}" />)</td>
                                                    <td>
                                                        <span class="badge ${rp.status eq 'ACCEPTED' ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-muted border'} px-2 py-1 rounded-pill">
                                                            <c:out value="${rp.status}" />
                                                        </span>
                                                    </td>
                                                    <td><small class="text-muted"><c:out value="${rp.message}" /></small></td>
                                                    <td><small class="text-muted"><c:out value="${rp.respondedAt}" /></small></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Tab 4: Recent Donations -->
                    <div class="tab-pane fade" id="recent-donations-pane" role="tabpanel">
                        <c:choose>
                            <c:when test="${empty recentCompletedDonations}">
                                <div class="text-center py-4 text-muted small">No recent completed donations recorded.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-custom mb-0">
                                        <thead>
                                            <tr>
                                                <th>Donor Name</th>
                                                <th>Blood Group</th>
                                                <th>Units</th>
                                                <th>Hospital / Camp</th>
                                                <th>Donation Date</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="rdon" items="${recentCompletedDonations}">
                                                <tr>
                                                    <td><strong><c:out value="${rdon.donorName}" /></strong></td>
                                                    <td><span class="badge-blood-pill"><c:out value="${rdon.bloodGroup}" /></span></td>
                                                    <td><span class="fw-bold text-danger"><c:out value="${rdon.unitsDonated}" /> Unit(s)</span></td>
                                                    <td><c:out value="${rdon.hospitalOrCamp}" /></td>
                                                    <td><small class="text-muted"><c:out value="${rdon.donationDate}" /></small></td>
                                                    <td>
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                            <c:out value="${rdon.status}" />
                                                        </span>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <!-- Filter Card for Blood Requests -->
        <div class="search-filter-card mb-4">
            <form action="${pageContext.request.contextPath}/admin-dashboard.jsp" method="get">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label for="filterBg" class="form-label fw-semibold">Blood Group</label>
                        <select class="form-select" id="filterBg" name="bloodGroup">
                            <option value="">-- All Blood Groups --</option>
                            <c:forEach var="bg" items="${['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-']}">
                                <option value="${bg}" ${selectedBg eq bg ? 'selected' : ''}>${bg}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-md-3">
                        <label for="filterUrgency" class="form-label fw-semibold">Urgency Level</label>
                        <select class="form-select" id="filterUrgency" name="urgency">
                            <option value="">-- All Urgencies --</option>
                            <option value="CRITICAL" ${selectedUrgency eq 'CRITICAL' ? 'selected' : ''}>CRITICAL</option>
                            <option value="URGENT" ${selectedUrgency eq 'URGENT' ? 'selected' : ''}>URGENT</option>
                            <option value="NORMAL" ${selectedUrgency eq 'NORMAL' ? 'selected' : ''}>NORMAL</option>
                        </select>
                    </div>

                    <div class="col-md-3">
                        <label for="filterStatus" class="form-label fw-semibold">Request Status</label>
                        <select class="form-select" id="filterStatus" name="status">
                            <option value="">-- All Statuses --</option>
                            <option value="PENDING" ${selectedStatus eq 'PENDING' ? 'selected' : ''}>PENDING</option>
                            <option value="MATCHED" ${selectedStatus eq 'MATCHED' ? 'selected' : ''}>MATCHED</option>
                            <option value="FULFILLED" ${selectedStatus eq 'FULFILLED' ? 'selected' : ''}>FULFILLED</option>
                            <option value="CANCELLED" ${selectedStatus eq 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                        </select>
                    </div>

                    <div class="col-md-3 d-flex gap-2">
                        <button type="submit" class="btn btn-primary-red flex-grow-1 py-2">
                            <i class="bi bi-funnel-fill me-1"></i> Filter
                        </button>
                        <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="btn btn-secondary-light py-2">
                            Reset
                        </a>
                    </div>
                </div>
            </form>
        </div>

        <!-- Blood Requests Section -->
        <div class="content-card mb-4">
            <div class="content-card-header">
                <h4 class="content-card-title text-danger">
                    <i class="bi bi-bell-fill"></i> Blood Requisitions Registry
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${allRequests.size()} Records Found
                </span>
            </div>

            <c:choose>
                <c:when test="${empty allRequests}">
                    <div class="text-center py-5 text-muted">
                        <i class="bi bi-search fs-1 d-block mb-2 text-secondary"></i>
                        No blood requests match the specified filter criteria.
                        <div class="mt-2">
                            <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="btn btn-outline-red btn-sm">Clear Filters</a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-custom">
                            <thead>
                                <tr>
                                    <th>Patient / Contact</th>
                                    <th>Group</th>
                                    <th>Units</th>
                                    <th>Hospital &amp; City</th>
                                    <th>Urgency</th>
                                    <th>Status</th>
                                    <th>Required Date</th>
                                    <th class="text-end">Manage Requisition</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="req" items="${allRequests}">
                                    <tr>
                                        <td>
                                            <strong><c:out value="${req.patientName}" /></strong>
                                            <div class="small text-muted"><c:out value="${req.contactPerson}" /> (<c:out value="${req.contactPhone}" />)</div>
                                        </td>
                                        <td>
                                            <span class="badge-blood-pill"><c:out value="${req.bloodGroup}" /></span>
                                        </td>
                                        <td><c:out value="${req.unitsNeeded}" /> Unit(s)</td>
                                        <td>
                                            <div class="text-dark"><c:out value="${req.hospitalName}" /></div>
                                            <small class="text-muted"><i class="bi bi-geo-alt me-1"></i><c:out value="${req.hospitalCity}" /></small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.urgencyLevel eq 'CRITICAL'}">
                                                    <span class="badge-urgency-critical">CRITICAL</span>
                                                </c:when>
                                                <c:when test="${req.urgencyLevel eq 'URGENT'}">
                                                    <span class="badge-urgency-urgent">URGENT</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-urgency-normal">NORMAL</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status eq 'PENDING'}">
                                                    <span class="badge-status-pending">PENDING</span>
                                                </c:when>
                                                <c:when test="${req.status eq 'MATCHED'}">
                                                    <span class="badge-status-matched">MATCHED</span>
                                                </c:when>
                                                <c:when test="${req.status eq 'FULFILLED'}">
                                                    <span class="badge-status-fulfilled">FULFILLED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-status-cancelled"><c:out value="${req.status}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><small class="text-muted"><c:out value="${req.requiredDate}" /></small></td>
                                        <td class="text-end">
                                            <div class="btn-group btn-group-sm">
                                                <!-- View Modal Trigger -->
                                                <button type="button" class="btn btn-outline-secondary btn-sm" 
                                                        data-bs-toggle="modal" data-bs-target="#adminViewModal${req.id}" title="View Details">
                                                    <i class="bi bi-eye"></i>
                                                </button>

                                                <!-- Status Update Trigger Modal -->
                                                <button type="button" class="btn btn-outline-primary btn-sm" 
                                                        data-bs-toggle="modal" data-bs-target="#adminStatusModal${req.id}" title="Update Status">
                                                    <i class="bi bi-arrow-repeat"></i> Status
                                                </button>
                                            </div>

                                            <!-- Modal: View Details -->
                                            <div class="modal fade text-start" id="adminViewModal${req.id}" tabindex="-1" aria-hidden="true">
                                                <div class="modal-dialog">
                                                    <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                        <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                            <h5 class="modal-title fw-bold text-danger">
                                                                <i class="bi bi-clipboard2-pulse me-2"></i>Requisition #REQ-<c:out value="${req.id}" />
                                                            </h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="d-flex align-items-center gap-3 p-3 bg-light rounded-3 mb-3">
                                                                <div class="blood-type-badge m-0" style="width: 50px; height: 50px; font-size: 1.2rem;">
                                                                    <c:out value="${req.bloodGroup}" />
                                                                </div>
                                                                <div>
                                                                    <h5 class="fw-bold mb-0"><c:out value="${req.patientName}" /></h5>
                                                                    <div class="text-danger fw-semibold"><c:out value="${req.unitsNeeded}" /> Unit(s) • Urgency: <c:out value="${req.urgencyLevel}" /></div>
                                                                </div>
                                                            </div>
                                                            <div class="small text-muted mb-2">
                                                                <p class="mb-1"><strong>Requester User ID:</strong> #USR-<c:out value="${req.requesterId}" /></p>
                                                                <p class="mb-1"><strong>Hospital:</strong> <c:out value="${req.hospitalName}" /></p>
                                                                <p class="mb-1"><strong>Address:</strong> <c:out value="${req.hospitalAddress}" />, <c:out value="${req.hospitalCity}" /></p>
                                                                <p class="mb-1"><strong>Contact Person:</strong> <c:out value="${req.contactPerson}" /> (<c:out value="${req.contactPhone}" />)</p>
                                                                <p class="mb-1"><strong>Required By:</strong> <c:out value="${req.requiredDate}" /></p>
                                                                <p class="mb-0"><strong>Current Status:</strong> <span class="badge bg-secondary"><c:out value="${req.status}" /></span></p>
                                                            </div>
                                                            <c:if test="${not empty req.additionalNotes}">
                                                                <div class="p-2 border rounded bg-white small mt-2">
                                                                    <strong>Notes:</strong> <c:out value="${req.additionalNotes}" />
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                        <div class="modal-footer border-top">
                                                            <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Close</button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Modal: Admin Status Update -->
                                            <div class="modal fade text-start" id="adminStatusModal${req.id}" tabindex="-1" aria-hidden="true">
                                                <div class="modal-dialog">
                                                    <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                        <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                            <h5 class="modal-title fw-bold text-danger">
                                                                <i class="bi bi-shield-check me-2"></i>Update Requisition Status
                                                            </h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <form action="${pageContext.request.contextPath}/blood-request" method="post">
                                                            <div class="modal-body">
                                                                <input type="hidden" name="action" value="updateStatus">
                                                                <input type="hidden" name="requestId" value="${req.id}">
                                                                <p class="text-dark small mb-3">
                                                                    Select the administrative status for request <strong>#REQ-<c:out value="${req.id}" /></strong> (Patient: <c:out value="${req.patientName}" />):
                                                                </p>
                                                                <div class="mb-3">
                                                                    <label for="statusSelect${req.id}" class="form-label fw-semibold">New Administrative Status</label>
                                                                    <select class="form-select" id="statusSelect${req.id}" name="newStatus" required>
                                                                        <option value="PENDING" ${req.status eq 'PENDING' ? 'selected' : ''}>PENDING (Open for Donor Match)</option>
                                                                        <option value="MATCHED" ${req.status eq 'MATCHED' ? 'selected' : ''}>MATCHED (Donors Identified)</option>
                                                                        <option value="FULFILLED" ${req.status eq 'FULFILLED' ? 'selected' : ''}>FULFILLED (Donation Complete)</option>
                                                                        <option value="CANCELLED" ${req.status eq 'CANCELLED' ? 'selected' : ''}>CANCELLED (Request Voided)</option>
                                                                    </select>
                                                                </div>
                                                            </div>
                                                            <div class="modal-footer border-top">
                                                                <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <button type="submit" class="btn btn-primary-red btn-sm">Save Status</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- All Donor Responses Audit Section (Admin View) -->
        <div class="content-card mb-4">
            <div class="content-card-header">
                <h4 class="content-card-title text-success">
                    <i class="bi bi-chat-heart-fill"></i> All Voluntary Donor Responses Audit Trail
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${allResponses.size()} Total Responses
                </span>
            </div>

            <c:choose>
                <c:when test="${empty allResponses}">
                    <div class="text-center py-4 text-muted small">
                        No donor responses logged across the platform yet.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-custom">
                            <thead>
                                <tr>
                                    <th>Donor Name &amp; Contact</th>
                                    <th>Donor Group</th>
                                    <th>Patient / Hospital</th>
                                    <th>Response Status</th>
                                    <th>Message</th>
                                    <th>Responded At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="resp" items="${allResponses}">
                                    <tr>
                                        <td>
                                            <div class="fw-semibold text-dark"><c:out value="${resp.donorName}" /></div>
                                            <small class="text-muted"><i class="bi bi-telephone-fill me-1 text-success"></i><c:out value="${resp.donorPhone}" /></small>
                                        </td>
                                        <td>
                                            <span class="badge-blood-pill"><c:out value="${resp.donorBloodGroup}" /></span>
                                        </td>
                                        <td>
                                            <div class="text-dark"><c:out value="${resp.patientName}" /></div>
                                            <small class="text-muted"><c:out value="${resp.hospitalName}" /></small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${resp.status eq 'ACCEPTED'}">
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                        <i class="bi bi-check-circle-fill me-1"></i> ACCEPTED
                                                    </span>
                                                </c:when>
                                                <c:when test="${resp.status eq 'REJECTED'}">
                                                    <span class="badge bg-secondary-subtle text-muted border px-2 py-1 rounded-pill">
                                                        <i class="bi bi-x-circle me-1"></i> REJECTED
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-warning-subtle text-dark border px-2 py-1 rounded-pill">
                                                        <c:out value="${resp.status}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><small class="text-muted"><c:out value="${resp.message}" /></small></td>
                                        <td><small class="text-muted"><c:out value="${resp.respondedAt}" /></small></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Verified Blood Donations Audit Section (Admin Overview) -->
        <div class="content-card mb-4">
            <div class="content-card-header">
                <h4 class="content-card-title text-danger">
                    <i class="bi bi-droplet-half"></i> Verified Blood Donations Audit Log
                </h4>
                <div>
                    <a href="${pageContext.request.contextPath}/donation-history" class="btn btn-primary-red btn-sm">
                        <i class="bi bi-journal-plus me-1"></i> Full Audit Registry &amp; Record Donation
                    </a>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty recentDonations}">
                    <div class="text-center py-4 text-muted small">
                        No official donation records logged in the database yet.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-custom">
                            <thead>
                                <tr>
                                    <th>Audit ID</th>
                                    <th>Donor Details</th>
                                    <th>Units</th>
                                    <th>Hospital / Camp</th>
                                    <th>Donation Date</th>
                                    <th>Status</th>
                                    <th>Remarks</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="don" items="${recentDonations}">
                                    <tr>
                                        <td><strong>#DON-<c:out value="${don.id}" /></strong></td>
                                        <td>
                                            <strong><c:out value="${don.donorName}" /></strong>
                                            <span class="badge-blood-pill ms-1"><c:out value="${don.bloodGroup}" /></span>
                                        </td>
                                        <td><span class="fw-bold text-danger"><c:out value="${don.unitsDonated}" /> Unit(s)</span></td>
                                        <td><c:out value="${don.hospitalOrCamp}" /></td>
                                        <td><small class="text-muted"><c:out value="${don.donationDate}" /></small></td>
                                        <td>
                                            <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                <c:out value="${don.status}" />
                                            </span>
                                        </td>
                                        <td><small class="text-muted"><c:out value="${don.remarks}" /></small></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Recent Donor Registrations Section -->
        <div class="content-card">
            <div class="content-card-header">
                <h4 class="content-card-title text-secondary">
                    <i class="bi bi-person-lines-fill"></i> Registered Voluntary Donors Registry
                </h4>
                <a href="${pageContext.request.contextPath}/search-donors.jsp" class="btn btn-secondary-light btn-sm">
                    <i class="bi bi-search me-1"></i> Search Filter
                </a>
            </div>

            <div class="table-responsive">
                <table class="table table-custom">
                    <thead>
                        <tr>
                            <th>Donor ID</th>
                            <th>Blood Group</th>
                            <th>Demographics</th>
                            <th>City &amp; State</th>
                            <th>Last Donation</th>
                            <th>Total Donations</th>
                            <th>Emergency Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="donor" items="${allDonors}">
                            <tr>
                                <td>
                                    <strong>#DNR-<c:out value="${donor.id}" /></strong>
                                </td>
                                <td>
                                    <span class="badge-blood-pill"><c:out value="${donor.bloodGroup}" /></span>
                                </td>
                                <td>
                                    <c:out value="${donor.age}" /> yrs / <c:out value="${donor.gender}" />
                                </td>
                                <td>
                                    <i class="bi bi-geo-alt text-danger me-1"></i><c:out value="${donor.city}" />, <c:out value="${donor.state}" />
                                </td>
                                <td>
                                    <small class="text-muted"><c:out value="${donor.lastDonationDate != null ? donor.lastDonationDate : 'First Time'}" /></small>
                                </td>
                                <td>
                                    <span class="fw-bold text-danger"><c:out value="${donor.totalDonations}" /></span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${donor.available}">
                                            <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                <i class="bi bi-check-circle-fill me-1"></i> Available
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary-subtle text-muted border px-2 py-1 rounded-pill">
                                                <i class="bi bi-dash-circle me-1"></i> Inactive
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
