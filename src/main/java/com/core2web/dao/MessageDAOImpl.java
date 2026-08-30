package com.core2web.dao;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.ChatMessage;
import com.core2web.model.Conversation;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MessageDAOImpl implements MessageDAO {

    private static final String CONVERSATIONS_COLLECTION = "conversations";
    private static final String MESSAGES_SUBCOLLECTION = "messages";

    // In-memory caching for instant local responsiveness & offline fallback
    private static final Map<String, Conversation> conversationCache = new ConcurrentHashMap<>();
    private static final Map<String, List<ChatMessage>> messageCache = new ConcurrentHashMap<>();

    private Firestore getFirestore() {
        return FirebaseConfig.getFirestore();
    }

    @Override
    public boolean saveMessage(ChatMessage message) {
        if (message == null || message.getConversationId() == null || message.getConversationId().trim().isEmpty()) {
            return false;
        }

        if (message.getMessageId() == null || message.getMessageId().trim().isEmpty()) {
            message.setMessageId("msg_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));
        }
        if (message.getTimestamp() <= 0) {
            message.setTimestamp(System.currentTimeMillis());
        }

        String convId = message.getConversationId().trim();

        // 1. Update in-memory message cache
        messageCache.computeIfAbsent(convId, k -> new CopyOnWriteArrayList<>()).add(message);

        // 2. Update in-memory conversation cache
        Conversation cachedConv = conversationCache.get(convId);
        if (cachedConv != null) {
            cachedConv.setLastMessage(message.getText());
            cachedConv.setLastSenderId(message.getSenderId());
            cachedConv.setLastMessageTime(message.getTimestamp());
            if (message.getReceiverId() != null && !message.getReceiverId().isEmpty()) {
                long currentUnread = cachedConv.getUnreadForUser(message.getReceiverId());
                cachedConv.getUnreadCounts().put(message.getReceiverId(), currentUnread + 1);
            }
        }

        // 3. Persist to Firestore asynchronously
        new Thread(() -> {
            Firestore db = getFirestore();
            if (db == null) return;

            try {
                // Save message document in conversations/{convId}/messages/{msgId}
                Map<String, Object> msgData = new HashMap<>();
                msgData.put("messageId", message.getMessageId());
                msgData.put("conversationId", convId);
                msgData.put("senderId", message.getSenderId() != null ? message.getSenderId() : "");
                msgData.put("senderName", message.getSenderName() != null ? message.getSenderName() : "User");
                msgData.put("receiverId", message.getReceiverId() != null ? message.getReceiverId() : "");
                msgData.put("receiverName", message.getReceiverName() != null ? message.getReceiverName() : "User");
                msgData.put("text", message.getText() != null ? message.getText() : "");
                msgData.put("timestamp", message.getTimestamp());
                msgData.put("read", message.isRead());
                msgData.put("listingId", message.getListingId() != null ? message.getListingId() : "");
                msgData.put("listingType", message.getListingType() != null ? message.getListingType() : "GENERAL");

                db.collection(CONVERSATIONS_COLLECTION)
                    .document(convId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .document(message.getMessageId())
                    .set(msgData)
                    .get();

                // Update parent conversation summary
                Map<String, Object> convUpdate = new HashMap<>();
                convUpdate.put("lastMessage", message.getText());
                convUpdate.put("lastSenderId", message.getSenderId());
                convUpdate.put("lastMessageTime", message.getTimestamp());

                if (message.getReceiverId() != null && !message.getReceiverId().isEmpty()) {
                    convUpdate.put("unreadCounts." + message.getReceiverId().trim(), FieldValue.increment(1));
                }

                db.collection(CONVERSATIONS_COLLECTION)
                    .document(convId)
                    .set(convUpdate, SetOptions.merge())
                    .get();

                System.out.println("[MessageDAOImpl] Message saved & conversation updated: " + message.getMessageId());
            } catch (Throwable e) {
                System.err.println("[MessageDAOImpl] Error saving message to Firestore: " + e.getMessage());
            }
        }).start();

        return true;
    }

    @Override
    public List<ChatMessage> getMessages(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String convId = conversationId.trim();
        Map<String, ChatMessage> merged = new LinkedHashMap<>();

        List<ChatMessage> cached = messageCache.get(convId);
        if (cached != null) {
            for (ChatMessage m : cached) {
                if (m.getMessageId() != null) {
                    merged.put(m.getMessageId(), m);
                }
            }
        }

        Firestore db = getFirestore();
        if (db != null) {
            try {
                ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .document(convId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get();

                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    ChatMessage msg = parseMessageFromSnapshot(doc);
                    merged.put(msg.getMessageId(), msg);
                }
            } catch (Throwable e) {
                System.err.println("[MessageDAOImpl] Error fetching messages: " + e.getMessage());
            }
        }

        List<ChatMessage> list = new ArrayList<>(merged.values());
        list.sort(Comparator.comparingLong(ChatMessage::getTimestamp));
        messageCache.put(convId, new CopyOnWriteArrayList<>(list));
        return list;
    }

    @Override
    public boolean markMessagesAsRead(String conversationId, String currentUserId) {
        if (conversationId == null || currentUserId == null) return false;
        String convId = conversationId.trim();
        String uid = currentUserId.trim();

        // 1. Update in-memory cache
        Conversation cached = conversationCache.get(convId);
        if (cached != null && cached.getUnreadCounts() != null) {
            cached.getUnreadCounts().put(uid, 0L);
        }
        List<ChatMessage> msgs = messageCache.get(convId);
        if (msgs != null) {
            for (ChatMessage m : msgs) {
                if (uid.equalsIgnoreCase(m.getReceiverId())) {
                    m.setRead(true);
                }
            }
        }

        // 2. Update Firestore
        new Thread(() -> {
            Firestore db = getFirestore();
            if (db == null) return;
            try {
                // Reset unread count for current user in conversation document
                Map<String, Object> convUpdate = new HashMap<>();
                convUpdate.put("unreadCounts." + uid, 0L);
                db.collection(CONVERSATIONS_COLLECTION).document(convId).set(convUpdate, SetOptions.merge()).get();

                // Mark unread messages as read
                ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .document(convId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .whereEqualTo("receiverId", uid)
                    .whereEqualTo("read", false)
                    .get();

                WriteBatch batch = db.batch();
                for (DocumentSnapshot doc : future.get().getDocuments()) {
                    batch.update(doc.getReference(), "read", true);
                }
                batch.commit().get();
            } catch (Throwable e) {
                System.err.println("[MessageDAOImpl] Error marking messages read: " + e.getMessage());
            }
        }).start();

        return true;
    }

    @Override
    public Conversation getOrCreateConversation(
        String user1Id, String user1Name, String user1Role,
        String user2Id, String user2Name, String user2Role,
        String listingId, String listingType, String listingTitle
    ) {
        if (user1Id == null || user2Id == null) return null;
        String u1 = user1Id.trim();
        String u2 = user2Id.trim();
        String lId = listingId != null ? listingId.trim() : "";
        String lType = listingType != null ? listingType.trim() : "GENERAL";
        String lTitle = listingTitle != null ? listingTitle.trim() : "";

        // Check local cache first
        for (Conversation c : conversationCache.values()) {
            if (c.getParticipants().contains(u1) && c.getParticipants().contains(u2)) {
                if (lId.isEmpty() || lId.equalsIgnoreCase(c.getListingId())) {
                    return c;
                }
            }
        }

        // Check Firestore
        Firestore db = getFirestore();
        if (db != null) {
            try {
                ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .whereArrayContains("participants", u1)
                    .get();

                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    Conversation conv = parseConversationFromSnapshot(doc);
                    if (conv.getParticipants().contains(u2)) {
                        if (lId.isEmpty() || lId.equalsIgnoreCase(conv.getListingId())) {
                            conversationCache.put(conv.getConversationId(), conv);
                            return conv;
                        }
                    }
                }
            } catch (Throwable e) {
                System.err.println("[MessageDAOImpl] Error finding conversation: " + e.getMessage());
            }
        }

        // Create new conversation with deterministic ID based on sorted UIDs
        String convId = "conv_" + (u1.compareTo(u2) < 0 ? (u1 + "_" + u2) : (u2 + "_" + u1));
        if (!lId.isEmpty()) {
            convId += "_" + lId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        }

        List<String> parts = Arrays.asList(u1, u2);
        Map<String, String> names = new HashMap<>();
        names.put(u1, user1Name != null && !user1Name.isEmpty() ? user1Name : "User");
        names.put(u2, user2Name != null && !user2Name.isEmpty() ? user2Name : "User");

        Map<String, String> roles = new HashMap<>();
        roles.put(u1, user1Role != null && !user1Role.isEmpty() ? user1Role : "STUDENT");
        roles.put(u2, user2Role != null && !user2Role.isEmpty() ? user2Role : "STUDENT");

        Map<String, Long> unreads = new HashMap<>();
        unreads.put(u1, 0L);
        unreads.put(u2, 0L);

        Conversation newConv = new Conversation(
            convId,
            parts,
            names,
            roles,
            lId,
            lType,
            lTitle,
            "Started conversation",
            u1,
            System.currentTimeMillis(),
            unreads
        );

        final String finalConvId = convId;
        conversationCache.put(finalConvId, newConv);

        // Save to Firestore
        new Thread(() -> {
            Firestore fdb = getFirestore();
            if (fdb == null) return;
            try {
                Map<String, Object> convData = new HashMap<>();
                convData.put("conversationId", newConv.getConversationId());
                convData.put("participants", newConv.getParticipants());
                convData.put("participantNames", newConv.getParticipantNames());
                convData.put("participantRoles", newConv.getParticipantRoles());
                convData.put("listingId", newConv.getListingId());
                convData.put("listingType", newConv.getListingType());
                convData.put("listingTitle", newConv.getListingTitle());
                convData.put("lastMessage", newConv.getLastMessage());
                convData.put("lastSenderId", newConv.getLastSenderId());
                convData.put("lastMessageTime", newConv.getLastMessageTime());
                convData.put("unreadCounts", newConv.getUnreadCounts());

                fdb.collection(CONVERSATIONS_COLLECTION)
                    .document(finalConvId)
                    .set(convData, SetOptions.merge())
                    .get();

                System.out.println("[MessageDAOImpl] Created new conversation in Firestore: " + finalConvId);
            } catch (Throwable e) {
                System.err.println("[MessageDAOImpl] Error creating conversation: " + e.getMessage());
            }
        }).start();

        return newConv;
    }

    @Override
    public List<Conversation> getConversationsForUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) return new ArrayList<>();
        String uid = userId.trim();

        Map<String, Conversation> merged = new HashMap<>();

        // Add from local cache
        for (Conversation c : conversationCache.values()) {
            if (c.getParticipants() != null && c.getParticipants().contains(uid)) {
                merged.put(c.getConversationId(), c);
            }
        }

        // Fetch from Firestore and merge
        Firestore db = getFirestore();
        if (db != null) {
            try {
                ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .whereArrayContains("participants", uid)
                    .get();

                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    Conversation conv = parseConversationFromSnapshot(doc);
                    conversationCache.put(conv.getConversationId(), conv);
                    merged.put(conv.getConversationId(), conv);
                }
            } catch (Throwable e) {
                System.err.println("[MessageDAOImpl] Error fetching user conversations: " + e.getMessage());
            }
        }

        List<Conversation> list = new ArrayList<>(merged.values());
        list.sort((c1, c2) -> Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));
        return list;
    }

    @Override
    public int getTotalUnreadCount(String userId) {
        if (userId == null || userId.trim().isEmpty()) return 0;
        String uid = userId.trim();
        int total = 0;
        for (Conversation c : getConversationsForUser(uid)) {
            total += c.getUnreadForUser(uid);
        }
        return total;
    }

    @Override
    public ListenerRegistration listenToMessages(String conversationId, Consumer<List<ChatMessage>> callback) {
        if (conversationId == null || conversationId.trim().isEmpty() || callback == null) return null;
        Firestore db = getFirestore();
        if (db == null) return null;

        try {
            return db.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId.trim())
                .collection(MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        System.err.println("[MessageDAOImpl] Real-time message listener error: " + error.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<ChatMessage> updatedList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            updatedList.add(parseMessageFromSnapshot(doc));
                        }
                        messageCache.put(conversationId.trim(), new CopyOnWriteArrayList<>(updatedList));
                        Platform.runLater(() -> callback.accept(updatedList));
                    }
                });
        } catch (Throwable e) {
            System.err.println("[MessageDAOImpl] Error setting up message snapshot listener: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ListenerRegistration listenToConversations(String userId, Consumer<List<Conversation>> callback) {
        if (userId == null || userId.trim().isEmpty() || callback == null) return null;
        Firestore db = getFirestore();
        if (db == null) return null;

        try {
            return db.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participants", userId.trim())
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        System.err.println("[MessageDAOImpl] Real-time conversation listener error: " + error.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<Conversation> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Conversation conv = parseConversationFromSnapshot(doc);
                            conversationCache.put(conv.getConversationId(), conv);
                            list.add(conv);
                        }
                        list.sort((c1, c2) -> Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));
                        Platform.runLater(() -> callback.accept(list));
                    }
                });
        } catch (Throwable e) {
            System.err.println("[MessageDAOImpl] Error setting up conversation snapshot listener: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Conversation parseConversationFromSnapshot(DocumentSnapshot doc) {
        String convId = doc.getString("conversationId");
        if (convId == null) convId = doc.getId();

        List<String> parts = (List<String>) doc.get("participants");
        if (parts == null) parts = new ArrayList<>();

        Map<String, String> names = (Map<String, String>) doc.get("participantNames");
        if (names == null) names = new HashMap<>();

        Map<String, String> roles = (Map<String, String>) doc.get("participantRoles");
        if (roles == null) roles = new HashMap<>();

        String lId = doc.getString("listingId");
        String lType = doc.getString("listingType");
        String lTitle = doc.getString("listingTitle");
        String lastMsg = doc.getString("lastMessage");
        String lastSender = doc.getString("lastSenderId");
        Long lastTime = doc.getLong("lastMessageTime");

        Map<String, Object> rawUnread = (Map<String, Object>) doc.get("unreadCounts");
        Map<String, Long> unreads = new HashMap<>();
        if (rawUnread != null) {
            for (Map.Entry<String, Object> entry : rawUnread.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    unreads.put(entry.getKey(), ((Number) entry.getValue()).longValue());
                }
            }
        }

        return new Conversation(
            convId,
            parts,
            names,
            roles,
            lId != null ? lId : "",
            lType != null ? lType : "GENERAL",
            lTitle != null ? lTitle : "",
            lastMsg != null ? lastMsg : "",
            lastSender != null ? lastSender : "",
            lastTime != null ? lastTime : System.currentTimeMillis(),
            unreads
        );
    }

    private ChatMessage parseMessageFromSnapshot(DocumentSnapshot doc) {
        String msgId = doc.getString("messageId");
        if (msgId == null) msgId = doc.getId();

        String convId = doc.getString("conversationId");
        String senderId = doc.getString("senderId");
        String senderName = doc.getString("senderName");
        String receiverId = doc.getString("receiverId");
        String receiverName = doc.getString("receiverName");
        String text = doc.getString("text");
        Long timestamp = doc.getLong("timestamp");
        Boolean read = doc.getBoolean("read");
        String listingId = doc.getString("listingId");
        String listingType = doc.getString("listingType");

        return new ChatMessage(
            msgId,
            convId != null ? convId : "",
            senderId != null ? senderId : "",
            senderName != null ? senderName : "User",
            receiverId != null ? receiverId : "",
            receiverName != null ? receiverName : "User",
            text != null ? text : "",
            timestamp != null ? timestamp : System.currentTimeMillis(),
            read != null ? read : false,
            listingId != null ? listingId : "",
            listingType != null ? listingType : "GENERAL"
        );
    }
}
