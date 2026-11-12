package com.blooddonation.controller;

import com.blooddonation.dao.NotificationDAO;
import com.blooddonation.model.Notification;
import com.blooddonation.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller handling user notification management:
 * - View in-app notifications
 * - Display unread notification count
 * - Mark individual notification as read (with ownership verification)
 * - Mark all notifications as read
 * - Delete notification
 * - Enforce user data isolation (users only access their own notifications)
 */
@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications", "/notification"})
public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please%20sign%20in%20to%20view%20notifications.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        List<Notification> userNotifications = notificationDAO.findByUserId(currentUser.getId());
        int unreadCount = notificationDAO.countUnreadByUserId(currentUser.getId());

        request.setAttribute("notificationsList", userNotifications);
        request.setAttribute("unreadCount", unreadCount);

        request.getRequestDispatcher("/notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Session%20expired.%20Please%20sign%20in%20again.");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        String action = request.getParameter("action");

        if ("markAsRead".equalsIgnoreCase(action)) {
            handleMarkAsRead(request, response, currentUser);
            return;
        }

        if ("markAllAsRead".equalsIgnoreCase(action)) {
            notificationDAO.markAllAsRead(currentUser.getId());
            response.sendRedirect(request.getContextPath() + "/notifications?markedAll=true");
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            handleDelete(request, response, currentUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/notifications");
    }

    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isBlank()) {
            try {
                Long notifId = Long.parseLong(idStr.trim());
                Optional<Notification> opt = notificationDAO.findById(notifId);
                // Verify user owns this notification
                if (opt.isPresent() && opt.get().getUserId().equals(currentUser.getId())) {
                    notificationDAO.markAsRead(notifId);
                }
            } catch (NumberFormatException ignored) {}
        }
        response.sendRedirect(request.getContextPath() + "/notifications");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isBlank()) {
            try {
                Long notifId = Long.parseLong(idStr.trim());
                Optional<Notification> opt = notificationDAO.findById(notifId);
                // Verify ownership before deleting
                if (opt.isPresent() && opt.get().getUserId().equals(currentUser.getId())) {
                    notificationDAO.delete(notifId);
                }
            } catch (NumberFormatException ignored) {}
        }
        response.sendRedirect(request.getContextPath() + "/notifications?deleted=true");
    }
}
