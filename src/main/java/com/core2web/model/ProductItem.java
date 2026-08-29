package com.core2web.model;

public class ProductItem {
    private String id; // productId / listingId
    private String sellerUid; // sellerId (Firebase UID)
    private String sellerName;
    private String sellerPhone;
    private String title; // itemName
    private String price;
    private String location;
    private String timePosted;
    private String category;
    private String condition;
    private String description;
    private String contactPreference;
    private String imagePath; // imageUrl
    private String imagePublicId;
    private String status = "ACTIVE"; // ACTIVE, SOLD, INACTIVE
    private boolean available = true;
    private long createdAt;
    private long updatedAt;

    public ProductItem() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "ACTIVE";
        this.available = true;
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, "assets/image/laptop_dell.png", null);
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone, String imagePath) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, imagePath, null);
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone, String imagePath, String sellerUid) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, imagePath, "", sellerUid, "ACTIVE", true, "Phone / Chat", System.currentTimeMillis(), System.currentTimeMillis());
    }

    public ProductItem(String id, String title, String price, String location, String timePosted, String category, String condition, String description, String sellerName, String sellerPhone, String imagePath, String imagePublicId, String sellerUid, boolean available, long createdAt, long updatedAt) {
        this(id, title, price, location, timePosted, category, condition, description, sellerName, sellerPhone, imagePath, imagePublicId, sellerUid, available ? "ACTIVE" : "SOLD", available, "Phone / Chat", createdAt, updatedAt);
    }

    public ProductItem(
        String id,
        String title,
        String price,
        String location,
        String timePosted,
        String category,
        String condition,
        String description,
        String sellerName,
        String sellerPhone,
        String imagePath,
        String imagePublicId,
        String sellerUid,
        String status,
        boolean available,
        String contactPreference,
        long createdAt,
        long updatedAt
    ) {
        this.id = id;
        this.title = title != null ? title : "";
        this.price = price != null ? price : "₹ 0";
        this.location = location != null ? location : "Pune";
        this.timePosted = timePosted != null ? timePosted : "Recently";
        this.category = category != null ? category : "General";
        this.condition = condition != null ? condition : "Used";
        this.description = description != null ? description : "";
        this.sellerName = sellerName != null ? sellerName : "Student Seller";
        this.sellerPhone = sellerPhone != null ? sellerPhone : "";
        this.imagePath = (imagePath != null && !imagePath.isEmpty()) ? imagePath : "assets/image/laptop_dell.png";
        this.imagePublicId = imagePublicId != null ? imagePublicId : "";
        this.sellerUid = sellerUid != null ? sellerUid : "";
        this.status = status != null ? status : "ACTIVE";
        this.available = !"SOLD".equalsIgnoreCase(this.status) && !"INACTIVE".equalsIgnoreCase(this.status) && available;
        this.contactPreference = contactPreference != null ? contactPreference : "Phone / Chat";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : this.createdAt;
    }

    public String getId() { return id; }
    public String getProductId() { return id; }
    public String getListingId() { return id; }
    public String getTitle() { return title; }
    public String getItemName() { return title; }
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
    public String getSellerId() { return sellerUid; }
    public String getStatus() { return status; }
    public boolean isAvailable() { return available && !"SOLD".equalsIgnoreCase(status); }
    public String getContactPreference() { return contactPreference; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setProductId(String productId) { this.id = productId; }
    public void setListingId(String listingId) { this.id = listingId; }
    public void setTitle(String title) { this.title = title; }
    public void setItemName(String itemName) { this.title = itemName; }
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
    public void setSellerId(String sellerId) { this.sellerUid = sellerId; }
    public void setStatus(String status) {
        this.status = status;
        this.available = !"SOLD".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status);
    }
    public void setAvailable(boolean available) {
        this.available = available;
        if (!available && "ACTIVE".equalsIgnoreCase(this.status)) {
            this.status = "SOLD";
        }
    }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
