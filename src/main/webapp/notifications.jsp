<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.blooddonation.dao.NotificationDAO" %>
<%@ page import="com.blooddonation.model.Notification" %>
<%@ page import="com.blooddonation.model.User" %>
<%@ page import="java.util.List" %>

<%
    // Session validation: Prevent unauthorized access
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20your%20notifications.");
        return;
    }

    // Direct access fallback if not forwarded by NotificationServlet
    if (request.getAttribute("notificationsList") == null) {
        NotificationDAO notificationDAO = new NotificationDAO();
        List<Notification> userNotifications = notificationDAO.findByUserId(currentUser.getId());
        int unreadCount = notificationDAO.countUnreadByUserId(currentUser.getId());
        request.setAttribute("notificationsList", userNotifications);
        request.setAttribute("unreadCount", unreadCount);
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications - BloodConnect</title>
    <jsp:include page="includes/header.jsp" />
    <style>
        .notification-card {
            background-color: var(--white);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 1.25rem;
            margin-bottom: 1rem;
            transition: all 0.2s ease;
            position: relative;
        }
        .notification-card:hover {
            box-shadow: var(--shadow-sm);
        }
        .notification-card.unread {
            background-color: #FFFDFD;
            border-left: 4px solid var(--primary-red);
        }
        .notification-icon-wrapper {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2rem;
            flex-shrink: 0;
        }
        .icon-emergency {
            background-color: var(--light-red);
            color: var(--primary-red);
        }
        .icon-response {
            background-color: #E8F5E9;
            color: #2E7D32;
        }
        .icon-status {
            background-color: #E3F2FD;
            color: #1565C0;
        }
        .icon-general {
            background-color: #F5F5F5;
            color: #455A64;
        }
    </style>
</head>
<body>

    <!-- Professional Navigation Bar -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Hero Header -->
    <section class="dashboard-hero">
        <div class="container">
            <div class="row align-items-center g-3">
                <div class="col-lg-8">
                    <span class="badge bg-danger text-white fw-semibold px-3 py-1 rounded-pill mb-2">
                        <i class="bi bi-bell-fill me-1"></i> Communication Center
                    </span>
                    <h1 class="dashboard-title">
                        System Notifications
                    </h1>
                    <p class="dashboard-subtitle">
                        Emergency requisition matches, donor coordination updates, and medical verification logs.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <c:if test="${unreadCount > 0}">
                        <form action="${pageContext.request.contextPath}/notifications" method="post" class="d-inline">
                            <input type="hidden" name="action" value="markAllAsRead">
                            <button type="submit" class="btn btn-outline-red">
                                <i class="bi bi-check2-all me-1"></i> Mark All as Read
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
    </section>

    <!-- Main Content Container -->
    <main class="container mb-5">
        <!-- Status Feedback Alerts -->
        <c:if test="${not empty param.markedAll}">
            <div class="alert alert-success alert-dismissible fade show custom-alert shadow-sm" role="alert" style="border-left-color: #2e7d32;">
                <i class="bi bi-check-circle-fill me-2 fs-5 text-success"></i>
                All notifications have been marked as read.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.deleted}">
            <div class="alert alert-secondary alert-dismissible fade show custom-alert shadow-sm" role="alert">
                <i class="bi bi-trash3-fill me-2 fs-5 text-secondary"></i>
                Notification successfully removed.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- Notification Summary Cards -->
        <div class="row g-3 mb-4">
            <div class="col-md-6">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-bell text-danger"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number text-danger">${unreadCount}</div>
                        <div class="stat-metric-label">Unread Notifications</div>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="stat-metric-card">
                    <div class="stat-icon-wrapper">
                        <i class="bi bi-inbox text-secondary"></i>
                    </div>
                    <div>
                        <div class="stat-metric-number">${notificationsList.size()}</div>
                        <div class="stat-metric-label">Total Notifications in Feed</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Notifications Feed Card -->
        <div class="content-card">
            <div class="content-card-header d-flex justify-content-between align-items-center">
                <h4 class="content-card-title text-danger mb-0">
                    <i class="bi bi-bell-fill me-2"></i> Activity Messages
                </h4>
                <span class="badge bg-light text-muted border px-2 py-1">
                    ${notificationsList.size()} Total
                </span>
            </div>

            <c:choose>
                <c:when test="${empty notificationsList}">
                    <div class="text-center py-5 text-muted">
                        <i class="bi bi-bell-slash fs-1 d-block mb-3 text-secondary"></i>
                        <h5 class="fw-semibold text-dark">No Notifications</h5>
                        <p class="small text-muted mb-0">You are completely up to date. Relevant alerts will appear here automatically.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="p-3">
                        <c:forEach var="notif" items="${notificationsList}">
                            <div class="notification-card ${notif.read ? '' : 'unread'}">
                                <div class="d-flex align-items-start gap-3">
                                    <!-- Dynamic Icon by Type -->
                                    <div class="notification-icon-wrapper 
                                        ${notif.type eq 'EMERGENCY_REQUEST' ? 'icon-emergency' : 
                                          notif.type eq 'DONOR_RESPONSE' ? 'icon-response' : 
                                          notif.type eq 'STATUS_UPDATE' ? 'icon-status' : 'icon-general'}">
                                        <c:choose>
                                            <c:when test="${notif.type eq 'EMERGENCY_REQUEST'}">
                                                <i class="bi bi-droplet-fill"></i>
                                            </c:when>
                                            <c:when test="${notif.type eq 'DONOR_RESPONSE'}">
                                                <i class="bi bi-chat-heart-fill"></i>
                                            </c:when>
                                            <c:when test="${notif.type eq 'STATUS_UPDATE'}">
                                                <i class="bi bi-arrow-repeat"></i>
                                            </c:when>
                                            <c:otherwise>
                                                <i class="bi bi-info-circle-fill"></i>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <!-- Notification Body -->
                                    <div class="flex-grow-1">
                                        <div class="d-flex justify-content-between align-items-center mb-1">
                                            <h5 class="fw-bold mb-0 text-dark" style="font-size: 1.05rem;">
                                                <c:out value="${notif.title}" />
                                                <c:if test="${not notif.read}">
                                                    <span class="badge bg-danger text-white rounded-pill ms-2" style="font-size: 0.65rem;">NEW</span>
                                                </c:if>
                                            </h5>
                                            <small class="text-muted">
                                                <i class="bi bi-clock me-1"></i><c:out value="${notif.createdAt}" />
                                            </small>
                                        </div>
                                        <p class="text-secondary small mb-2">
                                            <c:out value="${notif.message}" />
                                        </p>
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="badge bg-light text-muted border" style="font-size: 0.7rem;">
                                                <c:out value="${notif.type}" />
                                            </span>
                                            <div class="d-flex gap-2">
                                                <c:if test="${not notif.read}">
                                                    <form action="${pageContext.request.contextPath}/notifications" method="post" class="d-inline">
                                                        <input type="hidden" name="action" value="markAsRead">
                                                        <input type="hidden" name="id" value="${notif.id}">
                                                        <button type="submit" class="btn btn-outline-secondary btn-sm py-0 px-2" style="font-size: 0.75rem;">
                                                            <i class="bi bi-check2"></i> Mark as Read
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <form action="${pageContext.request.contextPath}/notifications" method="post" class="d-inline">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="${notif.id}">
                                                    <button type="submit" class="btn btn-light btn-sm py-0 px-2 text-danger" style="font-size: 0.75rem;" title="Delete notification">
                                                        <i class="bi bi-trash3"></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>

    <!-- Professional Footer -->
    <jsp:include page="includes/footer.jsp" />

</body>
</html>
