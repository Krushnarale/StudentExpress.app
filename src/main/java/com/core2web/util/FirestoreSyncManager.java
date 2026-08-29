package com.core2web.util;

import com.core2web.config.FirebaseConfig;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QuerySnapshot;
import javafx.application.Platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FirestoreSyncManager {

    private static final Map<String, ListenerRegistration> activeListeners = new ConcurrentHashMap<>();

    private static Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    public static synchronized ListenerRegistration listenToCollection(String collectionName, Runnable onUpdate) {
        if (collectionName == null || collectionName.trim().isEmpty() || onUpdate == null) {
            return null;
        }

        String key = collectionName.trim();
        removeListener(key);

        Firestore db = getFirestore();
        if (db == null) return null;

        try {
            ListenerRegistration registration = db.collection(key).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot snapshots, FirestoreException error) {
                    if (error != null) {
                        System.err.println("[FirestoreSyncManager] Listener error on collection " + key + ": " + error.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        Platform.runLater(onUpdate);
                    }
                }
            });
            activeListeners.put(key, registration);
            System.out.println("[FirestoreSyncManager] Registered snapshot listener for collection: " + key);
            return registration;
        } catch (Throwable e) {
            System.err.println("[FirestoreSyncManager] Error setting up listener for collection " + key + ": " + e.getMessage());
            return null;
        }
    }

    public static synchronized void removeListener(String collectionName) {
        if (collectionName == null) return;
        ListenerRegistration registration = activeListeners.remove(collectionName.trim());
        if (registration != null) {
            try {
                registration.remove();
                System.out.println("[FirestoreSyncManager] Removed snapshot listener for collection: " + collectionName);
            } catch (Throwable e) {
                System.err.println("[FirestoreSyncManager] Error removing listener for " + collectionName + ": " + e.getMessage());
            }
        }
    }

    public static synchronized void stopAllListeners() {
        for (Map.Entry<String, ListenerRegistration> entry : activeListeners.entrySet()) {
            try {
                if (entry.getValue() != null) {
                    entry.getValue().remove();
                }
            } catch (Throwable ignored) {}
        }
        activeListeners.clear();
        System.out.println("[FirestoreSyncManager] Stopped all snapshot listeners.");
    }
}
