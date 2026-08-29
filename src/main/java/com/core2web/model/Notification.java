package com.core2web.model;

public class Notification {
    private String notificationId;
    private String uid;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private long createdAt;

    public Notification() {
    }

    public Notification(String notificationId, String uid, String title, String message, String type) {
        this(notificationId, uid, title, message, type, false, System.currentTimeMillis());
    }

    public Notification(String notificationId, String uid, String title, String message, String type, boolean isRead, long createdAt) {
        this.notificationId = notificationId;
        this.uid = uid;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
