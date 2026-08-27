package com.core2web.model;

public class ProductItem {
    private String id; // productId
    private String sellerUid;
    private String sellerName;
    private String title;
    private String price;
    private String location;
    private String timePosted;
    private String category;
    private String condition;
    private String description;
    private String sellerPhone;
    private String imagePath; // imageUrl
    private String imagePublicId;
    private boolean available = true;
    private long createdAt;
    private long updatedAt;

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, "assets/image/laptop_dell.png", null);
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone, String imagePath) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, imagePath, null);
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone, String imagePath, String sellerUid) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, imagePath, "", sellerUid, true, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone, String imagePath, String imagePublicId, String sellerUid, boolean available, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.location = location;
        this.timePosted = timePosted != null ? timePosted : "Recently";
        this.category = category;
        this.condition = condition;
        this.description = description;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.imagePath = imagePath != null ? imagePath : "assets/image/laptop_dell.png";
        this.imagePublicId = imagePublicId != null ? imagePublicId : "";
        this.sellerUid = sellerUid;
        this.available = available;
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getProductId() { return id; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getLocation() { return location; }
    public String getTimePosted() { return timePosted; }
    public String getCategory() { return category; }
    public String getCondition() { return condition; }
    public String getDescription() { return description; }
    public String getSellerName() { return sellerName; }
    public String getSellerPhone() { return sellerPhone; }
    public String getImagePath() { return imagePath; }
    public String getImageUrl() { return imagePath; }
    public String getImagePublicId() { return imagePublicId; }
    public String getSellerUid() { return sellerUid; }
    public boolean isAvailable() { return available; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setProductId(String productId) { this.id = productId; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(String price) { this.price = price; }
    public void setLocation(String location) { this.location = location; }
    public void setTimePosted(String timePosted) { this.timePosted = timePosted; }
    public void setCategory(String category) { this.category = category; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setDescription(String description) { this.description = description; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setImageUrl(String imageUrl) { this.imagePath = imageUrl; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
