package com.core2web.controller;

import com.core2web.dao.UserDAO;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.User;
import com.core2web.service.CloudinaryService;
import com.core2web.util.SessionManager;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class UserController {

    private final UserDAO userDAO = new UserDAOImpl();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    public CloudinaryService.UploadResult uploadProfileImage(File file) {
        return CloudinaryService.uploadImage(file, "profileImages");
    }

    public boolean updateCurrentUser(User user) {
        if (user == null) return false;
        boolean saved = userDAO.save(user);
        if (saved) {
            sessionManager.login(user);
        }
        return saved;
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userDAO.findById(id);
    }
}
