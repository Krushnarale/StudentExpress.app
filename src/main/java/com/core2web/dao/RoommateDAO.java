package com.core2web.dao;

import com.core2web.model.RoommateItem;
import java.util.List;
import java.util.Optional;

public interface RoommateDAO {
    Optional<RoommateItem> findById(String id);
    Optional<RoommateItem> findByUserUid(String userUid);
    List<RoommateItem> findAll();
    boolean save(RoommateItem roommate);
    boolean delete(String id);
}
