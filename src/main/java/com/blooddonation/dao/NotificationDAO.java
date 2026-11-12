package com.blooddonation.dao;

import com.blooddonation.model.Notification;
import com.blooddonation.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Notification management.
 */
public class NotificationDAO {

    /**
     * Saves a new notification.
     *
     * @param notif Notification entity
     * @return Generated notification ID or null
     */
    public Long save(Notification notif) {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, notif.getUserId());
            ps.setString(2, notif.getTitle());
            ps.setString(3, notif.getMessage());
            ps.setString(4, notif.getType() != null ? notif.getType() : "GENERAL");
            ps.setBoolean(5, notif.isRead());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        notif.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error saving notification: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all notifications for a given user.
     *
     * @param userId User ID
     * @return List of Notifications
     */
    public List<Notification> findByUserId(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setUserId(rs.getLong("user_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setType(rs.getString("type"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error retrieving notifications: " + e.getMessage());
        }
        return list;
    }

    /**
     * Counts unread notifications for a user.
     *
     * @param userId User ID
     * @return unread count
     */
    public int countUnreadByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error counting unread notifications: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Marks a notification as read.
     *
     * @param id Notification ID
     * @return true if updated
     */
    public boolean markAsRead(Long id) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error marking notification as read: " + e.getMessage());
        }
        return false;
    }

    /**
     * Marks all unread notifications as read for a given user.
     *
     * @param userId User ID
     * @return true if one or more notifications were marked as read
     */
    public boolean markAllAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error marking all notifications as read: " + e.getMessage());
        }
        return false;
    }

    /**
     * Finds a single notification by primary key ID.
     *
     * @param id Notification ID
     * @return Optional Notification
     */
    public java.util.Optional<Notification> findById(Long id) {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setUserId(rs.getLong("user_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setType(rs.getString("type"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    return java.util.Optional.of(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error finding notification by id: " + e.getMessage());
        }
        return java.util.Optional.empty();
    }

    /**
     * Deletes a notification by ID.
     *
     * @param id Notification ID
     * @return true if deleted
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error deleting notification: " + e.getMessage());
        }
        return false;
    }
}
