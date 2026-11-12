<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.dao.BloodRequestDAO" %>
<%@ page import="com.blooddonation.dao.DonorResponseDAO" %>
<%@ page import="com.blooddonation.dao.DonationDAO" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.Donor" %>
<%@ page import="com.blooddonation.model.BloodRequest" %>
<%@ page import="com.blooddonation.model.DonorResponse" %>
<%@ page import="com.blooddonation.model.Donation" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Optional" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>

<%
    // Session validation: Prevent unauthorized access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20access%20the%20donor%20dashboard.");
        return;
    }
    if (currentUser.getRole() != com.blooddonation.model.Role.DONOR && currentUser.getRole() != com.blooddonation.model.Role.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Access%20denied.%20The%20donor%20portal%20is%20restricted%20to%20registered%20donors.");
        return;
    }

    DonorDAO donorDAO = new DonorDAO();
    BloodRequestDAO requestDAO = new BloodRequestDAO();
    DonorResponseDAO responseDAO = new DonorResponseDAO();
    DonationDAO donationDAO = new DonationDAO();

    Donor donorProfile = null;
    Optional<Donor> opt = donorDAO.findByUserId(currentUser.getId());
    if (opt.isPresent()) {
        donorProfile = opt.get();
    } else {
        // Fallback for Admin testing
        donorProfile = donorDAO.findById(1L).orElse(new Donor());
    }

    // Load available open blood requests, donor responses and donations
    List<BloodRequest> availableRequests = requestDAO.findByStatus("PENDING");
    List<DonorResponse> myResponses = responseDAO.findByDonorId(donorProfile.getId());
    List<Donation> myDonations = donationDAO.findByDonorId(donorProfile.getId());
    int totalUnitsDonated = donationDAO.getTotalUnitsDonatedByDonor(donorProfile.getId());

    // Map of request IDs to the status responded by this donor
    Map<Long, String> respondedMap = new HashMap<>();
    for (DonorResponse dr : myResponses) {
        respondedMap.put(dr.getRequestId(), dr.getStatus());
    }

    request.setAttribute("donor", donorProfile);
    request.setAttribute("availableRequests", availableRequests);
    request.setAttribute("myResponses", myResponses);
    request.setAttribute("myDonations", myDonations);
    request.setAttribute("totalUnitsDonated", totalUnitsDonated);
    request.setAttribute("respondedMap", respondedMap);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Donor Dashboard - BloodConnect</title>
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
                        <i class="bi bi-heart-pulse-fill me-1"></i> Voluntary Blood Donor Portal
                    </span>
                    <h1 class="dashboard-title">
                        Welcome back, <c:out value="${not empty sessionScope.userName ? sessionScope.userName : 'Rahul Sharma'}" />
                    </h1>
                    <p class="dashboard-subtitle">
                        Track emergency blood requests, manage your emergency readiness, accept requests, and review donation history.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <a href="${pageContext.request.contextPath}/notifications" class="btn btn-outline-red me-2">
                        <i class="bi bi-bell me-1"></i> Notifications
                    </a>
                    <a href="${pageContext.request.contextPath}/donor-profile.jsp" class="btn btn-outline-red me-2">
                        <i class="bi bi-person-gear me-1"></i> Edit Profile
                    </a>
                    <a href="${pageContext.request.contextPath}/search-donors.jsp" class="btn btn-primary-red">
                        <i class="bi bi-search me-1"></i> Search Donors
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Dashboard Container -->
    <main class="container mb-5">
        <!-- Quick Feedback Notifications -->
        <c:if test="${not empty param.responseAccepted}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Thank you! Your acceptance has been submitted to the requester. They have been notified immediately with your contact verification.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.responseRejected}">
            <div class="alert alert-secondary alert-dismissible fade show custom-alert shadow-sm" role="alert">
                <i class="bi bi-dash-circle-fill me-2 fs-5 text-secondary"></i>
                You have declined this blood request. Your preference has been safely recorded.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.statusUpdated}">
            <div class="alert alert-info alert-dismissible fade show custom-alert shadow-sm" role="alert">
                <i class="bi bi-info-circle-fill me-2 fs-5 text-info"></i>
                Your emergency availability status has been updated successfully.
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
                        <i class="bi bi-droplet-fill"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">
                            <c:out value="${donor.bloodGroup != null ? donor.bloodGroup : 'O+'}" />
                        </div>
                        <div class="stat-metric-label">Registered Blood Group</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-suit-heart-fill"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number">
                            <c:out value="${donor.totalDonations}" /> <small class="fs-6 text-muted">(${totalUnitsDonated} Units)</small>
                        </div>
                        <div class="stat-metric-label">Total Donations Made</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-bell-fill"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-primary">
                            ${availableRequests.size()}
                        </div>
                        <div class="stat-metric-label">Active Blood Requests</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-activity"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number ${donor.available ? 'text-success' : 'text-muted'}">
                            <c:choose>
                                <c:when test="${donor.available}">Available</c:when>
                                <c:otherwise>Busy</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="stat-metric-label">Emergency Readiness</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-4">
            <!-- Left Column: Available Blood Requests -->
            <div class="col-lg-8">
                <!-- Available Emergency Requests -->
                <div class="content-card mb-4">
                    <div class="content-card-header">
                        <h4 class="content-card-title text-danger">
                            <i class="bi bi-exclamation-diamond-fill"></i> Active Emergency Blood Requests
                        </h4>
                        <span class="badge bg-light text-muted border px-2 py-1">
                            ${availableRequests.size()} Open Requisitions
                        </span>
                    </div>

                    <c:choose>
                        <c:when test="${empty availableRequests}">
                            <div class="text-center py-4 text-muted">
                                <i class="bi bi-check-circle fs-2 text-success d-block mb-2"></i>
                                No critical blood requests currently pending in your location. All requirements are currently met!
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-custom">
                                    <thead>
                                        <tr>
                                            <th>Patient / Hospital</th>
                                            <th>Group</th>
                                            <th>Units</th>
                                            <th>Urgency</th>
                                            <th>Required By</th>
                                            <th class="text-end">Respond</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="req" items="${availableRequests}">
                                            <tr>
                                                <td>
                                                    <div class="fw-semibold text-dark"><c:out value="${req.patientName}" /></div>
                                                    <small class="text-muted"><i class="bi bi-hospital me-1"></i><c:out value="${req.hospitalName}" />, <c:out value="${req.hospitalCity}" /></small>
                                                </td>
                                                <td>
                                                    <span class="badge-blood-pill"><c:out value="${req.bloodGroup}" /></span>
                                                </td>
                                                <td><c:out value="${req.unitsNeeded}" /> Unit(s)</td>
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
                                                <td class="text-end">
                                                    <c:choose>
                                                        <c:when test="${not empty respondedMap[req.id]}">
                                                            <c:set var="userStatus" value="${respondedMap[req.id]}" />
                                                            <span class="badge ${userStatus eq 'ACCEPTED' ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-muted border'} px-2 py-1 rounded-pill">
                                                                <i class="bi ${userStatus eq 'ACCEPTED' ? 'bi-check-circle-fill' : 'bi-x-circle-fill'} me-1"></i>
                                                                Responded (${userStatus})
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="btn-group btn-group-sm">
                                                                <!-- Accept Button Modal Trigger -->
                                                                <button type="button" class="btn btn-primary-red btn-sm" 
                                                                        data-bs-toggle="modal" data-bs-target="#acceptModal${req.id}" title="Accept and Donate">
                                                                    <i class="bi bi-hand-thumbs-up me-1"></i> Accept
                                                                </button>
                                                                <!-- Reject Button Modal Trigger -->
                                                                <button type="button" class="btn btn-outline-secondary btn-sm" 
                                                                        data-bs-toggle="modal" data-bs-target="#rejectModal${req.id}" title="Decline Request">
                                                                    <i class="bi bi-x-lg"></i>
                                                                </button>
                                                            </div>

                                                            <!-- Modal for ACCEPTING request -->
                                                            <div class="modal fade text-start" id="acceptModal${req.id}" tabindex="-1" aria-hidden="true">
                                                                <div class="modal-dialog">
                                                                    <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                                        <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                                            <h5 class="modal-title fw-bold text-danger">
                                                                                <i class="bi bi-suit-heart-fill me-2"></i>Accept Voluntary Donation
                                                                            </h5>
                                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                        </div>
                                                                        <form action="${pageContext.request.contextPath}/donor-response" method="post">
                                                                            <div class="modal-body">
                                                                                <input type="hidden" name="action" value="accept">
                                                                                <input type="hidden" name="status" value="ACCEPTED">
                                                                                <input type="hidden" name="requestId" value="${req.id}">
                                                                                <input type="hidden" name="donorId" value="${donor.id}">
                                                                                
                                                                                <div class="p-3 bg-light rounded-3 mb-3">
                                                                                    <p class="mb-1"><strong>Patient:</strong> <c:out value="${req.patientName}" /></p>
                                                                                    <p class="mb-1"><strong>Blood Group:</strong> <span class="badge bg-danger"><c:out value="${req.bloodGroup}" /></span></p>
                                                                                    <p class="mb-1"><strong>Hospital:</strong> <c:out value="${req.hospitalName}" />, <c:out value="${req.hospitalCity}" /></p>
                                                                                    <p class="mb-0"><strong>Contact Person:</strong> <c:out value="${req.contactPerson}" /> (<c:out value="${req.contactPhone}" />)</p>
                                                                                </div>

                                                                                <div class="mb-3">
                                                                                    <label class="form-label fw-semibold">Message / Estimated Arrival Time (Optional)</label>
                                                                                    <textarea class="form-control" name="message" rows="2" 
                                                                                              placeholder="e.g. I am available and can reach the hospital blood bank tomorrow at 10 AM.">I am available to donate blood for this requirement.</textarea>
                                                                                </div>
                                                                            </div>
                                                                            <div class="modal-footer border-top">
                                                                                <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                                <button type="submit" class="btn btn-primary-red btn-sm">Confirm &amp; Alert Requester</button>
                                                                            </div>
                                                                        </form>
                                                                    </div>
                                                                </div>
                                                            </div>

                                                            <!-- Modal for REJECTING request -->
                                                            <div class="modal fade text-start" id="rejectModal${req.id}" tabindex="-1" aria-hidden="true">
                                                                <div class="modal-dialog">
                                                                    <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                                        <div class="modal-header border-bottom">
                                                                            <h5 class="modal-title fw-bold text-dark">
                                                                                <i class="bi bi-x-circle me-2 text-secondary"></i>Decline Blood Request
                                                                            </h5>
                                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                        </div>
                                                                        <form action="${pageContext.request.contextPath}/donor-response" method="post">
                                                                            <div class="modal-body">
                                                                                <input type="hidden" name="action" value="reject">
                                                                                <input type="hidden" name="status" value="REJECTED">
                                                                                <input type="hidden" name="requestId" value="${req.id}">
                                                                                <input type="hidden" name="donorId" value="${donor.id}">
                                                                                
                                                                                <p class="small text-muted">
                                                                                    Are you unable to donate for patient <strong><c:out value="${req.patientName}" /></strong> (<c:out value="${req.bloodGroup}" />)?
                                                                                </p>

                                                                                <div class="mb-3">
                                                                                    <label class="form-label small fw-semibold">Reason for Declining (Optional)</label>
                                                                                    <textarea class="form-control" name="message" rows="2" 
                                                                                              placeholder="e.g. Currently traveling or recently donated.">Currently unavailable to donate for this schedule.</textarea>
                                                                                </div>
                                                                            </div>
                                                                            <div class="modal-footer border-top">
                                                                                <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Back</button>
                                                                                <button type="submit" class="btn btn-outline-danger btn-sm">Confirm Decline</button>
                                                                            </div>
                                                                        </form>
                                                                    </div>
                                                                </div>
                                                            </div>
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

                <!-- My Recent Responses -->
                <div class="content-card">
                    <div class="content-card-header">
                        <h4 class="content-card-title text-secondary">
                            <i class="bi bi-chat-heart-fill"></i> My Submitted Responses
                        </h4>
                        <span class="badge bg-light text-muted border px-2 py-1">
                            ${myResponses.size()} Response(s)
                        </span>
                    </div>

                    <c:choose>
                        <c:when test="${empty myResponses}">
                            <div class="text-center py-4 text-muted small">
                                You haven't submitted any responses yet. Check the open requests list above.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-custom">
                                    <thead>
                                        <tr>
                                            <th>Patient / Requisition</th>
                                            <th>Response Status</th>
                                            <th>Message</th>
                                            <th>Responded On</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="res" items="${myResponses}">
                                            <tr>
                                                <td>
                                                    <strong><c:out value="${res.patientName != null ? res.patientName : 'Emergency Patient'}" /></strong>
                                                    <div class="small text-muted"><c:out value="${res.hospitalName != null ? res.hospitalName : 'Hospital Facility'}" /></div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${res.status eq 'ACCEPTED'}">
                                                            <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill">
                                                                <i class="bi bi-check-circle-fill me-1"></i> ACCEPTED
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${res.status eq 'REJECTED'}">
                                                            <span class="badge bg-secondary-subtle text-muted border px-2 py-1 rounded-pill">
                                                                <i class="bi bi-x-circle me-1"></i> REJECTED
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-warning-subtle text-dark border px-2 py-1 rounded-pill">
                                                                <c:out value="${res.status}" />
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td><small class="text-muted"><c:out value="${res.message}" /></small></td>
                                                <td><small class="text-muted"><c:out value="${res.respondedAt}" /></small></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Right Column: Profile Summary & Availability -->
            <div class="col-lg-4">
                <!-- Donor Profile Card -->
                <div class="content-card mb-4">
                    <div class="text-center pb-3 border-bottom mb-3">
                        <div class="blood-type-badge mb-2">
                            <c:out value="${donor.bloodGroup != null ? donor.bloodGroup : 'O+'}" />
                        </div>
                        <h5 class="fw-bold mb-1"><c:out value="${not empty sessionScope.userName ? sessionScope.userName : 'Rahul Sharma'}" /></h5>
                        <p class="text-muted small mb-0"><i class="bi bi-geo-alt-fill text-danger me-1"></i><c:out value="${donor.city != null ? donor.city : 'Bangalore'}" />, <c:out value="${donor.state != null ? donor.state : 'Karnataka'}" /></p>
                    </div>

                    <div class="mb-3">
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Donor Age / Gender:</span>
                            <span class="fw-semibold"><c:out value="${donor.age}" /> yrs / <c:out value="${donor.gender}" /></span>
                        </div>
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Contact Phone:</span>
                            <span class="fw-semibold"><c:out value="${not empty donor.phone ? donor.phone : '+91 9876500001'}" /></span>
                        </div>
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Last Donation Date:</span>
                            <span class="fw-semibold"><c:out value="${donor.lastDonationDate != null ? donor.lastDonationDate : 'No prior record'}" /></span>
                        </div>
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Lifetime Donations:</span>
                            <span class="fw-bold text-danger"><c:out value="${donor.totalDonations}" /> Completed</span>
                        </div>
                        <div class="d-flex justify-content-between small py-1">
                            <span class="text-muted">Total Units Donated:</span>
                            <span class="fw-bold text-success"><c:out value="${totalUnitsDonated}" /> Unit(s)</span>
                        </div>
                    </div>

                    <!-- Quick Availability Status Toggle -->
                    <div class="p-3 bg-light rounded-3 border">
                        <h6 class="fw-bold mb-2 small text-uppercase text-secondary">
                            <i class="bi bi-toggle-on me-1"></i> Emergency Readiness Toggle
                        </h6>
                        <form action="${pageContext.request.contextPath}/donor-profile" method="post">
                            <input type="hidden" name="action" value="toggleAvailability">
                            <input type="hidden" name="donorId" value="${donor.id}">
                            <input type="hidden" name="currentStatus" value="${donor.available}">

                            <div class="d-flex align-items-center justify-content-between">
                                <span class="small ${donor.available ? 'text-success fw-bold' : 'text-muted'}">
                                    <i class="bi ${donor.available ? 'bi-check-circle-fill' : 'bi-dash-circle'} me-1"></i>
                                    ${donor.available ? 'Ready for Calls' : 'Currently Inactive'}
                                </span>
                                <button type="submit" class="btn btn-sm ${donor.available ? 'btn-outline-secondary' : 'btn-primary-red'}">
                                    ${donor.available ? 'Set Unavailable' : 'Set Available'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- My Donation History Quick Card -->
                <div class="content-card">
                    <div class="content-card-header">
                        <h5 class="content-card-title">
                            <i class="bi bi-clock-history text-danger"></i> Past Donations
                        </h5>
                        <a href="${pageContext.request.contextPath}/donation-history.jsp" class="small text-danger">View All</a>
                    </div>

                    <c:choose>
                        <c:when test="${empty myDonations}">
                            <p class="small text-muted mb-0">No past donation records logged yet.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="list-group list-group-flush small">
                                <c:forEach var="don" items="${myDonations}">
                                    <li class="list-group-item px-0 py-2 border-bottom">
                                        <div class="d-flex justify-content-between">
                                            <strong><c:out value="${don.hospitalOrCamp}" /></strong>
                                            <span class="badge bg-success-subtle text-success"><c:out value="${don.status}" /></span>
                                        </div>
                                        <div class="text-muted small">
                                            <i class="bi bi-calendar3 me-1"></i><c:out value="${don.donationDate}" /> • <c:out value="${don.unitsDonated}" /> Unit
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
