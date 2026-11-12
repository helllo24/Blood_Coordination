<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<footer class="custom-footer">
    <div class="container">
        <div class="row g-4">
            <div class="col-lg-4">
                <div class="footer-brand">
                    <i class="bi bi-droplet-fill"></i>
                    <span>BloodConnect</span>
                </div>
                <p class="small text-muted mb-3">
                    Emergency Blood Request and Donor Coordination System. A life-saving healthcare platform bridging voluntary donors, patients, and healthcare centers.
                </p>
                <p class="small text-muted mb-0">
                    <i class="bi bi-quote me-1"></i>Every Drop Counts. Every Donor Matters.
                </p>
            </div>
            <div class="col-6 col-lg-2 offset-lg-1">
                <h5 class="footer-title">Platform</h5>
                <ul class="footer-links">
                    <li><a href="${pageContext.request.contextPath}/index.jsp">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/search-donors.jsp">Search Donors</a></li>
                    <li><a href="${pageContext.request.contextPath}/blood-request.jsp">Request Blood</a></li>
                    <li><a href="${pageContext.request.contextPath}/donation-history.jsp">Donation History</a></li>
                    <li><a href="${pageContext.request.contextPath}/index.jsp#how-it-works">How It Works</a></li>
                </ul>
            </div>
            <div class="col-6 col-lg-2">
                <h5 class="footer-title">Access Portals</h5>
                <ul class="footer-links">
                    <li><a href="${pageContext.request.contextPath}/register.jsp?role=DONOR">Join as Donor</a></li>
                    <li><a href="${pageContext.request.contextPath}/register.jsp?role=REQUESTER">Post Blood Request</a></li>
                    <li><a href="${pageContext.request.contextPath}/login.jsp?role=DONOR">Donor Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/login.jsp?role=REQUESTER">Requester Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/login.jsp?role=ADMIN">Admin Portal</a></li>
                </ul>
            </div>
            <div class="col-lg-3">
                <h5 class="footer-title">Donor Guidelines</h5>
                <p class="small text-muted mb-2">
                    <i class="bi bi-check2-circle text-danger me-1"></i>Age between 18 and 65 years<br>
                    <i class="bi bi-check2-circle text-danger me-1"></i>Minimum body weight: 45 kg<br>
                    <i class="bi bi-check2-circle text-danger me-1"></i>Minimum 90-day interval between donations
                </p>
                <div class="p-2 border rounded-3 bg-light text-muted small mt-2">
                    <i class="bi bi-shield-fill-check text-danger me-1"></i> Voluntary, non-remunerated healthcare initiative.
                </div>
            </div>
        </div>
        <div class="footer-bottom">
            <div>
                &copy; 2026 Emergency Blood Request and Donor Coordination System.
            </div>
            <div>
                <span class="text-muted">Built with Java 17, Servlet 6.0, JSP, JDBC, MySQL &amp; Bootstrap 5</span>
            </div>
        </div>
    </div>
</footer>

<!-- Bootstrap 5.3 Bundle JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<!-- Custom Application JS -->
<script src="${pageContext.request.contextPath}/js/main.js"></script>
