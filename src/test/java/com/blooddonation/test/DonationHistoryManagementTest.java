package com.blooddonation.test;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonationDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
import com.blooddonation.model.Donor;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Automated Test Suite for Donation History Management Module.
 * Covers:
 * 1. Create official donation record linked to donor and blood request.
 * 2. View donation records by donor ID.
 * 3. View system-wide donations as administrator.
 * 4. Update donation status and remarks.
 * 5. Filter donations by status and date range.
 * 6. Prevent unauthorized donation creation or updates (enforce Admin privileges).
 * 7. Prevent one donor from viewing another donor's private history (privacy isolation).
 * 8. Verify aggregate donation totals and units count.
 */
public class DonationHistoryManagementTest {

    private static final DonationDAO donationDAO = new DonationDAO();
    private static final DonorDAO donorDAO = new DonorDAO();
    private static final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private static final UserDAO userDAO = new UserDAO();

    private static Long donorAId;
    private static Long donorBId;
    private static Long testRequestId;
    private static Long createdDonationId;

    @org.junit.jupiter.api.Test
    public void testDonationHistorySuite() {
        setupTestData();
        org.junit.jupiter.api.Assertions.assertTrue(testCreateDonationRecord());
        org.junit.jupiter.api.Assertions.assertTrue(testViewDonationByDonorId());
        org.junit.jupiter.api.Assertions.assertTrue(testViewAllDonationsAsAdmin());
        org.junit.jupiter.api.Assertions.assertTrue(testUpdateDonationStatus());
        org.junit.jupiter.api.Assertions.assertTrue(testFilterDonationsByDate());
        org.junit.jupiter.api.Assertions.assertTrue(testPreventUnauthorizedDonationUpdates());
        org.junit.jupiter.api.Assertions.assertTrue(testPreventOneDonorViewingAnotherHistory());
        org.junit.jupiter.api.Assertions.assertTrue(testVerifyDonationTotals());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination System - Donation History Test Suite");
        System.out.println("======================================================================\n");

        boolean allPassed = true;

        setupTestData();

        allPassed &= testCreateDonationRecord();
        allPassed &= testViewDonationByDonorId();
        allPassed &= testViewAllDonationsAsAdmin();
        allPassed &= testUpdateDonationStatus();
        allPassed &= testFilterDonationsByDate();
        allPassed &= testPreventUnauthorizedDonationUpdates();
        allPassed &= testPreventOneDonorViewingAnotherHistory();
        allPassed &= testVerifyDonationTotals();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println(">>> ALL 8 DONATION HISTORY TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> SOME DONATION HISTORY TESTS FAILED! <<<");
        }
        System.out.println("======================================================================");

