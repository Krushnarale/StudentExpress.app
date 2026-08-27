package com.core2web.controller;

import com.core2web.dao.NotificationDAO;
import com.core2web.dao.NotificationDAOImpl;
import com.core2web.dao.ProductDAO;
import com.core2web.dao.ProductDAOImpl;
import com.core2web.model.Notification;
import com.core2web.model.ProductItem;
import com.core2web.service.CloudinaryService;
import com.core2web.util.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SellerController {

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    public List<ProductItem> getSellerProducts() {
        String uid = SessionManager.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return productDAO.findBySellerUid(uid.trim());
    }

    public CloudinaryService.UploadResult uploadProductImage(File file) {
        return CloudinaryService.uploadImage(file, "productImages");
    }

    public boolean addProductListing(ProductItem product) {
        String uid = SessionManager.getInstance().getUid();
        if (uid != null && !uid.trim().isEmpty()) {
            product.setSellerUid(uid.trim());
        }
        boolean saved = productDAO.save(product);
        if (saved && uid != null) {
            notificationDAO.save(new Notification(
                    "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4),
                    uid,
                    "New Product Listed",
                    "Your product '" + product.getTitle() + "' was successfully listed.",
                    "PRODUCT",
                    false,
                    System.currentTimeMillis()
            ));
        }
        return saved;
    }

    public boolean updateProductListing(ProductItem product) {
        return productDAO.save(product);
    }

    public boolean deleteProductListing(String productId) {
        return productDAO.delete(productId);
    }
}
