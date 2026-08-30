package com.core2web.view.messages;

import com.core2web.Main;
import com.core2web.dao.MessageDAO;
import com.core2web.dao.MessageDAOImpl;
import com.core2web.model.ChatMessage;
import com.core2web.model.Conversation;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.SessionManager;
import com.core2web.util.Theme;
import com.google.cloud.firestore.ListenerRegistration;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.*;

public class MessagesPage {

    private final MessageDAO messageDAO = new MessageDAOImpl();
    private Conversation activeConversation = null;
    private ListenerRegistration activeMessageListener = null;
    private ListenerRegistration activeConversationListener = null;

    private VBox conversationsContainer;
    private VBox chatMessagesContainer;
    private ScrollPane chatScrollPane;
    private Label chatHeaderName;
    private Label chatHeaderContext;
    private TextField chatInputField;
    private Button sendBtn;
    private VBox emptyChatPlaceholder;
    private VBox activeChatContent;
    private Label unreadTotalBadge;

    public Node getPageNode(Runnable onBack) {
        return getPageNodeWithActiveConversation(null, onBack);
    }

    public Node getPageNodeWithActiveConversation(Conversation initialActiveConv, Runnable onBack) {
        this.activeConversation = initialActiveConv;

        User currentUser = getCurrentUser();
        String currentUid = currentUser.getUid();

        BorderPane rootPane = new BorderPane();
        rootPane.setStyle(Theme.rootPaneStyle());

        // TOP HEADER BAR
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(14, 28, 14, 28));
        topBar.setStyle(Theme.topBarStyle());

        Button backBtn = new Button("← Back");
        backBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 6px 14px; -fx-font-weight: 700;");
        backBtn.setOnAction(e -> {
            cleanupListeners();
            if (onBack != null) onBack.run();
            else Main.redirectToCurrentRoleDashboard();
        });

        VBox titleBox = new VBox(2);
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        User.Role activeRole = currentUser.getRole();
        String roleHeaderTitle = "Messages & Inquiries";
        String roleSubTitle = "Direct communication with Property Owners, Sellers, Service Providers & Flatmates";
        if (activeRole != null) {
            switch (activeRole) {
                case OWNER:
                    roleHeaderTitle = "Property Owner • Messages";
                    roleSubTitle = "Direct communication with prospective student tenants & room inquiries";
                    break;
                case SELLER:
                    roleHeaderTitle = "Student Seller • Messages";
                    roleSubTitle = "Direct communication with buyers & marketplace inquiries";
                    break;
                case SERVICE_PROVIDER:
                    roleHeaderTitle = "Service Provider • Messages";
                    roleSubTitle = "Direct communication with student clients & booking inquiries";
                    break;
                case ADMIN:
                    roleHeaderTitle = "System Administration • Messages";
                    roleSubTitle = "System communication, user queries & moderation channels";
                    break;
                case STUDENT:
                default:
                    roleHeaderTitle = "Messages & Inquiries";
                    roleSubTitle = "Direct communication with Property Owners, Sellers, Service Providers & Flatmates";
                    break;
            }
        }

        Text titleTxt = new Text(roleHeaderTitle);
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");

        unreadTotalBadge = new Label("0 Unread");
        unreadTotalBadge.setStyle(Theme.badgeStyle() + " -fx-font-size: 11px; -fx-padding: 3px 8px;");
        unreadTotalBadge.setVisible(false);
        unreadTotalBadge.setManaged(false);

        titleRow.getChildren().addAll(titleTxt, unreadTotalBadge);

