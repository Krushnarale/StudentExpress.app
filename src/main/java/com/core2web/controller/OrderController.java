package com.core2web.controller;

import com.core2web.dao.*;
import com.core2web.model.Notification;
import com.core2web.model.Order;
import com.core2web.util.SessionManager;
import javafx.concurrent.Task;
import java.util.List;

public class OrderController {

    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public Task<List<Order>> fetchBuyerOrdersTask() {
        return new Task<>() {
            @Override
            protected List<Order> call() {
                String uid = SessionManager.getInstance().getUid();
                return orderDAO.findByBuyerUid(uid);
            }
        };
    }

    public Task<List<Order>> fetchSellerOrdersTask() {
        return new Task<>() {
            @Override
            protected List<Order> call() {
                String uid = SessionManager.getInstance().getUid();
                return orderDAO.findBySellerUid(uid);
            }
        };
    }

    public boolean createOrder(Order order) {
        return addOrder(order);
    }

    public boolean addOrder(Order order) {
        boolean saved = orderDAO.save(order);
        if (saved && order != null) {
            if (order.getSellerUid() != null && !order.getSellerUid().isEmpty()) {
                notificationDAO.save(new Notification(
                    "notif-" + System.currentTimeMillis(),
                    order.getSellerUid(),
                    "New Order Received",
                    "You have a new order for " + order.getItemName() + " (Tracking: " + order.getTrackingId() + ")",
                    "ORDER",
                    false,
                    System.currentTimeMillis()
                ));
            }
        }
        return saved;
    }

    public List<Order> getSellerOrders() {
        String uid = SessionManager.getInstance().getUid();
        return orderDAO.findBySellerUid(uid);
    }

    public boolean updateOrderStatus(String orderId, String status) {
        return updateOrderStatus(orderId, status, null);
    }

    public boolean updateOrderStatus(String orderId, String status, Order order) {
        boolean updated = orderDAO.updateStatus(orderId, status);
        if (updated && order != null && order.getBuyerUid() != null) {
            notificationDAO.save(new Notification(
                "notif-" + System.currentTimeMillis(),
                order.getBuyerUid(),
                "Order Status Update",
                "Your order " + order.getTrackingId() + " status is now " + status,
                "ORDER",
                false,
                System.currentTimeMillis()
            ));
        }
        return updated;
    }
}
