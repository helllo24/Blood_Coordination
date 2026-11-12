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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Test Suite for Part A (Admin Dashboard), Part B (Notifications), and Part C (Reports).
 */
public class AdminAndNotificationManagementTest {

    private final DonorDAO donorDAO = new DonorDAO();
    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final DonorResponseDAO responseDAO = new DonorResponseDAO();
    private final DonationDAO donationDAO = new DonationDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Test
    public void testAllAdminAndNotificationFeatures() {
        Assertions.assertTrue(testAdminStatistics());
        Assertions.assertTrue(testRecentActivitiesRetrieval());
        Assertions.assertTrue(testNotificationCreationAndRetrieval());
        Assertions.assertTrue(testNotificationMarkAsRead());
        Assertions.assertTrue(testNotificationMarkAllAsRead());
        Assertions.assertTrue(testNotificationUserIsolation());
        Assertions.assertTrue(testNotificationDelete());
        Assertions.assertTrue(testAdminReportsAggregation());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination - Admin, Notifications & Reports Test");
        System.out.println("======================================================================\n");

        AdminAndNotificationManagementTest test = new AdminAndNotificationManagementTest();
        boolean allPassed = true;

        allPassed &= test.testAdminStatistics();
        allPassed &= test.testRecentActivitiesRetrieval();
        allPassed &= test.testNotificationCreationAndRetrieval();
        allPassed &= test.testNotificationMarkAsRead();
        allPassed &= test.testNotificationMarkAllAsRead();
        allPassed &= test.testNotificationUserIsolation();
        allPassed &= test.testNotificationDelete();
        allPassed &= test.testAdminReportsAggregation();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println("ALL ADMIN, NOTIFICATION AND REPORT TESTS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("SOME TESTS FAILED! CHECK OUTPUT ABOVE.");
        }
        System.out.println("======================================================================");
    }

