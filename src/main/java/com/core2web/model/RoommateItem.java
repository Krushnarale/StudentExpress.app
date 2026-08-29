package com.core2web.model;

public class RoommateItem {
    private String id; // roommateProfileId / roommateId
    private String userUid; // studentId (Firebase UID)
    private String name;
    private String email;
    private String phone;
    private String gender;
    private int age;
    private String college;
    private String course; // department / branch
    private String year; // year of study (e.g., "3rd Year")
    private String location; // preferred location
    private String budget; // budget range
    private String accommodationType; // preferred room/flat type
    private String roommatesNeeded; // occupancy preference
    private String preference; // lifestyle / habits tags
    private String bio; // about me
    private String imagePath; // imageUrl / profilePhoto
    private String imagePublicId;
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
    private long createdAt;
    private long updatedAt;

    public RoommateItem() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

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
        this.updatedAt = this.createdAt;
        this.status = "ACTIVE";
    }

    public RoommateItem(
        String id,
        String userUid,
        String name,
        String email,
        String phone,
        String gender,
        int age,
        String college,
        String course,
        String year,
        String location,
        String budget,
        String accommodationType,
        String roommatesNeeded,
        String preference,
        String bio,
        String imagePath,
        String imagePublicId,
        String status,
        long createdAt,
        long updatedAt
    ) {
        this.id = id;
        this.userUid = userUid != null ? userUid : id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.age = age;
        this.college = college;
        this.course = course;
        this.year = year;
        this.location = location;
        this.budget = budget;
        this.accommodationType = accommodationType;
        this.roommatesNeeded = roommatesNeeded;
        this.preference = preference;
        this.bio = bio;
        this.imagePath = imagePath != null ? imagePath : "";
        this.imagePublicId = imagePublicId != null ? imagePublicId : "";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : this.createdAt;
    }

    public String getId() { return id; }
    public String getRoommateId() { return id; }
    public String getRoommateProfileId() { return id; }
    public String getUserUid() { return userUid; }
    public String getStudentId() { return userUid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getCollege() { return college; }
    public String getCourse() { return course; }
    public String getDepartment() { return course; }
    public String getYear() { return year; }
    public String getLocation() { return location; }
    public String getPreferredLocation() { return location; }
    public String getBudget() { return budget; }
    public String getAccommodationType() { return accommodationType; }
    public String getRoommatesNeeded() { return roommatesNeeded; }
    public String getPreference() { return preference; }
    public String getLifestyle() { return preference; }
    public String getBio() { return bio; }
    public String getAboutMe() { return bio; }
    public String getImagePath() { return imagePath; }
    public String getImageUrl() { return imagePath; }
    public String getProfilePhoto() { return imagePath; }
    public String getImagePublicId() { return imagePublicId; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setRoommateId(String roommateId) { this.id = roommateId; }
    public void setRoommateProfileId(String roommateProfileId) { this.id = roommateProfileId; }
    public void setUserUid(String userUid) { this.userUid = userUid; }
    public void setStudentId(String studentId) { this.userUid = studentId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAge(int age) { this.age = age; }
    public void setCollege(String college) { this.college = college; }
    public void setCourse(String course) { this.course = course; }
    public void setDepartment(String department) { this.course = department; }
    public void setYear(String year) { this.year = year; }
    public void setLocation(String location) { this.location = location; }
    public void setPreferredLocation(String preferredLocation) { this.location = preferredLocation; }
    public void setBudget(String budget) { this.budget = budget; }
    public void setAccommodationType(String accommodationType) { this.accommodationType = accommodationType; }
    public void setRoommatesNeeded(String roommatesNeeded) { this.roommatesNeeded = roommatesNeeded; }
    public void setPreference(String preference) { this.preference = preference; }
    public void setLifestyle(String lifestyle) { this.preference = lifestyle; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAboutMe(String aboutMe) { this.bio = aboutMe; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setImageUrl(String imageUrl) { this.imagePath = imageUrl; }
    public void setProfilePhoto(String profilePhoto) { this.imagePath = profilePhoto; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
