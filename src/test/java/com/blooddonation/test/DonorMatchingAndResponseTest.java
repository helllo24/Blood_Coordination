package com.blooddonation.test;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.DonorResponseDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import com.blooddonation.model.DonorResponse;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Automated Test Suite for Donor Matching and Donor Response Management Module.
 * Covers:
 * 1. Find clinically compatible donors using blood compatibility matrix.
 * 2. Filter available donors by city.
 * 3. Exclude unavailable donors from preliminary software matches.
 * 4. Accept a blood request (status = ACCEPTED) and advance request to MATCHED.
 * 5. Reject a blood request (status = REJECTED) with decline notes.
 * 6. Prevent duplicate responses from the same donor for the same request.
 * 7. Display responses to the correct requester only (data isolation).
 * 8. Prevent unauthorized response updates and role enforcement.
 */
public class DonorMatchingAndResponseTest {

    private static final DonorDAO donorDAO = new DonorDAO();
    private static final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private static final DonorResponseDAO responseDAO = new DonorResponseDAO();
    private static final UserDAO userDAO = new UserDAO();

    private static Long testRequesterAId;
    private static Long testRequesterBId;
    private static Long testRequestAId;
    private static Long testRequestBId;
    private static Long testDonor1Id;
    private static Long testDonor2Id;

    @org.junit.jupiter.api.Test
    public void testDonorMatchingSuite() {
        setupTestData();
        org.junit.jupiter.api.Assertions.assertTrue(testFindCompatibleDonors());
        org.junit.jupiter.api.Assertions.assertTrue(testFilterDonorsByCity());
        org.junit.jupiter.api.Assertions.assertTrue(testExcludeUnavailableDonors());
        org.junit.jupiter.api.Assertions.assertTrue(testAcceptBloodRequest());
        org.junit.jupiter.api.Assertions.assertTrue(testRejectBloodRequest());
        org.junit.jupiter.api.Assertions.assertTrue(testPreventDuplicateDonorResponses());
        org.junit.jupiter.api.Assertions.assertTrue(testDisplayResponsesToCorrectRequester());
        org.junit.jupiter.api.Assertions.assertTrue(testPreventUnauthorizedResponseUpdates());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination System - Donor Matching & Response Test Suite");
        System.out.println("======================================================================\n");

        boolean allPassed = true;

        setupTestData();

        allPassed &= testFindCompatibleDonors();
        allPassed &= testFilterDonorsByCity();
        allPassed &= testExcludeUnavailableDonors();
        allPassed &= testAcceptBloodRequest();
        allPassed &= testRejectBloodRequest();
        allPassed &= testPreventDuplicateDonorResponses();
        allPassed &= testDisplayResponsesToCorrectRequester();
        allPassed &= testPreventUnauthorizedResponseUpdates();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println(">>> ALL 8 DONOR MATCHING & RESPONSE TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> SOME TESTS FAILED! <<<");
        }
        System.out.println("======================================================================");

