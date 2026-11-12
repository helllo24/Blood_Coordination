<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.DonationDAO" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.dao.BloodRequestDAO" %>
<%@ page import="com.blooddonation.model.Donation" %>
<%@ page import="com.blooddonation.model.Donor" %>
<%@ page import="com.blooddonation.model.BloodRequest" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.Role" %>
<%@ page import="java.sql.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Optional" %>

<%
    // Session validation: Prevent unauthenticated access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20donation%20history.");
        return;
    }

    DonationDAO donationDAO = new DonationDAO();
    DonorDAO donorDAO = new DonorDAO();
    BloodRequestDAO requestDAO = new BloodRequestDAO();

    boolean isAdmin = (currentUser.getRole() == Role.ADMIN);
    boolean isDonor = (currentUser.getRole() == Role.DONOR);

    if (!isAdmin && !isDonor) {
        response.sendRedirect(request.getContextPath() + "/requester-dashboard.jsp");
        return;
    }

    // Direct access fallback if not forwarded by DonationHistoryServlet
    if (request.getAttribute("donationsList") == null) {
        if (isDonor) {
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
        } else if (isAdmin) {
            String status = request.getParameter("status");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");

            Date fromDate = null;
            Date toDate = null;
            try {
                if (fromDateStr != null && !fromDateStr.isBlank()) fromDate = Date.valueOf(fromDateStr.trim());
            } catch (Exception ignored) {}
            try {
                if (toDateStr != null && !toDateStr.isBlank()) toDate = Date.valueOf(toDateStr.trim());
            } catch (Exception ignored) {}

            List<Donation> allDonations = donationDAO.filterDonations(status, fromDate, toDate);
            int totalDonations = donationDAO.getTotalDonationsCount();
            int totalUnits = donationDAO.getTotalUnitsDonatedSystemWide();

            request.setAttribute("donationsList", allDonations);
            request.setAttribute("totalDonations", totalDonations);
            request.setAttribute("totalUnits", totalUnits);
            request.setAttribute("livesSaved", totalUnits * 3);
            request.setAttribute("allDonors", donorDAO.findAll());
            request.setAttribute("allRequests", requestDAO.findAll());
            request.setAttribute("selectedStatus", status != null ? status : "");
            request.setAttribute("selectedFromDate", fromDateStr != null ? fromDateStr : "");
            request.setAttribute("selectedToDate", toDateStr != null ? toDateStr : "");
            request.setAttribute("isAdminView", true);
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${sessionScope.userRole eq 'ADMIN' ? 'Donation Records & Audit Registry' : 'My Donation History'} - BloodConnect</title>
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
                    <span class="badge bg-danger-subtle text-danger fw-semibold px-3 py-1 rounded-pill mb-2">
                        <i class="bi bi-clock-history me-1"></i> Transparent Clinical Audit Registry
                    </span>
                    <h1 class="dashboard-title">
                        <c:choose>
                            <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                                System-Wide Blood Donation Registry
                            </c:when>
                            <c:otherwise>
                                My Verified Donation History
                            </c:otherwise>
                        </c:choose>
                    </h1>
                    <p class="dashboard-subtitle">
                        <c:choose>
                            <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                                Complete historical audit trail of all verified hospital and camp donations, units contributed, and clinical status.
                            </c:when>
                            <c:otherwise>
                                Detailed record of your verified blood donations, contributing hospital facilities, and estimated life-saving clinical impact.
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <c:choose>
                        <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                            <button type="button" class="btn btn-primary-red" data-bs-toggle="modal" data-bs-target="#newDonationModal">
                                <i class="bi bi-plus-circle me-1"></i> Record Blood Donation
                            </button>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/donor-dashboard.jsp" class="btn btn-outline-red me-2">
                                <i class="bi bi-speedometer2 me-1"></i> Donor Dashboard
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Content Container -->
    <main class="container mb-5">
        <!-- Status Feedback Alerts -->
        <c:if test="${not empty param.donationLogged}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Official blood donation record logged and verified successfully!
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.statusUpdated}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Donation audit status and clinical remarks updated successfully.
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

        <!-- Metric Stat Cards -->
        <div class="row g-3 mb-4">
            <div class="col-md-4">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-droplet-fill text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">${totalDonations} Completed</div>
                        <div class="stat-metric-label">
                            ${sessionScope.userRole eq 'ADMIN' ? 'Total Completed Donations' : 'My Lifetime Donations'}
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-box2-heart-fill text-primary"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-primary">${totalUnits} Unit(s)</div>
                        <div class="stat-metric-label">
                            ${sessionScope.userRole eq 'ADMIN' ? 'Total Blood Units Collected' : 'Total Units Contributed'}
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-heart-pulse-fill text-success"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-success">~${livesSaved} Lives</div>
                        <div class="stat-metric-label">Estimated Clinical Impact (~3 Lives / Unit)</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Admin Filter Bar -->
        <c:if test="${sessionScope.userRole eq 'ADMIN'}">
            <div class="search-filter-card mb-4">
                <form action="${pageContext.request.contextPath}/donation-history" method="get">
                    <div class="row g-3 align-items-end">
                        <div class="col-md-3">
                            <label for="filterStatus" class="form-label fw-semibold">Donation Status</label>
                            <select class="form-select" id="filterStatus" name="status">
                                <option value="">-- All Statuses --</option>
                                <option value="COMPLETED" ${selectedStatus eq 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                                <option value="SCHEDULED" ${selectedStatus eq 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                                <option value="CANCELLED" ${selectedStatus eq 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label for="fromDate" class="form-label fw-semibold">From Date</label>
                            <input type="date" class="form-control" id="fromDate" name="fromDate" value="<c:out value='${selectedFromDate}' />">
                        </div>
                        <div class="col-md-3">
                            <label for="toDate" class="form-label fw-semibold">To Date</label>
                            <input type="date" class="form-control" id="toDate" name="toDate" value="<c:out value='${selectedToDate}' />">
                        </div>
                        <div class="col-md-3 d-flex gap-2">
                            <button type="submit" class="btn btn-primary-red flex-grow-1 py-2">
                                <i class="bi bi-funnel-fill me-1"></i> Filter
                            </button>
                            <a href="${pageContext.request.contextPath}/donation-history" class="btn btn-secondary-light py-2">
                                Reset
                            </a>
                        </div>
                    </div>
                </form>
            </div>
        </c:if>

        <!-- Donation History Table -->
        <div class="content-card">
            <div class="content-card-header">
                <h4 class="content-card-title text-danger">
                    <i class="bi bi-journal-medical"></i> Verified Donation Audit Records
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${donationsList.size()} Records Listed
                </span>
            </div>

            <c:choose>
                <c:when test="${empty donationsList}">
                    <div class="text-center py-5 text-muted">
                        <i class="bi bi-journal-x fs-1 d-block mb-2 text-secondary"></i>
                        <c:choose>
                            <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                                No donation records match the specified search or filter criteria.
                            </c:when>
                            <c:otherwise>
                                You do not have any past donation records logged in the system yet.
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-custom">
                            <thead>
                                <tr>
                                    <th>Audit ID</th>
                                    <c:if test="${sessionScope.userRole eq 'ADMIN'}">
                                        <th>Donor Details</th>
                                    </c:if>
                                    <th>Units</th>
                                    <th>Hospital / Blood Bank / Camp</th>
                                    <th>Related Request</th>
                                    <th>Donation Date</th>
                                    <th>Status</th>
                                    <th>Clinical Remarks</th>
                                    <c:if test="${sessionScope.userRole eq 'ADMIN'}">
                                        <th class="text-end">Manage</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="don" items="${donationsList}">
                                    <tr>
                                        <td><strong>#DON-<c:out value="${don.id}" /></strong></td>
                                        <c:if test="${sessionScope.userRole eq 'ADMIN'}">
                                            <td>
                                                <div class="fw-semibold text-dark">
                                                    <c:out value="${don.donorName != null ? don.donorName : 'Voluntary Donor'}" />
                                                    <span class="badge-blood-pill ms-1"><c:out value="${don.bloodGroup != null ? don.bloodGroup : 'O+'}" /></span>
                                                </div>
                                                <small class="text-muted">
                                                    <i class="bi bi-telephone me-1"></i><c:out value="${don.donorPhone != null ? don.donorPhone : 'N/A'}" />
                                                </small>
                                            </td>
                                        </c:if>
                                        <td>
                                            <span class="fw-bold text-danger"><c:out value="${don.unitsDonated}" /> Unit(s)</span>
                                        </td>
                                        <td>
                                            <div class="text-dark"><c:out value="${don.hospitalOrCamp}" /></div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty don.requestId}">
                                                    <span class="badge bg-light text-dark border">
                                                        #REQ-<c:out value="${don.requestId}" />
                                                        <c:if test="${not empty don.patientName}">
                                                            (<c:out value="${don.patientName}" />)
                                                        </c:if>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted small">Voluntary Camp</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <small class="text-muted"><i class="bi bi-calendar-event me-1 text-danger"></i><c:out value="${don.donationDate}" /></small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${don.status eq 'COMPLETED'}">
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                        <i class="bi bi-check-circle-fill me-1"></i> COMPLETED
                                                    </span>
                                                </c:when>
                                                <c:when test="${don.status eq 'SCHEDULED'}">
                                                    <span class="badge bg-info-subtle text-info border border-info-subtle px-2 py-1 rounded-pill">
                                                        <i class="bi bi-calendar-check me-1"></i> SCHEDULED
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary-subtle text-muted border px-2 py-1 rounded-pill">
                                                        <i class="bi bi-x-circle me-1"></i> <c:out value="${don.status}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <small class="text-muted"><c:out value="${don.remarks != null ? don.remarks : 'Verified'}" /></small>
                                        </td>
                                        <c:if test="${sessionScope.userRole eq 'ADMIN'}">
                                            <td class="text-end">
                                                <button type="button" class="btn btn-outline-primary btn-sm" 
                                                        data-bs-toggle="modal" data-bs-target="#editDonationModal${don.id}" title="Update Status">
                                                    <i class="bi bi-pencil-square"></i> Status
                                                </button>

                                                <!-- Status Update Modal (Admin Only) -->
                                                <div class="modal fade text-start" id="editDonationModal${don.id}" tabindex="-1" aria-hidden="true">
                                                    <div class="modal-dialog">
                                                        <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                            <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                                <h5 class="modal-title fw-bold text-danger">
                                                                    <i class="bi bi-journal-check me-2"></i>Update Donation Audit #DON-<c:out value="${don.id}" />
                                                                </h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <form action="${pageContext.request.contextPath}/donation-history" method="post">
                                                                <div class="modal-body">
                                                                    <input type="hidden" name="action" value="updateStatus">
                                                                    <input type="hidden" name="donationId" value="${don.id}">

                                                                    <div class="p-3 bg-light rounded-3 mb-3 small">
                                                                        <p class="mb-1"><strong>Donor:</strong> <c:out value="${don.donorName}" /> (#DNR-<c:out value="${don.donorId}" />)</p>
                                                                        <p class="mb-1"><strong>Hospital:</strong> <c:out value="${don.hospitalOrCamp}" /></p>
                                                                        <p class="mb-0"><strong>Donation Date:</strong> <c:out value="${don.donationDate}" /> (Units: <c:out value="${don.unitsDonated}" />)</p>
                                                                    </div>

                                                                    <div class="mb-3">
                                                                        <label for="newStatus${don.id}" class="form-label fw-semibold">Verification Status</label>
                                                                        <select class="form-select" id="newStatus${don.id}" name="newStatus" required>
                                                                            <option value="COMPLETED" ${don.status eq 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                                                                            <option value="SCHEDULED" ${don.status eq 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                                                                            <option value="CANCELLED" ${don.status eq 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                                                                        </select>
                                                                    </div>

                                                                    <div class="mb-3">
                                                                        <label for="remarks${don.id}" class="form-label fw-semibold">Clinical Audit Remarks</label>
                                                                        <textarea class="form-control" id="remarks${don.id}" name="remarks" rows="2" 
                                                                                  placeholder="Clinical notes, hemoglobin verified, etc."><c:out value="${don.remarks}" /></textarea>
                                                                    </div>
                                                                </div>
                                                                <div class="modal-footer border-top">
                                                                    <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                    <button type="submit" class="btn btn-primary-red btn-sm">Save Changes</button>
                                                                </div>
                                                            </form>
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>

    <!-- Modal: Record Official Blood Donation (Admin Only) -->
    <c:if test="${sessionScope.userRole eq 'ADMIN'}">
        <div class="modal fade text-start" id="newDonationModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content" style="border-radius: var(--radius-lg);">
                    <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                        <h5 class="modal-title fw-bold text-danger">
                            <i class="bi bi-journal-plus me-2"></i>Record Official Blood Donation
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form action="${pageContext.request.contextPath}/donation-history" method="post" class="needs-validation" novalidate>
                        <div class="modal-body">
                            <input type="hidden" name="action" value="createDonation">

                            <div class="mb-3">
                                <label for="donorId" class="form-label fw-semibold">Select Donor <span class="text-danger">*</span></label>
                                <select class="form-select" id="donorId" name="donorId" required>
                                    <option value="">-- Choose Registered Donor --</option>
                                    <c:forEach var="d" items="${allDonors}">
                                        <option value="${d.id}">#DNR-${d.id} - ${d.fullName} (${d.bloodGroup}, ${d.city})</option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">Please select a donor.</div>
                            </div>

                            <div class="mb-3">
                                <label for="requestId" class="form-label fw-semibold">Related Blood Request (Optional)</label>
                                <select class="form-select" id="requestId" name="requestId">
                                    <option value="">-- Voluntary Camp / No Specific Request --</option>
                                    <c:forEach var="req" items="${allRequests}">
                                        <option value="${req.id}">#REQ-${req.id} - Patient: ${req.patientName} (${req.bloodGroup}, ${req.hospitalName})</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="row g-3 mb-3">
                                <div class="col-md-6">
                                    <label for="unitsDonated" class="form-label fw-semibold">Units Donated <span class="text-danger">*</span></label>
                                    <input type="number" class="form-control" id="unitsDonated" name="unitsDonated" value="1" min="1" max="5" required>
                                </div>
                                <div class="col-md-6">
                                    <label for="donationDate" class="form-label fw-semibold">Donation Date <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="donationDate" name="donationDate" required>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="hospitalOrCamp" class="form-label fw-semibold">Hospital / Blood Bank / Camp <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="hospitalOrCamp" name="hospitalOrCamp" 
                                       placeholder="e.g. Manipal Blood Bank or Red Cross Camp" required>
                            </div>

                            <div class="mb-3">
                                <label for="status" class="form-label fw-semibold">Verification Status</label>
                                <select class="form-select" id="status" name="status" required>
                                    <option value="COMPLETED" selected>COMPLETED</option>
                                    <option value="SCHEDULED">SCHEDULED</option>
                                    <option value="CANCELLED">CANCELLED</option>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label for="remarks" class="form-label fw-semibold">Clinical Audit Remarks</label>
                                <textarea class="form-control" id="remarks" name="remarks" rows="2" 
                                          placeholder="Routine voluntary whole blood donation. Pre-donation screening verified.">Routine voluntary whole blood donation. Pre-donation screening verified.</textarea>
                            </div>
                        </div>
                        <div class="modal-footer border-top">
                            <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Cancel</button>
                            <button type="submit" class="btn btn-primary-red btn-sm">Record &amp; Verify Donation</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script>
            // Set default date to today
            const donDateInput = document.getElementById('donationDate');
            if (donDateInput) {
                donDateInput.value = new Date().toISOString().split('T')[0];
            }
        </script>
    </c:if>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
