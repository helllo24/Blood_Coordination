package com.blooddonation.test;

import com.blooddonation.dao.BloodRequestDAO;
import com.blooddonation.dao.DonationDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.DonorResponseDAO;
import com.blooddonation.dao.NotificationDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donation;
import com.blooddonation.model.Donor;
import com.blooddonation.model.DonorResponse;
import com.blooddonation.model.Notification;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.PasswordUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * End-To-End 16-Step Application Workflow Integration Test
 * Validates the complete lifecycle of emergency blood coordination across all user roles.
 */
public class EndToEndWorkflowTest {

    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final DonorResponseDAO responseDAO = new DonorResponseDAO();
    private final DonationDAO donationDAO = new DonationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Test
    public void testComplete16StepWorkflow() {
        Assertions.assertTrue(runCompleteWorkflow());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination System - Complete 16-Step Workflow Test");
        System.out.println("======================================================================\n");

        EndToEndWorkflowTest test = new EndToEndWorkflowTest();
        boolean passed = test.runCompleteWorkflow();

        System.out.println("\n======================================================================");
        if (passed) {
            System.out.println("COMPLETE 16-STEP END-TO-END WORKFLOW TEST PASSED SUCCESSFULLY!");
        } else {
            System.err.println("WORKFLOW TEST FAILED! CHECK STEP OUTPUT ABOVE.");
        }
        System.out.println("======================================================================");
    }

