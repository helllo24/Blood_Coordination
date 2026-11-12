package com.blooddonation.dao;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Blood Request management,
 * supporting CRUD operations, soft cancellations, and multi-criteria administrative filtering.
 */
public class BloodRequestDAO {

    /**
     * CREATE: Persists a new emergency or scheduled blood request.
     *
     * @param request BloodRequest entity
     * @return Generated request ID or null
     */
    public Long save(BloodRequest request) {
        String sql = "INSERT INTO blood_requests (requester_id, patient_name, blood_group, units_needed, hospital_name, " +
                     "hospital_city, hospital_address, contact_person, contact_phone, urgency_level, status, required_date, additional_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, request.getRequesterId());
            ps.setString(2, request.getPatientName());
            ps.setString(3, request.getBloodGroup());
            ps.setInt(4, request.getUnitsNeeded());
            ps.setString(5, request.getHospitalName());
            ps.setString(6, request.getHospitalCity());
            ps.setString(7, request.getHospitalAddress());
            ps.setString(8, request.getContactPerson());
            ps.setString(9, request.getContactPhone());
            ps.setString(10, request.getUrgencyLevel() != null ? request.getUrgencyLevel() : "URGENT");
            ps.setString(11, request.getStatus() != null ? request.getStatus() : "PENDING");
            ps.setDate(12, request.getRequiredDate());
            ps.setString(13, request.getAdditionalNotes());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        request.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error saving blood request: " + e.getMessage());
        }
        return null;
    }

    /**
     * READ: Finds a blood request by its primary key ID.
     *
     * @param id Request ID
     * @return Optional BloodRequest
     */
    public Optional<BloodRequest> findById(Long id) {
        String sql = "SELECT * FROM blood_requests WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error finding request by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * READ: Retrieves all blood requests submitted by a specific requester.
     *
     * @param requesterId Requester user ID
     * @return List of BloodRequests
     */
    public List<BloodRequest> findByRequesterId(Long requesterId) {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests WHERE requester_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error fetching requests by requester: " + e.getMessage());
        }
        return list;
    }

    /**
     * READ: Retrieves all blood requests ordered by urgency and date.
     *
     * @return List of BloodRequests
     */
    public List<BloodRequest> findAll() {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests ORDER BY " +
                     "CASE urgency_level WHEN 'CRITICAL' THEN 1 WHEN 'URGENT' THEN 2 ELSE 3 END, created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error fetching all requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * READ: Finds blood requests filtered by status (e.g. PENDING, MATCHED, FULFILLED).
     *
     * @param status Status name
     * @return List of matching BloodRequests
     */
    public List<BloodRequest> findByStatus(String status) {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error finding requests by status: " + e.getMessage());
        }
        return list;
    }

    /**
     * UPDATE: Updates patient, clinical, hospital, contact and scheduling details of a pending request.
     * Only pending requests can be modified to maintain hospital audit integrity.
     *
     * @param request Updated BloodRequest entity
     * @return true if updated, false otherwise
     */
    public boolean update(BloodRequest request) {
        String sql = "UPDATE blood_requests SET patient_name = ?, blood_group = ?, units_needed = ?, " +
                     "hospital_name = ?, hospital_city = ?, hospital_address = ?, contact_person = ?, " +
                     "contact_phone = ?, urgency_level = ?, required_date = ?, additional_notes = ? " +
                     "WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getPatientName());
            ps.setString(2, request.getBloodGroup());
            ps.setInt(3, request.getUnitsNeeded());
            ps.setString(4, request.getHospitalName());
            ps.setString(5, request.getHospitalCity());
            ps.setString(6, request.getHospitalAddress());
            ps.setString(7, request.getContactPerson());
            ps.setString(8, request.getContactPhone());
            ps.setString(9, request.getUrgencyLevel());
            ps.setDate(10, request.getRequiredDate());
            ps.setString(11, request.getAdditionalNotes());
            ps.setLong(12, request.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error updating blood request: " + e.getMessage());
        }
        return false;
    }

    /**
     * UPDATE: Soft cancels a pending blood request without deleting audit trails.
     *
     * @param id Request ID
     * @return true if cancelled
     */
    public boolean cancelPendingRequest(Long id) {
        String sql = "UPDATE blood_requests SET status = 'CANCELLED' WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error cancelling pending request: " + e.getMessage());
        }
        return false;
    }

    /**
     * UPDATE: Updates status of a blood request (e.g. by Administrator).
     * Supported statuses: PENDING, MATCHED, FULFILLED, CANCELLED.
     *
     * @param id Request ID
     * @param status New status
     * @return true if updated
     */
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE blood_requests SET status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error updating status: " + e.getMessage());
        }
        return false;
    }

    /**
     * SEARCH / FILTER: Multi-criteria filtering by blood group, urgency level, status, and city.
     *
     * @param bloodGroup Optional blood group filter (e.g. O+, A-)
     * @param urgency Optional urgency filter (CRITICAL, URGENT, NORMAL)
     * @param status Optional status filter (PENDING, MATCHED, FULFILLED, CANCELLED)
     * @param city Optional hospital city filter
     * @return List of matching BloodRequests
     */
    public List<BloodRequest> searchAndFilter(String bloodGroup, String urgency, String status, String city) {
        List<BloodRequest> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM blood_requests WHERE 1=1");

        if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
            sql.append(" AND blood_group = ?");
        }
        if (urgency != null && !urgency.trim().isEmpty()) {
            sql.append(" AND urgency_level = ?");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
        }
        if (city != null && !city.trim().isEmpty()) {
            sql.append(" AND LOWER(hospital_city) = LOWER(?)");
        }

        sql.append(" ORDER BY CASE urgency_level WHEN 'CRITICAL' THEN 1 WHEN 'URGENT' THEN 2 ELSE 3 END, created_at DESC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
                ps.setString(paramIndex++, bloodGroup.trim());
            }
            if (urgency != null && !urgency.trim().isEmpty()) {
                ps.setString(paramIndex++, urgency.trim());
            }
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status.trim());
            }
            if (city != null && !city.trim().isEmpty()) {
                ps.setString(paramIndex++, city.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error filtering blood requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * Counts total blood requests with given status.
     *
     * @param status Status name
     * @return Count of matching records
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM blood_requests WHERE status = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error counting requests by status: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts the total number of blood requisitions recorded in the database.
     *
     * @return total requests count
     */
    public int getTotalRequestsCount() {
        String sql = "SELECT COUNT(*) FROM blood_requests";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error counting total requests: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the most recently submitted blood requests for recent activity logs.
     *
     * @param limit maximum records to fetch
     * @return list of recent blood requests
     */
    public List<BloodRequest> findRecentRequests(int limit) {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests ORDER BY created_at DESC, id DESC LIMIT ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error fetching recent requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * Generates distribution of blood requests grouped by status.
     *
     * @return Map of status to requisition count
     */
    public Map<String, Integer> getRequestsCountByStatus() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String[] statuses = {"PENDING", "MATCHED", "FULFILLED", "CANCELLED"};
        for (String s : statuses) {
            counts.put(s, 0);
        }

        String sql = "SELECT status, COUNT(*) AS cnt FROM blood_requests GROUP BY status";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String st = rs.getString("status");
                int cnt = rs.getInt("cnt");
                counts.put(st, cnt);
            }
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error fetching request status counts: " + e.getMessage());
        }
        return counts;
    }

    /**
     * Deletes a blood request by ID.
     *
     * @param id Request ID
     * @return true if deleted
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM blood_requests WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BloodRequestDAO] Error deleting request: " + e.getMessage());
        }
        return false;
    }

    private BloodRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        BloodRequest req = new BloodRequest();
        req.setId(rs.getLong("id"));
        req.setRequesterId(rs.getLong("requester_id"));
        req.setPatientName(rs.getString("patient_name"));
        req.setBloodGroup(rs.getString("blood_group"));
        req.setUnitsNeeded(rs.getInt("units_needed"));
        req.setHospitalName(rs.getString("hospital_name"));
        req.setHospitalCity(rs.getString("hospital_city"));
        req.setHospitalAddress(rs.getString("hospital_address"));
        req.setContactPerson(rs.getString("contact_person"));
        req.setContactPhone(rs.getString("contact_phone"));
        req.setUrgencyLevel(rs.getString("urgency_level"));
        req.setStatus(rs.getString("status"));
        req.setRequiredDate(rs.getDate("required_date"));
        req.setAdditionalNotes(rs.getString("additional_notes"));
        req.setCreatedAt(rs.getTimestamp("created_at"));
        return req;
    }
}
