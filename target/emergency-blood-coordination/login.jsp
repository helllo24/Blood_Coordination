<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In - Emergency Blood Coordination System</title>
    <jsp:include page="includes/header.jsp" />
</head>
<body>

    <!-- Professional Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Main Auth Container -->
    <main class="auth-wrapper">
        <div class="container">
            <div class="auth-card">
                <div class="auth-header">
                    <div class="auth-icon">
                        <i class="bi bi-shield-lock"></i>
                    </div>
                    <h2>Welcome Back</h2>
                    <p>Select your account role and sign in to access your dashboard</p>
                </div>

                <!-- Alert Messages -->
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger custom-alert d-flex align-items-center" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                        <div>
                            <c:out value="${param.error eq 'invalid' ? 'Invalid email, password, or role selection. Please try again.' : param.error}" />
                        </div>
                    </div>
                </c:if>

                <c:if test="${not empty param.registered}">
                    <div class="alert alert-success custom-alert d-flex align-items-center border-success" role="alert" style="border-left-color: #2e7d32;">
                        <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                        <div>Registration successful! You may now sign in with your credentials.</div>
                    </div>
                </c:if>

                <!-- Role Selector Tabs -->
                <div class="role-toggle-group">
                    <button type="button" class="role-toggle-btn active" data-role="DONOR">
                        <i class="bi bi-heart me-1"></i> Donor
                    </button>
                    <button type="button" class="role-toggle-btn" data-role="REQUESTER">
                        <i class="bi bi-clipboard-pulse me-1"></i> Requester
                    </button>
                    <button type="button" class="role-toggle-btn" data-role="ADMIN">
                        <i class="bi bi-shield-shaded me-1"></i> Admin
                    </button>
                </div>

                <!-- Login Form -->
                <form action="${pageContext.request.contextPath}/login" method="post" class="needs-validation" novalidate>
                    <!-- Hidden field storing the active role -->
                    <input type="hidden" name="role" id="selectedRoleInput" value="DONOR">

                    <!-- Email Field -->
                    <div class="mb-3">
                        <label for="email" class="form-label">Email Address</label>
                        <div class="input-group">
                            <span class="input-group-text bg-white border-end-0" style="border-color: var(--border-color);">
                                <i class="bi bi-envelope text-muted"></i>
                            </span>
                            <input type="email" class="form-control border-start-0" id="email" name="email" 
                                   placeholder="name@example.com" required autocomplete="email">
                        </div>
                        <div class="invalid-feedback">Please enter a valid email address.</div>
                    </div>

                    <!-- Password Field -->
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <label for="password" class="form-label mb-0">Password</label>
                            <a href="#" class="small text-muted" onclick="alert('Password reset assistance: Please contact your college administrator or check seed credentials below.'); return false;">Forgot?</a>
                        </div>
                        <div class="input-icon-group">
                            <input type="password" class="form-control" id="password" name="password" 
                                   placeholder="Enter your password" required autocomplete="current-password">
                            <i class="bi bi-eye input-icon-toggle" data-target="password" title="Toggle visibility"></i>
                        </div>
                        <div class="invalid-feedback">Password is required.</div>
                    </div>

                    <!-- Remember Me Checkbox -->
                    <div class="mb-4 form-check">
                        <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                        <label class="form-check-label small text-muted" for="rememberMe">Remember me on this browser</label>
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" class="btn btn-primary-red w-100 py-2 mb-3">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Sign In
                    </button>
                </form>

                <!-- Registration Prompt -->
                <div class="text-center pt-3 border-top" style="border-color: var(--border-color) !important;">
                    <p class="small text-muted mb-0">
                        Don't have an account? 
                        <a href="${pageContext.request.contextPath}/register.jsp" class="fw-semibold">Register here</a>
                    </p>
                </div>
            </div>

            <!-- Academic Demo Credentials Helper Box -->
            <div class="text-center mt-4">
                <div class="card p-3 mx-auto border-0 shadow-sm" style="max-width: 580px; background-color: var(--white); border-radius: var(--radius-md);">
                    <div class="small fw-semibold text-secondary mb-1">
                        <i class="bi bi-info-circle-fill me-1 text-danger"></i> Academic Seed Test Credentials:
                    </div>
                    <div class="small text-muted">
                        <strong>Admin:</strong> admin@bloodconnect.org | Admin@123<br>
                        <strong>Donor:</strong> rahul.donor@gmail.com | Donor@123<br>
                        <strong>Requester:</strong> vikram.req@gmail.com | Req@123
                    </div>
                </div>
            </div>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

    <script>
        // Preset role from URL query param if present (?role=DONOR / ?role=REQUESTER / ?role=ADMIN)
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
