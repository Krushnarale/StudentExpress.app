package com.core2web.dao;

import com.core2web.model.ProductItem;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Optional<ProductItem> findById(String id);
    List<ProductItem> findAll();
    List<ProductItem> findBySellerUid(String sellerUid);
    boolean save(ProductItem product);
    boolean updateStatus(String id, String status);
    boolean delete(String id);
}