        Text subTxt = new Text(roleSubTitle);
        subTxt.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleRow, subTxt);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 12px;");
        refreshBtn.setOnAction(e -> loadConversations(currentUid));

        topBar.getChildren().addAll(backBtn, titleBox, topSpacer, refreshBtn);
        rootPane.setTop(topBar);

        // SPLIT BODY (Left: Conversation List, Right: Active Chat)
        HBox body = new HBox(0);
        body.setStyle("-fx-background-color: transparent;");

        // LEFT PANE: Conversations Sidebar
        VBox leftPane = new VBox(12);
        leftPane.setPrefWidth(380);
        leftPane.setMinWidth(320);
        leftPane.setMaxWidth(420);
        leftPane.setPadding(new Insets(16, 16, 16, 24));
        leftPane.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 1px 0 0;");

        // + New Message Action Button
        Button newMsgBtn = new Button("➕  New Message");
        newMsgBtn.setMaxWidth(Double.MAX_VALUE);
        newMsgBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 13px; -fx-font-weight: 800; -fx-padding: 9px 14px; -fx-background-radius: 10px;");
        newMsgBtn.setOnAction(e -> showNewMessageDialog(currentUid, currentUser));

        // Search Bar in Left Pane
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search messages...");
        searchField.setStyle(Theme.searchFieldStyle());

        // Filter Pills Row (All / Unread)
        HBox filterPills = new HBox(8);
        Button pillAll = new Button("All Messages");
        pillAll.setStyle(Theme.filterPillStyle(true));
        Button pillUnread = new Button("Unread");
        pillUnread.setStyle(Theme.filterPillStyle(false));

        final boolean[] filterUnreadOnly = {false};
        pillAll.setOnAction(e -> {
            filterUnreadOnly[0] = false;
            pillAll.setStyle(Theme.filterPillStyle(true));
            pillUnread.setStyle(Theme.filterPillStyle(false));
            filterConversations(searchField.getText(), filterUnreadOnly[0], currentUid);
        });
        pillUnread.setOnAction(e -> {
            filterUnreadOnly[0] = true;
            pillAll.setStyle(Theme.filterPillStyle(false));
            pillUnread.setStyle(Theme.filterPillStyle(true));
            filterConversations(searchField.getText(), filterUnreadOnly[0], currentUid);
        });
        filterPills.getChildren().addAll(pillAll, pillUnread);

        // Conversations scrollable list container
        conversationsContainer = new VBox(8);
        conversationsContainer.setFillWidth(true);

        ScrollPane convScrollPane = new ScrollPane(conversationsContainer);
        convScrollPane.setFitToWidth(true);
        convScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        convScrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.CARD_BG + ";");
        VBox.setVgrow(convScrollPane, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, o, n) -> filterConversations(n, filterUnreadOnly[0], currentUid));

        leftPane.getChildren().addAll(newMsgBtn, searchField, filterPills, convScrollPane);

        // RIGHT PANE: Active Chat Panel
        VBox rightPane = new VBox();
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        rightPane.setStyle("-fx-background-color: " + Theme.BG_COLOR + ";");

        // 1. Empty State Placeholder when no chat selected
        emptyChatPlaceholder = new VBox(14);
        emptyChatPlaceholder.setAlignment(Pos.CENTER);
        emptyChatPlaceholder.setPadding(new Insets(60, 40, 60, 40));
        VBox.setVgrow(emptyChatPlaceholder, Priority.ALWAYS);

        StackPane emptyIconBadge = new StackPane();
        emptyIconBadge.setPrefSize(72, 72);
        emptyIconBadge.setMaxSize(72, 72);
        emptyIconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 36px;");
        emptyIconBadge.getChildren().add(IconFactory.getIconNode(IconFactory.PATH_MESSAGE, Theme.PRIMARY, 32));

        Text emptyTitle = new Text("No conversations yet");
        emptyTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");

        Text emptySub = new Text("Your messages will appear here when you contact another user.\nSelect a conversation from the left or contact an Owner, Seller, or Service Provider from their listings.");
        emptySub.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-text-alignment: center; -fx-line-spacing: 3px;");
        emptySub.setWrappingWidth(420);

        emptyChatPlaceholder.getChildren().addAll(emptyIconBadge, emptyTitle, emptySub);

        // 2. Active Chat Content View
        activeChatContent = new VBox(0);
        VBox.setVgrow(activeChatContent, Priority.ALWAYS);
        activeChatContent.setVisible(false);
        activeChatContent.setManaged(false);

        // Chat Header
        HBox chatHeader = new HBox(12);
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(14, 24, 14, 24));
        chatHeader.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1px 0;");

        StackPane chatAvatar = new StackPane();
        chatAvatar.setPrefSize(42, 42);
        chatAvatar.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 21px;");
        chatAvatar.getChildren().add(IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 18));

        VBox chatTitleBox = new VBox(2);
        chatHeaderName = new Label("User");
        chatHeaderName.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 800;");

        chatHeaderContext = new Label("Listing Context");
        chatHeaderContext.setStyle(Theme.mutedTextStyle());
        chatTitleBox.getChildren().addAll(chatHeaderName, chatHeaderContext);

        chatHeader.getChildren().addAll(chatAvatar, chatTitleBox);

        // Chat Message History (Scrollable)
        chatMessagesContainer = new VBox(12);
        chatMessagesContainer.setPadding(new Insets(20, 24, 20, 24));
        chatMessagesContainer.setFillWidth(true);

        chatScrollPane = new ScrollPane(chatMessagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Quick Reply Suggestions Bar
        HBox quickReplies = new HBox(8);
        quickReplies.setPadding(new Insets(6, 24, 6, 24));
        quickReplies.setAlignment(Pos.CENTER_LEFT);
        quickReplies.setStyle("-fx-background-color: transparent;");

        String[] quickPrompts = {"Is this still available?", "What is the final price?", "Can we schedule a visit?", "Please share contact details"};
        for (String qp : quickPrompts) {
            Button qpBtn = new Button(qp);
            qpBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + Theme.PRIMARY_DARK + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 600; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 14px; -fx-background-radius: 14px; -fx-padding: 4px 10px; -fx-cursor: hand;");
            qpBtn.setOnAction(e -> {
                if (chatInputField != null) {
                    chatInputField.setText(qp);
                    sendMessage(currentUid, currentUser.getName());
                }
            });
            quickReplies.getChildren().add(qpBtn);
        }

        // Chat Input Box Bottom Bar
        HBox chatInputBar = new HBox(12);
        chatInputBar.setAlignment(Pos.CENTER_LEFT);
        chatInputBar.setPadding(new Insets(12, 24, 16, 24));
        chatInputBar.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 1px 0 0 0;");

        chatInputField = new TextField();
        chatInputField.setPromptText("Type your message... (Press Enter to send)");
        chatInputField.setStyle(Theme.inputFieldStyle() + " -fx-font-size: 13.5px; -fx-padding: 10px 14px;");
        HBox.setHgrow(chatInputField, Priority.ALWAYS);

        sendBtn = new Button("Send →");
        sendBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 13.5px; -fx-font-weight: 800; -fx-padding: 9px 20px; -fx-background-radius: 10px;");

        chatInputField.setOnAction(e -> sendMessage(currentUid, currentUser.getName()));
        sendBtn.setOnAction(e -> sendMessage(currentUid, currentUser.getName()));

        chatInputBar.getChildren().addAll(chatInputField, sendBtn);

        activeChatContent.getChildren().addAll(chatHeader, chatScrollPane, quickReplies, chatInputBar);

        rightPane.getChildren().addAll(emptyChatPlaceholder, activeChatContent);

        body.getChildren().addAll(leftPane, rightPane);
        rootPane.setCenter(body);

        // Load conversations and setup real-time listener for conversation updates
        loadConversations(currentUid);
        setupConversationsListener(currentUid);

        if (activeConversation != null) {
            openConversation(activeConversation, currentUid);
        }

        return rootPane;
    }

    private User getCurrentUser() {
        User u = DataRepository.getInstance().getCurrentUser();
        if (u == null) u = SessionManager.getInstance().getCurrentUser();
        if (u == null || u.getUid() == null || u.getUid().trim().isEmpty()) {
            String uid = SessionManager.getInstance().getUid();
            String name = SessionManager.getInstance().getName();
            String email = SessionManager.getInstance().getEmail();
            User.Role role = SessionManager.getInstance().getRole();
            if (uid == null || uid.isEmpty()) uid = "user_guest";
            u = new User(uid, name != null ? name : "User", email != null ? email : "", "", role != null ? role : User.Role.STUDENT);
        }
        return u;
    }

    private List<Conversation> cachedUserConversations = new ArrayList<>();

    private void loadConversations(String currentUid) {
        new Thread(() -> {
            List<Conversation> list = messageDAO.getConversationsForUser(currentUid);
            Platform.runLater(() -> {
                this.cachedUserConversations = list;
                renderConversationList(list, currentUid);
                updateUnreadSummary(list, currentUid);
            });
        }).start();
    }

    private void setupConversationsListener(String currentUid) {
        if (activeConversationListener != null) {
            activeConversationListener.remove();
        }
        activeConversationListener = messageDAO.listenToConversations(currentUid, updatedList -> {
            this.cachedUserConversations = updatedList;
            renderConversationList(updatedList, currentUid);
            updateUnreadSummary(updatedList, currentUid);
        });
    }

    private void updateUnreadSummary(List<Conversation> list, String currentUid) {
        int totalUnread = 0;
        for (Conversation c : list) {
            totalUnread += c.getUnreadForUser(currentUid);
        }
        if (unreadTotalBadge != null) {
            if (totalUnread > 0) {
                unreadTotalBadge.setText(totalUnread + " Unread");
                unreadTotalBadge.setVisible(true);
                unreadTotalBadge.setManaged(true);
            } else {
                unreadTotalBadge.setVisible(false);
                unreadTotalBadge.setManaged(false);
            }
        }
    }

    private void filterConversations(String query, boolean unreadOnly, String currentUid) {
        String q = query != null ? query.trim().toLowerCase() : "";
        List<Conversation> filtered = new ArrayList<>();
        for (Conversation c : cachedUserConversations) {
            if (unreadOnly && c.getUnreadForUser(currentUid) <= 0) {
                continue;
            }
            if (!q.isEmpty()) {
                String otherName = c.getOtherParticipantName(currentUid).toLowerCase();
                String lastMsg = (c.getLastMessage() != null ? c.getLastMessage() : "").toLowerCase();
                String title = (c.getListingTitle() != null ? c.getListingTitle() : "").toLowerCase();
                if (!otherName.contains(q) && !lastMsg.contains(q) && !title.contains(q)) {
                    continue;
                }
            }
            filtered.add(c);
        }
        renderConversationList(filtered, currentUid);
    }

    private void renderConversationList(List<Conversation> list, String currentUid) {
        if (conversationsContainer == null) return;
        conversationsContainer.getChildren().clear();

        if (list.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30, 16, 30, 16));
            Text t1 = new Text("No conversations found");
            t1.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
            Text t2 = new Text("When you contact another user, conversations will appear here.");
            t2.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-text-alignment: center;");
            t2.setWrappingWidth(240);
            emptyBox.getChildren().addAll(t1, t2);
            conversationsContainer.getChildren().add(emptyBox);
            return;
        }

        for (Conversation c : list) {
            VBox card = createConversationItemNode(c, currentUid);
            conversationsContainer.getChildren().add(card);
        }
    }

    private VBox createConversationItemNode(Conversation c, String currentUid) {
        boolean isActive = activeConversation != null && activeConversation.getConversationId().equals(c.getConversationId());
        int unread = c.getUnreadForUser(currentUid);

        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(
            "-fx-background-color: " + (isActive ? Theme.PRIMARY_LIGHT : "white") + ";"
            + "-fx-background-radius: 12px;"
            + "-fx-border-color: " + (isActive ? Theme.PRIMARY : Theme.BORDER_COLOR) + ";"
            + "-fx-border-radius: 12px;"
            + "-fx-cursor: hand;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);"
        );

        // Top Row: Avatar + Name & Role + Time
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String otherRole = c.getOtherParticipantRole(currentUid);
        String roleIcon = getRoleIcon(otherRole);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(32, 32);
        avatar.setStyle("-fx-background-color: " + (isActive ? "white" : Theme.PRIMARY_LIGHT) + "; -fx-background-radius: 16px;");
        Text iconTxt = new Text(roleIcon);
        iconTxt.setStyle("-fx-font-size: 14px;");
        avatar.getChildren().add(iconTxt);

        VBox nameBox = new VBox(1);
        String otherName = c.getOtherParticipantName(currentUid);
        Label nameLbl = new Label(otherName);
        nameLbl.setMaxWidth(160);
        nameLbl.setTextOverrun(OverrunStyle.ELLIPSIS);
        nameLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-font-weight: 700;");

        Label roleLbl = new Label(formatRoleName(otherRole));
        roleLbl.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px;");
        nameBox.getChildren().addAll(nameLbl, roleLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text timeTxt = new Text(formatTime(c.getLastMessageTime()));
        timeTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px;");

        topRow.getChildren().addAll(avatar, nameBox, spacer, timeTxt);

        // Middle Row: Listing Context Tag (if any)
        HBox contextRow = new HBox();
        if (c.getListingTitle() != null && !c.getListingTitle().trim().isEmpty()) {
            Label contextLbl = new Label("📌 " + c.getListingTitle());
            contextLbl.setMaxWidth(260);
            contextLbl.setTextOverrun(OverrunStyle.ELLIPSIS);
            contextLbl.setStyle(
                "-fx-background-color: " + (isActive ? "rgba(79, 119, 45, 0.15)" : "#F3F4F6") + ";"
                + "-fx-text-fill: " + Theme.PRIMARY_DARK + ";"
                + "-fx-font-family: " + Theme.FONT + ";"
                + "-fx-font-size: 10.5px;"
                + "-fx-font-weight: 700;"
                + "-fx-padding: 2px 8px;"
                + "-fx-background-radius: 6px;"
            );
            contextRow.getChildren().add(contextLbl);
        }

        // Bottom Row: Last Message snippet + Unread Badge Dot
        HBox bottomRow = new HBox(6);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        String snippet = (c.getLastMessage() != null && !c.getLastMessage().isEmpty()) ? c.getLastMessage() : "Started conversation";
        Label msgSnippet = new Label(snippet);
        msgSnippet.setMaxWidth(220);
        msgSnippet.setTextOverrun(OverrunStyle.ELLIPSIS);
        msgSnippet.setStyle("-fx-text-fill: " + (unread > 0 ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED) + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px;" + (unread > 0 ? " -fx-font-weight: 700;" : ""));
        HBox.setHgrow(msgSnippet, Priority.ALWAYS);

        bottomRow.getChildren().add(msgSnippet);

        if (unread > 0) {
            Label unreadBadge = new Label(String.valueOf(unread));
            unreadBadge.setStyle(
                "-fx-background-color: " + Theme.PRIMARY + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-family: " + Theme.FONT + ";"
                + "-fx-font-size: 10.5px;"
                + "-fx-font-weight: 800;"
                + "-fx-padding: 2px 7px;"
                + "-fx-background-radius: 12px;"
            );
            bottomRow.getChildren().add(unreadBadge);
        }

        card.getChildren().addAll(topRow);
        if (!contextRow.getChildren().isEmpty()) {
            card.getChildren().add(contextRow);
        }
        card.getChildren().add(bottomRow);

        card.setOnMouseClicked(e -> openConversation(c, currentUid));

        return card;
    }

    public void openConversation(Conversation conv, String currentUid) {
        if (conv == null) return;
        this.activeConversation = conv;

        // Show active chat UI, hide placeholder
        emptyChatPlaceholder.setVisible(false);
        emptyChatPlaceholder.setManaged(false);
        activeChatContent.setVisible(true);
        activeChatContent.setManaged(true);

        // Update header details
        String otherName = conv.getOtherParticipantName(currentUid);
        String otherRole = conv.getOtherParticipantRole(currentUid);
        chatHeaderName.setText(otherName + "  (" + formatRoleName(otherRole) + ")");

        if (conv.getListingTitle() != null && !conv.getListingTitle().isEmpty()) {
            chatHeaderContext.setText("Inquiring about: " + conv.getListingTitle());
        } else {
            chatHeaderContext.setText("Direct Conversation");
        }

        // Mark messages as read
        messageDAO.markMessagesAsRead(conv.getConversationId(), currentUid);

        // Load existing messages
        loadChatMessages(conv.getConversationId(), currentUid);

        // Subscribe to real-time message stream for this conversation
        if (activeMessageListener != null) {
            activeMessageListener.remove();
        }
        activeMessageListener = messageDAO.listenToMessages(conv.getConversationId(), updatedMessages -> {
            renderChatMessages(updatedMessages, currentUid);
            messageDAO.markMessagesAsRead(conv.getConversationId(), currentUid);
        });

        // Re-render conversation list to reflect active selection
        renderConversationList(cachedUserConversations, currentUid);
    }

    private void loadChatMessages(String convId, String currentUid) {
        new Thread(() -> {
            List<ChatMessage> msgs = messageDAO.getMessages(convId);
            Platform.runLater(() -> renderChatMessages(msgs, currentUid));
        }).start();
    }

    private void renderChatMessages(List<ChatMessage> msgs, String currentUid) {
        if (chatMessagesContainer == null) return;
        chatMessagesContainer.getChildren().clear();

        if (msgs.isEmpty()) {
            VBox startBox = new VBox(6);
            startBox.setAlignment(Pos.CENTER);
            startBox.setPadding(new Insets(30));
            Text t1 = new Text("No messages yet in this conversation");
            t1.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
            Text t2 = new Text("Say hello or ask about the listing to get started!");
            t2.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px;");
            startBox.getChildren().addAll(t1, t2);
            chatMessagesContainer.getChildren().add(startBox);
            return;
        }

        String lastDate = "";
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEE, dd MMM yyyy");

        for (ChatMessage m : msgs) {
            String msgDate = dateFmt.format(new Date(m.getTimestamp()));
            if (!msgDate.equals(lastDate)) {
                lastDate = msgDate;
                HBox dateSep = createDateSeparator(msgDate);
                chatMessagesContainer.getChildren().add(dateSep);
            }

            boolean isMine = (m.getSenderId() != null && m.getSenderId().equalsIgnoreCase(currentUid.trim()));
            HBox bubbleRow = createMessageBubbleRow(m, isMine);
            chatMessagesContainer.getChildren().add(bubbleRow);
        }

        // Auto-scroll to bottom
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }

    private HBox createDateSeparator(String dateText) {
        HBox sepRow = new HBox(12);
        sepRow.setAlignment(Pos.CENTER);
        sepRow.setPadding(new Insets(10, 0, 6, 0));

        Region line1 = new Region();
        HBox.setHgrow(line1, Priority.ALWAYS);
        line1.setPrefHeight(1);
        line1.setStyle("-fx-background-color: " + Theme.BORDER_COLOR + ";");

        Label lbl = new Label(dateText);
        lbl.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 700;");

        Region line2 = new Region();
        HBox.setHgrow(line2, Priority.ALWAYS);
        line2.setPrefHeight(1);
        line2.setStyle("-fx-background-color: " + Theme.BORDER_COLOR + ";");

        sepRow.getChildren().addAll(line1, lbl, line2);
        return sepRow;
    }

    private HBox createMessageBubbleRow(ChatMessage m, boolean isMine) {
        HBox row = new HBox();
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(460);
        bubble.setPadding(new Insets(10, 14, 10, 14));

        if (isMine) {
            // Right aligned (StudentExpress primary theme green)
            bubble.setStyle(
                "-fx-background-color: " + Theme.PRIMARY + ";"
                + "-fx-background-radius: 16px 16px 4px 16px;"
                + "-fx-effect: dropshadow(gaussian, rgba(79, 119, 45, 0.18), 8, 0, 0, 2);"
            );
        } else {
            // Left aligned (Clean white card)
            bubble.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 16px 16px 16px 4px;"
                + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
                + "-fx-border-radius: 16px 16px 16px 4px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);"
            );
        }

        // Sender name on incoming message
        if (!isMine) {
            Label senderLbl = new Label(m.getSenderName() != null ? m.getSenderName() : "User");
            senderLbl.setStyle("-fx-text-fill: " + Theme.PRIMARY_DARK + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 800;");
            bubble.getChildren().add(senderLbl);
        }

        // Message text
        Text textNode = new Text(m.getText());
        textNode.setStyle("-fx-fill: " + (isMine ? "white" : Theme.TEXT_PRIMARY) + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-line-spacing: 2px;");
        textNode.setWrappingWidth(420);
        bubble.getChildren().add(textNode);

        // Time and read status
        HBox metaRow = new HBox(4);
        metaRow.setAlignment(Pos.CENTER_RIGHT);

        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
        String timeStr = timeFmt.format(new Date(m.getTimestamp()));
        Text timeNode = new Text(timeStr);
        timeNode.setStyle("-fx-fill: " + (isMine ? "rgba(255,255,255,0.78)" : Theme.TEXT_MUTED) + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 10.5px;");
        metaRow.getChildren().add(timeNode);

        if (isMine) {
            Text statusTick = new Text(m.isRead() ? " ✓✓" : " ✓");
            statusTick.setStyle("-fx-fill: " + (m.isRead() ? "#93C5FD" : "rgba(255,255,255,0.75)") + "; -fx-font-size: 10.5px; -fx-font-weight: bold;");
            metaRow.getChildren().add(statusTick);
        }

        bubble.getChildren().add(metaRow);
        row.getChildren().add(bubble);
        return row;
    }

    private void sendMessage(String currentUid, String currentName) {
        if (activeConversation == null || chatInputField == null) return;
        String text = chatInputField.getText() != null ? chatInputField.getText().trim() : "";
        if (text.isEmpty()) return;

        chatInputField.clear();

        String otherUid = activeConversation.getOtherParticipantId(currentUid);
        String otherName = activeConversation.getOtherParticipantName(currentUid);

        ChatMessage msg = new ChatMessage(
            "msg_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6),
            activeConversation.getConversationId(),
            currentUid,
            currentName != null ? currentName : "User",
            otherUid,
            otherName != null ? otherName : "User",
            text,
            System.currentTimeMillis(),
            false,
            activeConversation.getListingId(),
            activeConversation.getListingType()
        );

        // Optimistically render message
        boolean saved = messageDAO.saveMessage(msg);
        if (!saved) {
            showErrorAlert("Message Send Error", "Unable to send message. Please check your connection and try again.");
        }
    }

    public void cleanupListeners() {
        if (activeMessageListener != null) {
            activeMessageListener.remove();
            activeMessageListener = null;
        }
        if (activeConversationListener != null) {
            activeConversationListener.remove();
            activeConversationListener = null;
        }
    }

    private String getRoleIcon(String role) {
        if (role == null) return "👤";
        String r = role.toUpperCase();
        if (r.contains("OWNER")) return "🏢";
        if (r.contains("SELLER")) return "🛍️";
        if (r.contains("PROVIDER") || r.contains("SERVICE")) return "🛠️";
        if (r.contains("ADMIN")) return "⚡";
        return "🎓";
    }

    private String formatRoleName(String role) {
        if (role == null) return "User";
        String r = role.toUpperCase();
        if (r.contains("OWNER")) return "Property Owner";
        if (r.contains("SELLER")) return "Student Seller";
        if (r.contains("PROVIDER") || r.contains("SERVICE")) return "Service Provider";
        if (r.contains("ADMIN")) return "Administrator";
        return "Student";
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) return "";
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) return "Just now";
        if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + "m ago";
        if (diff < 24 * 60 * 60 * 1000) {
            SimpleDateFormat fmt = new SimpleDateFormat("h:mm a");
            return fmt.format(new Date(timestamp));
        }
        if (diff < 7 * 24 * 60 * 60 * 1000) {
            SimpleDateFormat fmt = new SimpleDateFormat("EEE");
            return fmt.format(new Date(timestamp));
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM");
        return fmt.format(new Date(timestamp));
    }

    private void showNewMessageDialog(String currentUid, User currentUser) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Message");
        dialog.setHeaderText("Start a conversation with verified campus contacts");

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(460);

        TextField searchContactField = new TextField();
        searchContactField.setPromptText("🔍 Search contacts by name, role or listing...");
        searchContactField.setStyle(Theme.searchFieldStyle());

        VBox contactListContainer = new VBox(8);
        contactListContainer.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(contactListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(340);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.CARD_BG + ";");

        class ContactItem {
            String uid;
            String name;
            String role;
            String listingId;
            String listingType;
            String listingTitle;

            ContactItem(String uid, String name, String role, String listingId, String listingType, String listingTitle) {
                this.uid = uid;
                this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : "User";
                this.role = (role != null && !role.trim().isEmpty()) ? role.trim() : "STUDENT";
                this.listingId = listingId != null ? listingId : "";
                this.listingType = listingType != null ? listingType : "GENERAL";
                this.listingTitle = listingTitle != null ? listingTitle : "";
            }
        }

        List<ContactItem> allContacts = new ArrayList<>();
        Set<String> addedKeys = new HashSet<>();

        // 1. Rooms / Owners
        for (com.core2web.model.RoomItem room : DataRepository.getInstance().getRooms()) {
            if (room.getOwnerUid() != null && !room.getOwnerUid().trim().isEmpty() && !room.getOwnerUid().equalsIgnoreCase(currentUid)) {
                String key = room.getOwnerUid() + "_" + room.getId();
                if (addedKeys.add(key)) {
                    allContacts.add(new ContactItem(room.getOwnerUid(), room.getOwnerName(), "OWNER", room.getId(), "ROOM", room.getTitle()));
                }
            }
        }

        // 2. Marketplace Products / Sellers
        for (com.core2web.model.ProductItem prod : DataRepository.getInstance().getProducts()) {
            if (prod.getSellerUid() != null && !prod.getSellerUid().trim().isEmpty() && !prod.getSellerUid().equalsIgnoreCase(currentUid)) {
                String key = prod.getSellerUid() + "_" + prod.getId();
                if (addedKeys.add(key)) {
                    allContacts.add(new ContactItem(prod.getSellerUid(), prod.getSellerName(), "SELLER", prod.getId(), "PRODUCT", prod.getTitle()));
                }
            }
        }

        // 3. Services / Providers
        for (com.core2web.model.ServiceItem serv : DataRepository.getInstance().getServices()) {
            if (serv.getProviderUid() != null && !serv.getProviderUid().trim().isEmpty() && !serv.getProviderUid().equalsIgnoreCase(currentUid)) {
                String key = serv.getProviderUid() + "_" + serv.getId();
                if (addedKeys.add(key)) {
                    allContacts.add(new ContactItem(serv.getProviderUid(), serv.getProviderName(), "SERVICE_PROVIDER", serv.getId(), "SERVICE", serv.getTitle()));
                }
            }
        }

        // 4. Roommates
        for (com.core2web.model.RoommateItem rm : DataRepository.getInstance().getRoommates()) {
            String rmUid = rm.getStudentId() != null ? rm.getStudentId() : "";
            if (!rmUid.isEmpty() && !rmUid.equalsIgnoreCase(currentUid)) {
                String key = rmUid + "_" + rm.getId();
                if (addedKeys.add(key)) {
                    allContacts.add(new ContactItem(rmUid, rm.getName(), "STUDENT", rm.getId(), "ROOMMATE", rm.getLocation()));
                }
            }
        }

        // 5. Existing conversation participants
        for (Conversation c : cachedUserConversations) {
            String otherUid = c.getOtherParticipantId(currentUid);
            String otherName = c.getOtherParticipantName(currentUid);
            String otherRole = c.getOtherParticipantRole(currentUid);
            if (!otherUid.isEmpty() && !otherUid.equalsIgnoreCase(currentUid)) {
                String key = otherUid + "_" + c.getListingId();
                if (addedKeys.add(key)) {
                    allContacts.add(new ContactItem(otherUid, otherName, otherRole, c.getListingId(), c.getListingType(), c.getListingTitle()));
                }
            }
        }

        Runnable populateList = () -> {
            contactListContainer.getChildren().clear();
            String query = searchContactField.getText() != null ? searchContactField.getText().trim().toLowerCase() : "";

            int count = 0;
            for (ContactItem ci : allContacts) {
                if (!query.isEmpty()) {
                    boolean match = ci.name.toLowerCase().contains(query)
                            || ci.role.toLowerCase().contains(query)
                            || ci.listingTitle.toLowerCase().contains(query);
                    if (!match) continue;
                }

                HBox itemRow = new HBox(12);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                itemRow.setPadding(new Insets(10, 14, 10, 14));
                itemRow.setStyle(
                    "-fx-background-color: white;"
                    + "-fx-background-radius: 10px;"
                    + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
                    + "-fx-border-radius: 10px;"
                    + "-fx-cursor: hand;"
                );

                StackPane avatar = new StackPane();
                avatar.setPrefSize(38, 38);
                avatar.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 19px;");
                Text icon = new Text(getRoleIcon(ci.role));
                icon.setStyle("-fx-font-size: 16px;");
                avatar.getChildren().add(icon);

                VBox infoBox = new VBox(2);
                HBox nameRoleRow = new HBox(8);
                nameRoleRow.setAlignment(Pos.CENTER_LEFT);
                Label nameLbl = new Label(ci.name);
                nameLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";");
                Label roleBadge = new Label(formatRoleName(ci.role));
                roleBadge.setStyle(Theme.badgeStyle() + " -fx-font-size: 10px; -fx-padding: 2px 6px;");
                nameRoleRow.getChildren().addAll(nameLbl, roleBadge);

                Label contextLbl = new Label(ci.listingTitle.isEmpty() ? "Direct Campus Message" : ci.listingTitle);
                contextLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
                infoBox.getChildren().addAll(nameRoleRow, contextLbl);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                Button startChatBtn = new Button("Message →");
                startChatBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 11px; -fx-padding: 4px 10px;");

                itemRow.getChildren().addAll(avatar, infoBox, startChatBtn);

                itemRow.setOnMouseClicked(e -> {
                    dialog.setResult(null);
                    dialog.close();
                    Conversation conv = messageDAO.getOrCreateConversation(
                        currentUid, currentUser.getName(), currentUser.getRole() != null ? currentUser.getRole().name() : "STUDENT",
                        ci.uid, ci.name, ci.role,
                        ci.listingId, ci.listingType, ci.listingTitle
                    );
                    openConversation(conv, currentUid);
                    if (chatInputField != null) {
                        chatInputField.requestFocus();
                    }
                });

                contactListContainer.getChildren().add(itemRow);
                count++;
            }

            if (count == 0) {
                VBox empty = new VBox(6);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(30));
                Text noMatch = new Text("No matching contacts found");
                noMatch.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px;");
                empty.getChildren().add(noMatch);
                contactListContainer.getChildren().add(empty);
            }
        };

        populateList.run();
        searchContactField.textProperty().addListener((obs, o, n) -> populateList.run());

        contentBox.getChildren().addAll(searchContactField, scrollPane);
        dialog.getDialogPane().setContent(contentBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
