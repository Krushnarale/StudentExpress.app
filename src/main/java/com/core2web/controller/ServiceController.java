package com.core2web.controller;

import com.core2web.dao.ServiceDAO;
import com.core2web.dao.ServiceDAOImpl;
import com.core2web.model.ServiceItem;
import java.util.List;
import java.util.Optional;

public class ServiceController {

    private final ServiceDAO serviceDAO = new ServiceDAOImpl();

    public List<ServiceItem> getAllServices() {
        return serviceDAO.findAll();
    }

    public List<ServiceItem> getServicesByProvider(String providerUid) {
        return serviceDAO.findByProviderUid(providerUid);
    }

    public Optional<ServiceItem> getServiceById(String id) {
        return serviceDAO.findById(id);
    }

    public boolean addService(ServiceItem service) {
        return serviceDAO.save(service);
    }

    public boolean removeService(String serviceId) {
        return serviceDAO.delete(serviceId);
    }
}
