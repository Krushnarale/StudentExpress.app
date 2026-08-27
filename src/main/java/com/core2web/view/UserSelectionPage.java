package com.core2web.view;

import com.core2web.model.User;
import com.core2web.model.User.Role;
import com.core2web.util.Theme;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class UserSelectionPage {

    private Scene selectionScene;

    public javafx.scene.Node getPageNode(Consumer<User.Role> onSelectRole) {
        BorderPane rootPane = new BorderPane();
        // Theme green background
        rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E4A18, #4F772D, #6A9E45);");

        // ─── Header ─────────────────────────────────────────────
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(44, 20, 20, 20));

        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER);

        StackPane logoBadge = new StackPane();
        Circle badgeBg = new Circle(26, Color.web("#FFFFFF25"));
        Text badgeIcon = new Text("🎓");
        badgeIcon.setStyle("-fx-font-size: 26px;");
        logoBadge.getChildren().addAll(badgeBg, badgeIcon);

        Text logoTxt = new Text("StudentExpress");
        logoTxt.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 32px; -fx-font-weight: 800;");
        logoBox.getChildren().addAll(logoBadge, logoTxt);

        Text headerTitle = new Text("Choose Your Portal");
        headerTitle.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");

        Text headerSub = new Text("Click on a user logo to enter your dedicated login page.");
        headerSub.setStyle("-fx-fill: rgba(255, 255, 255, 0.85); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px;");

        header.getChildren().addAll(logoBox, headerTitle, headerSub);
        rootPane.setTop(header);

        // ─── Single Horizontal Line of 4 Role Logos ───────────────
        HBox roleRow = new HBox(24);
        roleRow.setAlignment(Pos.CENTER);
        roleRow.setPadding(new Insets(30, 24, 30, 24));

        VBox studentItem = createLogoPortalItem(
            "🎓", "Student / Buyer",
            "Rent rooms, buy books, electronics & find roommates.",
            () -> { if (onSelectRole != null) onSelectRole.accept(User.Role.STUDENT); }
        );

        VBox ownerItem = createLogoPortalItem(
            "🏢", "Property Owner",
            "List PG rooms, flats & manage tenant applications.",
            () -> { if (onSelectRole != null) onSelectRole.accept(User.Role.OWNER); }
        );

        VBox sellerItem = createLogoPortalItem(
            "🛍️", "Student Seller",
            "Sell old books, gadgets, cycles & furniture to peers.",
            () -> { if (onSelectRole != null) onSelectRole.accept(User.Role.SELLER); }
        );

        VBox providerItem = createLogoPortalItem(
            "🛠️", "Service Provider",
            "Offer laundry, tiffin mess, cleaning & tech services.",
            () -> { if (onSelectRole != null) onSelectRole.accept(User.Role.SERVICE_PROVIDER); }
        );

        roleRow.getChildren().addAll(studentItem, ownerItem, sellerItem, providerItem);

        ScrollPane scrollWrapper = new ScrollPane(roleRow);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setMinHeight(0);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        rootPane.setCenter(scrollWrapper);

        // ─── Footer ─────────────────────────────────────────────
        HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(16, 20, 36, 20));

        Text adminText = new Text("System Administrator?");
        adminText.setStyle("-fx-fill: rgba(255, 255, 255, 0.75); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px;");

        Hyperlink adminLink = new Hyperlink("Access Admin Portal →");
        adminLink.setStyle("-fx-text-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-font-weight: 700; -fx-underline: false;");
        adminLink.setOnAction(e -> { if (onSelectRole != null) onSelectRole.accept(User.Role.ADMIN); });
        footer.getChildren().addAll(adminText, adminLink);
        rootPane.setBottom(footer);

        return rootPane;
    }

    public Scene getPageScene(Consumer<User.Role> onSelectRole) {
        javafx.scene.Node node = getPageNode(onSelectRole);
        selectionScene = new Scene(new BorderPane(node), 1050, 700);
        return selectionScene;
    }

    private VBox createLogoPortalItem(
        String icon, String title, String shortDesc, Runnable onClick
    ) {
        VBox item = new VBox(14);
        item.setPrefWidth(220);
        item.setMinWidth(210);
        item.setAlignment(Pos.CENTER);
        item.setPadding(new Insets(24, 18, 24, 18));

        String defaultStyle =
            "-fx-background-color: transparent;"
            + "-fx-cursor: hand;";

        String hoverStyle =
            "-fx-background-color: transparent;"
            + "-fx-translate-y: -4px;"
            + "-fx-cursor: hand;";

        item.setStyle(defaultStyle);

        // Large Logo Badge
        StackPane logoCircle = new StackPane();
        logoCircle.setPrefSize(96, 96);
        logoCircle.setMinSize(96, 96);
        logoCircle.setMaxSize(96, 96);

        String logoDefault =
            "-fx-background-color: rgba(255, 255, 255, 0.22);"
            + "-fx-background-radius: 50%;"
            + "-fx-border-color: rgba(255, 255, 255, 0.40);"
            + "-fx-border-width: 2px;"
            + "-fx-border-radius: 50%;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4);";

        String logoHover =
            "-fx-background-color: rgba(255, 255, 255, 0.38);"
            + "-fx-background-radius: 50%;"
            + "-fx-border-color: #FFFFFF;"
            + "-fx-border-width: 3px;"
            + "-fx-border-radius: 50%;"
            + "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.4), 22, 0, 0, 6);";

        logoCircle.setStyle(logoDefault);

        item.setOnMouseEntered(e -> {
            item.setStyle(hoverStyle);
            logoCircle.setStyle(logoHover);
        });
        item.setOnMouseExited(e -> {
            item.setStyle(defaultStyle);
            logoCircle.setStyle(logoDefault);
        });
        item.setOnMouseClicked(e -> { if (onClick != null) onClick.run(); });

        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 44px;");
        logoCircle.getChildren().add(iconTxt);

        // Title text (White)
        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 17px; -fx-font-weight: 800; -fx-text-alignment: center;");

        // Small description (Soft White)
        Text descTxt = new Text(shortDesc);
        descTxt.setStyle("-fx-fill: rgba(255, 255, 255, 0.85); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px; -fx-line-spacing: 2px; -fx-text-alignment: center;");
        descTxt.setWrappingWidth(184);

        item.getChildren().addAll(logoCircle, titleTxt, descTxt);
        return item;
    }
}
