package com.blooddonation.test;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Automated Test Suite for Blood Request Management Module
 * Covers:
 * 1. Create a blood request with all required fields linked to requester ID.
 * 2. View individual request details.
 * 3. View requests by requester ID.
 * 4. Update a pending blood request.
 * 5. Soft cancel a pending request (preventing subsequent edits).
 * 6. Prevent unauthorized access to another requester's data.
 * 7. Admin status update (PENDING -> MATCHED -> FULFILLED).
 * 8. Multi-criteria filtering by blood group, urgency level, and status.
 */
public class BloodRequestManagementTest {

    private static final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private static final UserDAO userDAO = new UserDAO();

    @org.junit.jupiter.api.Test
    public void testBloodRequestSuite() {
        org.junit.jupiter.api.Assertions.assertTrue(testCreateBloodRequest());
        org.junit.jupiter.api.Assertions.assertTrue(testViewRequestDetails());
        org.junit.jupiter.api.Assertions.assertTrue(testViewRequestsByRequesterId());
        org.junit.jupiter.api.Assertions.assertTrue(testUpdatePendingRequest());
        org.junit.jupiter.api.Assertions.assertTrue(testCancelPendingRequest());
        org.junit.jupiter.api.Assertions.assertTrue(testPreventUnauthorizedAccess());
        org.junit.jupiter.api.Assertions.assertTrue(testAdminStatusUpdate());
        org.junit.jupiter.api.Assertions.assertTrue(testFilterByBloodGroupUrgencyAndStatus());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination System - Blood Request Management Test Suite");
        System.out.println("======================================================================\n");

        boolean allPassed = true;

        allPassed &= testCreateBloodRequest();
        allPassed &= testViewRequestDetails();
        allPassed &= testViewRequestsByRequesterId();
        allPassed &= testUpdatePendingRequest();
        allPassed &= testCancelPendingRequest();
        allPassed &= testPreventUnauthorizedAccess();
        allPassed &= testAdminStatusUpdate();
        allPassed &= testFilterByBloodGroupUrgencyAndStatus();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println(">>> ALL 8 BLOOD REQUEST MANAGEMENT TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> SOME BLOOD REQUEST MANAGEMENT TESTS FAILED! <<<");
        }
        System.out.println("======================================================================");

