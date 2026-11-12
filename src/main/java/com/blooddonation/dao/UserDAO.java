package com.blooddonation.dao;

import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.DBConnectionUtil;
import com.blooddonation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for User database operations using PreparedStatement.
 */
public class UserDAO {

    /**
     * Saves a new user record in the database.
     *
     * @param user User entity to persist
     * @return Generated user ID or null on failure
     */
    public Long save(User user) {
        String sql = "INSERT INTO users (full_name, email, password, phone, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash password before saving if not already hashed
            String passwordToStore = user.getPassword();
            if (passwordToStore != null && passwordToStore.length() != 64) {
                passwordToStore = PasswordUtil.hashPassword(passwordToStore);
            }

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, passwordToStore);
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        user.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error saving user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a user by their primary key ID.
     *
     * @param id User ID
     * @return Optional User
     */
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, full_name, email, password, phone, role, status, created_at FROM users WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error finding user by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their unique email address.
     *
     * @param email User email
     * @return Optional User
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, full_name, email, password, phone, role, status, created_at FROM users WHERE email = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error finding user by email: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Authenticates a user with email, raw password, and role using SHA-256 password verification.
     *
     * @param email User email
     * @param rawPassword User raw input password
     * @param role User role
     * @return Optional authenticated User or empty if invalid
     */
    public Optional<User> authenticate(String email, String rawPassword, Role role) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == role && "ACTIVE".equalsIgnoreCase(user.getStatus())) {
                if (PasswordUtil.verifyPassword(rawPassword, user.getPassword())) {
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all users.
     *
     * @return List of Users
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password, phone, role, status, created_at FROM users ORDER BY id ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching all users: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates user personal profile information (full name and phone).
     *
     * @param id User ID
     * @param fullName Full Name
     * @param phone Mobile Phone
     * @return true if updated, false otherwise
     */
    public boolean updateProfile(Long id, String fullName, String phone) {
        String sql = "UPDATE users SET full_name = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error updating user profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates user status (ACTIVE, PENDING, SUSPENDED).
     *
     * @param id User ID
     * @param status New status
     * @return true if updated, false otherwise
     */
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error updating user status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts the total number of users registered under a specific role (ADMIN, DONOR, REQUESTER).
     *
     * @param role Role enum
     * @return count of matching users
     */
    public int countByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error counting users by role: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts total users in the system.
     *
     * @return total user count
     */
    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error counting total users: " + e.getMessage());
        }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
