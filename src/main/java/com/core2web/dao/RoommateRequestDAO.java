package com.core2web.dao;

import com.core2web.model.RoommateRequest;
import java.util.List;
import java.util.Optional;

public interface RoommateRequestDAO {
    Optional<RoommateRequest> findById(String id);
    List<RoommateRequest> findAll();
    List<RoommateRequest> findByReceiverUid(String receiverUid);
    List<RoommateRequest> findBySenderUid(String senderUid);
    boolean save(RoommateRequest request);
    boolean updateStatus(String requestId, String status);
    boolean delete(String id);
}
