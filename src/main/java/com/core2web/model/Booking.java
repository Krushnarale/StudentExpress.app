package com.core2web.model;

public class Booking {
    private String id; // bookingId
    private String userUid;
    private String ownerUid;
    private String providerUid;
    private String itemId;
    private String bookingType; // "ROOM" or "SERVICE"
    private String bookingDate; // date
    private String timeSlot;
    private String address;
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED
    private String itemOrServiceName;
    private String category;
    private String userEmail;
    private long createdAt;

    public Booking() {
        this.createdAt = System.currentTimeMillis();
    }

    public Booking(String id, String itemOrServiceName, String category, String date, String status, String userEmail) {
        this(id, itemOrServiceName, category, date, status, userEmail, null, null);
    }

    public Booking(String id, String itemOrServiceName, String category, String date, String status, String userEmail, String userUid, String ownerUid) {
        this.id = id;
        this.itemOrServiceName = itemOrServiceName;
        this.category = category;
        this.bookingDate = date;
        this.status = status;
        this.userEmail = userEmail;
        this.userUid = userUid;
        this.ownerUid = ownerUid;
        this.providerUid = ownerUid;
        this.itemId = "";
        this.timeSlot = "10:00 AM - 12:00 PM";
        this.address = "Campus Area";
        this.bookingType = "Service".equalsIgnoreCase(category) ? "SERVICE" : "ROOM";
        this.createdAt = System.currentTimeMillis();
    }

    public Booking(String id, String userUid, String ownerUid, String itemId, String itemOrServiceName, String date, String status, String bookingType, long createdAt) {
        this.id = id;
        this.userUid = userUid;
        this.ownerUid = ownerUid;
        this.providerUid = ownerUid;
        this.itemId = itemId != null ? itemId : "";
        this.itemOrServiceName = itemOrServiceName != null ? itemOrServiceName : "Booking";
        this.bookingDate = date != null ? date : "";
        this.status = status != null ? status : "PENDING";
        this.bookingType = bookingType != null ? bookingType : "ROOM";
        this.category = "SERVICE".equalsIgnoreCase(bookingType) ? "Service" : "Room";
        this.timeSlot = "10:00 AM - 12:00 PM";
        this.address = "Campus Area";
        this.userEmail = "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public Booking(String id, String userUid, String ownerUid, String providerUid, String itemId, String bookingType, String bookingDate, String timeSlot, String address, String status, String itemOrServiceName, String category, String userEmail, long createdAt) {
        this.id = id;
        this.userUid = userUid;
        this.ownerUid = ownerUid;
        this.providerUid = providerUid != null ? providerUid : ownerUid;
        this.itemId = itemId != null ? itemId : "";
        this.bookingType = bookingType != null ? bookingType : "ROOM";
        this.bookingDate = bookingDate != null ? bookingDate : "";
        this.timeSlot = timeSlot != null ? timeSlot : "";
        this.address = address != null ? address : "";
        this.status = status != null ? status : "PENDING";
        this.itemOrServiceName = itemOrServiceName != null ? itemOrServiceName : "Booking";
        this.category = category != null ? category : "General";
        this.userEmail = userEmail != null ? userEmail : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getBookingId() { return id; }
    public String getUserUid() { return userUid; }
    public String getUserId() { return userUid; }
    public String getOwnerUid() { return ownerUid; }
    public String getOwnerId() { return ownerUid; }
    public String getProviderUid() { return providerUid != null ? providerUid : ownerUid; }
    public String getProviderId() { return getProviderUid(); }
    public String getItemId() { return itemId != null ? itemId : ""; }
    public String getRoomId() { return itemId; }
    public String getServiceId() { return itemId; }
    public String getBookingType() { return bookingType; }
    public String getDate() { return bookingDate; }
    public String getBookingDate() { return bookingDate; }
    public String getTimeSlot() { return timeSlot; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public String getItemOrServiceName() { return itemOrServiceName; }
    public String getRoomTitle() { return itemOrServiceName; }
    public String getServiceTitle() { return itemOrServiceName; }
    public String getCategory() { return category; }
    public String getUserEmail() { return userEmail; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setBookingId(String bookingId) { this.id = bookingId; }
    public void setUserUid(String userUid) { this.userUid = userUid; }
    public void setUserId(String userUid) { this.userUid = userUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
    public void setOwnerId(String ownerId) { this.ownerUid = ownerId; }
    public void setProviderUid(String providerUid) { this.providerUid = providerUid; }
    public void setProviderId(String providerId) { this.providerUid = providerId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setRoomId(String roomId) { this.itemId = roomId; }
    public void setServiceId(String serviceId) { this.itemId = serviceId; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    public void setDate(String date) { this.bookingDate = date; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public void setAddress(String address) { this.address = address; }
    public void setStatus(String status) { this.status = status; }
    public void setItemOrServiceName(String itemOrServiceName) { this.itemOrServiceName = itemOrServiceName; }
    public void setCategory(String category) { this.category = category; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
