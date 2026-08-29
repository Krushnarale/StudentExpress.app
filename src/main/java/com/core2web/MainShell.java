package com.core2web;

import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.chatbot.AIChatBot;
import com.core2web.view.component.GlobalSearchPopup;
import java.util.HashMap;
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
    private AIChatBot aiChatBot;

    private Button btnHome;
    private Button btnRent;
    private Button btnBuySell;
    private Button btnRoommates;
    private Button btnServices;
    private Button btnSaved;
    private Button btnMessages;
    private Button btnRentals;
    private Button btnOrders;
    private Button btnProfile;

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

        // Add Floating StudentExpress AI ChatBot overlay
        aiChatBot = new AIChatBot();

        StackPane centerWrapper = new StackPane(centerContainer, globalSearchPopup, aiChatBot);
        centerWrapper.setStyle("-fx-background-color: transparent;");
        centerWrapper.setMinHeight(0);
        setCenter(centerWrapper);
    }


    private void buildHeader() {
        topHeader = new HBox(16);
        topHeader.setAlignment(Pos.CENTER_LEFT);
        topHeader.setPadding(new Insets(8, 24, 8, 24));
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
        notifBtn.setOnAction(e -> showAlert("Notifications", "1. Rental Request Accepted for PG Room!\n2. Study Table rental expires in 25 days\n3. New message from Roommate inquiry"));

        StackPane dotBadge = new StackPane();
        dotBadge.setPrefSize(8, 8);
        dotBadge.setMaxSize(8, 8);
        dotBadge.setStyle("-fx-background-color: #E53E3E; -fx-background-radius: 4px;");
        StackPane.setAlignment(dotBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(dotBadge, new Insets(2, 2, 0, 0));
        notifWrapper.getChildren().addAll(notifBtn, dotBadge);

        // Profile Button
        profileBtn = new Button("Darshan");
        profileBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 16));
        profileBtn.setStyle(Theme.profileBtnStyle());
        profileBtn.setOnAction(e -> Main.showProfilePage());

        // Top Logout / Switch Portal Button
        Button topLogoutBtn = new Button("Logout");
        topLogoutBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_LOGOUT, "#C62828", 14));
        topLogoutBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 7px 14px;");
        topLogoutBtn.setOnAction(e -> {
            new com.core2web.controller.AuthController().logout();
            Main.showUserSelectionPage();
        });

        topHeader.getChildren().addAll(logoBox, searchContainer, notifWrapper, profileBtn, topLogoutBtn);
    }

    private Map<String, String> navSvgPaths = new HashMap<>();

    private void buildSidebar() {
        leftSidebar = new VBox(3);
        leftSidebar.setPadding(new Insets(6, 10, 6, 10));
        leftSidebar.setPrefWidth(270);
        leftSidebar.setMinWidth(270);
        leftSidebar.setMaxWidth(270);
        leftSidebar.setMinHeight(0);
        leftSidebar.setStyle(Theme.sidebarStyle());

        // Bind leftSidebar maxHeight strictly to available mainShell height minus header height
        leftSidebar.maxHeightProperty().bind(heightProperty().subtract(topHeader.heightProperty()));

        // EXPLORE Section
        Label exploreLabel = createSectionLabel("EXPLORE");
        VBox.setMargin(exploreLabel, new Insets(1, 0, 1, 0));

        btnHome = createVectorSidebarBtn("Home", IconFactory.PATH_HOME, "HOME", () -> Main.showHomePage());
        btnRent = createVectorSidebarBtn("Rent", IconFactory.PATH_KEY, "RENT", () -> Main.showRentPage());
        btnBuySell = createVectorSidebarBtn("Buy & Sell", IconFactory.PATH_SHOPPING_BAG, "BUY_SELL", () -> Main.showBuySellPage());
        btnRoommates = createVectorSidebarBtn("Roommates", IconFactory.PATH_USERS, "ROOMMATES", () -> Main.showRoommateFinderPage());
        btnServices = createVectorSidebarBtn("Services", IconFactory.PATH_WRENCH, "SERVICES", () -> Main.showServicesPage());

        // PERSONAL Section
        Label personalLabel = createSectionLabel("PERSONAL");
        VBox.setMargin(personalLabel, new Insets(6, 0, 1, 0));

        btnSaved = createVectorSidebarBtn("Saved", IconFactory.PATH_HEART_OUTLINE, "SAVED", () -> Main.showSavedItemsPage());
        btnMessages = createVectorSidebarBtn("Messages", IconFactory.PATH_MESSAGE, "MESSAGES", () -> showAlert("Messages", "No unread messages."));
        btnRentals = createVectorSidebarBtn("My Rentals", IconFactory.PATH_KEY, "MY_RENTALS", () -> Main.showMyRentalsPage());
        btnOrders = createVectorSidebarBtn("My Orders", IconFactory.PATH_SHOPPING_BAG, "ORDERS", () -> Main.showMyOrdersPage());

        // ACCOUNT Section
        Label accountLabel = createSectionLabel("ACCOUNT");
        VBox.setMargin(accountLabel, new Insets(6, 0, 1, 0));

        btnProfile = createVectorSidebarBtn("Profile", IconFactory.PATH_USER, "PROFILE", () -> Main.showProfilePage());

        Button btnLogout = createVectorSidebarBtn("Logout / Switch Login", IconFactory.PATH_LOGOUT, "LOGOUT", () -> {
            new com.core2web.controller.AuthController().logout();
            Main.showUserSelectionPage();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Compact Promo Card
        VBox promoCard = new VBox(4);
        promoCard.setPadding(new Insets(6, 10, 6, 10));
        VBox.setMargin(promoCard, new Insets(3, 0, 0, 0));
        promoCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.12);"
            + "-fx-background-radius: 10px;"
            + "-fx-border-color: rgba(255, 255, 255, 0.20);"
            + "-fx-border-radius: 10px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );
        Text promoText = new Text("List your item or room today!");
        promoText.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 11.5px;");

        Button postNowBtn = new Button("Post Now →");
        postNowBtn.setMaxWidth(Double.MAX_VALUE);
        postNowBtn.setStyle(
            "-fx-background-color: white;"
            + "-fx-text-fill: " + Theme.PRIMARY_DARK + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-weight: 800;"
            + "-fx-font-size: 11px;"
            + "-fx-padding: 4px;"
            + "-fx-background-radius: 5px;"
            + "-fx-cursor: hand;"
        );
        postNowBtn.setOnAction(e -> Main.showPostItemPage());
        promoCard.getChildren().addAll(promoText, postNowBtn);

        leftSidebar.getChildren().addAll(
            exploreLabel, btnHome, btnRent, btnBuySell, btnRoommates, btnServices,
            personalLabel, btnSaved, btnMessages, btnRentals, btnOrders,
            accountLabel, btnProfile, btnLogout,
            spacer, promoCard
        );
    }

    private Label createSectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(Theme.sidebarSectionLabelStyle());
        return lbl;
    }

    private Button createVectorSidebarBtn(String text, String svgPath, String key, Runnable action) {
        Button btn = new Button("  " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        navSvgPaths.put(key, svgPath);

        Node iconNode = IconFactory.getIconNode(svgPath, "#FFFFFF", 20);
        btn.setGraphic(iconNode);

        btn.setStyle(Theme.sidebarBtnStyle(false));

        btn.setOnMouseEntered(e -> {
            if (!key.equalsIgnoreCase(currentNavKey)) {
                btn.setStyle(Theme.sidebarBtnHoverStyle());
                btn.setGraphic(IconFactory.getIconNode(svgPath, "#FFFFFF", 20));
            }
        });
        btn.setOnMouseExited(e -> {
            if (!key.equalsIgnoreCase(currentNavKey)) {
                btn.setStyle(Theme.sidebarBtnStyle(false));
                btn.setGraphic(IconFactory.getIconNode(svgPath, "#FFFFFF", 20));
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
                btn.setGraphic(IconFactory.getIconNode(path, isActive ? Theme.PRIMARY_DARK : "#FFFFFF", 20));
            }
        });

        // Hide search popup on navigation change
        if (globalSearchPopup != null) {
            globalSearchPopup.setVisible(false);
        }

        User user = DataRepository.getInstance().getCurrentUser();
        if (user == null) user = com.core2web.util.SessionManager.getInstance().getCurrentUser();
        if (user != null && user.getName() != null && !user.getName().trim().isEmpty() && !user.getName().equals("Not provided")) {
            profileBtn.setText(user.getName().trim());
        } else {
            profileBtn.setText("My Profile");
        }
    }

    public void showShellContent(String activeNavKey, Node pageContent) {
        setTop(topHeader);
        setLeft(leftSidebar);
        updateActiveNav(activeNavKey);
        centerContainer.getChildren().setAll(pageContent);
        // Show chatbot only inside the main authenticated shell
        if (aiChatBot != null) aiChatBot.setVisible(true);
    }

    public void showFullContent(Node pageContent) {
        // Remove header and sidebar for full-screen pages
        setTop(null);
        setLeft(null);

        // Hide chatbot on pre-login full-screen pages (Splash, Welcome, Login, Sign Up)
        if (aiChatBot != null) aiChatBot.setVisible(false);

        // Make the page occupy the complete available center area
        if (pageContent instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region page = (javafx.scene.layout.Region) pageContent;

            page.setMinWidth(0);
            page.setMinHeight(0);

            page.setMaxWidth(Double.MAX_VALUE);
            page.setMaxHeight(Double.MAX_VALUE);
        }

        // Center and stretch the page inside MainShell
        javafx.scene.layout.StackPane.setAlignment(
            pageContent,
            javafx.geometry.Pos.CENTER
        );

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


