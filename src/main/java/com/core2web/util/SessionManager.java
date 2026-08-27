package com.core2web.util;

import com.core2web.model.User;

public class SessionManager {

    private static SessionManager instance;

    private String uid;
    private String name;
    private String email;
    private User.Role role;
    private String phone;
    private String profileImage;
    private String profilePublicId;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        if (user != null) {
            this.currentUser = user;
            this.uid = user.getUid();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
            this.phone = user.getPhone();
            this.profileImage = user.getProfileImage();
            this.profilePublicId = user.getProfilePublicId();
        }
    }

    public void setActiveUser(User user) {
        login(user);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getActiveUser() {
        return currentUser;
    }

    public String getUid() {
        return uid != null ? uid : (currentUser != null ? currentUser.getUid() : null);
    }

    public String getName() {
        return name != null ? name : (currentUser != null ? currentUser.getName() : null);
    }

    public String getEmail() {
        return email != null ? email : (currentUser != null ? currentUser.getEmail() : null);
    }

    public User.Role getRole() {
        return role != null ? role : (currentUser != null ? currentUser.getRole() : null);
    }

    public String getPhone() {
        return phone != null ? phone : (currentUser != null ? currentUser.getPhone() : "");
    }

    public String getProfileImage() {
        return profileImage != null ? profileImage : (currentUser != null ? currentUser.getProfileImage() : "");
    }

    public String getProfilePublicId() {
        return profilePublicId != null ? profilePublicId : (currentUser != null ? currentUser.getProfilePublicId() : "");
    }

    public boolean isLoggedIn() {
        return currentUser != null || uid != null;
    }

    public void logout() {
        this.currentUser = null;
        this.uid = null;
        this.name = null;
        this.email = null;
        this.role = null;
        this.phone = null;
        this.profileImage = null;
        this.profilePublicId = null;
    }
}
