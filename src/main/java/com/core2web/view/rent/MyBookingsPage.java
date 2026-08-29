package com.core2web.view.rent;

import com.core2web.Main;
import com.core2web.model.Booking;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;

public class MyBookingsPage {

    private Scene scene;

    public Node getPageNode(Runnable onBack) {
        VBox mainContent = new VBox(14);
        mainContent.setPadding(new Insets(20, 30, 20, 30));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        HBox headingRow = new HBox(12);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        Node calHeaderIcon = IconFactory.getIconNode(IconFactory.PATH_CALENDAR, Theme.PRIMARY, 24);
        Text headingText = new Text("My Service Bookings");
        headingText.setStyle(Theme.titleTextStyle());

        HBox titleBox = new HBox(8, calHeaderIcon, headingText);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Profile");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        headingRow.getChildren().addAll(titleBox, backBtn);

        Text headingSub = new Text("Track your active room bookings, service appointments, and rental confirmations.");
        headingSub.setStyle(Theme.mutedTextStyle());

        VBox bookingsList = new VBox(12);
        bookingsList.setMaxWidth(Double.MAX_VALUE);

        Runnable[] refreshArr = new Runnable[1];
        refreshArr[0] = () -> {
            bookingsList.getChildren().clear();
            List<Booking> list = DataRepository.getInstance().getBookings();
            if (list.isEmpty()) {
                EmptyStateNode empty = new EmptyStateNode(
                    "No Active Bookings",
                    "You have no upcoming or past service bookings.",
                    null
                );
                bookingsList.getChildren().add(empty);
                return;
            }

            for (Booking b : list) {
                VBox card = new VBox(10);
                card.setMaxWidth(Double.MAX_VALUE);
                card.setPadding(new Insets(14, 18, 14, 18));

                String status = b.getStatus();
                String accentColor = "CONFIRMED".equals(status) ? "#2563EB"
                    : "PENDING".equals(status) ? "#D97706"
                    : "#4F772D";

                card.setStyle(
                    "-fx-background-color: " + Theme.CARD_BG + ";"
                    + "-fx-border-color: " + accentColor + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + ";"
                    + "-fx-border-width: 3px 1px 1px 1px;"
                    + "-fx-border-radius: 12px;"
                    + "-fx-background-radius: 12px;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
                );

                HBox topRow = new HBox(12);
                topRow.setAlignment(Pos.CENTER_LEFT);

                StackPane iconBadge = new StackPane();
                iconBadge.setPrefSize(38, 38);
                iconBadge.setMinSize(38, 38);
                iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
                String catIconPath = "Room".equalsIgnoreCase(b.getCategory()) ? IconFactory.PATH_KEY
                    : "Service".equalsIgnoreCase(b.getCategory()) ? IconFactory.PATH_WRENCH : IconFactory.PATH_SHOPPING_BAG;
                Node iconNode = IconFactory.getIconNode(catIconPath, Theme.PRIMARY, 18);
                iconBadge.getChildren().add(iconNode);

                VBox titleInfo = new VBox(2);
                HBox.setHgrow(titleInfo, Priority.ALWAYS);
                Text itemTitle = new Text(b.getItemOrServiceName());
                itemTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");
                Text categoryLabel = new Text(b.getCategory() + " Booking  •  ID: " + b.getId());
                categoryLabel.setStyle(Theme.mutedTextStyle());
                titleInfo.getChildren().addAll(itemTitle, categoryLabel);

                Label statusBadge = new Label(status);
                statusBadge.setStyle("CONFIRMED".equals(status) ? Theme.successBadgeStyle()
                    : "PENDING".equals(status) ? Theme.warningBadgeStyle() : Theme.badgeStyle());

                topRow.getChildren().addAll(iconBadge, titleInfo, statusBadge);

                HBox detailsRow = new HBox(40);
                detailsRow.setPadding(new Insets(8, 14, 8, 14));
                detailsRow.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 8px;");
                HBox.setHgrow(detailsRow, Priority.ALWAYS);

                detailsRow.getChildren().addAll(
                    detailItem("Date", b.getDate()),
                    detailItem("Category", b.getCategory()),
                    detailItem("Booking ID", b.getId())
                );

                HBox actionsRow = new HBox(10);
                actionsRow.setAlignment(Pos.CENTER_RIGHT);

                Button viewReceiptBtn = new Button("View Receipt");
                viewReceiptBtn.setPrefHeight(30);
                viewReceiptBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-padding: 4px 12px; -fx-font-size: 12px;");
                viewReceiptBtn.setOnAction(ev -> showAlert("Booking Receipt",
                    "Booking ID: " + b.getId()
                    + "\nItem: " + b.getItemOrServiceName()
                    + "\nDate: " + b.getDate()
                    + "\nCategory: " + b.getCategory()
                    + "\nStatus: " + b.getStatus()));

                Button cancelBtn = new Button("Cancel Booking");
                cancelBtn.setPrefHeight(30);
                cancelBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 4px 12px; -fx-font-size: 12px;");
                cancelBtn.setOnAction(ev -> {
                    DataRepository.getInstance().getBookings().remove(b);
                    showAlert("Cancelled", "Booking '" + b.getItemOrServiceName() + "' has been cancelled.");
                    refreshArr[0].run();
                });

                actionsRow.getChildren().addAll(viewReceiptBtn, cancelBtn);
                card.getChildren().addAll(topRow, detailsRow, actionsRow);
                bookingsList.getChildren().add(card);
            }
        };

        refreshArr[0].run();
        mainContent.getChildren().addAll(headingRow, headingSub, bookingsList);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private VBox detailItem(String label, String value) {
        VBox b = new VBox(2);
        Text labelTxt = new Text(label);
        labelTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        Text valueTxt = new Text(value);
        valueTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        b.getChildren().addAll(labelTxt, valueTxt);
        return b;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
