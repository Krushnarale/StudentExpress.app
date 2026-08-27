package com.core2web.controller;

import com.core2web.dao.*;
import com.core2web.model.User;
import java.util.List;

public class AdminController {

    private final UserDAO userDAO = new UserDAOImpl();
    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final ServiceDAO serviceDAO = new ServiceDAOImpl();
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public int getTotalUsersCount() {
        return userDAO.findAll().size();
    }

    public int getTotalRoomsCount() {
        return roomDAO.findAll().size();
    }

    public int getTotalProductsCount() {
        return productDAO.findAll().size();
    }

    public int getTotalServicesCount() {
        return serviceDAO.findAll().size();
    }

    public int getTotalOrdersCount() {
        return orderDAO.findAll().size();
    }

    public int getTotalBookingsCount() {
        return bookingDAO.findAll().size();
    }
}
