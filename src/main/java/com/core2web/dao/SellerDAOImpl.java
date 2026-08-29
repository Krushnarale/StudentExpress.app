package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.SellerProfile;
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

public class SellerDAOImpl implements SellerDAO {

    private static final String COLLECTION_NAME = "sellers";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<SellerProfile> findBySellerId(String sellerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        String clean = sellerId.trim();
        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(clean).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseSellerFromSnapshot(snapshot));
            }

            // Fallback query
            ApiFuture<QuerySnapshot> q = db.collection(COLLECTION_NAME).whereEqualTo("userUid", clean).get();
            List<QueryDocumentSnapshot> docs = q.get().getDocuments();
            if (!docs.isEmpty()) {
                return Optional.of(parseSellerFromSnapshot(docs.get(0)));
            }
        } catch (Throwable e) {
            System.err.println("[SellerDAOImpl] Error finding seller profile: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<SellerProfile> findAll() {
        List<SellerProfile> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                list.add(parseSellerFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[SellerDAOImpl] Error fetching all seller profiles: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(SellerProfile profile) {
        if (profile == null || profile.getSellerId() == null || profile.getSellerId().trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("sellerId", profile.getSellerId().trim());
            docData.put("userUid", profile.getSellerId().trim());
            docData.put("name", profile.getName() != null ? profile.getName() : "");
            docData.put("email", profile.getEmail() != null ? profile.getEmail() : "");
            docData.put("phone", profile.getPhone() != null ? profile.getPhone() : "");
            docData.put("college", profile.getCollege() != null ? profile.getCollege() : "");
            docData.put("location", profile.getLocation() != null ? profile.getLocation() : "");
            docData.put("description", profile.getDescription() != null ? profile.getDescription() : "");
            docData.put("profileImage", profile.getProfileImage() != null ? profile.getProfileImage() : "");
            docData.put("profilePublicId", profile.getProfilePublicId() != null ? profile.getProfilePublicId() : "");
            docData.put("status", profile.getStatus() != null ? profile.getStatus() : "ACTIVE");
            docData.put("createdAt", profile.getCreatedAt() > 0 ? profile.getCreatedAt() : System.currentTimeMillis());
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(profile.getSellerId().trim()).set(docData);
            result.get();
            System.out.println("[SellerDAOImpl] Seller profile saved to Firestore: " + profile.getSellerId());
            return true;
        } catch (Throwable e) {
            System.err.println("[SellerDAOImpl] Error saving seller profile: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String sellerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(sellerId.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                String publicId = snapshot.getString("profilePublicId");
                if (publicId != null && !publicId.trim().isEmpty()) {
                    CloudinaryService.deleteImage(publicId.trim());
                }
            }
            db.collection(COLLECTION_NAME).document(sellerId.trim()).delete().get();
            System.out.println("[SellerDAOImpl] Seller profile deleted: " + sellerId);
            return true;
        } catch (Throwable e) {
            System.err.println("[SellerDAOImpl] Error deleting seller profile: " + e.getMessage());
            return false;
        }
    }

    private SellerProfile parseSellerFromSnapshot(DocumentSnapshot doc) {
        String sellerId = doc.getString("sellerId");
        if (sellerId == null) sellerId = doc.getString("userUid");
        if (sellerId == null) sellerId = doc.getId();

        String name = doc.getString("name");
        String email = doc.getString("email");
        String phone = doc.getString("phone");
        String college = doc.getString("college");
        String location = doc.getString("location");
        String description = doc.getString("description");
        String profileImage = doc.getString("profileImage");
        String profilePublicId = doc.getString("profilePublicId");
        String status = doc.getString("status");
        Long createdAt = doc.getLong("createdAt");
        Long updatedAt = doc.getLong("updatedAt");

        return new SellerProfile(
            sellerId,
            name != null ? name : "Student Seller",
            email != null ? email : "",
            phone != null ? phone : "",
            college != null ? college : "",
            location != null ? location : "",
            description != null ? description : "",
            profileImage != null ? profileImage : "",
            profilePublicId != null ? profilePublicId : "",
            status != null ? status : "ACTIVE",
            createdAt != null ? createdAt : System.currentTimeMillis(),
            updatedAt != null ? updatedAt : System.currentTimeMillis()
        );
    }
}
