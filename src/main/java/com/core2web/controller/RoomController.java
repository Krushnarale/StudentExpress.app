package com.core2web.controller;

import com.core2web.dao.RoomDAO;
import com.core2web.dao.RoomDAOImpl;
import com.core2web.model.RoomItem;
import java.util.List;
import java.util.Optional;

public class RoomController {

    private final RoomDAO roomDAO = new RoomDAOImpl();

    public List<RoomItem> getAllRooms() {
        return roomDAO.findAll();
    }

    public List<RoomItem> getRoomsByOwner(String ownerUid) {
        return roomDAO.findByOwnerUid(ownerUid);
    }

    public Optional<RoomItem> getRoomById(String id) {
        return roomDAO.findById(id);
    }

    public boolean addRoom(RoomItem room) {
        return roomDAO.save(room);
    }

    public boolean removeRoom(String roomId) {
        return roomDAO.delete(roomId);
    }
}
