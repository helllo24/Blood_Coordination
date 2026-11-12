<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Emergency Blood Coordination System</title>
    <jsp:include page="includes/header.jsp" />
</head>
<body>

    <!-- Professional Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Main Registration Container -->
    <main class="auth-wrapper">
        <div class="container">
            <div class="auth-card" style="max-width: 680px;">
                <div class="auth-header">
                    <div class="auth-icon">
                        <i class="bi bi-person-heart"></i>
                    </div>
                    <h2>Create an Account</h2>
                    <p>Join the community to donate or request emergency blood units</p>
                </div>

                <!-- Password Mismatch Alert (Client-side JS trigger) -->
                <div id="passwordMismatchAlert" class="alert alert-danger custom-alert d-none" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> Passwords do not match. Please re-enter them carefully.
                </div>

                <!-- Server Error Alert -->
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger custom-alert d-flex align-items-center" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                        <div><c:out value="${param.error}" /></div>
                    </div>
                </c:if>

                <!-- Role Selection Switcher -->
                <div class="role-toggle-group">
                    <button type="button" class="role-toggle-btn active" data-role="DONOR">
                        <i class="bi bi-heart-pulse-fill me-1"></i> Register as Blood Donor
                    </button>
                    <button type="button" class="role-toggle-btn" data-role="REQUESTER">
                        <i class="bi bi-hospital me-1"></i> Register as Requester / Hospital
                    </button>
                </div>

                <!-- Registration Form -->
                <form action="${pageContext.request.contextPath}/register" method="post" class="needs-validation" novalidate>
                    <!-- Hidden field storing the active role -->
                    <input type="hidden" name="role" id="selectedRoleInput" value="DONOR">

                    <!-- Section: Account Basics -->
                    <h5 class="fw-bold mb-3 pb-1 border-bottom" style="font-size: 1rem; color: var(--primary-red);">
                        1. Personal &amp; Contact Information
                    </h5>

                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label for="fullName" class="form-label">Full Name</label>
                            <input type="text" class="form-control" id="fullName" name="fullName" 
                                   placeholder="e.g. Rahul Sharma" required>
                            <div class="invalid-feedback">Full name is required.</div>
                        </div>
                        <div class="col-md-6">
                            <label for="phone" class="form-label">Mobile Number</label>
                            <input type="tel" class="form-control" id="phone" name="phone" 
                                   placeholder="+91 9876543210" pattern="[0-9+ -]{10,15}" required>
                            <div class="invalid-feedback">Valid phone number is required.</div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="email" class="form-label">Email Address</label>
                        <input type="email" class="form-control" id="email" name="email" 
                               placeholder="name@example.com" required autocomplete="email">
                        <div class="invalid-feedback">Please enter a valid email address.</div>
                    </div>

                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label for="password" class="form-label">Password</label>
                            <div class="input-icon-group">
                                <input type="password" class="form-control" id="password" name="password" 
                                       placeholder="At least 6 characters" minlength="6" required autocomplete="new-password">
                                <i class="bi bi-eye input-icon-toggle" data-target="password" title="Toggle visibility"></i>
                            </div>
                            <div class="invalid-feedback">Password must be at least 6 characters.</div>
                        </div>
                        <div class="col-md-6">
                            <label for="confirmPassword" class="form-label">Confirm Password</label>
                            <div class="input-icon-group">
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" 
                                       placeholder="Re-enter password" minlength="6" required autocomplete="new-password">
                                <i class="bi bi-eye input-icon-toggle" data-target="confirmPassword" title="Toggle visibility"></i>
                            </div>
                            <div class="invalid-feedback">Please confirm your password.</div>
                        </div>
                    </div>

                    <!-- Section: Location Information -->
                    <h5 class="fw-bold mb-3 pt-2 pb-1 border-bottom" style="font-size: 1rem; color: var(--primary-red);">
                        2. Location &amp; Address
                    </h5>

                    <div class="row g-3 mb-3">
                        <div class="col-md-5">
                            <label for="city" class="form-label">City</label>
                            <input type="text" class="form-control" id="city" name="city" 
                                   placeholder="e.g. Bangalore" required>
                            <div class="invalid-feedback">City is required.</div>
                        </div>
                        <div class="col-md-4">
                            <label for="state" class="form-label">State</label>
                            <input type="text" class="form-control" id="state" name="state" 
                                   placeholder="e.g. Karnataka" required>
                            <div class="invalid-feedback">State is required.</div>
                        </div>
                        <div class="col-md-3">
                            <label for="pincode" class="form-label">Pincode</label>
                            <input type="text" class="form-control" id="pincode" name="pincode" 
                                   placeholder="560001" pattern="[0-9]{6}" required>
                            <div class="invalid-feedback">6-digit pincode.</div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="address" class="form-label">Street Address / Locality</label>
                        <textarea class="form-control" id="address" name="address" rows="2" 
                                  placeholder="Hospital vicinity or area name" required></textarea>
                    </div>

                    <!-- Section: Donor Specific Details (Dynamic Container) -->
                    <div id="donorSpecificFields">
                        <h5 class="fw-bold mb-3 pt-2 pb-1 border-bottom" style="font-size: 1rem; color: var(--primary-red);">
                            3. Donor Medical Profile
                        </h5>

                        <div class="mb-3">
                            <label class="form-label d-block">Select Blood Group <span class="text-danger">*</span></label>
                            <div class="blood-group-selector">
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-ap" name="bloodGroup" value="A+" checked>
                                    <label class="blood-option-label" for="bg-ap">A+</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-an" name="bloodGroup" value="A-">
                                    <label class="blood-option-label" for="bg-an">A-</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-bp" name="bloodGroup" value="B+">
                                    <label class="blood-option-label" for="bg-bp">B+</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-bn" name="bloodGroup" value="B-">
                                    <label class="blood-option-label" for="bg-bn">B-</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-abp" name="bloodGroup" value="AB+">
                                    <label class="blood-option-label" for="bg-abp">AB+</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-abn" name="bloodGroup" value="AB-">
                                    <label class="blood-option-label" for="bg-abn">AB-</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-op" name="bloodGroup" value="O+">
                                    <label class="blood-option-label" for="bg-op">O+</label>
                                </div>
                                <div>
                                    <input type="radio" class="blood-option-input" id="bg-on" name="bloodGroup" value="O-">
                                    <label class="blood-option-label" for="bg-on">O-</label>
                                </div>
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-4">
                                <label for="age" class="form-label">Age (18 - 65)</label>
                                <input type="number" class="form-control" id="age" name="age" 
                                       min="18" max="65" value="25" data-conditional-required="true">
                                <div class="invalid-feedback">Eligible age: 18 - 65.</div>
                            </div>
                            <div class="col-md-4">
                                <label for="gender" class="form-label">Gender</label>
                                <select class="form-select" id="gender" name="gender" data-conditional-required="true">
                                    <option value="MALE">Male</option>
                                    <option value="FEMALE">Female</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label for="lastDonationDate" class="form-label">Last Donation Date</label>
                                <input type="date" class="form-control" id="lastDonationDate" name="lastDonationDate">
                                <div class="form-text small">Leave blank if first-time donor</div>
                            </div>
                        </div>

                        <div class="mb-3 form-check">
                            <input type="checkbox" class="form-check-input" id="isAvailable" name="isAvailable" checked>
                            <label class="form-check-label small" for="isAvailable">
                                <strong>Available for Emergency Calls</strong> (Notify me when urgent matching requests are posted in my city)
                            </label>
                        </div>
                    </div>

                    <!-- Terms & Eligibility Pledge -->
                    <div class="mb-4 form-check pt-2">
                        <input type="checkbox" class="form-check-input" id="termsCheck" required>
                        <label class="form-check-label small text-muted" for="termsCheck">
                            I confirm that the provided medical and personal details are accurate, and I agree to the voluntary blood donation coordination guidelines.
                        </label>
                        <div class="invalid-feedback">You must accept the terms before creating an account.</div>
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" class="btn btn-primary-red w-100 py-2 mb-3">
                        <i class="bi bi-person-check-fill me-1"></i> Complete Registration
                    </button>
                </form>

                <!-- Login Redirect Prompt -->
                <div class="text-center pt-3 border-top" style="border-color: var(--border-color) !important;">
                    <p class="small text-muted mb-0">
                        Already have an account? 
                        <a href="${pageContext.request.contextPath}/login.jsp" class="fw-semibold">Sign in here</a>
                    </p>
                </div>
            </div>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

    <script>
        // Preset role from URL query param if present (?role=DONOR / ?role=REQUESTER)
        const urlParams = new URLSearchParams(window.location.search);
        const roleParam = urlParams.get('role');
        if (roleParam) {
            const targetBtn = document.querySelector(`.role-toggle-btn[data-role="${roleParam.toUpperCase()}"]`);
            if (targetBtn) {
                targetBtn.click();
            }
        }
    </script>
</body>
</html>
