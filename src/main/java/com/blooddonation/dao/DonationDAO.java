package com.blooddonation.dao;

import com.blooddonation.model.Donation;
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
 * Data Access Object (DAO) for Blood Donation records,
 * supporting audit trail creation, donor-specific histories,
 * administrative filtering, and aggregate analytics.
 */
public class DonationDAO {

    /**
     * CREATE: Records a new official blood donation event.
     * If the status is COMPLETED, automatically updates the donor's
     * last donation date and increments total donation count.
     *
     * @param donation Donation entity
     * @return Generated donation ID or null
     */
    public Long save(Donation donation) {
        String sql = "INSERT INTO donations (donor_id, request_id, units_donated, donation_date, hospital_or_camp, status, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, donation.getDonorId());
            if (donation.getRequestId() != null) {
                ps.setLong(2, donation.getRequestId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setInt(3, donation.getUnitsDonated() > 0 ? donation.getUnitsDonated() : 1);
            ps.setDate(4, donation.getDonationDate());
            ps.setString(5, donation.getHospitalOrCamp());
            ps.setString(6, donation.getStatus() != null ? donation.getStatus() : "COMPLETED");
            ps.setString(7, donation.getRemarks());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        donation.setId(id);

                        // If completed, update the donor's stats in donors table
                        if ("COMPLETED".equalsIgnoreCase(donation.getStatus())) {
                            updateDonorStatsOnDonation(donation.getDonorId(), donation.getDonationDate());
                        }

                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error saving donation: " + e.getMessage());
        }
        return null;
    }

    /**
     * READ: Finds a donation record by ID with donor and request details.
     *
     * @param id Donation ID
     * @return Optional Donation
     */
    public Optional<Donation> findById(Long id) {
        String sql = "SELECT don.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group, " +
                     "br.patient_name " +
                     "FROM donations don " +
                     "JOIN donors d ON don.donor_id = d.id " +
                     "JOIN users u ON d.user_id = u.id " +
                     "LEFT JOIN blood_requests br ON don.request_id = br.id " +
                     "WHERE don.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDonation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error finding donation by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * READ: Retrieves donation history for a specific donor.
     *
     * @param donorId Donor ID
     * @return List of Donations
     */
    public List<Donation> findByDonorId(Long donorId) {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT don.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group, " +
                     "br.patient_name " +
                     "FROM donations don " +
                     "JOIN donors d ON don.donor_id = d.id " +
                     "JOIN users u ON d.user_id = u.id " +
                     "LEFT JOIN blood_requests br ON don.request_id = br.id " +
                     "WHERE don.donor_id = ? " +
                     "ORDER BY don.donation_date DESC, don.id DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error finding donations by donor: " + e.getMessage());
        }
        return list;
    }

    /**
     * READ: Retrieves all donation records with donor and request details.
     *
     * @return List of Donations
     */
    public List<Donation> findAll() {
        return filterDonations(null, null, null);
    }

