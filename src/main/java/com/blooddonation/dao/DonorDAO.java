package com.blooddonation.dao;

import com.blooddonation.model.Donor;
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
 * Data Access Object (DAO) for Donor profile management, CRUD operations,
 * and intelligent donor matching using PreparedStatements.
 */
public class DonorDAO {

    /**
     * CREATE: Persists a new donor record.
     *
     * @param donor Donor profile entity
     * @return Generated donor ID or null on failure
     */
    public Long save(Donor donor) {
        String sql = "INSERT INTO donors (user_id, blood_group, age, gender, city, state, pincode, address, last_donation_date, is_available, total_donations) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, donor.getUserId());
            ps.setString(2, donor.getBloodGroup());
            ps.setInt(3, donor.getAge());
            ps.setString(4, donor.getGender());
            ps.setString(5, donor.getCity());
            ps.setString(6, donor.getState());
            ps.setString(7, donor.getPincode());
            ps.setString(8, donor.getAddress());
            ps.setDate(9, donor.getLastDonationDate());
            ps.setBoolean(10, donor.isAvailable());
            ps.setInt(11, donor.getTotalDonations());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        donor.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error saving donor: " + e.getMessage());
        }
        return null;
    }

    /**
     * READ: Finds a donor by their primary key ID with linked user details.
     *
     * @param id Donor ID
     * @return Optional Donor
     */
    public Optional<Donor> findById(Long id) {
        String sql = "SELECT d.*, u.full_name, u.phone, u.email FROM donors d " +
                     "JOIN users u ON d.user_id = u.id WHERE d.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDonor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error finding donor by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * READ: Finds a donor profile linked to a specific user ID with linked user details.
     *
     * @param userId User account ID
     * @return Optional Donor
     */
    public Optional<Donor> findByUserId(Long userId) {
        String sql = "SELECT d.*, u.full_name, u.phone, u.email FROM donors d " +
                     "JOIN users u ON d.user_id = u.id WHERE d.user_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDonor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error finding donor by user_id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * READ: Retrieves all registered donors with linked user details.
     *
     * @return List of Donors
     */
    public List<Donor> findAll() {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name, u.phone, u.email FROM donors d " +
                     "JOIN users u ON d.user_id = u.id ORDER BY d.id ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToDonor(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error retrieving all donors: " + e.getMessage());
        }
        return list;
    }

    /**
     * UPDATE: Updates donor personal, medical, and location profile details.
     *
     * @param donor Updated donor entity
     * @return true if updated, false otherwise
     */
    public boolean update(Donor donor) {
        String sql = "UPDATE donors SET blood_group = ?, age = ?, gender = ?, city = ?, state = ?, " +
                     "pincode = ?, address = ?, last_donation_date = ?, is_available = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, donor.getBloodGroup());
            ps.setInt(2, donor.getAge());
            ps.setString(3, donor.getGender());
            ps.setString(4, donor.getCity());
            ps.setString(5, donor.getState());
            ps.setString(6, donor.getPincode());
            ps.setString(7, donor.getAddress());
            ps.setDate(8, donor.getLastDonationDate());
            ps.setBoolean(9, donor.isAvailable());
            ps.setLong(10, donor.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error updating donor profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * DELETE (Soft Delete / Deactivation):
     * Deactivates a donor profile by setting availability to false,
     * preserving valuable historical donation records.
     *
     * @param id Donor ID
     * @return true if deactivated
     */
    public boolean deactivateDonor(Long id) {
        return updateAvailability(id, false);
    }

    /**
     * Activates a donor profile by setting availability to true.
     *
     * @param id Donor ID
     * @return true if activated
     */
    public boolean activateDonor(Long id) {
        return updateAvailability(id, true);
    }

    /**
     * UPDATE: Quick toggle for donor emergency availability.
     *
     * @param id Donor ID
     * @param isAvailable true if available, false if busy
     * @return true if updated
     */
    public boolean updateAvailability(Long id, boolean isAvailable) {
        String sql = "UPDATE donors SET is_available = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isAvailable);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error updating availability: " + e.getMessage());
        }
        return false;
    }

    /**
     * Flexible multi-criteria search: Blood group, City, and Availability status.
     *
     * @param bloodGroup Optional blood group filter (e.g. O+, A-)
     * @param city Optional city filter
     * @param availableOnly If true, filters only is_available = TRUE; if false or null, returns all
     * @return List of matching Donors
     */
    public List<Donor> searchDonors(String bloodGroup, String city, Boolean availableOnly) {
        List<Donor> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT d.*, u.full_name, u.phone, u.email FROM donors d " +
            "JOIN users u ON d.user_id = u.id WHERE 1=1"
        );

        if (availableOnly != null && availableOnly) {
            sql.append(" AND d.is_available = TRUE");
        }
        if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
            sql.append(" AND d.blood_group = ?");
        }
        if (city != null && !city.trim().isEmpty()) {
            sql.append(" AND LOWER(d.city) = LOWER(?)");
        }
        sql.append(" ORDER BY d.total_donations DESC, d.id ASC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (bloodGroup != null && !bloodGroup.trim().isEmpty()) {
                ps.setString(paramIndex++, bloodGroup.trim());
            }
            if (city != null && !city.trim().isEmpty()) {
                ps.setString(paramIndex++, city.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error searching donors: " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches available donors by exact blood group and optional city.
     *
     * @param bloodGroup Blood group string
     * @param city Optional city filter
     * @return List of matching available donors
     */
    public List<Donor> findByBloodGroupAndCity(String bloodGroup, String city) {
        return searchDonors(bloodGroup, city, true);
    }

    /**
     * Clinical cross-matching: Finds clinically compatible donors for a recipient blood group
     * using the blood_compatibility matrix.
     *
     * @param recipientBloodGroup The blood group needed by the patient
     * @param city Optional city filter
     * @return List of compatible available donors
     */
    public List<Donor> findCompatibleDonors(String recipientBloodGroup, String city) {
        List<Donor> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT d.*, u.full_name, u.phone, u.email FROM donors d " +
            "JOIN users u ON d.user_id = u.id " +
            "JOIN blood_compatibility bc ON d.blood_group = bc.donor_blood_group " +
            "WHERE bc.recipient_blood_group = ? AND d.is_available = TRUE"
        );
        if (city != null && !city.trim().isEmpty()) {
            sql.append(" AND LOWER(d.city) = LOWER(?)");
        }
        sql.append(" ORDER BY d.total_donations DESC, d.id ASC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, recipientBloodGroup.trim());
            if (city != null && !city.trim().isEmpty()) {
                ps.setString(2, city.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error finding compatible donors: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds all available donors in a specific city.
     *
     * @param city City name
     * @return List of available donors in the city
     */
    public List<Donor> findAvailableDonorsByCity(String city) {
        return searchDonors(null, city, true);
    }

    /**
     * Records a new donation date and increments total donations count.
     *
     * @param id Donor ID
     * @param donationDate Date of donation
     * @return true if updated
     */
    public boolean updateLastDonation(Long id, Date donationDate) {
        String sql = "UPDATE donors SET last_donation_date = ?, total_donations = total_donations + 1 WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, donationDate);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error updating last donation: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts the total number of registered voluntary donors.
     *
     * @return total donor count
     */
    public int getTotalDonorsCount() {
        String sql = "SELECT COUNT(*) FROM donors";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error counting total donors: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the most recently registered donors for recent activity logs.
     *
     * @param limit maximum records to fetch
     * @return list of recent donors
     */
    public List<Donor> findRecentDonors(int limit) {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name, u.phone, u.email FROM donors d " +
                     "JOIN users u ON d.user_id = u.id ORDER BY d.created_at DESC, d.id DESC LIMIT ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error fetching recent donors: " + e.getMessage());
        }
        return list;
    }

    /**
     * Generates analytical distribution of donors grouped by blood group.
     * Guaranteed to include all 8 ABO-Rh blood groups.
     *
     * @return Map of blood group to registered donor count
     */
    public Map<String, Integer> getDonorsCountByBloodGroup() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        // Initialize standard ABO blood groups with zero
        String[] groups = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        for (String bg : groups) {
            counts.put(bg, 0);
        }

        String sql = "SELECT blood_group, COUNT(*) AS cnt FROM donors GROUP BY blood_group";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String bg = rs.getString("blood_group");
                int cnt = rs.getInt("cnt");
                counts.put(bg, cnt);
            }
        } catch (SQLException e) {
            System.err.println("[DonorDAO] Error fetching blood group counts: " + e.getMessage());
        }
        return counts;
    }

    private Donor mapResultSetToDonor(ResultSet rs) throws SQLException {
        Donor donor = new Donor();
        donor.setId(rs.getLong("id"));
        donor.setUserId(rs.getLong("user_id"));
        donor.setBloodGroup(rs.getString("blood_group"));
        donor.setAge(rs.getInt("age"));
        donor.setGender(rs.getString("gender"));
        donor.setCity(rs.getString("city"));
        donor.setState(rs.getString("state"));
        donor.setPincode(rs.getString("pincode"));
        donor.setAddress(rs.getString("address"));
        donor.setLastDonationDate(rs.getDate("last_donation_date"));
        donor.setAvailable(rs.getBoolean("is_available"));
        donor.setTotalDonations(rs.getInt("total_donations"));

        // Safely extract joined User columns if present
        try {
            donor.setFullName(rs.getString("full_name"));
        } catch (SQLException ignored) {}

        try {
            donor.setPhone(rs.getString("phone"));
        } catch (SQLException ignored) {}

        try {
            donor.setEmail(rs.getString("email"));
        } catch (SQLException ignored) {}

        return donor;
    }
}
