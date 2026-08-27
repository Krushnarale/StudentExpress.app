package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.RoomItem;
import com.core2web.service.CloudinaryService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoomDAOImpl implements RoomDAO {

    private static final String COLLECTION_NAME = "rooms";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<RoomItem> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseRoomFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[RoomDAOImpl] Error finding room by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<RoomItem> findAll() {
        List<RoomItem> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseRoomFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[RoomDAOImpl] Error fetching all rooms: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<RoomItem> findByOwnerUid(String ownerUid) {
        List<RoomItem> list = new ArrayList<>();
        if (ownerUid == null || ownerUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("ownerUid", ownerUid.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseRoomFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[RoomDAOImpl] Error fetching rooms by owner UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(RoomItem room) {
        if (room == null || room.getId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("roomId", room.getId());
            docData.put("id", room.getId());
            docData.put("ownerUid", room.getOwnerUid() != null ? room.getOwnerUid() : "");
            docData.put("ownerName", room.getOwnerName() != null ? room.getOwnerName() : "");
            docData.put("title", room.getTitle() != null ? room.getTitle() : "");
            docData.put("location", room.getLocation() != null ? room.getLocation() : "");
            docData.put("price", room.getPrice() != null ? room.getPrice() : "");
            docData.put("distance", room.getDistance() != null ? room.getDistance() : "");
            docData.put("occupants", room.getOccupants() != null ? room.getOccupants() : "");
            docData.put("category", room.getCategory() != null ? room.getCategory() : "Rooms & PGs");
            docData.put("type", room.getCategory() != null ? room.getCategory() : "Rooms & PGs");
            docData.put("tags", room.getTags() != null ? Arrays.asList(room.getTags()) : new ArrayList<>());
            docData.put("description", room.getDescription() != null ? room.getDescription() : "");
            docData.put("ownerPhone", room.getOwnerPhone() != null ? room.getOwnerPhone() : "");
            docData.put("imageUrl", room.getImageUrl() != null ? room.getImageUrl() : "");
            docData.put("imagePath", room.getImageUrl() != null ? room.getImageUrl() : "");
            docData.put("imagePublicId", room.getImagePublicId() != null ? room.getImagePublicId() : "");
            docData.put("available", room.isAvailable());
            docData.put("createdAt", room.getCreatedAt() > 0 ? room.getCreatedAt() : System.currentTimeMillis());
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(room.getId().trim()).set(docData);
            result.get();
            System.out.println("[RoomDAOImpl] Room saved successfully to Firestore: " + room.getId());
            return true;
        } catch (Throwable e) {
            System.err.println("[RoomDAOImpl] Error saving room to Firestore: " + e.getMessage());
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
            System.out.println("[RoomDAOImpl] Room deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[RoomDAOImpl] Error deleting room: " + e.getMessage());
            return false;
        }
    }

    private RoomItem parseRoomFromSnapshot(DocumentSnapshot doc) {
        String id = doc.getString("roomId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String title = doc.getString("title");
        String location = doc.getString("location");
        String price = doc.getString("price");
        String distance = doc.getString("distance");
        String occupants = doc.getString("occupants");
        String category = doc.getString("category");
        if (category == null) category = doc.getString("type");
        String description = doc.getString("description");
        String ownerName = doc.getString("ownerName");
        String ownerPhone = doc.getString("ownerPhone");
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl == null) imageUrl = doc.getString("imagePath");
        String imagePublicId = doc.getString("imagePublicId");
        String ownerUid = doc.getString("ownerUid");
        Boolean available = doc.getBoolean("available");
        Long createdAt = doc.getLong("createdAt");
        Long updatedAt = doc.getLong("updatedAt");

        List<?> tagsList = (List<?>) doc.get("tags");
        String[] tags = new String[0];
        if (tagsList != null) {
            tags = tagsList.stream().map(Object::toString).toArray(String[]::new);
        }

        return new RoomItem(
            id,
            title != null ? title : "Room",
            location != null ? location : "Pune",
            price != null ? price : "₹ 5,000 / month",
            distance != null ? distance : "1.0 km",
            occupants != null ? occupants : "1 Occupant",
            category != null ? category : "Rooms & PGs",
            tags,
            description != null ? description : "",
            ownerName != null ? ownerName : "Owner",
            ownerPhone != null ? ownerPhone : "",
            imageUrl != null ? imageUrl : "",
            imagePublicId != null ? imagePublicId : "",
            ownerUid,
            available != null ? available : true,
            createdAt != null ? createdAt : System.currentTimeMillis(),
            updatedAt != null ? updatedAt : System.currentTimeMillis()
        );
    }
}
