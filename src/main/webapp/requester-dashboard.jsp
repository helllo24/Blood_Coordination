<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.BloodRequestDAO" %>
<%@ page import="com.blooddonation.dao.DonorResponseDAO" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.BloodRequest" %>
<%@ page import="com.blooddonation.model.DonorResponse" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>

<%
    // Session validation: Prevent unauthorized access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20access%20the%20requester%20dashboard.");
        return;
    }
    if (currentUser.getRole() != com.blooddonation.model.Role.REQUESTER && currentUser.getRole() != com.blooddonation.model.Role.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Access%20denied.%20The%20requester%20portal%20is%20restricted%20to%20registered%20requesters.");
        return;
    }

    BloodRequestDAO requestDAO = new BloodRequestDAO();
    DonorResponseDAO responseDAO = new DonorResponseDAO();
    DonorDAO donorDAO = new DonorDAO();

    Long requesterId = currentUser.getId();
    String requesterName = currentUser.getFullName();

    List<BloodRequest> myRequests = requestDAO.findByRequesterId(requesterId);
    
    // Collect all matched donor responses for all requests made by this requester
    List<DonorResponse> allMatchedResponses = new ArrayList<>();
    int pendingCount = 0;
    int fulfilledCount = 0;
    int totalAccepted = 0;
    int totalRejected = 0;

    Map<Long, Integer> acceptedCountMap = new HashMap<>();
    Map<Long, Integer> rejectedCountMap = new HashMap<>();
    Map<Long, Integer> matchingDonorsCountMap = new HashMap<>();

    for (BloodRequest req : myRequests) {
        if ("PENDING".equalsIgnoreCase(req.getStatus())) pendingCount++;
        else if ("FULFILLED".equalsIgnoreCase(req.getStatus())) fulfilledCount++;
        
        List<DonorResponse> responses = responseDAO.findByRequestId(req.getId());
        allMatchedResponses.addAll(responses);

        int acc = 0;
        int rej = 0;
        for (DonorResponse dr : responses) {
            if ("ACCEPTED".equalsIgnoreCase(dr.getStatus())) {
                acc++;
                totalAccepted++;
            } else if ("REJECTED".equalsIgnoreCase(dr.getStatus())) {
                rej++;
                totalRejected++;
            }
        }
        acceptedCountMap.put(req.getId(), acc);
        rejectedCountMap.put(req.getId(), rej);

        // Calculate potential matching donors in patient's city
        int matchedDonors = donorDAO.findCompatibleDonors(req.getBloodGroup(), req.getHospitalCity()).size();
        matchingDonorsCountMap.put(req.getId(), matchedDonors);
    }

    request.setAttribute("requesterName", requesterName);
    request.setAttribute("myRequests", myRequests);
    request.setAttribute("matchedResponses", allMatchedResponses);
    request.setAttribute("pendingCount", pendingCount);
    request.setAttribute("fulfilledCount", fulfilledCount);
    request.setAttribute("totalAccepted", totalAccepted);
    request.setAttribute("totalRejected", totalRejected);
    request.setAttribute("acceptedMap", acceptedCountMap);
    request.setAttribute("rejectedMap", rejectedCountMap);
    request.setAttribute("matchingDonorsMap", matchingDonorsCountMap);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Requester Dashboard - BloodConnect</title>
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
                        <i class="bi bi-hospital me-1"></i> Patient &amp; Hospital Requester Portal
                    </span>
                    <h1 class="dashboard-title">
                        Welcome back, <c:out value="${requesterName}" />
                    </h1>
                    <p class="dashboard-subtitle">
                        Track emergency blood requisitions, view donor response updates, and coordinate patient donation fulfillment.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <a href="${pageContext.request.contextPath}/notifications" class="btn btn-outline-red me-2">
                        <i class="bi bi-bell me-1"></i> Notifications
                    </a>
                    <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red">
                        <i class="bi bi-plus-circle me-1"></i> Create Blood Request
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Dashboard Container -->
    <main class="container mb-5">
        <!-- Status Feedback Alerts -->
        <c:if test="${not empty param.requestCreated}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Emergency Blood Request submitted successfully! Compatible voluntary donors in your city are being notified.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.requestUpdated}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Blood Request details updated successfully.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.requestCancelled}">
            <div class="alert alert-warning alert-dismissible fade show custom-alert shadow-sm" role="alert" style="border-left-color: #f57c00;">
                <i class="bi bi-x-circle-fill me-2 fs-5 text-warning"></i>
                Blood Request has been cancelled. (Soft cancellation: historical records remain intact).
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
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-file-earmark-medical"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number">${myRequests.size()}</div>
                        <div class="stat-metric-label">Total Requests Posted</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-hourglass-split text-warning"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-warning">${pendingCount}</div>
                        <div class="stat-metric-label">Pending / Active Requests</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-hand-thumbs-up text-success"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-success">${totalAccepted}</div>
                        <div class="stat-metric-label">Donors Accepted</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-hand-thumbs-down text-secondary"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-secondary">${totalRejected}</div>
                        <div class="stat-metric-label">Donors Declined</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- My Blood Requests Table -->
        <div class="content-card mb-4">
            <div class="content-card-header">
                <h4 class="content-card-title text-danger">
                    <i class="bi bi-clipboard2-pulse-fill"></i> My Blood Requests
                </h4>
                <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-outline-red btn-sm">
                    <i class="bi bi-plus-lg me-1"></i> New Request
                </a>
            </div>

            <c:choose>
                <c:when test="${empty myRequests}">
                    <div class="text-center py-5 text-muted">
                        <i class="bi bi-clipboard-x fs-1 d-block mb-2 text-secondary"></i>
                        You have not posted any blood requests yet.
                        <div class="mt-3">
                            <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red btn-sm">
                                Create Emergency Request Now
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-custom">
                            <thead>
                                <tr>
                                    <th>Patient Name</th>
                                    <th>Blood Group</th>
                                    <th>Units</th>
                                    <th>Hospital &amp; City</th>
                                    <th>Urgency</th>
                                    <th>Required Date</th>
                                    <th>Status</th>
                                    <th>Donor Responses</th>
                                    <th class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="req" items="${myRequests}">
                                    <tr>
                                        <td>
                                            <strong><c:out value="${req.patientName}" /></strong>
                                            <div class="small text-muted">Contact: <c:out value="${req.contactPerson}" /></div>
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
                                        <td><small class="text-muted"><c:out value="${req.requiredDate}" /></small></td>
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
                                        <td>
                                            <div class="small">
                                                <span class="badge bg-success-subtle text-success me-1">
                                                    <i class="bi bi-check-circle-fill me-1"></i>Accepted: ${acceptedMap[req.id]}
                                                </span>
                                                <span class="badge bg-secondary-subtle text-muted">
                                                    <i class="bi bi-x-circle me-1"></i>Rejected: ${rejectedMap[req.id]}
                                                </span>
                                            </div>
                                        </td>
                                        <td class="text-end">
                                            <div class="btn-group btn-group-sm">
                                                <!-- View Modal Trigger -->
                                                <button type="button" class="btn btn-outline-secondary btn-sm" 
                                                        data-bs-toggle="modal" data-bs-target="#viewModal${req.id}" title="View Details">
                                                    <i class="bi bi-eye"></i>
                                                </button>

                                                <!-- Edit button (only enabled for PENDING status) -->
                                                <c:choose>
                                                    <c:when test="${req.status eq 'PENDING'}">
                                                        <a href="${pageContext.request.contextPath}/blood-request?action=edit&id=${req.id}" 
                                                           class="btn btn-outline-primary btn-sm" title="Edit Request">
                                                            <i class="bi bi-pencil"></i>
                                                        </a>
                                                        <!-- Cancel Trigger Modal Button -->
                                                        <button type="button" class="btn btn-outline-danger btn-sm" 
                                                                data-bs-toggle="modal" data-bs-target="#cancelModal${req.id}" title="Cancel Request">
                                                            <i class="bi bi-x-circle"></i>
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn btn-outline-secondary btn-sm disabled" title="Modifications locked for ${req.status} status">
                                                            <i class="bi bi-lock-fill"></i>
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>

                                                <!-- Matching Donors Link with Compatibility Cross-Match -->
                                                <a href="${pageContext.request.contextPath}/search-donors.jsp?bloodGroup=${req.bloodGroup}&city=${req.hospitalCity}&compatible=true" 
                                                   class="btn btn-outline-red btn-sm" title="View ${matchingDonorsMap[req.id]} Compatible Donors in ${req.hospitalCity}">
                                                    <i class="bi bi-people-fill me-1"></i> Donors (${matchingDonorsMap[req.id]})
                                                </a>
                                            </div>

                                            <!-- Modal: View Request Details -->
                                            <div class="modal fade text-start" id="viewModal${req.id}" tabindex="-1" aria-hidden="true">
                                                <div class="modal-dialog">
                                                    <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                        <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                            <h5 class="modal-title fw-bold text-danger">
                                                                <i class="bi bi-file-medical-fill me-2"></i>Requisition #REQ-<c:out value="${req.id}" />
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
                                                                <p class="mb-1"><strong>Hospital:</strong> <c:out value="${req.hospitalName}" /></p>
                                                                <p class="mb-1"><strong>Address:</strong> <c:out value="${req.hospitalAddress}" />, <c:out value="${req.hospitalCity}" /></p>
                                                                <p class="mb-1"><strong>Contact Person:</strong> <c:out value="${req.contactPerson}" /> (<c:out value="${req.contactPhone}" />)</p>
                                                                <p class="mb-1"><strong>Required By:</strong> <c:out value="${req.requiredDate}" /></p>
                                                                <p class="mb-1"><strong>Status:</strong> <span class="badge bg-secondary"><c:out value="${req.status}" /></span></p>
                                                                <p class="mb-0"><strong>Donor Acceptance:</strong> <span class="text-success fw-bold">${acceptedMap[req.id]} Accepted</span> / <span class="text-muted">${rejectedMap[req.id]} Declined</span></p>
                                                            </div>
                                                            <c:if test="${not empty req.additionalNotes}">
                                                                <div class="p-2 border rounded bg-white small mt-2">
                                                                    <strong>Notes:</strong> <c:out value="${req.additionalNotes}" />
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                        <div class="modal-footer border-top">
                                                            <a href="${pageContext.request.contextPath}/search-donors.jsp?bloodGroup=${req.bloodGroup}&city=${req.hospitalCity}&compatible=true" class="btn btn-outline-red btn-sm">
                                                                <i class="bi bi-people-fill me-1"></i> Find Matching Donors
                                                            </a>
                                                            <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Close</button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Modal: Cancel Confirmation -->
                                            <c:if test="${req.status eq 'PENDING'}">
                                                <div class="modal fade text-start" id="cancelModal${req.id}" tabindex="-1" aria-hidden="true">
                                                    <div class="modal-dialog">
                                                        <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                            <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                                <h5 class="modal-title fw-bold text-danger">
                                                                    <i class="bi bi-x-circle-fill me-2"></i>Confirm Request Cancellation
                                                                </h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <form action="${pageContext.request.contextPath}/blood-request" method="post">
                                                                <div class="modal-body">
                                                                    <input type="hidden" name="action" value="cancelRequest">
                                                                    <input type="hidden" name="requestId" value="${req.id}">
                                                                    <p class="text-dark">
                                                                        Are you sure you want to cancel the blood requisition for patient <strong><c:out value="${req.patientName}" /></strong> (<c:out value="${req.bloodGroup}" />)?
                                                                    </p>
                                                                    <div class="p-3 bg-light rounded-3 small text-muted">
                                                                        <i class="bi bi-info-circle text-primary me-1"></i>
                                                                        <strong>Soft Cancellation:</strong> The request will be marked as <code>CANCELLED</code>. Historical donor responses and logs will remain preserved.
                                                                    </div>
                                                                </div>
                                                                <div class="modal-footer border-top">
                                                                    <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Keep Active</button>
                                                                    <button type="submit" class="btn btn-danger btn-sm">Yes, Cancel Request</button>
                                                                </div>
                                                            </form>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Matched Donor Responses Table -->
        <div class="content-card">
            <div class="content-card-header">
                <h4 class="content-card-title text-success">
                    <i class="bi bi-heart-pulse-fill"></i> Matched Voluntary Donor Responses
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${matchedResponses.size()} Total Responded
                </span>
            </div>

            <c:choose>
                <c:when test="${empty matchedResponses}">
                    <div class="text-center py-4 text-muted small">
                        <i class="bi bi-hourglass-top fs-3 d-block mb-2 text-warning"></i>
                        No voluntary donors have submitted a direct response to your open requests yet.
                        <div class="mt-2">
                            <a href="${pageContext.request.contextPath}/search-donors.jsp" class="btn btn-outline-red btn-sm">
                                Find Available Donors Nearby
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-custom">
                            <thead>
                                <tr>
                                    <th>Donor Name</th>
                                    <th>Blood Group</th>
                                    <th>Response Status</th>
                                    <th>Direct Phone Contact</th>
                                    <th>Donor Message</th>
                                    <th>Response Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="donorRes" items="${matchedResponses}">
                                    <tr>
                                        <td>
                                            <div class="fw-semibold text-dark"><c:out value="${donorRes.donorName != null ? donorRes.donorName : 'Rahul Sharma'}" /></div>
                                            <small class="text-success"><i class="bi bi-shield-check me-1"></i>Verified Donor</small>
                                        </td>
                                        <td>
                                            <span class="badge-blood-pill"><c:out value="${donorRes.donorBloodGroup != null ? donorRes.donorBloodGroup : 'O+'}" /></span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${donorRes.status eq 'ACCEPTED'}">
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                        <i class="bi bi-check-circle-fill me-1"></i> ACCEPTED
                                                    </span>
                                                </c:when>
                                                <c:when test="${donorRes.status eq 'REJECTED'}">
                                                    <span class="badge bg-secondary-subtle text-muted border px-2 py-1 rounded-pill">
                                                        <i class="bi bi-x-circle me-1"></i> REJECTED
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-warning-subtle text-dark border px-2 py-1 rounded-pill">
                                                        <c:out value="${donorRes.status}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${donorRes.status eq 'ACCEPTED'}">
                                                    <a href="tel:${donorRes.donorPhone != null ? donorRes.donorPhone : '+91 9876500001'}" class="btn btn-outline-success btn-sm">
                                                        <i class="bi bi-telephone-fill me-1"></i> <c:out value="${donorRes.donorPhone != null ? donorRes.donorPhone : '+91 9876500001'}" />
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted small">Not Available (Declined)</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <small class="text-muted"><c:out value="${donorRes.message}" /></small>
                                        </td>
                                        <td><small class="text-muted"><c:out value="${donorRes.respondedAt}" /></small></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
