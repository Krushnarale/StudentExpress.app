package com.core2web.view.authentication;

import com.core2web.Main;
import com.core2web.model.Booking;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.CloudinaryService;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.SessionManager;
import com.core2web.util.Theme;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class AdminDashboard {

    private Scene adminScene;

    public Node getPageNode(Runnable onLogout) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));

        // Top Bar Navigation & Actions
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefSize(36, 36);
        roleBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text roleIcon = new Text("🛡️");
        roleIcon.setStyle("-fx-font-size: 16px;");
        roleBadge.getChildren().add(roleIcon);
        Text logoTxt = new Text("StudentExpress  •  System Admin Console");
        logoTxt.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");
        logoRow.getChildren().addAll(roleBadge, logoTxt);
        HBox.setHgrow(logoRow, Priority.ALWAYS);

        Button messagesBtn = new Button("💬 Messages");
        messagesBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-padding: 7px 14px;");
        messagesBtn.setOnAction(e -> Main.showMessagesPage());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Admin Logout");
            alert.setHeaderText("Logout from System Admin Console?");
            alert.setContentText("You will return to the portal selection screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });

        topBar.getChildren().addAll(logoRow, messagesBtn, logoutBtn);

        User resolvedUser = DataRepository.getInstance().getCurrentUser();
        if (resolvedUser == null) resolvedUser = SessionManager.getInstance().getCurrentUser();
        if (resolvedUser == null) resolvedUser = new User("", "Not provided", "Not provided", "Not provided", User.Role.ADMIN);
        final User currentUser = resolvedUser;

        // Heading
        VBox headingBox = new VBox(4);
        Text heading = new Text("System Administration Console");
        heading.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");
        Text sub = new Text("Platform metrics, user activity, bookings moderation & system overview");
        sub.setStyle(Theme.mutedTextStyle());
        headingBox.getChildren().addAll(heading, sub);

        // Admin Profile Card
        VBox profileCard = createAdminProfileCard(currentUser, () -> {
            Main.showAdminDashboard();
        });

        // Stats Row
        HBox statsBox = new HBox(16);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_USERS, "Total Users", "2,840", Theme.PRIMARY, "+12% this week"),
            createStatCard(IconFactory.PATH_KEY, "Active Rooms", String.valueOf(DataRepository.getInstance().getRooms().size()), "#2563EB", "+5 new today"),
            createStatCard(IconFactory.PATH_SHOPPING_BAG, "Marketplace Products", String.valueOf(DataRepository.getInstance().getProducts().size()), "#D97706", "38 sold this month"),
            createStatCard(IconFactory.PATH_WRENCH, "Services Listed", String.valueOf(DataRepository.getInstance().getServices().size()), "#7C3AED", "98% satisfaction")
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
        Label totalBadge = new Label(DataRepository.getInstance().getBookings().size() + " total events");
        totalBadge.setStyle(Theme.badgeStyle());
        bookHeaderRow.getChildren().addAll(bookTitle, totalBadge);

        VBox bookingsList = new VBox(10);
        for (Booking b : DataRepository.getInstance().getBookings()) {
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
        mainContent.getChildren().addAll(topBar, headingBox, profileCard, statsBox, bookingsCard);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private VBox createAdminProfileCard(User admin, Runnable onRefresh) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(Theme.cardStyle());

        HBox mainRow = new HBox(18);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        VBox avatarCol = new VBox(6);
        avatarCol.setAlignment(Pos.CENTER);

        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(68, 68);
        avatarPane.setMinSize(68, 68);
        avatarPane.setMaxSize(68, 68);
        avatarPane.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 34px;");

        Image avatarImg = null;
        if (admin.getProfileImage() != null && !admin.getProfileImage().trim().isEmpty()) {
            avatarImg = ImageUtil.loadImage(admin.getProfileImage());
        }

        if (avatarImg != null && !avatarImg.isError()) {
            ImageView imgView = new ImageView(avatarImg);
            imgView.setFitWidth(68);
            imgView.setFitHeight(68);
            imgView.setPreserveRatio(false);
            Circle clip = new Circle(34, 34, 34);
            imgView.setClip(clip);
            avatarPane.getChildren().add(imgView);
        } else {
            String initial = (admin.getName() != null && !admin.getName().isEmpty())
                ? admin.getName().substring(0, 1).toUpperCase() : "A";
            Text initText = new Text(initial);
            initText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");
            avatarPane.getChildren().add(initText);
        }

        HBox photoBtnRow = new HBox(4);
        photoBtnRow.setAlignment(Pos.CENTER);

        Button changePhotoBtn = new Button("📷 " + (admin.getProfileImage() != null && !admin.getProfileImage().isEmpty() ? "Change" : "Upload"));
        changePhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 9.5px; -fx-padding: 2px 6px;");
        changePhotoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                try {
                    CloudinaryService.UploadResult res = CloudinaryService.uploadImage(file, "profileImages");
                    String imgUrl = (res != null && res.isSuccess()) ? res.getSecureUrl() : file.getAbsolutePath();
                    admin.setProfileImage(imgUrl);
                    if (res != null && res.isSuccess()) {
                        admin.setProfilePublicId(res.getPublicId());
                    }
                    new com.core2web.dao.UserDAOImpl().save(admin);
                    DataRepository.getInstance().setCurrentUser(admin);
                    SessionManager.getInstance().login(admin);
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Admin profile photo updated successfully!");
                    a.showAndWait();
                    if (onRefresh != null) onRefresh.run();
                } catch (Exception ex) {
                    admin.setProfileImage(file.getAbsolutePath());
                    new com.core2web.dao.UserDAOImpl().save(admin);
                    DataRepository.getInstance().setCurrentUser(admin);
                    SessionManager.getInstance().login(admin);
                    if (onRefresh != null) onRefresh.run();
                }
            }
        });

        photoBtnRow.getChildren().add(changePhotoBtn);

        if (admin.getProfileImage() != null && !admin.getProfileImage().isEmpty()) {
            Button removePhotoBtn = new Button("✕");
            removePhotoBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 9.5px; -fx-padding: 2px 5px;");
            removePhotoBtn.setOnAction(e -> {
                if (admin.getProfilePublicId() != null && !admin.getProfilePublicId().isEmpty()) {
                    CloudinaryService.deleteImage(admin.getProfilePublicId());
                }
                admin.setProfileImage("");
                admin.setProfilePublicId("");
                new com.core2web.dao.UserDAOImpl().save(admin);
                DataRepository.getInstance().setCurrentUser(admin);
                SessionManager.getInstance().login(admin);
                if (onRefresh != null) onRefresh.run();
            });
            photoBtnRow.getChildren().add(removePhotoBtn);
        }

        avatarCol.getChildren().addAll(avatarPane, photoBtnRow);

        VBox infoCol = new VBox(4);
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Text nameText = new Text(admin.getName() != null && !admin.getName().trim().isEmpty() && !admin.getName().equals("Not provided") ? admin.getName().trim() : "Not provided");
        nameText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 17px; -fx-font-weight: 800;");
        Label roleBadge = new Label("🛡️ ADMIN CONSOLE");
        roleBadge.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );
        titleRow.getChildren().addAll(nameText, roleBadge);

        HBox contactRow = new HBox(18);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        String emailDisplay = (admin.getEmail() != null && !admin.getEmail().trim().isEmpty() && !admin.getEmail().equals("Not provided")) ? admin.getEmail().trim() : "Not provided";
        String phoneDisplay = (admin.getPhone() != null && !admin.getPhone().trim().isEmpty() && !admin.getPhone().equals("Not provided")) ? admin.getPhone().trim() : "Not provided";

        Text emailTxt = new Text("✉ " + emailDisplay);
        emailTxt.setStyle(Theme.mutedTextStyle());
        Text phoneTxt = new Text("📞 " + phoneDisplay);
        phoneTxt.setStyle(Theme.mutedTextStyle());

        contactRow.getChildren().addAll(emailTxt, phoneTxt);
        infoCol.getChildren().addAll(titleRow, contactRow);

        mainRow.getChildren().addAll(avatarCol, infoCol);
        card.getChildren().add(mainRow);
        return card;
    }

    public Scene getPageScene(Runnable onLogout) {
        Node node = getPageNode(onLogout);
        BorderPane rootPane = new BorderPane(node);
        rootPane.setStyle(Theme.rootPaneStyle());

        // Top Bar (dark green gradient)
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

        Button messagesBtn = new Button("💬 Messages");
        messagesBtn.setStyle(
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
        messagesBtn.setOnAction(e -> Main.showMessagesPage());

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
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Admin Logout");
            alert.setHeaderText("Logout from System Admin Console?");
            alert.setContentText("You will return to the portal selection screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });

        topBar.getChildren().addAll(logoRow, messagesBtn, logoutBtn);
        rootPane.setTop(topBar);

        if (adminScene == null) {
            adminScene = new Scene(rootPane, 1050, 700);
        }
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
