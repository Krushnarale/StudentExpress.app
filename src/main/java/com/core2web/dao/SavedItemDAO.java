package com.core2web.dao;

import java.util.Set;

public interface SavedItemDAO {
    Set<String> getSavedRoomIds();
    Set<String> getSavedProductIds();
    Set<String> getSavedRoomIds(String userUid);
    Set<String> getSavedProductIds(String userUid);
    boolean toggleSavedRoom(String roomId);
    boolean toggleSavedProduct(String productId);
    boolean toggleSavedRoom(String userUid, String roomId);
    boolean toggleSavedProduct(String userUid, String productId);
}
