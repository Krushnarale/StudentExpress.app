package com.core2web.model;

import java.time.LocalDate;

public class Rental {
    private String rentalId;
    private String itemId;
    private String itemTitle;
    private String itemCategory;
    private String itemImagePath;

    private String ownerId;
    private String ownerName;
    private String ownerPhone;

    private String studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;

    private String rentType; // "Daily", "Weekly", "Monthly"
    private LocalDate startDate;
    private LocalDate endDate;
    private int duration;
    private String durationUnit; // "Days", "Weeks", "Months"

    private double rentAmount; // Rate per period
    private double securityDeposit;
    private double totalAmount; // (rentAmount * duration) + securityDeposit

    private String paymentStatus; // "UNPAID", "PAID", "DEPOSIT_PAID"
    private String rentalStatus;  // "REQUESTED", "ACCEPTED", "ACTIVE", "EXPIRING_SOON", "EXTENSION_REQUESTED", "COMPLETED", "CANCELLED", "REJECTED"

    private Integer extensionDuration;
    private String extensionStatus;
    private LocalDate newEndDate;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Rental(
        String rentalId,
        String itemId,
        String itemTitle,
        String itemCategory,
        String itemImagePath,
        String ownerId,
        String ownerName,
        String ownerPhone,
        String studentId,
        String studentName,
        String studentEmail,
        String studentPhone,
        String rentType,
        LocalDate startDate,
        LocalDate endDate,
        int duration,
        String durationUnit,
        double rentAmount,
        double securityDeposit,
        double totalAmount,
        String paymentStatus,
        String rentalStatus
    ) {
        this.rentalId = rentalId;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemCategory = itemCategory;
        this.itemImagePath = itemImagePath;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentPhone = studentPhone;
        this.rentType = rentType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.durationUnit = durationUnit;
        this.rentAmount = rentAmount;
        this.securityDeposit = securityDeposit;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.rentalStatus = rentalStatus;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public String getRentalId() { return rentalId; }
    public String getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public String getItemCategory() { return itemCategory; }
    public String getItemImagePath() { return itemImagePath; }

    public String getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }

    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getStudentPhone() { return studentPhone; }

    public String getRentType() { return rentType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getDurationUnit() { return durationUnit; }
    public void setDurationUnit(String durationUnit) { this.durationUnit = durationUnit; }

    public double getRentAmount() { return rentAmount; }
    public double getSecurityDeposit() { return securityDeposit; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getRentTotal() {
        return rentAmount * duration;
    }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getRentalStatus() { return rentalStatus; }
    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
        this.updatedAt = LocalDate.now();
    }

    public Integer getExtensionDuration() { return extensionDuration; }
    public void setExtensionDuration(Integer extensionDuration) { this.extensionDuration = extensionDuration; }

    public String getExtensionStatus() { return extensionStatus; }
    public void setExtensionStatus(String extensionStatus) { this.extensionStatus = extensionStatus; }

    public LocalDate getNewEndDate() { return newEndDate; }
    public void setNewEndDate(LocalDate newEndDate) { this.newEndDate = newEndDate; }

    public LocalDate getCreatedAt() { return createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
}
