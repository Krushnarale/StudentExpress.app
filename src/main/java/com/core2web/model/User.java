package com.core2web.model;

public class User {
    public enum Role {
        STUDENT,
        OWNER,
        SELLER,
        SERVICE_PROVIDER,
        ADMIN
    }

    private String id; // maps to uid
    private String name;
    private String email;
    private String phone;
    private String college;
    private String branch;
    private Role role;
    private String password;
    private String profileImage;
    private String profilePublicId;
    private long createdAt;
    private long updatedAt;

    public User(String id, String name, String email, String phone, Role role) {
        this(id, name, email, phone, role, "123456");
    }

    public User(String id, String name, String email, String phone, Role role, String password) {
        this(id, name, email, phone, "COEP Pune", "Computer Engineering", role, password, System.currentTimeMillis());
    }

    public User(String id, String name, String email, String phone, String college, String branch, Role role, String password, long createdAt) {
        this(id, name, email, phone, college, branch, role, password, "", createdAt);
    }

    public User(String id, String name, String email, String phone, String college, String branch, Role role, String password, String profileImage, long createdAt) {
        this(id, name, email, phone, college, branch, role, password, profileImage, "", createdAt, System.currentTimeMillis());
    }

    public User(String id, String name, String email, String phone, String college, String branch, Role role, String password, String profileImage, String profilePublicId, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.college = college != null ? college : "Campus";
        this.branch = branch != null ? branch : "General";
        this.role = role;
        this.password = password != null ? password : "123456";
        this.profileImage = profileImage != null ? profileImage : "";
        this.profilePublicId = profilePublicId != null ? profilePublicId : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getUid() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCollege() { return college; }
    public String getBranch() { return branch; }
    public Role getRole() { return role; }
    public String getPassword() { return password; }
    public String getProfileImage() { return profileImage; }
    public String getAvatarUrl() { return profileImage; }
    public String getProfilePublicId() { return profilePublicId; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setUid(String uid) { this.id = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCollege(String college) { this.college = college; }
    public void setBranch(String branch) { this.branch = branch; }
    public void setRole(Role role) { this.role = role; }
    public void setPassword(String password) { this.password = password; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setAvatarUrl(String avatarUrl) { this.profileImage = avatarUrl; }
    public void setProfilePublicId(String profilePublicId) { this.profilePublicId = profilePublicId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
