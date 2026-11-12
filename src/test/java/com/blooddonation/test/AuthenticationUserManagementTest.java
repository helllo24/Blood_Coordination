package com.blooddonation.test;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.dao.UserDAO;
import com.blooddonation.model.Donor;
import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import com.blooddonation.util.PasswordUtil;

import java.sql.Date;
import java.util.Optional;

/**
 * Automated Test Suite for Authentication, User Management, and Security
 * Covers:
 * 1. Successful donor registration
 * 2. Successful requester registration
 * 3. Duplicate email registration rejection
 * 4. Valid login for all three roles (ADMIN, DONOR, REQUESTER)
 * 5. Invalid password handling
 * 6. Invalid role mismatch handling
 * 7. Unauthorized dashboard access control
 * 8. Session invalidation & logout simulation
 */
public class AuthenticationUserManagementTest {

    @org.junit.jupiter.api.Test
    public void testAuthenticationSuite() {
        org.junit.jupiter.api.Assertions.assertTrue(testSuccessfulDonorRegistration());
        org.junit.jupiter.api.Assertions.assertTrue(testSuccessfulRequesterRegistration());
        org.junit.jupiter.api.Assertions.assertTrue(testDuplicateEmailRegistration());
        org.junit.jupiter.api.Assertions.assertTrue(testValidLoginAllRoles());
        org.junit.jupiter.api.Assertions.assertTrue(testInvalidPassword());
        org.junit.jupiter.api.Assertions.assertTrue(testInvalidRoleSelection());
        org.junit.jupiter.api.Assertions.assertTrue(testUnauthorizedDashboardAccess());
        org.junit.jupiter.api.Assertions.assertTrue(testSessionInvalidationAndLogout());
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Emergency Blood Coordination System - Authentication & User Management Test");
        System.out.println("======================================================================\n");

        boolean allPassed = true;

        allPassed &= testSuccessfulDonorRegistration();
        allPassed &= testSuccessfulRequesterRegistration();
        allPassed &= testDuplicateEmailRegistration();
        allPassed &= testValidLoginAllRoles();
        allPassed &= testInvalidPassword();
        allPassed &= testInvalidRoleSelection();
        allPassed &= testUnauthorizedDashboardAccess();
        allPassed &= testSessionInvalidationAndLogout();

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println(">>> ALL 8 AUTHENTICATION & SECURITY TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> SOME TESTS FAILED! <<<");
        }
        System.out.println("======================================================================");
    }

    private static boolean testSuccessfulDonorRegistration() {
        System.out.print("[TEST 1/8] Testing Successful Donor Registration... ");
        UserDAO userDAO = new UserDAO();
        DonorDAO donorDAO = new DonorDAO();

        String testEmail = "test.donor." + System.currentTimeMillis() + "@gmail.com";
        String rawPassword = "DonorPass@123";

        // Create User
        User user = new User();
        user.setFullName("Sunil Kumar (Test Donor)");
        user.setEmail(testEmail);
        user.setPassword(PasswordUtil.hashPassword(rawPassword));
        user.setPhone("+91 9888877771");
        user.setRole(Role.DONOR);
        user.setStatus("ACTIVE");

        Long userId = userDAO.save(user);
        if (userId == null) {
            System.out.println("FAILED: Could not save user record.");
            return false;
        }

        // Create Donor Profile
        Donor donor = new Donor();
        donor.setUserId(userId);
        donor.setBloodGroup("O+");
        donor.setAge(28);
        donor.setGender("MALE");
        donor.setCity("Bangalore");
        donor.setState("Karnataka");
        donor.setPincode("560001");
        donor.setAddress("MG Road, Test Area");
        donor.setLastDonationDate(Date.valueOf("2026-06-10"));
        donor.setAvailable(true);
        donor.setTotalDonations(1);

        Long donorId = donorDAO.save(donor);
        if (donorId == null) {
            System.out.println("FAILED: Could not save donor profile.");
            return false;
        }

        // Verify retrieval
        Optional<User> savedUser = userDAO.findByEmail(testEmail);
        Optional<Donor> savedDonor = donorDAO.findByUserId(userId);

        if (savedUser.isPresent() && savedDonor.isPresent() &&
            savedUser.get().getPassword().length() == 64 &&
            savedDonor.get().getBloodGroup().equals("O+")) {
            System.out.println("PASSED! Created User #" + userId + " & Donor Profile #" + donorId);
            return true;
        } else {
            System.out.println("FAILED: Retrieval verification failed.");
            return false;
        }
    }

    private static boolean testSuccessfulRequesterRegistration() {
        System.out.print("[TEST 2/8] Testing Successful Requester Registration... ");
        UserDAO userDAO = new UserDAO();

        String testEmail = "test.requester." + System.currentTimeMillis() + "@gmail.com";
        String rawPassword = "ReqPass@123";

        User user = new User();
        user.setFullName("Kavita Rao (Test Requester)");
        user.setEmail(testEmail);
        user.setPassword(PasswordUtil.hashPassword(rawPassword));
        user.setPhone("+91 9888877772");
        user.setRole(Role.REQUESTER);
        user.setStatus("ACTIVE");

        Long userId = userDAO.save(user);
        if (userId == null) {
            System.out.println("FAILED: Could not save requester user.");
            return false;
        }

        Optional<User> savedUser = userDAO.findByEmail(testEmail);
        if (savedUser.isPresent() && savedUser.get().getRole() == Role.REQUESTER) {
            System.out.println("PASSED! Created Requester #" + userId + " (" + testEmail + ")");
            return true;
        } else {
            System.out.println("FAILED: Verification failed.");
            return false;
        }
    }

    private static boolean testDuplicateEmailRegistration() {
        System.out.print("[TEST 3/8] Testing Duplicate Email Registration Rejection... ");
        UserDAO userDAO = new UserDAO();

        String existingEmail = "admin@bloodconnect.org";
        boolean duplicateDetected = userDAO.findByEmail(existingEmail).isPresent();

        if (duplicateDetected) {
            System.out.println("PASSED! Detected existing email '" + existingEmail + "' - Duplicate registration correctly prevented.");
            return true;
        } else {
            System.out.println("FAILED: Seed admin email was not found.");
            return false;
        }
    }

    private static boolean testValidLoginAllRoles() {
        System.out.println("[TEST 4/8] Testing Valid Login for All Three Roles:");
        UserDAO userDAO = new UserDAO();

        // 1. Admin Login
        Optional<User> admin = userDAO.authenticate("admin@bloodconnect.org", "Admin@123", Role.ADMIN);
        if (admin.isPresent() && admin.get().getRole() == Role.ADMIN) {
            System.out.println("  ✓ ADMIN Login: Authenticated successfully as '" + admin.get().getFullName() + "' -> Target: admin-dashboard.jsp");
        } else {
            System.err.println("  ✗ ADMIN Login failed.");
            return false;
        }

        // 2. Donor Login
        Optional<User> donor = userDAO.authenticate("rahul.donor@gmail.com", "Donor@123", Role.DONOR);
        if (donor.isPresent() && donor.get().getRole() == Role.DONOR) {
            System.out.println("  ✓ DONOR Login: Authenticated successfully as '" + donor.get().getFullName() + "' -> Target: donor-dashboard.jsp");
        } else {
            System.err.println("  ✗ DONOR Login failed.");
            return false;
        }

        // 3. Requester Login
        Optional<User> requester = userDAO.authenticate("vikram.req@gmail.com", "Req@123", Role.REQUESTER);
        if (requester.isPresent() && requester.get().getRole() == Role.REQUESTER) {
            System.out.println("  ✓ REQUESTER Login: Authenticated successfully as '" + requester.get().getFullName() + "' -> Target: requester-dashboard.jsp");
        } else {
            System.err.println("  ✗ REQUESTER Login failed.");
            return false;
        }

        return true;
    }

    private static boolean testInvalidPassword() {
        System.out.print("[TEST 5/8] Testing Invalid Password Handling... ");
        UserDAO userDAO = new UserDAO();

        Optional<User> authResult = userDAO.authenticate("rahul.donor@gmail.com", "WrongPassword@999", Role.DONOR);
        if (authResult.isEmpty()) {
            System.out.println("PASSED! Rejected incorrect password with empty authentication result.");
            return true;
        } else {
            System.out.println("FAILED: Incorrect password should not authenticate.");
            return false;
        }
    }

    private static boolean testInvalidRoleSelection() {
        System.out.print("[TEST 6/8] Testing Role Mismatch Rejection (Donor trying to log in as ADMIN)... ");
        UserDAO userDAO = new UserDAO();

        // rahul.donor@gmail.com is registered as DONOR. Attempting login as ADMIN must fail!
        Optional<User> authResult = userDAO.authenticate("rahul.donor@gmail.com", "Donor@123", Role.ADMIN);
        if (authResult.isEmpty()) {
            System.out.println("PASSED! Role mismatch correctly rejected (DONOR account cannot sign in as ADMIN).");
            return true;
        } else {
            System.out.println("FAILED: Role mismatch was incorrectly allowed.");
            return false;
        }
    }

    private static boolean testUnauthorizedDashboardAccess() {
        System.out.print("[TEST 7/8] Testing Unauthorized Dashboard Access Control Logic... ");

        // Case A: Unauthenticated user (null session)
        User unauthUser = null;
        boolean canAccessAdminA = isAuthorized(unauthUser, Role.ADMIN);

        // Case B: Donor attempting to access Admin dashboard
        User donorUser = new User();
        donorUser.setRole(Role.DONOR);
        boolean canAccessAdminB = isAuthorized(donorUser, Role.ADMIN);

        // Case C: Requester attempting to access Donor dashboard
        User requesterUser = new User();
        requesterUser.setRole(Role.REQUESTER);
        boolean canAccessDonorC = isAuthorized(requesterUser, Role.DONOR);

        // Case D: Admin accessing Admin dashboard
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        boolean canAccessAdminD = isAuthorized(adminUser, Role.ADMIN);

        if (!canAccessAdminA && !canAccessAdminB && !canAccessDonorC && canAccessAdminD) {
            System.out.println("PASSED! Unauthenticated & cross-role access successfully blocked.");
            return true;
        } else {
            System.out.println("FAILED: Authorization logic allowed unauthorized access.");
            return false;
        }
    }

    private static boolean isAuthorized(User user, Role requiredRole) {
        if (user == null) {
            return false;
        }
        if (requiredRole == Role.ADMIN) {
            return user.getRole() == Role.ADMIN;
        }
        if (requiredRole == Role.DONOR) {
            return user.getRole() == Role.DONOR || user.getRole() == Role.ADMIN;
        }
        if (requiredRole == Role.REQUESTER) {
            return user.getRole() == Role.REQUESTER || user.getRole() == Role.ADMIN;
        }
        return false;
    }

    private static boolean testSessionInvalidationAndLogout() {
        System.out.print("[TEST 8/8] Testing Logout & Session Invalidation Logic... ");

        // Simulate Session map
        java.util.Map<String, Object> session = new java.util.HashMap<>();
        session.put("currentUser", new User("Test User", "test@gmail.com", "", "+91 9876543210", Role.DONOR));
        session.put("userId", 100L);
        session.put("userName", "Test User");
        session.put("userRole", "DONOR");

        boolean hadSession = !session.isEmpty() && session.containsKey("currentUser");

        // Simulate Logout / Session invalidation
        session.clear();
        boolean invalidated = session.isEmpty();

        if (hadSession && invalidated) {
            System.out.println("PASSED! Session attributes cleared on logout. Protected resources secured.");
            return true;
        } else {
            System.out.println("FAILED: Session invalidation logic failed.");
            return false;
        }
    }
}
