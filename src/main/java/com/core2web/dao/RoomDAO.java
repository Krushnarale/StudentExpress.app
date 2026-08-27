package com.core2web.dao;

import com.core2web.model.RoomItem;
import java.util.List;
import java.util.Optional;

public interface RoomDAO {
    Optional<RoomItem> findById(String id);
    List<RoomItem> findAll();
    List<RoomItem> findByOwnerUid(String ownerUid);
    boolean save(RoomItem room);
    boolean delete(String id);
}
