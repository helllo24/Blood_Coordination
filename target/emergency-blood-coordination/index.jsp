<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emergency Blood Request and Donor Coordination System</title>
    <jsp:include page="includes/header.jsp" />
</head>
<body>

    <!-- Professional Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Main Content -->
    <main>
        <!-- Session Notification Alerts -->
        <c:if test="${not empty param.loginSuccess and not empty sessionScope.currentUser}">
            <div class="container pt-3">
                <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                    <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                    Welcome back, <strong><c:out value="${sessionScope.userName}" /></strong>! You have successfully signed in as <strong><c:out value="${sessionScope.userRole}" /></strong>.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </c:if>
        <c:if test="${not empty param.loggedOut}">
            <div class="container pt-3">
                <div class="alert alert-info alert-dismissible fade show custom-alert shadow-sm" role="alert">
                    <i class="bi bi-info-circle-fill me-2 fs-5 text-info"></i>
                    You have been logged out successfully.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </c:if>

        <!-- Hero Section -->
        <section class="hero-section">
            <div class="container">
                <div class="row align-items-center g-5">
                    <div class="col-lg-7">
                        <div class="hero-badge">
                            <i class="bi bi-heart-pulse-fill"></i>
                            <span>24/7 Verified Emergency Donor Network</span>
                        </div>
                        <h1 class="hero-title">
                            Every Drop Counts.<br>
                            <span>Every Donor Matters.</span>
                        </h1>
                        <p class="hero-subtitle">
                            An intelligent emergency blood coordination platform connecting voluntary donors, 
                            patients, and hospitals in real time to save lives during critical medical emergencies.
                        </p>
                        <div class="d-flex flex-wrap gap-3 mb-4">
                            <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red btn-lg">
                                <i class="bi bi-telephone-plus me-2"></i>Request Blood
                            </a>
                            <a href="${pageContext.request.contextPath}/register.jsp?role=DONOR" class="btn btn-outline-red btn-lg">
                                <i class="bi bi-suit-heart-fill me-2"></i>Become a Donor
                            </a>
                        </div>
                        <div class="row g-3 pt-3">
                            <div class="col-sm-4">
                                <div class="stat-box">
                                    <i class="bi bi-people-fill"></i>
                                    <div>
                                        <div class="stat-number">100%</div>
                                        <div class="stat-label">Volunteer Donors</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-4">
                                <div class="stat-box">
                                    <i class="bi bi-lightning-charge-fill"></i>
                                    <div>
                                        <div class="stat-number">15 Mins</div>
                                        <div class="stat-label">Avg Coordination</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-4">
                                <div class="stat-box">
                                    <i class="bi bi-shield-check"></i>
                                    <div>
                                        <div class="stat-number">8 Groups</div>
                                        <div class="stat-label">Full Compatibility</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-5">
                        <div class="hero-card">
                            <div class="d-flex align-items-center gap-3 mb-4">
                                <div class="auth-icon m-0" style="width: 52px; height: 52px; font-size: 1.4rem;">
                                    <i class="bi bi-shield-exclamation"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0" style="font-size: 1.15rem;">Emergency Blood Assistance</h4>
                                    <p class="text-muted small mb-0">Direct coordination during crises</p>
                                </div>
                            </div>
                            <div class="alert custom-alert bg-light border-0 mb-4" style="background-color: var(--light-red) !important;">
                                <div class="d-flex align-items-center gap-2 text-danger fw-semibold small mb-1">
                                    <i class="bi bi-exclamation-octagon-fill"></i> Rapid Protocol
                                </div>
                                <p class="small text-muted mb-0">
                                    Verified donors within your city receive instant coordination alerts once a verified blood request is submitted.
                                </p>
                            </div>
                            <div class="d-grid gap-2">
                                <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red text-center py-2">
                                    <i class="bi bi-plus-circle me-1"></i> Post Emergency Request
                                </a>
                                <a href="${pageContext.request.contextPath}/search-donors.jsp" class="btn btn-secondary-light text-center py-2">
                                    <i class="bi bi-search me-1"></i> Search Active Donors
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Blood Group Information Section -->
        <section id="blood-groups" class="py-5">
            <div class="container">
                <div class="section-header">
                    <span class="section-tag">Medical Reference</span>
                    <h2 class="section-title">Blood Group &amp; Compatibility Guide</h2>
                    <p class="section-desc">
                        Understanding blood type compatibility ensures rapid matching between donors and patients during critical emergencies.
                    </p>
                </div>
                <div class="row g-4">
                    <!-- O- -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">O-</div>
                                <h4 class="blood-card-title">Universal Red Cell Donor</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>All Blood Types (Universal)</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>Only O-</span>
                                </div>
                            </div>
                            <span class="badge bg-danger-subtle text-danger mt-3">High Emergency Demand</span>
                        </div>
                    </div>

                    <!-- O+ -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">O+</div>
                                <h4 class="blood-card-title">Most Common Blood Type</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>O+, A+, B+, AB+</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>O+, O-</span>
                                </div>
                            </div>
                            <span class="badge bg-secondary-subtle text-secondary mt-3">Widely Needed</span>
                        </div>
                    </div>

                    <!-- A+ -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">A+</div>
                                <h4 class="blood-card-title">Common Type A</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>A+, AB+</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>A+, A-, O+, O-</span>
                                </div>
                            </div>
                            <span class="badge bg-secondary-subtle text-secondary mt-3">Frequent Requirement</span>
                        </div>
                    </div>

                    <!-- A- -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">A-</div>
                                <h4 class="blood-card-title">Rare Type A</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>A+, A-, AB+, AB-</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>A-, O-</span>
                                </div>
                            </div>
                            <span class="badge bg-danger-subtle text-danger mt-3">Critical Reserve</span>
                        </div>
                    </div>

                    <!-- B+ -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">B+</div>
                                <h4 class="blood-card-title">Common Type B</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>B+, AB+</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>B+, B-, O+, O-</span>
                                </div>
                            </div>
                            <span class="badge bg-secondary-subtle text-secondary mt-3">Frequent Requirement</span>
                        </div>
                    </div>

                    <!-- B- -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">B-</div>
                                <h4 class="blood-card-title">Rare Type B</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>B+, B-, AB+, AB-</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>B-, O-</span>
                                </div>
                            </div>
                            <span class="badge bg-danger-subtle text-danger mt-3">Critical Reserve</span>
                        </div>
                    </div>

                    <!-- AB+ -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">AB+</div>
                                <h4 class="blood-card-title">Universal Recipient</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>Only AB+</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>All Blood Types</span>
                                </div>
                            </div>
                            <span class="badge bg-success-subtle text-success mt-3">Universal Receiver</span>
                        </div>
                    </div>

                    <!-- AB- -->
                    <div class="col-md-6 col-lg-3">
                        <div class="blood-group-card">
                            <div>
                                <div class="blood-type-badge">AB-</div>
                                <h4 class="blood-card-title">Rarest Blood Type</h4>
                                <div class="compat-item">
                                    <span class="compat-label">Can Give To</span>
                                    <span>AB+, AB-</span>
                                </div>
                                <div class="compat-item">
                                    <span class="compat-label">Can Receive From</span>
                                    <span>AB-, A-, B-, O-</span>
                                </div>
                            </div>
                            <span class="badge bg-danger-subtle text-danger mt-3">Ultra Rare</span>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- How It Works Section -->
        <section id="how-it-works" class="py-5" style="background-color: var(--white); border-top: 1px solid var(--border-color); border-bottom: 1px solid var(--border-color);">
            <div class="container">
                <div class="section-header">
                    <span class="section-tag">Simple &amp; Seamless</span>
                    <h2 class="section-title">How The Coordination Works</h2>
                    <p class="section-desc">
                        Our streamlined three-step workflow ensures minimal response time between emergency blood requests and verified voluntary donors.
                    </p>
                </div>
                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="step-card">
                            <span class="step-number">01</span>
                            <div class="step-icon-wrapper">
                                <i class="bi bi-person-badge"></i>
                            </div>
                            <h4>1. Register &amp; Profile</h4>
                            <p>
                                Join as a voluntary donor by providing your blood group and city, or sign up as a requester to post urgent hospital requirements.
                            </p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="step-card">
                            <span class="step-number">02</span>
                            <div class="step-icon-wrapper">
                                <i class="bi bi-search-heart"></i>
                            </div>
                            <h4>2. Smart Matching</h4>
                            <p>
                                The platform instantly matches compatible blood groups based on clinical cross-compatibility tables and geographic vicinity.
                            </p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="step-card">
                            <span class="step-number">03</span>
                            <div class="step-icon-wrapper">
                                <i class="bi bi-hospital"></i>
                            </div>
                            <h4>3. Hospital Coordination</h4>
                            <p>
                                Verified donors connect with the hospital or patient family directly, facilitating swift donation and transparent history tracking.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- About Us Section -->
        <section id="about-us" class="py-5">
            <div class="container">
                <div class="row align-items-center g-5">
                    <div class="col-lg-6">
                        <span class="section-tag">About Our Initiative</span>
                        <h2 class="section-title">Bridging The Gap in Emergency Blood Supply</h2>
                        <p class="text-muted mb-3">
                            BloodConnect is an academic healthcare initiative developed using Java Servlet, JSP, JDBC, and MySQL. 
                            Our mission is to eliminate life-threatening shortages by transforming blood request coordination from 
                            manual phone calls into an automated, transparent, and rapid matching platform.
                        </p>
                        <p class="text-muted mb-4">
                            By maintaining an updated voluntary donor registry, cross-referencing biological compatibility, 
                            and providing dedicated portals for Requesters, Donors, and Administrators, we ensure that no emergency 
                            goes unanswered.
                        </p>
                        <div class="row g-3">
                            <div class="col-sm-6">
                                <div class="d-flex align-items-center gap-3 p-3 bg-white border rounded-3">
                                    <i class="bi bi-shield-check text-danger fs-3"></i>
                                    <div>
                                        <h6 class="mb-0 fw-bold">100% Verified</h6>
                                        <span class="text-muted small">Validated donors &amp; requests</span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="d-flex align-items-center gap-3 p-3 bg-white border rounded-3">
                                    <i class="bi bi-clock-history text-danger fs-3"></i>
                                    <div>
                                        <h6 class="mb-0 fw-bold">Real-Time Alerts</h6>
                                        <span class="text-muted small">Instant response matching</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="p-4 bg-white border rounded-4 shadow-sm">
                            <h4 class="fw-bold mb-3" style="color: var(--primary-red);">Why Choose BloodConnect?</h4>
                            <div class="d-flex gap-3 mb-3">
                                <div class="stat-icon-wrapper m-0" style="width: 44px; height: 44px; font-size: 1.25rem;">
                                    <i class="bi bi-geo-alt"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-bold">Geographic Proximity</h6>
                                    <p class="text-muted small mb-0">Searches and matches available donors within the same city to minimize transit delay.</p>
                                </div>
                            </div>
                            <div class="d-flex gap-3 mb-3">
                                <div class="stat-icon-wrapper m-0" style="width: 44px; height: 44px; font-size: 1.25rem;">
                                    <i class="bi bi-diagram-3"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-bold">Clinical Compatibility</h6>
                                    <p class="text-muted small mb-0">Leverages a medical cross-compatibility matrix so universal donors like O- can be utilized effectively.</p>
                                </div>
                            </div>
                            <div class="d-flex gap-3">
                                <div class="stat-icon-wrapper m-0" style="width: 44px; height: 44px; font-size: 1.25rem;">
                                    <i class="bi bi-clipboard2-pulse"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-bold">Complete Transparency</h6>
                                    <p class="text-muted small mb-0">Track responses, verified donations, and historical audit logs in real time across all user roles.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Contact Information Section -->
        <section id="contact-us" class="py-5" style="background-color: var(--white); border-top: 1px solid var(--border-color);">
            <div class="container">
                <div class="section-header">
                    <span class="section-tag">Get in Touch</span>
                    <h2 class="section-title">Emergency Coordination Desk &amp; Contact</h2>
                    <p class="section-desc">
                        Need assistance with emergency blood procurement or want to register a voluntary donation camp? Contact our support desk.
                    </p>
                </div>
                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="stat-metric-card h-100">
                            <div class="stat-icon-wrapper">
                                <i class="bi bi-telephone-inbound-fill"></i>
                            </div>
                            <div>
                                <h6 class="fw-bold mb-1">Emergency Helpline</h6>
                                <p class="text-muted small mb-1">24/7 Rapid Response Desk</p>
                                <span class="fw-bold text-danger">1800-BLOOD-SAVE (+91 80 2566 3300)</span>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="stat-metric-card h-100">
                            <div class="stat-icon-wrapper">
                                <i class="bi bi-envelope-check-fill"></i>
                            </div>
                            <div>
                                <h6 class="fw-bold mb-1">Email Coordination</h6>
                                <p class="text-muted small mb-1">Inquiries &amp; Hospital Verification</p>
                                <span class="fw-bold text-danger">support@bloodconnect.org</span>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="stat-metric-card h-100">
                            <div class="stat-icon-wrapper">
                                <i class="bi bi-geo-alt-fill"></i>
                            </div>
                            <div>
                                <h6 class="fw-bold mb-1">Central Blood Bank Desk</h6>
                                <p class="text-muted small mb-1">National Coordination Office</p>
                                <span class="fw-bold text-secondary">Indiranagar, Bangalore, Karnataka 560001</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Emergency Callout Banner -->
        <section id="emergency-contact" class="container">
            <div class="emergency-banner">
                <div class="row align-items-center g-3">
                    <div class="col-lg-8">
                        <h3><i class="bi bi-telephone-inbound-fill me-2"></i>Immediate Emergency Blood Requirement?</h3>
                        <p class="mb-0 text-muted">
                            Post an urgent blood request with patient and hospital details to alert active compatible donors nearby.
                        </p>
                    </div>
                    <div class="col-lg-4 text-lg-end">
                        <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red btn-lg">
                            <i class="bi bi-plus-circle-fill me-2"></i>Post Blood Request
                        </a>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
