package com.core2web;

import com.core2web.controller.AuthController;
import com.core2web.dao.UserDAO;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.User;

public class AuthRoleValidationTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STUDENTEXPRESS ROLE AUTHENTICATION TEST SUITE   ");
        System.out.println("==================================================");

        AuthController authController = new AuthController();
        UserDAO userDAO = new UserDAOImpl();

        // Prepare test accounts
        User testStudent = new User("t-stud", "Test Student", "teststudent@example.com", "+91 99999 11111", User.Role.STUDENT, "pass123");
        userDAO.save(testStudent);

        User testOwner = new User("t-own", "Test Owner", "testowner@example.com", "+91 99999 22222", User.Role.OWNER, "pass123");
        userDAO.save(testOwner);

        User testSeller = new User("t-sell", "Test Seller", "testseller@example.com", "+91 99999 33333", User.Role.SELLER, "pass123");
        userDAO.save(testSeller);

        User testProvider = new User("t-prov", "Test Provider", "testprovider@example.com", "+91 99999 44444", User.Role.SERVICE_PROVIDER, "pass123");
        userDAO.save(testProvider);

        User testAdmin = new User("t-admin", "Test Admin", "testadmin@example.com", "+91 99999 55555", User.Role.ADMIN, "pass123");
        userDAO.save(testAdmin);

        int passed = 0;
        int failed = 0;

        // Test 1: Create/login Student -> Student Login succeeds
        AuthController.AuthResult t1 = authController.login("teststudent@example.com", "pass123", User.Role.STUDENT);
        if (t1.isSuccess()) {
            System.out.println("✔ Test 1 PASSED: Student Login succeeds for Student credentials.");
            passed++;
        } else {
            System.err.println("❌ Test 1 FAILED: " + t1.getMessage());
            failed++;
        }

        // Test 2: Use same Student credentials on Owner Login -> MUST fail
        AuthController.AuthResult t2 = authController.login("teststudent@example.com", "pass123", User.Role.OWNER);
        if (!t2.isSuccess() && t2.getMessage().contains("This account is registered as Student. Please use Student Login.")) {
            System.out.println("✔ Test 2 PASSED: Student credentials on Owner Login rejected with message: " + t2.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 2 FAILED: Success=" + t2.isSuccess() + ", Message=" + t2.getMessage());
            failed++;
        }

        // Test 3: Use same Student credentials on Seller Login -> MUST fail
        AuthController.AuthResult t3 = authController.login("teststudent@example.com", "pass123", User.Role.SELLER);
        if (!t3.isSuccess() && t3.getMessage().contains("This account is registered as Student. Please use Student Login.")) {
            System.out.println("✔ Test 3 PASSED: Student credentials on Seller Login rejected with message: " + t3.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 3 FAILED: Success=" + t3.isSuccess() + ", Message=" + t3.getMessage());
            failed++;
        }

        // Test 4: Use same Student credentials on Provider Login -> MUST fail
        AuthController.AuthResult t4 = authController.login("teststudent@example.com", "pass123", User.Role.SERVICE_PROVIDER);
        if (!t4.isSuccess() && t4.getMessage().contains("This account is registered as Student. Please use Student Login.")) {
            System.out.println("✔ Test 4 PASSED: Student credentials on Provider Login rejected with message: " + t4.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 4 FAILED: Success=" + t4.isSuccess() + ", Message=" + t4.getMessage());
            failed++;
        }

        // Test 5: Use same Student credentials on Admin Login -> MUST fail
        AuthController.AuthResult t5 = authController.login("teststudent@example.com", "pass123", User.Role.ADMIN);
        if (!t5.isSuccess() && t5.getMessage().contains("This account is registered as Student. Please use Student Login.")) {
            System.out.println("✔ Test 5 PASSED: Student credentials on Admin Login rejected with message: " + t5.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 5 FAILED: Success=" + t5.isSuccess() + ", Message=" + t5.getMessage());
            failed++;
        }

        // Test 6: Create Owner account -> Owner Login succeeds, Student Login fails
        AuthController.AuthResult t6a = authController.login("testowner@example.com", "pass123", User.Role.OWNER);
        AuthController.AuthResult t6b = authController.login("testowner@example.com", "pass123", User.Role.STUDENT);
        if (t6a.isSuccess() && !t6b.isSuccess() && t6b.getMessage().contains("This account is registered as Owner. Please use Owner Login.")) {
            System.out.println("✔ Test 6 PASSED: Owner Login succeeds and Student Login correctly rejected with message: " + t6b.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 6 FAILED: t6a.isSuccess=" + t6a.isSuccess() + ", t6b msg=" + t6b.getMessage());
            failed++;
        }

        // Test 7: Create Seller account -> Seller Login succeeds, other role logins fail
        AuthController.AuthResult t7a = authController.login("testseller@example.com", "pass123", User.Role.SELLER);
        AuthController.AuthResult t7b = authController.login("testseller@example.com", "pass123", User.Role.STUDENT);
        if (t7a.isSuccess() && !t7b.isSuccess() && t7b.getMessage().contains("This account is registered as Seller. Please use Seller Login.")) {
            System.out.println("✔ Test 7 PASSED: Seller Login succeeds and other role logins correctly rejected with message: " + t7b.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 7 FAILED: t7a=" + t7a.isSuccess() + ", t7b=" + t7b.getMessage());
            failed++;
        }

        // Test 8: Create Provider account -> Provider Login succeeds, other role logins fail
        AuthController.AuthResult t8a = authController.login("testprovider@example.com", "pass123", User.Role.SERVICE_PROVIDER);
        AuthController.AuthResult t8b = authController.login("testprovider@example.com", "pass123", User.Role.STUDENT);
        if (t8a.isSuccess() && !t8b.isSuccess() && t8b.getMessage().contains("This account is registered as Provider. Please use Provider Login.")) {
            System.out.println("✔ Test 8 PASSED: Provider Login succeeds and other role logins correctly rejected with message: " + t8b.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 8 FAILED: t8a=" + t8a.isSuccess() + ", t8b=" + t8b.getMessage());
            failed++;
        }

        // Test 9: Admin account -> only Admin Login succeeds
        AuthController.AuthResult t9a = authController.login("testadmin@example.com", "pass123", User.Role.ADMIN);
        AuthController.AuthResult t9b = authController.login("testadmin@example.com", "pass123", User.Role.STUDENT);
        if (t9a.isSuccess() && !t9b.isSuccess() && t9b.getMessage().contains("This account is registered as Admin. Please use Admin Login.")) {
            System.out.println("✔ Test 9 PASSED: Admin Login succeeds and other role logins rejected with message: " + t9b.getMessage());
            passed++;
        } else {
            System.err.println("❌ Test 9 FAILED: t9a=" + t9a.isSuccess() + ", t9b=" + t9b.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("RESULTS: " + passed + "/9 PASSED, " + failed + " FAILED");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
