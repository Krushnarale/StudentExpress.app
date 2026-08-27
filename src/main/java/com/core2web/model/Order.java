package com.core2web.model;

import com.core2web.util.TrackingUtil;

public class Order {
    private String id; // orderId
    private String buyerUid;
    private String sellerUid;
    private String productId;
    private String productTitle; // itemName
    private String price;
    private String trackingId;
    private String status; // PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String category;
    private String date;
    private long createdAt;

    public Order() {
        this.createdAt = System.currentTimeMillis();
        this.trackingId = TrackingUtil.generateTrackingId();
        this.status = "PLACED";
    }

    public Order(String id, String itemName, String price, String date, String status, String trackingId, String category) {
        this(id, itemName, price, date, status, trackingId, category, null, null);
    }

    public Order(String id, String itemName, String price, String date, String status, String trackingId, String category, String buyerUid, String sellerUid) {
        this(id, itemName, price, date, status, trackingId, category, buyerUid, sellerUid, null, System.currentTimeMillis());
    }

    public Order(String id, String itemName, String price, String date, String status, String trackingId, String category, String buyerUid, String sellerUid, String productId, long createdAt) {
        this.id = id;
        this.productTitle = itemName != null ? itemName : "";
        this.price = price != null ? price : "";
        this.date = date != null ? date : "";
        this.status = status != null ? status : "PLACED";
        this.trackingId = (trackingId != null && !trackingId.trim().isEmpty()) ? trackingId : TrackingUtil.generateTrackingId();
        this.category = category != null ? category : "Marketplace";
        this.buyerUid = buyerUid != null ? buyerUid : "";
        this.sellerUid = sellerUid != null ? sellerUid : "";
        this.productId = productId != null ? productId : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getOrderId() { return id; }
    public String getBuyerUid() { return buyerUid; }
    public String getBuyerId() { return buyerUid; }
    public String getSellerUid() { return sellerUid; }
    public String getSellerId() { return sellerUid; }
    public String getProductId() { return productId; }
    public String getProductTitle() { return productTitle; }
    public String getItemName() { return productTitle; }
    public String getPrice() { return price; }
    public String getTrackingId() { return trackingId; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setOrderId(String orderId) { this.id = orderId; }
    public void setBuyerUid(String buyerUid) { this.buyerUid = buyerUid; }
    public void setBuyerId(String buyerId) { this.buyerUid = buyerId; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }
    public void setSellerId(String sellerId) { this.sellerUid = sellerId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public void setItemName(String itemName) { this.productTitle = itemName; }
    public void setPrice(String price) { this.price = price; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(String date) { this.date = date; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
