package com.core2web;

import com.core2web.dao.MessageDAO;
import com.core2web.dao.MessageDAOImpl;
import com.core2web.model.ChatMessage;
import com.core2web.model.Conversation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MessagingSystemTest {

    private MessageDAO messageDAO;

    @Before
    public void setUp() {
        messageDAO = new MessageDAOImpl();
    }

    @Test
    public void testStudentOwnerConversationAndDuplicatePrevention() {
        String studentUid = "uid_student_test_101";
        String studentName = "Aarav Student";
        String ownerUid = "uid_owner_test_201";
        String ownerName = "Mr. Sharma Owner";
        String roomId = "room_kothrud_1";
        String roomTitle = "Single Room in Kothrud";

        // 1. Student contacts Owner
        Conversation conv1 = messageDAO.getOrCreateConversation(
            studentUid, studentName, "STUDENT",
            ownerUid, ownerName, "OWNER",
            roomId, "ROOM", roomTitle
        );

        Assert.assertNotNull("Conversation must be created", conv1);
        Assert.assertTrue("Participants must contain student", conv1.getParticipants().contains(studentUid));
        Assert.assertTrue("Participants must contain owner", conv1.getParticipants().contains(ownerUid));

        // 2. Student sends message
        ChatMessage msg1 = new ChatMessage(
            "msg_1", conv1.getConversationId(),
            studentUid, studentName,
            ownerUid, ownerName,
            "Hello, is this room available?",
            System.currentTimeMillis(), false,
            roomId, "ROOM"
        );
        boolean sent1 = messageDAO.saveMessage(msg1);
        Assert.assertTrue("Message must be saved", sent1);

        // 3. Duplicate conversation test: Student contacts owner again for same room
        Conversation conv2 = messageDAO.getOrCreateConversation(
            studentUid, studentName, "STUDENT",
            ownerUid, ownerName, "OWNER",
            roomId, "ROOM", roomTitle
        );
        Assert.assertEquals("Duplicate conversation must NOT be created", conv1.getConversationId(), conv2.getConversationId());

        // 4. Reverse conversation check: Owner contacts student
        Conversation convOwnerView = messageDAO.getOrCreateConversation(
            ownerUid, ownerName, "OWNER",
            studentUid, studentName, "STUDENT",
            roomId, "ROOM", roomTitle
        );
        Assert.assertEquals("Reverse perspective must return identical conversation ID", conv1.getConversationId(), convOwnerView.getConversationId());

        // 5. Owner replies
        ChatMessage msg2 = new ChatMessage(
            "msg_2", conv1.getConversationId(),
            ownerUid, ownerName,
            studentUid, studentName,
            "Yes, it is available.",
            System.currentTimeMillis(), false,
            roomId, "ROOM"
        );
        boolean sent2 = messageDAO.saveMessage(msg2);
        Assert.assertTrue("Owner reply must be saved", sent2);

        // 6. Messages list verification
        List<ChatMessage> messages = messageDAO.getMessages(conv1.getConversationId());
        Assert.assertTrue("Messages list must contain both messages", messages.size() >= 2);
    }

    @Test
    public void testStudentSellerConversation() {
        String studentUid = "uid_student_test_102";
        String studentName = "Neha Student";
        String sellerUid = "uid_seller_test_301";
        String sellerName = "Rohan Seller";
        String prodId = "prod_mech_book_1";
        String prodTitle = "Engineering Mechanics Textbook";

        // Student contacts Seller
        Conversation conv = messageDAO.getOrCreateConversation(
            studentUid, studentName, "STUDENT",
            sellerUid, sellerName, "SELLER",
            prodId, "PRODUCT", prodTitle
        );

        Assert.assertNotNull("Seller conversation must be created", conv);
        Assert.assertEquals("Listing type must be PRODUCT", "PRODUCT", conv.getListingType());

        // Student sends message
        ChatMessage msg = new ChatMessage(
            "msg_sell_1", conv.getConversationId(),
            studentUid, studentName,
            sellerUid, sellerName,
            "Hi, is the textbook in good condition?",
            System.currentTimeMillis(), false,
            prodId, "PRODUCT"
        );
        boolean saved = messageDAO.saveMessage(msg);
        Assert.assertTrue(saved);

        // Seller receives and replies
        ChatMessage reply = new ChatMessage(
            "msg_sell_2", conv.getConversationId(),
            sellerUid, sellerName,
            studentUid, studentName,
            "Yes, it is like new!",
            System.currentTimeMillis(), false,
            prodId, "PRODUCT"
        );
        boolean replySaved = messageDAO.saveMessage(reply);
        Assert.assertTrue(replySaved);
    }

    @Test
    public void testStudentServiceProviderConversationAndProviderIsolation() {
        String studentUid = "uid_student_test_103";
        String studentName = "Kunal Student";
        String providerAUid = "uid_laundry_provider_A";
        String providerAName = "Speedy Wash Laundry";
        String providerBUid = "uid_laundry_provider_B";
        String providerBName = "Campus Cleaners";
        String servId = "serv_laundry_1";
        String servTitle = "Express Laundry & Dry Clean";

        // 1. Student contacts Provider A
        Conversation convA = messageDAO.getOrCreateConversation(
            studentUid, studentName, "STUDENT",
            providerAUid, providerAName, "SERVICE_PROVIDER",
            servId, "SERVICE", servTitle
        );

        ChatMessage msgA = new ChatMessage(
            "msg_serv_A_1", convA.getConversationId(),
            studentUid, studentName,
            providerAUid, providerAName,
            "Can I get laundry pickup today at 4 PM?",
            System.currentTimeMillis(), false,
            servId, "SERVICE"
        );
        messageDAO.saveMessage(msgA);

        // 2. Provider A conversations check
        List<Conversation> providerAConversations = messageDAO.getConversationsForUser(providerAUid);
        boolean foundInA = providerAConversations.stream().anyMatch(c -> c.getConversationId().equals(convA.getConversationId()));
        Assert.assertTrue("Provider A must see conversation A", foundInA);

        // 3. Provider B Isolation check: Provider B MUST NOT see Provider A's conversation
        List<Conversation> providerBConversations = messageDAO.getConversationsForUser(providerBUid);
        boolean leakedToB = providerBConversations.stream().anyMatch(c -> c.getConversationId().equals(convA.getConversationId()));
        Assert.assertFalse("Provider B MUST NOT see Provider A's private conversation", leakedToB);
    }

    @Test
    public void testUnreadCountsAndMarkAsRead() {
        String user1 = "uid_user_unread_1";
        String user2 = "uid_user_unread_2";

        Conversation conv = messageDAO.getOrCreateConversation(
            user1, "User 1", "STUDENT",
            user2, "User 2", "STUDENT",
            "", "GENERAL", "Direct Message"
        );

        ChatMessage m1 = new ChatMessage(
            "msg_unr_1", conv.getConversationId(),
            user1, "User 1",
            user2, "User 2",
            "Are you there?",
            System.currentTimeMillis(), false,
            "", "GENERAL"
        );
        messageDAO.saveMessage(m1);

        // Mark as read by user2
        boolean marked = messageDAO.markMessagesAsRead(conv.getConversationId(), user2);
        Assert.assertTrue("Mark as read must succeed", marked);
    }
}
