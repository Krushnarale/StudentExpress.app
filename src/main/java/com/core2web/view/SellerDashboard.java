package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.OrderController;
import com.core2web.controller.ProductController;
import com.core2web.controller.SellerController;
import com.core2web.model.ProductItem;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.io.File;
import java.util.ArrayList;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class SellerDashboard {

    private Scene sellerScene;
    private String activeCategory = "All Items";
    private final SellerController sellerController = new SellerController();
    private final ProductController productController = new ProductController();
    private final OrderController orderController = new OrderController();

    public Node getPageNode(Runnable onPostItem, Runnable onLogout) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));

        // Top Bar Navigation & Actions
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backToAppBtn = new Button("← Back to StudentExpress App");
        backToAppBtn.setStyle(Theme.outlineBtnStyle());
        backToAppBtn.setOnAction(e -> Main.showHomePage());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        topBar.getChildren().addAll(backToAppBtn, topSpacer, logoutBtn);

        // Heading + Primary Action
        HBox headingBox = new HBox(16);
        headingBox.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Text titleTxt = new Text("Student Seller Marketplace Workspace");
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");
        Text subTxt = new Text("List books, electronics, hostel furniture, cycles & gadgets for sale to campus peers");
        subTxt.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleTxt, subTxt);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button postItemBtn = new Button("Post New Product for Sale");
        postItemBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_PLUS, "white", 14));
        postItemBtn.setStyle(Theme.primaryBtnStyle());
        headingBox.getChildren().addAll(titleBox, postItemBtn);

        // Quick Stats Row (4 Cards)
        HBox statsBox = new HBox(16);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_PACKAGE, String.valueOf(sellerController.getSellerProducts().size()), "Active Listings", Theme.PRIMARY),
            createStatCard(IconFactory.PATH_CHECK, "18", "Items Sold", "#2563EB"),
            createStatCard(IconFactory.PATH_MESSAGE, "24", "Buyer Chats", "#D97706"),
            createStatCard(IconFactory.PATH_MONEY, "₹ 14,200", "Total Earnings", "#10B981")
        );

        // Section Title + Category Filters
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("My Items Listed for Sale");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        sectionHeader.getChildren().add(secTitle);

        HBox categoryPills = new HBox(10);
        String[] sellerCats = {"All Items", "📚 Books", "💻 Electronics", "🪑 Furniture", "🏋️ Gym & Fitness", "🚴 Cycles", "👕 Fashion"};

        VBox itemsContainer = new VBox(16);

        final Runnable[] refreshItems = new Runnable[1];
        refreshItems[0] = () -> {
            itemsContainer.getChildren().clear();
            List<ProductItem> filtered = getFilteredProducts(activeCategory);

            if (filtered.isEmpty()) {
                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                empty.setStyle(Theme.cardStyle());
                Text icon = new Text("📦");
                icon.setStyle("-fx-font-size: 40px;");
                Text emptyTxt = new Text("No products listed in this category yet.");
                emptyTxt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().addAll(icon, emptyTxt);
                itemsContainer.getChildren().add(empty);
            } else {
                for (ProductItem prod : filtered) {
                    HBox card = createItemCard(prod, () -> {
                        productController.removeProduct(prod.getId());
                        if (refreshItems[0] != null) refreshItems[0].run();
                    });
                    itemsContainer.getChildren().add(card);
                }
            }

            // Update category pills styles
            for (javafx.scene.Node n : categoryPills.getChildren()) {
                if (n instanceof Button) {
                    Button b = (Button) n;
                    b.setStyle(Theme.filterPillStyle(b.getText().contains(activeCategory) || (activeCategory.equals("All Items") && b.getText().equals("All Items"))));
                }
            }
        };

        for (String cat : sellerCats) {
            Button pill = new Button(cat);
            pill.setStyle(Theme.filterPillStyle(cat.equals("All Items")));
            pill.setOnAction(e -> {
                if (cat.contains("Books")) activeCategory = "Books";
                else if (cat.contains("Electronics")) activeCategory = "Electronics";
                else if (cat.contains("Furniture")) activeCategory = "Furniture";
                else if (cat.contains("Gym")) activeCategory = "Gym & Fitness";
                else if (cat.contains("Cycles")) activeCategory = "Cycles";
                else if (cat.contains("Fashion")) activeCategory = "Fashion";
                else activeCategory = "All Items";
                refreshItems[0].run();
            });
            categoryPills.getChildren().add(pill);
        }

        postItemBtn.setOnAction(e -> showAddProductDialog(refreshItems[0]));
        refreshItems[0].run();

        VBox sellerOrdersSection = createSellerOrdersSection();

        mainContent.getChildren().addAll(topBar, headingBox, statsBox, sellerOrdersSection, sectionHeader, categoryPills, itemsContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onPostItem, Runnable onLogout) {
        Node node = getPageNode(onPostItem, onLogout);
        BorderPane rootPane = new BorderPane(node);
        rootPane.setStyle(Theme.rootPaneStyle());

        // ─── Top Bar ────────────────────────────────────────────
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 30, 16, 30));
        topBar.setStyle(Theme.topBarStyle());

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefSize(36, 36);
        roleBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text roleIcon = new Text("🛍️");
        roleIcon.setStyle("-fx-font-size: 16px;");
        roleBadge.getChildren().add(roleIcon);
        Text logoTxt = new Text("StudentExpress  •  Student Seller Workspace");
        logoTxt.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");
        logoRow.getChildren().addAll(roleBadge, logoTxt);
        HBox.setHgrow(logoRow, Priority.ALWAYS);

        Button backAppBtn = new Button("← Back to App");
        backAppBtn.setStyle(Theme.outlineBtnStyle());
        backAppBtn.setOnAction(e -> Main.showHomePage());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Seller Logout");
            alert.setHeaderText("Logout from Seller Workspace?");
            alert.setContentText("You will return to the login screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });

        topBar.getChildren().addAll(logoRow, backAppBtn, logoutBtn);
        rootPane.setTop(topBar);


        sellerScene = new Scene(rootPane, 1050, 700);
        return sellerScene;
    }

    private List<ProductItem> getFilteredProducts(String catFilter) {
        List<ProductItem> result = new ArrayList<>();
        for (ProductItem p : sellerController.getSellerProducts()) {
            if (catFilter.equals("All Items")) {
                result.add(p);
            } else if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(catFilter)) {
                result.add(p);
            }
        }
        return result;
    }

    private String countByCategory(String cat) {
        return String.valueOf(getFilteredProducts(cat).size());
    }

    private HBox createItemCard(ProductItem p, Runnable onDelete) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(Theme.cardStyle());

        // Category Color Bar
        String catColor = "#2563EB"; // Default Blue
        String catLabelText = p.getCategory() != null ? p.getCategory() : "General";

        if ("Books".equalsIgnoreCase(catLabelText)) {
            catColor = "#EA580C";
        } else if ("Electronics".equalsIgnoreCase(catLabelText)) {
            catColor = "#2563EB";
        } else if ("Furniture".equalsIgnoreCase(catLabelText)) {
            catColor = "#D97706";
        } else if ("Gym & Fitness".equalsIgnoreCase(catLabelText)) {
            catColor = "#7C3AED";
        } else if ("Cycles".equalsIgnoreCase(catLabelText)) {
            catColor = "#4F772D";
        } else if ("Fashion".equalsIgnoreCase(catLabelText)) {
            catColor = "#0D9488";
        }

        Rectangle colorBar = new Rectangle(5, 42);
        colorBar.setArcWidth(4); colorBar.setArcHeight(4);
        colorBar.setStyle("-fx-fill: " + catColor + ";");

        // Thumbnail Image
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(75, 55);
        imgBox.setMinSize(75, 55);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");

        Image img = com.core2web.util.ImageUtil.loadImage(p.getImagePath());
        if (img != null) {
            try {
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(75);
                imgView.setFitHeight(55);
                imgView.setPreserveRatio(false);
                Rectangle clip = new Rectangle(75, 55);
                clip.setArcWidth(8); clip.setArcHeight(8);
                imgView.setClip(clip);
                imgBox.getChildren().add(imgView);
            } catch (Exception e) {}
        }

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text titleText = new Text(p.getTitle() + "   •   " + p.getPrice());
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(catLabelText);
        catBadge.setStyle(
            "-fx-background-color: " + catColor + "1E;"
            + "-fx-text-fill: " + catColor + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );

        Label condBadge = new Label(p.getCondition() != null ? p.getCondition() : "Good Condition");
        condBadge.setStyle(Theme.badgeStyle());

        Text locText = new Text("📍 " + p.getLocation() + "   •   Posted " + p.getTimePosted());
        locText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, condBadge, locText);
        info.getChildren().addAll(titleText, metaRow);

        Label statusLbl = new Label("Active Listing");
        statusLbl.setStyle(Theme.successBadgeStyle());

        Button editBtn = new Button("Edit");
        editBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_EDIT, Theme.PRIMARY, 14));
        editBtn.setStyle(Theme.secondaryBtnStyle());
        editBtn.setOnAction(ev -> showAlert("Edit Item", "Edit details for: " + p.getTitle()));

        Button delBtn = new Button("Delete");
        delBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_TRASH, "#C62828", 14));
        delBtn.setStyle(Theme.dangerBtnStyle());
        delBtn.setOnAction(ev -> onDelete.run());

        card.getChildren().addAll(colorBar, imgBox, info, statusLbl, editBtn, delBtn);
        return card;
    }

    private VBox createStatCard(String iconPath, String value, String title, String accentColor) {
        return createStatCard(iconPath, title, value, accentColor, "");
    }

    private VBox createStatCard(String iconPath, String title, String value, String accentColor, String subtext) {
        VBox b = new VBox(8);
        b.setPrefWidth(210);
        b.setPadding(new Insets(18));
        b.setStyle(Theme.statCardStyle(accentColor));

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(38, 38);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Node iconNode = IconFactory.getIconNode(iconPath, accentColor, 18);
        iconBadge.getChildren().add(iconNode);
        topRow.getChildren().add(iconBadge);

        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 28px; -fx-font-weight: 800;");

        Text lblTxt = new Text(title);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 600;");

        b.getChildren().addAll(topRow, valTxt, lblTxt);
        return b;
    }

    private void showAddProductDialog(Runnable onAdded) {
        Dialog<ProductItem> dialog = new Dialog<>();
        dialog.setTitle("Post New Item to Student Marketplace");
        dialog.setHeaderText("Choose category and enter item listing details:");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(420);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Books", "Electronics", "Furniture", "Gym & Fitness", "Cycles", "Fashion");
        catBox.setValue("Books");
        catBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> condBox = new ComboBox<>();
        condBox.getItems().addAll("Like New", "Good Condition", "Used");
        condBox.setValue("Like New");
        condBox.setMaxWidth(Double.MAX_VALUE);

        TextField titleField = new TextField();
        titleField.setPromptText("Title (e.g. Data Structures Book / Dumbbells Set)");

        TextField locField = new TextField();
        locField.setPromptText("Location (e.g. Kothrud, Pune)");
        locField.setText("Kothrud, Pune");

        TextField priceField = new TextField();
        priceField.setPromptText("Price (e.g. ₹ 450)");

        TextField descField = new TextField();
        descField.setPromptText("Item Description");

        content.getChildren().addAll(
            new Label("Item Category:"), catBox,
            new Label("Item Condition:"), condBox,
            new Label("Item Title:"), titleField,
            new Label("Location:"), locField,
            new Label("Price:"), priceField,
            new Label("Description:"), descField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !titleField.getText().trim().isEmpty()) {
                String imgPath = "";
                String selectedCat = catBox.getValue();

                return new ProductItem(
                    "p" + System.currentTimeMillis(),
                    titleField.getText().trim(),
                    priceField.getText().trim().isEmpty() ? "₹ 299" : priceField.getText().trim(),
                    locField.getText().trim().isEmpty() ? "Pune" : locField.getText().trim(),
                    "Just now",
                    selectedCat,
                    condBox.getValue(),
                    descField.getText().trim().isEmpty() ? "Listed by Student Seller." : descField.getText().trim(),
                    "Darshan",
                    "+91 94050 53651",
                    imgPath
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newItem -> {
            productController.addProduct(newItem);
            showAlert("Success", "'" + newItem.getTitle() + "' posted to " + newItem.getCategory() + " marketplace!");
            onAdded.run();
        });
    }

    private VBox createSellerOrdersSection() {
        VBox section = new VBox(14);
        Text title = new Text("Orders Received from Student Buyers");
        title.setStyle(Theme.sectionHeaderStyle());

        VBox ordersList = new VBox(12);

        Runnable refresh = () -> {
            ordersList.getChildren().clear();
            List<com.core2web.model.Order> list = orderController.getSellerOrders();

            if (list.isEmpty()) {
                VBox empty = new VBox(8);
                empty.setPadding(new Insets(18));
                empty.setStyle(Theme.cardStyle());
                Text txt = new Text("No orders received yet.");
                txt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().add(txt);
                ordersList.getChildren().add(empty);
            } else {
                for (com.core2web.model.Order o : list) {
                    HBox card = new HBox(16);
                    card.setPadding(new Insets(14, 18, 14, 18));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(Theme.cardStyle());

                    VBox info = new VBox(4);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Text oTitle = new Text(o.getItemName() + "   •   " + o.getPrice());
                    oTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: 700; -fx-font-size: 14px;");
                    Text oSub = new Text("Order ID: " + o.getId() + "   |   Tracking ID: " + o.getTrackingId() + "   |   Date: " + o.getDate());
                    oSub.setStyle(Theme.mutedTextStyle());
                    info.getChildren().addAll(oTitle, oSub);

                    ComboBox<String> statusCombo = new ComboBox<>();
                    statusCombo.getItems().addAll("PLACED", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
                    statusCombo.setValue(o.getStatus() != null ? o.getStatus() : "PLACED");
                    statusCombo.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 6px;");

                    Button updateBtn = new Button("Update Status");
                    updateBtn.setStyle(Theme.primaryBtnStyle());
                    updateBtn.setOnAction(ev -> {
                        String newStatus = statusCombo.getValue();
                        orderController.updateOrderStatus(o.getId(), newStatus);
                        showAlert("Status Updated", "Order " + o.getId() + " status updated to " + newStatus);
                    });

                    card.getChildren().addAll(info, statusCombo, updateBtn);
                    ordersList.getChildren().add(card);
                }
            }
        };

        refresh.run();
        section.getChildren().addAll(title, ordersList);
        return section;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
