package com.core2web.model;

import java.util.ArrayList;
import java.util.List;

public class SavedItem {
    private String userUid;
    private List<String> savedRoomIds;
    private List<String> savedProductIds;
    private long updatedAt;

    public SavedItem() {
        this.savedRoomIds = new ArrayList<>();
        this.savedProductIds = new ArrayList<>();
        this.updatedAt = System.currentTimeMillis();
    }

    public SavedItem(String userUid, List<String> savedRoomIds, List<String> savedProductIds, long updatedAt) {
        this.userUid = userUid;
        this.savedRoomIds = savedRoomIds != null ? savedRoomIds : new ArrayList<>();
        this.savedProductIds = savedProductIds != null ? savedProductIds : new ArrayList<>();
        this.updatedAt = updatedAt > 0 ? updatedAt : System.currentTimeMillis();
    }

    public String getUserUid() { return userUid; }
    public List<String> getSavedRoomIds() { return savedRoomIds; }
    public List<String> getSavedProductIds() { return savedProductIds; }
    public long getUpdatedAt() { return updatedAt; }

    public void setUserUid(String userUid) { this.userUid = userUid; }
    public void setSavedRoomIds(List<String> savedRoomIds) { this.savedRoomIds = savedRoomIds; }
    public void setSavedProductIds(List<String> savedProductIds) { this.savedProductIds = savedProductIds; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
