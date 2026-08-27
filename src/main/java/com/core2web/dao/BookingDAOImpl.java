package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.Booking;
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
import java.util.Optional;
import java.util.UUID;

public class BookingDAOImpl implements BookingDAO {

    private static final String COLLECTION_NAME = "bookings";
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<Booking> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseBookingFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error finding booking by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseBookingFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error fetching all bookings: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Booking> findByUserUid(String userUid) {
        List<Booking> list = new ArrayList<>();
        if (userUid == null || userUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("userUid", userUid.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseBookingFromSnapshot(doc));
            }
            if (list.isEmpty()) {
                ApiFuture<QuerySnapshot> altFuture = db.collection(COLLECTION_NAME).whereEqualTo("userId", userUid.trim()).get();
                for (DocumentSnapshot doc : altFuture.get().getDocuments()) {
                    list.add(parseBookingFromSnapshot(doc));
                }
            }
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error fetching bookings by user UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Booking> findByOwnerUid(String ownerUid) {
        List<Booking> list = new ArrayList<>();
        if (ownerUid == null || ownerUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("ownerUid", ownerUid.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseBookingFromSnapshot(doc));
            }
            if (list.isEmpty()) {
                ApiFuture<QuerySnapshot> altFuture = db.collection(COLLECTION_NAME).whereEqualTo("ownerId", ownerUid.trim()).get();
                for (DocumentSnapshot doc : altFuture.get().getDocuments()) {
                    list.add(parseBookingFromSnapshot(doc));
                }
            }
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error fetching bookings by owner UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Booking> findByProviderId(String providerId) {
        List<Booking> list = new ArrayList<>();
        if (providerId == null || providerId.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("providerUid", providerId.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseBookingFromSnapshot(doc));
            }
            if (list.isEmpty()) {
                return findByOwnerUid(providerId);
            }
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error fetching bookings by provider ID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Booking> findByProviderUid(String providerUid) {
        return findByProviderId(providerUid);
    }

    @Override
    public boolean updateStatus(String id, String status) {
        if (id == null || id.trim().isEmpty() || status == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> update = new HashMap<>();
            String cleanStatus = status.trim().toUpperCase();
            update.put("status", cleanStatus);
            update.put("updatedAt", System.currentTimeMillis());
            db.collection(COLLECTION_NAME).document(id.trim()).update(update).get();
            System.out.println("[BookingDAOImpl] Updated booking status: " + id + " -> " + cleanStatus);

            Optional<Booking> opt = findById(id.trim());
            if (opt.isPresent()) {
                Booking b = opt.get();
                String targetUid = b.getUserUid();
                if (targetUid != null && !targetUid.isEmpty()) {
                    String title = "ACCEPTED".equals(cleanStatus) ? "Booking Accepted" : ("REJECTED".equals(cleanStatus) ? "Booking Rejected" : "Booking Updated");
                    String msg = "Your booking for '" + b.getItemOrServiceName() + "' status is now: " + cleanStatus;
                    notificationDAO.save(new Notification(
                            "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                            targetUid,
                            title,
                            msg,
                            "BOOKING",
                            false,
                            System.currentTimeMillis()
                    ));
                }
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error updating status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean save(Booking booking) {
        if (booking == null || booking.getId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("bookingId", booking.getId());
            docData.put("id", booking.getId());
            docData.put("userUid", booking.getUserUid() != null ? booking.getUserUid() : "");
            docData.put("ownerUid", booking.getOwnerUid() != null ? booking.getOwnerUid() : "");
            docData.put("providerUid", booking.getProviderUid() != null ? booking.getProviderUid() : "");
            docData.put("itemId", booking.getItemId() != null ? booking.getItemId() : "");
            docData.put("bookingType", booking.getBookingType() != null ? booking.getBookingType() : "ROOM");
            docData.put("bookingDate", booking.getBookingDate() != null ? booking.getBookingDate() : "");
            docData.put("timeSlot", booking.getTimeSlot() != null ? booking.getTimeSlot() : "");
            docData.put("address", booking.getAddress() != null ? booking.getAddress() : "");
            docData.put("status", booking.getStatus() != null ? booking.getStatus() : "PENDING");
            docData.put("itemOrServiceName", booking.getItemOrServiceName() != null ? booking.getItemOrServiceName() : "");
            docData.put("category", booking.getCategory() != null ? booking.getCategory() : "");
            docData.put("userEmail", booking.getUserEmail() != null ? booking.getUserEmail() : "");
            docData.put("createdAt", booking.getCreatedAt() > 0 ? booking.getCreatedAt() : System.currentTimeMillis());
            docData.put("updatedAt", System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(booking.getId().trim()).set(docData);
            result.get();
            System.out.println("[BookingDAOImpl] Booking saved successfully to Firestore: " + booking.getId());

            String recipientUid = booking.getOwnerUid();
            if (recipientUid == null || recipientUid.isEmpty()) recipientUid = booking.getProviderUid();
            if (recipientUid != null && !recipientUid.isEmpty()) {
                notificationDAO.save(new Notification(
                        "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                        recipientUid,
                        "New Booking Request",
                        "You received a new booking request for '" + booking.getItemOrServiceName() + "'",
                        "BOOKING",
                        false,
                        System.currentTimeMillis()
                ));
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error saving booking to Firestore: " + e.getMessage());
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
            System.out.println("[BookingDAOImpl] Booking deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[BookingDAOImpl] Error deleting booking: " + e.getMessage());
            return false;
        }
    }

    private Booking parseBookingFromSnapshot(DocumentSnapshot doc) {
        String id = doc.getString("bookingId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String itemOrServiceName = doc.getString("itemOrServiceName");
        if (itemOrServiceName == null) itemOrServiceName = doc.getString("roomTitle");
        if (itemOrServiceName == null) itemOrServiceName = doc.getString("serviceTitle");

        String category = doc.getString("category");
        String date = doc.getString("bookingDate");
        if (date == null) date = doc.getString("date");

        String timeSlot = doc.getString("timeSlot");
        String address = doc.getString("address");
        String status = doc.getString("status");
        String userEmail = doc.getString("userEmail");

        String userUid = doc.getString("userUid");
        if (userUid == null) userUid = doc.getString("userId");

        String ownerUid = doc.getString("ownerUid");
        if (ownerUid == null) ownerUid = doc.getString("ownerId");

        String providerUid = doc.getString("providerUid");
        if (providerUid == null) providerUid = doc.getString("providerId");

        String itemId = doc.getString("itemId");
        if (itemId == null) itemId = doc.getString("roomId");
        if (itemId == null) itemId = doc.getString("serviceId");

        String bookingType = doc.getString("bookingType");
        Long createdAt = doc.getLong("createdAt");

        return new Booking(
            id,
            userUid != null ? userUid : "",
            ownerUid != null ? ownerUid : "",
            providerUid != null ? providerUid : ownerUid,
            itemId != null ? itemId : "",
            bookingType != null ? bookingType : "ROOM",
            date != null ? date : "Today",
            timeSlot != null ? timeSlot : "",
            address != null ? address : "",
            status != null ? status : "PENDING",
            itemOrServiceName != null ? itemOrServiceName : "Booking",
            category != null ? category : "General",
            userEmail != null ? userEmail : "",
            createdAt != null ? createdAt : System.currentTimeMillis()
        );
    }
}
