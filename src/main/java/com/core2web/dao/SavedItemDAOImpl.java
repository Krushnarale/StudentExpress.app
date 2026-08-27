package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.util.SessionManager;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SavedItemDAOImpl implements SavedItemDAO {

    private static final String COLLECTION_NAME = "savedItems";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    private String getActiveUserUid() {
        String uid = SessionManager.getInstance().getUid();
        return uid != null ? uid : "default_user";
    }

    @Override
    public Set<String> getSavedRoomIds() {
        return getSavedRoomIds(getActiveUserUid());
    }

    @Override
    public Set<String> getSavedProductIds() {
        return getSavedProductIds(getActiveUserUid());
    }

    @Override
    public Set<String> getSavedRoomIds(String userUid) {
        Set<String> set = new HashSet<>();
        if (userUid == null || userUid.trim().isEmpty()) return set;
        Firestore db = getFirestore();
        if (db == null) return set;

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(userUid.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                List<?> list = (List<?>) snapshot.get("savedRoomIds");
                if (list != null) {
                    for (Object item : list) {
                        set.add(item.toString());
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[SavedItemDAOImpl] Error getting saved room IDs: " + e.getMessage());
        }
        return set;
    }

    @Override
    public Set<String> getSavedProductIds(String userUid) {
        Set<String> set = new HashSet<>();
        if (userUid == null || userUid.trim().isEmpty()) return set;
        Firestore db = getFirestore();
        if (db == null) return set;

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(userUid.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                List<?> list = (List<?>) snapshot.get("savedProductIds");
                if (list != null) {
                    for (Object item : list) {
                        set.add(item.toString());
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[SavedItemDAOImpl] Error getting saved product IDs: " + e.getMessage());
        }
        return set;
    }

    @Override
    public boolean toggleSavedRoom(String roomId) {
        return toggleSavedRoom(getActiveUserUid(), roomId);
    }

    @Override
    public boolean toggleSavedProduct(String productId) {
        return toggleSavedProduct(getActiveUserUid(), productId);
    }

    @Override
    public boolean toggleSavedRoom(String userUid, String roomId) {
        if (userUid == null || roomId == null) return false;
        Set<String> roomIds = getSavedRoomIds(userUid);
        Set<String> productIds = getSavedProductIds(userUid);

        boolean nowSaved;
        if (roomIds.contains(roomId)) {
            roomIds.remove(roomId);
            nowSaved = false;
        } else {
            roomIds.add(roomId);
            nowSaved = true;
        }

        saveToFirestore(userUid, roomIds, productIds);
        return nowSaved;
    }

    @Override
    public boolean toggleSavedProduct(String userUid, String productId) {
        if (userUid == null || productId == null) return false;
        Set<String> roomIds = getSavedRoomIds(userUid);
        Set<String> productIds = getSavedProductIds(userUid);

        boolean nowSaved;
        if (productIds.contains(productId)) {
            productIds.remove(productId);
            nowSaved = false;
        } else {
            productIds.add(productId);
            nowSaved = true;
        }

        saveToFirestore(userUid, roomIds, productIds);
        return nowSaved;
    }

    private void saveToFirestore(String userUid, Set<String> roomIds, Set<String> productIds) {
        Firestore db = getFirestore();
        if (db == null) return;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("userUid", userUid);
            docData.put("savedRoomIds", new ArrayList<>(roomIds));
            docData.put("savedProductIds", new ArrayList<>(productIds));
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(userUid.trim()).set(docData);
            result.get();
            System.out.println("[SavedItemDAOImpl] Saved items updated for user: " + userUid);
        } catch (Throwable e) {
            System.err.println("[SavedItemDAOImpl] Error updating saved items in Firestore: " + e.getMessage());
        }
    }
}
