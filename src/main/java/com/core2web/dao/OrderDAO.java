package com.core2web.dao;

import com.core2web.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Optional<Order> findById(String id);
    List<Order> findAll();
    List<Order> findByBuyerUid(String buyerUid);
    List<Order> findBySellerUid(String sellerUid);
    boolean updateStatus(String id, String status);
    boolean save(Order order);
    boolean delete(String id);
}
