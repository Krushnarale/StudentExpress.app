package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.ProductItem;
import com.core2web.service.CloudinaryService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    private static final String COLLECTION_NAME = "products";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<ProductItem> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseProductFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[ProductDAOImpl] Error finding product by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<ProductItem> findAll() {
        List<ProductItem> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseProductFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[ProductDAOImpl] Error fetching all products: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ProductItem> findBySellerUid(String sellerUid) {
        List<ProductItem> list = new ArrayList<>();
        if (sellerUid == null || sellerUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        String cleanUid = sellerUid.trim();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("sellerUid", cleanUid).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseProductFromSnapshot(doc));
            }
            if (list.isEmpty()) {
                ApiFuture<QuerySnapshot> altFuture = db.collection(COLLECTION_NAME).whereEqualTo("sellerId", cleanUid).get();
                for (DocumentSnapshot doc : altFuture.get().getDocuments()) {
                    list.add(parseProductFromSnapshot(doc));
                }
            }
        } catch (Throwable e) {
            System.err.println("[ProductDAOImpl] Error fetching products by seller UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(ProductItem product) {
        if (product == null || product.getId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("productId", product.getId());
            docData.put("listingId", product.getId());
            docData.put("id", product.getId());
            docData.put("sellerUid", product.getSellerUid() != null ? product.getSellerUid() : "");
            docData.put("sellerId", product.getSellerUid() != null ? product.getSellerUid() : "");
            docData.put("sellerName", product.getSellerName() != null ? product.getSellerName() : "Student Seller");
            docData.put("sellerPhone", product.getSellerPhone() != null ? product.getSellerPhone() : "");
            docData.put("title", product.getTitle() != null ? product.getTitle() : "");
            docData.put("itemName", product.getTitle() != null ? product.getTitle() : "");
            docData.put("price", product.getPrice() != null ? product.getPrice() : "₹ 0");
            docData.put("location", product.getLocation() != null ? product.getLocation() : "Pune");
            docData.put("timePosted", product.getTimePosted() != null ? product.getTimePosted() : "Recently");
            docData.put("category", product.getCategory() != null ? product.getCategory() : "General");
            docData.put("condition", product.getCondition() != null ? product.getCondition() : "Used");
            docData.put("description", product.getDescription() != null ? product.getDescription() : "");
            docData.put("contactPreference", product.getContactPreference() != null ? product.getContactPreference() : "Phone / Chat");
            docData.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");
            docData.put("imagePath", product.getImageUrl() != null ? product.getImageUrl() : "");
            docData.put("imagePublicId", product.getImagePublicId() != null ? product.getImagePublicId() : "");
            docData.put("status", product.getStatus() != null ? product.getStatus() : "ACTIVE");
            docData.put("available", product.isAvailable());
            docData.put("createdAt", product.getCreatedAt() > 0 ? product.getCreatedAt() : System.currentTimeMillis());
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(product.getId().trim()).set(docData);
            result.get();
            System.out.println("[ProductDAOImpl] Product saved successfully to Firestore: " + product.getId() + " (sellerId: " + product.getSellerUid() + ", category: " + product.getCategory() + ")");
            return true;
        } catch (Throwable e) {
            System.err.println("[ProductDAOImpl] Error saving product to Firestore: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(String id, String status) {
        if (id == null || id.trim().isEmpty() || status == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            String cleanStatus = status.trim().toUpperCase();
            Map<String, Object> update = new HashMap<>();
            update.put("status", cleanStatus);
            update.put("available", !"SOLD".equalsIgnoreCase(cleanStatus) && !"INACTIVE".equalsIgnoreCase(cleanStatus));
            update.put("updatedAt", System.currentTimeMillis());

            db.collection(COLLECTION_NAME).document(id.trim()).update(update).get();
            System.out.println("[ProductDAOImpl] Product status updated: " + id + " -> " + cleanStatus);
            return true;
        } catch (Throwable e) {
            System.err.println("[ProductDAOImpl] Error updating product status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                String publicId = snapshot.getString("imagePublicId");
                if (publicId != null && !publicId.trim().isEmpty()) {
                    CloudinaryService.deleteImage(publicId.trim());
                }
            }
            db.collection(COLLECTION_NAME).document(id.trim()).delete().get();
            System.out.println("[ProductDAOImpl] Product deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[ProductDAOImpl] Error deleting product: " + e.getMessage());
            return false;
        }
    }

    private ProductItem parseProductFromSnapshot(DocumentSnapshot doc) {
        String id = doc.getString("productId");
        if (id == null) id = doc.getString("listingId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String title = doc.getString("title");
        if (title == null) title = doc.getString("itemName");
        String price = doc.getString("price");
        String location = doc.getString("location");
        String timePosted = doc.getString("timePosted");
        String category = doc.getString("category");
        String condition = doc.getString("condition");
        String description = doc.getString("description");
        String contactPref = doc.getString("contactPreference");
        String sellerName = doc.getString("sellerName");
        String sellerPhone = doc.getString("sellerPhone");
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl == null) imageUrl = doc.getString("imagePath");
        String imagePublicId = doc.getString("imagePublicId");
        
        String sellerUid = doc.getString("sellerUid");
        if (sellerUid == null) sellerUid = doc.getString("sellerId");

        String status = doc.getString("status");
        if (status == null) {
            Boolean avail = doc.getBoolean("available");
            status = (avail != null && !avail) ? "SOLD" : "ACTIVE";
        }
        Boolean available = doc.getBoolean("available");
        if (available == null) {
            available = !"SOLD".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status);
        }

        Long createdAt = doc.getLong("createdAt");
        Long updatedAt = doc.getLong("updatedAt");

        return new ProductItem(
            id,
            title != null ? title : "Product",
            price != null ? price : "₹ 0",
            location != null ? location : "Pune",
            timePosted != null ? timePosted : "Recently",
            category != null ? category : "General",
            condition != null ? condition : "Used",
            description != null ? description : "",
            sellerName != null ? sellerName : "Student Seller",
            sellerPhone != null ? sellerPhone : "",
            imageUrl != null ? imageUrl : "",
            imagePublicId != null ? imagePublicId : "",
            sellerUid != null ? sellerUid : "",
            status,
            available,
            contactPref != null ? contactPref : "Phone / Chat",
            createdAt != null ? createdAt : System.currentTimeMillis(),
            updatedAt != null ? updatedAt : System.currentTimeMillis()
        );
    }
}
