package com.core2web.model;

public class RoommateItem {
    private String id; // roommateId
    private String userUid;
    private String name;
    private String location;
    private String budget;
    private String preference;
    private String gender;
    private String bio;
    private String phone;
    private String imagePath; // imageUrl
    private String imagePublicId;
    private long createdAt;

    public RoommateItem(String id, String name, String location, String budget, String preference, String gender, String bio, String phone) {
        this(id, null, name, gender, location, budget, preference, bio, phone, "", "", System.currentTimeMillis());
    }

    public RoommateItem(String id, String userUid, String name, String gender, String location, String budget, String preference, String bio, String phone, String imagePath, String imagePublicId, long createdAt) {
        this.id = id;
        this.userUid = userUid != null ? userUid : id;
        this.name = name;
        this.gender = gender;
        this.location = location;
        this.budget = budget;
        this.preference = preference;
        this.bio = bio;
        this.phone = phone;
        this.imagePath = imagePath != null ? imagePath : "";
        this.imagePublicId = imagePublicId != null ? imagePublicId : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getRoommateId() { return id; }
    public String getUserUid() { return userUid; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getBudget() { return budget; }
    public String getPreference() { return preference; }
    public String getGender() { return gender; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getImagePath() { return imagePath; }
    public String getImageUrl() { return imagePath; }
    public String getImagePublicId() { return imagePublicId; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setRoommateId(String roommateId) { this.id = roommateId; }
    public void setUserUid(String userUid) { this.userUid = userUid; }
    public void setName(String name) { this.name = name; }
    public void setGender(String gender) { this.gender = gender; }
    public void setLocation(String location) { this.location = location; }
    public void setBudget(String budget) { this.budget = budget; }
    public void setPreference(String preference) { this.preference = preference; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setImageUrl(String imageUrl) { this.imagePath = imageUrl; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
