package com.core2web.view;

import com.core2web.Main;
import com.core2web.model.Order;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.List;

public class MyOrdersPage {

    private Scene scene;

    public Node getPageNode(Runnable onBack) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25, 40, 25, 40));
        mainContent.setMaxWidth(750);

        HBox headerBox = new HBox(15);
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

        VBox ordersList = new VBox(15);

        Runnable refreshOrders = () -> {
            ordersList.getChildren().clear();
            com.core2web.controller.OrderController controller = new com.core2web.controller.OrderController();
            javafx.concurrent.Task<List<Order>> task = controller.fetchBuyerOrdersTask();

            task.setOnSucceeded(ev -> {
                List<Order> orders = task.getValue();
                renderOrderCards(ordersList, orders);
            });

            task.setOnFailed(ev -> {
                com.core2web.controller.OrderController ctrl = new com.core2web.controller.OrderController();
                renderOrderCards(ordersList, ctrl.getAllOrders());
            });

            new Thread(task).start();
        };

        refreshOrders.run();

        mainContent.getChildren().addAll(headerBox, ordersList);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(mainContent);

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
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

    private void renderOrderCards(VBox ordersList, List<Order> orders) {
        ordersList.getChildren().clear();

        if (orders == null || orders.isEmpty()) {
            EmptyStateNode emptyState = new EmptyStateNode(
                "No Orders Found",
                "You haven't placed any orders yet.",
                null
            );
            ordersList.getChildren().add(emptyState);
        } else {
            for (Order o : orders) {
                VBox card = new VBox(12);
                card.setPadding(new Insets(18));
                card.setStyle(Theme.cardStyle());

                HBox topRow = new HBox(10);
                topRow.setAlignment(Pos.CENTER_LEFT);
                Text itemTitle = new Text(o.getItemName() != null ? o.getItemName() : "Order Item");
                itemTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                HBox.setHgrow(itemTitle, Priority.ALWAYS);

                Label statusBadge = new Label(o.getStatus() != null ? o.getStatus() : "PLACED");
                statusBadge.setStyle(Theme.successBadgeStyle());
                topRow.getChildren().addAll(itemTitle, statusBadge);

                Text detailsText = new Text("Price: " + (o.getPrice() != null ? o.getPrice() : "N/A") + "  •  Date: " + (o.getDate() != null ? o.getDate() : "N/A") + "  •  Tracking ID: " + (o.getTrackingId() != null ? o.getTrackingId() : "N/A"));
                detailsText.setStyle(Theme.mutedTextStyle());

                HBox actionsRow = new HBox(10);
                actionsRow.setAlignment(Pos.CENTER_RIGHT);
                Button trackBtn = new Button("Track Order");
                trackBtn.setStyle(Theme.secondaryBtnStyle());
                trackBtn.setOnAction(ev2 -> showAlert("Tracking Order", "Order ID: " + o.getId() + "\nStatus: " + o.getStatus() + "\nTracking ID: " + (o.getTrackingId() != null ? o.getTrackingId() : "N/A")));

                Button invoiceBtn = new Button("Invoice");
                invoiceBtn.setStyle(Theme.outlineBtnStyle());
                invoiceBtn.setOnAction(ev2 -> showAlert("Invoice Generated", "Invoice for order " + o.getId() + " (" + o.getPrice() + ") downloaded."));

                actionsRow.getChildren().addAll(trackBtn, invoiceBtn);
                card.getChildren().addAll(topRow, detailsText, actionsRow);
                ordersList.getChildren().add(card);
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