    /**
     * SEARCH / FILTER: Multi-criteria filtering by donation status and date range.
     *
     * @param status Optional status filter (COMPLETED, SCHEDULED, CANCELLED)
     * @param fromDate Optional start date
     * @param toDate Optional end date
     * @return List of matching Donations
     */
    public List<Donation> filterDonations(String status, Date fromDate, Date toDate) {
        List<Donation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT don.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group, " +
            "br.patient_name " +
            "FROM donations don " +
            "JOIN donors d ON don.donor_id = d.id " +
            "JOIN users u ON d.user_id = u.id " +
            "LEFT JOIN blood_requests br ON don.request_id = br.id " +
            "WHERE 1=1"
        );

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND don.status = ?");
        }
        if (fromDate != null) {
            sql.append(" AND don.donation_date >= ?");
        }
        if (toDate != null) {
            sql.append(" AND don.donation_date <= ?");
        }

        sql.append(" ORDER BY don.donation_date DESC, don.id DESC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIdx++, status.trim().toUpperCase());
            }
            if (fromDate != null) {
                ps.setDate(paramIdx++, fromDate);
            }
            if (toDate != null) {
                ps.setDate(paramIdx++, toDate);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error filtering donations: " + e.getMessage());
        }
        return list;
    }

    /**
     * UPDATE: Updates an official donation record's clinical parameters.
     *
     * @param donation Updated donation entity
     * @return true if updated
     */
    public boolean update(Donation donation) {
        String sql = "UPDATE donations SET units_donated = ?, donation_date = ?, hospital_or_camp = ?, " +
                     "status = ?, remarks = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, donation.getUnitsDonated());
            ps.setDate(2, donation.getDonationDate());
            ps.setString(3, donation.getHospitalOrCamp());
            ps.setString(4, donation.getStatus());
            ps.setString(5, donation.getRemarks());
            ps.setLong(6, donation.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error updating donation: " + e.getMessage());
        }
        return false;
    }

    /**
     * UPDATE: Updates the status and clinical remarks of a donation record.
     *
     * @param id Donation ID
     * @param status New status (COMPLETED, SCHEDULED, CANCELLED)
     * @param remarks Clinical remarks
     * @return true if updated
     */
    public boolean updateStatus(Long id, String status, String remarks) {
        String sql = "UPDATE donations SET status = ?, remarks = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setLong(3, id);

            boolean updated = ps.executeUpdate() > 0;
            if (updated && "COMPLETED".equalsIgnoreCase(status)) {
                findById(id).ifPresent(don -> updateDonorStatsOnDonation(don.getDonorId(), don.getDonationDate()));
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error updating donation status: " + e.getMessage());
        }
        return false;
    }

    /**
     * DELETE / CANCEL: Soft-cancels a donation record without deleting audit history.
     *
     * @param id Donation ID
     * @return true if cancelled
     */
    public boolean cancelDonation(Long id) {
        return updateStatus(id, "CANCELLED", "Cancelled by authorized medical administrator.");
    }

    /**
     * Returns aggregate count of all completed blood donations.
     *
     * @return total count
     */
    public int getTotalDonationsCount() {
        String sql = "SELECT COUNT(*) FROM donations WHERE status = 'COMPLETED'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error counting donations: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calculates total units of blood contributed by a specific donor.
     *
     * @param donorId Donor ID
     * @return Total units donated
     */
    public int getTotalUnitsDonatedByDonor(Long donorId) {
        String sql = "SELECT COALESCE(SUM(units_donated), 0) FROM donations WHERE donor_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error calculating donor units: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calculates total units of blood donated system-wide across all completed records.
     *
     * @return System-wide total blood units
     */
    public int getTotalUnitsDonatedSystemWide() {
        String sql = "SELECT COALESCE(SUM(units_donated), 0) FROM donations WHERE status = 'COMPLETED'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error calculating system-wide units: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts donation records by status (e.g. COMPLETED, SCHEDULED, CANCELLED).
     *
     * @param status Status name
     * @return count of matching records
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM donations WHERE status = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error counting donations by status: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the most recently completed blood donations for recent activities feed.
     *
     * @param limit maximum records to fetch
     * @return list of recent donations
     */
    public List<Donation> findRecentDonations(int limit) {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT don.*, u.full_name AS donor_name, u.phone AS donor_phone, d.blood_group, " +
                     "br.patient_name " +
                     "FROM donations don " +
                     "JOIN donors d ON don.donor_id = d.id " +
                     "JOIN users u ON d.user_id = u.id " +
                     "LEFT JOIN blood_requests br ON don.request_id = br.id " +
                     "ORDER BY don.donation_date DESC, don.id DESC LIMIT ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error fetching recent donations: " + e.getMessage());
        }
        return list;
    }

    /**
     * Calculates monthly aggregation of completed blood donations and units collected.
     *
     * @return Map of 'YYYY-MM' -> int[]{donationsCount, unitsCollected}
     */
    public Map<String, int[]> getDonationsByMonth() {
        Map<String, int[]> monthlyMap = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(donation_date, '%Y-%m') AS don_month, " +
                     "COUNT(*) AS don_count, " +
                     "COALESCE(SUM(units_donated), 0) AS units_sum " +
                     "FROM donations WHERE status = 'COMPLETED' " +
                     "GROUP BY don_month ORDER BY don_month ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String month = rs.getString("don_month");
                int count = rs.getInt("don_count");
                int units = rs.getInt("units_sum");
                monthlyMap.put(month, new int[]{count, units});
            }
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error fetching monthly donations: " + e.getMessage());
        }
        return monthlyMap;
    }

    private void updateDonorStatsOnDonation(Long donorId, Date donationDate) {
        String sql = "UPDATE donors SET last_donation_date = ?, total_donations = total_donations + 1 WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, donationDate);
            ps.setLong(2, donorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DonationDAO] Error updating donor stats: " + e.getMessage());
        }
    }

    private Donation mapResultSetToDonation(ResultSet rs) throws SQLException {
        Donation don = new Donation();
        don.setId(rs.getLong("id"));
        don.setDonorId(rs.getLong("donor_id"));
        long reqId = rs.getLong("request_id");
        if (!rs.wasNull()) {
            don.setRequestId(reqId);
        }
        don.setUnitsDonated(rs.getInt("units_donated"));
        don.setDonationDate(rs.getDate("donation_date"));
        don.setHospitalOrCamp(rs.getString("hospital_or_camp"));
        don.setStatus(rs.getString("status"));
        don.setRemarks(rs.getString("remarks"));
        don.setCreatedAt(rs.getTimestamp("created_at"));

        // Safely extract joined columns if present
        try {
            don.setDonorName(rs.getString("donor_name"));
        } catch (SQLException ignored) {}

        try {
            don.setDonorPhone(rs.getString("donor_phone"));
        } catch (SQLException ignored) {}

        try {
            don.setBloodGroup(rs.getString("blood_group"));
        } catch (SQLException ignored) {}

        try {
            don.setPatientName(rs.getString("patient_name"));
        } catch (SQLException ignored) {}

        return don;
    }
}
