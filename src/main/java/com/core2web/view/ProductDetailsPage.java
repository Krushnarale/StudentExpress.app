package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.OrderController;
import com.core2web.controller.ProductController;
import com.core2web.model.ProductItem;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;
import java.io.File;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ProductDetailsPage {

    private final ProductController productController = new ProductController();
    private final OrderController orderController = new OrderController();

    public Node getPageNode(ProductItem item, Runnable backCallback) {
        ProductItem p = item != null ? item : productController.getAllProducts().get(0);

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        // Back Link
        Button backBtn = new Button("← Back to Marketplace");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (backCallback != null) backCallback.run(); });

        HBox columnsBox = new HBox(24);

        // Left Column (Product Image + Info Cards)
        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Header Title Box
        VBox titleBox = new VBox(6);
        Text titleText = new Text(p.getTitle());
        titleText.setStyle(Theme.titleTextStyle());

        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label catBadge = new Label(p.getCategory());
        catBadge.setStyle(Theme.badgeStyle());

        Label condBadge = new Label(p.getCondition());
        condBadge.setStyle(Theme.successBadgeStyle());

        Text locText = new Text("📍 " + p.getLocation() + "  •  Posted " + p.getTimePosted());
        locText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, condBadge, locText);
        titleBox.getChildren().addAll(titleText, metaRow);

        // Image Frame Box
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(500, 300);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");

        Image img = com.core2web.util.ImageUtil.loadImage(p.getImagePath());
        if (img != null) {
            try {
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(500);
                imgView.setFitHeight(300);
                imgView.setPreserveRatio(false);
                Rectangle clip = new Rectangle(500, 300);
                clip.setArcWidth(12); clip.setArcHeight(12);
                imgView.setClip(clip);
                imgBox.getChildren().add(imgView);
            } catch (Exception e) {}
        }

        VBox descCard = createContentCard("Product Description");
        Text descText = new Text(p.getDescription() != null ? p.getDescription() : "Well maintained item available for sale.");
        descText.setStyle(Theme.bodyTextStyle());
        descCard.getChildren().add(descText);

        leftColumn.getChildren().addAll(titleBox, imgBox, descCard);

        // Right Column (Buy & Contact Seller Card)
        VBox rightColumn = new VBox(20);
        rightColumn.setMinWidth(360);
        rightColumn.setMaxWidth(360);

        VBox actionCard = new VBox(14);
        actionCard.setPadding(new Insets(20));
        actionCard.setStyle(Theme.cardStyle());

        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Text starIcon = new Text("★");
        starIcon.setStyle("-fx-fill: #D97706; -fx-font-size: 16px;");
        Text ratingTxt = new Text("4.9  (14 Verified Buyers)");
        ratingTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 13px;");
        ratingRow.getChildren().addAll(starIcon, ratingTxt);

        Text priceText = new Text(p.getPrice());
        priceText.setStyle(Theme.priceTextStyle());

        Text sellerText = new Text("Sold by: " + (p.getSellerName() != null ? p.getSellerName() : "Peer Student"));
        sellerText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 600; -fx-font-size: 14px;");

        Button buyBtn = new Button("⚡ Buy Now");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setStyle(Theme.primaryBtnStyle());
        buyBtn.setOnAction(e -> {
            String buyerId = com.core2web.util.SessionManager.getInstance().getUid();
            String sellerId = p.getSellerUid() != null ? p.getSellerUid() : "1";
            String orderId = "ord_" + System.currentTimeMillis();
            String trackingId = com.core2web.util.TrackingUtil.generateTrackingId();
            String dateStr = java.time.LocalDate.now().toString();

            com.core2web.model.Order order = new com.core2web.model.Order(
                orderId,
                p.getTitle(),
                p.getPrice(),
                dateStr,
                "PLACED",
                trackingId,
                p.getCategory() != null ? p.getCategory() : "General",
                buyerId != null ? buyerId : "default_user",
                sellerId,
                p.getId(),
                System.currentTimeMillis()
            );

            orderController.createOrder(order);
            showAlert("Order Placed Successfully!", "Your order for '" + p.getTitle() + "' has been placed!\nTracking ID: " + trackingId + "\nStatus: PLACED");
        });

        Button chatBtn = new Button("💬 Message Seller");
        chatBtn.setMaxWidth(Double.MAX_VALUE);
        chatBtn.setStyle(Theme.secondaryBtnStyle());
        chatBtn.setOnAction(e -> showAlert("Chat Started", "Opening chat window with " + p.getSellerName()));

        Button callBtn = new Button("📞 Call " + p.getSellerPhone());
        callBtn.setMaxWidth(Double.MAX_VALUE);
        callBtn.setStyle(Theme.outlineBtnStyle());
        callBtn.setOnAction(e -> showAlert("Calling Seller", "Calling " + p.getSellerPhone()));

        actionCard.getChildren().addAll(ratingRow, priceText, sellerText, buyBtn, chatBtn, callBtn);
        rightColumn.getChildren().add(actionCard);

        columnsBox.getChildren().addAll(leftColumn, rightColumn);

        // Similar Products Section at Bottom
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Similar Products in Marketplace");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        HBox simCardsBox = new HBox(16);
        List<ProductItem> allProds = productController.getAllProducts();
        int added = 0;
        for (ProductItem prod : allProds) {
            if (!prod.getId().equals(p.getId())) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    prod.getId(), ListingCardNode.CardType.PRODUCT, "AVAILABLE",
                    prod.getTitle(), prod.getLocation(), prod.getPrice(), prod.getTimePosted(),
                    prod.getImagePath(), prod.getCategory(), () -> Main.showProductDetailsPage(prod)
                ));
                added++;
                if (added >= 4) break;
            }
        }
        similarSection.getChildren().addAll(simTitle, simCardsBox);

        mainContent.getChildren().addAll(backBtn, columnsBox, similarSection);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private void updateHeartBtn(Button btn, boolean isSaved) {
        String path = isSaved ? IconFactory.PATH_HEART_FILLED : IconFactory.PATH_HEART_OUTLINE;
        String color = isSaved ? "#E53E3E" : "#4B5563";
        btn.setGraphic(IconFactory.getIconNode(path, color, 16));
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 8px; -fx-cursor: hand;");
    }

    public Scene getPageScene(ProductItem item, Runnable backCallback) {
        Node node = getPageNode(item, backCallback);
        return new Scene(new BorderPane(node), 1050, 700);
    }

    private VBox createContentCard(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle(Theme.cardStyle());
        Text t = new Text(title);
        t.setStyle(Theme.sectionHeaderStyle());
        box.getChildren().add(t);
        return box;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
