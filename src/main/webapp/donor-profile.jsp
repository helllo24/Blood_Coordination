<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.dao.UserDAO" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.Donor" %>
<%@ page import="java.util.Optional" %>

<%
    // Session validation: Prevent unauthorized access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20your%20donor%20profile.");
        return;
    }
    if (currentUser.getRole() != com.blooddonation.model.Role.DONOR && currentUser.getRole() != com.blooddonation.model.Role.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Access%20denied.%20Donor%20privileges%20required.");
        return;
    }

    // If attributes not already set by DonorProfileServlet, populate them
    Donor donorProfile = (Donor) request.getAttribute("donor");
    User userAccount = (User) request.getAttribute("accountUser");

    if (donorProfile == null) {
        DonorDAO donorDAO = new DonorDAO();
        Optional<Donor> opt = donorDAO.findByUserId(currentUser.getId());
        if (opt.isPresent()) {
            donorProfile = opt.get();
        } else {
            donorProfile = donorDAO.findById(1L).orElse(new Donor());
            donorProfile.setUserId(currentUser.getId());
        }
        request.setAttribute("donor", donorProfile);
    }

    if (userAccount == null) {
        userAccount = currentUser;
        request.setAttribute("accountUser", userAccount);
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Donor Profile - BloodConnect</title>
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
                        <i class="bi bi-person-badge-fill me-1"></i> Voluntary Medical Registry
                    </span>
                    <h1 class="dashboard-title">
                        My Donor Profile
                    </h1>
                    <p class="dashboard-subtitle">
                        Review and maintain your verified contact details, clinical blood group, and emergency readiness.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <a href="${pageContext.request.contextPath}/donor-dashboard.jsp" class="btn btn-outline-red">
                        <i class="bi bi-speedometer2 me-1"></i> Back to Dashboard
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Content Container -->
    <main class="container mb-5">
        <!-- Status Feedback Alerts -->
        <c:if test="${not empty param.updated}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Your donor profile and location preferences were successfully saved!
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.statusUpdated}">
            <div class="alert alert-info alert-dismissible fade show custom-alert shadow-sm" role="alert">
                <i class="bi bi-info-circle-fill me-2 fs-5 text-info"></i>
                Your emergency readiness availability status has been updated.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.deactivated}">
            <div class="alert alert-warning alert-dismissible fade show custom-alert shadow-sm" role="alert" style="border-left-color: #f57c00;">
                <i class="bi bi-pause-circle-fill me-2 fs-5 text-warning"></i>
                Your donor profile has been deactivated (availability paused). Your donation history remains safely preserved.
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

        <div class="row g-4">
            <!-- Left Column: Donor Badge & Quick Stats -->
            <div class="col-lg-4">
                <div class="content-card text-center mb-4">
                    <div class="blood-type-badge mb-3" style="width: 76px; height: 76px; font-size: 1.8rem;">
                        <c:out value="${donor.bloodGroup}" />
                    </div>
                    <h4 class="fw-bold mb-1"><c:out value="${accountUser.fullName}" /></h4>
                    <p class="text-muted small mb-3">
                        <i class="bi bi-shield-check text-success me-1"></i>Verified Voluntary Donor
                    </p>

                    <div class="d-flex justify-content-center gap-2 mb-3">
                        <span class="badge ${donor.available ? 'bg-success' : 'bg-secondary'} px-3 py-2 rounded-pill">
                            <i class="bi ${donor.available ? 'bi-check-circle-fill' : 'bi-pause-circle-fill'} me-1"></i>
                            ${donor.available ? 'Available for Emergency' : 'Currently Unavailable'}
                        </span>
                    </div>

                    <div class="p-3 bg-light rounded-3 text-start mb-3">
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Total Contributions:</span>
                            <span class="fw-bold text-danger"><c:out value="${donor.totalDonations}" /> Completed</span>
                        </div>
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Last Donation Date:</span>
                            <span class="fw-semibold"><c:out value="${donor.lastDonationDate != null ? donor.lastDonationDate : 'No prior donations'}" /></span>
                        </div>
                        <div class="d-flex justify-content-between small py-1 border-bottom">
                            <span class="text-muted">Registered Role:</span>
                            <span class="fw-semibold text-dark"><c:out value="${accountUser.role}" /></span>
                        </div>
                        <div class="d-flex justify-content-between small py-1">
                            <span class="text-muted">Account Status:</span>
                            <span class="badge bg-success-subtle text-success"><c:out value="${accountUser.status}" /></span>
                        </div>
                    </div>

                    <!-- Quick Availability Switch Form -->
                    <form action="${pageContext.request.contextPath}/donor-profile" method="post" class="mb-2">
                        <input type="hidden" name="action" value="toggleAvailability">
                        <input type="hidden" name="donorId" value="${donor.id}">
                        <input type="hidden" name="currentStatus" value="${donor.available}">
                        <input type="hidden" name="fromProfile" value="true">
                        <button type="submit" class="btn w-100 ${donor.available ? 'btn-outline-secondary' : 'btn-primary-red'}">
                            <i class="bi bi-power me-1"></i> ${donor.available ? 'Pause Emergency Readiness' : 'Activate Emergency Readiness'}
                        </button>
                    </form>

                    <!-- Soft Deactivation Trigger Modal Button -->
                    <c:if test="${donor.available}">
                        <button type="button" class="btn btn-outline-danger btn-sm w-100" data-bs-toggle="modal" data-bs-target="#deactivateModal">
                            <i class="bi bi-slash-circle me-1"></i> Deactivate Donor Profile
                        </button>
                    </c:if>
                </div>

                <!-- Eligibility Guidelines Card -->
                <div class="content-card">
                    <h5 class="content-card-title text-danger mb-3">
                        <i class="bi bi-info-circle-fill"></i> Health Guidelines
                    </h5>
                    <ul class="list-unstyled small text-muted mb-0">
                        <li class="mb-2"><i class="bi bi-check2 text-danger me-1"></i> Maintain hemoglobin levels &gt; 12.5 g/dL.</li>
                        <li class="mb-2"><i class="bi bi-check2 text-danger me-1"></i> 3 months interval required between whole blood donations.</li>
                        <li class="mb-2"><i class="bi bi-check2 text-danger me-1"></i> Ensure sound sleep and adequate hydration prior to donation.</li>
                        <li><i class="bi bi-check2 text-danger me-1"></i> Avoid alcohol consumption 24 hours prior to blood extraction.</li>
                    </ul>
                </div>
            </div>

            <!-- Right Column: Profile Edit Form -->
            <div class="col-lg-8">
                <div class="content-card">
                    <div class="content-card-header">
                        <h4 class="content-card-title">
                            <i class="bi bi-pencil-square text-danger"></i> Profile &amp; Location Information
                        </h4>
                    </div>

                    <form action="${pageContext.request.contextPath}/donor-profile" method="post" class="needs-validation" novalidate>
                        <input type="hidden" name="action" value="updateProfile">
                        <input type="hidden" name="donorId" value="${donor.id}">
                        <input type="hidden" name="userId" value="${accountUser.id}">

                        <!-- Account Details -->
                        <h6 class="fw-bold text-danger text-uppercase small mb-3">1. Personal &amp; Contact Information</h6>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="fullName" class="form-label fw-semibold">Full Name</label>
                                <input type="text" class="form-control" id="fullName" name="fullName" 
                                       value="<c:out value='${accountUser.fullName}' />" required>
                                <div class="invalid-feedback">Please enter your full name.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="phone" class="form-label fw-semibold">Mobile Number</label>
                                <input type="text" class="form-control" id="phone" name="phone" 
                                       value="<c:out value='${accountUser.phone}' />" required>
                                <div class="invalid-feedback">Please enter a valid phone number.</div>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label fw-semibold">Registered Email</label>
                            <input type="email" class="form-control bg-light" id="email" 
                                   value="<c:out value='${accountUser.email}' />" readonly>
                            <div class="form-text small">Email cannot be changed as it serves as your unique authentication identifier.</div>
                        </div>

                        <!-- Medical Profile -->
                        <h6 class="fw-bold text-danger text-uppercase small mb-3 pt-3 border-top">2. Clinical &amp; Medical Profile</h6>
                        <div class="row g-3 mb-3">
                            <div class="col-md-4">
                                <label for="bloodGroup" class="form-label fw-semibold">Blood Group</label>
                                <select class="form-select" id="bloodGroup" name="bloodGroup" required>
                                    <c:forEach var="bg" items="${['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']}">
                                        <option value="${bg}" ${donor.bloodGroup eq bg ? 'selected' : ''}>${bg}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label for="age" class="form-label fw-semibold">Age (18-65)</label>
                                <input type="number" class="form-control" id="age" name="age" 
                                       value="${donor.age}" min="18" max="65" required>
                            </div>
                            <div class="col-md-4">
                                <label for="gender" class="form-label fw-semibold">Gender</label>
                                <select class="form-select" id="gender" name="gender" required>
                                    <option value="MALE" ${donor.gender eq 'MALE' ? 'selected' : ''}>Male</option>
                                    <option value="FEMALE" ${donor.gender eq 'FEMALE' ? 'selected' : ''}>Female</option>
                                    <option value="OTHER" ${donor.gender eq 'OTHER' ? 'selected' : ''}>Other</option>
                                </select>
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="lastDonationDate" class="form-label fw-semibold">Last Donation Date</label>
                                <input type="date" class="form-control" id="lastDonationDate" name="lastDonationDate" 
                                       value="${donor.lastDonationDate}">
                                <div class="form-text small">Leave blank if this will be your first donation.</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-semibold d-block">Emergency Readiness</label>
                                <div class="form-check form-switch pt-2">
                                    <input class="form-check-input" type="checkbox" role="switch" id="isAvailable" name="isAvailable" value="true" ${donor.available ? 'checked' : ''}>
                                    <label class="form-check-label" for="isAvailable">
                                        Available to receive urgent donation requests
                                    </label>
                                </div>
                            </div>
                        </div>

                        <!-- Address & Location -->
                        <h6 class="fw-bold text-danger text-uppercase small mb-3 pt-3 border-top">3. Location &amp; Emergency Reach</h6>
                        <div class="row g-3 mb-3">
                            <div class="col-md-5">
                                <label for="city" class="form-label fw-semibold">City</label>
                                <input type="text" class="form-control" id="city" name="city" 
                                       value="<c:out value='${donor.city}' />" required>
                            </div>
                            <div class="col-md-4">
                                <label for="state" class="form-label fw-semibold">State</label>
                                <input type="text" class="form-control" id="state" name="state" 
                                       value="<c:out value='${donor.state}' />" required>
                            </div>
                            <div class="col-md-3">
                                <label for="pincode" class="form-label fw-semibold">Pincode</label>
                                <input type="text" class="form-control" id="pincode" name="pincode" 
                                       value="<c:out value='${donor.pincode}' />" required>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="address" class="form-label fw-semibold">Street Address / Locality</label>
                            <textarea class="form-control" id="address" name="address" rows="2" required><c:out value='${donor.address}' /></textarea>
                        </div>

                        <div class="d-flex justify-content-end gap-2">
                            <a href="${pageContext.request.contextPath}/donor-dashboard.jsp" class="btn btn-secondary-light">Cancel</a>
                            <button type="submit" class="btn btn-primary-red">
                                <i class="bi bi-check2-circle me-1"></i> Save Changes
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>

    <!-- Modal for Soft Deactivation Confirmation -->
    <div class="modal fade" id="deactivateModal" tabindex="-1" aria-labelledby="deactivateModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content" style="border-radius: var(--radius-lg);">
                <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                    <h5 class="modal-title fw-bold text-danger" id="deactivateModalLabel">
                        <i class="bi bi-slash-circle me-2"></i>Pause / Deactivate Donor Profile
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="${pageContext.request.contextPath}/donor-profile" method="post">
                    <div class="modal-body">
                        <input type="hidden" name="action" value="deactivate">
                        <input type="hidden" name="donorId" value="${donor.id}">
                        <p class="text-dark">
                            Are you sure you want to deactivate your donor profile?
                        </p>
                        <div class="p-3 bg-light rounded-3 small text-muted">
                            <p class="mb-1"><i class="bi bi-shield-check text-success me-1"></i> <strong>Safe Deactivation:</strong> Your account and previous donation history will <strong>not</strong> be deleted.</p>
                            <p class="mb-0"><i class="bi bi-bell-slash text-danger me-1"></i> You will stop receiving emergency blood match notifications until you reactivate.</p>
                        </div>
                    </div>
                    <div class="modal-footer border-top">
                        <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Keep Active</button>
                        <button type="submit" class="btn btn-danger btn-sm">Yes, Deactivate</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
