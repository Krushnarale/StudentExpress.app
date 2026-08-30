package com.core2web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

public class RoomItem {
    private String id; // roomId
    private String ownerUid;
    private String ownerName;
    private String title;
    private String location;
    private String price;
    private String distance;
    private String occupants;
    private String category; // type
    private String[] tags;
    private String description;
    private String ownerPhone;
    private String imagePath; // imageUrl
    private String imagePublicId;
    private List<String> images = new ArrayList<>();
    private boolean available = true;
    private long createdAt;
    private long updatedAt;

    // Updated UI rental terms
    private String rentType;
    private int minDuration;
    private Integer maxDuration;
    private double securityDeposit;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    private String availabilityStatus;

    public RoomItem(String id, String title, String location, String price, String distance, String occupants, String type, String[] tags, String description, String ownerName, String ownerPhone) {
        this(id, title, location, price, distance, occupants, type, tags, description, ownerName, ownerPhone, "assets/image/room_single.png", null);
    }

    public RoomItem(String id, String title, String location, String price, String distance, String occupants, String type, String[] tags, String description, String ownerName, String ownerPhone, String imagePath) {
        this(id, title, location, price, distance, occupants, type, tags, description, ownerName, ownerPhone, imagePath, null);
    }

    public RoomItem(String id, String title, String location, String price, String distance, String occupants, String type, String[] tags, String description, String ownerName, String ownerPhone, String imagePath, String ownerUid) {
        this(id, title, location, price, distance, occupants, type, tags, description, ownerName, ownerPhone, imagePath, "", ownerUid, true, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public RoomItem(String id, String title, String location, String price, String distance, String occupants, String category, String[] tags, String description, String ownerName, String ownerPhone, String imagePath, String imagePublicId, String ownerUid, boolean available, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.price = price;
        this.distance = distance;
        this.occupants = occupants;
        this.category = category != null ? category : "Rooms & PGs";
        this.tags = tags != null ? tags : new String[0];
        this.description = description;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.imagePath = imagePath != null ? imagePath : "assets/image/room_single.png";
        this.imagePublicId = imagePublicId != null ? imagePublicId : "";
        this.ownerUid = ownerUid;
        this.available = available;
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : System.currentTimeMillis();
        this.rentType = "Monthly";
        this.minDuration = 1;
        this.maxDuration = null;
        this.securityDeposit = 0.0;
        this.availableFrom = LocalDate.now();
        this.availableUntil = null;
        this.availabilityStatus = available ? "AVAILABLE" : "CURRENTLY_RENTED";
    }

    public RoomItem(String id, String title, String location, String price, String distance, String occupants, String category, String[] tags, String description, String ownerName, String ownerPhone, String imagePath, String rentType, int minDuration, Integer maxDuration, double securityDeposit, LocalDate availableFrom, LocalDate availableUntil, String availabilityStatus) {
        this(id, title, location, price, distance, occupants, category, tags, description, ownerName, ownerPhone, imagePath, "", null, true, System.currentTimeMillis(), System.currentTimeMillis());
        this.rentType = rentType != null ? rentType : "Monthly";
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.securityDeposit = securityDeposit;
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : "AVAILABLE";
    }

    public String getId() { return id; }
    public String getRoomId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public String getDistance() { return distance; }
    public String getOccupants() { return occupants; }
    public String getType() { return category; }
    public String getCategory() { return category; }
    public String[] getTags() { return tags; }
    public List<String> getTagsList() { return tags != null ? Arrays.asList(tags) : new ArrayList<>(); }
    public String getDescription() { return description; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public String getImagePath() { return imagePath; }
    public String getImageUrl() { return imagePath; }
    public String getImagePublicId() { return imagePublicId; }
    public String getOwnerUid() { return ownerUid; }
    public boolean isAvailable() { return available; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setRoomId(String roomId) { this.id = roomId; }
    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setPrice(String price) { this.price = price; }
    public void setDistance(String distance) { this.distance = distance; }
    public void setOccupants(String occupants) { this.occupants = occupants; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.category = type; }
    public void setTags(String[] tags) { this.tags = tags; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setImageUrl(String imageUrl) { this.imagePath = imageUrl; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public String getRentType() { return rentType; }
    public void setRentType(String rentType) { this.rentType = rentType; }
    public int getMinDuration() { return minDuration; }
    public void setMinDuration(int minDuration) { this.minDuration = minDuration; }
    public Integer getMaxDuration() { return maxDuration; }
    public void setMaxDuration(Integer maxDuration) { this.maxDuration = maxDuration; }
    public double getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(double securityDeposit) { this.securityDeposit = securityDeposit; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public LocalDate getAvailableUntil() { return availableUntil; }
    public void setAvailableUntil(LocalDate availableUntil) { this.availableUntil = availableUntil; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public double getNumericRentAmount() {
        try {
            if (price == null) return 0.0;
            String clean = price.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
        } catch (Exception e) { return 0.0; }
    }

    public List<String> getImages() {
        if (images == null) images = new ArrayList<>();
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    public void addImage(String img) {
        if (img != null && !img.trim().isEmpty()) {
            if (this.images == null) this.images = new ArrayList<>();
            this.images.add(img.trim());
        }
    }

}
