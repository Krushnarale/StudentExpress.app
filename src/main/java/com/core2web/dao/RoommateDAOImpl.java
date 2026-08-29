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
    public Optional<RoommateItem> findByUserUid(String userUid) {
        if (userUid == null || userUid.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        String clean = userUid.trim();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("userUid", clean).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents != null && !documents.isEmpty()) {
                return Optional.of(parseRoommateFromSnapshot(documents.get(0)));
            }

            ApiFuture<QuerySnapshot> f2 = db.collection(COLLECTION_NAME).whereEqualTo("studentId", clean).get();
            List<QueryDocumentSnapshot> d2 = f2.get().getDocuments();
            if (d2 != null && !d2.isEmpty()) {
                return Optional.of(parseRoommateFromSnapshot(d2.get(0)));
            }

            // Fallback scan
            List<RoommateItem> all = findAll();
            for (RoommateItem rm : all) {
                if (clean.equalsIgnoreCase(rm.getUserUid())) {
                    return Optional.of(rm);
                }
            }
        } catch (Throwable e) {
            System.err.println("[RoommateDAOImpl] Error finding roommate by user UID: " + e.getMessage());
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
            docData.put("roommateProfileId", roommate.getId());
            docData.put("roommateId", roommate.getId());
            docData.put("id", roommate.getId());
            docData.put("studentId", roommate.getUserUid() != null ? roommate.getUserUid() : roommate.getId());
            docData.put("userUid", roommate.getUserUid() != null ? roommate.getUserUid() : roommate.getId());
            docData.put("name", roommate.getName() != null ? roommate.getName() : "");
            docData.put("email", roommate.getEmail() != null ? roommate.getEmail() : "");
            docData.put("phone", roommate.getPhone() != null ? roommate.getPhone() : "");
            docData.put("gender", roommate.getGender() != null ? roommate.getGender() : "Any");
            docData.put("age", roommate.getAge());
            docData.put("college", roommate.getCollege() != null ? roommate.getCollege() : "");
            docData.put("course", roommate.getCourse() != null ? roommate.getCourse() : "");
            docData.put("department", roommate.getCourse() != null ? roommate.getCourse() : "");
            docData.put("year", roommate.getYear() != null ? roommate.getYear() : "");
            docData.put("location", roommate.getLocation() != null ? roommate.getLocation() : "");
            docData.put("preferredLocation", roommate.getLocation() != null ? roommate.getLocation() : "");
            docData.put("budget", roommate.getBudget() != null ? roommate.getBudget() : "");
            docData.put("accommodationType", roommate.getAccommodationType() != null ? roommate.getAccommodationType() : "Any");
            docData.put("roommatesNeeded", roommate.getRoommatesNeeded() != null ? roommate.getRoommatesNeeded() : "Any");
            docData.put("preference", roommate.getPreference() != null ? roommate.getPreference() : "");
            docData.put("lifestyle", roommate.getPreference() != null ? roommate.getPreference() : "");
            docData.put("bio", roommate.getBio() != null ? roommate.getBio() : "");
            docData.put("aboutMe", roommate.getBio() != null ? roommate.getBio() : "");
            docData.put("imageUrl", roommate.getImageUrl() != null ? roommate.getImageUrl() : "");
            docData.put("imagePath", roommate.getImageUrl() != null ? roommate.getImageUrl() : "");
            docData.put("profilePhoto", roommate.getImageUrl() != null ? roommate.getImageUrl() : "");
            docData.put("imagePublicId", roommate.getImagePublicId() != null ? roommate.getImagePublicId() : "");
            docData.put("status", roommate.getStatus() != null ? roommate.getStatus() : "ACTIVE");
            docData.put("createdAt", roommate.getCreatedAt() > 0 ? roommate.getCreatedAt() : System.currentTimeMillis());
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(roommate.getId().trim()).set(docData);
            result.get();
            System.out.println("[RoommateDAOImpl] Roommate profile saved successfully to Firestore: " + roommate.getId() + " (userUid: " + roommate.getUserUid() + ")");
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
        String id = doc.getString("roommateProfileId");
        if (id == null) id = doc.getString("roommateId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String userUid = doc.getString("studentId");
        if (userUid == null) userUid = doc.getString("userUid");
        if (userUid == null) userUid = doc.getString("userId");

        String name = doc.getString("name");
        String email = doc.getString("email");
        String phone = doc.getString("phone");
        String gender = doc.getString("gender");
        Long ageVal = doc.getLong("age");
        int age = ageVal != null ? ageVal.intValue() : 0;

        String college = doc.getString("college");
        String course = doc.getString("course");
        if (course == null) course = doc.getString("department");
        String year = doc.getString("year");

        String location = doc.getString("preferredLocation");
        if (location == null) location = doc.getString("location");

        String budget = doc.getString("budget");
        String accommodationType = doc.getString("accommodationType");
        String roommatesNeeded = doc.getString("roommatesNeeded");

        String preference = doc.getString("preference");
        if (preference == null) preference = doc.getString("lifestyle");

        String bio = doc.getString("bio");
        if (bio == null) bio = doc.getString("aboutMe");

        String imageUrl = doc.getString("profilePhoto");
        if (imageUrl == null) imageUrl = doc.getString("imageUrl");
        if (imageUrl == null) imageUrl = doc.getString("imagePath");

        String imagePublicId = doc.getString("imagePublicId");
        String status = doc.getString("status");
        Long createdAt = doc.getLong("createdAt");
        Long updatedAt = doc.getLong("updatedAt");

        return new RoommateItem(
            id,
            userUid != null ? userUid : id,
            name != null ? name : "Student",
            email != null ? email : "",
            phone != null ? phone : "",
            gender != null ? gender : "Any",
            age,
            college != null ? college : "",
            course != null ? course : "",
            year != null ? year : "",
            location != null ? location : "Pune",
            budget != null ? budget : "₹ 5,000 / mo",
            accommodationType != null ? accommodationType : "Any",
            roommatesNeeded != null ? roommatesNeeded : "Any",
            preference != null ? preference : "Non-Smoker",
            bio != null ? bio : "",
            imageUrl != null ? imageUrl : "",
            imagePublicId != null ? imagePublicId : "",
            status != null ? status : "ACTIVE",
            createdAt != null ? createdAt : System.currentTimeMillis(),
            updatedAt != null ? updatedAt : System.currentTimeMillis()
        );
    }
}
