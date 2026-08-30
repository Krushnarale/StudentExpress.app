package com.core2web.view.marketplace;

import com.core2web.Main;
import com.core2web.dao.OrderDAOImpl;
import com.core2web.dao.ProductDAOImpl;
import com.core2web.model.Order;
import com.core2web.model.ProductItem;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ProductDetailsPage {

    public Node getPageNode(ProductItem item, Runnable backCallback) {
        ProductItem p = item != null ? item : (!DataRepository.getInstance().getProducts().isEmpty() ? DataRepository.getInstance().getProducts().get(0) : null);

        if (p == null) {
            VBox empty = new VBox(20);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(50));
            Text t = new Text("Product Listing Not Found");
            t.setStyle(Theme.sectionHeaderStyle());
            Button b = new Button("← Back to Marketplace");
            b.setStyle(Theme.primaryBtnStyle());
            b.setOnAction(e -> { if (backCallback != null) backCallback.run(); });
            empty.getChildren().addAll(t, b);
            return empty;
        }

        User currentUser = DataRepository.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "";
        boolean isMyItem = currentUid != null && !currentUid.isEmpty() && currentUid.equalsIgnoreCase(p.getSellerUid());

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(20, 32, 28, 32));

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

        Label condBadge = new Label(p.getCondition() != null ? p.getCondition() : "Good Condition");
        condBadge.setStyle(Theme.successBadgeStyle());

        Text locText = new Text("📍 " + p.getLocation() + "  •  Posted " + p.getTimePosted());
        locText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, condBadge, locText);
        if (isMyItem) {
            Label myBadge = new Label("YOUR LISTING");
            myBadge.setStyle(Theme.warningBadgeStyle());
            metaRow.getChildren().add(myBadge);
        }
        titleBox.getChildren().addAll(titleText, metaRow);

        // Product Image Box
        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(320);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 14px;");

        Image img = ImageUtil.loadImage(p.getImagePath());
        if (img != null && !img.isError()) {
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(520);
            imgView.setFitHeight(320);
            imgView.setPreserveRatio(false);

            Rectangle clip = new Rectangle(520, 320);
            clip.setArcWidth(14); clip.setArcHeight(14);
            imgView.setClip(clip);

            imgBox.getChildren().add(imgView);
        } else {
            Node icon = IconFactory.getIconNode(IconFactory.PATH_SHOPPING_BAG, Theme.PRIMARY, 60);
            imgBox.getChildren().add(icon);
        }

        // Save / Favorite Toggle
        DataRepository repo = DataRepository.getInstance();
        boolean isSaved = repo.getSavedProductIds().contains(p.getId());
        Button heartBtn = new Button();
        updateHeartBtn(heartBtn, isSaved);

        heartBtn.setOnAction(e -> {
            boolean nowSaved = repo.toggleSavedProduct(p.getId());
            updateHeartBtn(heartBtn, nowSaved);
            showAlert(nowSaved ? "Saved" : "Removed", nowSaved ? "'" + p.getTitle() + "' saved to favorites!" : "'" + p.getTitle() + "' removed from favorites.");
        });
        StackPane.setAlignment(heartBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(heartBtn, new Insets(14));
        imgBox.getChildren().add(heartBtn);

        // Description Card
        VBox descCard = createContentCard("Product Description");
        Text descBody = new Text(p.getDescription() != null && !p.getDescription().isEmpty() ? p.getDescription() : "Pre-owned student item in verified condition.");
        descBody.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-line-spacing: 4px;");
        descCard.getChildren().add(descBody);

        // Specifications Card
        VBox specsCard = createContentCard("Specifications & Details");
        GridPane specsGrid = new GridPane();
        specsGrid.setHgap(24);
        specsGrid.setVgap(10);
        specsGrid.add(new Text("Category:"), 0, 0); specsGrid.add(new Text(p.getCategory()), 1, 0);
        specsGrid.add(new Text("Condition:"), 0, 1); specsGrid.add(new Text(p.getCondition() != null ? p.getCondition() : "Good"), 1, 1);
        specsGrid.add(new Text("Location:"), 0, 2); specsGrid.add(new Text(p.getLocation()), 1, 2);
        specsGrid.add(new Text("Contact Preference:"), 0, 3); specsGrid.add(new Text(p.getContactPreference() != null ? p.getContactPreference() : "Phone / Message"), 1, 3);
        specsGrid.add(new Text("Seller Status:"), 0, 4); specsGrid.add(new Text("✓ Verified Student Seller"), 1, 4);
        specsCard.getChildren().add(specsGrid);

        leftColumn.getChildren().addAll(titleBox, imgBox, descCard, specsCard);

        // Right Column (Sticky Action Card)
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(330);

        VBox actionCard = new VBox(16);
        actionCard.setPadding(new Insets(22));
        actionCard.setStyle(Theme.elevatedCardStyle());

        // Rating
        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node star = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 16);
        Text ratingTxt = new Text("4.9  (Verified Item)");
        ratingTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-weight: 700; -fx-font-size: 12px;");
        ratingRow.getChildren().addAll(star, ratingTxt);

        Text priceText = new Text(p.getPrice());
        priceText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 28px; -fx-font-weight: 800;");

        String sName = (p.getSellerName() != null && !p.getSellerName().trim().isEmpty()) ? p.getSellerName().trim() : "Student Seller";
        String sPhone = (p.getSellerPhone() != null && !p.getSellerPhone().trim().isEmpty()) ? p.getSellerPhone().trim() : "Available on Request";

        Text sellerText = new Text("👤 Seller: " + sName);
        sellerText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: 700;");

        Text phoneText = new Text("📞 Mobile: " + sPhone);
        phoneText.setStyle(Theme.mutedTextStyle());

        if (isMyItem) {
            Button manageBtn = new Button("🛍️ Manage in Seller Portal");
            manageBtn.setMaxWidth(Double.MAX_VALUE);
            manageBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-weight: 800;");
            manageBtn.setOnAction(e -> Main.showSellerDashboard());

            actionCard.getChildren().addAll(ratingRow, priceText, sellerText, phoneText, manageBtn);
        } else {
            Button reqBtn = new Button("⚡ Send Purchase Request");
            reqBtn.setMaxWidth(Double.MAX_VALUE);
            reqBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-weight: 800;");

            reqBtn.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog("Hi " + sName + ", I would like to buy '" + p.getTitle() + "'!");
                dialog.setTitle("Send Purchase Request");
                dialog.setHeaderText("Send request to " + sName + " for " + p.getTitle());
                dialog.setContentText("Message to Seller:");

                dialog.showAndWait().ifPresent(msg -> {
                    String bUid = (currentUser != null && currentUser.getUid() != null) ? currentUser.getUid() : "stud_" + System.currentTimeMillis();
                    String bName = (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) ? currentUser.getName() : "Student Buyer";
                    String bEmail = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";
                    String bPhone = (currentUser != null && currentUser.getPhone() != null) ? currentUser.getPhone() : "";

                    Order newReq = new Order(
                        "order_" + System.currentTimeMillis(),
                        bUid,
                        bName,
                        bEmail,
                        bPhone,
                        p.getSellerUid(),
                        sName,
                        p.getId(),
                        p.getTitle(),
                        p.getPrice(),
                        "TRK-" + System.currentTimeMillis(),
                        "PENDING",
                        p.getCategory(),
                        msg.trim(),
                        "Today",
                        System.currentTimeMillis(),
                        System.currentTimeMillis()
                    );

                    // Save to Firestore
                    new Thread(() -> new OrderDAOImpl().save(newReq)).start();
                    // Save to DataRepository
                    DataRepository.getInstance().addOrUpdateOrder(newReq);

                    showAlert("Request Sent!", "Your purchase request for '" + p.getTitle() + "' has been sent to " + sName + "!\nThe seller will receive it in their Seller Portal.");
                    reqBtn.setText("✓ Request Sent");
                    reqBtn.setDisable(true);
                });
            });

            Button chatBtn = new Button("💬 Contact Seller");
            chatBtn.setMaxWidth(Double.MAX_VALUE);
            chatBtn.setStyle(Theme.secondaryBtnStyle());
            chatBtn.setOnAction(e -> {
                String sellerUid = (p.getSellerUid() != null && !p.getSellerUid().trim().isEmpty())
                    ? p.getSellerUid().trim()
                    : ("seller_" + Math.abs(sName.hashCode()));
                Main.showChatWithUser(sellerUid, sName, "SELLER", p.getId(), "PRODUCT", p.getTitle());
            });

            Button callBtn = new Button("📞 Call Seller");
            callBtn.setMaxWidth(Double.MAX_VALUE);
            callBtn.setStyle(Theme.outlineBtnStyle());
            callBtn.setOnAction(e -> showAlert("Calling", "Dialing " + sPhone));

            actionCard.getChildren().addAll(ratingRow, priceText, sellerText, phoneText, reqBtn, chatBtn, callBtn);
        }

        rightColumn.getChildren().add(actionCard);
        columnsBox.getChildren().addAll(leftColumn, rightColumn);

        // Similar Products Section at Bottom
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Similar Products in Marketplace");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        FlowPane simCardsBox = new FlowPane(16, 16);
        List<ProductItem> allProds = DataRepository.getInstance().getProducts();
        int added = 0;
        for (ProductItem prod : allProds) {
            if (!prod.getId().equals(p.getId()) && prod.isAvailable()) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    prod.getId(), ListingCardNode.CardType.PRODUCT, "AVAILABLE",
                    prod.getTitle(), prod.getLocation(), prod.getPrice(), prod.getTimePosted(),
                    prod.getImagePath(), prod.getCategory(), () -> Main.showProductDetailsPage(prod)
                ));
                added++;
                if (added >= 4) break;
            }
        }

        if (added > 0) {
            similarSection.getChildren().addAll(simTitle, simCardsBox);
            mainContent.getChildren().addAll(backBtn, columnsBox, similarSection);
        } else {
            mainContent.getChildren().addAll(backBtn, columnsBox);
        }

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
