package com.core2web.view.authentication;

import com.core2web.Main;
import com.core2web.dao.SellerDAOImpl;
import com.core2web.model.SellerProfile;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.CloudinaryService;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.Theme;
import java.io.File;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class ProfilePage {

    private Scene profileScene;
    private Text nameText;
    private Text emailText;
    private Label collegeValLbl;
    private Label branchValLbl;
    private String collegeName = "Not provided";
    private String branchName = "Not provided";
    private String userLocation = "Not provided";

    public Node getPageNode(
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices
    ) {
        User resolvedUser = DataRepository.getInstance().getCurrentUser();
        if (resolvedUser == null) {
            resolvedUser = com.core2web.util.SessionManager.getInstance().getCurrentUser();
        }
        if (resolvedUser == null) {
            resolvedUser = new User("", "Not provided", "Not provided", "Not provided", User.Role.STUDENT);
        }
        final User currentUser = resolvedUser;

        collegeName = (currentUser.getCollege() != null && !currentUser.getCollege().trim().isEmpty())
            ? currentUser.getCollege().trim() : "Not provided";
        branchName = (currentUser.getBranch() != null && !currentUser.getBranch().trim().isEmpty())
            ? currentUser.getBranch().trim() : "Not provided";
        userLocation = (currentUser.getCollege() != null && !currentUser.getCollege().trim().isEmpty())
            ? currentUser.getCollege().trim() : "Not provided";

        VBox mainContent = new VBox(14);
        mainContent.setPadding(new Insets(18, 30, 20, 30));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        // 1. Compact Profile Header Card
        HBox avatarInfoCard = new HBox(18);
        avatarInfoCard.setPadding(new Insets(16, 20, 16, 20));
        avatarInfoCard.setAlignment(Pos.CENTER_LEFT);
        avatarInfoCard.setStyle(Theme.elevatedCardStyle());

        VBox avatarCol = new VBox(6);
        avatarCol.setAlignment(Pos.CENTER);

        StackPane avatarBox = new StackPane();
        avatarBox.setPrefSize(68, 68);
        avatarBox.setMinSize(68, 68);
        avatarBox.setMaxSize(68, 68);
        avatarBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + Theme.PRIMARY + ", " + Theme.PRIMARY_DARK + ");"
            + "-fx-background-radius: 34px;"
            + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.3), 8, 0, 0, 2);"
        );

        Image userImg = null;
        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().trim().isEmpty()) {
            userImg = com.core2web.util.ImageUtil.loadImage(currentUser.getProfileImage());
        }

        if (userImg != null && !userImg.isError()) {
            ImageView imgView = new ImageView(userImg);
            imgView.setFitWidth(68);
            imgView.setFitHeight(68);
            imgView.setPreserveRatio(false);
            Circle clip = new Circle(34, 34, 34);
            imgView.setClip(clip);
            avatarBox.getChildren().add(imgView);
        } else {
            String initial = (currentUser.getName() != null && !currentUser.getName().isEmpty())
                ? currentUser.getName().substring(0, 1).toUpperCase() : "S";
            Text avatarInitial = new Text(initial);
            avatarInitial.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: bold;");
            avatarBox.getChildren().add(avatarInitial);
        }

        HBox photoBtnRow = new HBox(4);
        photoBtnRow.setAlignment(Pos.CENTER);

        Button changePhotoBtn = new Button("📷 " + (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty() ? "Change" : "Upload"));
        changePhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 9.5px; -fx-padding: 2px 6px;");
        changePhotoBtn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            java.io.File file = chooser.showOpenDialog(null);
            if (file != null) {
                try {
                    com.core2web.service.CloudinaryService.UploadResult res = com.core2web.service.CloudinaryService.uploadImage(file, "profileImages");
                    String imgUrl = (res != null && res.isSuccess()) ? res.getSecureUrl() : file.getAbsolutePath();
                    currentUser.setProfileImage(imgUrl);
                    if (res != null && res.isSuccess()) {
                        currentUser.setProfilePublicId(res.getPublicId());
                    }
                    new com.core2web.dao.UserDAOImpl().save(currentUser);
                    DataRepository.getInstance().setCurrentUser(currentUser);
                    com.core2web.util.SessionManager.getInstance().login(currentUser);
                    showAlert("Photo Updated", "Your profile photo has been updated successfully!");
                    Main.showProfilePage();
                } catch (Exception ex) {
                    currentUser.setProfileImage(file.getAbsolutePath());
                    new com.core2web.dao.UserDAOImpl().save(currentUser);
                    DataRepository.getInstance().setCurrentUser(currentUser);
                    com.core2web.util.SessionManager.getInstance().login(currentUser);
                    Main.showProfilePage();
                }
            }
        });

        photoBtnRow.getChildren().add(changePhotoBtn);

        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            Button removePhotoBtn = new Button("✕");
            removePhotoBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 9.5px; -fx-padding: 2px 5px;");
            removePhotoBtn.setOnAction(e -> {
                if (currentUser.getProfilePublicId() != null && !currentUser.getProfilePublicId().isEmpty()) {
                    com.core2web.service.CloudinaryService.deleteImage(currentUser.getProfilePublicId());
                }
                currentUser.setProfileImage("");
                currentUser.setProfilePublicId("");
                new com.core2web.dao.UserDAOImpl().save(currentUser);
                DataRepository.getInstance().setCurrentUser(currentUser);
                com.core2web.util.SessionManager.getInstance().login(currentUser);
                showAlert("Photo Removed", "Profile photo removed.");
                Main.showProfilePage();
            });
            photoBtnRow.getChildren().add(removePhotoBtn);
        }

        avatarCol.getChildren().addAll(avatarBox, photoBtnRow);

        VBox userDetails = new VBox(4);
        HBox.setHgrow(userDetails, Priority.ALWAYS);

        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameText = new Text(currentUser.getName() != null && !currentUser.getName().trim().isEmpty() ? currentUser.getName().trim() : "Not provided");
        nameText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        String roleStr = (currentUser.getRole() != null) ? currentUser.getRole().toString().replace("_", " ") : "STUDENT";
        Label roleBadge = new Label(roleStr);
        roleBadge.setStyle(Theme.badgeStyle());
        nameRow.getChildren().addAll(nameText, roleBadge);

        String emailDisplay = (currentUser.getEmail() != null && !currentUser.getEmail().trim().isEmpty()) ? currentUser.getEmail().trim() : "Not provided";
        String phoneDisplay = (currentUser.getPhone() != null && !currentUser.getPhone().trim().isEmpty()) ? currentUser.getPhone().trim() : "Not provided";

        emailText = new Text("✉ " + emailDisplay + "  •  📞 " + phoneDisplay + "  •  📍 " + userLocation);
        emailText.setStyle(Theme.mutedTextStyle());

        Text collegeSubText = new Text("🎓 " + collegeName);
        collegeSubText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px; -fx-font-weight: 700;");

        userDetails.getChildren().addAll(nameRow, emailText, collegeSubText);

        HBox headerActions = new HBox(10);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        Button editProfileBtn = new Button("Edit Profile");
        editProfileBtn.setPrefHeight(32);
        editProfileBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
        editProfileBtn.setOnAction(e -> showEditProfileDialog());

        Button logoutHeaderBtn = new Button("Log Out");
        logoutHeaderBtn.setPrefHeight(32);
        logoutHeaderBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
        logoutHeaderBtn.setOnAction(e -> handleLogout());

        headerActions.getChildren().addAll(editProfileBtn, logoutHeaderBtn);
        avatarInfoCard.getChildren().addAll(avatarCol, userDetails, headerActions);

        // Responsive 2-Column Layout
        HBox columns = new HBox(16);
        columns.setMaxWidth(Double.MAX_VALUE);

        // --- LEFT COLUMN ---
        VBox leftCol = new VBox(14);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Personal & Academic Info Card
        VBox academicCard = new VBox(10);
        academicCard.setPadding(new Insets(16, 18, 16, 18));
        academicCard.setStyle(Theme.cardStyle());

        Text academicTitle = new Text("Personal & Academic Information");
        academicTitle.setStyle(Theme.sectionHeaderStyle());

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(10);

        infoGrid.add(createGridLabel("College / University:"), 0, 0);
        collegeValLbl = createGridVal(collegeName);
        infoGrid.add(collegeValLbl, 1, 0);

        infoGrid.add(createGridLabel("Course / Branch:"), 0, 1);
        branchValLbl = createGridVal(branchName);
        infoGrid.add(branchValLbl, 1, 1);

        infoGrid.add(createGridLabel("Location / Area:"), 0, 2);
        infoGrid.add(createGridVal(userLocation), 1, 2);

        infoGrid.add(createGridLabel("Mobile Number:"), 0, 3);
        infoGrid.add(createGridVal(phoneDisplay), 1, 3);

        infoGrid.add(createGridLabel("Email Address:"), 0, 4);
        infoGrid.add(createGridVal(emailDisplay), 1, 4);

        academicCard.getChildren().addAll(academicTitle, new Separator(), infoGrid);

        // Account Settings Card
        VBox settingsCard = new VBox(10);
        settingsCard.setPadding(new Insets(16, 18, 16, 18));
        settingsCard.setStyle(Theme.cardStyle());

        Text settingsTitle = new Text("Account Settings");
        settingsTitle.setStyle(Theme.sectionHeaderStyle());

        VBox settingsList = new VBox(0);
        settingsList.getChildren().addAll(
            createSettingRow(IconFactory.PATH_KEY, "Change Password", "Update security password", () -> showAlert("Change Password", "Password reset link sent to " + currentUser.getEmail())),
            createSettingRow(IconFactory.PATH_USER, "Update Email", currentUser.getEmail(), () -> showEditProfileDialog()),
            createSettingRow(IconFactory.PATH_USER, "Update Phone", currentUser.getPhone(), () -> showEditProfileDialog()),
            createSettingRow(IconFactory.PATH_BELL, "Notification Preferences", "Email & Push alerts enabled", () -> showAlert("Notification Preferences", "Push and Email notifications are currently enabled."))
        );

        settingsCard.getChildren().addAll(settingsTitle, new Separator(), settingsList);

        leftCol.getChildren().addAll(academicCard, settingsCard);

        // --- RIGHT COLUMN ---
        VBox rightCol = new VBox(14);
        rightCol.setPrefWidth(380);
        rightCol.setMinWidth(320);

        // Student Activity Stats Card
        VBox activityCard = new VBox(10);
        activityCard.setPadding(new Insets(16, 18, 16, 18));
        activityCard.setStyle(Theme.cardStyle());

        Text activityTitle = new Text("Student Activity");
        activityTitle.setStyle(Theme.sectionHeaderStyle());

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(10);

        int bookingCount = DataRepository.getInstance().getBookings().size();
        int orderCount = DataRepository.getInstance().getOrders().size();
        int savedCount = DataRepository.getInstance().getSavedRoomIds().size() + DataRepository.getInstance().getSavedProductIds().size();

        VBox statBookings = quickStatTile(String.valueOf(bookingCount), "Active Bookings", "#2563EB", () -> Main.showMyBookingsPage());
        VBox statOrders = quickStatTile(String.valueOf(orderCount), "Orders Placed", "#7C3AED", () -> Main.showMyOrdersPage());
        VBox statSaved = quickStatTile(String.valueOf(savedCount), "Saved Items", "#4F772D", () -> Main.showSavedItemsPage());
        VBox statMessages = quickStatTile("0", "Messages", "#D97706", () -> showAlert("Messages", "No unread messages."));

        statsGrid.add(statBookings, 0, 0);
        statsGrid.add(statOrders, 1, 0);
        statsGrid.add(statSaved, 0, 1);
        statsGrid.add(statMessages, 1, 1);

        activityCard.getChildren().addAll(activityTitle, statsGrid);

        User.Role activeRole = (currentUser != null && currentUser.getRole() != null)
                ? currentUser.getRole()
                : com.core2web.util.SessionManager.getInstance().getRole();

        String currentUid = (currentUser != null && currentUser.getUid() != null) ? currentUser.getUid() : "";
        SellerProfile existingSeller = DataRepository.getInstance().getSellerProfile(currentUid);
        boolean isRegisteredSeller = (existingSeller != null) || (activeRole == User.Role.SELLER);

        // Quick Actions Group
        VBox quickActionsGroup = buildMenuGroup("QUICK ACTIONS",
            new String[]{IconFactory.PATH_CALENDAR, IconFactory.PATH_PACKAGE, IconFactory.PATH_HEART_FILLED, IconFactory.PATH_USERS, IconFactory.PATH_SHOPPING_BAG},
            new String[]{"My Bookings", "My Orders", "Saved Items", "My Roommate Profile", isRegisteredSeller ? "Seller Portal" : "Register as Seller"},
            new Runnable[]{
                () -> Main.showMyBookingsPage(),
                () -> Main.showMyOrdersPage(),
                () -> Main.showSavedItemsPage(),
                () -> Main.showRoommateRegistrationPage(),
                () -> {
                    if (isRegisteredSeller) Main.showSellerDashboard();
                    else showRegisterAsSellerDialog(currentUser);
                }
            }
        );

        FlowPane switchBtnsRow = new FlowPane(8, 8);

        if (activeRole == User.Role.OWNER) {
            Button btnOwner = new Button("Owner Portal");
            btnOwner.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11.5px; -fx-padding: 5px 10px;");
            btnOwner.setOnAction(e -> Main.showOwnerDashboard());
            switchBtnsRow.getChildren().add(btnOwner);
        } else if (activeRole == User.Role.SELLER || isRegisteredSeller) {
            Button btnSeller = new Button("Seller Portal");
            btnSeller.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11.5px; -fx-padding: 5px 10px;");
            btnSeller.setOnAction(e -> Main.showSellerDashboard());
            switchBtnsRow.getChildren().add(btnSeller);
        } else if (activeRole == User.Role.SERVICE_PROVIDER) {
            Button btnService = new Button("Provider Portal");
            btnService.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11.5px; -fx-padding: 5px 10px;");
            btnService.setOnAction(e -> Main.showServiceProviderDashboard());
            switchBtnsRow.getChildren().add(btnService);
        } else if (activeRole == User.Role.ADMIN) {
            Button btnAdmin = new Button("Admin Portal");
            btnAdmin.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11.5px; -fx-padding: 5px 10px;");
            btnAdmin.setOnAction(e -> Main.showAdminDashboard());
            switchBtnsRow.getChildren().add(btnAdmin);
        } else {
            // Student can also click Register as Seller button here
            Button btnRegSeller = new Button("🛍️ Register as Seller");
            btnRegSeller.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11.5px; -fx-padding: 5px 10px;");
            btnRegSeller.setOnAction(e -> showRegisterAsSellerDialog(currentUser));
            switchBtnsRow.getChildren().add(btnRegSeller);
        }

        if (!switchBtnsRow.getChildren().isEmpty()) {
            VBox portalSwitchCard = new VBox(10);
            portalSwitchCard.setPadding(new Insets(14, 18, 14, 18));
            portalSwitchCard.setStyle(Theme.cardStyle());

            Text switchTitle = new Text(isRegisteredSeller || activeRole != User.Role.STUDENT ? "Role Portals" : "Seller Registration");
            switchTitle.setStyle(Theme.sectionHeaderStyle());
            portalSwitchCard.getChildren().addAll(switchTitle, switchBtnsRow);

            rightCol.getChildren().addAll(activityCard, quickActionsGroup, portalSwitchCard);
        } else {
            rightCol.getChildren().addAll(activityCard, quickActionsGroup);
        }

        columns.getChildren().addAll(leftCol, rightCol);

        mainContent.getChildren().addAll(avatarInfoCard, columns);

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

    private HBox createSettingRow(String svgPath, String title, String subtitle, Runnable action) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(10, 8, 10, 8));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-cursor: hand; -fx-background-radius: 8px;");

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(32, 32);
        iconBadge.setMinSize(32, 32);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");
        Node iconNode = IconFactory.getIconNode(svgPath, Theme.PRIMARY, 15);
        iconBadge.getChildren().add(iconNode);

        VBox titleBox = new VBox(2);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-font-weight: 700;");
        Text subTxt = new Text(subtitle);
        subTxt.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleTxt, subTxt);

        Text arrowTxt = new Text("›");
        arrowTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 18px; -fx-font-weight: 300;");

        row.getChildren().addAll(iconBadge, titleBox, arrowTxt);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-cursor: hand; -fx-background-radius: 8px;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 8px;"));
        row.setOnMouseClicked(e -> { if (action != null) action.run(); });

        return row;
    }

    private VBox quickStatTile(String value, String label, String accentColor, Runnable action) {
        VBox b = new VBox(4);
        b.setPrefWidth(165);
        b.setPadding(new Insets(12, 14, 12, 14));
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(Theme.statCardStyle(accentColor) + " -fx-cursor: hand;");

        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");
        Text lblTxt = new Text(label);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 600;");
        b.getChildren().addAll(valTxt, lblTxt);

        if (action != null) {
            b.setOnMouseClicked(e -> action.run());
        }
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
            + "-fx-padding: 10px 16px 6px 16px;"
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
        HBox item = new HBox(12);
        item.setPadding(new Insets(10, 16, 10, 16));
        item.setAlignment(Pos.CENTER_LEFT);
        if (!isLast) {
            item.setStyle("-fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        } else {
            item.setStyle("-fx-cursor: hand;");
        }

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(30, 30);
        iconBadge.setMinSize(30, 30);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");
        Node iconNode = IconFactory.getIconNode(svgPath, Theme.PRIMARY, 15);
        iconBadge.getChildren().add(iconNode);

        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-font-weight: 600;");
        HBox.setHgrow(titleTxt, Priority.ALWAYS);

        Text arrowTxt = new Text("›");
        arrowTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 18px; -fx-font-weight: 300;");

        item.getChildren().addAll(iconBadge, titleTxt, arrowTxt);

        item.setOnMouseEntered(e -> item.setStyle(item.getStyle() + "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"));
        item.setOnMouseExited(e -> item.setStyle(isLast ? "-fx-cursor: hand;" : "-fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));

        item.setOnMouseClicked(e -> { if (action != null) action.run(); });
        return item;
    }

    private void showEditProfileDialog() {
        User u = DataRepository.getInstance().getCurrentUser();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile Information");
        dialog.setHeaderText("Update your personal details:");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(400);

        String currentNameVal = (u != null && u.getName() != null && !u.getName().equals("Not provided")) ? u.getName() : "";
        String currentPhoneVal = (u != null && u.getPhone() != null && !u.getPhone().equals("Not provided")) ? u.getPhone() : "";
        String currentEmailVal = (u != null && u.getEmail() != null && !u.getEmail().equals("Not provided")) ? u.getEmail() : "";
        String currentCollegeVal = (collegeName != null && !collegeName.equals("Not provided")) ? collegeName : "";
        String currentBranchVal = (branchName != null && !branchName.equals("Not provided")) ? branchName : "";

        TextField nameField = new TextField(currentNameVal);
        nameField.setPromptText("Enter your full name");
        TextField phoneField = new TextField(currentPhoneVal);
        phoneField.setPromptText("Enter your phone number");
        TextField emailField = new TextField(currentEmailVal);
        emailField.setPromptText("Enter your email address");
        TextField collegeField = new TextField(currentCollegeVal);
        collegeField.setPromptText("Enter your college/university");
        TextField branchField = new TextField(currentBranchVal);
        branchField.setPromptText("Enter your branch/course");

        content.getChildren().addAll(
            new Label("Full Name:"), nameField,
            new Label("Mobile Number:"), phoneField,
            new Label("Email Address:"), emailField,
            new Label("College / University:"), collegeField,
            new Label("Branch / Year:"), branchField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK && u != null) {
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
                }
                if (!branchField.getText().trim().isEmpty()) {
                    branchName = branchField.getText().trim();
                }

                u.setCollege(collegeName);
                u.setBranch(branchName);
                new com.core2web.dao.UserDAOImpl().save(u);
                com.core2web.util.SessionManager.getInstance().login(u);
                DataRepository.getInstance().setCurrentUser(u);

                showAlert("Profile Updated", "Your profile details have been saved successfully!");
                Main.showProfilePage();
            }
        });
    }

    private void showRegisterAsSellerDialog(User u) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register as Student Seller");
        dialog.setHeaderText("Create your Student Seller Profile:");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setPrefWidth(460);

        String currentName = (u != null && u.getName() != null && !u.getName().equals("Not provided")) ? u.getName() : "";
        String currentPhone = (u != null && u.getPhone() != null && !u.getPhone().equals("Not provided")) ? u.getPhone() : "";
        String currentEmail = (u != null && u.getEmail() != null && !u.getEmail().equals("Not provided")) ? u.getEmail() : "";
        String currentCollege = (u != null && u.getCollege() != null && !u.getCollege().equals("Not provided")) ? u.getCollege() : "";

        TextField nameField = new TextField(currentName);
        nameField.setPromptText("Your Seller / Full Name *");
        TextField phoneField = new TextField(currentPhone);
        phoneField.setPromptText("Your Contact Mobile Number *");
        TextField emailField = new TextField(currentEmail);
        emailField.setEditable(false);
        TextField collegeField = new TextField(currentCollege);
        collegeField.setPromptText("Your College / Campus");
        TextField locField = new TextField("Pune");
        locField.setPromptText("Campus Area / City (e.g. Kothrud, Pune)");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Short seller description (e.g. Selling 2nd/3rd year engineering books, stationery, cycle)");
        descArea.setPrefRowCount(3);

        final File[] photoFile = new File[1];
        final Label photoLbl = new Label("No new photo selected");
        photoLbl.setStyle(Theme.mutedTextStyle());

        Button uploadBtn = new Button("📷 Choose Profile Photo");
        uploadBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11.5px; -fx-padding: 4px 10px;");
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(content.getScene().getWindow());
            if (file != null) {
                photoFile[0] = file;
                photoLbl.setText("Selected: " + file.getName());
            }
        });

        HBox photoRow = new HBox(10, uploadBtn, photoLbl);
        photoRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
            new Label("Seller Name *:"), nameField,
            new Label("Mobile Number *:"), phoneField,
            new Label("Email Address:"), emailField,
            new Label("College / University:"), collegeField,
            new Label("Location / Area *:"), locField,
            new Label("Profile Photo:"), photoRow,
            new Label("Seller Description:"), descArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK && u != null) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String loc = locField.getText().trim();
                String col = collegeField.getText().trim();
                String desc = descArea.getText().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    showAlert("Missing Info", "Seller Name and Mobile Phone are required.");
                    return;
                }

                String photoUrl = u.getProfileImage() != null ? u.getProfileImage() : "";
                String publicId = u.getProfilePublicId() != null ? u.getProfilePublicId() : "";

                if (photoFile[0] != null) {
                    try {
                        CloudinaryService.UploadResult res = CloudinaryService.uploadImage(photoFile[0], "profileImages");
                        if (res != null && res.isSuccess()) {
                            photoUrl = res.getSecureUrl();
                            publicId = res.getPublicId();
                        } else {
                            photoUrl = photoFile[0].getAbsolutePath();
                        }
                    } catch (Exception ignored) {
                        photoUrl = photoFile[0].getAbsolutePath();
                    }
                }

                boolean sellerEnabledBefore = u.isSellerEnabled();
                u.setSellerEnabled(true);

                SellerProfile sp = new SellerProfile(
                    u.getUid(),
                    name,
                    u.getEmail(),
                    phone,
                    col,
                    loc,
                    desc,
                    photoUrl,
                    publicId,
                    "ACTIVE",
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                );

                new Thread(() -> new SellerDAOImpl().save(sp)).start();
                DataRepository.getInstance().addOrUpdateSeller(sp);

                u.setName(name);
                u.setPhone(phone);
                u.setCollege(col);
                u.setProfileImage(photoUrl);
                u.setProfilePublicId(publicId);
                u.setSellerEnabled(true);
                new Thread(() -> new com.core2web.dao.UserDAOImpl().save(u)).start();
                com.core2web.util.SessionManager.getInstance().login(u);
                DataRepository.getInstance().setCurrentUser(u);

                System.out.println("========== SELLER REGISTRATION ==========");
                System.out.println("Current Firebase UID = " + u.getUid());
                System.out.println("Current Student role = " + u.getRole());
                System.out.println("Seller enabled before = " + sellerEnabledBefore);
                System.out.println("Seller enabled after = true");
                System.out.println("Seller profile path = sellers/" + u.getUid());
                System.out.println("Seller profile created = true");
                System.out.println("Navigation target = Seller Portal");
                System.out.println("==========================================");

                Alert success = new Alert(Alert.AlertType.CONFIRMATION, "Congratulations! You are now registered as a Student Seller.\nWould you like to open your Seller Portal now?", ButtonType.YES, ButtonType.NO);
                success.setTitle("Registration Successful");
                success.setHeaderText(null);
                success.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.YES) {
                        Main.showSellerDashboard();
                    } else {
                        Main.showProfilePage();
                    }
                });
            }
        });
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be returned to the portal selection screen.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new com.core2web.controller.AuthController().logout();
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
