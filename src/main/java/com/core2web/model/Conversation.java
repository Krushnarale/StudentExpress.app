package com.core2web.model;

import java.util.*;

public class Conversation {
    private String conversationId;
    private List<String> participants = new ArrayList<>();
    private Map<String, String> participantNames = new HashMap<>();
    private Map<String, String> participantRoles = new HashMap<>();
    private String listingId;
    private String listingType;
    private String listingTitle;
    private String lastMessage;
    private String lastSenderId;
    private long lastMessageTime;
    private Map<String, Long> unreadCounts = new HashMap<>();

    public Conversation() {
        this.lastMessageTime = System.currentTimeMillis();
    }

    public Conversation(
        String conversationId,
        List<String> participants,
        Map<String, String> participantNames,
        Map<String, String> participantRoles,
        String listingId,
        String listingType,
        String listingTitle,
        String lastMessage,
        String lastSenderId,
        long lastMessageTime,
        Map<String, Long> unreadCounts
    ) {
        this.conversationId = conversationId;
        if (participants != null) this.participants = new ArrayList<>(participants);
        if (participantNames != null) this.participantNames = new HashMap<>(participantNames);
        if (participantRoles != null) this.participantRoles = new HashMap<>(participantRoles);
        this.listingId = listingId != null ? listingId : "";
        this.listingType = listingType != null ? listingType : "GENERAL";
        this.listingTitle = listingTitle != null ? listingTitle : "";
        this.lastMessage = lastMessage != null ? lastMessage : "";
        this.lastSenderId = lastSenderId != null ? lastSenderId : "";
        this.lastMessageTime = lastMessageTime > 0 ? lastMessageTime : System.currentTimeMillis();
        if (unreadCounts != null) this.unreadCounts = new HashMap<>(unreadCounts);
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public Map<String, String> getParticipantNames() { return participantNames; }
    public void setParticipantNames(Map<String, String> participantNames) { this.participantNames = participantNames; }

    public Map<String, String> getParticipantRoles() { return participantRoles; }
    public void setParticipantRoles(Map<String, String> participantRoles) { this.participantRoles = participantRoles; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public Map<String, Long> getUnreadCounts() { return unreadCounts; }
    public void setUnreadCounts(Map<String, Long> unreadCounts) { this.unreadCounts = unreadCounts; }

    // Helpers
    public String getOtherParticipantId(String myUid) {
        if (participants == null || participants.isEmpty()) return "";
        for (String p : participants) {
            if (myUid != null && !p.equalsIgnoreCase(myUid.trim())) {
                return p;
            }
        }
        return participants.get(0);
    }

    public String getOtherParticipantName(String myUid) {
        String otherId = getOtherParticipantId(myUid);
        if (participantNames != null && participantNames.containsKey(otherId)) {
            String name = participantNames.get(otherId);
            if (name != null && !name.trim().isEmpty() && !name.equals("Not provided")) {
                return name;
            }
        }
        return "User";
    }

    public String getOtherParticipantRole(String myUid) {
        String otherId = getOtherParticipantId(myUid);
        if (participantRoles != null && participantRoles.containsKey(otherId)) {
            return participantRoles.get(otherId);
        }
        return "User";
    }

    public int getUnreadForUser(String myUid) {
        if (myUid == null || unreadCounts == null) return 0;
        Long count = unreadCounts.get(myUid.trim());
        if (count == null) count = unreadCounts.get(myUid);
        return count != null ? count.intValue() : 0;
    }
}