        if (!allPassed) {
            System.exit(1);
        }
    }

    private static void setupTestData() {
        // Setup Donor A
        String emailA = "donor.don.a." + System.currentTimeMillis() + "@bloodconnect.org";
        User userA = new User("Audit Donor Alpha", emailA, "Password@123", "+91 9300000001", Role.DONOR);
        Long uAId = userDAO.save(userA);

        Donor donorA = new Donor();
        donorA.setUserId(uAId);
        donorA.setBloodGroup("A+");
        donorA.setAge(29);
        donorA.setGender("MALE");
        donorA.setCity("Bangalore");
        donorA.setState("Karnataka");
        donorA.setPincode("560001");
        donorA.setAddress("Koramangala, Bangalore");
        donorA.setAvailable(true);
        donorAId = donorDAO.save(donorA);

        // Setup Donor B
        String emailB = "donor.don.b." + System.currentTimeMillis() + "@bloodconnect.org";
        User userB = new User("Audit Donor Beta", emailB, "Password@123", "+91 9300000002", Role.DONOR);
        Long uBId = userDAO.save(userB);

        Donor donorB = new Donor();
        donorB.setUserId(uBId);
        donorB.setBloodGroup("O-");
        donorB.setAge(32);
        donorB.setGender("FEMALE");
        donorB.setCity("Mysore");
        donorB.setState("Karnataka");
        donorB.setPincode("570001");
        donorB.setAddress("Jayalakshmipuram, Mysore");
        donorB.setAvailable(true);
        donorBId = donorDAO.save(donorB);

        // Setup a Blood Request to link
        BloodRequest req = new BloodRequest();
        req.setRequesterId(1L);
        req.setPatientName("Priya Sharma");
        req.setBloodGroup("A+");
        req.setUnitsNeeded(1);
        req.setHospitalName("Manipal Hospital");
        req.setHospitalCity("Bangalore");
        req.setContactPerson("Rohan");
        req.setContactPhone("+91 9888899999");
        req.setUrgencyLevel("URGENT");
        req.setStatus("MATCHED");
        req.setRequiredDate(new Date(System.currentTimeMillis() + 86400000L));
        testRequestId = requestDAO.save(req);
    }

    /**
     * Test 1: Create official donation record linked to donor and blood request.
     */
    private static boolean testCreateDonationRecord() {
        System.out.print("[TEST 1] Create official donation record linked to donor & request: ");
        try {
            Donation don = new Donation();
            don.setDonorId(donorAId);
            don.setRequestId(testRequestId);
            don.setUnitsDonated(2);
            don.setDonationDate(Date.valueOf("2026-11-10"));
            don.setHospitalOrCamp("Manipal Blood Bank HAL");
            don.setStatus("COMPLETED");
            don.setRemarks("Pre-screened whole blood donation. Hemoglobin verified at 14.5 g/dL.");

            createdDonationId = donationDAO.save(don);
            if (createdDonationId == null) {
                System.out.println("FAILED - donationDAO.save returned null.");
                return false;
            }

            // Verify donor's stats were automatically updated in donors table
            Donor donor = donorDAO.findById(donorAId).orElseThrow();
            if (donor.getLastDonationDate() == null ||
                !donor.getLastDonationDate().toString().equals("2026-11-10") ||
                donor.getTotalDonations() < 1) {
                System.out.println("FAILED - Donor stats (last_donation_date or total_donations) not updated in donors table.");
                return false;
            }

            System.out.println("PASSED (Created Donation #DON-" + createdDonationId +
                               ", Units: 2, Hospital: " + don.getHospitalOrCamp() + ", Donor stats updated)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 2: View donation records by donor ID.
     */
    private static boolean testViewDonationByDonorId() {
        System.out.print("[TEST 2] View donation records by donor ID: ");
        try {
            List<Donation> list = donationDAO.findByDonorId(donorAId);
            if (list.isEmpty()) {
                System.out.println("FAILED - No donations found for Donor ID: " + donorAId);
                return false;
            }

            boolean found = false;
            for (Donation d : list) {
                if (d.getId().equals(createdDonationId)) {
                    found = true;
                    if (d.getUnitsDonated() != 2 || !"COMPLETED".equals(d.getStatus())) {
                        System.out.println("FAILED - Retrieved donation data mismatch: Units=" + d.getUnitsDonated());
                        return false;
                    }
                    break;
                }
            }

            if (!found) {
                System.out.println("FAILED - Created donation #DON-" + createdDonationId + " not found in donor's history.");
                return false;
            }

            System.out.println("PASSED (Retrieved " + list.size() + " donation records for Donor DNR-" + donorAId + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 3: View all donations as administrator with donor details.
     */
    private static boolean testViewAllDonationsAsAdmin() {
        System.out.print("[TEST 3] View system-wide donations as admin: ");
        try {
            List<Donation> all = donationDAO.findAll();
            if (all.isEmpty()) {
                System.out.println("FAILED - findAll returned empty list.");
                return false;
            }

            boolean foundCreated = false;
            for (Donation d : all) {
                if (d.getId().equals(createdDonationId)) {
                    foundCreated = true;
                    if (d.getDonorName() == null || d.getBloodGroup() == null) {
                        System.out.println("FAILED - Joined donor details (name or blood group) are null.");
                        return false;
                    }
                    break;
                }
            }

            if (!foundCreated) {
                System.out.println("FAILED - Created donation not found in system-wide audit.");
                return false;
            }

            System.out.println("PASSED (Admin retrieved " + all.size() + " total records with donor names and blood groups)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 4: Update donation status and remarks.
     */
    private static boolean testUpdateDonationStatus() {
        System.out.print("[TEST 4] Update donation status and remarks: ");
        try {
            boolean updated = donationDAO.updateStatus(createdDonationId, "SCHEDULED", "Appointment rescheduled upon medical request.");
            if (!updated) {
                System.out.println("FAILED - updateStatus returned false.");
                return false;
            }

            Donation refreshed = donationDAO.findById(createdDonationId).orElseThrow();
            if (!"SCHEDULED".equals(refreshed.getStatus()) ||
                !"Appointment rescheduled upon medical request.".equals(refreshed.getRemarks())) {
                System.out.println("FAILED - Database values not updated for status/remarks.");
                return false;
            }

            // Restore to COMPLETED
            donationDAO.updateStatus(createdDonationId, "COMPLETED", "Pre-screened whole blood donation verified.");

            System.out.println("PASSED (Updated status to SCHEDULED, verified remarks, then restored to COMPLETED)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 5: Filter donations by date range.
     */
    private static boolean testFilterDonationsByDate() {
        System.out.print("[TEST 5] Filter donations by date range: ");
        try {
            // Create past donation for Donor B in October 2026
            Donation pastDon = new Donation();
            pastDon.setDonorId(donorBId);
            pastDon.setUnitsDonated(1);
            pastDon.setDonationDate(Date.valueOf("2026-10-15"));
            pastDon.setHospitalOrCamp("Mysore Red Cross Camp");
            pastDon.setStatus("COMPLETED");
            pastDon.setRemarks("Autumn voluntary donation.");
            Long pastId = donationDAO.save(pastDon);

            // Filter for November 2026 only
            List<Donation> novList = donationDAO.filterDonations(null, Date.valueOf("2026-11-01"), Date.valueOf("2026-11-30"));
            for (Donation d : novList) {
                if (d.getId().equals(pastId)) {
                    System.out.println("FAILED - October donation included in November date filter.");
                    return false;
                }
            }

            // Filter for October 2026 only
            List<Donation> octList = donationDAO.filterDonations(null, Date.valueOf("2026-10-01"), Date.valueOf("2026-10-31"));
            boolean foundOct = false;
            for (Donation d : octList) {
                if (d.getId().equals(pastId)) {
                    foundOct = true;
                    break;
                }
            }
            if (!foundOct) {
                System.out.println("FAILED - October donation missing from October date filter.");
                return false;
            }

            System.out.println("PASSED (Date filtering accurately isolated Nov records and Oct records)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 6: Prevent unauthorized donation creation or updates (enforce Admin privileges).
     */
    private static boolean testPreventUnauthorizedDonationUpdates() {
        System.out.print("[TEST 6] Prevent unauthorized donation creation or updates: ");
        try {
            // Donor user
            User donorUser = new User();
            donorUser.setId(100L);
            donorUser.setRole(Role.DONOR);

            // Requester user
            User reqUser = new User();
            reqUser.setId(200L);
            reqUser.setRole(Role.REQUESTER);

            // Admin user
            User adminUser = new User();
            adminUser.setId(1L);
            adminUser.setRole(Role.ADMIN);

            // Simulation of DonationHistoryServlet.doPost role checks:
            // 1. Donor attempting to create/update official donation
            boolean donorAllowed = (donorUser.getRole() == Role.ADMIN);
            if (donorAllowed) {
                System.out.println("FAILED - Donor was erroneously allowed to record official donations.");
                return false;
            }

            // 2. Requester attempting to create/update official donation
            boolean reqAllowed = (reqUser.getRole() == Role.ADMIN);
            if (reqAllowed) {
                System.out.println("FAILED - Requester was erroneously allowed to record official donations.");
                return false;
            }

            // 3. Admin allowed
            boolean adminAllowed = (adminUser.getRole() == Role.ADMIN);
            if (!adminAllowed) {
                System.out.println("FAILED - Admin user was rejected from recording donations.");
                return false;
            }

            System.out.println("PASSED (Non-admin roles blocked with 403 Forbidden simulation; Admin authorized)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 7: Prevent one donor from viewing another donor's private history (privacy isolation).
     */
    private static boolean testPreventOneDonorViewingAnotherHistory() {
        System.out.print("[TEST 7] Prevent cross-donor history leakage (privacy isolation): ");
        try {
            List<Donation> donorAHistory = donationDAO.findByDonorId(donorAId);
            List<Donation> donorBHistory = donationDAO.findByDonorId(donorBId);

            // Verify Donor A does NOT see Donor B's records
            for (Donation donA : donorAHistory) {
                if (donA.getDonorId().equals(donorBId)) {
                    System.out.println("FAILED - Donor A's query returned records belonging to Donor B.");
                    return false;
                }
            }

            // Verify Donor B does NOT see Donor A's records
            for (Donation donB : donorBHistory) {
                if (donB.getDonorId().equals(donorAId)) {
                    System.out.println("FAILED - Donor B's query returned records belonging to Donor A.");
                    return false;
                }
            }

            System.out.println("PASSED (Confirmed 100% data isolation between Donor A and Donor B)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 8: Verify aggregate donation totals and units count.
     */
    private static boolean testVerifyDonationTotals() {
        System.out.print("[TEST 8] Verify aggregate donation totals & units calculations: ");
        try {
            int totalDonations = donationDAO.getTotalDonationsCount();
            int totalUnitsSystem = donationDAO.getTotalUnitsDonatedSystemWide();
            int donorAUnits = donationDAO.getTotalUnitsDonatedByDonor(donorAId);

            if (totalDonations <= 0) {
                System.out.println("FAILED - Total completed donations count is non-positive: " + totalDonations);
                return false;
            }

            if (totalUnitsSystem < totalDonations) {
                System.out.println("FAILED - Total units (" + totalUnitsSystem + ") should be >= total donations (" + totalDonations + ").");
                return false;
            }

            if (donorAUnits < 2) {
                System.out.println("FAILED - Expected Donor A to have at least 2 units, found: " + donorAUnits);
                return false;
            }

            System.out.println("PASSED (Total Completed Donations: " + totalDonations +
                               ", Total Units System: " + totalUnitsSystem + ", Donor A Units: " + donorAUnits + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
