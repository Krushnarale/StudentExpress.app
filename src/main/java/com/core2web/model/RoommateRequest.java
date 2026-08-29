package com.core2web.model;

public class RoommateRequest {
    private String requestId;
    private String senderStudentId; // sender student's Firebase UID
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String receiverStudentId; // target roommate's Firebase UID
    private String receiverName;
    private String roommateProfileId;
    private String status; // PENDING, ACCEPTED, REJECTED
    private String message;
    private long timestamp;

    public RoommateRequest() {
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }

    public RoommateRequest(
        String requestId,
        String senderStudentId,
        String senderName,
        String senderEmail,
        String senderPhone,
        String receiverStudentId,
        String receiverName,
        String roommateProfileId,
        String status,
        String message,
        long timestamp
    ) {
        this.requestId = requestId;
        this.senderStudentId = senderStudentId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderPhone = senderPhone;
        this.receiverStudentId = receiverStudentId;
        this.receiverName = receiverName;
        this.roommateProfileId = roommateProfileId;
        this.status = status != null ? status : "PENDING";
        this.message = message != null ? message : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getSenderStudentId() { return senderStudentId; }
    public void setSenderStudentId(String senderStudentId) { this.senderStudentId = senderStudentId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public String getReceiverStudentId() { return receiverStudentId; }
    public void setReceiverStudentId(String receiverStudentId) { this.receiverStudentId = receiverStudentId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getRoommateProfileId() { return roommateProfileId; }
    public void setRoommateProfileId(String roommateProfileId) { this.roommateProfileId = roommateProfileId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
