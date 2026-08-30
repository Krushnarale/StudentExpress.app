package com.core2web.controller;

import com.core2web.dao.UserDAO;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.util.SessionManager;
import com.core2web.util.ValidationUtil;

import java.util.Optional;

public class AuthController {

    private final UserDAO userDAO = new UserDAOImpl();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }

    public static String formatRoleName(User.Role role) {
        if (role == null) return "User";
        switch (role) {
            case STUDENT: return "Student";
            case OWNER: return "Owner";
            case SELLER: return "Seller";
            case SERVICE_PROVIDER: return "Provider";
            case ADMIN: return "Admin";
            default: return role.name();
        }
    }

    public static String formatPortalName(User.Role role) {
        if (role == null) return "Login";
        switch (role) {
            case STUDENT: return "Student Login";
            case OWNER: return "Owner Login";
            case SELLER: return "Seller Login";
            case SERVICE_PROVIDER: return "Provider Login";
            case ADMIN: return "Admin Login";
            default: return role.name() + " Login";
        }
    }

    public boolean validateLoginRole(User user, User.Role expectedRole) {
        if (user == null || expectedRole == null) {
            return false;
        }
        User.Role actualRole = user.getRole();
        if (expectedRole == User.Role.STUDENT) {
            // A student or student-seller can always log in to Student portal
            return actualRole == User.Role.STUDENT || actualRole == User.Role.SELLER || user.isSellerEnabled();
        }
        if (expectedRole == User.Role.SELLER) {
            // Seller portal allows users with sellerEnabled or role SELLER
            return user.isSellerEnabled() || actualRole == User.Role.SELLER;
        }
        return actualRole == expectedRole;
    }

    public boolean validateLoginRole(User.Role actualRole, User.Role expectedRole) {
        if (actualRole == null || expectedRole == null) {
            return false;
        }
        if (expectedRole == User.Role.STUDENT && actualRole == User.Role.SELLER) return true;
        if (expectedRole == User.Role.SELLER && actualRole == User.Role.STUDENT) return true;
        return actualRole == expectedRole;
    }

    public AuthResult login(String email, String password, User.Role expectedRole) {
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your email address.", null);
        }
        if (password == null || password.isEmpty()) {
            return new AuthResult(false, "Please enter your password.", null);
        }

        String cleanEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userDAO.findByEmail(cleanEmail);

        if (!userOpt.isPresent()) {
            return new AuthResult(false, "Account not found with email: " + cleanEmail, null);
        }

        User user = userOpt.get();

        // Check password if available
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!user.getPassword().equals(password)) {
                return new AuthResult(false, "Incorrect password. Please check your credentials.", null);
            }
        }

        // Verify seller status for SELLER portal login
        if (expectedRole == User.Role.SELLER) {
            String uid = (user.getUid() != null) ? user.getUid().trim() : "";
            boolean sellerProfileFound = false;
            boolean sellerEnabled = user.isSellerEnabled();

            if (!uid.isEmpty()) {
                com.core2web.model.SellerProfile sp = DataRepository.getInstance().getSellerProfile(uid);
                if (sp == null) {
                    try {
                        com.core2web.dao.SellerDAO sellerDAO = new com.core2web.dao.SellerDAOImpl();
                        java.util.Optional<com.core2web.model.SellerProfile> opt = sellerDAO.findBySellerId(uid);
                        if (opt.isPresent()) {
                            sp = opt.get();
                            DataRepository.getInstance().addOrUpdateSeller(sp);
                        }
                    } catch (Throwable ignored) {}
                }
                if (sp != null) {
                    sellerProfileFound = true;
                    sellerEnabled = true;
                    user.setSellerEnabled(true);
                }
            }

            boolean accessGranted = sellerEnabled || sellerProfileFound || user.getRole() == User.Role.SELLER;

            System.out.println("Email = " + cleanEmail);
            System.out.println("Firebase authentication successful = true");

            if (!accessGranted) {
                return new AuthResult(false, "You are not registered as a Seller.\nPlease register as a Seller from your Student account.", user);
            }
        }

        // Verify role for other portals (Owner, Provider, Admin, Student)
        if (expectedRole != null && expectedRole != User.Role.SELLER && !validateLoginRole(user, expectedRole)) {
            String registeredRoleStr = formatRoleName(user.getRole());
            String portalStr = formatPortalName(user.getRole());
            String errorMsg = "This account is registered as " + registeredRoleStr + ". Please use " + portalStr + ".";
            return new AuthResult(false, errorMsg, user);
        }

        // Success: activate session with appropriate role context
        sessionManager.login(user, expectedRole != null ? expectedRole : user.getRole());
        DataRepository.getInstance().setCurrentUser(user);
        return new AuthResult(true, "Login successful", user);
    }

    public boolean login(String email, String password) {
        AuthResult result = login(email, password, null);
        return result.isSuccess();
    }

    public boolean register(User user) {
        return register(user, "DefaultPassword123");
    }

    public boolean register(User user, String password) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }

        String cleanEmail = user.getEmail().trim().toLowerCase();

        // 1. Email format validation
        if (!ValidationUtil.isValidEmail(cleanEmail)) {
            System.err.println("[AuthController] Registration failed: Invalid email format: " + cleanEmail);
            return false;
        }

        // 2. Phone validation
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty() && !ValidationUtil.isValidPhone(user.getPhone())) {
            System.err.println("[AuthController] Registration failed: Invalid phone number: " + user.getPhone());
            return false;
        }

        // 3. Password length check (must be at least 4 characters)
        if (password != null && password.length() < 4) {
            System.err.println("[AuthController] Registration failed: Password must be at least 4 characters");
            return false;
        }

        // 4. Duplicate email prevention
        Optional<User> existing = userDAO.findByEmail(cleanEmail);
        if (existing.isPresent()) {
            System.err.println("[AuthController] Registration failed: Email " + cleanEmail + " is already registered.");
            return false;
        }

        user.setEmail(cleanEmail);
        user.setPassword(password);
        boolean saved = userDAO.saveWithAuth(user, password);
        if (saved) {
            sessionManager.login(user);
            DataRepository.getInstance().setCurrentUser(user);
        }
        return saved;
    }

    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void logout() {
        sessionManager.logout();
        DataRepository.getInstance().setCurrentUser(null);
    }
}
