package com.core2web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private boolean available = true;
    private long createdAt;
    private long updatedAt;

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
}
