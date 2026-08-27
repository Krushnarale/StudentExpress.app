package com.core2web.dao;

import com.core2web.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(String id);
    Optional<User> findByUid(String uid);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    boolean save(User user);
    boolean saveWithAuth(User user, String password);
    boolean delete(String id);
}
