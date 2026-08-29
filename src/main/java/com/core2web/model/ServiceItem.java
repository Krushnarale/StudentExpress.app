package com.core2web.model;

public class ServiceItem {
    private String id; // serviceId
    private String icon;
    private String title;
    private String category;
    private String subtitle;
    private String price;
    private String providerName;
    private String providerPhone;
    private String description;
    private String providerUid;
    private String imagePath; // imageUrl
    private String imagePublicId;
    private long createdAt;

    public ServiceItem(String id, String icon, String title, String category, String subtitle, String price, String providerName, String providerPhone, String description) {
        this(id, icon, title, category, subtitle, price, providerName, providerPhone, description, null);
    }

    public ServiceItem(String id, String icon, String title, String category, String subtitle, String price, String providerName, String providerPhone, String description, String providerUid) {
        this(id, icon, title, category, subtitle, price, providerName, providerPhone, description, providerUid, icon, "", System.currentTimeMillis());
    }

    public ServiceItem(String id, String icon, String title, String category, String subtitle, String price, String providerName, String providerPhone, String description, String providerUid, String imagePath, String imagePublicId, long createdAt) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.category = category;
        this.subtitle = subtitle;
        this.price = price;
        this.providerName = providerName;
        this.providerPhone = providerPhone;
        this.description = description;
        this.providerUid = providerUid;
        this.imagePath = imagePath != null ? imagePath : icon;
        this.imagePublicId = imagePublicId != null ? imagePublicId : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getServiceId() { return id; }
    public String getIcon() { return icon != null ? icon : imagePath; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getSubtitle() { return subtitle; }
    public String getPrice() { return price; }
    public String getProviderName() { return providerName; }
    public String getProviderPhone() { return providerPhone; }
    public String getDescription() { return description; }
    public String getProviderUid() { return providerUid; }
    public String getImagePath() { return imagePath; }
    public String getImageUrl() { return imagePath; }
    public String getImagePublicId() { return imagePublicId; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setServiceId(String serviceId) { this.id = serviceId; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setPrice(String price) { this.price = price; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public void setDescription(String description) { this.description = description; }
    public void setProviderUid(String providerUid) { this.providerUid = providerUid; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setImageUrl(String imageUrl) { this.imagePath = imageUrl; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
