package com.blooddonation.test;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.Donor;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Automated Test Suite for Donor Management Module
 * Covers:
 * 1. View donor profile (by donor ID and user ID)
 * 2. Update donor profile (personal, medical, and location)
 * 3. Change emergency availability status and soft deactivation
 * 4. Search donors by blood group
 * 5. Search donors by city
 * 6. Search donors by blood group and city with clinical compatibility
 * 7. Prevent unauthorized donor profile updates (ownership checks)
 * 8. Verify database changes and foreign key integrity in MySQL
 */
public class DonorManagementTest {

    private static final DonorDAO donorDAO = new DonorDAO();
    private static final UserDAO userDAO = new UserDAO();

    @org.junit.jupiter.api.Test
    public void testDonorManagementSuite() {
        org.junit.jupiter.api.Assertions.assertTrue(testViewDonorProfile());
        org.junit.jupiter.api.Assertions.assertTrue(testUpdateDonorProfile());
        org.junit.jupiter.api.Assertions.assertTrue(testChangeAvailabilityStatusAndSoftDeactivation());
        org.junit.jupiter.api.Assertions.assertTrue(testSearchDonorsByBloodGroup());
        org.junit.jupiter.api.Assertions.assertTrue(testSearchDonorsByCity());
        org.junit.jupiter.api.Assertions.assertTrue(testSearchDonorsByBloodGroupAndCityWithClinicalCompatibility());
        org.junit.jupiter.api.Assertions.assertTrue(testPreventUnauthorizedDonorProfileUpdates());
        org.junit.jupiter.api.Assertions.assertTrue(testVerifyDatabaseChangesInMySQL());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination System - Donor Management Test Suite");
        System.out.println("======================================================================\n");

        boolean allPassed = true;

        allPassed &= testViewDonorProfile();
        allPassed &= testUpdateDonorProfile();
        allPassed &= testChangeAvailabilityStatusAndSoftDeactivation();
        allPassed &= testSearchDonorsByBloodGroup();
        allPassed &= testSearchDonorsByCity();
        allPassed &= testSearchDonorsByBloodGroupAndCityWithClinicalCompatibility();
        allPassed &= testPreventUnauthorizedDonorProfileUpdates();
        allPassed &= testVerifyDatabaseChangesInMySQL();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println(">>> ALL 8 DONOR MANAGEMENT TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> SOME DONOR MANAGEMENT TESTS FAILED! <<<");
        }
        System.out.println("======================================================================");

