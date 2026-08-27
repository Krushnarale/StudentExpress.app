package com.core2web.controller;

import com.core2web.dao.RoommateDAO;
import com.core2web.dao.RoommateDAOImpl;
import com.core2web.model.RoommateItem;
import com.core2web.service.CloudinaryService;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class RoommateController {

    private final RoommateDAO roommateDAO = new RoommateDAOImpl();

    public List<RoommateItem> getAllRoommates() {
        return roommateDAO.findAll();
    }

    public Optional<RoommateItem> getRoommateById(String id) {
        return roommateDAO.findById(id);
    }

    public CloudinaryService.UploadResult uploadProfileImage(File file) {
        return CloudinaryService.uploadImage(file, "profileImages");
    }

    public boolean addRoommate(RoommateItem roommate) {
        return roommateDAO.save(roommate);
    }

    public boolean updateRoommate(RoommateItem roommate) {
        return roommateDAO.save(roommate);
    }

    public boolean removeRoommate(String roommateId) {
        return roommateDAO.delete(roommateId);
    }
}