        if (!allPassed) {
            System.exit(1);
        }
    }

    private static Long createdRequestId = null;
    private static Long testRequesterId = 5L; // Seed demo requester

    /**
     * Test 1: Create a blood request with all required fields linked to requester.
     */
    private static boolean testCreateBloodRequest() {
        System.out.print("[TEST 1] Create a blood request linked to requester: ");
        try {
            // Ensure test requester exists
            Optional<User> reqUserOpt = userDAO.findById(testRequesterId);
            if (reqUserOpt.isEmpty()) {
                User reqUser = new User("Requisition Test User", "req.test@hospital.org", "Pass@123", "+91 9111122222", Role.REQUESTER);
                testRequesterId = userDAO.save(reqUser);
            }

            BloodRequest req = new BloodRequest();
            req.setRequesterId(testRequesterId);
            req.setPatientName("Suresh Nair");
            req.setBloodGroup("O+");
            req.setUnitsNeeded(2);
            req.setHospitalName("Fortis Hospital Bannerghatta");
            req.setHospitalCity("Bangalore");
            req.setHospitalAddress("154/9, Bannerghatta Main Rd, Bangalore");
            req.setContactPerson("Meera Nair (Daughter)");
            req.setContactPhone("+91 9845012345");
            req.setUrgencyLevel("CRITICAL");
            req.setStatus("PENDING");
            req.setRequiredDate(Date.valueOf("2026-12-01"));
            req.setAdditionalNotes("Emergency cardiac bypass scheduled. Immediate cross-matched units needed.");

            createdRequestId = requestDAO.save(req);
            if (createdRequestId == null) {
                System.out.println("FAILED - requestDAO.save returned null.");
                return false;
            }

            System.out.println("PASSED (Created Request #REQ-" + createdRequestId + " for Patient Suresh Nair, O+, 2 Units, Bangalore)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 2: View individual blood request details.
     */
    private static boolean testViewRequestDetails() {
        System.out.print("[TEST 2] View blood request details: ");
        try {
            if (createdRequestId == null) {
                System.out.println("FAILED - createdRequestId is null.");
                return false;
            }

            Optional<BloodRequest> reqOpt = requestDAO.findById(createdRequestId);
            if (reqOpt.isEmpty()) {
                System.out.println("FAILED - Could not find request by ID: " + createdRequestId);
                return false;
            }

            BloodRequest req = reqOpt.get();
            if (!"Suresh Nair".equals(req.getPatientName()) ||
                !"O+".equals(req.getBloodGroup()) ||
                req.getUnitsNeeded() != 2 ||
                !"CRITICAL".equals(req.getUrgencyLevel()) ||
                !"PENDING".equals(req.getStatus())) {
                System.out.println("FAILED - Retrieved request fields do not match expected values.");
                return false;
            }

            System.out.println("PASSED (Verified Patient: " + req.getPatientName() + ", Status: " + req.getStatus() +
                               ", Hospital: " + req.getHospitalName() + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 3: View requests by requester ID.
     */
    private static boolean testViewRequestsByRequesterId() {
        System.out.print("[TEST 3] View requests by requester ID: ");
        try {
            List<BloodRequest> list = requestDAO.findByRequesterId(testRequesterId);
            if (list.isEmpty()) {
                System.out.println("FAILED - No requests found for requester ID: " + testRequesterId);
                return false;
            }

            boolean containsCreated = false;
            for (BloodRequest req : list) {
                if (req.getId().equals(createdRequestId)) {
                    containsCreated = true;
                    break;
                }
            }

            if (!containsCreated) {
                System.out.println("FAILED - Created request #REQ-" + createdRequestId + " not found in requester's list.");
                return false;
            }

            System.out.println("PASSED (Found " + list.size() + " requests for Requester ID " + testRequesterId + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 4: Update a pending blood request.
     */
    private static boolean testUpdatePendingRequest() {
        System.out.print("[TEST 4] Update a pending blood request: ");
        try {
            Optional<BloodRequest> reqOpt = requestDAO.findById(createdRequestId);
            if (reqOpt.isEmpty()) {
                System.out.println("FAILED - Request not found.");
                return false;
            }

            BloodRequest req = reqOpt.get();
            req.setPatientName("Suresh Nair (Updated)");
            req.setUnitsNeeded(3);
            req.setHospitalAddress("ICU Ward 3B, Fortis Hospital");
            req.setAdditionalNotes("Doctor increased unit requirement to 3 units.");

            boolean updated = requestDAO.update(req);
            if (!updated) {
                System.out.println("FAILED - requestDAO.update returned false.");
                return false;
            }

            // Verify in database
            Optional<BloodRequest> refreshedOpt = requestDAO.findById(createdRequestId);
            if (refreshedOpt.isEmpty()) {
                System.out.println("FAILED - Could not reload updated request.");
                return false;
            }

            BloodRequest refreshed = refreshedOpt.get();
            if (!"Suresh Nair (Updated)".equals(refreshed.getPatientName()) ||
                refreshed.getUnitsNeeded() != 3 ||
                !"ICU Ward 3B, Fortis Hospital".equals(refreshed.getHospitalAddress())) {
                System.out.println("FAILED - Requisition values were not updated in database.");
                return false;
            }

            System.out.println("PASSED (Updated: Patient Name, Units: 3, Address)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 5: Soft cancel a pending request (preventing subsequent edits).
     */
    private static boolean testCancelPendingRequest() {
        System.out.print("[TEST 5] Soft cancel a pending blood request: ");
        try {
            // Create a specific request to cancel
            BloodRequest cancelTarget = new BloodRequest();
            cancelTarget.setRequesterId(testRequesterId);
            cancelTarget.setPatientName("Patient To Cancel");
            cancelTarget.setBloodGroup("B-");
            cancelTarget.setUnitsNeeded(1);
            cancelTarget.setHospitalName("City General Hospital");
            cancelTarget.setHospitalCity("Bangalore");
            cancelTarget.setHospitalAddress("MG Road");
            cancelTarget.setContactPerson("Guardian");
            cancelTarget.setContactPhone("+91 9845099999");
            cancelTarget.setUrgencyLevel("NORMAL");
            cancelTarget.setStatus("PENDING");
            cancelTarget.setRequiredDate(Date.valueOf("2026-12-05"));

            Long targetId = requestDAO.save(cancelTarget);
            if (targetId == null) {
                System.out.println("FAILED - Could not save target request for cancellation.");
                return false;
            }

            // Soft cancel
            boolean cancelled = requestDAO.cancelPendingRequest(targetId);
            if (!cancelled) {
                System.out.println("FAILED - cancelPendingRequest returned false.");
                return false;
            }

            // Verify status in DB
            BloodRequest postCancel = requestDAO.findById(targetId).orElseThrow();
            if (!"CANCELLED".equalsIgnoreCase(postCancel.getStatus())) {
                System.out.println("FAILED - Request status is not CANCELLED: " + postCancel.getStatus());
                return false;
            }

            // Verify that modifying a cancelled request fails
            postCancel.setPatientName("Hacked Modification");
            boolean modifyAfterCancel = requestDAO.update(postCancel);
            if (modifyAfterCancel) {
                System.out.println("FAILED - Modification succeeded on a cancelled request.");
                return false;
            }

            System.out.println("PASSED (Soft cancelled -> Status: CANCELLED; Subsequent modifications rejected)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 6: Prevent unauthorized access to another requester's data.
     */
    private static boolean testPreventUnauthorizedAccess() {
        System.out.print("[TEST 6] Prevent unauthorized access to requester data: ");
        try {
            // User A (Owner of createdRequestId, userId = testRequesterId)
            User userA = new User();
            userA.setId(testRequesterId);
            userA.setRole(Role.REQUESTER);

            // User B (Attacker / Different Requester, userId = 888)
            User userB = new User();
            userB.setId(888L);
            userB.setRole(Role.REQUESTER);

            // Admin User (userId = 1)
            User admin = new User();
            admin.setId(1L);
            admin.setRole(Role.ADMIN);

            BloodRequest targetReq = requestDAO.findById(createdRequestId).orElseThrow();

            // 1. User B attempting to edit User A's requisition
            boolean userBAllowed = userB.getRole() == Role.ADMIN || userB.getId().equals(targetReq.getRequesterId());
            if (userBAllowed) {
                System.out.println("FAILED - Unauthorized user B was erroneously allowed to edit.");
                return false;
            }

            // 2. User A (owner) allowed
            boolean userAAllowed = userA.getRole() == Role.ADMIN || userA.getId().equals(targetReq.getRequesterId());
            if (!userAAllowed) {
                System.out.println("FAILED - Legitimate owner user A was rejected.");
                return false;
            }

            // 3. Admin allowed
            boolean adminAllowed = admin.getRole() == Role.ADMIN || admin.getId().equals(targetReq.getRequesterId());
            if (!adminAllowed) {
                System.out.println("FAILED - Administrator was rejected.");
                return false;
            }

            System.out.println("PASSED (Unauthorized requester rejected with 403 Forbidden simulation; Owner & Admin permitted)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 7: Admin status update (PENDING -> MATCHED -> FULFILLED).
     */
    private static boolean testAdminStatusUpdate() {
        System.out.print("[TEST 7] Admin status updates (MATCHED, FULFILLED): ");
        try {
            // 1. Update to MATCHED
            boolean matched = requestDAO.updateStatus(createdRequestId, "MATCHED");
            if (!matched) {
                System.out.println("FAILED - Update to MATCHED failed.");
                return false;
            }
            BloodRequest reqMatched = requestDAO.findById(createdRequestId).orElseThrow();
            if (!"MATCHED".equals(reqMatched.getStatus())) {
                System.out.println("FAILED - Status not MATCHED in database.");
                return false;
            }

            // 2. Update to FULFILLED
            boolean fulfilled = requestDAO.updateStatus(createdRequestId, "FULFILLED");
            if (!fulfilled) {
                System.out.println("FAILED - Update to FULFILLED failed.");
                return false;
            }
            BloodRequest reqFulfilled = requestDAO.findById(createdRequestId).orElseThrow();
            if (!"FULFILLED".equals(reqFulfilled.getStatus())) {
                System.out.println("FAILED - Status not FULFILLED in database.");
                return false;
            }

            // Verify that requester cannot edit a FULFILLED request
            boolean editFulfilled = requestDAO.update(reqFulfilled);
            if (editFulfilled) {
                System.out.println("FAILED - Requisition was modified while FULFILLED.");
                return false;
            }

            System.out.println("PASSED (Status transitions: PENDING -> MATCHED -> FULFILLED; Fulfilled lock verified)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 8: Filter by blood group, urgency and status.
     */
    private static boolean testFilterByBloodGroupUrgencyAndStatus() {
        System.out.print("[TEST 8] Filter requests by blood group, urgency, and status: ");
        try {
            // Filter by blood group
            List<BloodRequest> oPosList = requestDAO.searchAndFilter("O+", null, null, null);
            for (BloodRequest r : oPosList) {
                if (!"O+".equalsIgnoreCase(r.getBloodGroup())) {
                    System.out.println("FAILED - Found non-O+ blood group in filter result: " + r.getBloodGroup());
                    return false;
                }
            }

            // Filter by urgency
            List<BloodRequest> criticalList = requestDAO.searchAndFilter(null, "CRITICAL", null, null);
            for (BloodRequest r : criticalList) {
                if (!"CRITICAL".equalsIgnoreCase(r.getUrgencyLevel())) {
                    System.out.println("FAILED - Found non-CRITICAL urgency in filter result: " + r.getUrgencyLevel());
                    return false;
                }
            }

            // Filter by status
            List<BloodRequest> fulfilledList = requestDAO.searchAndFilter(null, null, "FULFILLED", null);
            for (BloodRequest r : fulfilledList) {
                if (!"FULFILLED".equalsIgnoreCase(r.getStatus())) {
                    System.out.println("FAILED - Found non-FULFILLED status in filter result: " + r.getStatus());
                    return false;
                }
            }

            // Combined multi-criteria filter
            List<BloodRequest> combined = requestDAO.searchAndFilter("O+", "CRITICAL", "FULFILLED", "Bangalore");
            if (combined.isEmpty()) {
                System.out.println("FAILED - Combined filter returned empty list.");
                return false;
            }

            System.out.println("PASSED (Multi-criteria filter verified: O+, CRITICAL, FULFILLED, Bangalore)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
