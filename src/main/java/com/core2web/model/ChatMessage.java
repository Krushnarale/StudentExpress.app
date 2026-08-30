package com.core2web.model;

public class ChatMessage {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String text;
    private long timestamp;
    private boolean read;
    private String listingId;
    private String listingType;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public ChatMessage(
        String messageId,
        String conversationId,
        String senderId,
        String senderName,
        String receiverId,
        String receiverName,
        String text,
        long timestamp,
        boolean read,
        String listingId,
        String listingType
    ) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.text = text;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.read = read;
        this.listingId = listingId != null ? listingId : "";
        this.listingType = listingType != null ? listingType : "GENERAL";
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName != null && !senderName.isEmpty() ? senderName : "User"; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName != null && !receiverName.isEmpty() ? receiverName : "User"; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }
}
