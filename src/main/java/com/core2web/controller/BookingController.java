package com.core2web.controller;

import com.core2web.dao.BookingDAO;
import com.core2web.dao.BookingDAOImpl;
import com.core2web.dao.NotificationDAO;
import com.core2web.dao.NotificationDAOImpl;
import com.core2web.model.Booking;
import com.core2web.model.Notification;
import com.core2web.util.SessionManager;
import javafx.concurrent.Task;
import java.util.List;

public class BookingController {

    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public List<Booking> getUserBookings() {
        String uid = SessionManager.getInstance().getUid();
        return bookingDAO.findByUserUid(uid);
    }

    public Task<List<Booking>> fetchUserBookingsTask() {
        return new Task<>() {
            @Override
            protected List<Booking> call() {
                String uid = SessionManager.getInstance().getUid();
                return bookingDAO.findByUserUid(uid);
            }
        };
    }

    public Task<List<Booking>> fetchOwnerBookingsTask() {
        return new Task<>() {
            @Override
            protected List<Booking> call() {
                String uid = SessionManager.getInstance().getUid();
                return bookingDAO.findByOwnerUid(uid);
            }
        };
    }

    public Task<List<Booking>> fetchProviderBookingsTask() {
        return new Task<>() {
            @Override
            protected List<Booking> call() {
                String uid = SessionManager.getInstance().getUid();
                return bookingDAO.findByProviderUid(uid);
            }
        };
    }

    public List<Booking> getOwnerBookings() {
        String uid = SessionManager.getInstance().getUid();
        return bookingDAO.findByOwnerUid(uid);
    }

    public List<Booking> getProviderBookings() {
        String uid = SessionManager.getInstance().getUid();
        return bookingDAO.findByProviderUid(uid);
    }

    public boolean createBooking(Booking booking) {
        return addBooking(booking);
    }

    public boolean addBooking(Booking booking) {
        boolean saved = bookingDAO.save(booking);
        if (saved && booking != null) {
            // Notify owner or provider
            String notifyTarget = booking.getOwnerUid() != null ? booking.getOwnerUid() : booking.getProviderId();
            if (notifyTarget != null && !notifyTarget.isEmpty()) {
                notificationDAO.save(new Notification(
                    "notif-" + System.currentTimeMillis(),
                    notifyTarget,
                    "New Booking Request",
                    "You received a new booking request for " + booking.getItemOrServiceName(),
                    "BOOKING",
                    false,
                    System.currentTimeMillis()
                ));
            }
        }
        return saved;
    }

    public boolean updateBookingStatus(String bookingId, String status) {
        return updateBookingStatus(bookingId, status, null);
    }

    public boolean updateBookingStatus(String bookingId, String status, Booking booking) {
        boolean updated = bookingDAO.updateStatus(bookingId, status);
        if (updated && booking != null && booking.getUserUid() != null) {
            notificationDAO.save(new Notification(
                "notif-" + System.currentTimeMillis(),
                booking.getUserUid(),
                "Booking Update",
                "Your booking for " + booking.getItemOrServiceName() + " status changed to " + status,
                "BOOKING",
                false,
                System.currentTimeMillis()
            ));
        }
        return updated;
    }

    public boolean cancelBooking(String bookingId) {
        return bookingDAO.delete(bookingId);
    }
}
