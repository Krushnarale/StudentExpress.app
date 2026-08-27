package com.core2web.view;

import com.core2web.Main;
import com.core2web.model.User;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.GlobalSearchPopup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MainShell extends BorderPane {

    private HBox topHeader;
    private VBox leftSidebar;
    private StackPane centerContainer;
    private GlobalSearchPopup globalSearchPopup;

    private Button btnHome;
    private Button btnRent;
    private Button btnBuySell;
    private Button btnRoommates;
    private Button btnServices;
    private Button btnSaved;
    private Button btnMessages;
    private Button btnBookings;
    private Button btnOrders;
    private Button btnProfile;
    private Button btnDashboard;

    private Button profileBtn;
    private String currentNavKey = "";
    private Map<String, Button> navButtons = new HashMap<>();

    public MainShell() {
        setStyle(Theme.rootPaneStyle());
        setMinHeight(0);
        buildHeader();
        buildSidebar();

        centerContainer = new StackPane();
        centerContainer.setStyle("-fx-background-color: transparent;");
        centerContainer.setMinHeight(0);

        // Add Global Search Popup floating overlay in center container
        globalSearchPopup = new GlobalSearchPopup();
        globalSearchPopup.setVisible(false);
        StackPane.setAlignment(globalSearchPopup, Pos.TOP_LEFT);
        StackPane.setMargin(globalSearchPopup, new Insets(10, 0, 0, 10));

        StackPane centerWrapper = new StackPane(centerContainer, globalSearchPopup);
        centerWrapper.setStyle("-fx-background-color: transparent;");
        centerWrapper.setMinHeight(0);
        setCenter(centerWrapper);
    }

    private void buildHeader() {
        topHeader = new HBox(16);
        topHeader.setAlignment(Pos.CENTER_LEFT);
        topHeader.setPadding(new Insets(12, 28, 12, 28));
        topHeader.setStyle(Theme.topBarStyle());

        // Logo
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-cursor: hand;");
        logoBox.setOnMouseClicked(e -> Main.showHomePage());

        Node logoIconNode = IconFactory.getIconNode(IconFactory.PATH_GRADUATION_CAP, Theme.PRIMARY, 32);
        Text logoText = new Text("StudentExpress");
        logoText.setStyle(Theme.logoTextStyle());
        logoBox.getChildren().addAll(logoIconNode, logoText);

        // Search Bar with Vector Icon
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchContainer, Priority.ALWAYS);

        Node searchIconNode = IconFactory.getIconNode(IconFactory.PATH_SEARCH, Theme.TEXT_MUTED, 16);
        TextField searchField = new TextField();
        searchField.setPromptText("Search rooms, bikes, books, electronics, services...");
        searchField.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-prompt-text-fill: " + Theme.TEXT_MUTED + ";");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (globalSearchPopup != null) {
                globalSearchPopup.updateSearch(newVal);
            }
        });

        searchContainer.getChildren().addAll(searchIconNode, searchField);

        // Notification Button
        StackPane notifWrapper = new StackPane();
        Button notifBtn = new Button();
        notifBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_BELL, Theme.PRIMARY, 16));
        notifBtn.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-background-radius: 20px;"
            + "-fx-border-radius: 20px;"
            + "-fx-padding: 7px 11px;"
            + "-fx-cursor: hand;"
        );

        Label notifBadgeLbl = new Label("0");
        notifBadgeLbl.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1px 4px; -fx-background-radius: 8px;");
        StackPane.setAlignment(notifBadgeLbl, Pos.TOP_RIGHT);
        StackPane.setMargin(notifBadgeLbl, new Insets(-2, -2, 0, 0));

        Runnable updateNotifBadge = () -> {
            String uid = com.core2web.util.SessionManager.getInstance().getUid();
            if (uid != null) {
                new Thread(() -> {
                    com.core2web.controller.NotificationController notifCtrl = new com.core2web.controller.NotificationController();
                    int count = notifCtrl.getUnreadNotificationCount(uid);
                    javafx.application.Platform.runLater(() -> {
                        notifBadgeLbl.setText(String.valueOf(count));
                        notifBadgeLbl.setVisible(count > 0);
                    });
                }).start();
            }
        };

        notifBtn.setOnAction(e -> {
            String uid = com.core2web.util.SessionManager.getInstance().getUid();
            com.core2web.controller.NotificationController notifCtrl = new com.core2web.controller.NotificationController();
            List<com.core2web.model.Notification> list = notifCtrl.getNotificationsByUser(uid);

            StringBuilder sb = new StringBuilder();
            if (list == null || list.isEmpty()) {
                sb.append("No new notifications.");
            } else {
                int idx = 1;
                for (com.core2web.model.Notification n : list) {
                    sb.append(idx++).append(". ").append(n.getTitle()).append("\n   ").append(n.getMessage()).append("\n\n");
                    notifCtrl.markAsRead(n.getNotificationId());
                }
            }
            showAlert("Notifications", sb.toString().trim());
            updateNotifBadge.run();
        });

        updateNotifBadge.run();
        notifWrapper.getChildren().addAll(notifBtn, notifBadgeLbl);

        // Profile Button
        String currentUserName = com.core2web.util.SessionManager.getInstance().getName();
        profileBtn = new Button(currentUserName != null ? currentUserName : "Profile");
        profileBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 16));
        profileBtn.setStyle(Theme.profileBtnStyle());
        profileBtn.setOnAction(e -> Main.showProfilePage());

        // Top Logout / Switch Portal Button
        Button topLogoutBtn = new Button("Logout");
        topLogoutBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_LOGOUT, "#C62828", 14));
        topLogoutBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 7px 14px;");
        topLogoutBtn.setOnAction(e -> Main.showUserSelectionPage());

        topHeader.getChildren().addAll(logoBox, searchContainer, notifWrapper, profileBtn, topLogoutBtn);
    }

    private Map<String, String> navSvgPaths = new HashMap<>();

    private void buildSidebar() {
        VBox sidebarContent = new VBox(6);
        sidebarContent.setPadding(new Insets(18, 14, 18, 14));
        sidebarContent.setStyle("-fx-background-color: transparent;");

        // EXPLORE Section
        Label exploreLabel = createSectionLabel("EXPLORE");

        btnHome = createVectorSidebarBtn("Home", IconFactory.PATH_HOME, "HOME", () -> Main.showHomePage());
        btnRent = createVectorSidebarBtn("Rent", IconFactory.PATH_KEY, "RENT", () -> Main.showRentPage());
        btnBuySell = createVectorSidebarBtn("Buy & Sell", IconFactory.PATH_SHOPPING_BAG, "BUY_SELL", () -> Main.showBuySellPage());
        btnRoommates = createVectorSidebarBtn("Roommates", IconFactory.PATH_USERS, "ROOMMATES", () -> Main.showRoommateFinderPage());
        btnServices = createVectorSidebarBtn("Services", IconFactory.PATH_WRENCH, "SERVICES", () -> Main.showServicesPage());

        // PERSONAL Section
        Label personalLabel = createSectionLabel("PERSONAL");
        btnSaved = createVectorSidebarBtn("Saved", IconFactory.PATH_HEART_OUTLINE, "SAVED", () -> Main.showSavedItemsPage());
        btnMessages = createVectorSidebarBtn("Messages", IconFactory.PATH_MESSAGE, "MESSAGES", () -> showAlert("Messages", "No unread messages."));
        btnBookings = createVectorSidebarBtn("My Bookings", IconFactory.PATH_KEY, "BOOKINGS", () -> Main.showMyBookingsPage());
        btnOrders = createVectorSidebarBtn("My Orders", IconFactory.PATH_SHOPPING_BAG, "ORDERS", () -> Main.showMyOrdersPage());

        // ACCOUNT Section
        Label accountLabel = createSectionLabel("ACCOUNT");
        btnProfile = createVectorSidebarBtn("Profile", IconFactory.PATH_USER, "PROFILE", () -> Main.showProfilePage());

        Button btnLogout = createVectorSidebarBtn("Logout / Switch Login", IconFactory.PATH_LOGOUT, "LOGOUT", () -> {
            Main.showUserSelectionPage();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Promo Card
        VBox promoCard = new VBox(8);
        promoCard.setPadding(new Insets(14));
        promoCard.setStyle(Theme.gradientPanelStyle() + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.2), 10, 0, 0, 4);");
        Text promoText = new Text("List your item\nor room today!");
        promoText.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 13px;");

        Button postNowBtn = new Button("Post Now →");
        postNowBtn.setMaxWidth(Double.MAX_VALUE);
        postNowBtn.setStyle(
            "-fx-background-color: white;"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 800;"
            + "-fx-font-size: 12px;"
            + "-fx-padding: 8px;"
            + "-fx-background-radius: 8px;"
            + "-fx-cursor: hand;"
        );
        postNowBtn.setOnAction(e -> Main.showPostItemPage());
        promoCard.getChildren().addAll(promoText, postNowBtn);

        sidebarContent.getChildren().addAll(
            exploreLabel, btnHome, btnRent, btnBuySell, btnRoommates, btnServices,
            personalLabel, btnSaved, btnMessages, btnBookings, btnOrders,
            accountLabel, btnProfile, btnLogout,
            spacer, promoCard
        );

        ScrollPane sidebarScroll = new ScrollPane(sidebarContent);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setMinHeight(0);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

        leftSidebar = new VBox(sidebarScroll);
        leftSidebar.setPrefWidth(270);
        leftSidebar.setMinWidth(270);
        leftSidebar.setMaxWidth(270);
        leftSidebar.setMinHeight(0);
        leftSidebar.setStyle(Theme.sidebarStyle());
        VBox.setVgrow(sidebarScroll, Priority.ALWAYS);
    }

    private Label createSectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-text-fill: " + Theme.TEXT_MUTED + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px;"
            + "-fx-font-weight: 800;"
            + "-fx-padding: 12 0 6 14px;"
        );
        return lbl;
    }

    private Button createVectorSidebarBtn(String text, String svgPath, String key, Runnable action) {
        Button btn = new Button("  " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        navSvgPaths.put(key, svgPath);

        Node iconNode = IconFactory.getIconNode(svgPath, Theme.TEXT_PRIMARY, 22);
        btn.setGraphic(iconNode);

        btn.setStyle(Theme.sidebarBtnStyle(false));

        btn.setOnMouseEntered(e -> {
            if (!key.equalsIgnoreCase(currentNavKey)) {
                btn.setStyle(
                    "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
                    + "-fx-text-fill: " + Theme.PRIMARY + ";"
                    + "-fx-font-family: " + Theme.FONT + ";"
                    + "-fx-font-weight: 700;"
                    + "-fx-font-size: 14.5px;"
                    + "-fx-padding: 10px 14px 10px 16px;"
                    + "-fx-background-radius: 12px;"
                    + "-fx-cursor: hand;"
                );
                btn.setGraphic(IconFactory.getIconNode(svgPath, Theme.PRIMARY, 22));
            }
        });
        btn.setOnMouseExited(e -> {
            if (!key.equalsIgnoreCase(currentNavKey)) {
                btn.setStyle(Theme.sidebarBtnStyle(false));
                btn.setGraphic(IconFactory.getIconNode(svgPath, Theme.TEXT_PRIMARY, 22));
            }
        });
        btn.setOnAction(e -> {
            if (action != null) action.run();
        });

        navButtons.put(key, btn);
        return btn;
    }

    public void updateActiveNav(String activeKey) {
        this.currentNavKey = activeKey != null ? activeKey : "";
        navButtons.forEach((key, btn) -> {
            boolean isActive = key.equalsIgnoreCase(this.currentNavKey);
            btn.setStyle(Theme.sidebarBtnStyle(isActive));
            String path = navSvgPaths.get(key);
            if (path != null) {
                btn.setGraphic(IconFactory.getIconNode(path, isActive ? Theme.PRIMARY : Theme.TEXT_PRIMARY, 22));
            }
        });

        // Hide search popup on navigation change
        if (globalSearchPopup != null) {
            globalSearchPopup.setVisible(false);
        }

        User user = com.core2web.util.SessionManager.getInstance().getCurrentUser();
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            profileBtn.setText(user.getName());
        }
    }

    public void showShellContent(String activeNavKey, Node pageContent) {
        setTop(topHeader);
        setLeft(leftSidebar);
        updateActiveNav(activeNavKey);
        centerContainer.getChildren().setAll(pageContent);
    }

    public void showFullContent(Node pageContent) {
        setTop(null);
        setLeft(null);
        centerContainer.getChildren().setAll(pageContent);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
