package com.blooddonation.dao;

import com.blooddonation.model.DonorResponse;
import com.blooddonation.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for managing Donor responses to blood requests,
 * duplicate prevention, and response analytics.
 */
public class DonorResponseDAO {

    /**
     * Persists a donor's response (ACCEPTED, REJECTED, PENDING) to an emergency blood request.
     * Prevents duplicate responses from the same donor for the same request.
     *
     * @param resp DonorResponse entity
     * @return Generated response ID, or null if failed or duplicate
     */
    public Long save(DonorResponse resp) {
        if (hasResponded(resp.getRequestId(), resp.getDonorId())) {
            System.err.println("[DonorResponseDAO] Duplicate response blocked for Request " +
                               resp.getRequestId() + " and Donor " + resp.getDonorId());
            return null;
        }

        String sql = "INSERT INTO donor_responses (request_id, donor_id, status, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, resp.getRequestId());
            ps.setLong(2, resp.getDonorId());
            ps.setString(3, resp.getStatus() != null ? resp.getStatus() : "ACCEPTED");
            ps.setString(4, resp.getMessage());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        resp.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error saving response: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a donor has already submitted a response for a specific blood request.
     *
     * @param requestId Blood request ID
     * @param donorId Donor ID
     * @return true if response exists, false otherwise
     */
    public boolean hasResponded(Long requestId, Long donorId) {
        String sql = "SELECT COUNT(*) FROM donor_responses WHERE request_id = ? AND donor_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requestId);
            ps.setLong(2, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error checking duplicate response: " + e.getMessage());
        }
        return false;
    }

    /**
     * Finds a response by request ID and donor ID.
     *
     * @param requestId Blood request ID
     * @param donorId Donor ID
     * @return Optional DonorResponse
     */
    public Optional<DonorResponse> findByRequestIdAndDonorId(Long requestId, Long donorId) {
        String sql = "SELECT * FROM donor_responses WHERE request_id = ? AND donor_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requestId);
            ps.setLong(2, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResponse(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error finding response by request and donor: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Finds a donor response by primary key ID.
     *
     * @param id Response ID
     * @return Optional DonorResponse
     */
    public Optional<DonorResponse> findById(Long id) {
        String sql = "SELECT * FROM donor_responses WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResponse(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error finding response by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Retrieves all donor responses for a specific blood request with donor details.
     *
     * @param requestId Blood request ID
     * @return List of DonorResponses
     */
    public List<DonorResponse> findByRequestId(Long requestId) {
        List<DonorResponse> list = new ArrayList<>();
        String sql = "SELECT dr.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group AS donor_blood_group " +
                     "FROM donor_responses dr " +
                     "JOIN donors d ON dr.donor_id = d.id " +
                     "JOIN users u ON d.user_id = u.id " +
                     "WHERE dr.request_id = ? ORDER BY dr.responded_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonorResponse resp = mapResultSetToResponse(rs);
                    resp.setDonorName(rs.getString("donor_name"));
                    resp.setDonorPhone(rs.getString("donor_phone"));
                    resp.setDonorBloodGroup(rs.getString("donor_blood_group"));
                    list.add(resp);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error fetching responses by request: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all responses submitted by a specific donor.
     *
     * @param donorId Donor ID
     * @return List of DonorResponses
     */
    public List<DonorResponse> findByDonorId(Long donorId) {
        List<DonorResponse> list = new ArrayList<>();
        String sql = "SELECT dr.*, br.patient_name, br.hospital_name " +
                     "FROM donor_responses dr " +
                     "JOIN blood_requests br ON dr.request_id = br.id " +
                     "WHERE dr.donor_id = ? ORDER BY dr.responded_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonorResponse resp = mapResultSetToResponse(rs);
                    resp.setPatientName(rs.getString("patient_name"));
                    resp.setHospitalName(rs.getString("hospital_name"));
                    list.add(resp);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error fetching responses by donor: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all donor responses across all requests (for Administrator).
     *
     * @return List of all DonorResponses
     */
    public List<DonorResponse> findAll() {
        List<DonorResponse> list = new ArrayList<>();
        String sql = "SELECT dr.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group AS donor_blood_group, " +
                     "br.patient_name, br.hospital_name " +
                     "FROM donor_responses dr " +
                     "JOIN donors d ON dr.donor_id = d.id " +
                     "JOIN users u ON d.user_id = u.id " +
                     "JOIN blood_requests br ON dr.request_id = br.id " +
                     "ORDER BY dr.responded_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DonorResponse resp = mapResultSetToResponse(rs);
                resp.setDonorName(rs.getString("donor_name"));
                resp.setDonorPhone(rs.getString("donor_phone"));
                resp.setDonorBloodGroup(rs.getString("donor_blood_group"));
                resp.setPatientName(rs.getString("patient_name"));
                resp.setHospitalName(rs.getString("hospital_name"));
                list.add(resp);
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error fetching all responses: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates the status of a donor response (e.g. ACCEPTED, REJECTED, PENDING).
     *
     * @param id Response ID
     * @param status New status
     * @return true if updated
     */
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE donor_responses SET status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error updating status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts total responses matching a given status (ACCEPTED, REJECTED, PENDING).
     *
     * @param status Response status
     * @return Count of responses
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM donor_responses WHERE status = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error counting responses by status: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts responses for a specific request with a given status.
     *
     * @param requestId Blood request ID
     * @param status Response status (ACCEPTED, REJECTED, etc.)
     * @return Count of responses
     */
    public int countByRequestIdAndStatus(Long requestId, String status) {
        String sql = "SELECT COUNT(*) FROM donor_responses WHERE request_id = ? AND status = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requestId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error counting request responses by status: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts the total number of donor responses logged.
     *
     * @return count of all responses
     */
    public int getTotalResponsesCount() {
        String sql = "SELECT COUNT(*) FROM donor_responses";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error counting all responses: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the most recent donor responses for activity audit feeds.
     *
     * @param limit maximum records to fetch
     * @return list of recent donor responses with joined details
     */
    public List<DonorResponse> findRecentResponses(int limit) {
        List<DonorResponse> list = new ArrayList<>();
        String sql = "SELECT dr.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group AS donor_blood_group, " +
                     "br.patient_name, br.hospital_name " +
                     "FROM donor_responses dr " +
                     "JOIN donors d ON dr.donor_id = d.id " +
                     "JOIN users u ON d.user_id = u.id " +
                     "JOIN blood_requests br ON dr.request_id = br.id " +
                     "ORDER BY dr.responded_at DESC, dr.id DESC LIMIT ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonorResponse resp = mapResultSetToResponse(rs);
                    resp.setDonorName(rs.getString("donor_name"));
                    resp.setDonorPhone(rs.getString("donor_phone"));
                    resp.setDonorBloodGroup(rs.getString("donor_blood_group"));
                    resp.setPatientName(rs.getString("patient_name"));
                    resp.setHospitalName(rs.getString("hospital_name"));
                    list.add(resp);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorResponseDAO] Error fetching recent responses: " + e.getMessage());
        }
        return list;
    }

    private DonorResponse mapResultSetToResponse(ResultSet rs) throws SQLException {
        DonorResponse resp = new DonorResponse();
        resp.setId(rs.getLong("id"));
        resp.setRequestId(rs.getLong("request_id"));
        resp.setDonorId(rs.getLong("donor_id"));
        resp.setStatus(rs.getString("status"));
        resp.setMessage(rs.getString("message"));
        resp.setRespondedAt(rs.getTimestamp("responded_at"));
        return resp;
    }
}
