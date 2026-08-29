package com.core2web.dao;

import com.core2web.model.Rental;
import java.util.List;
import java.util.Optional;

public interface RentalDAO {
    Optional<Rental> findById(String id);
    List<Rental> findAll();
    List<Rental> findByOwnerId(String ownerId);
    List<Rental> findByStudentId(String studentId);
    boolean save(Rental rental);
    boolean updateStatus(String rentalId, String status);
    boolean delete(String id);
}
