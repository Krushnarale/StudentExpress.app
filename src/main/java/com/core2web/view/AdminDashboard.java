package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.AdminController;
import com.core2web.controller.BookingController;
import com.core2web.controller.ProductController;
import com.core2web.controller.RoomController;
import com.core2web.controller.ServiceController;
import com.core2web.model.Booking;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class AdminDashboard {

    private Scene adminScene;
    private final AdminController adminController = new AdminController();
    private final RoomController roomController = new RoomController();
    private final ProductController productController = new ProductController();
    private final ServiceController serviceController = new ServiceController();
    private final BookingController bookingController = new BookingController();

    public Node getPageNode(Runnable onLogout) {
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

        // Heading
        VBox headingBox = new VBox(4);
        Text heading = new Text("System Administration Console");
        heading.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");
        Text sub = new Text("Platform metrics, user activity, bookings moderation & system overview");
        sub.setStyle(Theme.mutedTextStyle());
        headingBox.getChildren().addAll(heading, sub);

        // Stats Row
        HBox statsBox = new HBox(16);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_USERS, "Total Users", String.valueOf(adminController.getTotalUsersCount()), Theme.PRIMARY, "+12% this week"),
            createStatCard(IconFactory.PATH_KEY, "Active Rooms", String.valueOf(roomController.getAllRooms().size()), "#2563EB", "+5 new today"),
            createStatCard(IconFactory.PATH_SHOPPING_BAG, "Marketplace Products", String.valueOf(productController.getAllProducts().size()), "#D97706", "38 sold this month"),
            createStatCard(IconFactory.PATH_WRENCH, "Services Listed", String.valueOf(serviceController.getAllServices().size()), "#7C3AED", "98% satisfaction")
        );

        // System Activity / Bookings Moderation Card
        VBox bookingsCard = new VBox(14);
        bookingsCard.setPadding(new Insets(20));
        bookingsCard.setStyle(Theme.cardStyle());

        HBox bookHeaderRow = new HBox();
        bookHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Text bookTitle = new Text("Platform Booking Activity Log");
        bookTitle.setStyle(Theme.sectionHeaderStyle());
        HBox.setHgrow(bookTitle, Priority.ALWAYS);
        Label totalBadge = new Label(bookingController.getAllBookings().size() + " total events");
        totalBadge.setStyle(Theme.badgeStyle());
        bookHeaderRow.getChildren().addAll(bookTitle, totalBadge);

        VBox bookingsList = new VBox(10);
        for (Booking b : bookingController.getAllBookings()) {
            HBox bRow = new HBox(14);
            bRow.setAlignment(Pos.CENTER_LEFT);
            bRow.setPadding(new Insets(12, 16, 12, 16));
            bRow.setStyle(
                "-fx-background-color: " + Theme.BG_COLOR + ";"
                + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
                + "-fx-border-radius: 10px;"
                + "-fx-background-radius: 10px;"
            );

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            Text t = new Text(b.getItemOrServiceName() + "  (" + b.getCategory() + ")");
            t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 14px;");
            Text u = new Text("👤 " + b.getUserEmail() + "   |   📅 " + b.getDate() + "   |   ID: " + b.getId());
            u.setStyle(Theme.mutedTextStyle());
            info.getChildren().addAll(t, u);

            String status = b.getStatus();
            Label statusLbl = new Label(status);
            statusLbl.setStyle("CONFIRMED".equals(status) ? Theme.successBadgeStyle()
                : "PENDING".equals(status) ? Theme.warningBadgeStyle() : Theme.badgeStyle());

            bRow.getChildren().addAll(info, statusLbl);
            bookingsList.getChildren().add(bRow);
        }

        bookingsCard.getChildren().addAll(bookHeaderRow, bookingsList);
        mainContent.getChildren().addAll(topBar, headingBox, statsBox, bookingsCard);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onLogout) {
        Node node = getPageNode(onLogout);
        BorderPane rootPane = new BorderPane(node);
        rootPane.setStyle(Theme.rootPaneStyle());

        // ─── Top Bar (dark green gradient) ──────────────────────
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 30, 16, 30));
        topBar.setStyle(
            "-fx-background-color: linear-gradient(to right, #2E4A18, #4F772D);"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 12, 0, 0, 4);"
        );

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefSize(36, 36);
        roleBadge.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 10px;");
        Text roleIcon = new Text("🛡️");
        roleIcon.setStyle("-fx-font-size: 16px;");
        roleBadge.getChildren().add(roleIcon);
        Text logoTxt = new Text("StudentExpress  •  System Admin Console");
        logoTxt.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");
        logoRow.getChildren().addAll(roleBadge, logoTxt);
        HBox.setHgrow(logoRow, Priority.ALWAYS);

        Button backAppBtn = new Button("← Back to App");
        backAppBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);"
            + "-fx-text-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 700;"
            + "-fx-border-color: rgba(255,255,255,0.3);"
            + "-fx-border-radius: 10px;"
            + "-fx-background-radius: 10px;"
            + "-fx-padding: 8px 16px;"
            + "-fx-cursor: hand;"
        );
        backAppBtn.setOnAction(e -> Main.showHomePage());

        Button logoutBtn = new Button("Logout");

        logoutBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);"
            + "-fx-text-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 700;"
            + "-fx-border-color: rgba(255,255,255,0.3);"
            + "-fx-border-radius: 10px;"
            + "-fx-background-radius: 10px;"
            + "-fx-padding: 8px 16px;"
            + "-fx-cursor: hand;"
        );
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        topBar.getChildren().addAll(logoRow, backAppBtn, logoutBtn);
        rootPane.setTop(topBar);


        adminScene = new Scene(rootPane, 1050, 700);
        return adminScene;
    }

    private VBox createStatCard(String iconPath, String title, String value, String accentColor, String trend) {
        VBox b = new VBox(10);
        b.setPrefWidth(200);
        b.setPadding(new Insets(18));
        b.setStyle(Theme.statCardStyle(accentColor));

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(40, 40);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");
        Node iconNode = IconFactory.getIconNode(iconPath, accentColor, 20);
        iconBadge.getChildren().add(iconNode);

        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 30px; -fx-font-weight: 800;");
        Text lblTxt = new Text(title);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 600;");
        Text trendTxt = new Text(trend);
        trendTxt.setStyle("-fx-fill: #2E7D32; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");

        b.getChildren().addAll(iconBadge, valTxt, lblTxt, trendTxt);
        return b;
    }
}
