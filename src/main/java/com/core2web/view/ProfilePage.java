package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.UserController;
import com.core2web.model.User;
import com.core2web.util.IconFactory;
import com.core2web.util.SessionManager;
import com.core2web.util.Theme;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class ProfilePage {

    private Scene profileScene;
    private Text nameText;
    private Text emailText;
    private String collegeName = "COEP Technological University, Pune";
    private String branchName = "B.Tech Computer Engineering (3rd Year)";
    private String userLocation = "Kothrud, Pune";

    public Node getPageNode(
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices
    ) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            currentUser = new User("1", "Student", "student@express.com", "+91 98765 43210", User.Role.STUDENT);
        }


        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20, 40, 30, 40));
        mainContent.setMaxWidth(680);

        // Profile Cover Banner
        StackPane coverBanner = new StackPane();
        coverBanner.setPrefHeight(120);
        coverBanner.setStyle(
            "-fx-background-color: linear-gradient(to right, #2E4A18, #4F772D, #6A9E45);"
            + "-fx-background-radius: 18px;"
        );

        // Avatar + Info Card
        HBox avatarInfoCard = new HBox(20);
        avatarInfoCard.setPadding(new Insets(16, 24, 20, 24));
        avatarInfoCard.setAlignment(Pos.CENTER_LEFT);
        avatarInfoCard.setStyle(Theme.elevatedCardStyle());

        StackPane avatarBox = new StackPane();
        avatarBox.setPrefSize(74, 74);
        avatarBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + Theme.PRIMARY + ", " + Theme.PRIMARY_DARK + ");"
            + "-fx-background-radius: 37px;"
            + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.3), 10, 0, 0, 3);"
        );
        Text avatarInitial = new Text(currentUser.getName() != null && !currentUser.getName().isEmpty() ? currentUser.getName().substring(0, 1).toUpperCase() : "D");
        avatarInitial.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 32px; -fx-font-weight: bold;");
        avatarBox.getChildren().add(avatarInitial);

        VBox userDetails = new VBox(4);
        HBox.setHgrow(userDetails, Priority.ALWAYS);

        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameText = new Text(currentUser.getName());
        nameText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label roleBadge = new Label(currentUser.getRole().toString().replace("_", " "));
        roleBadge.setStyle(Theme.badgeStyle());
        nameRow.getChildren().addAll(nameText, roleBadge);

        emailText = new Text(currentUser.getEmail() + "   •   " + currentUser.getPhone());
        emailText.setStyle(Theme.mutedTextStyle());
        userDetails.getChildren().addAll(nameRow, emailText);

        Button editProfileBtn = new Button("Edit Profile");
        editProfileBtn.setStyle(Theme.primaryBtnStyle());
        editProfileBtn.setOnAction(e -> showEditProfileDialog());

        Button logoutHeaderBtn = new Button("Log Out");
        logoutHeaderBtn.setStyle(Theme.dangerBtnStyle());
        logoutHeaderBtn.setOnAction(e -> handleLogout());

        avatarInfoCard.getChildren().addAll(avatarBox, userDetails, editProfileBtn, logoutHeaderBtn);

        VBox profileHeader = new VBox(0);
        profileHeader.getChildren().addAll(coverBanner, avatarInfoCard);

        // Profile Completion
        VBox completionCard = new VBox(10);
        completionCard.setPadding(new Insets(16, 20, 16, 20));
        completionCard.setStyle(Theme.cardStyle());

        HBox compHeader = new HBox(10);
        compHeader.setAlignment(Pos.CENTER_LEFT);
        Text compTitle = new Text("Profile Completeness");
        compTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(compTitle, Priority.ALWAYS);

        Text compPct = new Text("90% Complete");
        compPct.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 13px; -fx-font-weight: 800;");
        compHeader.getChildren().addAll(compTitle, compPct);

        ProgressBar pBar = new ProgressBar(0.9);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setStyle("-fx-accent: " + Theme.PRIMARY + "; -fx-control-inner-background: " + Theme.PRIMARY_LIGHT + ";");

        Text compSub = new Text("Add an emergency contact & college ID proof to reach 100% verified status.");
        compSub.setStyle(Theme.mutedTextStyle());

        completionCard.getChildren().addAll(compHeader, pBar, compSub);

        // Personal & Academic Details Card
        VBox academicCard = new VBox(12);
        academicCard.setPadding(new Insets(18, 20, 18, 20));
        academicCard.setStyle(Theme.cardStyle());

        Text academicTitle = new Text("Personal & Academic Info");
        academicTitle.setStyle(Theme.sectionHeaderStyle());

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);

        infoGrid.add(createGridLabel("College / University:"), 0, 0);
        infoGrid.add(createGridVal(collegeName), 1, 0);

        infoGrid.add(createGridLabel("Course / Branch:"), 0, 1);
        infoGrid.add(createGridVal(branchName), 1, 1);

        infoGrid.add(createGridLabel("Location / Area:"), 0, 2);
        infoGrid.add(createGridVal(userLocation), 1, 2);

        infoGrid.add(createGridLabel("Mobile Number:"), 0, 3);
        infoGrid.add(createGridVal(currentUser.getPhone()), 1, 3);

        infoGrid.add(createGridLabel("Email Address:"), 0, 4);
        infoGrid.add(createGridVal(currentUser.getEmail()), 1, 4);

        academicCard.getChildren().addAll(academicTitle, new Separator(), infoGrid);

        // Quick Stats Row
        HBox quickStats = new HBox(16);
        quickStats.getChildren().addAll(
            quickStatItem("4", "Active\nBookings", "#2563EB"),
            quickStatItem("7", "My\nPosts", "#4F772D"),
            quickStatItem("12", "Orders\nPlaced", "#7C3AED")
        );

        // Workspace Switcher Card
        VBox portalSwitchCard = new VBox(12);
        portalSwitchCard.setPadding(new Insets(18, 20, 18, 20));
        portalSwitchCard.setStyle(Theme.cardStyle());

        Text switchTitle = new Text("Switch Workspace / Role Portal");
        switchTitle.setStyle(Theme.sectionHeaderStyle());

        HBox switchBtnsRow = new HBox(10);
        Button btnStudent = new Button("Student App");
        btnStudent.setStyle(Theme.primaryBtnStyle());
        btnStudent.setOnAction(e -> Main.showHomePage());

        Button btnOwner = new Button("Owner Portal");
        btnOwner.setStyle(Theme.secondaryBtnStyle());
        btnOwner.setOnAction(e -> Main.showOwnerDashboard());

        Button btnSeller = new Button("Seller Portal");
        btnSeller.setStyle(Theme.secondaryBtnStyle());
        btnSeller.setOnAction(e -> Main.showSellerDashboard());

        Button btnService = new Button("Provider Portal");
        btnService.setStyle(Theme.secondaryBtnStyle());
        btnService.setOnAction(e -> Main.showServiceProviderDashboard());

        Button btnAdmin = new Button("Admin Portal");
        btnAdmin.setStyle(Theme.secondaryBtnStyle());
        btnAdmin.setOnAction(e -> Main.showAdminDashboard());

        Button btnSwitchLogin = new Button("Logout / Switch Portal");
        btnSwitchLogin.setStyle(Theme.dangerBtnStyle());
        btnSwitchLogin.setOnAction(e -> handleLogout());

        switchBtnsRow.getChildren().addAll(btnStudent, btnOwner, btnSeller, btnService, btnAdmin, btnSwitchLogin);
        portalSwitchCard.getChildren().addAll(switchTitle, switchBtnsRow);

        // Menu Groups with Vector SVG Icons
        VBox actionsGroup = buildMenuGroup("MY ACTIVITY",
            new String[]{IconFactory.PATH_CALENDAR, IconFactory.PATH_PACKAGE, IconFactory.PATH_SHOPPING_BAG, IconFactory.PATH_HEART_FILLED},
            new String[]{"My Bookings", "My Posts", "My Orders", "Saved Items"},
            new Runnable[]{
                () -> Main.showMyBookingsPage(),
                () -> Main.showMyPostsPage(),
                () -> Main.showMyOrdersPage(),
                () -> Main.showSavedItemsPage()
            }
        );

        VBox supportGroup = buildMenuGroup("SUPPORT & HELP",
            new String[]{IconFactory.PATH_BELL, IconFactory.PATH_USERS, IconFactory.PATH_WRENCH},
            new String[]{"Notifications", "Help & Support", "App Settings"},
            new Runnable[]{
                () -> showAlert("Notifications", "You have 3 active notifications."),
                () -> showAlert("Help & Support", "Email: support@studentexpress.com\nHelpline: 1800-200-5555"),
                () -> showAlert("Settings", "App version 1.0.0 (Latest)")
            }
        );

        mainContent.getChildren().addAll(
            profileHeader, completionCard, academicCard, quickStats,
            portalSwitchCard, actionsGroup, supportGroup
        );

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices
    ) {
        Node node = getPageNode(onNavigateHome, onNavigateRent, onNavigateBuySell, onNavigateRoommates, onNavigateServices);
        profileScene = new Scene(new BorderPane(node), 1050, 700);
        return profileScene;
    }

    private Label createGridLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        return l;
    }

    private Label createGridVal(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        return l;
    }

    private void showEditProfileDialog() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile Information");
        dialog.setHeaderText("Update your personal details:");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(400);

        TextField nameField = new TextField(u.getName());
        TextField phoneField = new TextField(u.getPhone());
        TextField emailField = new TextField(u.getEmail());
        TextField collegeField = new TextField(collegeName);
        TextField branchField = new TextField(branchName);

        final File[] selectedAvatarFile = new File[1];
        Button chooseAvatarBtn = new Button("📷 Choose Profile Photo");
        chooseAvatarBtn.setStyle(Theme.outlineBtnStyle());
        Label avatarFileLbl = new Label("No file chosen");
        avatarFileLbl.setStyle(Theme.mutedTextStyle());
        HBox avatarBox = new HBox(10, chooseAvatarBtn, avatarFileLbl);
        avatarBox.setAlignment(Pos.CENTER_LEFT);
        chooseAvatarBtn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Profile Avatar");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File f = chooser.showOpenDialog(null);
            if (f != null) {
                selectedAvatarFile[0] = f;
                avatarFileLbl.setText(f.getName());
            }
        });

        content.getChildren().addAll(
            new Label("Full Name:"), nameField,
            new Label("Mobile Number:"), phoneField,
            new Label("Email Address:"), emailField,
            new Label("College / University:"), collegeField,
            new Label("Branch / Year:"), branchField,
            new Label("Profile Photo:"), avatarBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (selectedAvatarFile[0] != null && selectedAvatarFile[0].exists()) {
                    com.core2web.service.CloudinaryService.UploadResult uploadRes =
                        com.core2web.service.CloudinaryService.uploadImage(selectedAvatarFile[0], "profileImages");
                    if (uploadRes != null && uploadRes.isSuccess()) {
                        u.setProfileImage(uploadRes.getSecureUrl());
                    }
                }
                if (!nameField.getText().trim().isEmpty()) {
                    u.setName(nameField.getText().trim());
                }
                if (!phoneField.getText().trim().isEmpty()) {
                    u.setPhone(phoneField.getText().trim());
                }
                if (!emailField.getText().trim().isEmpty()) {
                    u.setEmail(emailField.getText().trim());
                }
                if (!collegeField.getText().trim().isEmpty()) {
                    collegeName = collegeField.getText().trim();
                    u.setCollege(collegeName);
                }
                if (!branchField.getText().trim().isEmpty()) {
                    branchName = branchField.getText().trim();
                    u.setBranch(branchName);
                }

                new UserController().updateCurrentUser(u);

                nameText.setText(u.getName());
                emailText.setText(u.getEmail() + "   •   " + u.getPhone());


                showAlert("Profile Updated", "Your profile details have been saved successfully!");
                Main.showProfilePage();
            }
        });
    }

    private VBox quickStatItem(String value, String label, String accentColor) {
        VBox b = new VBox(4);
        b.setPrefWidth(140);
        b.setPadding(new Insets(16, 18, 16, 18));
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(Theme.statCardStyle(accentColor));

        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");
        Text lblTxt = new Text(label);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 600;");
        b.getChildren().addAll(valTxt, lblTxt);
        return b;
    }

    private VBox buildMenuGroup(String groupLabel, String[] svgPaths, String[] titles, Runnable[] actions) {
        VBox group = new VBox(0);
        group.setStyle(Theme.cardStyle());

        Label grpHeader = new Label(groupLabel);
        grpHeader.setStyle(
            "-fx-text-fill: " + Theme.TEXT_MUTED + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px;"
            + "-fx-font-weight: 700;"
            + "-fx-padding: 12px 20px 8px 20px;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-width: 0 0 1 0;"
        );

        group.getChildren().add(grpHeader);
        for (int i = 0; i < titles.length; i++) {
            group.getChildren().add(createMenuItem(svgPaths[i], titles[i], actions[i], i == titles.length - 1));
        }
        return group;
    }

    private HBox createMenuItem(String svgPath, String title, Runnable action, boolean isLast) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(14, 20, 14, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        if (!isLast) {
            item.setStyle("-fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        } else {
            item.setStyle("-fx-cursor: hand;");
        }

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(36, 36);
        iconBadge.setMinSize(36, 36);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Node iconNode = IconFactory.getIconNode(svgPath, Theme.PRIMARY, 16);
        iconBadge.getChildren().add(iconNode);

        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 600;");
        HBox.setHgrow(titleTxt, Priority.ALWAYS);

        Text arrowTxt = new Text("›");
        arrowTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 18px; -fx-font-weight: 300;");

        item.getChildren().addAll(iconBadge, titleTxt, arrowTxt);

        item.setOnMouseEntered(e -> item.setStyle(item.getStyle() + "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"));
        item.setOnMouseExited(e -> item.setStyle(isLast ? "-fx-cursor: hand;" : "-fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));

        item.setOnMouseClicked(e -> { if (action != null) action.run(); });
        return item;
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be returned to the portal selection screen.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Main.showUserSelectionPage();
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
