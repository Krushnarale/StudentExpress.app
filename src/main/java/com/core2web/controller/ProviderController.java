package com.core2web.controller;

import com.core2web.dao.ServiceDAO;
import com.core2web.dao.ServiceDAOImpl;
import com.core2web.model.ServiceItem;
import com.core2web.service.CloudinaryService;
import com.core2web.util.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProviderController {

    private final ServiceDAO serviceDAO = new ServiceDAOImpl();

    public List<ServiceItem> getProviderServices() {
        String uid = SessionManager.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return serviceDAO.findByProviderUid(uid.trim());
    }

    public CloudinaryService.UploadResult uploadServiceImage(File file) {
        return CloudinaryService.uploadImage(file, "serviceImages");
    }

    public boolean addServiceListing(ServiceItem service) {
        String uid = SessionManager.getInstance().getUid();
        if (uid != null && !uid.trim().isEmpty()) {
            service.setProviderUid(uid.trim());
        }
        return serviceDAO.save(service);
    }

    public boolean updateServiceListing(ServiceItem service) {
        return serviceDAO.save(service);
    }

    public boolean removeServiceListing(String serviceId) {
        return serviceDAO.delete(serviceId);
    }
}
