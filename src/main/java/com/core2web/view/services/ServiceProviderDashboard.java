package com.core2web.view.services;

import com.core2web.Main;
import com.core2web.dao.ServiceDAOImpl;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.Booking;
import com.core2web.model.ServiceItem;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.CloudinaryService;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.SessionManager;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class ServiceProviderDashboard {

    private Scene serviceProviderScene;
    private String activeCategory = "All Services";

    public Node getPageNode(Runnable onLogout) {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle(Theme.rootPaneStyle());

        User resolvedUser = DataRepository.getInstance().getCurrentUser();
        if (resolvedUser == null) resolvedUser = SessionManager.getInstance().getCurrentUser();
        if (resolvedUser == null) resolvedUser = new User("", "Not provided", "Not provided", "Not provided", User.Role.SERVICE_PROVIDER);
        final User currentUser = resolvedUser;

        // Top Bar (Without Back to App button)
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 30, 16, 30));
        topBar.setStyle(Theme.topBarStyle());

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefSize(36, 36);
        roleBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text roleIcon = new Text("🛠️");
        roleIcon.setStyle("-fx-font-size: 16px;");
        roleBadge.getChildren().add(roleIcon);
        Text logoTxt = new Text("StudentExpress  •  Service Provider Workspace");
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
            alert.setTitle("Provider Logout");
            alert.setHeaderText("Logout from Service Provider Workspace?");
            alert.setContentText("You will return to the portal selection screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });

        topBar.getChildren().addAll(logoRow, messagesBtn, logoutBtn);
        rootPane.setTop(topBar);

        // Main Content
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));

        // Heading + Primary Action
        HBox headingBox = new HBox(16);
        headingBox.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Text titleTxt = new Text("Service Provider Hub & Management");
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");
        Text subTxt = new Text("Offer Laundry, Tiffin / Mess, Room Cleaning, Wi-Fi Setup & Maintenance to campus students");
        subTxt.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleTxt, subTxt);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button addServiceBtn = new Button("Add New Service Listing");
        addServiceBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_PLUS, "white", 14));
        addServiceBtn.setStyle(Theme.primaryBtnStyle());
        headingBox.getChildren().addAll(titleBox, addServiceBtn);

        final Runnable[] refreshServices = new Runnable[1];

        // Provider Profile Card
        VBox profileCard = createProviderProfileCard(currentUser, () -> {
            if (refreshServices[0] != null) refreshServices[0].run();
        });

        // Stats Row (4 Cards)
        List<Booking> fsInitialReqs = new com.core2web.dao.BookingDAOImpl().findByProviderUid(currentUser.getUid());
        if (fsInitialReqs.isEmpty() && currentUser.getName() != null && !currentUser.getName().isEmpty() && !currentUser.getName().equals("Not provided")) {
            fsInitialReqs = new com.core2web.dao.BookingDAOImpl().findByProviderId(currentUser.getName());
        }
        long pendingReqCount = fsInitialReqs.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count();

        HBox statsBox = new HBox(16);
        List<ServiceItem> myInitialServices = getFilteredServices("All Services", currentUser);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_WRENCH, String.valueOf(myInitialServices.size()), "My Active Services", Theme.PRIMARY),
            createStatCard(IconFactory.PATH_BELL, String.valueOf(pendingReqCount), "Pending Requests", "#D97706"),
            createStatCard(IconFactory.PATH_CALENDAR, String.valueOf(fsInitialReqs.stream().filter(b -> "ACCEPTED".equalsIgnoreCase(b.getStatus())).count()), "Bookings Accepted", "#2563EB"),
            createStatCard(IconFactory.PATH_STAR, "4.9", "Average Rating", "#10B981")
        );

        // Section 1: Incoming Service Requests Management
        VBox requestsSection = new VBox(14);
        HBox reqHeader = new HBox(12);
        reqHeader.setAlignment(Pos.CENTER_LEFT);
        Text reqTitle = new Text("Incoming Student Service Requests");
        reqTitle.setStyle(Theme.sectionHeaderStyle());
        reqHeader.getChildren().add(reqTitle);

        VBox requestsContainer = new VBox(12);

        Runnable refreshRequests = () -> {
            requestsContainer.getChildren().clear();
            List<Booking> fsRequests = new com.core2web.dao.BookingDAOImpl().findByProviderUid(currentUser.getUid());
            if (fsRequests.isEmpty() && currentUser.getName() != null && !currentUser.getName().isEmpty() && !currentUser.getName().equals("Not provided")) {
                fsRequests = new com.core2web.dao.BookingDAOImpl().findByProviderId(currentUser.getName());
            }
            List<Booking> memoryRequests = new ArrayList<>();
            for (Booking b : DataRepository.getInstance().getBookings()) {
                if (currentUser.getUid().equalsIgnoreCase(b.getProviderUid()) || currentUser.getName().equalsIgnoreCase(b.getProviderUid())) {
                    memoryRequests.add(b);
                }
            }
            List<Booking> reqList = new ArrayList<>(fsRequests);
            for (Booking mb : memoryRequests) {
                if (reqList.stream().noneMatch(b -> b.getId().equals(mb.getId()))) {
                    reqList.add(mb);
                }
            }

            if (reqList.isEmpty()) {
                VBox emptyReq = new VBox(10);
                emptyReq.setAlignment(Pos.CENTER);
                emptyReq.setPadding(new Insets(20));
                emptyReq.setStyle(Theme.cardStyle());
                Text emptyTxt = new Text("No incoming service requests received yet.");
                emptyTxt.setStyle(Theme.mutedTextStyle());
                emptyReq.getChildren().add(emptyTxt);
                requestsContainer.getChildren().add(emptyReq);
            } else {
                for (Booking req : reqList) {
                    requestsContainer.getChildren().add(createServiceRequestCard(req, refreshServices[0]));
                }
            }
        };

        requestsSection.getChildren().addAll(reqHeader, requestsContainer);

        // Section 2: Offered Services Management
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("My Offered Student Services");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        sectionHeader.getChildren().add(secTitle);

        HBox categoryPills = new HBox(10);
        String[] serviceCats = {"All Services", "🧺 Laundry", "🍱 Tiffin / Mess", "🧹 Cleaning", "📶 Wi-Fi", "🛠️ Repair & Maintenance"};

        VBox servicesListContainer = new VBox(16);

        refreshServices[0] = () -> {
            refreshRequests.run();

            servicesListContainer.getChildren().clear();
            List<ServiceItem> filtered = getFilteredServices(activeCategory, currentUser);

            if (filtered.isEmpty()) {
                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                empty.setStyle(Theme.cardStyle());
                Text icon = new Text("🛠️");
                icon.setStyle("-fx-font-size: 40px;");
                Text emptyTxt = new Text("No services offered in this category yet.");
                emptyTxt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().addAll(icon, emptyTxt);
                servicesListContainer.getChildren().add(empty);
            } else {
                for (ServiceItem s : filtered) {
                    HBox card = createServiceCard(s, currentUser, () -> {
                        DataRepository.getInstance().removeService(s.getId());
                        new ServiceDAOImpl().delete(s.getId());
                        showAlert("Deleted", "'" + s.getTitle() + "' removed from your service catalog.");
                        if (refreshServices[0] != null) refreshServices[0].run();
                    });
                    servicesListContainer.getChildren().add(card);
                }
            }

            for (javafx.scene.Node n : categoryPills.getChildren()) {
                if (n instanceof Button) {
                    Button b = (Button) n;
                    b.setStyle(Theme.filterPillStyle(b.getText().contains(activeCategory) || (activeCategory.equals("All Services") && b.getText().equals("All Services"))));
                }
            }
        };

        for (String cat : serviceCats) {
            Button pill = new Button(cat);
            pill.setStyle(Theme.filterPillStyle(cat.equals("All Services")));
            pill.setOnAction(e -> {
                if (cat.contains("Laundry")) activeCategory = "Laundry";
                else if (cat.contains("Tiffin") || cat.contains("Mess")) activeCategory = "Tiffin / Mess";
                else if (cat.contains("Cleaning")) activeCategory = "Cleaning";
                else if (cat.contains("Wi-Fi")) activeCategory = "Wi-Fi";
                else if (cat.contains("Repair") || cat.contains("Maintenance")) activeCategory = "Repair & Maintenance";
                else activeCategory = "All Services";
                refreshServices[0].run();
            });
            categoryPills.getChildren().add(pill);
        }

        addServiceBtn.setOnAction(e -> showAddServiceDialog(currentUser, refreshServices[0]));
        refreshServices[0].run();

        mainContent.getChildren().addAll(headingBox, profileCard, statsBox, requestsSection, sectionHeader, categoryPills, servicesListContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        rootPane.setCenter(scrollPane);
        return rootPane;
    }

    private VBox createProviderProfileCard(User provider, Runnable onRefresh) {
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
        if (provider.getProfileImage() != null && !provider.getProfileImage().trim().isEmpty()) {
            avatarImg = ImageUtil.loadImage(provider.getProfileImage());
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
            String initial = (provider.getName() != null && !provider.getName().isEmpty())
                ? provider.getName().substring(0, 1).toUpperCase() : "P";
            Text initText = new Text(initial);
            initText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");
            avatarPane.getChildren().add(initText);
        }

        HBox photoBtnRow = new HBox(4);
        photoBtnRow.setAlignment(Pos.CENTER);

        Button changePhotoBtn = new Button("📷 " + (provider.getProfileImage() != null && !provider.getProfileImage().isEmpty() ? "Change" : "Upload"));
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
                    provider.setProfileImage(imgUrl);
                    if (res != null && res.isSuccess()) {
                        provider.setProfilePublicId(res.getPublicId());
                    }
                    new UserDAOImpl().save(provider);
                    DataRepository.getInstance().setCurrentUser(provider);
                    SessionManager.getInstance().login(provider);
                    showAlert("Photo Updated", "Your service provider profile photo has been updated!");
                    if (onRefresh != null) onRefresh.run();
                } catch (Exception ex) {
                    provider.setProfileImage(file.getAbsolutePath());
                    new UserDAOImpl().save(provider);
                    DataRepository.getInstance().setCurrentUser(provider);
                    SessionManager.getInstance().login(provider);
                    if (onRefresh != null) onRefresh.run();
                }
            }
        });

        photoBtnRow.getChildren().add(changePhotoBtn);

        if (provider.getProfileImage() != null && !provider.getProfileImage().isEmpty()) {
            Button removePhotoBtn = new Button("✕");
            removePhotoBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 9.5px; -fx-padding: 2px 5px;");
            removePhotoBtn.setOnAction(e -> {
                if (provider.getProfilePublicId() != null && !provider.getProfilePublicId().isEmpty()) {
                    CloudinaryService.deleteImage(provider.getProfilePublicId());
                }
                provider.setProfileImage("");
                provider.setProfilePublicId("");
                new UserDAOImpl().save(provider);
                DataRepository.getInstance().setCurrentUser(provider);
                SessionManager.getInstance().login(provider);
                showAlert("Photo Removed", "Profile photo removed.");
                if (onRefresh != null) onRefresh.run();
            });
            photoBtnRow.getChildren().add(removePhotoBtn);
        }

        avatarCol.getChildren().addAll(avatarPane, photoBtnRow);

        VBox infoCol = new VBox(4);
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Text nameText = new Text(provider.getName() != null && !provider.getName().trim().isEmpty() && !provider.getName().equals("Not provided") ? provider.getName().trim() : "Not provided");
        nameText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 17px; -fx-font-weight: 800;");

        Label verifiedBadge = new Label("✓ Verified Provider");
        verifiedBadge.setStyle(Theme.successBadgeStyle());

        Label roleBadge = new Label("🛠️ PROVIDER PORTAL");
        roleBadge.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );
        titleRow.getChildren().addAll(nameText, verifiedBadge, roleBadge);

        HBox contactRow = new HBox(18);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        String emailDisplay = (provider.getEmail() != null && !provider.getEmail().trim().isEmpty() && !provider.getEmail().equals("Not provided")) ? provider.getEmail().trim() : "Not provided";
        String phoneDisplay = (provider.getPhone() != null && !provider.getPhone().trim().isEmpty() && !provider.getPhone().equals("Not provided")) ? provider.getPhone().trim() : "Not provided";
        String locationStr = (provider.getCollege() != null && !provider.getCollege().trim().isEmpty() && !provider.getCollege().equals("Not provided")) ? provider.getCollege().trim() : "Not provided";

        Text emailTxt = new Text("✉ " + emailDisplay);
        emailTxt.setStyle(Theme.mutedTextStyle());
        Text phoneTxt = new Text("📞 " + phoneDisplay);
        phoneTxt.setStyle(Theme.mutedTextStyle());
        Text locTxt = new Text("📍 " + locationStr);
        locTxt.setStyle(Theme.mutedTextStyle());

        contactRow.getChildren().addAll(emailTxt, phoneTxt, locTxt);
        infoCol.getChildren().addAll(titleRow, contactRow);

        Button editBtn = new Button("✏ Edit Provider Details");
        editBtn.setStyle(Theme.secondaryBtnStyle());
        editBtn.setOnAction(e -> showEditProviderDialog(provider, onRefresh));

        mainRow.getChildren().addAll(avatarCol, infoCol, editBtn);
        card.getChildren().add(mainRow);
        return card;
    }

    private void showEditProviderDialog(User provider, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Service Provider Details");
        dialog.setHeaderText("Update your provider profile and service parameters:");

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setPrefWidth(400);

        String currentName = (provider.getName() != null && !provider.getName().equals("Not provided")) ? provider.getName() : "";
        String currentPhone = (provider.getPhone() != null && !provider.getPhone().equals("Not provided")) ? provider.getPhone() : "";
        String currentLoc = (provider.getCollege() != null && !provider.getCollege().equals("Not provided")) ? provider.getCollege() : "";

        TextField nameField = new TextField(currentName);
        nameField.setPromptText("Enter provider name");
        TextField phoneField = new TextField(currentPhone);
        phoneField.setPromptText("Enter phone number");
        TextField locField = new TextField(currentLoc);
        locField.setPromptText("Enter service area / location");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Provider Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Phone Number:"), 0, 1);
        grid.add(phoneField, 1, 1);

        grid.add(new Label("Service Area / Location:"), 0, 2);
        grid.add(locField, 1, 2);

        content.getChildren().add(grid);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                provider.setName(nameField.getText().trim());
                provider.setPhone(phoneField.getText().trim());
                provider.setCollege(locField.getText().trim());
                new UserDAOImpl().save(provider);
                DataRepository.getInstance().setCurrentUser(provider);
                SessionManager.getInstance().login(provider);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(ok -> {
            if (ok) {
                showAlert("Details Saved", "Your provider profile has been updated successfully.");
                if (onRefresh != null) onRefresh.run();
            }
        });
    }

    private VBox createServiceRequestCard(Booking b, Runnable onRefresh) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16, 20, 16, 20));

        String status = b.getStatus() != null ? b.getStatus() : "PENDING";
        String accentColor = "PENDING".equalsIgnoreCase(status) ? "#D97706"
            : "ACCEPTED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status) ? Theme.PRIMARY
            : "REJECTED".equalsIgnoreCase(status) ? "#C62828" : "#6B7280";

        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-border-color: " + accentColor + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + ";"
            + "-fx-border-width: 3px 1px 1px 1px;"
            + "-fx-border-radius: 10px;"
            + "-fx-background-radius: 10px;"
        );

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox studInfo = new VBox(2);
        HBox.setHgrow(studInfo, Priority.ALWAYS);

        String emailDisplay = (b.getUserEmail() != null && !b.getUserEmail().isEmpty()) ? b.getUserEmail() : "Student";
        Text studentTitle = new Text("👤 Student: " + emailDisplay + " (ID: " + b.getUserUid() + ")");
        studentTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 800;");

        Text itemLine = new Text("Service: " + b.getItemOrServiceName() + "  •  Category: " + b.getCategory());
        itemLine.setStyle(Theme.mutedTextStyle());

        studInfo.getChildren().addAll(studentTitle, itemLine);

        Label statusBadge = new Label(status);
        statusBadge.setStyle("PENDING".equalsIgnoreCase(status) ? Theme.warningBadgeStyle() : ("ACCEPTED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status) ? Theme.successBadgeStyle() : Theme.dangerBadgeStyle()));

        topRow.getChildren().addAll(studInfo, statusBadge);

        HBox detailsRow = new HBox(30);
        detailsRow.setPadding(new Insets(8, 12, 8, 12));
        detailsRow.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 6px;");

        detailsRow.getChildren().addAll(
            detailItem("Preferred Date", b.getBookingDate()),
            detailItem("Time Slot", b.getTimeSlot() != null && !b.getTimeSlot().isEmpty() ? b.getTimeSlot() : "Flexible"),
            detailItem("Service Location", b.getAddress() != null && !b.getAddress().isEmpty() ? b.getAddress() : "Campus Area")
        );

        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Button chatStudentBtn = new Button("💬 Message Student");
        chatStudentBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
        chatStudentBtn.setOnAction(ev -> {
            String studentUid = (b.getUserUid() != null && !b.getUserUid().isEmpty()) ? b.getUserUid() : "student_" + Math.abs(emailDisplay.hashCode());
            Main.showChatWithUser(studentUid, emailDisplay, "STUDENT", b.getItemId(), "SERVICE", b.getItemOrServiceName());
        });
        actionsRow.getChildren().add(chatStudentBtn);

        if ("PENDING".equalsIgnoreCase(status)) {
            Button acceptBtn = new Button("✓ Accept Request");
            acceptBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            acceptBtn.setOnAction(ev -> {
                DataRepository.getInstance().updateBookingStatus(b.getId(), "ACCEPTED");
                showAlert("Request Accepted", "Service booking from " + emailDisplay + " has been ACCEPTED.");
                if (onRefresh != null) onRefresh.run();
            });

            Button rejectBtn = new Button("✕ Reject Request");
            rejectBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            rejectBtn.setOnAction(ev -> {
                DataRepository.getInstance().updateBookingStatus(b.getId(), "REJECTED");
                showAlert("Request Rejected", "Service booking from " + emailDisplay + " has been REJECTED.");
                if (onRefresh != null) onRefresh.run();
            });

            actionsRow.getChildren().addAll(acceptBtn, rejectBtn);
        } else {
            Label doneLbl = new Label("Status: " + status);
            doneLbl.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: 600;");
            actionsRow.getChildren().add(doneLbl);
        }

        card.getChildren().addAll(topRow, detailsRow, actionsRow);
        return card;
    }

    private VBox detailItem(String label, String value) {
        VBox b = new VBox(2);
        Text labelTxt = new Text(label);
        labelTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        Text valueTxt = new Text(value != null ? value : "N/A");
        valueTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        b.getChildren().addAll(labelTxt, valueTxt);
        return b;
    }

    public Scene getPageScene(Runnable onLogout) {
        if (serviceProviderScene == null) {
            serviceProviderScene = new Scene((BorderPane) getPageNode(onLogout), 1050, 700);
        }
        return serviceProviderScene;
    }

    private boolean matchesServiceCategory(ServiceItem s, String catFilter) {
        if (catFilter == null || catFilter.isEmpty() || catFilter.equalsIgnoreCase("All Services") || catFilter.equalsIgnoreCase("All")) {
            return true;
        }
        return ServicesPage.matchesCategory(s, catFilter);
    }

    private List<ServiceItem> getFilteredServices(String catFilter, User currentUser) {
        List<ServiceItem> result = new ArrayList<>();
        String currentUid = currentUser != null ? currentUser.getUid() : "";
        String currentName = currentUser != null ? currentUser.getName() : "";
        String currentEmail = currentUser != null ? currentUser.getEmail() : "";

        for (ServiceItem s : DataRepository.getInstance().getServices()) {
            boolean isMine = (s.getProviderUid() != null && !s.getProviderUid().isEmpty() && (s.getProviderUid().equalsIgnoreCase(currentUid) || s.getProviderUid().equalsIgnoreCase(currentEmail)))
                || (s.getProviderName() != null && !s.getProviderName().isEmpty() && !currentName.isEmpty() && !currentName.equals("Not provided") && s.getProviderName().equalsIgnoreCase(currentName));

            if (isMine) {
                if (matchesServiceCategory(s, catFilter)) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    private HBox createServiceCard(ServiceItem s, User currentUser, Runnable onDelete) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(Theme.cardStyle());

        // Thumbnail / Icon Box
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(70, 52);
        imgBox.setMinSize(70, 52);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");

        Image simg = null;
        if (s.getImagePath() != null && !s.getImagePath().isEmpty() && !s.getImagePath().startsWith("assets/image/placeholder") && s.getImagePath().length() > 4) {
            simg = ImageUtil.loadImage(s.getImagePath());
        }

        if (simg != null && !simg.isError()) {
            ImageView iv = new ImageView(simg);
            iv.setFitWidth(70);
            iv.setFitHeight(52);
            iv.setPreserveRatio(false);
            Rectangle clip = new Rectangle(70, 52);
            clip.setArcWidth(10); clip.setArcHeight(10);
            iv.setClip(clip);
            imgBox.getChildren().add(iv);
        } else {
            Text iconTxt = new Text(s.getIcon() != null && !s.getIcon().isEmpty() ? s.getIcon() : "🛠️");
            iconTxt.setStyle("-fx-font-size: 24px;");
            imgBox.getChildren().add(iconTxt);
        }

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text titleText = new Text(s.getTitle() + "   •   " + s.getPrice());
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(s.getCategory() != null ? s.getCategory() : "Service");
        catBadge.setStyle(
            "-fx-background-color: #4F772D1E;"
            + "-fx-text-fill: #4F772D;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );

        Text subText = new Text(s.getSubtitle() != null ? s.getSubtitle() : "Campus Student Service");
        subText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, subText);
        info.getChildren().addAll(titleText, metaRow);

        Label statusLbl = new Label("Active Service");
        statusLbl.setStyle(Theme.successBadgeStyle());

        Button editBtn = new Button("Edit");
        editBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_EDIT, Theme.PRIMARY, 14));
        editBtn.setStyle(Theme.secondaryBtnStyle());
        editBtn.setOnAction(ev -> showAlert("Edit Service", "Edit details for: " + s.getTitle()));

        Button delBtn = new Button("Delete");
        delBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_TRASH, "#C62828", 14));
        delBtn.setStyle(Theme.dangerBtnStyle());
        delBtn.setOnAction(ev -> {
            boolean isMine = (s.getProviderUid() != null && (s.getProviderUid().equalsIgnoreCase(currentUser.getUid()) || s.getProviderUid().equalsIgnoreCase(currentUser.getEmail())))
                || (s.getProviderName() != null && s.getProviderName().equalsIgnoreCase(currentUser.getName()));
            if (!isMine && (s.getProviderUid() == null || s.getProviderUid().isEmpty())) {
                isMine = true;
            }
            if (!isMine) {
                showAlert("Access Denied", "You can only delete your own service listings.");
                return;
            }
            onDelete.run();
        });

        card.getChildren().addAll(imgBox, info, statusLbl, editBtn, delBtn);
        return card;
    }

    private VBox createStatCard(String iconPath, String value, String title, String accentColor) {
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

    // Comprehensive Post Service Dialog with Photo Upload, Preview & ScrollPane
    private void showAddServiceDialog(User currentUser, Runnable onAdded) {
        Dialog<ServiceItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Campus Service Offering");
        dialog.setHeaderText("Specify full service offering parameters and upload photo:");

        VBox content = new VBox(14);
        content.setPadding(new Insets(18));
        content.setPrefWidth(520);

        TextField titleField = new TextField();
        titleField.setPromptText("Service Title (e.g. Express Laundry & Ironing / Mess Tiffin Service)");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Laundry", "Tiffin / Mess", "Cleaning", "Wi-Fi", "Repair & Maintenance");
        catBox.setValue("Laundry");
        catBox.setMaxWidth(Double.MAX_VALUE);

        TextField priceField = new TextField();
        priceField.setPromptText("Price / Rate (e.g. ₹ 499 / month or ₹ 150 / session)");

        TextField subField = new TextField();
        subField.setPromptText("Short Subtitle / Tagline (e.g. Wash, Dry & Fold / Pure Veg Meals)");

        TextField locField = new TextField();
        locField.setPromptText("Service Area / Location (e.g. Area, City)");
        locField.setText(currentUser.getCollege() != null && !currentUser.getCollege().isEmpty() && !currentUser.getCollege().equals("Not provided") ? currentUser.getCollege() : "");

        TextField hoursField = new TextField();
        hoursField.setPromptText("Working Hours / Availability (e.g. 8:00 AM - 9:00 PM)");

        TextField durationField = new TextField();
        durationField.setPromptText("Turnaround Time / Duration (e.g. 24 Hours / Instant)");

        TextField providerField = new TextField();
        providerField.setPromptText("Provider Name");
        providerField.setText(currentUser.getName() != null && !currentUser.getName().isEmpty() && !currentUser.getName().equals("Not provided") ? currentUser.getName() : "");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Contact Phone Number");
        phoneField.setText(currentUser.getPhone() != null && !currentUser.getPhone().isEmpty() && !currentUser.getPhone().equals("Not provided") ? currentUser.getPhone() : "");

        TextField descField = new TextField();
        descField.setPromptText("Full Description, Features & Inclusions");

        // Photo Upload Section
        VBox photoSection = new VBox(8);
        photoSection.setPadding(new Insets(12));
        photoSection.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 8px; -fx-border-style: dashed; -fx-border-width: 1.5px;");

        HBox photoHeaderRow = new HBox(10);
        photoHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Text photoTitle = new Text("Service Photo / Banner");
        photoTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Region phSpacer = new Region();
        HBox.setHgrow(phSpacer, Priority.ALWAYS);

        Button choosePhotoBtn = new Button("📷 Choose Photo");
        choosePhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 4px 10px; -fx-font-size: 11px;");
        photoHeaderRow.getChildren().addAll(photoTitle, phSpacer, choosePhotoBtn);

        StackPane photoPreviewBox = new StackPane();
        photoPreviewBox.setPrefSize(100, 70);
        photoPreviewBox.setMinSize(100, 70);
        photoPreviewBox.setMaxSize(100, 70);
        photoPreviewBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");

        Text phPlaceholder = new Text("No photo");
        phPlaceholder.setStyle(Theme.mutedTextStyle());
        photoPreviewBox.getChildren().add(phPlaceholder);

        final File[] selectedPhotoFile = {null};
        final String[] uploadedPhotoUrl = {""};
        final String[] uploadedPhotoPublicId = {""};

        HBox previewRow = new HBox(12);
        previewRow.setAlignment(Pos.CENTER_LEFT);
        Label photoStatusLbl = new Label("Default category icon will be used if no photo uploaded.");
        photoStatusLbl.setStyle(Theme.mutedTextStyle());
        previewRow.getChildren().addAll(photoPreviewBox, photoStatusLbl);

        choosePhotoBtn.setOnAction(ev -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Service Photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                selectedPhotoFile[0] = file;
                try {
                    Image pimg = new Image(file.toURI().toString());
                    ImageView iv = new ImageView(pimg);
                    iv.setFitWidth(100);
                    iv.setFitHeight(70);
                    iv.setPreserveRatio(false);
                    Rectangle clip = new Rectangle(100, 70);
                    clip.setArcWidth(8); clip.setArcHeight(8);
                    iv.setClip(clip);
                    photoPreviewBox.getChildren().clear();
                    photoPreviewBox.getChildren().add(iv);
                    photoStatusLbl.setText("Selected: " + file.getName());
                } catch (Exception ignored) {}
            }
        });

        photoSection.getChildren().addAll(photoHeaderRow, previewRow);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.add(new Label("Service Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);

        grid.add(new Label("Price / Rate:"), 0, 2);
        grid.add(priceField, 1, 2);

        grid.add(new Label("Tagline / Subtitle:"), 0, 3);
        grid.add(subField, 1, 3);

        grid.add(new Label("Service Area:"), 0, 4);
        grid.add(locField, 1, 4);

        grid.add(new Label("Working Hours:"), 0, 5);
        grid.add(hoursField, 1, 5);

        grid.add(new Label("Turnaround Time:"), 0, 6);
        grid.add(durationField, 1, 6);

        grid.add(new Label("Provider Name:"), 0, 7);
        grid.add(providerField, 1, 7);

        grid.add(new Label("Contact Phone:"), 0, 8);
        grid.add(phoneField, 1, 8);

        grid.add(new Label("Full Description:"), 0, 9);
        grid.add(descField, 1, 9);

        content.getChildren().addAll(grid, photoSection);

        // Wrap inside ScrollPane for full responsive accessibility
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(500);
        scrollPane.setMaxHeight(550);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.CARD_BG + ";");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !titleField.getText().trim().isEmpty()) {
                String icon = "🧺";
                String selectedCat = catBox.getValue();
                if ("Tiffin / Mess".equals(selectedCat) || (selectedCat != null && (selectedCat.contains("Tiffin") || selectedCat.contains("Mess")))) icon = "🍱";
                else if ("Cleaning".equals(selectedCat) || (selectedCat != null && selectedCat.contains("Clean"))) icon = "🧹";
                else if ("Wi-Fi".equals(selectedCat) || (selectedCat != null && selectedCat.contains("Wi-Fi"))) icon = "📶";
                else if ("Repair & Maintenance".equals(selectedCat) || (selectedCat != null && (selectedCat.contains("Repair") || selectedCat.contains("Maintenance")))) icon = "🛠️";

                String imgPath = icon;
                String publicId = "";

                if (selectedPhotoFile[0] != null) {
                    try {
                        CloudinaryService.UploadResult ures = CloudinaryService.uploadImage(selectedPhotoFile[0], "serviceImages");
                        if (ures != null && ures.isSuccess()) {
                            imgPath = ures.getSecureUrl();
                            publicId = ures.getPublicId();
                        } else {
                            imgPath = selectedPhotoFile[0].getAbsolutePath();
                        }
                    } catch (Exception ex) {
                        imgPath = selectedPhotoFile[0].getAbsolutePath();
                    }
                }

                String fullDesc = descField.getText().trim().isEmpty() ? "Verified student campus service." : descField.getText().trim();
                if (!hoursField.getText().trim().isEmpty()) {
                    fullDesc += " | Hours: " + hoursField.getText().trim();
                }
                if (!locField.getText().trim().isEmpty()) {
                    fullDesc += " | Area: " + locField.getText().trim();
                }

                ServiceItem newItem = new ServiceItem(
                    "s" + System.currentTimeMillis(),
                    icon,
                    titleField.getText().trim(),
                    selectedCat,
                    subField.getText().trim().isEmpty() ? "Student Campus Service" : subField.getText().trim(),
                    priceField.getText().trim().isEmpty() ? "₹ 299 / session" : priceField.getText().trim(),
                    providerField.getText().trim(),
                    phoneField.getText().trim().isEmpty() ? currentUser.getPhone() : phoneField.getText().trim(),
                    fullDesc,
                    currentUser.getUid(),
                    imgPath,
                    publicId,
                    System.currentTimeMillis()
                );
                return newItem;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newItem -> {
            DataRepository.getInstance().addService(newItem);
            new ServiceDAOImpl().save(newItem);
            showAlert("Success", "'" + newItem.getTitle() + "' added to your service catalog!");
            onAdded.run();
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
