package com.core2web.model;

public class SellerProfile {
    private String sellerId; // authenticated Firebase UID
    private String name;
    private String email;
    private String phone;
    private String college;
    private String location;
    private String description;
    private String profileImage;
    private String profilePublicId;
    private String status = "ACTIVE";
    private long createdAt;
    private long updatedAt;

    public SellerProfile() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public SellerProfile(String sellerId, String name, String email, String phone, String college, String location, String description, String profileImage, String profilePublicId, String status, long createdAt, long updatedAt) {
        this.sellerId = sellerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.college = college != null ? college : "";
        this.location = location != null ? location : "";
        this.description = description != null ? description : "";
        this.profileImage = profileImage != null ? profileImage : "";
        this.profilePublicId = profilePublicId != null ? profilePublicId : "";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : this.createdAt;
    }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getUserUid() { return sellerId; }
    public void setUserUid(String userUid) { this.sellerId = userUid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getProfilePublicId() { return profilePublicId; }
    public void setProfilePublicId(String profilePublicId) { this.profilePublicId = profilePublicId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
