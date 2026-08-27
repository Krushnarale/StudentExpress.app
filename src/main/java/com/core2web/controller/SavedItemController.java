package com.core2web.controller;

import com.core2web.dao.SavedItemDAO;
import com.core2web.dao.SavedItemDAOImpl;
import java.util.Set;

public class SavedItemController {

    private final SavedItemDAO savedItemDAO = new SavedItemDAOImpl();

    public Set<String> getSavedRoomIds() {
        return savedItemDAO.getSavedRoomIds();
    }

    public Set<String> getSavedProductIds() {
        return savedItemDAO.getSavedProductIds();
    }

    public boolean toggleSavedRoom(String roomId) {
        return savedItemDAO.toggleSavedRoom(roomId);
    }

    public boolean toggleSavedProduct(String productId) {
        return savedItemDAO.toggleSavedProduct(productId);
    }
}
