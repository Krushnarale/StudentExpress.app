package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.Notification;
import com.core2web.model.RoommateRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.*;

public class RoommateRequestDAOImpl implements RoommateRequestDAO {

    private static final String COLLECTION_NAME = "roommateRequests";
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<RoommateRequest> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseRequestFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error finding request by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<RoommateRequest> findAll() {
        List<RoommateRequest> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                list.add(parseRequestFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error fetching all requests: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<RoommateRequest> findByReceiverUid(String receiverUid) {
        List<RoommateRequest> list = new ArrayList<>();
        if (receiverUid == null || receiverUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        String clean = receiverUid.trim();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("receiverStudentId", clean)
                    .get();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                RoommateRequest r = parseRequestFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRequestId().equals(r.getRequestId()))) {
                    list.add(r);
                }
            }
            if (list.isEmpty()) {
                List<RoommateRequest> all = findAll();
                for (RoommateRequest r : all) {
                    if (clean.equalsIgnoreCase(r.getReceiverStudentId())) {
                        if (list.stream().noneMatch(existing -> existing.getRequestId().equals(r.getRequestId()))) {
                            list.add(r);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error fetching requests by receiver UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<RoommateRequest> findBySenderUid(String senderUid) {
        List<RoommateRequest> list = new ArrayList<>();
        if (senderUid == null || senderUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        String clean = senderUid.trim();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("senderStudentId", clean)
                    .get();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                RoommateRequest r = parseRequestFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRequestId().equals(r.getRequestId()))) {
                    list.add(r);
                }
            }
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error fetching requests by sender UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(RoommateRequest request) {
        if (request == null || request.getRequestId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("requestId", request.getRequestId());
            docData.put("id", request.getRequestId());
            docData.put("senderStudentId", request.getSenderStudentId() != null ? request.getSenderStudentId() : "");
            docData.put("senderUid", request.getSenderStudentId() != null ? request.getSenderStudentId() : "");
            docData.put("senderName", request.getSenderName() != null ? request.getSenderName() : "Student");
            docData.put("senderEmail", request.getSenderEmail() != null ? request.getSenderEmail() : "");
            docData.put("senderPhone", request.getSenderPhone() != null ? request.getSenderPhone() : "");
            docData.put("receiverStudentId", request.getReceiverStudentId() != null ? request.getReceiverStudentId() : "");
            docData.put("receiverUid", request.getReceiverStudentId() != null ? request.getReceiverStudentId() : "");
            docData.put("receiverName", request.getReceiverName() != null ? request.getReceiverName() : "Student");
            docData.put("roommateProfileId", request.getRoommateProfileId() != null ? request.getRoommateProfileId() : "");
            docData.put("status", request.getStatus() != null ? request.getStatus() : "PENDING");
            docData.put("message", request.getMessage() != null ? request.getMessage() : "");
            docData.put("timestamp", request.getTimestamp() > 0 ? request.getTimestamp() : System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(request.getRequestId().trim()).set(docData);
            result.get();
            System.out.println("[RoommateRequestDAOImpl] Roommate request saved to Firestore: " + request.getRequestId());

            // Notify receiver student
            if (request.getReceiverStudentId() != null && !request.getReceiverStudentId().isEmpty()) {
                notificationDAO.save(new Notification(
                    "notif_rm_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                    request.getReceiverStudentId(),
                    "New Roommate Connection Request",
                    request.getSenderName() + " wants to connect with you for roommate sharing!",
                    "ROOMMATE",
                    false,
                    System.currentTimeMillis()
                ));
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error saving roommate request: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(String requestId, String status) {
        if (requestId == null || requestId.trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> update = new HashMap<>();
            update.put("status", status != null ? status : "PENDING");
            update.put("updatedAt", System.currentTimeMillis());

            db.collection(COLLECTION_NAME).document(requestId.trim()).update(update).get();
            System.out.println("[RoommateRequestDAOImpl] Roommate request status updated: " + requestId + " -> " + status);
            return true;
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error updating status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            db.collection(COLLECTION_NAME).document(id.trim()).delete().get();
            System.out.println("[RoommateRequestDAOImpl] Roommate request deleted: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[RoommateRequestDAOImpl] Error deleting request: " + e.getMessage());
            return false;
        }
    }

    private RoommateRequest parseRequestFromSnapshot(DocumentSnapshot doc) {
        String reqId = doc.getString("requestId");
        if (reqId == null) reqId = doc.getString("id");
        if (reqId == null) reqId = doc.getId();

        String senderId = doc.getString("senderStudentId");
        if (senderId == null) senderId = doc.getString("senderUid");
        String senderName = doc.getString("senderName");
        String senderEmail = doc.getString("senderEmail");
        String senderPhone = doc.getString("senderPhone");

        String receiverId = doc.getString("receiverStudentId");
        if (receiverId == null) receiverId = doc.getString("receiverUid");
        String receiverName = doc.getString("receiverName");

        String profileId = doc.getString("roommateProfileId");
        String status = doc.getString("status");
        String msg = doc.getString("message");
        Long ts = doc.getLong("timestamp");

        return new RoommateRequest(
            reqId,
            senderId != null ? senderId : "",
            senderName != null ? senderName : "Student",
            senderEmail != null ? senderEmail : "",
            senderPhone != null ? senderPhone : "",
            receiverId != null ? receiverId : "",
            receiverName != null ? receiverName : "Student",
            profileId != null ? profileId : "",
            status != null ? status : "PENDING",
            msg != null ? msg : "",
            ts != null ? ts : System.currentTimeMillis()
        );
    }
}