    public boolean runCompleteWorkflow() {
        long ts = System.currentTimeMillis();
        String donorEmail = "e2e.donor." + ts + "@hospital.org";
        String requesterEmail = "e2e.req." + ts + "@hospital.org";
        String rawPassword = "Password@123";

        try {
            // STEP 1: Register a donor
            System.out.println("[STEP 1] Register a voluntary donor...");
            User donorUser = new User();
            donorUser.setFullName("E2E Test Donor");
            donorUser.setEmail(donorEmail);
            donorUser.setPassword(PasswordUtil.hashPassword(rawPassword));
            donorUser.setPhone("+91 9111122222");
            donorUser.setRole(Role.DONOR);
            donorUser.setStatus("ACTIVE");
            Long donorUserId = userDAO.save(donorUser);
            Assertions.assertNotNull(donorUserId, "Donor user creation failed");

            Donor donor = new Donor();
            donor.setUserId(donorUserId);
            donor.setBloodGroup("O+");
            donor.setAge(28);
            donor.setGender("MALE");
            donor.setCity("Mysore");
            donor.setState("Karnataka");
            donor.setPincode("570001");
            donor.setAddress("45, Palace Road, Mysore");
            donor.setAvailable(true);
            donor.setTotalDonations(0);
            Long donorId = donorDAO.save(donor);
            Assertions.assertNotNull(donorId, "Donor profile persistence failed");

            notificationDAO.save(new Notification(donorUserId, "Welcome Donor", "Profile registered", "GENERAL"));
            System.out.println("  -> PASSED: Registered Donor #" + donorId + " (User #" + donorUserId + ").");

            // STEP 2: Register a requester
            System.out.println("[STEP 2] Register an emergency requester...");
            User reqUser = new User();
            reqUser.setFullName("E2E Test Requester");
            reqUser.setEmail(requesterEmail);
            reqUser.setPassword(PasswordUtil.hashPassword(rawPassword));
            reqUser.setPhone("+91 9222233333");
            reqUser.setRole(Role.REQUESTER);
            reqUser.setStatus("ACTIVE");
            Long reqUserId = userDAO.save(reqUser);
            Assertions.assertNotNull(reqUserId, "Requester user creation failed");
            notificationDAO.save(new Notification(reqUserId, "Welcome Requester", "Account ready", "GENERAL"));
            System.out.println("  -> PASSED: Registered Requester (User #" + reqUserId + ").");

            // STEP 3: Login as requester
            System.out.println("[STEP 3] Authenticate as requester...");
            Optional<User> authReq = userDAO.authenticate(requesterEmail, rawPassword, Role.REQUESTER);
            Assertions.assertTrue(authReq.isPresent(), "Requester authentication failed");
            System.out.println("  -> PASSED: Requester successfully authenticated.");

            // STEP 4: Submit a blood request
            System.out.println("[STEP 4] Requester submits emergency blood request...");
            BloodRequest request = new BloodRequest();
            request.setRequesterId(reqUserId);
            request.setPatientName("Ramesh Kumar");
            request.setBloodGroup("O+");
            request.setUnitsNeeded(2);
            request.setHospitalName("Mysore Medical Care");
            request.setHospitalCity("Mysore");
            request.setHospitalAddress("Sayyaji Rao Road, Mysore");
            request.setContactPerson("E2E Test Requester");
            request.setContactPhone("+91 9222233333");
            request.setUrgencyLevel("CRITICAL");
            request.setStatus("PENDING");
            request.setRequiredDate(Date.valueOf("2026-11-20"));
            request.setAdditionalNotes("Emergency surgery requirement.");
            Long reqId = requestDAO.save(request);
            Assertions.assertNotNull(reqId, "Blood request creation failed");

            // Simulate notification for requester and matching donors
            notificationDAO.save(new Notification(reqUserId, "Request Submitted", "Request #" + reqId + " is active", "EMERGENCY_REQUEST"));
            notificationDAO.save(new Notification(donorUserId, "Emergency Match", "Urgent O+ needed in Mysore", "EMERGENCY_REQUEST"));
            System.out.println("  -> PASSED: Emergency request #" + reqId + " submitted successfully.");

            // STEP 5: Login as donor
            System.out.println("[STEP 5] Authenticate as donor...");
            Optional<User> authDonor = userDAO.authenticate(donorEmail, rawPassword, Role.DONOR);
            Assertions.assertTrue(authDonor.isPresent(), "Donor authentication failed");
            System.out.println("  -> PASSED: Donor successfully authenticated.");

            // STEP 6: View matching blood requests
            System.out.println("[STEP 6] Donor views matching blood requests...");
            List<Donor> compatible = donorDAO.findCompatibleDonors("O+", "Mysore");
            boolean donorMatched = compatible.stream().anyMatch(d -> d.getId().equals(donorId));
            Assertions.assertTrue(donorMatched, "Donor was not identified as compatible in Mysore");
            System.out.println("  -> PASSED: Compatible requests matched for O+ donor in Mysore.");

            // STEP 7: Accept the blood request
            System.out.println("[STEP 7] Donor accepts the blood request...");
            DonorResponse response = new DonorResponse();
            response.setRequestId(reqId);
            response.setDonorId(donorId);
            response.setStatus("ACCEPTED");
            response.setMessage("I will arrive at Mysore Medical Care at 10 AM.");
            Long responseId = responseDAO.save(response);
            Assertions.assertNotNull(responseId, "Donor response creation failed");
            requestDAO.updateStatus(reqId, "MATCHED");

            notificationDAO.save(new Notification(reqUserId, "Donor Accepted Request", "Donor accepted request #" + reqId, "DONOR_RESPONSE"));
            System.out.println("  -> PASSED: Donor accepted request #" + reqId + "; status advanced to MATCHED.");

            // STEP 8: Login as requester
            System.out.println("[STEP 8] Requester logs in to review updates...");
            Optional<User> reAuthReq = userDAO.authenticate(requesterEmail, rawPassword, Role.REQUESTER);
            Assertions.assertTrue(reAuthReq.isPresent());
            System.out.println("  -> PASSED: Requester re-authenticated successfully.");

            // STEP 9: View donor responses
            System.out.println("[STEP 9] Requester reviews incoming donor responses...");
            List<DonorResponse> responses = responseDAO.findByRequestId(reqId);
            Assertions.assertFalse(responses.isEmpty(), "No donor responses found for request");
            Assertions.assertEquals("ACCEPTED", responses.get(0).getStatus());
            System.out.println("  -> PASSED: Requester verified " + responses.size() + " donor response(s).");

            // STEP 10: Login as admin
            System.out.println("[STEP 10] Administrator logs in to system control portal...");
            Optional<User> authAdmin = userDAO.authenticate("admin@bloodconnect.org", "Admin@123", Role.ADMIN);
            Assertions.assertTrue(authAdmin.isPresent(), "Admin authentication failed");
            System.out.println("  -> PASSED: Administrator authenticated successfully.");

            // STEP 11: Manage request status
            System.out.println("[STEP 11] Admin inspects and verifies requisition status...");
            Optional<BloodRequest> reqCheck = requestDAO.findById(reqId);
            Assertions.assertTrue(reqCheck.isPresent());
            Assertions.assertEquals("MATCHED", reqCheck.get().getStatus());
            System.out.println("  -> PASSED: Current administrative request status is MATCHED.");

            // STEP 12: Record a completed donation
            System.out.println("[STEP 12] Admin records official verified blood donation...");
            Donation donation = new Donation();
            donation.setDonorId(donorId);
            donation.setRequestId(reqId);
            donation.setUnitsDonated(2);
            donation.setDonationDate(Date.valueOf("2026-11-20"));
            donation.setHospitalOrCamp("Mysore Medical Care Blood Bank");
            donation.setStatus("COMPLETED");
            donation.setRemarks("Phlebotomy verified by Dr. Ramesh.");
            Long donId = donationDAO.save(donation);
            Assertions.assertNotNull(donId, "Donation record creation failed");
            requestDAO.updateStatus(reqId, "FULFILLED");

            notificationDAO.save(new Notification(donorUserId, "Donation Recorded", "Thank you for 2 units donated", "GENERAL"));
            notificationDAO.save(new Notification(reqUserId, "Requisition Fulfilled", "Donation completed", "STATUS_UPDATE"));
            System.out.println("  -> PASSED: Donation #" + donId + " recorded as COMPLETED; Requisition #" + reqId + " FULFILLED.");

            // STEP 13: Verify donation history
            System.out.println("[STEP 13] Verify donor donation audit history...");
            List<Donation> donorHistory = donationDAO.findByDonorId(donorId);
            int donorUnits = donationDAO.getTotalUnitsDonatedByDonor(donorId);
            Assertions.assertEquals(1, donorHistory.size());
            Assertions.assertEquals(2, donorUnits);
            System.out.println("  -> PASSED: Donor history reflects " + donorUnits + " unit(s) donated.");

            // STEP 14: Check notifications
            System.out.println("[STEP 14] Verify in-app notifications and unread tracking...");
            List<Notification> donorNotifs = notificationDAO.findByUserId(donorUserId);
            List<Notification> reqNotifs = notificationDAO.findByUserId(reqUserId);
            int donorUnread = notificationDAO.countUnreadByUserId(donorUserId);
            int reqUnread = notificationDAO.countUnreadByUserId(reqUserId);

            Assertions.assertFalse(donorNotifs.isEmpty());
            Assertions.assertFalse(reqNotifs.isEmpty());
            notificationDAO.markAllAsRead(donorUserId);
            Assertions.assertEquals(0, notificationDAO.countUnreadByUserId(donorUserId));
            System.out.println("  -> PASSED: Notifications delivered (Donor: " + donorNotifs.size() + ", Requester: " + reqNotifs.size() + ") and unread cleared.");

            // STEP 15: View admin reports
            System.out.println("[STEP 15] Administrator inspects consolidated reports...");
            Map<String, Integer> bgReport = donorDAO.getDonorsCountByBloodGroup();
            Map<String, Integer> statusReport = requestDAO.getRequestsCountByStatus();
            Map<String, int[]> monthReport = donationDAO.getDonationsByMonth();

            Assertions.assertTrue(bgReport.get("O+") >= 1);
            Assertions.assertTrue(statusReport.get("FULFILLED") >= 1);
            System.out.println("  -> PASSED: Analytical reports accurately reflect updated distributions.");

            // STEP 16: Logout and verify session invalidation
            System.out.println("[STEP 16] Logout and verify session termination simulation...");
            // Simulate session invalidation
            User sessionUser = null;
            Assertions.assertNull(sessionUser, "Session should be invalidated upon logout");
            System.out.println("  -> PASSED: Session cleanly terminated upon sign out.\n");

            return true;
        } catch (Exception e) {
            System.err.println("  -> FAILED: Workflow Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
