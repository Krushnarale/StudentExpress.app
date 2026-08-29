package com.core2web.service;

import com.core2web.model.Rental;
import com.core2web.model.RoomItem;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalService {

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }

    public static LocalDate calculateEndDate(LocalDate startDate, int duration, String rentType) {
        if (startDate == null) return LocalDate.now();
        if (duration <= 0) duration = 1;
        String type = rentType != null ? rentType.toLowerCase() : "monthly";

        if (type.contains("daily") || type.contains("day")) {
            return startDate.plusDays(duration);
        } else if (type.contains("weekly") || type.contains("week")) {
            return startDate.plusWeeks(duration);
        } else {
            // Default Monthly
            return startDate.plusMonths(duration);
        }
    }

    public static ValidationResult validateBooking(RoomItem room, LocalDate startDate, int duration, List<Rental> existingRentals) {
        if (room == null) {
            return new ValidationResult(false, "Selected rental item is invalid.");
        }

        if (startDate == null) {
            return new ValidationResult(false, "Please select a valid start date.");
        }

        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today)) {
            return new ValidationResult(false, "Start date cannot be in the past.");
        }

        if (room.getAvailableFrom() != null && startDate.isBefore(room.getAvailableFrom())) {
            return new ValidationResult(false, "Start date cannot be before Available From date (" + room.getAvailableFrom() + ").");
        }

        if (room.getAvailableUntil() != null && startDate.isAfter(room.getAvailableUntil())) {
            return new ValidationResult(false, "Start date is beyond owner's Available Until date (" + room.getAvailableUntil() + ").");
        }

        if (duration < room.getMinDuration()) {
            return new ValidationResult(false, "Duration must be at least " + room.getMinDuration() + " " + room.getRentType() + "(s).");
        }

        if (room.getMaxDuration() != null && room.getMaxDuration() > 0 && duration > room.getMaxDuration()) {
            return new ValidationResult(false, "Duration cannot exceed maximum allowed stay of " + room.getMaxDuration() + " " + room.getRentType() + "(s).");
        }

        if ("MAINTENANCE".equalsIgnoreCase(room.getAvailabilityStatus())) {
            return new ValidationResult(false, "This item is currently under maintenance and unavailable for rent.");
        }

        LocalDate endDate = calculateEndDate(startDate, duration, room.getRentType());

        if (checkOverlappingPeriod(existingRentals, room.getId(), startDate, endDate, null)) {
            return new ValidationResult(false, "Selected period (" + startDate + " to " + endDate + ") overlaps with an existing confirmed rental for this item.");
        }

        return new ValidationResult(true, "Validation successful");
    }

    public static boolean checkOverlappingPeriod(List<Rental> rentals, String itemId, LocalDate newStart, LocalDate newEnd, String currentRentalId) {
        if (rentals == null || itemId == null || newStart == null || newEnd == null) return false;

        for (Rental r : rentals) {
            if (r.getItemId().equals(itemId)) {
                if (currentRentalId != null && r.getRentalId().equals(currentRentalId)) {
                    continue; // Skip comparing against self when extending
                }
                String status = r.getRentalStatus();
                if ("ACCEPTED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status) || "EXPIRING_SOON".equalsIgnoreCase(status) || "EXTENSION_REQUESTED".equalsIgnoreCase(status)) {
                    LocalDate existStart = r.getStartDate();
                    LocalDate existEnd = r.getEndDate();
                    if (existStart != null && existEnd != null) {
                        // Check date range overlap
                        if (newStart.isBefore(existEnd) && newEnd.isAfter(existStart)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static long calculateDaysRemaining(LocalDate endDate) {
        if (endDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    public static void autoUpdateRentalStatuses(List<Rental> rentals, List<RoomItem> rooms) {
        if (rentals == null) return;
        LocalDate today = LocalDate.now();

        for (Rental r : rentals) {
            String status = r.getRentalStatus();
            LocalDate start = r.getStartDate();
            LocalDate end = r.getEndDate();

            if ("ACCEPTED".equalsIgnoreCase(status)) {
                if (start != null && !start.isAfter(today)) {
                    r.setRentalStatus("ACTIVE");
                    status = "ACTIVE";
                }
            }

            if ("ACTIVE".equalsIgnoreCase(status) || "EXPIRING_SOON".equalsIgnoreCase(status)) {
                if (end != null) {
                    if (end.isBefore(today)) {
                        r.setRentalStatus("COMPLETED");
                        // Free room availability status if no other active rentals
                        updateRoomAvailabilityIfFree(r.getItemId(), rooms, rentals);
                    } else {
                        long daysLeft = ChronoUnit.DAYS.between(today, end);
                        if (daysLeft <= 30) {
                            r.setRentalStatus("EXPIRING_SOON");
                        }
                    }
                }
            }
        }
    }

    private static void updateRoomAvailabilityIfFree(String itemId, List<RoomItem> rooms, List<Rental> rentals) {
        if (rooms == null || itemId == null) return;
        boolean hasActive = false;
        if (rentals != null) {
            for (Rental r : rentals) {
                if (r.getItemId().equals(itemId)) {
                    String st = r.getRentalStatus();
                    if ("ACCEPTED".equals(st) || "ACTIVE".equals(st) || "EXPIRING_SOON".equals(st) || "EXTENSION_REQUESTED".equals(st)) {
                        hasActive = true;
                        break;
                    }
                }
            }
        }

        if (!hasActive) {
            for (RoomItem room : rooms) {
                if (room.getId().equals(itemId)) {
                    if ("CURRENTLY_RENTED".equalsIgnoreCase(room.getAvailabilityStatus())) {
                        room.setAvailabilityStatus("AVAILABLE");
                    }
                    break;
                }
            }
        }
    }
}
