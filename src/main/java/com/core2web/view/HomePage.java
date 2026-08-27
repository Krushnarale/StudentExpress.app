package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.ProductController;
import com.core2web.controller.RoomController;
import com.core2web.model.ProductItem;
import com.core2web.model.RoomItem;
import com.core2web.util.IconFactory;
import com.core2web.util.SessionManager;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class HomePage {

    private Scene homeScene;

    public Node getPageNode(
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices,
        Runnable onNavigatePostItem,
        Runnable onNavigateProfile
    ) {
        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(24, 32, 28, 32));

        // Hero Banner Card
        HBox heroBanner = new HBox(0);
        heroBanner.setPadding(new Insets(28, 32, 28, 32));
        heroBanner.setAlignment(Pos.CENTER_LEFT);
        heroBanner.setStyle(
            "-fx-background-color: linear-gradient(to right, #2E4A18, #4F772D);"
            + "-fx-background-radius: 18px;"
            + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.25), 20, 0, 0, 6);"
        );

        VBox heroText = new VBox(10);
        HBox.setHgrow(heroText, Priority.ALWAYS);

        Label heroBadge = new Label("✨ Verified Campus Listings");
        heroBadge.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);"
            + "-fx-text-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 4px 12px;"
            + "-fx-background-radius: 20px;"
        );

        String userName = SessionManager.getInstance().getName();
        if (userName == null || userName.isEmpty()) userName = "Student";
        Text heroTitle = new Text("Hello, " + userName + "! 👋\nFind your perfect student space.");
        heroTitle.setStyle(
            "-fx-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 26px;"
            + "-fx-font-weight: 800;"
        );

        Text heroSub = new Text("Affordable rooms, student deals, furniture rentals, and campus services — all in one place.");
        heroSub.setStyle("-fx-fill: rgba(255,255,255,0.85); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");

        HBox heroBtns = new HBox(12);
        heroBtns.setAlignment(Pos.CENTER_LEFT);

        Button exploreRoomsBtn = new Button("Explore Rooms & Rentals  →");
        exploreRoomsBtn.setStyle(
            "-fx-background-color: white;"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 800;"
            + "-fx-font-size: 13px;"
            + "-fx-padding: 10px 22px;"
            + "-fx-background-radius: 10px;"
            + "-fx-cursor: hand;"
        );
        exploreRoomsBtn.setOnAction(e -> { if (onNavigateRent != null) onNavigateRent.run(); });

        heroBtns.getChildren().add(exploreRoomsBtn);
        heroText.getChildren().addAll(heroBadge, heroTitle, heroSub, heroBtns);
        heroBanner.getChildren().add(heroText);

        // Compact Statistics Section
        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            createStatCard("Available Rooms", "150+", IconFactory.PATH_KEY, "#4F772D"),
            createStatCard("Student Listings", "320+", IconFactory.PATH_SHOPPING_BAG, "#2563EB"),
            createStatCard("Campus Services", "45+", IconFactory.PATH_WRENCH, "#D97706"),
            createStatCard("Active Students", "1,200+", IconFactory.PATH_USERS, "#059669")
        );

        // Category Cards Single Horizontal Row
        HBox categoryBox = new HBox(14);
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        categoryBox.getChildren().addAll(
            createCategoryCard(IconFactory.PATH_KEY, "Rent", "Rooms & Furniture", onNavigateRent),
            createCategoryCard(IconFactory.PATH_SHOPPING_BAG, "Buy & Sell", "Books & Gadgets", onNavigateBuySell),
            createCategoryCard(IconFactory.PATH_USERS, "Roommates", "Find Flatmates", onNavigateRoommates),
            createCategoryCard(IconFactory.PATH_WRENCH, "Services", "Laundry & Mess", onNavigateServices),
            createCategoryCard(IconFactory.PATH_PLUS, "Post Item", "List Something", onNavigatePostItem)
        );

        // Recommended For You Section
        VBox recommendedSection = new VBox(14);
        HBox recHeader = new HBox();
        recHeader.setAlignment(Pos.CENTER_LEFT);

        Text recTitle = new Text("Recommended for You");
        recTitle.setStyle(Theme.sectionHeaderStyle());
        HBox.setHgrow(recTitle, Priority.ALWAYS);

        Button viewAllRec = new Button("View All →");
        viewAllRec.setStyle(Theme.outlineBtnStyle());
        viewAllRec.setOnAction(e -> { if (onNavigateRent != null) onNavigateRent.run(); });
        recHeader.getChildren().addAll(recTitle, viewAllRec);

        FlowPane recCardsBox = new FlowPane(16, 16);

        // Populate dynamic recommended listing cards using Controllers by ID
        RoomController roomCtrl = new RoomController();
        ProductController prodCtrl = new ProductController();
        List<RoomItem> rooms = roomCtrl.getAllRooms();
        List<ProductItem> products = prodCtrl.getAllProducts();


        // 1. Student Room (r1)
        RoomItem r1 = rooms.stream().filter(r -> "r1".equals(r.getId())).findFirst().orElse(null);
        if (r1 != null) {
            recCardsBox.getChildren().add(new ListingCardNode(
                r1.getId(), ListingCardNode.CardType.ROOM, "FEATURED",
                r1.getTitle(), r1.getLocation(), r1.getPrice(), r1.getDistance(),
                r1.getImagePath(), "Room", () -> Main.showRoomDetailsPage(r1)
            ));
        }

        // 2. Bike (r18 - Yamaha R15 V4 Bike)
        RoomItem r18 = rooms.stream().filter(r -> "r18".equals(r.getId())).findFirst().orElse(null);
        if (r18 != null) {
            recCardsBox.getChildren().add(new ListingCardNode(
                r18.getId(), ListingCardNode.CardType.ROOM, "POPULAR",
                r18.getTitle(), r18.getLocation(), r18.getPrice(), r18.getDistance(),
                r18.getImagePath(), "Vehicle", () -> Main.showRoomDetailsPage(r18)
            ));
        }

        // 3. Books (p1 - Engineering Math Book)
        ProductItem p1 = products.stream().filter(p -> "p1".equals(p.getId())).findFirst().orElse(null);
        if (p1 != null) {
            recCardsBox.getChildren().add(new ListingCardNode(
                p1.getId(), ListingCardNode.CardType.PRODUCT, "VERIFIED",
                p1.getTitle(), p1.getLocation(), p1.getPrice(), p1.getTimePosted(),
                p1.getImagePath(), "Book", () -> Main.showProductDetailsPage(p1)
            ));
        }

        // 4. Electronics (r10 - MacBook Air M1 Laptop)
        RoomItem r10 = rooms.stream().filter(r -> "r10".equals(r.getId())).findFirst().orElse(null);
        if (r10 != null) {
            recCardsBox.getChildren().add(new ListingCardNode(
                r10.getId(), ListingCardNode.CardType.ROOM, "HOT DEAL",
                r10.getTitle(), r10.getLocation(), r10.getPrice(), r10.getDistance(),
                r10.getImagePath(), "Electronics", () -> Main.showRoomDetailsPage(r10)
            ));
        }

        recommendedSection.getChildren().addAll(recHeader, recCardsBox);

        // Recently Added Section
        VBox recentlyAddedSection = new VBox(14);
        HBox recentHeader = new HBox();
        recentHeader.setAlignment(Pos.CENTER_LEFT);

        Text recentTitle = new Text("Recently Added");
        recentTitle.setStyle(Theme.sectionHeaderStyle());
        HBox.setHgrow(recentTitle, Priority.ALWAYS);

        Button viewAllRecent = new Button("View All →");
        viewAllRecent.setStyle(Theme.outlineBtnStyle());
        viewAllRecent.setOnAction(e -> { if (onNavigateBuySell != null) onNavigateBuySell.run(); });
        recentHeader.getChildren().addAll(recentTitle, viewAllRecent);

        FlowPane recentCardsBox = new FlowPane(16, 16);

        // 1. Furniture (r6 - Rent Study Table & Chair Set)
        RoomItem r6 = rooms.stream().filter(r -> "r6".equals(r.getId())).findFirst().orElse(null);
        if (r6 != null) {
            recentCardsBox.getChildren().add(new ListingCardNode(
                r6.getId(), ListingCardNode.CardType.ROOM, "NEW",
                r6.getTitle(), r6.getLocation(), r6.getPrice(), r6.getDistance(),
                r6.getImagePath(), "Furniture", () -> Main.showRoomDetailsPage(r6)
            ));
        }

        // 2. Appliances (r13 - Rent Mini Refrigerator 50L)
        RoomItem r13 = rooms.stream().filter(r -> "r13".equals(r.getId())).findFirst().orElse(null);
        if (r13 != null) {
            recentCardsBox.getChildren().add(new ListingCardNode(
                r13.getId(), ListingCardNode.CardType.ROOM, "NEW",
                r13.getTitle(), r13.getLocation(), r13.getPrice(), r13.getDistance(),
                r13.getImagePath(), "Appliance", () -> Main.showRoomDetailsPage(r13)
            ));
        }

        // 3. Fitness (r20 - Adjustable Dumbbells 20kg)
        RoomItem r20 = rooms.stream().filter(r -> "r20".equals(r.getId())).findFirst().orElse(null);
        if (r20 != null) {
            recentCardsBox.getChildren().add(new ListingCardNode(
                r20.getId(), ListingCardNode.CardType.ROOM, "NEW",
                r20.getTitle(), r20.getLocation(), r20.getPrice(), r20.getDistance(),
                r20.getImagePath(), "Fitness", () -> Main.showRoomDetailsPage(r20)
            ));
        }

        // 4. PG / Room (r3 - 2 Sharing Room)
        RoomItem r3 = rooms.stream().filter(r -> "r3".equals(r.getId())).findFirst().orElse(null);
        if (r3 != null) {
            recentCardsBox.getChildren().add(new ListingCardNode(
                r3.getId(), ListingCardNode.CardType.ROOM, "NEW",
                r3.getTitle(), r3.getLocation(), r3.getPrice(), r3.getDistance(),
                r3.getImagePath(), "Room", () -> Main.showRoomDetailsPage(r3)
            ));
        }

        recentlyAddedSection.getChildren().addAll(recentHeader, recentCardsBox);

        mainContent.getChildren().addAll(heroBanner, statsRow, categoryBox, recommendedSection, recentlyAddedSection);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private VBox createStatCard(String label, String value, String iconPath, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setStyle(Theme.statCardStyle(accentColor));

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Node icon = IconFactory.getIconNode(iconPath, accentColor, 24);
        Text valText = new Text(value);
        valText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 25px; -fx-font-weight: 800;");
        topRow.getChildren().addAll(icon, valText);

        Text lblText = new Text(label);
        lblText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 600;");

        card.getChildren().addAll(topRow, lblText);
        return card;
    }

    private VBox createCategoryCard(String iconPath, String title, String subtitle, Runnable action) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setMinWidth(150);
        card.setPrefWidth(190);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(135);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setStyle(Theme.cardStyle() + " -fx-cursor: hand;");

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(44, 44);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");
        Node iconNode = IconFactory.getIconNode(iconPath, Theme.PRIMARY, 22);
        iconBadge.getChildren().add(iconNode);

        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 15px;");
        Text subTxt = new Text(subtitle);
        subTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px;");

        card.getChildren().addAll(iconBadge, titleTxt, subTxt);

        String defaultStyle = Theme.cardStyle() + " -fx-cursor: hand;";
        String hoverStyle = Theme.elevatedCardStyle() + " -fx-cursor: hand; -fx-translate-y: -3px;";
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));
        card.setOnMouseClicked(e -> { if (action != null) action.run(); });
        return card;
    }

    public Scene getPageScene(
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices,
        Runnable onNavigatePostItem,
        Runnable onNavigateProfile
    ) {
        Node node = getPageNode(onNavigateRent, onNavigateBuySell, onNavigateRoommates, onNavigateServices, onNavigatePostItem, onNavigateProfile);
        homeScene = new Scene(new BorderPane(node), 1050, 700);
        return homeScene;
    }
}
