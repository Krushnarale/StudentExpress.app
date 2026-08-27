package com.core2web.view;

import com.core2web.Main;
import com.core2web.model.Booking;
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
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));
        mainContent.setMaxWidth(780);

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

        VBox bookingsList = new VBox(16);

        Runnable[] refreshArr = new Runnable[1];
        refreshArr[0] = () -> {
            bookingsList.getChildren().clear();
            String activeUid = com.core2web.util.SessionManager.getInstance().getUid();
            
            com.core2web.controller.BookingController controller = new com.core2web.controller.BookingController();
            javafx.concurrent.Task<List<Booking>> task = controller.fetchUserBookingsTask();

            task.setOnSucceeded(taskEv -> {
                List<Booking> list = task.getValue();
                bookingsList.getChildren().clear();
                if (list == null || list.isEmpty()) {
                    EmptyStateNode empty = new EmptyStateNode(
                        "No Active Bookings",
                        "You have no upcoming or past service or room bookings.",
                        null
                    );
                    bookingsList.getChildren().add(empty);
                    return;
                }

                for (Booking b : list) {
                VBox card = new VBox(0);
                card.setStyle(
                    "-fx-background-color: " + Theme.CARD_BG + ";"
                    + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
                    + "-fx-border-radius: 14px;"
                    + "-fx-background-radius: 14px;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
                );

                String status = b.getStatus();
                String accentColor = "CONFIRMED".equals(status) ? "#2563EB"
                    : "PENDING".equals(status) ? "#D97706"
                    : "#4F772D";

                Rectangle topAccent = new Rectangle();
                topAccent.setWidth(700);
                topAccent.setHeight(4);
                topAccent.setFill(Color.web(accentColor));

                VBox cardBody = new VBox(12);
                cardBody.setPadding(new Insets(16, 20, 20, 20));

                HBox topRow = new HBox(12);
                topRow.setAlignment(Pos.CENTER_LEFT);

                StackPane iconBadge = new StackPane();
                iconBadge.setPrefSize(44, 44);
                iconBadge.setMinSize(44, 44);
                iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
                String catIconPath = "Room".equalsIgnoreCase(b.getCategory()) ? IconFactory.PATH_KEY
                    : "Service".equalsIgnoreCase(b.getCategory()) ? IconFactory.PATH_WRENCH : IconFactory.PATH_SHOPPING_BAG;
                Node iconNode = IconFactory.getIconNode(catIconPath, Theme.PRIMARY, 20);
                iconBadge.getChildren().add(iconNode);

                VBox titleInfo = new VBox(3);
                HBox.setHgrow(titleInfo, Priority.ALWAYS);
                Text itemTitle = new Text(b.getItemOrServiceName());
                itemTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 800;");
                Text categoryLabel = new Text(b.getCategory() + " Booking  •  ID: " + b.getId());
                categoryLabel.setStyle(Theme.mutedTextStyle());
                titleInfo.getChildren().addAll(itemTitle, categoryLabel);

                Label statusBadge = new Label(status);
                statusBadge.setStyle("CONFIRMED".equals(status) ? Theme.successBadgeStyle()
                    : "PENDING".equals(status) ? Theme.warningBadgeStyle() : Theme.badgeStyle());

                topRow.getChildren().addAll(iconBadge, titleInfo, statusBadge);

                HBox detailsRow = new HBox(24);
                detailsRow.setPadding(new Insets(10, 14, 10, 14));
                detailsRow.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 10px;");

                detailsRow.getChildren().addAll(
                    detailItem("Date", b.getDate()),
                    detailItem("Category", b.getCategory()),
                    detailItem("Booking ID", b.getId())
                );

                HBox actionsRow = new HBox(12);
                Button viewReceiptBtn = new Button("View Receipt");
                viewReceiptBtn.setStyle(Theme.secondaryBtnStyle());
                viewReceiptBtn.setOnAction(ev -> showAlert("Booking Receipt",
                    "Booking ID: " + b.getId()
                    + "\nItem: " + b.getItemOrServiceName()
                    + "\nDate: " + b.getDate()
                    + "\nCategory: " + b.getCategory()
                    + "\nStatus: " + b.getStatus()));

                Button cancelBtn = new Button("Cancel Booking");
                cancelBtn.setStyle(Theme.dangerBtnStyle());
                cancelBtn.setOnAction(ev -> {
                    controller.cancelBooking(b.getId());
                    showAlert("Cancelled", "Booking '" + b.getItemOrServiceName() + "' has been cancelled.");
                    refreshArr[0].run();
                });

                actionsRow.getChildren().addAll(viewReceiptBtn, cancelBtn);
                cardBody.getChildren().addAll(topRow, detailsRow, actionsRow);
                card.getChildren().addAll(topAccent, cardBody);
                bookingsList.getChildren().add(card);
            }
            });

            new Thread(task).start();
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
