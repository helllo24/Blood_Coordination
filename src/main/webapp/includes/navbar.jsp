<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%
    int navUnreadCount = 0;
    if (session.getAttribute("currentUser") != null) {
        com.blooddonation.model.User navUser = (com.blooddonation.model.User) session.getAttribute("currentUser");
        navUnreadCount = new com.blooddonation.dao.NotificationDAO().countUnreadByUserId(navUser.getId());
        request.setAttribute("navUnreadCount", navUnreadCount);
    }
%>

<nav class="navbar navbar-expand-lg custom-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/index.jsp">
            <i class="bi bi-droplet-fill"></i>
            <span>BloodConnect</span>
        </a>
        <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent" 
                aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarContent">
            <ul class="navbar-nav mx-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp">Home</a>
                </li>

                <!-- Role Specific Navigation -->
                <c:choose>
                    <c:when test="${sessionScope.userRole eq 'DONOR'}">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/donor-dashboard.jsp">Dashboard</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/donor-profile.jsp">My Profile</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/search-donors.jsp">Find Donors</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/donation-history.jsp">Donations</a>
                        </li>
                    </c:when>

                    <c:when test="${sessionScope.userRole eq 'REQUESTER'}">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/requester-dashboard.jsp">Dashboard</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/blood-request.jsp">Request Blood</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/search-donors.jsp">Find Donors</a>
                        </li>
                    </c:when>

                    <c:when test="${sessionScope.userRole eq 'ADMIN'}">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin-dashboard.jsp">Admin Portal</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/search-donors.jsp">All Donors</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/blood-request.jsp">Requests</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/donation-history.jsp">Audits</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/reports">Reports</a>
                        </li>
                    </c:when>

                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/search-donors.jsp">Search Donors</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp#how-it-works">How It Works</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp#blood-groups">Blood Groups</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp#about-us">About Us</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp#contact-us">Contact</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>

            <!-- Auth Buttons / User Profile Menu -->
            <div class="d-flex align-items-center gap-2">
                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">
                        <!-- Notification Bell with dynamic unread badge -->
                        <a href="${pageContext.request.contextPath}/notifications" class="btn btn-secondary-light position-relative p-2" title="Notifications">
                            <i class="bi bi-bell-fill text-danger"></i>
                            <c:if test="${navUnreadCount > 0}">
                                <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="font-size: 0.65rem;">
                                    ${navUnreadCount}
                                </span>
                            </c:if>
                        </a>

                        <div class="dropdown">
                            <button class="btn btn-secondary-light dropdown-toggle d-flex align-items-center gap-2" 
                                    type="button" id="userMenuButton" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle"></i>
                                <span class="fw-semibold small"><c:out value="${sessionScope.userName}" /></span>
                                <span class="badge bg-danger text-white ms-1" style="font-size: 0.7rem;"><c:out value="${sessionScope.userRole}" /></span>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="userMenuButton" style="border-radius: var(--radius-md); border-color: var(--border-color);">
                                <c:if test="${sessionScope.userRole eq 'DONOR'}">
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/donor-dashboard.jsp"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/donor-profile.jsp"><i class="bi bi-person-gear me-2"></i>My Profile</a></li>
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/donation-history.jsp"><i class="bi bi-clock-history me-2"></i>My Donations</a></li>
                                </c:if>
                                <c:if test="${sessionScope.userRole eq 'REQUESTER'}">
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/requester-dashboard.jsp"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/blood-request.jsp"><i class="bi bi-plus-circle me-2"></i>Create Request</a></li>
                                </c:if>
                                <c:if test="${sessionScope.userRole eq 'ADMIN'}">
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/admin-dashboard.jsp"><i class="bi bi-shield-check me-2"></i>Admin Dashboard</a></li>
                                    <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/reports"><i class="bi bi-graph-up me-2"></i>Analytics &amp; Reports</a></li>
                                </c:if>
                                <li>
                                    <a class="dropdown-item small" href="${pageContext.request.contextPath}/notifications">
                                        <i class="bi bi-bell me-2"></i>Notifications
                                        <c:if test="${navUnreadCount > 0}">
                                            <span class="badge bg-danger rounded-pill ms-2">${navUnreadCount}</span>
                                        </c:if>
                                    </a>
                                </li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <a class="dropdown-item small text-danger" href="${pageContext.request.contextPath}/logout">
                                        <i class="bi bi-box-arrow-right me-2"></i>Sign Out
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-outline-red">
                            <i class="bi bi-box-arrow-in-right me-1"></i> Sign In
                        </a>
                        <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-primary-red">
                            <i class="bi bi-person-plus me-1"></i> Register
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>
