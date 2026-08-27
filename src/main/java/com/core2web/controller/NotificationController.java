package com.core2web.controller;

import com.core2web.dao.NotificationDAO;
import com.core2web.dao.NotificationDAOImpl;
import com.core2web.model.Notification;
import com.core2web.util.SessionManager;
import javafx.concurrent.Task;

import java.util.List;

public class NotificationController {

    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    public Task<List<Notification>> fetchUserNotificationsTask() {
        return new Task<>() {
            @Override
            protected List<Notification> call() {
                String uid = SessionManager.getInstance().getUid();
                return notificationDAO.findByUid(uid);
            }
        };
    }

    public Task<Integer> fetchUnreadCountTask() {
        return new Task<>() {
            @Override
            protected Integer call() {
                String uid = SessionManager.getInstance().getUid();
                return notificationDAO.getUnreadCount(uid);
            }
        };
    }

    public int getUnreadNotificationCount(String userUid) {
        return notificationDAO.getUnreadCount(userUid);
    }

    public List<Notification> getNotificationsByUser(String userUid) {
        return notificationDAO.findByUid(userUid);
    }

    public boolean markAsRead(String notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    public boolean deleteNotification(String notificationId) {
        return notificationDAO.delete(notificationId);
    }
}
