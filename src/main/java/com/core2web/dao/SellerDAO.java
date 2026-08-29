package com.core2web.dao;

import com.core2web.model.SellerProfile;
import java.util.List;
import java.util.Optional;

public interface SellerDAO {
    Optional<SellerProfile> findBySellerId(String sellerId);
    List<SellerProfile> findAll();
    boolean save(SellerProfile profile);
    boolean delete(String sellerId);
}
