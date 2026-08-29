package com.core2web.view.chatbot;

import com.core2web.service.GeminiService;
import com.core2web.util.AIConfig;
import com.core2web.util.Theme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class AIChatBot extends StackPane {

    private final GeminiService geminiService;
    private final VBox chatWindow;
    private final VBox messageContainer;
    private final ScrollPane chatScrollPane;
    private final TextField inputField;
    private final Button sendBtn;
    private final Button toggleBtn;
    private boolean isChatOpen = false;

    public AIChatBot() {
        this.geminiService = new GeminiService();

        // ─────────────────────────────────────────────────────────
        // 1. FLOATING TOGGLE BUTTON
        // ─────────────────────────────────────────────────────────
        toggleBtn = new Button("🤖  StudentExpress AI");
        toggleBtn.setStyle(
            "-fx-background-color: " + Theme.PRIMARY + ";"
            + "-fx-text-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 13.5px;"
            + "-fx-font-weight: 800;"
            + "-fx-padding: 10px 18px;"
            + "-fx-background-radius: 24px;"
            + "-fx-cursor: hand;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);"
        );
        toggleBtn.setOnAction(e -> toggleChatWindow());

        // ─────────────────────────────────────────────────────────
        // 2. CHAT CARD WINDOW (Initially Hidden)
        // ─────────────────────────────────────────────────────────
        chatWindow = new VBox(0);
        chatWindow.setPrefSize(370, 500);
        chatWindow.setMaxSize(370, 500);
        chatWindow.setVisible(false);
        chatWindow.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 18px;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 18px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 20, 0, 0, 6);"
        );

        // Header Bar
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle(
            "-fx-background-color: " + Theme.PRIMARY + ";"
            + "-fx-background-radius: 18px 18px 0 0;"
        );

        Text headerTitle = new Text("🤖  StudentExpress AI");
        headerTitle.setStyle(
            "-fx-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 15px;"
            + "-fx-font-weight: 800;"
        );
        HBox.setHgrow(headerTitle, Priority.ALWAYS);

        Button minimizeBtn = new Button("✕");
        minimizeBtn.setStyle(
            "-fx-background-color: transparent;"
            + "-fx-text-fill: rgba(255,255,255,0.85);"
            + "-fx-font-size: 14px;"
            + "-fx-font-weight: bold;"
            + "-fx-cursor: hand;"
            + "-fx-padding: 2px 6px;"
        );
        minimizeBtn.setOnAction(e -> toggleChatWindow());
        header.getChildren().addAll(headerTitle, minimizeBtn);

        // Message Container & ScrollPane
        messageContainer = new VBox(12);
        messageContainer.setPadding(new Insets(14, 14, 14, 14));
        messageContainer.setStyle("-fx-background-color: #FAF6EE;");

        chatScrollPane = new ScrollPane(messageContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setFitToHeight(true);
        chatScrollPane.setStyle("-fx-background-color: transparent; -fx-background: #FAF6EE;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Welcome initial message
        addAIMessage("Hi! 👋 I'm your StudentExpress AI assistant.\n\nHow can I help you find rooms, marketplace items, roommates, or student services today?");

        // Input Area
        HBox inputBar = new HBox(8);
        inputBar.setAlignment(Pos.CENTER_LEFT);
        inputBar.setPadding(new Insets(10, 14, 12, 14));
        inputBar.setStyle(
            "-fx-background-color: white;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-width: 1px 0 0 0;"
            + "-fx-background-radius: 0 0 18px 18px;"
        );

        inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.setStyle(Theme.inputFieldStyle() + " -fx-font-size: 13px; -fx-padding: 8px 12px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendBtn = new Button("➤");
        sendBtn.setStyle(
            "-fx-background-color: " + Theme.PRIMARY + ";"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 14px;"
            + "-fx-padding: 7px 14px;"
            + "-fx-background-radius: 8px;"
            + "-fx-cursor: hand;"
        );

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage();
            }
        });
        sendBtn.setOnAction(e -> handleSendMessage());
        inputBar.getChildren().addAll(inputField, sendBtn);
        chatWindow.getChildren().addAll(header, chatScrollPane, inputBar);

        // ─────────────────────────────────────────────────────────
        // 3. STACKPANE LAYOUT POSITIONING
        // ─────────────────────────────────────────────────────────
        setPickOnBounds(false); // Let mouse events pass through transparent areas
        setAlignment(Pos.BOTTOM_RIGHT);
        setMargin(toggleBtn, new Insets(0, 24, 24, 0));
        setMargin(chatWindow, new Insets(0, 24, 72, 0));

        getChildren().addAll(chatWindow, toggleBtn);
    }

    private void toggleChatWindow() {
        isChatOpen = !isChatOpen;
        chatWindow.setVisible(isChatOpen);
        if (isChatOpen) {
            inputField.requestFocus();
        }
    }

    private void handleSendMessage() {
        String text = inputField.getText() == null ? "" : inputField.getText().trim();
        if (text.isEmpty()) return;

        // Add user message bubble and clear the input
        addUserMessage(text);
        inputField.clear();

        // Check API Key before hitting the network
        if (!AIConfig.isApiKeyAvailable()) {
            addErrorMessage("Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable.");
            return;
        }

        // Loading state
        Node loadingBubble = createLoadingBubble();
        messageContainer.getChildren().add(loadingBubble);
        scrollToBottom();

        // Lock controls while task is running
        inputField.setDisable(true);
        sendBtn.setDisable(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return geminiService.sendMessage(text);
            }
        };

        task.setOnSucceeded(event -> {
            messageContainer.getChildren().remove(loadingBubble);
            inputField.setDisable(false);
            sendBtn.setDisable(false);
            String response = task.getValue();
            addAIMessage(response);
            inputField.requestFocus();
        });

        task.setOnFailed(event -> {
            messageContainer.getChildren().remove(loadingBubble);
            inputField.setDisable(false);
            sendBtn.setDisable(false);
            Throwable ex = task.getException();
            String errorMsg = null;
            if (ex != null && ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
                errorMsg = ex.getMessage();
            } else if (ex != null && ex.getCause() != null
                    && ex.getCause().getMessage() != null
                    && !ex.getCause().getMessage().trim().isEmpty()) {
                errorMsg = ex.getCause().getMessage();
            }
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "Unable to get a response from Gemini. Please try again.";
            }
            addErrorMessage(errorMsg);
            inputField.requestFocus();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void addUserMessage(String message) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = new VBox();
        bubble.setMaxWidth(260);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
            "-fx-background-color: " + Theme.PRIMARY + ";"
            + "-fx-background-radius: 14px 14px 2px 14px;"
        );

        Label txt = new Label(message);
        txt.setWrapText(true);
        txt.setStyle(
            "-fx-text-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 13px;"
        );
        bubble.getChildren().add(txt);
        row.getChildren().add(bubble);
        messageContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addAIMessage(String message) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = new VBox();
        bubble.setMaxWidth(270);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
            "-fx-background-color: white;"
            + "-fx-background-radius: 14px 14px 14px 2px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 14px 14px 14px 2px;"
        );

        Label txt = new Label(message);
        txt.setWrapText(true);
        txt.setStyle(
            "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 13px;"
            + "-fx-line-spacing: 3px;"
        );
        bubble.getChildren().add(txt);
        row.getChildren().add(bubble);
        messageContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addErrorMessage(String error) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = new VBox();
        bubble.setMaxWidth(270);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
            "-fx-background-color: #FFF0F0;"
            + "-fx-background-radius: 14px 14px 14px 2px;"
            + "-fx-border-color: #FFCDD2;"
            + "-fx-border-radius: 14px 14px 14px 2px;"
        );

        Label txt = new Label("⚠️ " + error);
        txt.setWrapText(true);
        txt.setStyle(
            "-fx-text-fill: #D32F2F;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 12.5px;"
        );
        bubble.getChildren().add(txt);
        row.getChildren().add(bubble);
        messageContainer.getChildren().add(row);
        scrollToBottom();
    }

    private Node createLoadingBubble() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = new VBox();
        bubble.setPadding(new Insets(8, 14, 8, 14));
        bubble.setStyle(
            "-fx-background-color: white;"
            + "-fx-background-radius: 14px;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 14px;"
        );

        Label txt = new Label("🤖 StudentExpress AI is thinking...");
        txt.setStyle(
            "-fx-text-fill: " + Theme.TEXT_MUTED + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 12px;"
            + "-fx-font-style: italic;"
        );
        bubble.getChildren().add(txt);
        row.getChildren().add(bubble);
        return row;
    }

    private void scrollToBottom() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }
}
