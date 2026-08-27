package com.core2web.dao;

import com.core2web.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingDAO {
    Optional<Booking> findById(String id);
    List<Booking> findAll();
    List<Booking> findByUserUid(String userUid);
    List<Booking> findByOwnerUid(String ownerUid);
    List<Booking> findByProviderId(String providerId);
    List<Booking> findByProviderUid(String providerUid);
    boolean updateStatus(String id, String status);
    boolean save(Booking booking);
    boolean delete(String id);
}
