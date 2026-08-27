package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.RoommateItem;
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

public class RoommateDAOImpl implements RoommateDAO {

    private static final String COLLECTION_NAME = "roommates";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<RoommateItem> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseRoommateFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[RoommateDAOImpl] Error finding roommate by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<RoommateItem> findAll() {
        List<RoommateItem> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseRoommateFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[RoommateDAOImpl] Error fetching all roommates: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(RoommateItem roommate) {
        if (roommate == null || roommate.getId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("roommateId", roommate.getId());
            docData.put("id", roommate.getId());
            docData.put("userUid", roommate.getUserUid() != null ? roommate.getUserUid() : roommate.getId());
            docData.put("name", roommate.getName() != null ? roommate.getName() : "");
            docData.put("gender", roommate.getGender() != null ? roommate.getGender() : "");
            docData.put("location", roommate.getLocation() != null ? roommate.getLocation() : "");
            docData.put("budget", roommate.getBudget() != null ? roommate.getBudget() : "");
            docData.put("preference", roommate.getPreference() != null ? roommate.getPreference() : "");
            docData.put("bio", roommate.getBio() != null ? roommate.getBio() : "");
            docData.put("phone", roommate.getPhone() != null ? roommate.getPhone() : "");
            docData.put("imageUrl", roommate.getImageUrl() != null ? roommate.getImageUrl() : "");
            docData.put("imagePath", roommate.getImageUrl() != null ? roommate.getImageUrl() : "");
            docData.put("imagePublicId", roommate.getImagePublicId() != null ? roommate.getImagePublicId() : "");
            docData.put("createdAt", roommate.getCreatedAt() > 0 ? roommate.getCreatedAt() : System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(roommate.getId().trim()).set(docData);
            result.get();
            System.out.println("[RoommateDAOImpl] Roommate profile saved successfully to Firestore: " + roommate.getId());
            return true;
        } catch (Throwable e) {
            System.err.println("[RoommateDAOImpl] Error saving roommate to Firestore: " + e.getMessage());
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
            System.out.println("[RoommateDAOImpl] Roommate deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[RoommateDAOImpl] Error deleting roommate: " + e.getMessage());
            return false;
        }
    }

    private RoommateItem parseRoommateFromSnapshot(DocumentSnapshot doc) {
        String id = doc.getString("roommateId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String userUid = doc.getString("userUid");
        String name = doc.getString("name");
        String gender = doc.getString("gender");
        String location = doc.getString("location");
        String budget = doc.getString("budget");
        String preference = doc.getString("preference");
        String bio = doc.getString("bio");
        String phone = doc.getString("phone");
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl == null) imageUrl = doc.getString("imagePath");
        String imagePublicId = doc.getString("imagePublicId");
        Long createdAt = doc.getLong("createdAt");

        return new RoommateItem(
            id,
            userUid != null ? userUid : id,
            name != null ? name : "Student",
            gender != null ? gender : "Any",
            location != null ? location : "Pune",
            budget != null ? budget : "₹ 5,000 / mo",
            preference != null ? preference : "Non-Smoker",
            bio != null ? bio : "",
            phone != null ? phone : "",
            imageUrl != null ? imageUrl : "",
            imagePublicId != null ? imagePublicId : "",
            createdAt != null ? createdAt : System.currentTimeMillis()
        );
    }
}
