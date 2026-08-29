package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.Notification;
import com.core2web.model.Rental;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RentalDAOImpl implements RentalDAO {

    private static final String COLLECTION_NAME = "rentals";
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<Rental> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseRentalFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error finding rental by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Rental> findAll() {
        List<Rental> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseRentalFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error fetching all rentals: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Rental> findByOwnerId(String ownerId) {
        List<Rental> list = new ArrayList<>();
        if (ownerId == null || ownerId.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        String clean = ownerId.trim();
        try {
            // Check ownerId
            ApiFuture<QuerySnapshot> f1 = db.collection(COLLECTION_NAME).whereEqualTo("ownerId", clean).get();
            for (DocumentSnapshot doc : f1.get().getDocuments()) {
                Rental r = parseRentalFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                    list.add(r);
                }
            }
            // Check ownerUid
            ApiFuture<QuerySnapshot> f2 = db.collection(COLLECTION_NAME).whereEqualTo("ownerUid", clean).get();
            for (DocumentSnapshot doc : f2.get().getDocuments()) {
                Rental r = parseRentalFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                    list.add(r);
                }
            }
            // Check ownerName fallback
            ApiFuture<QuerySnapshot> f3 = db.collection(COLLECTION_NAME).whereEqualTo("ownerName", clean).get();
            for (DocumentSnapshot doc : f3.get().getDocuments()) {
                Rental r = parseRentalFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                    list.add(r);
                }
            }

            // In-memory scan fallback to ensure 100% detection regardless of Firestore index state
            if (list.isEmpty()) {
                List<Rental> allRentals = findAll();
                for (Rental r : allRentals) {
                    if (clean.equalsIgnoreCase(r.getOwnerId()) || clean.equalsIgnoreCase(r.getOwnerName())) {
                        if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                            list.add(r);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error fetching rentals by owner ID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Rental> findByStudentId(String studentId) {
        List<Rental> list = new ArrayList<>();
        if (studentId == null || studentId.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        String clean = studentId.trim();
        try {
            ApiFuture<QuerySnapshot> f1 = db.collection(COLLECTION_NAME).whereEqualTo("studentId", clean).get();
            for (DocumentSnapshot doc : f1.get().getDocuments()) {
                Rental r = parseRentalFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                    list.add(r);
                }
            }
            ApiFuture<QuerySnapshot> f2 = db.collection(COLLECTION_NAME).whereEqualTo("studentEmail", clean).get();
            for (DocumentSnapshot doc : f2.get().getDocuments()) {
                Rental r = parseRentalFromSnapshot(doc);
                if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                    list.add(r);
                }
            }
            if (list.isEmpty()) {
                List<Rental> allRentals = findAll();
                for (Rental r : allRentals) {
                    if (clean.equalsIgnoreCase(r.getStudentId()) || clean.equalsIgnoreCase(r.getStudentEmail())) {
                        if (list.stream().noneMatch(existing -> existing.getRentalId().equals(r.getRentalId()))) {
                            list.add(r);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error fetching rentals by student ID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean save(Rental rental) {
        if (rental == null || rental.getRentalId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("requestId", rental.getRentalId());
            docData.put("rentalId", rental.getRentalId());
            docData.put("id", rental.getRentalId());
            docData.put("listingId", rental.getItemId() != null ? rental.getItemId() : "");
            docData.put("itemId", rental.getItemId() != null ? rental.getItemId() : "");
            docData.put("itemTitle", rental.getItemTitle() != null ? rental.getItemTitle() : "");
            docData.put("itemCategory", rental.getItemCategory() != null ? rental.getItemCategory() : "");
            docData.put("itemImagePath", rental.getItemImagePath() != null ? rental.getItemImagePath() : "");

            docData.put("ownerId", rental.getOwnerId() != null ? rental.getOwnerId() : "");
            docData.put("ownerUid", rental.getOwnerId() != null ? rental.getOwnerId() : "");
            docData.put("ownerName", rental.getOwnerName() != null ? rental.getOwnerName() : "");
            docData.put("ownerPhone", rental.getOwnerPhone() != null ? rental.getOwnerPhone() : "");

            docData.put("studentId", rental.getStudentId() != null ? rental.getStudentId() : "");
            docData.put("userId", rental.getStudentId() != null ? rental.getStudentId() : "");
            docData.put("userUid", rental.getStudentId() != null ? rental.getStudentId() : "");
            docData.put("studentName", rental.getStudentName() != null ? rental.getStudentName() : "");
            docData.put("studentEmail", rental.getStudentEmail() != null ? rental.getStudentEmail() : "");
            docData.put("studentPhone", rental.getStudentPhone() != null ? rental.getStudentPhone() : "");

            docData.put("rentType", rental.getRentType() != null ? rental.getRentType() : "Monthly");
            docData.put("startDate", rental.getStartDate() != null ? rental.getStartDate().toString() : LocalDate.now().toString());
            docData.put("endDate", rental.getEndDate() != null ? rental.getEndDate().toString() : LocalDate.now().plusMonths(1).toString());
            docData.put("duration", rental.getDuration());
            docData.put("durationUnit", rental.getDurationUnit() != null ? rental.getDurationUnit() : "Months");

            docData.put("rentAmount", rental.getRentAmount());
            docData.put("securityDeposit", rental.getSecurityDeposit());
            docData.put("totalAmount", rental.getTotalAmount());

            docData.put("paymentStatus", rental.getPaymentStatus() != null ? rental.getPaymentStatus() : "UNPAID");
            docData.put("rentalStatus", rental.getRentalStatus() != null ? rental.getRentalStatus() : "REQUESTED");
            docData.put("status", rental.getRentalStatus() != null ? rental.getRentalStatus() : "REQUESTED");

            if (rental.getExtensionDuration() != null) {
                docData.put("extensionDuration", rental.getExtensionDuration());
            }
            if (rental.getExtensionStatus() != null) {
                docData.put("extensionStatus", rental.getExtensionStatus());
            }
            if (rental.getNewEndDate() != null) {
                docData.put("newEndDate", rental.getNewEndDate().toString());
            }

            docData.put("timestamp", System.currentTimeMillis());
            docData.put("createdAt", rental.getCreatedAt() != null ? rental.getCreatedAt().toString() : LocalDate.now().toString());
            docData.put("updatedAt", LocalDate.now().toString());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(rental.getRentalId().trim()).set(docData);
            result.get();
            System.out.println("[RentalDAOImpl] Rental saved successfully to Firestore: " + rental.getRentalId() + " (ownerId: " + rental.getOwnerId() + ", studentId: " + rental.getStudentId() + ")");

            // Notify owner
            if (rental.getOwnerId() != null && !rental.getOwnerId().isEmpty()) {
                notificationDAO.save(new Notification(
                    "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                    rental.getOwnerId(),
                    "New Rental Request",
                    "Student " + rental.getStudentName() + " requested to rent '" + rental.getItemTitle() + "'",
                    "RENTAL",
                    false,
                    System.currentTimeMillis()
                ));
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error saving rental to Firestore: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(String rentalId, String status) {
        if (rentalId == null || rentalId.trim().isEmpty() || status == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> update = new HashMap<>();
            String cleanStatus = status.trim().toUpperCase();
            update.put("rentalStatus", cleanStatus);
            update.put("status", cleanStatus);
            update.put("updatedAt", LocalDate.now().toString());
            if ("ACCEPTED".equalsIgnoreCase(cleanStatus) || "ACTIVE".equalsIgnoreCase(cleanStatus)) {
                update.put("paymentStatus", "PAID");
            }
            db.collection(COLLECTION_NAME).document(rentalId.trim()).update(update).get();
            System.out.println("[RentalDAOImpl] Updated rental status: " + rentalId + " -> " + cleanStatus);

            Optional<Rental> opt = findById(rentalId.trim());
            if (opt.isPresent()) {
                Rental r = opt.get();
                String targetUid = r.getStudentId();
                if (targetUid != null && !targetUid.isEmpty()) {
                    String title = "ACCEPTED".equals(cleanStatus) ? "Rental Request Accepted!" : ("REJECTED".equals(cleanStatus) ? "Rental Request Rejected" : "Rental Status Updated");
                    String msg = "Your rental request for '" + r.getItemTitle() + "' status is now: " + cleanStatus;
                    notificationDAO.save(new Notification(
                        "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                        targetUid,
                        title,
                        msg,
                        "RENTAL",
                        false,
                        System.currentTimeMillis()
                    ));
                }
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error updating rental status: " + e.getMessage());
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
            System.out.println("[RentalDAOImpl] Rental deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[RentalDAOImpl] Error deleting rental: " + e.getMessage());
            return false;
        }
    }

    private Rental parseRentalFromSnapshot(DocumentSnapshot doc) {
        String rentalId = doc.getString("rentalId");
        if (rentalId == null) rentalId = doc.getString("requestId");
        if (rentalId == null) rentalId = doc.getString("id");
        if (rentalId == null) rentalId = doc.getId();

        String itemId = doc.getString("itemId");
        if (itemId == null) itemId = doc.getString("listingId");
        String itemTitle = doc.getString("itemTitle");
        String itemCategory = doc.getString("itemCategory");
        String itemImagePath = doc.getString("itemImagePath");

        String ownerId = doc.getString("ownerId");
        if (ownerId == null || ownerId.trim().isEmpty()) ownerId = doc.getString("ownerUid");
        String ownerName = doc.getString("ownerName");
        String ownerPhone = doc.getString("ownerPhone");

        String studentId = doc.getString("studentId");
        if (studentId == null || studentId.trim().isEmpty()) studentId = doc.getString("userId");
        if (studentId == null || studentId.trim().isEmpty()) studentId = doc.getString("userUid");
        String studentName = doc.getString("studentName");
        String studentEmail = doc.getString("studentEmail");
        String studentPhone = doc.getString("studentPhone");

        String rentType = doc.getString("rentType");
        String startStr = doc.getString("startDate");
        String endStr = doc.getString("endDate");

        LocalDate startDate = LocalDate.now();
        if (startStr != null && !startStr.isEmpty()) {
            try { startDate = LocalDate.parse(startStr); } catch (Exception ignored) {}
        }

        LocalDate endDate = LocalDate.now().plusMonths(1);
        if (endStr != null && !endStr.isEmpty()) {
            try { endDate = LocalDate.parse(endStr); } catch (Exception ignored) {}
        }

        Long durVal = doc.getLong("duration");
        int duration = durVal != null ? durVal.intValue() : 1;
        String durationUnit = doc.getString("durationUnit");

        Double rentAmount = doc.getDouble("rentAmount");
        Double securityDeposit = doc.getDouble("securityDeposit");
        Double totalAmount = doc.getDouble("totalAmount");

        String paymentStatus = doc.getString("paymentStatus");
        String rentalStatus = doc.getString("rentalStatus");
        if (rentalStatus == null) rentalStatus = doc.getString("status");

        Rental r = new Rental(
            rentalId,
            itemId != null ? itemId : "",
            itemTitle != null ? itemTitle : "Rental Item",
            itemCategory != null ? itemCategory : "Rooms & PG",
            itemImagePath != null ? itemImagePath : "assets/image/room_single.png",
            ownerId != null ? ownerId : "",
            ownerName != null ? ownerName : "Owner",
            ownerPhone != null ? ownerPhone : "",
            studentId != null ? studentId : "",
            studentName != null ? studentName : "Student",
            studentEmail != null ? studentEmail : "",
            studentPhone != null ? studentPhone : "",
            rentType != null ? rentType : "Monthly",
            startDate,
            endDate,
            duration,
            durationUnit != null ? durationUnit : "Months",
            rentAmount != null ? rentAmount : 0.0,
            securityDeposit != null ? securityDeposit : 0.0,
            totalAmount != null ? totalAmount : 0.0,
            paymentStatus != null ? paymentStatus : "UNPAID",
            rentalStatus != null ? rentalStatus : "REQUESTED"
        );

        Long extDur = doc.getLong("extensionDuration");
        if (extDur != null) r.setExtensionDuration(extDur.intValue());
        String extStatus = doc.getString("extensionStatus");
        if (extStatus != null) r.setExtensionStatus(extStatus);
        String newEndStr = doc.getString("newEndDate");
        if (newEndStr != null) {
            try { r.setNewEndDate(LocalDate.parse(newEndStr)); } catch (Exception ignored) {}
        }

        return r;
    }
}
