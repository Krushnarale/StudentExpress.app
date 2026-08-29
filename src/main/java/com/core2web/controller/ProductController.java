package com.core2web.controller;

import com.core2web.dao.ProductDAO;
import com.core2web.dao.ProductDAOImpl;
import com.core2web.model.ProductItem;
import java.util.List;
import java.util.Optional;

public class ProductController {

    private final ProductDAO productDAO = new ProductDAOImpl();

    public List<ProductItem> getAllProducts() {
        return productDAO.findAll();
    }

    public List<ProductItem> getProductsBySeller(String sellerUid) {
        return productDAO.findBySellerUid(sellerUid);
    }

    public Optional<ProductItem> getProductById(String id) {
        return productDAO.findById(id);
    }

    public boolean addProduct(ProductItem product) {
        return productDAO.save(product);
    }

    public boolean removeProduct(String productId) {
        return productDAO.delete(productId);
    }
}
