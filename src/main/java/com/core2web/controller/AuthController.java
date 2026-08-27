package com.core2web.controller;

import com.core2web.dao.UserDAO;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.User;
import com.core2web.util.SessionManager;
import com.core2web.util.ValidationUtil;

import java.util.Optional;

public class AuthController {

    private final UserDAO userDAO = new UserDAOImpl();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public boolean login(String email, String password) {
        if (email == null || email.trim().isEmpty()) return false;
        
        Optional<User> userOpt = userDAO.findByEmail(email.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sessionManager.login(user);
            return true;
        }
        return false;
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

        // 3. Password length check (must be at least 6 characters)
        if (password != null && password.length() < 6) {
            System.err.println("[AuthController] Registration failed: Password must be at least 6 characters");
            return false;
        }

        // 4. Duplicate email prevention
        Optional<User> existing = userDAO.findByEmail(cleanEmail);
        if (existing.isPresent()) {
            System.err.println("[AuthController] Registration failed: Email " + cleanEmail + " is already registered.");
            return false;
        }

        user.setEmail(cleanEmail);
        boolean saved = userDAO.saveWithAuth(user, password);
        if (saved) {
            sessionManager.login(user);
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
    }
}
