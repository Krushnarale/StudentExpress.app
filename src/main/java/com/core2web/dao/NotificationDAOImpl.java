package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.Notification;
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

public class NotificationDAOImpl implements NotificationDAO {

    private static final String COLLECTION_NAME = "notifications";

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public boolean save(Notification notification) {
        if (notification == null || notification.getNotificationId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("notificationId", notification.getNotificationId());
            docData.put("id", notification.getNotificationId());
            docData.put("uid", notification.getUid() != null ? notification.getUid() : "");
            docData.put("title", notification.getTitle() != null ? notification.getTitle() : "");
            docData.put("message", notification.getMessage() != null ? notification.getMessage() : "");
            docData.put("type", notification.getType() != null ? notification.getType() : "GENERAL");
            docData.put("isRead", notification.isRead());
            docData.put("createdAt", notification.getCreatedAt() > 0 ? notification.getCreatedAt() : System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME)
                    .document(notification.getNotificationId().trim())
                    .set(docData);
            result.get();
            System.out.println("[NotificationDAOImpl] Notification saved to Firestore: " + notification.getNotificationId());
            return true;
        } catch (Throwable e) {
            System.err.println("[NotificationDAOImpl] Error saving notification: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Notification> findByUid(String uid) {
        List<Notification> list = new ArrayList<>();
        if (uid == null || uid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("uid", uid.trim())
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseFromSnapshot(doc));
            }
            list.sort((n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));
        } catch (Throwable e) {
            System.err.println("[NotificationDAOImpl] Error fetching notifications: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int getUnreadCount(String uid) {
        if (uid == null || uid.trim().isEmpty()) return 0;
        Firestore db = getFirestore();
        if (db == null) return 0;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("uid", uid.trim())
                    .whereEqualTo("isRead", false)
                    .get();
            return future.get().getDocuments().size();
        } catch (Throwable e) {
            System.err.println("[NotificationDAOImpl] Error getting unread count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean markAsRead(String notificationId) {
        if (notificationId == null || notificationId.trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> update = new HashMap<>();
            update.put("isRead", true);
            db.collection(COLLECTION_NAME).document(notificationId.trim()).update(update).get();
            return true;
        } catch (Throwable e) {
            System.err.println("[NotificationDAOImpl] Error marking notification read: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String notificationId) {
        if (notificationId == null || notificationId.trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            db.collection(COLLECTION_NAME).document(notificationId.trim()).delete().get();
            return true;
        } catch (Throwable e) {
            System.err.println("[NotificationDAOImpl] Error deleting notification: " + e.getMessage());
            return false;
        }
    }

    private Notification parseFromSnapshot(DocumentSnapshot doc) {
        String notificationId = doc.getString("notificationId");
        if (notificationId == null) notificationId = doc.getString("id");
        if (notificationId == null) notificationId = doc.getId();

        String uid = doc.getString("uid");
        String title = doc.getString("title");
        String message = doc.getString("message");
        String type = doc.getString("type");
        Boolean isRead = doc.getBoolean("isRead");
        Long createdAt = doc.getLong("createdAt");

        return new Notification(
                notificationId,
                uid != null ? uid : "",
                title != null ? title : "Notification",
                message != null ? message : "",
                type != null ? type : "GENERAL",
                isRead != null ? isRead : false,
                createdAt != null ? createdAt : System.currentTimeMillis()
        );
    }
}