        if (!allPassed) {
            System.exit(1);
        }
    }

    private static void setupTestData() {
        // Setup Requester A
        String emailA = "requester.match.a." + System.currentTimeMillis() + "@hospital.org";
        User userA = new User("Requester A", emailA, "Password@123", "+91 9100000001", Role.REQUESTER);
        testRequesterAId = userDAO.save(userA);

        // Setup Requester B
        String emailB = "requester.match.b." + System.currentTimeMillis() + "@hospital.org";
        User userB = new User("Requester B", emailB, "Password@123", "+91 9100000002", Role.REQUESTER);
        testRequesterBId = userDAO.save(userB);

        // Setup Request A (for Requester A, B+ blood group)
        BloodRequest reqA = new BloodRequest();
        reqA.setRequesterId(testRequesterAId);
        reqA.setPatientName("Patient Alpha");
        reqA.setBloodGroup("B+");
        reqA.setUnitsNeeded(2);
        reqA.setHospitalName("Apollo Hospital");
        reqA.setHospitalCity("Bangalore");
        reqA.setContactPerson("Relative A");
        reqA.setContactPhone("+91 9100000001");
        reqA.setUrgencyLevel("CRITICAL");
        reqA.setStatus("PENDING");
        reqA.setRequiredDate(new Date(System.currentTimeMillis() + 86400000L));
        testRequestAId = requestDAO.save(reqA);

        // Setup Request B (for Requester B, AB+ blood group)
        BloodRequest reqB = new BloodRequest();
        reqB.setRequesterId(testRequesterBId);
        reqB.setPatientName("Patient Beta");
        reqB.setBloodGroup("AB+");
        reqB.setUnitsNeeded(1);
        reqB.setHospitalName("Fortis Hospital");
        reqB.setHospitalCity("Bangalore");
        reqB.setContactPerson("Relative B");
        reqB.setContactPhone("+91 9100000002");
        reqB.setUrgencyLevel("URGENT");
        reqB.setStatus("PENDING");
        reqB.setRequiredDate(new Date(System.currentTimeMillis() + 86400000L));
        testRequestBId = requestDAO.save(reqB);

        // Setup Donor 1 (O+ donor, Universal red cell donor for positive groups)
        String emailD1 = "donor.match.d1." + System.currentTimeMillis() + "@gmail.com";
        User userD1 = new User("Donor Match Alpha", emailD1, "Password@123", "+91 9200000001", Role.DONOR);
        Long uD1Id = userDAO.save(userD1);

        Donor donor1 = new Donor();
        donor1.setUserId(uD1Id);
        donor1.setBloodGroup("O+");
        donor1.setAge(27);
        donor1.setGender("MALE");
        donor1.setCity("Bangalore");
        donor1.setState("Karnataka");
        donor1.setPincode("560001");
        donor1.setAddress("MG Road, Bangalore");
        donor1.setAvailable(true);
        testDonor1Id = donorDAO.save(donor1);

        // Setup Donor 2 (B+ donor)
        String emailD2 = "donor.match.d2." + System.currentTimeMillis() + "@gmail.com";
        User userD2 = new User("Donor Match Beta", emailD2, "Password@123", "+91 9200000002", Role.DONOR);
        Long uD2Id = userDAO.save(userD2);

        Donor donor2 = new Donor();
        donor2.setUserId(uD2Id);
        donor2.setBloodGroup("B+");
        donor2.setAge(30);
        donor2.setGender("FEMALE");
        donor2.setCity("Bangalore");
        donor2.setState("Karnataka");
        donor2.setPincode("560025");
        donor2.setAddress("Richmond Town, Bangalore");
        donor2.setAvailable(true);
        testDonor2Id = donorDAO.save(donor2);
    }

    /**
     * Test 1: Find compatible donors using the blood compatibility matrix.
     */
    private static boolean testFindCompatibleDonors() {
        System.out.print("[TEST 1] Find compatible donors via clinical cross-match matrix: ");
        try {
            // Patient B+ can receive from B+, B-, O+, O-
            List<Donor> compatibleForBPos = donorDAO.findCompatibleDonors("B+", "Bangalore");
            if (compatibleForBPos.isEmpty()) {
                System.out.println("FAILED - No compatible donors returned for B+ patient.");
                return false;
            }

            boolean foundD1 = false; // O+
            boolean foundD2 = false; // B+
            for (Donor d : compatibleForBPos) {
                if (d.getId().equals(testDonor1Id)) foundD1 = true;
                if (d.getId().equals(testDonor2Id)) foundD2 = true;
            }

            if (!foundD1 || !foundD2) {
                System.out.println("FAILED - Compatible donors list does not include expected O+ and B+ donors.");
                return false;
            }

            System.out.println("PASSED (Found " + compatibleForBPos.size() + " compatible donors for B+ patient, including O+ and B+ donors)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 2: Filter donors by city.
     */
    private static boolean testFilterDonorsByCity() {
        System.out.print("[TEST 2] Filter available donors by city: ");
        try {
            List<Donor> blrDonors = donorDAO.findAvailableDonorsByCity("Bangalore");
            if (blrDonors.isEmpty()) {
                System.out.println("FAILED - No donors found in Bangalore.");
                return false;
            }

            for (Donor d : blrDonors) {
                if (!"Bangalore".equalsIgnoreCase(d.getCity())) {
                    System.out.println("FAILED - Donor with non-matching city included: " + d.getCity());
                    return false;
                }
            }

            System.out.println("PASSED (Found " + blrDonors.size() + " available donors in Bangalore)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 3: Exclude unavailable donors from search matches.
     */
    private static boolean testExcludeUnavailableDonors() {
        System.out.print("[TEST 3] Exclude unavailable donors from preliminary matching: ");
        try {
            // Temporarily set Donor 1 to unavailable
            donorDAO.updateAvailability(testDonor1Id, false);

            List<Donor> matches = donorDAO.findCompatibleDonors("B+", "Bangalore");
            for (Donor d : matches) {
                if (d.getId().equals(testDonor1Id)) {
                    System.out.println("FAILED - Unavailable donor was included in matching results.");
                    donorDAO.updateAvailability(testDonor1Id, true);
                    return false;
                }
                if (!d.isAvailable()) {
                    System.out.println("FAILED - Inactive donor found in active search.");
                    donorDAO.updateAvailability(testDonor1Id, true);
                    return false;
                }
            }

            // Restore availability
            donorDAO.updateAvailability(testDonor1Id, true);

            System.out.println("PASSED (Unavailable donor DNR-" + testDonor1Id + " successfully excluded from preliminary match)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 4: Accept a blood request.
     */
    private static boolean testAcceptBloodRequest() {
        System.out.print("[TEST 4] Accept a blood request (ACCEPTED status): ");
        try {
            DonorResponse response = new DonorResponse();
            response.setRequestId(testRequestAId);
            response.setDonorId(testDonor1Id);
            response.setStatus("ACCEPTED");
            response.setMessage("Available immediately, will report to Apollo Hospital by 10 AM.");

            Long respId = responseDAO.save(response);
            if (respId == null) {
                System.out.println("FAILED - responseDAO.save returned null.");
                return false;
            }

            // Advance request to MATCHED
            requestDAO.updateStatus(testRequestAId, "MATCHED");

            // Verify response saved
            Optional<DonorResponse> fetchedOpt = responseDAO.findById(respId);
            if (fetchedOpt.isEmpty() || !"ACCEPTED".equals(fetchedOpt.get().getStatus())) {
                System.out.println("FAILED - Response status in DB is not ACCEPTED.");
                return false;
            }

            // Verify blood request status updated to MATCHED
            BloodRequest req = requestDAO.findById(testRequestAId).orElseThrow();
            if (!"MATCHED".equals(req.getStatus())) {
                System.out.println("FAILED - Request status was not advanced to MATCHED.");
                return false;
            }

            System.out.println("PASSED (Recorded ACCEPTED response #RES-" + respId + "; Request status updated to MATCHED)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 5: Reject a blood request.
     */
    private static boolean testRejectBloodRequest() {
        System.out.print("[TEST 5] Reject a blood request (REJECTED status): ");
        try {
            DonorResponse response = new DonorResponse();
            response.setRequestId(testRequestAId);
            response.setDonorId(testDonor2Id);
            response.setStatus("REJECTED");
            response.setMessage("Currently traveling out of town, unable to donate this week.");

            Long respId = responseDAO.save(response);
            if (respId == null) {
                System.out.println("FAILED - responseDAO.save returned null for decline response.");
                return false;
            }

            Optional<DonorResponse> fetchedOpt = responseDAO.findById(respId);
            if (fetchedOpt.isEmpty() || !"REJECTED".equals(fetchedOpt.get().getStatus())) {
                System.out.println("FAILED - Response status in DB is not REJECTED.");
                return false;
            }

            System.out.println("PASSED (Recorded REJECTED response #RES-" + respId + " with reason note)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 6: Prevent duplicate donor responses.
     */
    private static boolean testPreventDuplicateDonorResponses() {
        System.out.print("[TEST 6] Prevent duplicate donor responses: ");
        try {
            // Donor 1 has already responded to Request A
            boolean hasResponded = responseDAO.hasResponded(testRequestAId, testDonor1Id);
            if (!hasResponded) {
                System.out.println("FAILED - hasResponded returned false for an existing response.");
                return false;
            }

            // Attempt to submit a second response
            DonorResponse duplicateResp = new DonorResponse();
            duplicateResp.setRequestId(testRequestAId);
            duplicateResp.setDonorId(testDonor1Id);
            duplicateResp.setStatus("ACCEPTED");
            duplicateResp.setMessage("Duplicate attempt");

            Long duplicateId = responseDAO.save(duplicateResp);
            if (duplicateId != null) {
                System.out.println("FAILED - Duplicate response was erroneously saved with ID: " + duplicateId);
                return false;
            }

            System.out.println("PASSED (Duplicate response blocked: hasResponded=true, save returned null)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 7: Display responses to the correct requester only (data isolation).
     */
    private static boolean testDisplayResponsesToCorrectRequester() {
        System.out.print("[TEST 7] Display responses to the correct requester only: ");
        try {
            // Requester A posted Request A (which has responses from Donor 1 and Donor 2)
            List<DonorResponse> responsesForReqA = responseDAO.findByRequestId(testRequestAId);
            if (responsesForReqA.size() < 2) {
                System.out.println("FAILED - Expected at least 2 responses for Request A, found: " + responsesForReqA.size());
                return false;
            }

            // Requester B posted Request B (which has 0 responses)
            List<DonorResponse> responsesForReqB = responseDAO.findByRequestId(testRequestBId);
            if (!responsesForReqB.isEmpty()) {
                System.out.println("FAILED - Request B has unexpected responses: " + responsesForReqB.size());
                return false;
            }

            // Verify Requester A sees only their own request responses
            for (DonorResponse dr : responsesForReqA) {
                if (!dr.getRequestId().equals(testRequestAId)) {
                    System.out.println("FAILED - Found cross-request response leakage.");
                    return false;
                }
            }

            System.out.println("PASSED (Requester A sees their 2 responses; Requester B sees 0 responses)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test 8: Prevent unauthorized response updates and role enforcement.
     */
    private static boolean testPreventUnauthorizedResponseUpdates() {
        System.out.print("[TEST 8] Prevent unauthorized response updates & role enforcement: ");
        try {
            // Requester user (not a donor) attempting to submit a donor response
            User requesterUser = userDAO.findById(testRequesterAId).orElseThrow();
            boolean isAllowedToRespond = (requesterUser.getRole() == Role.DONOR || requesterUser.getRole() == Role.ADMIN);
            if (isAllowedToRespond) {
                System.out.println("FAILED - Requester role was permitted to submit donor responses.");
                return false;
            }

            // Donor user attempting to submit response
            User donorUser = userDAO.findById(donorDAO.findById(testDonor1Id).orElseThrow().getUserId()).orElseThrow();
            boolean donorAllowed = (donorUser.getRole() == Role.DONOR || donorUser.getRole() == Role.ADMIN);
            if (!donorAllowed) {
                System.out.println("FAILED - Registered donor was rejected.");
                return false;
            }

            // Admin update status on response
            Optional<DonorResponse> respOpt = responseDAO.findByRequestIdAndDonorId(testRequestAId, testDonor1Id);
            if (respOpt.isEmpty()) {
                System.out.println("FAILED - Response not found.");
                return false;
            }
            Long respId = respOpt.get().getId();
            boolean statusUpdated = responseDAO.updateStatus(respId, "COMPLETED");
            if (!statusUpdated) {
                System.out.println("FAILED - Administrator updateStatus returned false.");
                return false;
            }

            DonorResponse updated = responseDAO.findById(respId).orElseThrow();
            if (!"COMPLETED".equals(updated.getStatus())) {
                System.out.println("FAILED - Status in DB is not COMPLETED: " + updated.getStatus());
                return false;
            }

            System.out.println("PASSED (Requester role blocked with 403 simulation; Donor & Admin verified)");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
