package com.core2web.dao;

import com.core2web.model.Notification;
import java.util.List;

public interface NotificationDAO {
    boolean save(Notification notification);
    List<Notification> findByUid(String uid);
    int getUnreadCount(String uid);
    boolean markAsRead(String notificationId);
    boolean delete(String notificationId);
}
