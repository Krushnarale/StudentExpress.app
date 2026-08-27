package com.core2web.model;

public class Notification {
    private String notificationId;
    private String uid;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private long createdAt;

    public Notification() {}

    public Notification(String notificationId, String uid, String title, String message, String type, boolean isRead, long createdAt) {
        this.notificationId = notificationId;
        this.uid = uid;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getNotificationId() { return notificationId; }
    public String getId() { return notificationId; }
    public String getUid() { return uid; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public long getCreatedAt() { return createdAt; }

    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setUid(String uid) { this.uid = uid; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setRead(boolean read) { isRead = read; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
