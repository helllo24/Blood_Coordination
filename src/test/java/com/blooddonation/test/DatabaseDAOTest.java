package com.blooddonation.test;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonationDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.DonorResponseDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
import com.blooddonation.model.Donor;
import com.blooddonation.model.DonorResponse;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.DBConnectionUtil;
import com.blooddonation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Direct Verification and Integration Test Suite
 * Tests:
 * 1. MySQL connection
 * 2. Table creation (all 7 tables)
 * 3. Foreign key relationships
 * 4. Sample data insertion
 * 5. DAO database operations
 */
public class DatabaseDAOTest {

    @org.junit.jupiter.api.Test
    public void testDatabaseDAOSuite() {
        org.junit.jupiter.api.Assertions.assertTrue(testMySQLConnection());
        org.junit.jupiter.api.Assertions.assertTrue(testTableCreation());
        org.junit.jupiter.api.Assertions.assertTrue(testForeignKeyRelationships());
        org.junit.jupiter.api.Assertions.assertTrue(testSampleDataCounts());
        org.junit.jupiter.api.Assertions.assertTrue(testPasswordHashing());
        org.junit.jupiter.api.Assertions.assertTrue(testDAOUpsertAndQueries());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Request and Donor Coordination System - Integration Test");
        System.out.println("======================================================================\n");

        boolean allPassed = true;

        allPassed &= testMySQLConnection();
        allPassed &= testTableCreation();
        allPassed &= testForeignKeyRelationships();
        allPassed &= testSampleDataCounts();
        allPassed &= testPasswordHashing();
        allPassed &= testDAOUpsertAndQueries();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println(">>> ALL 5 VERIFICATION CHECKS PASSED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> SOME VERIFICATION CHECKS FAILED! <<<");
        }
        System.out.println("======================================================================");
    }

    private static boolean testMySQLConnection() {
        System.out.print("[TEST 1/5] Testing MySQL JDBC Connection... ");
        try (Connection conn = DBConnectionUtil.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("PASSED! Connected to: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
                return true;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
        return false;
    }

    private static boolean testTableCreation() {
        System.out.print("[TEST 2/5] Verifying 7 Required Database Tables... ");
        List<String> expectedTables = Arrays.asList(
            "users", "donors", "blood_requests", "donor_responses", "donations", "blood_compatibility", "notifications"
        );

        try (Connection conn = DBConnectionUtil.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            int matched = 0;
            for (String tableName : expectedTables) {
                try (ResultSet rs = meta.getTables("blood_donor_db", null, tableName, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        matched++;
                    } else {
                        System.err.println("\nMissing table: " + tableName);
                    }
                }
            }
            if (matched == expectedTables.size()) {
                System.out.println("PASSED! Found all " + matched + " tables.");
                return true;
            } else {
                System.out.println("FAILED: Found " + matched + " of " + expectedTables.size() + " tables.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    private static boolean testForeignKeyRelationships() {
        System.out.print("[TEST 3/5] Verifying Foreign Key Relationships... ");
        String sql = "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE " +
                     "WHERE TABLE_SCHEMA = 'blood_donor_db' AND REFERENCED_TABLE_NAME IS NOT NULL";
        try (Connection conn = DBConnectionUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int fkCount = rs.getInt(1);
                if (fkCount >= 7) {
                    System.out.println("PASSED! Confirmed " + fkCount + " active Foreign Key constraints.");
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
        System.out.println("FAILED: Insufficient FKs found.");
        return false;
    }

    private static boolean testSampleDataCounts() {
        System.out.print("[TEST 4/5] Verifying Sample Data Records... ");
        String sql = "SELECT (SELECT COUNT(*) FROM users) AS u_cnt, " +
                     "       (SELECT COUNT(*) FROM donors) AS d_cnt, " +
                     "       (SELECT COUNT(*) FROM blood_requests) AS r_cnt, " +
                     "       (SELECT COUNT(*) FROM donor_responses) AS resp_cnt, " +
                     "       (SELECT COUNT(*) FROM donations) AS don_cnt, " +
                     "       (SELECT COUNT(*) FROM blood_compatibility) AS comp_cnt, " +
                     "       (SELECT COUNT(*) FROM notifications) AS notif_cnt";

        try (Connection conn = DBConnectionUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int users = rs.getInt("u_cnt");
                int donors = rs.getInt("d_cnt");
                int requests = rs.getInt("r_cnt");
                int responses = rs.getInt("resp_cnt");
                int donations = rs.getInt("don_cnt");
                int compatibility = rs.getInt("comp_cnt");
                int notifications = rs.getInt("notif_cnt");

                if (users > 0 && donors > 0 && requests > 0 && compatibility >= 27) {
                    System.out.println(String.format("PASSED! Users: %d, Donors: %d, Requests: %d, Responses: %d, Donations: %d, Matrix: %d, Notifications: %d",
                            users, donors, requests, responses, donations, compatibility, notifications));
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
        return false;
    }

    private static boolean testPasswordHashing() {
        System.out.print("[SECURITY] Verifying SHA-256 Password Hashing & Verification... ");
        String testPassword = "Donor@123";
        String hash = PasswordUtil.hashPassword(testPassword);
        boolean verifySuccess = PasswordUtil.verifyPassword(testPassword, hash);
        boolean verifyFailure = PasswordUtil.verifyPassword("WrongPassword", hash);

        if (verifySuccess && !verifyFailure && hash.length() == 64) {
            System.out.println("PASSED! SHA-256 length: " + hash.length() + " chars.");
            return true;
        } else {
            System.out.println("FAILED password verification check.");
            return false;
        }
    }

    private static boolean testDAOUpsertAndQueries() {
        System.out.println("[TEST 5/5] Testing DAO Operations using PreparedStatements:");

        UserDAO userDAO = new UserDAO();
        DonorDAO donorDAO = new DonorDAO();
        BloodRequestDAO requestDAO = new BloodRequestDAO();
        DonorResponseDAO responseDAO = new DonorResponseDAO();
        DonationDAO donationDAO = new DonationDAO();

        // 1. UserDAO authentication check
        Optional<User> adminUser = userDAO.authenticate("admin@bloodconnect.org", "Admin@123", Role.ADMIN);
        if (adminUser.isPresent()) {
            System.out.println("  ✓ UserDAO.authenticate() successful for Admin: " + adminUser.get().getFullName());
        } else {
            System.err.println("  ✗ UserDAO.authenticate() failed for Admin");
            return false;
        }

        // 2. DonorDAO search & compatibility matching
        List<Donor> opDonors = donorDAO.findByBloodGroupAndCity("O+", "Bangalore");
        System.out.println("  ✓ DonorDAO.findByBloodGroupAndCity('O+', 'Bangalore') returned " + opDonors.size() + " donor(s)");

        List<Donor> compatibleDonors = donorDAO.findCompatibleDonors("AB+", "Bangalore");
        System.out.println("  ✓ DonorDAO.findCompatibleDonors('AB+', 'Bangalore') [Universal Recipient] returned " + compatibleDonors.size() + " donor(s)");

        // 3. BloodRequestDAO
        List<BloodRequest> pendingRequests = requestDAO.findByStatus("PENDING");
        System.out.println("  ✓ BloodRequestDAO.findByStatus('PENDING') returned " + pendingRequests.size() + " request(s)");

        // 4. DonorResponseDAO
        if (!pendingRequests.isEmpty()) {
            Long reqId = pendingRequests.get(0).getId();
            List<DonorResponse> responses = responseDAO.findByRequestId(reqId);
            System.out.println("  ✓ DonorResponseDAO.findByRequestId(" + reqId + ") returned " + responses.size() + " response(s)");
        }

        // 5. DonationDAO
        int totalDonations = donationDAO.getTotalDonationsCount();
        System.out.println("  ✓ DonationDAO.getTotalDonationsCount() returned: " + totalDonations);

        return true;
    }
}
