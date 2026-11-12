<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.BloodRequestDAO" %>
<%@ page import="com.blooddonation.model.BloodRequest" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="com.blooddonation.model.Role" %>
<%@ page import="java.util.Optional" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20access%20the%20blood%20requisition%20system.");
        return;
    }

    // Check if loading for Edit mode via direct JSP access
    BloodRequest editRequest = (BloodRequest) request.getAttribute("editRequest");
    BloodRequest viewRequest = (BloodRequest) request.getAttribute("viewRequest");

    String actionParam = request.getParameter("action");
    String idParam = request.getParameter("id");

    if (editRequest == null && "edit".equalsIgnoreCase(actionParam) && idParam != null) {
        try {
            Long reqId = Long.parseLong(idParam);
            BloodRequestDAO dao = new BloodRequestDAO();
            Optional<BloodRequest> opt = dao.findById(reqId);
            if (opt.isPresent() && (currentUser.getId().equals(opt.get().getRequesterId()) || currentUser.getRole() == Role.ADMIN)) {
                editRequest = opt.get();
                request.setAttribute("editRequest", editRequest);
            }
        } catch (NumberFormatException ignored) {}
    }

    if (viewRequest == null && "view".equalsIgnoreCase(actionParam) && idParam != null) {
        try {
            Long reqId = Long.parseLong(idParam);
            BloodRequestDAO dao = new BloodRequestDAO();
            Optional<BloodRequest> opt = dao.findById(reqId);
            if (opt.isPresent()) {
                viewRequest = opt.get();
                request.setAttribute("viewRequest", viewRequest);
            }
        } catch (NumberFormatException ignored) {}
    }

    boolean isEditMode = (editRequest != null);
    boolean isViewMode = (viewRequest != null);
    request.setAttribute("isEditMode", isEditMode);
    request.setAttribute("isViewMode", isViewMode);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEditMode ? 'Edit Blood Requisition' : (isViewMode ? 'Requisition Details' : 'Post Blood Request')} - BloodConnect</title>
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
                        <i class="bi bi-shield-exclamation me-1"></i> Rapid Emergency Protocol
                    </span>
                    <h1 class="dashboard-title">
                        <c:choose>
                            <c:when test="${isEditMode}">
                                Edit Requisition #REQ-<c:out value="${editRequest.id}" />
                            </c:when>
                            <c:when test="${isViewMode}">
                                Blood Requisition #REQ-<c:out value="${viewRequest.id}" />
                            </c:when>
                            <c:otherwise>
                                Emergency Blood Requirement Form
                            </c:otherwise>
                        </c:choose>
                    </h1>
                    <p class="dashboard-subtitle">
                        <c:choose>
                            <c:when test="${isEditMode}">
                                Modify clinical and hospital details for your pending blood request.
                            </c:when>
                            <c:when test="${isViewMode}">
                                Comprehensive patient and hospital requisition verification details.
                            </c:when>
                            <c:otherwise>
                                Submit an emergency blood request to instantly notify compatible voluntary donors in your city.
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <c:choose>
                        <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                            <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="btn btn-outline-red">
                                <i class="bi bi-speedometer2 me-1"></i> Admin Portal
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/requester-dashboard.jsp" class="btn btn-outline-red me-2">
                                <i class="bi bi-speedometer2 me-1"></i> My Dashboard
                            </a>
                            <a href="${pageContext.request.contextPath}/search-donors.jsp" class="btn btn-primary-red">
                                <i class="bi bi-search me-1"></i> Find Donors
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Form Container -->
    <main class="container mb-5">
        <!-- Status Feedback Alerts -->
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger custom-alert d-flex align-items-center shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5 text-danger"></i>
                <div><c:out value="${param.error}" /></div>
            </div>
        </c:if>

        <c:if test="${not empty param.created}">
            <div class="alert alert-success alert-dismissible fade show custom-alert border-success shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                Emergency Blood Request registered successfully! Active compatible donors nearby have been alerted.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>
            <%-- VIEW-ONLY DETAIL CARD --%>
            <c:when test="${isViewMode}">
                <div class="row g-4 justify-content-center">
                    <div class="col-lg-8">
                        <div class="content-card">
                            <div class="content-card-header">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="blood-type-badge m-0" style="width: 58px; height: 58px; font-size: 1.4rem;">
                                        <c:out value="${viewRequest.bloodGroup}" />
                                    </div>
                                    <div>
                                        <h4 class="content-card-title text-danger mb-0">
                                            Requisition Details: <c:out value="${viewRequest.patientName}" />
                                        </h4>
                                        <small class="text-muted">Requisition Reference: #REQ-<c:out value="${viewRequest.id}" /></small>
                                    </div>
                                </div>
                                <div>
                                    <c:choose>
                                        <c:when test="${viewRequest.status eq 'PENDING'}"><span class="badge-status-pending">PENDING</span></c:when>
                                        <c:when test="${viewRequest.status eq 'MATCHED'}"><span class="badge-status-matched">MATCHED</span></c:when>
                                        <c:when test="${viewRequest.status eq 'FULFILLED'}"><span class="badge-status-fulfilled">FULFILLED</span></c:when>
                                        <c:otherwise><span class="badge-status-cancelled"><c:out value="${viewRequest.status}" /></span></c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="row g-3 mb-4">
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3">
                                        <small class="text-muted d-block mb-1">Patient Name</small>
                                        <strong class="text-dark fs-6"><c:out value="${viewRequest.patientName}" /></strong>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3">
                                        <small class="text-muted d-block mb-1">Units Required</small>
                                        <strong class="text-danger fs-6"><c:out value="${viewRequest.unitsNeeded}" /> Unit(s)</strong>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3">
                                        <small class="text-muted d-block mb-1">Hospital / Medical Center</small>
                                        <strong class="text-dark"><c:out value="${viewRequest.hospitalName}" /></strong>
                                        <div class="small text-muted"><c:out value="${viewRequest.hospitalAddress}" />, <c:out value="${viewRequest.hospitalCity}" /></div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3">
                                        <small class="text-muted d-block mb-1">Emergency Contact</small>
                                        <strong class="text-dark"><c:out value="${viewRequest.contactPerson}" /></strong>
                                        <div><a href="tel:${viewRequest.contactPhone}" class="text-danger small"><i class="bi bi-telephone-fill me-1"></i><c:out value="${viewRequest.contactPhone}" /></a></div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3">
                                        <small class="text-muted d-block mb-1">Urgency Priority</small>
                                        <c:choose>
                                            <c:when test="${viewRequest.urgencyLevel eq 'CRITICAL'}"><span class="badge-urgency-critical">CRITICAL</span></c:when>
                                            <c:when test="${viewRequest.urgencyLevel eq 'URGENT'}"><span class="badge-urgency-urgent">URGENT</span></c:when>
                                            <c:otherwise><span class="badge-urgency-normal">NORMAL</span></c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3">
                                        <small class="text-muted d-block mb-1">Required By Date</small>
                                        <strong class="text-dark"><i class="bi bi-calendar-event me-1 text-danger"></i><c:out value="${viewRequest.requiredDate}" /></strong>
                                    </div>
                                </div>
                            </div>

                            <c:if test="${not empty viewRequest.additionalNotes}">
                                <div class="mb-4">
                                    <h6 class="fw-semibold text-secondary small text-uppercase">Clinical Notes &amp; Instructions</h6>
                                    <div class="p-3 bg-light rounded-3 text-secondary small">
                                        <c:out value="${viewRequest.additionalNotes}" />
                                    </div>
                                </div>
                            </c:if>

                            <div class="d-flex justify-content-end gap-2 border-top pt-3">
                                <c:choose>
                                    <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                                        <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="btn btn-secondary-light">
                                            Back to Admin Dashboard
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/requester-dashboard.jsp" class="btn btn-secondary-light">
                                            Back to Requester Dashboard
                                        </a>
                                        <c:if test="${viewRequest.status eq 'PENDING'}">
                                            <a href="${pageContext.request.contextPath}/blood-request?action=edit&id=${viewRequest.id}" class="btn btn-outline-red">
                                                <i class="bi bi-pencil-square me-1"></i> Edit Requisition
                                            </a>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- CREATE OR EDIT FORM --%>
            <c:otherwise>
                <div class="row g-4">
                    <!-- Left Column: Request Form -->
                    <div class="col-lg-8">
                        <div class="content-card">
                            <div class="content-card-header">
                                <h4 class="content-card-title text-danger">
                                    <i class="bi bi-file-earmark-medical-fill"></i>
                                    ${isEditMode ? 'Update Blood Requisition' : 'Patient & Hospital Requisition'}
                                </h4>
                                <span class="small text-muted">* All medical &amp; contact fields are required</span>
                            </div>

                            <form action="${pageContext.request.contextPath}/blood-request" method="post" class="needs-validation" novalidate>
                                <input type="hidden" name="action" value="${isEditMode ? 'updateRequest' : 'createRequest'}">
                                <c:if test="${isEditMode}">
                                    <input type="hidden" name="requestId" value="${editRequest.id}">
                                </c:if>

                                <!-- Section 1: Patient & Requirement -->
                                <h6 class="fw-bold text-danger text-uppercase small mb-3">1. Patient Medical Details</h6>
                                <div class="row g-3 mb-3">
                                    <div class="col-md-7">
                                        <label for="patientName" class="form-label fw-semibold">Patient Full Name</label>
                                        <input type="text" class="form-control" id="patientName" name="patientName" 
                                               placeholder="e.g. Ramesh Kumar" 
                                               value="<c:out value='${isEditMode ? editRequest.patientName : ""}' />" required>
                                        <div class="invalid-feedback">Patient name is required.</div>
                                    </div>
                                    <div class="col-md-5">
                                        <label for="unitsNeeded" class="form-label fw-semibold">Units / Bottles Needed</label>
                                        <input type="number" class="form-control" id="unitsNeeded" name="unitsNeeded" 
                                               value="${isEditMode ? editRequest.unitsNeeded : 1}" min="1" max="20" required>
                                        <div class="invalid-feedback">Specify units (1 - 20).</div>
                                    </div>
                                </div>

                                <!-- Blood Group Selector -->
                                <div class="mb-3">
                                    <label class="form-label fw-semibold d-block">Required Blood Group <span class="text-danger">*</span></label>
                                    <div class="blood-group-selector">
                                        <c:forEach var="bg" items="${['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-']}">
                                            <div>
                                                <input type="radio" class="blood-option-input" id="req-${bg}" name="bloodGroup" value="${bg}" 
                                                       ${(isEditMode ? editRequest.bloodGroup : 'O+') eq bg ? 'checked' : ''} required>
                                                <label class="blood-option-label" for="req-${bg}"><c:out value="${bg}" /></label>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>

                                <div class="row g-3 mb-3">
                                    <div class="col-md-6">
                                        <label for="urgencyLevel" class="form-label fw-semibold">Urgency Priority</label>
                                        <select class="form-select" id="urgencyLevel" name="urgencyLevel" required>
                                            <option value="CRITICAL" ${(isEditMode ? editRequest.urgencyLevel : '') eq 'CRITICAL' ? 'selected' : ''}>Critical (Immediate / Emergency Surgery)</option>
                                            <option value="URGENT" ${(isEditMode ? editRequest.urgencyLevel : 'URGENT') eq 'URGENT' ? 'selected' : ''}>Urgent (Within 24 Hours)</option>
                                            <option value="NORMAL" ${(isEditMode ? editRequest.urgencyLevel : '') eq 'NORMAL' ? 'selected' : ''}>Normal (Scheduled Procedure)</option>
                                        </select>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="requiredDate" class="form-label fw-semibold">Required By Date</label>
                                        <input type="date" class="form-control" id="requiredDate" name="requiredDate" 
                                               value="${isEditMode ? editRequest.requiredDate : ''}" required>
                                        <div class="invalid-feedback">Required date is mandatory.</div>
                                    </div>
                                </div>

                                <!-- Section 2: Hospital Details -->
                                <h6 class="fw-bold text-danger text-uppercase small mb-3 pt-3 border-top">2. Hospital &amp; Medical Facility Details</h6>
                                <div class="row g-3 mb-3">
                                    <div class="col-md-7">
                                        <label for="hospitalName" class="form-label fw-semibold">Hospital / Blood Bank Name</label>
                                        <input type="text" class="form-control" id="hospitalName" name="hospitalName" 
                                               placeholder="e.g. Manipal Multi-Speciality Hospital" 
                                               value="<c:out value='${isEditMode ? editRequest.hospitalName : ""}' />" required>
                                        <div class="invalid-feedback">Hospital name is required.</div>
                                    </div>
                                    <div class="col-md-5">
                                        <label for="hospitalCity" class="form-label fw-semibold">Hospital City</label>
                                        <input type="text" class="form-control" id="hospitalCity" name="hospitalCity" 
                                               placeholder="e.g. Bangalore" 
                                               value="<c:out value='${isEditMode ? editRequest.hospitalCity : ""}' />" required>
                                        <div class="invalid-feedback">Hospital city is required.</div>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="hospitalAddress" class="form-label fw-semibold">Hospital Street Address / Wing / Room No.</label>
                                    <textarea class="form-control" id="hospitalAddress" name="hospitalAddress" rows="2" 
                                              placeholder="e.g. Ward 4B, ICU Block, HAL Airport Road, Bangalore" required><c:out value="${isEditMode ? editRequest.hospitalAddress : ''}" /></textarea>
                                    <div class="invalid-feedback">Please enter the hospital address.</div>
                                </div>

                                <!-- Section 3: Contact Person -->
                                <h6 class="fw-bold text-danger text-uppercase small mb-3 pt-3 border-top">3. Emergency Contact Details</h6>
                                <div class="row g-3 mb-3">
                                    <div class="col-md-6">
                                        <label for="contactPerson" class="form-label fw-semibold">Contact Person Name</label>
                                        <input type="text" class="form-control" id="contactPerson" name="contactPerson" 
                                               placeholder="e.g. Vikram Mehta (Son)" 
                                               value="<c:out value='${isEditMode ? editRequest.contactPerson : ""}' />" required>
                                        <div class="invalid-feedback">Contact person name is required.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="contactPhone" class="form-label fw-semibold">Emergency Phone Number</label>
                                        <input type="tel" class="form-control" id="contactPhone" name="contactPhone" 
                                               placeholder="+91 9876543210" pattern="[0-9+ -]{7,20}" 
                                               value="<c:out value='${isEditMode ? editRequest.contactPhone : ""}' />" required>
                                        <div class="invalid-feedback">Valid contact phone is required (7-20 digits).</div>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <label for="additionalNotes" class="form-label fw-semibold">Additional Clinical Notes or Instructions</label>
                                    <textarea class="form-control" id="additionalNotes" name="additionalNotes" rows="2" 
                                              placeholder="e.g. Doctor recommendation, replacement donor details, surgery time..."><c:out value="${isEditMode ? editRequest.additionalNotes : ''}" /></textarea>
                                </div>

                                <div class="d-flex gap-2">
                                    <c:if test="${isEditMode}">
                                        <a href="${pageContext.request.contextPath}/requester-dashboard.jsp" class="btn btn-secondary-light py-2 px-4">
                                            Cancel
                                        </a>
                                    </c:if>
                                    <button type="submit" class="btn btn-primary-red flex-grow-1 py-2 fs-6">
                                        <i class="bi ${isEditMode ? 'bi-check2-circle' : 'bi-broadcast'} me-1"></i>
                                        ${isEditMode ? 'Save & Update Blood Request' : 'Broadcast Emergency Blood Request'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Right Column: Requisition Guidance & Helpline -->
                    <div class="col-lg-4">
                        <div class="content-card mb-4">
                            <h5 class="content-card-title text-danger mb-3">
                                <i class="bi bi-shield-check"></i> Requisition Checklist
                            </h5>
                            <ul class="list-unstyled small text-muted mb-0">
                                <li class="mb-2">
                                    <i class="bi bi-check-circle-fill text-danger me-1"></i>
                                    <strong>Doctor's Prescription:</strong> Keep hospital blood requisition slip ready for donor verification.
                                </li>
                                <li class="mb-2">
                                    <i class="bi bi-check-circle-fill text-danger me-1"></i>
                                    <strong>Active Phone Line:</strong> Ensure the contact number remains reachable for incoming donor calls.
                                </li>
                                <li class="mb-2">
                                    <i class="bi bi-check-circle-fill text-danger me-1"></i>
                                    <strong>Compatibility:</strong> Donors matching either identical or clinically universal blood types will be matched.
                                </li>
                                <li>
                                    <i class="bi bi-check-circle-fill text-danger me-1"></i>
                                    <strong>Zero Charges:</strong> Voluntary blood donation is strictly non-remunerated by law.
                                </li>
                            </ul>
                        </div>

                        <div class="content-card bg-light border">
                            <h5 class="content-card-title text-dark mb-2">
                                <i class="bi bi-telephone-inbound-fill text-danger me-1"></i> 24/7 Helpline Support
                            </h5>
                            <p class="small text-muted mb-3">
                                Need immediate assistance reaching rare blood group donors (AB-, O-)?
                            </p>
                            <a href="tel:1800256633" class="btn btn-outline-red w-100 text-center">
                                <i class="bi bi-telephone-fill me-1"></i> Call 1800-BLOOD-SAVE
                            </a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

    <script>
        // Set minimum required date to today if creating new request
        const dateInput = document.getElementById('requiredDate');
        if (dateInput && !dateInput.value) {
            const today = new Date().toISOString().split('T')[0];
            dateInput.min = today;
            dateInput.value = today;
        }
    </script>
</body>
</html>
