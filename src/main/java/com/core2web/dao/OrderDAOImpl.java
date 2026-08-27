package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.Notification;
import com.core2web.model.Order;
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

public class OrderDAOImpl implements OrderDAO {

    private static final String COLLECTION_NAME = "orders";
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Firestore db = getFirestore();
        if (db == null) return Optional.empty();

        try {
            DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id.trim()).get().get();
            if (snapshot != null && snapshot.exists()) {
                return Optional.of(parseOrderFromSnapshot(snapshot));
            }
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error finding order by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseOrderFromSnapshot(doc));
            }
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error fetching all orders: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Order> findByBuyerUid(String buyerUid) {
        List<Order> list = new ArrayList<>();
        if (buyerUid == null || buyerUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("buyerUid", buyerUid.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseOrderFromSnapshot(doc));
            }
            if (list.isEmpty()) {
                ApiFuture<QuerySnapshot> altFuture = db.collection(COLLECTION_NAME).whereEqualTo("buyerId", buyerUid.trim()).get();
                for (DocumentSnapshot doc : altFuture.get().getDocuments()) {
                    list.add(parseOrderFromSnapshot(doc));
                }
            }
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error fetching orders by buyer UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Order> findBySellerUid(String sellerUid) {
        List<Order> list = new ArrayList<>();
        if (sellerUid == null || sellerUid.trim().isEmpty()) return list;
        Firestore db = getFirestore();
        if (db == null) return list;

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("sellerUid", sellerUid.trim()).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                list.add(parseOrderFromSnapshot(doc));
            }
            if (list.isEmpty()) {
                ApiFuture<QuerySnapshot> altFuture = db.collection(COLLECTION_NAME).whereEqualTo("sellerId", sellerUid.trim()).get();
                for (DocumentSnapshot doc : altFuture.get().getDocuments()) {
                    list.add(parseOrderFromSnapshot(doc));
                }
            }
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error fetching orders by seller UID: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateStatus(String id, String status) {
        if (id == null || id.trim().isEmpty() || status == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            String cleanStatus = status.trim().toUpperCase();
            Map<String, Object> update = new HashMap<>();
            update.put("status", cleanStatus);
            update.put("updatedAt", System.currentTimeMillis());
            db.collection(COLLECTION_NAME).document(id.trim()).update(update).get();
            System.out.println("[OrderDAOImpl] Order status updated: " + id + " -> " + cleanStatus);

            Optional<Order> opt = findById(id.trim());
            if (opt.isPresent()) {
                Order order = opt.get();
                if (order.getBuyerUid() != null && !order.getBuyerUid().isEmpty()) {
                    String title = "SHIPPED".equals(cleanStatus) ? "Order Shipped!" : "Order Status Updated";
                    String msg = "Your order for '" + order.getProductTitle() + "' (Tracking ID: " + order.getTrackingId() + ") is now " + cleanStatus;
                    notificationDAO.save(new Notification(
                            "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                            order.getBuyerUid(),
                            title,
                            msg,
                            "ORDER",
                            false,
                            System.currentTimeMillis()
                    ));
                }
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error updating order status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean save(Order order) {
        if (order == null || order.getId() == null) return false;
        Firestore db = getFirestore();
        if (db == null) return false;

        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("orderId", order.getId());
            docData.put("id", order.getId());
            docData.put("buyerUid", order.getBuyerUid() != null ? order.getBuyerUid() : "");
            docData.put("buyerId", order.getBuyerUid() != null ? order.getBuyerUid() : "");
            docData.put("sellerUid", order.getSellerUid() != null ? order.getSellerUid() : "");
            docData.put("sellerId", order.getSellerUid() != null ? order.getSellerUid() : "");
            docData.put("productId", order.getProductId() != null ? order.getProductId() : "");
            docData.put("productTitle", order.getProductTitle() != null ? order.getProductTitle() : "");
            docData.put("price", order.getPrice() != null ? order.getPrice() : "");
            docData.put("trackingId", order.getTrackingId() != null ? order.getTrackingId() : "");
            docData.put("status", order.getStatus() != null ? order.getStatus() : "PLACED");
            docData.put("category", order.getCategory() != null ? order.getCategory() : "");
            docData.put("createdAt", order.getCreatedAt() > 0 ? order.getCreatedAt() : System.currentTimeMillis());

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(order.getId().trim()).set(docData);
            result.get();
            System.out.println("[OrderDAOImpl] Order saved successfully to Firestore: " + order.getId());

            if (order.getSellerUid() != null && !order.getSellerUid().isEmpty()) {
                notificationDAO.save(new Notification(
                        "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                        order.getSellerUid(),
                        "New Product Order!",
                        "Your item '" + order.getProductTitle() + "' has been ordered. Tracking ID: " + order.getTrackingId(),
                        "ORDER",
                        false,
                        System.currentTimeMillis()
                ));
            }
            return true;
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error saving order to Firestore: " + e.getMessage());
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
            System.out.println("[OrderDAOImpl] Order deleted from Firestore: " + id);
            return true;
        } catch (Throwable e) {
            System.err.println("[OrderDAOImpl] Error deleting order: " + e.getMessage());
            return false;
        }
    }

    private Order parseOrderFromSnapshot(DocumentSnapshot doc) {
        String id = doc.getString("orderId");
        if (id == null) id = doc.getString("id");
        if (id == null) id = doc.getId();

        String productTitle = doc.getString("productTitle");
        if (productTitle == null) productTitle = doc.getString("itemName");

        String price = doc.getString("price");
        String date = doc.getString("date");
        String status = doc.getString("status");
        String trackingId = doc.getString("trackingId");
        String category = doc.getString("category");

        String buyerUid = doc.getString("buyerUid");
        if (buyerUid == null) buyerUid = doc.getString("buyerId");

        String sellerUid = doc.getString("sellerUid");
        if (sellerUid == null) sellerUid = doc.getString("sellerId");

        String productId = doc.getString("productId");
        Long createdAt = doc.getLong("createdAt");

        return new Order(
            id,
            productTitle != null ? productTitle : "Item",
            price != null ? price : "₹ 0",
            date != null ? date : "Today",
            status != null ? status : "PLACED",
            trackingId != null ? trackingId : "STX-20260826-1001",
            category != null ? category : "General",
            buyerUid,
            sellerUid,
            productId,
            createdAt != null ? createdAt : System.currentTimeMillis()
        );
    }
}
