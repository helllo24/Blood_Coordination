<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.DonorDAO" %>
<%@ page import="com.blooddonation.model.Donor" %>
<%@ page import="java.util.List" %>

<%
    // Fallback if accessed directly without passing through DonorSearchServlet
    if (request.getAttribute("donorsList") == null) {
        String bloodGroup = request.getParameter("bloodGroup");
        String city = request.getParameter("city");
        String compatibleParam = request.getParameter("compatible");
        String availableOnlyParam = request.getParameter("availableOnly");
        boolean isCompatible = "true".equalsIgnoreCase(compatibleParam);

        DonorDAO donorDAO = new DonorDAO();
        List<Donor> matchingDonors;

        if (bloodGroup != null && !bloodGroup.isBlank()) {
            if (isCompatible) {
                matchingDonors = donorDAO.findCompatibleDonors(bloodGroup.trim(), city);
            } else {
                Boolean avail = (availableOnlyParam != null && !availableOnlyParam.isBlank()) ? Boolean.valueOf(availableOnlyParam) : null;
                matchingDonors = donorDAO.searchDonors(bloodGroup.trim(), city, avail);
            }
        } else if (city != null && !city.isBlank()) {
            Boolean avail = (availableOnlyParam != null && !availableOnlyParam.isBlank()) ? Boolean.valueOf(availableOnlyParam) : null;
            matchingDonors = donorDAO.searchDonors(null, city, avail);
        } else {
            Boolean avail = (availableOnlyParam != null && !availableOnlyParam.isBlank()) ? Boolean.valueOf(availableOnlyParam) : null;
            matchingDonors = donorDAO.searchDonors(null, null, avail);
        }

        request.setAttribute("donorsList", matchingDonors);
        request.setAttribute("selectedBg", bloodGroup != null ? bloodGroup : "");
        request.setAttribute("selectedCity", city != null ? city : "");
        request.setAttribute("isCompatible", isCompatible);
        request.setAttribute("availableOnly", availableOnlyParam);
        request.setAttribute("disclaimer", "Compatibility results are preliminary software filtering only. Actual donor eligibility and blood compatibility must be confirmed by qualified blood-bank professionals.");
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Search Blood Donors - BloodConnect</title>
    <jsp:include page="includes/header.jsp" />
</head>
<body>

    <!-- Professional Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Hero Header -->
    <section class="dashboard-hero">
        <div class="container">
            <div class="row align-items-center g-3">
                <div class="col-lg-8">
                    <span class="badge bg-danger-subtle text-danger fw-semibold px-3 py-1 rounded-pill mb-2">
                        <i class="bi bi-search me-1"></i> Voluntary Registry Search
                    </span>
                    <h1 class="dashboard-title">
                        Search Voluntary Blood Donors
                    </h1>
                    <p class="dashboard-subtitle">
                        Find verified, available blood donors in your vicinity with intelligent cross-compatibility matching.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red">
                        <i class="bi bi-plus-circle me-1"></i> Post Emergency Request
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Content Container -->
    <main class="container mb-5">

        <!-- Search Filter Card -->
        <div class="search-filter-card mb-4">
            <form action="${pageContext.request.contextPath}/search-donors" method="get">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label for="bloodGroup" class="form-label fw-semibold">Patient Blood Group</label>
                        <select class="form-select" id="bloodGroup" name="bloodGroup">
                            <option value="">-- All Blood Groups --</option>
                            <c:forEach var="bg" items="${['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-']}">
                                <option value="${bg}" ${selectedBg eq bg ? 'selected' : ''}>${bg}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-md-3">
                        <label for="city" class="form-label fw-semibold">City or Locality</label>
                        <div class="input-group">
                            <span class="input-group-text bg-white border-end-0">
                                <i class="bi bi-geo-alt text-danger"></i>
                            </span>
                            <input type="text" class="form-control border-start-0" id="city" name="city" 
                                   placeholder="e.g. Bangalore, Mumbai..." value="<c:out value='${selectedCity}' />">
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="d-flex flex-column gap-1 pt-1">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" id="compatible" name="compatible" value="true" ${isCompatible ? 'checked' : ''}>
                                <label class="form-check-label small" for="compatible">
                                    <strong>Clinical Cross-Match</strong> (Include compatible universal donors)
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" id="availableOnly" name="availableOnly" value="true" ${availableOnly eq 'true' ? 'checked' : ''}>
                                <label class="form-check-label small" for="availableOnly">
                                    <strong>Active Donors Only</strong> (Hide paused / busy donors)
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary-red w-100 py-2">
                            <i class="bi bi-filter me-1"></i> Filter
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <!-- Medical Disclaimer Alert Card -->
        <div class="alert alert-warning border-0 shadow-sm rounded-3 p-3 mb-4" style="background-color: #fff8e1; border-left: 5px solid #ffb300 !important;">
            <div class="d-flex align-items-start">
                <i class="bi bi-shield-exclamation fs-4 text-warning me-3 mt-1"></i>
                <div>
                    <h6 class="fw-bold text-dark mb-1">Important Medical Disclaimer</h6>
                    <p class="mb-0 text-secondary small">
                        <c:out value="${not empty disclaimer ? disclaimer : 'Compatibility results are preliminary software filtering only. Actual donor eligibility and blood compatibility must be confirmed by qualified blood-bank professionals.'}" />
                    </p>
                </div>
            </div>
        </div>

        <!-- Search Results Count Banner -->
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0">
                <i class="bi bi-people-fill text-danger me-1"></i>
                Matching Donors Found: <span class="text-danger">${donorsList.size()}</span>
            </h5>
            <c:if test="${not empty selectedBg}">
                <span class="badge bg-light text-muted border px-2 py-1">
                    Filtered for: <strong>${selectedBg}</strong> ${isCompatible ? '(Cross-Match Included)' : '(Exact Match)'}
                </span>
            </c:if>
        </div>

        <!-- Results Grid -->
        <c:choose>
            <c:when test="${empty donorsList}">
                <div class="content-card text-center py-5">
                    <i class="bi bi-search fs-1 d-block mb-3 text-muted"></i>
                    <h5 class="fw-bold">No Matching Donors Found</h5>
                    <p class="text-muted small mb-3">
                        No active donors currently match the exact criteria in this location. 
                        Try enabling <strong>"Clinical Cross-Match"</strong> to include universal donor groups, or broaden your city search.
                    </p>
                    <a href="${pageContext.request.contextPath}/blood-request.jsp" class="btn btn-primary-red btn-sm">
                        Broadcast Emergency Request Instead
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <c:forEach var="donor" items="${donorsList}">
                        <div class="col-md-6 col-lg-4">
                            <div class="blood-group-card text-start h-100 p-4 d-flex flex-column">
                                <div class="d-flex justify-content-between align-items-start mb-3">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="blood-type-badge m-0" style="width: 52px; height: 52px; font-size: 1.25rem;">
                                            <c:out value="${donor.bloodGroup}" />
                                        </div>
                                        <div>
                                            <h5 class="fw-bold mb-0">Donor #DNR-<c:out value="${donor.id}" /></h5>
                                            <small class="text-muted"><i class="bi bi-geo-alt-fill text-danger me-1"></i><c:out value="${donor.city}" />, <c:out value="${donor.state}" /></small>
                                        </div>
                                    </div>
                                    <c:choose>
                                        <c:when test="${donor.available}">
                                            <span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill">
                                                Available
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary-subtle text-muted border rounded-pill">
                                                Unavailable
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="p-3 bg-light rounded-3 mb-3 small">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="text-muted">Demographics:</span>
                                        <span class="fw-semibold"><c:out value="${donor.age}" /> yrs • <c:out value="${donor.gender}" /></span>
                                    </div>
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="text-muted">Total Donations:</span>
                                        <span class="fw-bold text-danger"><c:out value="${donor.totalDonations}" /> Completed</span>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span class="text-muted">Last Donated:</span>
                                        <span class="text-dark"><c:out value="${donor.lastDonationDate != null ? donor.lastDonationDate : 'First Time Donor'}" /></span>
                                    </div>
                                </div>

                                <div class="mt-auto">
                                    <!-- Privacy protected Coordinator Contact -->
                                    <button type="button" class="btn btn-outline-red w-100 btn-sm" data-bs-toggle="modal" data-bs-target="#contactModal${donor.id}">
                                        <i class="bi bi-telephone-fill me-1"></i> Contact Donor Coordinator
                                    </button>

                                    <!-- Contact Modal -->
                                    <div class="modal fade" id="contactModal${donor.id}" tabindex="-1" aria-hidden="true">
                                        <div class="modal-dialog">
                                            <div class="modal-content" style="border-radius: var(--radius-lg);">
                                                <div class="modal-header border-bottom" style="background-color: var(--light-red);">
                                                    <h5 class="modal-title fw-bold text-danger">
                                                        <i class="bi bi-shield-check me-2"></i>Coordinated Donor Contact
                                                    </h5>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                </div>
                                                <div class="modal-body">
                                                    <div class="text-center mb-3">
                                                        <div class="blood-type-badge mx-auto mb-2" style="width: 60px; height: 60px; font-size: 1.4rem;">
                                                            <c:out value="${donor.bloodGroup}" />
                                                        </div>
                                                        <h5 class="fw-bold mb-1">Donor DNR-<c:out value="${donor.id}" /></h5>
                                                        <p class="text-muted small"><i class="bi bi-geo-alt-fill text-danger me-1"></i><c:out value="${donor.city}" />, <c:out value="${donor.state}" /></p>
                                                    </div>
                                                    <div class="p-3 bg-light rounded-3 small text-muted mb-3">
                                                        <p class="mb-1"><i class="bi bi-info-circle text-primary me-1"></i> To protect donor privacy and prevent unsolicited spam, direct personal phone numbers are coordinated through our verified emergency desk.</p>
                                                        <p class="mb-0"><i class="bi bi-telephone-inbound text-success me-1"></i> Central Coordinator Desk: <strong>+91 800-BLOOD-HELP</strong> (24x7 Hotline)</p>
                                                    </div>
                                                    <c:choose>
                                                        <c:when test="${not empty donor.phone}">
                                                            <div class="p-2 border rounded text-center small">
                                                                <span class="text-muted">Registered Verification Hotline: </span>
                                                                <strong class="text-dark"><c:out value="${donor.phone}" /></strong>
                                                            </div>
                                                        </c:when>
                                                    </c:choose>
                                                </div>
                                                <div class="modal-footer border-top">
                                                    <a href="tel:+918002566343" class="btn btn-primary-red btn-sm">
                                                        <i class="bi bi-telephone-fill me-1"></i> Call Emergency Desk
                                                    </a>
                                                    <button type="button" class="btn btn-secondary-light btn-sm" data-bs-dismiss="modal">Close</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
