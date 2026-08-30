package com.core2web.dao;

import com.core2web.model.ChatMessage;
import com.core2web.model.Conversation;
import com.google.cloud.firestore.ListenerRegistration;
import java.util.List;
import java.util.function.Consumer;

public interface MessageDAO {
    boolean saveMessage(ChatMessage message);
    List<ChatMessage> getMessages(String conversationId);
    boolean markMessagesAsRead(String conversationId, String currentUserId);
    Conversation getOrCreateConversation(
        String user1Id, String user1Name, String user1Role,
        String user2Id, String user2Name, String user2Role,
        String listingId, String listingType, String listingTitle
    );
    List<Conversation> getConversationsForUser(String userId);
    int getTotalUnreadCount(String userId);
    ListenerRegistration listenToMessages(String conversationId, Consumer<List<ChatMessage>> callback);
    ListenerRegistration listenToConversations(String userId, Consumer<List<Conversation>> callback);
}