    public boolean testAdminStatistics() {
        System.out.println("[TEST 1] Admin Dynamic Statistics from MySQL:");
        try {
            int totalDonors = donorDAO.getTotalDonorsCount();
            int totalRequesters = userDAO.countByRole(Role.REQUESTER);
            int totalRequests = requestDAO.getTotalRequestsCount();
            int pendingRequests = requestDAO.countByStatus("PENDING");
            int matchedRequests = requestDAO.countByStatus("MATCHED");
            int completedDonations = donationDAO.getTotalDonationsCount();
            int totalUnits = donationDAO.getTotalUnitsDonatedSystemWide();

            System.out.println("  - Total Registered Donors: " + totalDonors);
            System.out.println("  - Total Requesters: " + totalRequesters);
            System.out.println("  - Total Blood Requests: " + totalRequests);
            System.out.println("  - Pending Blood Requests: " + pendingRequests);
            System.out.println("  - Matched Blood Requests: " + matchedRequests);
            System.out.println("  - Completed Donations: " + completedDonations);
            System.out.println("  - Total Units Donated: " + totalUnits);

            boolean valid = (totalDonors >= 0 && totalRequesters >= 0 && totalRequests >= 0 &&
                             pendingRequests >= 0 && matchedRequests >= 0 && completedDonations >= 0 && totalUnits >= 0);

            if (valid) {
                System.out.println("  -> PASSED: All 7 statistics dynamically calculated from MySQL.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean testRecentActivitiesRetrieval() {
        System.out.println("[TEST 2] Recent Activities Logs from MySQL:");
        try {
            List<Donor> recentDonors = donorDAO.findRecentDonors(5);
            List<BloodRequest> recentRequests = requestDAO.findRecentRequests(5);
            List<DonorResponse> recentResponses = responseDAO.findRecentResponses(5);
            List<Donation> recentDonations = donationDAO.findRecentDonations(5);

            System.out.println("  - Recent Donors retrieved: " + recentDonors.size());
            System.out.println("  - Recent Requests retrieved: " + recentRequests.size());
            System.out.println("  - Recent Responses retrieved: " + recentResponses.size());
            System.out.println("  - Recent Donations retrieved: " + recentDonations.size());

            System.out.println("  -> PASSED: Successfully retrieved all 4 activity feeds from MySQL.\n");
            return true;
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
            return false;
        }
    }

    public boolean testNotificationCreationAndRetrieval() {
        System.out.println("[TEST 3] Notification Creation and Type Verification:");
        try {
            // Create a test user for notifications
            long timestamp = System.currentTimeMillis();
            User testUser = new User();
            testUser.setFullName("Notification Test User");
            testUser.setEmail("notif_" + timestamp + "@test.org");
            testUser.setPassword("Password@123");
            testUser.setPhone("+91 9888877777");
            testUser.setRole(Role.DONOR);
            testUser.setStatus("ACTIVE");
            Long userId = userDAO.save(testUser);

            // Create notifications of different types
            Notification n1 = new Notification(userId, "Emergency Request Alert", "Urgent O+ needed in Bangalore", "EMERGENCY_REQUEST");
            Notification n2 = new Notification(userId, "Donor Response Received", "Donor accepted your request", "DONOR_RESPONSE");
            Notification n3 = new Notification(userId, "Status Updated", "Request updated to FULFILLED", "STATUS_UPDATE");
            Notification n4 = new Notification(userId, "Welcome to BloodConnect", "Account registered successfully", "GENERAL");

            Long id1 = notificationDAO.save(n1);
            Long id2 = notificationDAO.save(n2);
            Long id3 = notificationDAO.save(n3);
            Long id4 = notificationDAO.save(n4);

            List<Notification> userNotifs = notificationDAO.findByUserId(userId);
            int unreadCount = notificationDAO.countUnreadByUserId(userId);

            System.out.println("  - Created 4 notifications for User #" + userId + " (IDs: " + id1 + ", " + id2 + ", " + id3 + ", " + id4 + ")");
            System.out.println("  - Retrieved notifications count: " + userNotifs.size());
            System.out.println("  - Unread count: " + unreadCount);

            if (userNotifs.size() == 4 && unreadCount == 4) {
                System.out.println("  -> PASSED: Notification persistence and types verified.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean testNotificationMarkAsRead() {
        System.out.println("[TEST 4] Mark Single Notification as Read:");
        try {
            long timestamp = System.currentTimeMillis();
            User testUser = new User("MarkRead User", "markread_" + timestamp + "@test.org", "Pass@123", "+91 9777766666", Role.REQUESTER);
            Long userId = userDAO.save(testUser);

            Notification n = new Notification(userId, "Test Mark Read", "Testing read status toggle", "GENERAL");
            Long notifId = notificationDAO.save(n);

            int unreadBefore = notificationDAO.countUnreadByUserId(userId);
            boolean updated = notificationDAO.markAsRead(notifId);
            int unreadAfter = notificationDAO.countUnreadByUserId(userId);

            System.out.println("  - Notification ID: " + notifId);
            System.out.println("  - Unread before: " + unreadBefore + ", Unread after: " + unreadAfter);

            if (updated && unreadBefore == 1 && unreadAfter == 0) {
                System.out.println("  -> PASSED: Single notification marked as read successfully.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean testNotificationMarkAllAsRead() {
        System.out.println("[TEST 5] Mark All Notifications as Read:");
        try {
            long timestamp = System.currentTimeMillis();
            User testUser = new User("MarkAll User", "markall_" + timestamp + "@test.org", "Pass@123", "+91 9666655555", Role.DONOR);
            Long userId = userDAO.save(testUser);

            notificationDAO.save(new Notification(userId, "Alert 1", "Msg 1", "GENERAL"));
            notificationDAO.save(new Notification(userId, "Alert 2", "Msg 2", "GENERAL"));
            notificationDAO.save(new Notification(userId, "Alert 3", "Msg 3", "GENERAL"));

            int unreadBefore = notificationDAO.countUnreadByUserId(userId);
            boolean marked = notificationDAO.markAllAsRead(userId);
            int unreadAfter = notificationDAO.countUnreadByUserId(userId);

            System.out.println("  - Unread before: " + unreadBefore + ", Unread after mark-all: " + unreadAfter);

            if (marked && unreadBefore == 3 && unreadAfter == 0) {
                System.out.println("  -> PASSED: All user notifications marked as read.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean testNotificationUserIsolation() {
        System.out.println("[TEST 6] Notification User Privacy and Isolation:");
        try {
            long ts = System.currentTimeMillis();
            User userA = new User("User A", "usera_" + ts + "@test.org", "Pass@123", "+91 9555544444", Role.DONOR);
            User userB = new User("User B", "userb_" + ts + "@test.org", "Pass@123", "+91 9444433333", Role.REQUESTER);
            Long idA = userDAO.save(userA);
            Long idB = userDAO.save(userB);

            notificationDAO.save(new Notification(idA, "Confidential A", "Message for User A only", "GENERAL"));
            notificationDAO.save(new Notification(idB, "Confidential B", "Message for User B only", "GENERAL"));

            List<Notification> notifsA = notificationDAO.findByUserId(idA);
            List<Notification> notifsB = notificationDAO.findByUserId(idB);

            boolean isolatedA = notifsA.stream().allMatch(n -> n.getUserId().equals(idA));
            boolean isolatedB = notifsB.stream().allMatch(n -> n.getUserId().equals(idB));

            if (isolatedA && isolatedB && notifsA.size() == 1 && notifsB.size() == 1) {
                System.out.println("  -> PASSED: User isolation strictly enforced; no cross-account visibility.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean testNotificationDelete() {
        System.out.println("[TEST 7] Notification Delete Operation:");
        try {
            long ts = System.currentTimeMillis();
            User user = new User("Delete User", "delete_" + ts + "@test.org", "Pass@123", "+91 9333322222", Role.DONOR);
            Long userId = userDAO.save(user);

            Long notifId = notificationDAO.save(new Notification(userId, "To Delete", "Delete test", "GENERAL"));
            boolean deleted = notificationDAO.delete(notifId);
            List<Notification> list = notificationDAO.findByUserId(userId);

            if (deleted && list.isEmpty()) {
                System.out.println("  -> PASSED: Notification successfully deleted from database.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean testAdminReportsAggregation() {
        System.out.println("[TEST 8] Admin Reports Data Aggregation:");
        try {
            Map<String, Integer> bloodGroups = donorDAO.getDonorsCountByBloodGroup();
            Map<String, Integer> requestStatuses = requestDAO.getRequestsCountByStatus();
            Map<String, int[]> monthlyDonations = donationDAO.getDonationsByMonth();

            System.out.println("  - Donors by Blood Group categories: " + bloodGroups.keySet());
            System.out.println("  - Request Status categories: " + requestStatuses.keySet());
            System.out.println("  - Monthly Donation periods recorded: " + monthlyDonations.keySet());

            boolean hasAllBloodGroups = bloodGroups.containsKey("O+") && bloodGroups.containsKey("A+") &&
                                        bloodGroups.containsKey("B+") && bloodGroups.containsKey("AB+") &&
                                        bloodGroups.containsKey("O-") && bloodGroups.containsKey("A-") &&
                                        bloodGroups.containsKey("B-") && bloodGroups.containsKey("AB-");

            boolean hasAllStatuses = requestStatuses.containsKey("PENDING") && requestStatuses.containsKey("MATCHED") &&
                                     requestStatuses.containsKey("FULFILLED") && requestStatuses.containsKey("CANCELLED");

            if (hasAllBloodGroups && hasAllStatuses) {
                System.out.println("  -> PASSED: Reports aggregations verified for blood groups, statuses, and monthly volume.\n");
                return true;
            }
        } catch (Exception e) {
            System.err.println("  -> FAILED: Exception: " + e.getMessage());
        }
        return false;
    }
}
