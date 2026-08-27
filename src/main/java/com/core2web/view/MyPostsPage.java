package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.ProductController;
import com.core2web.controller.SellerController;
import com.core2web.model.ProductItem;
import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class MyPostsPage {

    private Scene scene;
    private final SellerController sellerController = new SellerController();
    private final ProductController productController = new ProductController();

    public Node getPageNode(Runnable onBack, Runnable onPostNew) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));
        mainContent.setMaxWidth(780);

        HBox headingRow = new HBox(16);
        headingRow.setAlignment(Pos.CENTER_LEFT);
        Text titleText = new Text("📦 My Listings & Posts");
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");
        HBox.setHgrow(titleText, Priority.ALWAYS);

        Button newPostBtn = new Button("➕ Post New Item");
        newPostBtn.setStyle(Theme.primaryBtnStyle());
        newPostBtn.setOnAction(e -> { if (onPostNew != null) onPostNew.run(); });

        Button backBtn = new Button("← Back to Profile");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        headingRow.getChildren().addAll(titleText, newPostBtn, backBtn);

        Text headingSub = new Text("Manage your active items for sale, rental properties, and roommate requests.");
        headingSub.setStyle(Theme.mutedTextStyle());

        HBox miniStats = new HBox(14);
        miniStats.getChildren().addAll(
            miniStatCard("📦", String.valueOf(sellerController.getSellerProducts().size()), "Active Listings", Theme.PRIMARY),
            miniStatCard("👁️", "148", "Total Views", "#2563EB"),
            miniStatCard("💬", "12", "Inquiries", "#D97706")
        );

        VBox postsList = new VBox(14);
        var products = sellerController.getSellerProducts();

        if (products.isEmpty()) {
            VBox empty = new VBox(16);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(50, 30, 50, 30));
            empty.setStyle(Theme.cardStyle());
            Text emptyIcon = new Text("📦");
            emptyIcon.setStyle("-fx-font-size: 48px;");
            Text emptyTxt = new Text("You haven't posted any items yet.");
            emptyTxt.setStyle(Theme.mutedTextStyle());

            Button postFirstBtn = new Button("➕ Post Your First Item");
            postFirstBtn.setStyle(Theme.primaryBtnStyle());
            postFirstBtn.setOnAction(e -> { if (onPostNew != null) onPostNew.run(); });

            empty.getChildren().addAll(emptyIcon, emptyTxt, postFirstBtn);
            postsList.getChildren().add(empty);
        } else {
            for (ProductItem p : products) {
                VBox card = new VBox(12);
                card.setPadding(new Insets(18, 20, 18, 20));
                card.setStyle(Theme.cardStyle());

                HBox topRow = new HBox(12);
                topRow.setAlignment(Pos.CENTER_LEFT);
                Text itemTitle = new Text(p.getTitle());
                itemTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 800;");
                HBox.setHgrow(itemTitle, Priority.ALWAYS);

                Text priceTag = new Text(p.getPrice());
                priceTag.setStyle(Theme.priceTextStyle());

                Label statusBadge = new Label("● Active");
                statusBadge.setStyle(Theme.successBadgeStyle());

                topRow.getChildren().addAll(itemTitle, priceTag, statusBadge);

                HBox detailRow = new HBox(20);
                detailRow.setAlignment(Pos.CENTER_LEFT);
                Text catTxt = new Text("📁 " + p.getCategory());
                catTxt.setStyle(Theme.mutedTextStyle());
                Text locTxt = new Text("📍 " + p.getLocation());
                locTxt.setStyle(Theme.mutedTextStyle());
                Text condTxt = new Text("✨ " + p.getCondition());
                condTxt.setStyle(Theme.mutedTextStyle());
                detailRow.getChildren().addAll(catTxt, locTxt, condTxt);

                HBox actionsRow = new HBox(10);
                actionsRow.setAlignment(Pos.CENTER_RIGHT);
                Button editBtn = new Button("✏ Edit");
                editBtn.setStyle(Theme.secondaryBtnStyle());
                editBtn.setOnAction(e -> showAlert("Edit Listing", "Editing: " + p.getTitle()));

                Button markSoldBtn = new Button("✓ Mark as Sold");
                markSoldBtn.setStyle(Theme.outlineBtnStyle());
                markSoldBtn.setOnAction(e -> showAlert("Status Updated", p.getTitle() + " marked as sold."));

                Button delBtn = new Button("🗑 Delete");
                delBtn.setStyle(Theme.dangerBtnStyle());
                delBtn.setOnAction(e -> {
                    productController.removeProduct(p.getId());
                    showAlert("Deleted", p.getTitle() + " has been deleted.");
                });

                actionsRow.getChildren().addAll(editBtn, markSoldBtn, delBtn);
                card.getChildren().addAll(topRow, detailRow, actionsRow);
                postsList.getChildren().add(card);
            }
        }

        mainContent.getChildren().addAll(headingRow, headingSub, miniStats, postsList);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onBack, Runnable onPostNew) {
        Node node = getPageNode(onBack, onPostNew);
        scene = new Scene(new BorderPane(node), 1050, 700);
        return scene;
    }

    private HBox miniStatCard(String icon, String value, String label, String accentColor) {
        HBox b = new HBox(12);
        b.setPrefWidth(180);
        b.setPadding(new Insets(14, 16, 14, 16));
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(Theme.statCardStyle(accentColor));

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(36, 36);
        iconBadge.setMinSize(36, 36);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 15px;");
        iconBadge.getChildren().add(iconTxt);

        VBox textBlock = new VBox(2);
        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");
        Text lblTxt = new Text(label);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        textBlock.getChildren().addAll(valTxt, lblTxt);

        b.getChildren().addAll(iconBadge, textBlock);
        return b;
    }

    private VBox detailItem(String icon, String label, String value) {
        VBox b = new VBox(2);
        Text labelTxt = new Text(icon + " " + label);
        labelTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        Text valueTxt = new Text(value != null ? value : "—");
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
