package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.ServiceItem;
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

public class ServiceDAOImpl implements ServiceDAO {

    private static final String COLLECTION_NAME = "services";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<ServiceItem> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseServiceFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[ServiceDAOImpl] Error finding service by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<ServiceItem> findAll() {
        List<ServiceItem> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseServiceFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[ServiceDAOImpl] Error fetching all services: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ServiceItem> findByProviderUid(String providerUid) {
        List<ServiceItem> list = new ArrayList<>();
        if (providerUid == null || providerUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("providerUid", providerUid.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseServiceFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[ServiceDAOImpl] Error fetching services by provider UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(ServiceItem service) {
        if (service == null || service.getId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("serviceId", service.getId());
            docData.put("id", service.getId());
            docData.put("providerUid", service.getProviderUid() != null ? service.getProviderUid() : "");
            docData.put("providerName", service.getProviderName() != null ? service.getProviderName() : "");
            docData.put("providerPhone", service.getProviderPhone() != null ? service.getProviderPhone() : "");
            docData.put("title", service.getTitle() != null ? service.getTitle() : "");
            docData.put("subtitle", service.getSubtitle() != null ? service.getSubtitle() : "");
            docData.put("category", service.getCategory() != null ? service.getCategory() : "");
            docData.put("price", service.getPrice() != null ? service.getPrice() : "");
            docData.put("description", service.getDescription() != null ? service.getDescription() : "");
            docData.put("icon", service.getIcon() != null ? service.getIcon() : "🛠️");
            docData.put("imageUrl", service.getImageUrl() != null ? service.getImageUrl() : "");
            docData.put("imagePath", service.getImageUrl() != null ? service.getImageUrl() : "");
            docData.put("imagePublicId", service.getImagePublicId() != null ? service.getImagePublicId() : "");
            docData.put("createdAt", service.getCreatedAt() > 0 ? service.getCreatedAt() : System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(service.getId().trim()).set(docData);
            result.get();
            System.out.println("[ServiceDAOImpl] Service saved successfully to Firestore: " + service.getId());
            return true;
        } catch (Throwable e) {
            System.err.println("[ServiceDAOImpl] Error saving service to Firestore: " + e.getMessage());
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
            System.out.println("[ServiceDAOImpl] Service deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[ServiceDAOImpl] Error deleting service: " + e.getMessage());
            return false;
        }
    }

    private ServiceItem parseServiceFromSnapshot(DocumentSnapshot doc) {
        String id = doc.getString("serviceId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String icon = doc.getString("icon");
        String title = doc.getString("title");
        String category = doc.getString("category");
        String subtitle = doc.getString("subtitle");
        String price = doc.getString("price");
        String providerName = doc.getString("providerName");
        String providerPhone = doc.getString("providerPhone");
        String description = doc.getString("description");
        String providerUid = doc.getString("providerUid");
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl == null) imageUrl = doc.getString("imagePath");
        String imagePublicId = doc.getString("imagePublicId");
        Long createdAt = doc.getLong("createdAt");

        return new ServiceItem(
            id,
            icon != null ? icon : "🛠️",
            title != null ? title : "Service",
            category != null ? category : "General",
            subtitle != null ? subtitle : "",
            price != null ? price : "₹ 0",
            providerName != null ? providerName : "Provider",
            providerPhone != null ? providerPhone : "",
            description != null ? description : "",
            providerUid,
            imageUrl != null ? imageUrl : "",
            imagePublicId != null ? imagePublicId : "",
            createdAt != null ? createdAt : System.currentTimeMillis()
        );
    }
}
