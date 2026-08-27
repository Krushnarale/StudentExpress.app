package com.core2web.controller;

import com.core2web.dao.NotificationDAO;
import com.core2web.dao.NotificationDAOImpl;
import com.core2web.dao.RoomDAO;
import com.core2web.dao.RoomDAOImpl;
import com.core2web.model.Notification;
import com.core2web.model.RoomItem;
import com.core2web.service.CloudinaryService;
import com.core2web.util.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OwnerController {

    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    public List<RoomItem> getOwnerRooms() {
        String uid = SessionManager.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return roomDAO.findByOwnerUid(uid.trim());
    }

    public CloudinaryService.UploadResult uploadRoomImage(File file) {
        return CloudinaryService.uploadImage(file, "roomImages");
    }

    public boolean addRoomListing(RoomItem room) {
        String uid = SessionManager.getInstance().getUid();
        if (uid != null && !uid.trim().isEmpty()) {
            room.setOwnerUid(uid.trim());
        }
        boolean saved = roomDAO.save(room);
        if (saved && uid != null) {
            notificationDAO.save(new Notification(
                    "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                    uid,
                    "New Room Listed",
                    "Your room '" + room.getTitle() + "' was successfully listed.",
                    "ROOM",
                    false,
                    System.currentTimeMillis()
            ));
        }
        return saved;
    }

    public boolean updateRoomListing(RoomItem room) {
        return roomDAO.save(room);
    }

    public boolean removeRoomListing(String roomId) {
        return roomDAO.delete(roomId);
    }
}