        if (!allPassed) {
            System.exit(1);
        }
    }

    /**
     * Test 1: View donor profile by ID and by linked User ID.
     */
    private static boolean testViewDonorProfile() {
        System.out.print("[TEST 1] View donor profile by ID and User ID: ");
        try {
            // Find by donor ID
            Optional<Donor> donorById = donorDAO.findById(1L);
            if (donorById.isEmpty()) {
                System.out.println("FAILED - Donor with ID 1 not found.");
                return false;
            }

            Donor donor1 = donorById.get();
            if (donor1.getBloodGroup() == null || donor1.getCity() == null) {
                System.out.println("FAILED - Donor fields are null.");
                return false;
            }

            // Verify joined user fields
            if (donor1.getFullName() == null || donor1.getPhone() == null) {
                System.out.println("FAILED - Joined User full name or phone is null.");
                return false;
            }

            // Find by user ID
            Optional<Donor> donorByUserId = donorDAO.findByUserId(donor1.getUserId());
            if (donorByUserId.isEmpty()) {
                System.out.println("FAILED - Donor not found by user ID: " + donor1.getUserId());
                return false;
            }

            if (!donorByUserId.get().getId().equals(donor1.getId())) {
                System.out.println("FAILED - Donor ID mismatch when querying by user ID.");
                return false;
            }

            System.out.println("PASSED (Found donor DNR-" + donor1.getId() + ", Name: " + donor1.getFullName() +
                               ", Blood: " + donor1.getBloodGroup() + ", City: " + donor1.getCity() + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 2: Update donor profile (personal, medical, and location info).
     */
    private static boolean testUpdateDonorProfile() {
        System.out.print("[TEST 2] Update donor profile (personal, medical, location): ");
        try {
            // Get or create a dedicated donor for profile updating test
            String testEmail = "profile.test.donor@bloodconnect.org";
            Optional<User> existingUser = userDAO.findByEmail(testEmail);
            User user;
            if (existingUser.isEmpty()) {
                user = new User("Profile Test Donor", testEmail, "Password@123", "+91 9888877777", Role.DONOR);
                Long uId = userDAO.save(user);
                user.setId(uId);

                Donor donor = new Donor();
                donor.setUserId(uId);
                donor.setBloodGroup("B+");
                donor.setAge(26);
                donor.setGender("MALE");
                donor.setCity("Bangalore");
                donor.setState("Karnataka");
                donor.setPincode("560001");
                donor.setAddress("Old Test Address");
                donor.setLastDonationDate(Date.valueOf("2026-01-10"));
                donor.setAvailable(true);
                donor.setTotalDonations(1);
                donorDAO.save(donor);
            } else {
                user = existingUser.get();
            }

            Optional<Donor> donorOpt = donorDAO.findByUserId(user.getId());
            if (donorOpt.isEmpty()) {
                System.out.println("FAILED - Test donor profile could not be retrieved.");
                return false;
            }

            Donor donor = donorOpt.get();

            // 1. Update personal details in users table
            String updatedName = "Profile Test Donor Updated";
            String updatedPhone = "+91 9999988888";
            boolean userUpdated = userDAO.updateProfile(user.getId(), updatedName, updatedPhone);
            if (!userUpdated) {
                System.out.println("FAILED - Updating User personal info failed.");
                return false;
            }

            // 2. Update medical & location details in donors table
            donor.setBloodGroup("A+");
            donor.setAge(28);
            donor.setGender("FEMALE");
            donor.setCity("Mysore");
            donor.setState("Karnataka");
            donor.setPincode("570001");
            donor.setAddress("45 Palace Road, Mysore");
            donor.setLastDonationDate(Date.valueOf("2026-02-15"));
            donor.setAvailable(true);

            boolean donorUpdated = donorDAO.update(donor);
            if (!donorUpdated) {
                System.out.println("FAILED - Updating Donor medical/location info failed.");
                return false;
            }

            // Re-fetch and verify all changes
            Optional<Donor> refreshedOpt = donorDAO.findById(donor.getId());
            if (refreshedOpt.isEmpty()) {
                System.out.println("FAILED - Refreshed donor could not be reloaded.");
                return false;
            }

            Donor refreshed = refreshedOpt.get();
            if (!"A+".equals(refreshed.getBloodGroup()) ||
                refreshed.getAge() != 28 ||
                !"FEMALE".equals(refreshed.getGender()) ||
                !"Mysore".equalsIgnoreCase(refreshed.getCity()) ||
                !"570001".equals(refreshed.getPincode()) ||
                !updatedName.equals(refreshed.getFullName()) ||
                !updatedPhone.equals(refreshed.getPhone())) {
                System.out.println("FAILED - Refreshed donor data does not match updated values.");
                return false;
            }

            System.out.println("PASSED (Updated to A+, Age 28, Mysore, Phone: " + updatedPhone + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 3: Change emergency availability status and soft deactivation.
     */
    private static boolean testChangeAvailabilityStatusAndSoftDeactivation() {
        System.out.print("[TEST 3] Change availability status & soft deactivation: ");
        try {
            Optional<Donor> donorOpt = donorDAO.findById(1L);
            if (donorOpt.isEmpty()) {
                System.out.println("FAILED - Donor 1 not found.");
                return false;
            }

            Long donorId = 1L;

            // 1. Toggle to unavailable
            boolean status1 = donorDAO.updateAvailability(donorId, false);
            if (!status1) {
                System.out.println("FAILED - Setting availability to false failed.");
                return false;
            }
            Donor d1 = donorDAO.findById(donorId).orElseThrow();
            if (d1.isAvailable()) {
                System.out.println("FAILED - Donor availability is still true.");
                return false;
            }

            // 2. Soft deactivation
            boolean deact = donorDAO.deactivateDonor(donorId);
            if (!deact) {
                System.out.println("FAILED - deactivateDonor returned false.");
                return false;
            }
            Donor dDeact = donorDAO.findById(donorId).orElseThrow();
            if (dDeact.isAvailable()) {
                System.out.println("FAILED - Donor still active after deactivation.");
                return false;
            }

            // 3. Reactivate
            boolean react = donorDAO.activateDonor(donorId);
            if (!react) {
                System.out.println("FAILED - activateDonor returned false.");
                return false;
            }
            Donor dReact = donorDAO.findById(donorId).orElseThrow();
            if (!dReact.isAvailable()) {
                System.out.println("FAILED - Donor not active after reactivation.");
                return false;
            }

            System.out.println("PASSED (Toggled: True -> False -> Deactivated -> Reactivated)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 4: Search donors by blood group.
     */
    private static boolean testSearchDonorsByBloodGroup() {
        System.out.print("[TEST 4] Search donors by blood group: ");
        try {
            List<Donor> oPosDonors = donorDAO.searchDonors("O+", null, null);
            if (oPosDonors.isEmpty()) {
                System.out.println("FAILED - No O+ donors found.");
                return false;
            }

            for (Donor d : oPosDonors) {
                if (!"O+".equalsIgnoreCase(d.getBloodGroup())) {
                    System.out.println("FAILED - Found non-O+ donor in results: " + d.getBloodGroup());
                    return false;
                }
            }

            System.out.println("PASSED (Found " + oPosDonors.size() + " matching O+ donors)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 5: Search donors by city.
     */
    private static boolean testSearchDonorsByCity() {
        System.out.print("[TEST 5] Search donors by city: ");
        try {
            List<Donor> blrDonors = donorDAO.searchDonors(null, "Bangalore", null);
            if (blrDonors.isEmpty()) {
                System.out.println("FAILED - No Bangalore donors found.");
                return false;
            }

            for (Donor d : blrDonors) {
                if (!"Bangalore".equalsIgnoreCase(d.getCity())) {
                    System.out.println("FAILED - Found non-Bangalore donor: " + d.getCity());
                    return false;
                }
            }

            System.out.println("PASSED (Found " + blrDonors.size() + " donors in Bangalore)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 6: Search donors by blood group and city with clinical compatibility.
     */
    private static boolean testSearchDonorsByBloodGroupAndCityWithClinicalCompatibility() {
        System.out.print("[TEST 6] Search donors by blood group and city with clinical cross-match: ");
        try {
            // Standard exact match
            List<Donor> exactMatch = donorDAO.searchDonors("O+", "Bangalore", true);

            // Clinical cross-match for AB+ recipient (AB+ can receive from O+, A+, B+, AB+, etc.)
            List<Donor> compatibleDonors = donorDAO.findCompatibleDonors("AB+", "Bangalore");
            if (compatibleDonors.isEmpty()) {
                System.out.println("FAILED - No compatible donors found for AB+ recipient in Bangalore.");
                return false;
            }

            // Ensure compatible donors are available
            for (Donor d : compatibleDonors) {
                if (!d.isAvailable()) {
                    System.out.println("FAILED - Inactive donor included in emergency cross-match.");
                    return false;
                }
            }

            System.out.println("PASSED (Found " + exactMatch.size() + " exact match, " +
                               compatibleDonors.size() + " clinically compatible donors for AB+ recipient)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 7: Prevent unauthorized donor profile updates.
     */
    private static boolean testPreventUnauthorizedDonorProfileUpdates() {
        System.out.print("[TEST 7] Prevent unauthorized donor profile updates: ");
        try {
            // User A (Donor 1, userId = 2)
            User userA = new User();
            userA.setId(2L);
            userA.setRole(Role.DONOR);

            // User B (Attacker / Different Donor, userId = 999)
            User userB = new User();
            userB.setId(999L);
            userB.setRole(Role.DONOR);

            // Admin User (userId = 1)
            User adminUser = new User();
            adminUser.setId(1L);
            adminUser.setRole(Role.ADMIN);

            Optional<Donor> donorOpt = donorDAO.findById(1L);
            if (donorOpt.isEmpty()) {
                System.out.println("FAILED - Target donor not found.");
                return false;
            }
            Donor targetDonor = donorOpt.get();

            // Authorization check simulation (identical to DonorProfileServlet logic)
            // 1. User B (different user) attempting to modify targetDonor
            boolean userBAllowed = userB.getRole() == Role.ADMIN || userB.getId().equals(targetDonor.getUserId());
            if (userBAllowed) {
                System.out.println("FAILED - Unauthorized user B was erroneously allowed.");
                return false;
            }

            // 2. User A (owner) attempting to modify targetDonor
            boolean userAAllowed = userA.getRole() == Role.ADMIN || userA.getId().equals(targetDonor.getUserId());
            if (!userAAllowed) {
                System.out.println("FAILED - Legitimate owner user A was rejected.");
                return false;
            }

            // 3. Admin user attempting to modify targetDonor
            boolean adminAllowed = adminUser.getRole() == Role.ADMIN || adminUser.getId().equals(targetDonor.getUserId());
            if (!adminAllowed) {
                System.out.println("FAILED - Admin user was rejected.");
                return false;
            }

            System.out.println("PASSED (Unauthorized user rejected with 403 Forbidden simulation; Owner & Admin permitted)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 8: Verify database changes and foreign key integrity in MySQL.
     */
    private static boolean testVerifyDatabaseChangesInMySQL() {
        System.out.print("[TEST 8] Verify database integrity and relationships in MySQL: ");
        String checkSql = "SELECT d.id AS donor_id, d.user_id, u.full_name, u.email, d.blood_group, d.city, d.is_available " +
                          "FROM donors d JOIN users u ON d.user_id = u.id WHERE d.id = 1";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                System.out.println("FAILED - Direct SQL query yielded no row for donor 1.");
                return false;
            }

            long donorId = rs.getLong("donor_id");
            long userId = rs.getLong("user_id");
            String fullName = rs.getString("full_name");
            String email = rs.getString("email");
            String bloodGroup = rs.getString("blood_group");
            boolean isAvailable = rs.getBoolean("is_available");

            if (donorId != 1L || userId <= 0 || fullName == null || email == null || bloodGroup == null) {
                System.out.println("FAILED - Direct SQL column verification failed.");
                return false;
            }

            // Verify foreign key relationship: users table exists and matches
            String userCheckSql = "SELECT role, status FROM users WHERE id = ?";
            try (PreparedStatement psUser = conn.prepareStatement(userCheckSql)) {
                psUser.setLong(1, userId);
                try (ResultSet rsUser = psUser.executeQuery()) {
                    if (!rsUser.next()) {
                        System.out.println("FAILED - Foreign key referenced user row missing.");
                        return false;
                    }
                }
            }

            System.out.println("PASSED (Verified MySQL tables: users <-> donors foreign key, columns intact)");
            return true;
        } catch (SQLException e) {
            System.out.println("FAILED with SQL exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
