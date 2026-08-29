package com.core2web.model;

import com.core2web.util.TrackingUtil;

public class Order {
    private String id; // orderId / requestId
    private String buyerUid; // studentId
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String sellerUid; // sellerId
    private String sellerName;
    private String productId; // listingId
    private String productTitle; // itemName
    private String price;
    private String trackingId;
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED, PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String category;
    private String message;
    private String date;
    private long createdAt;
    private long updatedAt;

    public Order() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.trackingId = TrackingUtil.generateTrackingId();
        this.status = "PENDING";
    }

    public Order(String id, String itemName, String price, String date, String status, String trackingId, String category) {
        this(id, itemName, price, date, status, trackingId, category, null, null);
    }

    public Order(String id, String itemName, String price, String date, String status, String trackingId, String category, String buyerUid, String sellerUid) {
        this(id, itemName, price, date, status, trackingId, category, buyerUid, sellerUid, null, System.currentTimeMillis());
    }

    public Order(String id, String itemName, String price, String date, String status, String trackingId, String category, String buyerUid, String sellerUid, String productId, long createdAt) {
        this(id, buyerUid, "Student Buyer", "", "", sellerUid, "Student Seller", productId, itemName, price, trackingId, status, category, "", date, createdAt, createdAt);
    }

    public Order(
        String id,
        String buyerUid,
        String buyerName,
        String buyerEmail,
        String buyerPhone,
        String sellerUid,
        String sellerName,
        String productId,
        String productTitle,
        String price,
        String trackingId,
        String status,
        String category,
        String message,
        String date,
        long createdAt,
        long updatedAt
    ) {
        this.id = id;
        this.buyerUid = buyerUid != null ? buyerUid : "";
        this.buyerName = buyerName != null ? buyerName : "Student Buyer";
        this.buyerEmail = buyerEmail != null ? buyerEmail : "";
        this.buyerPhone = buyerPhone != null ? buyerPhone : "";
        this.sellerUid = sellerUid != null ? sellerUid : "";
        this.sellerName = sellerName != null ? sellerName : "Student Seller";
        this.productId = productId != null ? productId : "";
        this.productTitle = productTitle != null ? productTitle : "";
        this.price = price != null ? price : "";
        this.trackingId = (trackingId != null && !trackingId.trim().isEmpty()) ? trackingId : TrackingUtil.generateTrackingId();
        this.status = status != null ? status : "PENDING";
        this.category = category != null ? category : "Marketplace";
        this.message = message != null ? message : "";
        this.date = date != null ? date : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : this.createdAt;
    }

    public String getId() { return id; }
    public String getOrderId() { return id; }
    public String getRequestId() { return id; }
    public String getBuyerUid() { return buyerUid; }
    public String getBuyerId() { return buyerUid; }
    public String getStudentId() { return buyerUid; }
    public String getBuyerName() { return buyerName; }
    public String getStudentName() { return buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getStudentEmail() { return buyerEmail; }
    public String getBuyerPhone() { return buyerPhone; }
    public String getStudentPhone() { return buyerPhone; }
    public String getSellerUid() { return sellerUid; }
    public String getSellerId() { return sellerUid; }
    public String getSellerName() { return sellerName; }
    public String getProductId() { return productId; }
    public String getListingId() { return productId; }
    public String getProductTitle() { return productTitle; }
    public String getItemName() { return productTitle; }
    public String getPrice() { return price; }
    public String getTrackingId() { return trackingId; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setOrderId(String orderId) { this.id = orderId; }
    public void setRequestId(String requestId) { this.id = requestId; }
    public void setBuyerUid(String buyerUid) { this.buyerUid = buyerUid; }
    public void setBuyerId(String buyerId) { this.buyerUid = buyerId; }
    public void setStudentId(String studentId) { this.buyerUid = studentId; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public void setStudentName(String studentName) { this.buyerName = studentName; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public void setStudentEmail(String studentEmail) { this.buyerEmail = studentEmail; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }
    public void setStudentPhone(String studentPhone) { this.buyerPhone = studentPhone; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }
    public void setSellerId(String sellerId) { this.sellerUid = sellerId; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setListingId(String listingId) { this.productId = listingId; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public void setItemName(String itemName) { this.productTitle = itemName; }
    public void setPrice(String price) { this.price = price; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }
    public void setMessage(String message) { this.message = message; }
    public void setDate(String date) { this.date = date; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
