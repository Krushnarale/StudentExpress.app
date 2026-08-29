package com.core2web.view.marketplace;

import com.core2web.Main;
import com.core2web.model.WalletTransaction;
import com.core2web.repository.DataRepository;
import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.List;

public class WalletPage {

    private Scene scene;
    private Text balAmountText;
    private double currentBalance = DataRepository.getInstance().getWalletBalance();
    private List<WalletTransaction> transactions = DataRepository.getInstance().getTransactions();

    public Node getPageNode(Runnable onBack) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));
        mainContent.setMaxWidth(780);

        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Text titleText = new Text("💳 StudentExpress Wallet");
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");
        HBox.setHgrow(titleText, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Profile");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        titleBox.getChildren().addAll(titleText, backBtn);

        // Wallet Balance Gradient Card
        StackPane balanceCardWrapper = new StackPane();
        VBox balanceCard = new VBox(14);
        balanceCard.setPadding(new Insets(24, 28, 24, 28));
        balanceCard.setStyle(
            "-fx-background-color: linear-gradient(to right, #2E4A18, #4F772D, #6A9E45);"
            + "-fx-background-radius: 18px;"
            + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.3), 16, 0, 0, 5);"
        );

        HBox cardTop = new HBox();
        cardTop.setAlignment(Pos.CENTER_LEFT);
        Text cardLabel = new Text("AVAILABLE BALANCE");
        cardLabel.setStyle("-fx-fill: rgba(255,255,255,0.75); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        HBox.setHgrow(cardLabel, Priority.ALWAYS);
        Text cardType = new Text("Student Wallet Pass");
        cardType.setStyle("-fx-fill: rgba(255,255,255,0.6); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px;");
        cardTop.getChildren().addAll(cardLabel, cardType);

        balAmountText = new Text("₹ " + String.format("%,.2f", currentBalance));
        balAmountText.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 38px; -fx-font-weight: 800;");

        HBox cardActionRow = new HBox(12);
        cardActionRow.setAlignment(Pos.CENTER_LEFT);

        Button addMoneyBtn = new Button("➕  Add Money");
        addMoneyBtn.setStyle(
            "-fx-background-color: white;"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 800;"
            + "-fx-font-size: 13px;"
            + "-fx-padding: 9px 20px;"
            + "-fx-background-radius: 10px;"
            + "-fx-cursor: hand;"
        );
        addMoneyBtn.setOnAction(e -> showAddMoneyDialog());

        Button sendMoneyBtn = new Button("↗  Send to Student");
        sendMoneyBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.18);"
            + "-fx-text-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 700;"
            + "-fx-font-size: 13px;"
            + "-fx-padding: 9px 18px;"
            + "-fx-background-radius: 10px;"
            + "-fx-cursor: hand;"
        );
        sendMoneyBtn.setOnAction(e -> showSendMoneyDialog());

        cardActionRow.getChildren().addAll(addMoneyBtn, sendMoneyBtn);

        Text cardHolder = new Text(DataRepository.getInstance().getCurrentUser().getName().toUpperCase() + "   •   ID: SE-88492");
        cardHolder.setStyle("-fx-fill: rgba(255,255,255,0.65); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px;");

        balanceCard.getChildren().addAll(cardTop, balAmountText, cardActionRow, cardHolder);
        balanceCardWrapper.getChildren().add(balanceCard);

        // ─── Quick action chips ─────────────────────────────────
        HBox quickChips = new HBox(12);
        quickChips.getChildren().addAll(
            quickChip("📱", "UPI Pay"),
            quickChip("🏦", "NetBanking"),
            quickChip("💳", "Debit Card"),
            quickChip("📦", "Pay for Order")
        );

        // ─── Payment Methods Card ───────────────────────────────
        VBox paymentMethodsCard = new VBox(0);
        paymentMethodsCard.setStyle(Theme.cardStyle());

        Label pmHeader = new Label("SAVED PAYMENT METHODS");
        pmHeader.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 14px 20px 10px 20px; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");
        paymentMethodsCard.getChildren().add(pmHeader);

        com.core2web.model.User curU = DataRepository.getInstance().getCurrentUser();
        if (curU == null) curU = com.core2web.util.SessionManager.getInstance().getCurrentUser();
        String upiId = (curU != null && curU.getEmail() != null && !curU.getEmail().isEmpty())
                ? curU.getEmail().split("@")[0] + "@upi" : "user@upi";

        paymentMethodsCard.getChildren().addAll(
            paymentMethodRow("📱", "Google Pay / PhonePe", upiId, "Primary UPI", true),
            paymentMethodRow("🏦", "State Bank of India", "••••  ••••  ••••  4591", "Savings Account", false)
        );

        // ─── Transaction History ────────────────────────────────
        VBox historyCard = new VBox(0);
        historyCard.setStyle(Theme.cardStyle());

        Label txHeader = new Label("RECENT TRANSACTIONS");
        txHeader.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 14px 20px 10px 20px; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");
        historyCard.getChildren().add(txHeader);

        var txList = DataRepository.getInstance().getTransactions();
        for (int i = 0; i < txList.size(); i++) {
            WalletTransaction tx = txList.get(i);
            boolean isLast = (i == txList.size() - 1);
            boolean isCredit = "CREDIT".equals(tx.getType());

            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14, 20, 14, 20));
            if (!isLast) row.setStyle("-fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

            // Icon badge
            StackPane txIcon = new StackPane();
            txIcon.setPrefSize(40, 40);
            txIcon.setMinSize(40, 40);
            txIcon.setStyle("-fx-background-color: " + (isCredit ? "#E6F4EA" : "#FFF5F5") + "; -fx-background-radius: 12px;");
            Text txIconTxt = new Text(isCredit ? "↓" : "↑");
            txIconTxt.setStyle("-fx-fill: " + (isCredit ? "#2E7D32" : "#C62828") + "; -fx-font-size: 18px; -fx-font-weight: 800;");
            txIcon.getChildren().add(txIconTxt);

            VBox txInfo = new VBox(3);
            HBox.setHgrow(txInfo, Priority.ALWAYS);
            Text t = new Text(tx.getTitle());
            t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 14px;");
            Text d = new Text("📅 " + tx.getDate());
            d.setStyle(Theme.mutedTextStyle());
            txInfo.getChildren().addAll(t, d);

            Text amt = new Text(tx.getAmount());
            amt.setStyle("-fx-fill: " + (isCredit ? "#2E7D32" : "#C62828") + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 800; -fx-font-size: 15px;");

            Label statusLbl = new Label(tx.getStatus());
            statusLbl.setStyle("SUCCESS".equals(tx.getStatus()) ? Theme.successBadgeStyle() : Theme.warningBadgeStyle());

            row.getChildren().addAll(txIcon, txInfo, amt, statusLbl);
            historyCard.getChildren().add(row);
        }

        mainContent.getChildren().addAll(titleBox, balanceCardWrapper, quickChips, paymentMethodsCard, historyCard);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onBack) {
        Node node = getPageNode(onBack);
        scene = new Scene(new BorderPane(node), 1050, 700);
        return scene;
    }

    private void showAddMoneyDialog() {
        TextInputDialog dialog = new TextInputDialog("500");
        dialog.setTitle("Add Funds");
        dialog.setHeaderText("Add Funds to Wallet via UPI / NetBanking");
        dialog.setContentText("Enter Amount (₹):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                double amt = Double.parseDouble(val);
                if (amt > 0) {
                    DataRepository.getInstance().addFunds(amt);
                    currentBalance = DataRepository.getInstance().getWalletBalance();
                    if (balAmountText != null) balAmountText.setText("₹ " + String.format("%,.2f", currentBalance));
                    showAlert("Success", "₹ " + amt + " added to your wallet successfully!");
                }
            } catch (Exception ex) {
                showAlert("Error", "Please enter a valid amount.");
            }
        });
    }

    private void showSendMoneyDialog() {
        TextInputDialog dialog = new TextInputDialog("200");
        dialog.setTitle("Send Money");
        dialog.setHeaderText("Transfer Funds to Peer Student");
        dialog.setContentText("Enter Amount (₹):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                double amt = Double.parseDouble(val);
                if (amt > 0 && amt <= currentBalance) {
                    DataRepository.getInstance().addFunds(-amt);
                    currentBalance = DataRepository.getInstance().getWalletBalance();
                    if (balAmountText != null) balAmountText.setText("₹ " + String.format("%,.2f", currentBalance));
                    showAlert("Transfer Complete", "₹ " + amt + " transferred to student wallet!");
                } else {
                    showAlert("Insufficient Funds", "Entered amount exceeds available wallet balance.");
                }
            } catch (Exception ex) {
                showAlert("Error", "Please enter a valid amount.");
            }
        });
    }

    private HBox quickChip(String icon, String label) {
        HBox chip = new HBox(8);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(10, 18, 10, 18));
        chip.setStyle(Theme.cardStyle() + "-fx-cursor: hand;");
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 16px;");
        Text labelTxt = new Text(label);
        labelTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        chip.getChildren().addAll(iconTxt, labelTxt);
        return chip;
    }

    private HBox paymentMethodRow(String icon, String title, String subtitle, String tag, boolean isLast) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        if (!isLast) row.setStyle("-fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(40, 40);
        iconBadge.setMinSize(40, 40);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 17px;");
        iconBadge.getChildren().add(iconTxt);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Text t = new Text(title);
        t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 14px;");
        Text s = new Text(subtitle);
        s.setStyle(Theme.mutedTextStyle());
        info.getChildren().addAll(t, s);

        Label tagLbl = new Label(tag);
        tagLbl.setStyle(Theme.badgeStyle());

        row.getChildren().addAll(iconBadge, info, tagLbl);
        return row;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
