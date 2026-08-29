package com.core2web.dao;

import com.core2web.model.ServiceItem;
import java.util.List;
import java.util.Optional;

public interface ServiceDAO {
    Optional<ServiceItem> findById(String id);
    List<ServiceItem> findAll();
    List<ServiceItem> findByProviderUid(String providerUid);
    boolean save(ServiceItem service);
    boolean delete(String id);
}
