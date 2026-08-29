package com.core2web.view.marketplace;

import com.core2web.Main;
import com.core2web.model.Order;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class MyOrdersPage {

    private Scene scene;

    public Node getPageNode(Runnable onBack) {
        VBox mainContent = new VBox(14);
        mainContent.setPadding(new Insets(20, 30, 20, 30));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back to Profile");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        Node packageIcon = IconFactory.getIconNode(IconFactory.PATH_PACKAGE, Theme.PRIMARY, 24);
        Text titleText = new Text("My Orders & Purchases");
        titleText.setStyle(Theme.titleTextStyle());

        HBox titleBox = new HBox(8, packageIcon, titleText);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        headerBox.getChildren().addAll(titleBox, new Region(), backBtn);
        HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS);

        VBox ordersList = new VBox(12);
        ordersList.setMaxWidth(Double.MAX_VALUE);
        var orders = DataRepository.getInstance().getOrders();

        if (orders.isEmpty()) {
            EmptyStateNode emptyState = new EmptyStateNode(
                "No Orders Found",
                "You haven't placed any orders yet.",
                null
            );
            ordersList.getChildren().add(emptyState);
        } else {
            for (Order o : orders) {
                VBox card = new VBox(10);
                card.setMaxWidth(Double.MAX_VALUE);
                card.setPadding(new Insets(14, 18, 14, 18));
                card.setStyle(
                    "-fx-background-color: " + Theme.CARD_BG + ";"
                    + "-fx-border-color: " + Theme.PRIMARY + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + ";"
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
                Node iconNode = IconFactory.getIconNode(IconFactory.PATH_PACKAGE, Theme.PRIMARY, 18);
                iconBadge.getChildren().add(iconNode);

                VBox titleInfo = new VBox(2);
                HBox.setHgrow(titleInfo, Priority.ALWAYS);

                Text itemTitle = new Text(o.getItemName());
                itemTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");
                Text subText = new Text("Order ID: " + o.getId());
                subText.setStyle(Theme.mutedTextStyle());
                titleInfo.getChildren().addAll(itemTitle, subText);

                Label statusBadge = new Label(o.getStatus());
                statusBadge.setStyle(Theme.successBadgeStyle());
                topRow.getChildren().addAll(iconBadge, titleInfo, statusBadge);

                HBox infoRow = new HBox(40);
                infoRow.setPadding(new Insets(8, 14, 8, 14));
                infoRow.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 8px;");
                HBox.setHgrow(infoRow, Priority.ALWAYS);

                infoRow.getChildren().addAll(
                    detailItem("Price", o.getPrice()),
                    detailItem("Date", o.getDate()),
                    detailItem("Category", o.getCategory())
                );

                HBox actionsRow = new HBox(10);
                actionsRow.setAlignment(Pos.CENTER_RIGHT);

                Button trackBtn = new Button("Track Order");
                trackBtn.setPrefHeight(30);
                trackBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-padding: 4px 12px; -fx-font-size: 12px;");
                trackBtn.setOnAction(ev -> showAlert("Tracking Order", "Order Status: " + o.getStatus() + "\nEstimated Delivery: Tomorrow by 5:00 PM"));

                Button invoiceBtn = new Button("Invoice");
                invoiceBtn.setPrefHeight(30);
                invoiceBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 4px 12px; -fx-font-size: 12px;");
                invoiceBtn.setOnAction(ev -> showAlert("Invoice Generated", "Invoice for order " + o.getId() + " (" + o.getPrice() + ") downloaded."));

                actionsRow.getChildren().addAll(trackBtn, invoiceBtn);
                card.getChildren().addAll(topRow, infoRow, actionsRow);
                ordersList.getChildren().add(card);
            }
        }

        mainContent.getChildren().addAll(headerBox, ordersList);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(mainContent);

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
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

    public Scene getPageScene(Runnable onBack) {
        Node node = getPageNode(onBack);
        scene = new Scene(new BorderPane(node), 1050, 700);
        return scene;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
