package com.core2web.view.rent;

import com.core2web.dao.RoomDAOImpl;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.Rental;
import com.core2web.model.RoomItem;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.CloudinaryService;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.SessionManager;
import com.core2web.util.Theme;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

public class OwnerDashboard {

    private Scene ownerScene;
    private String activeCategory = "All Items";

    public Node getPageNode(Runnable onLogout) {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle(Theme.rootPaneStyle());

        User resolvedUser = DataRepository.getInstance().getCurrentUser();
        if (resolvedUser == null) {
            resolvedUser = SessionManager.getInstance().getCurrentUser();
        }
        if (resolvedUser == null) {
            resolvedUser = new User("", "Not provided", "Not provided", "Not provided", User.Role.OWNER);
        }
        final User currentUser = resolvedUser;

        // ─── Top Bar (Without Back to App button) ───────────────
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 30, 16, 30));
        topBar.setStyle(Theme.topBarStyle());

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefSize(36, 36);
        roleBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text roleIcon = new Text("🏢");
        roleIcon.setStyle("-fx-font-size: 16px;");
        roleBadge.getChildren().add(roleIcon);
        Text logoTxt = new Text("StudentExpress  •  Property Owner Workspace");
        logoTxt.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");
        logoRow.getChildren().addAll(roleBadge, logoTxt);
        HBox.setHgrow(logoRow, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Owner Logout");
            alert.setHeaderText("Logout from Property Owner Workspace?");
            alert.setContentText("You will return to the portal selection screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });

        topBar.getChildren().addAll(logoRow, logoutBtn);
        rootPane.setTop(topBar);

        // ─── Main Content ───────────────────────────────────────
        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28, 40, 28, 40));

        // Page Header
        VBox headingBox = new VBox(4);
        Text heading = new Text("Property Owner Workspace");
        heading.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 28px; -fx-font-weight: 800;");
        Text sub = new Text("Manage your room listings, furniture, electronics, appliances, rental duration parameters, and student requests.");
        sub.setStyle(Theme.mutedTextStyle());
        headingBox.getChildren().addAll(heading, sub);

        // Print OWNER DEBUG on workspace initialization
        System.out.println("[OWNER DEBUG] Firebase UID = " + (currentUser != null ? currentUser.getUid() : "null"));
        System.out.println("[OWNER DEBUG] Database user ID = " + (currentUser != null ? currentUser.getId() : "null"));
        System.out.println("[OWNER DEBUG] Role = " + (currentUser != null ? currentUser.getRole() : "null"));

        // Container for refreshing dynamic parts
        final Runnable[] refreshAll = new Runnable[1];

        // Owner Profile & Details Section
        VBox ownerProfileSection = createOwnerProfileCard(currentUser, () -> {
            if (refreshAll[0] != null) refreshAll[0].run();
        });

        // Stats Row
        List<Rental> allOwnerRentals = DataRepository.getInstance().getRentalsForOwner(currentUser.getName());
        long pendingCount = allOwnerRentals.stream().filter(r -> "REQUESTED".equalsIgnoreCase(r.getRentalStatus()) || "PENDING".equalsIgnoreCase(r.getRentalStatus()) || "EXTENSION_REQUESTED".equalsIgnoreCase(r.getRentalStatus())).count();

        HBox statsBox = new HBox(18);
        List<RoomItem> initialOwnerRooms = getFilteredListings("All Items", currentUser);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_KEY, String.valueOf(initialOwnerRooms.size()), "My Listings", Theme.PRIMARY),
            createStatCard(IconFactory.PATH_USERS, String.valueOf(allOwnerRentals.stream().filter(r -> "ACTIVE".equalsIgnoreCase(r.getRentalStatus()) || "ACCEPTED".equalsIgnoreCase(r.getRentalStatus())).count()), "Active Tenants", "#2563EB"),
            createStatCard(IconFactory.PATH_BELL, String.valueOf(pendingCount), "Pending Requests", "#D97706"),
            createStatCard(IconFactory.PATH_MONEY, "₹ 52,000", "Monthly Revenue", "#10B981")
        );

        // ─── Section 1: Rental Requests Management ────────────────
        VBox requestsSection = new VBox(14);
        HBox reqHeader = new HBox(12);
        reqHeader.setAlignment(Pos.CENTER_LEFT);
        Text reqTitle = new Text("Student Rental & Extension Requests");
        reqTitle.setStyle(Theme.sectionHeaderStyle());
        reqHeader.getChildren().add(reqTitle);

        VBox requestsContainer = new VBox(12);

        Runnable refreshRequests = () -> {
            requestsContainer.getChildren().clear();
            List<Rental> fsRequests = new com.core2web.dao.RentalDAOImpl().findByOwnerId(currentUser.getUid());
            if (fsRequests.isEmpty() && currentUser.getId() != null && !currentUser.getId().trim().isEmpty() && !currentUser.getId().equals(currentUser.getUid())) {
                fsRequests = new com.core2web.dao.RentalDAOImpl().findByOwnerId(currentUser.getId());
            }
            if (fsRequests.isEmpty() && currentUser.getName() != null && !currentUser.getName().isEmpty() && !currentUser.getName().equals("Not provided")) {
                fsRequests = new com.core2web.dao.RentalDAOImpl().findByOwnerId(currentUser.getName());
            }
            List<Rental> memoryRequests = DataRepository.getInstance().getRentalsForOwner(currentUser.getUid());
            if (memoryRequests.isEmpty() && currentUser.getId() != null && !currentUser.getId().trim().isEmpty() && !currentUser.getId().equals(currentUser.getUid())) {
                memoryRequests = DataRepository.getInstance().getRentalsForOwner(currentUser.getId());
            }
            if (memoryRequests.isEmpty() && currentUser.getName() != null && !currentUser.getName().isEmpty() && !currentUser.getName().equals("Not provided")) {
                memoryRequests = DataRepository.getInstance().getRentalsForOwner(currentUser.getName());
            }
            List<Rental> requests = new ArrayList<>(fsRequests);
            for (Rental mr : memoryRequests) {
                if (requests.stream().noneMatch(r -> r.getRentalId().equals(mr.getRentalId()))) {
                    requests.add(mr);
                }
            }
            System.out.println("[OWNER QUERY]");
            System.out.println("Query ownerId: " + currentUser.getUid());
            System.out.println("Requests found: " + requests.size());

            if (requests.isEmpty()) {
                VBox emptyReq = new VBox(10);
                emptyReq.setAlignment(Pos.CENTER);
                emptyReq.setPadding(new Insets(20));
                emptyReq.setStyle(Theme.cardStyle());
                Text emptyTxt = new Text("No student rental requests received yet.");
                emptyTxt.setStyle(Theme.mutedTextStyle());
                emptyReq.getChildren().add(emptyTxt);
                requestsContainer.getChildren().add(emptyReq);
            } else {
                for (Rental req : requests) {
                    VBox reqCard = createRentalRequestCard(req, refreshAll[0]);
                    requestsContainer.getChildren().add(reqCard);
                }
            }
        };

        requestsSection.getChildren().addAll(reqHeader, requestsContainer);

        // ─── Section 2: Inventory Listings Management ─────────────
        HBox sectionHeader = new HBox(20);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("My Inventory Listings");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        HBox.setHgrow(secTitle, Priority.ALWAYS);

        Button addListingBtn = new Button("➕  Add New Rental Listing");
        addListingBtn.setStyle(Theme.primaryBtnStyle());

        sectionHeader.getChildren().addAll(secTitle, addListingBtn);

        // Category Pills Filter Row
        HBox categoryPills = new HBox(10);
        categoryPills.setAlignment(Pos.CENTER_LEFT);
        String[] ownerCats = {"All Items", "Rooms", "Furniture", "Electronics", "Gym", "Appliances", "Vehicles"};

        VBox listingsContainer = new VBox(16);

        refreshAll[0] = () -> {
            refreshRequests.run();

            listingsContainer.getChildren().clear();
            List<RoomItem> items = getFilteredListings(activeCategory, currentUser);

            for (RoomItem r : items) {
                listingsContainer.getChildren().add(createListingCard(r, currentUser, () -> {
                    DataRepository.getInstance().removeRoom(r.getId());
                    new RoomDAOImpl().delete(r.getId());
                    showAlert("Deleted", "'" + r.getTitle() + "' removed from your inventory.");
                    if (refreshAll[0] != null) refreshAll[0].run();
                }));
            }

            if (items.isEmpty()) {
                VBox emptyBox = new VBox(10);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(30));
                emptyBox.setStyle(Theme.cardStyle());
                Text emptyTitle = new Text("No Listings in " + activeCategory);
                emptyTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");
                emptyBox.getChildren().add(emptyTitle);
                listingsContainer.getChildren().add(emptyBox);
            }

            // Update category pills styles
            for (javafx.scene.Node n : categoryPills.getChildren()) {
                if (n instanceof Button) {
                    Button b = (Button) n;
                    b.setStyle(Theme.filterPillStyle(b.getText().equalsIgnoreCase(activeCategory) || (activeCategory.equals("All Items") && b.getText().equals("All Items"))));
                }
            }
        };

        for (String cat : ownerCats) {
            Button pill = new Button(cat);
            pill.setStyle(Theme.filterPillStyle(cat.equals("All Items")));
            pill.setOnAction(e -> {
                activeCategory = cat;
                refreshAll[0].run();
            });
            categoryPills.getChildren().add(pill);
        }

        addListingBtn.setOnAction(e -> showAddListingDialog(currentUser, refreshAll[0]));

        // Initial populate
        refreshAll[0].run();

        mainContent.getChildren().addAll(headingBox, ownerProfileSection, statsBox, requestsSection, sectionHeader, categoryPills, listingsContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        rootPane.setCenter(scrollPane);
        return rootPane;
    }

    public Scene getPageScene(Runnable onLogout) {
        if (ownerScene == null) {
            ownerScene = new Scene((BorderPane) getPageNode(onLogout), 1050, 700);
        }
        return ownerScene;
    }

    private VBox createOwnerProfileCard(User owner, Runnable onRefresh) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(Theme.cardStyle());

        HBox mainRow = new HBox(20);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar Box
        VBox avatarCol = new VBox(8);
        avatarCol.setAlignment(Pos.CENTER);

        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(74, 74);
        avatarPane.setMinSize(74, 74);
        avatarPane.setMaxSize(74, 74);
        avatarPane.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 37px;");

        Image avatarImg = null;
        if (owner.getProfileImage() != null && !owner.getProfileImage().trim().isEmpty()) {
            avatarImg = ImageUtil.loadImage(owner.getProfileImage());
        }

        if (avatarImg != null && !avatarImg.isError()) {
            ImageView imgView = new ImageView(avatarImg);
            imgView.setFitWidth(74);
            imgView.setFitHeight(74);
            imgView.setPreserveRatio(false);
            Circle clip = new Circle(37, 37, 37);
            imgView.setClip(clip);
            avatarPane.getChildren().add(imgView);
        } else {
            String initial = (owner.getName() != null && !owner.getName().isEmpty())
                ? owner.getName().substring(0, 1).toUpperCase() : "O";
            Text initText = new Text(initial);
            initText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");
            avatarPane.getChildren().add(initText);
        }

        HBox photoActionRow = new HBox(6);
        photoActionRow.setAlignment(Pos.CENTER);

        Button uploadPhotoBtn = new Button("📷 " + (owner.getProfileImage() != null && !owner.getProfileImage().isEmpty() ? "Change" : "Upload"));
        uploadPhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 10px; -fx-padding: 3px 8px;");
        uploadPhotoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                try {
                    CloudinaryService.UploadResult res = CloudinaryService.uploadImage(file, "profileImages");
                    String imgUrl = (res != null && res.isSuccess()) ? res.getSecureUrl() : file.getAbsolutePath();
                    owner.setProfileImage(imgUrl);
                    if (res != null && res.isSuccess()) {
                        owner.setProfilePublicId(res.getPublicId());
                    }
                    new UserDAOImpl().save(owner);
                    DataRepository.getInstance().setCurrentUser(owner);
                    SessionManager.getInstance().login(owner);
                    showAlert("Photo Updated", "Your owner profile photo has been updated successfully!");
                    if (onRefresh != null) onRefresh.run();
                } catch (Exception ex) {
                    owner.setProfileImage(file.getAbsolutePath());
                    new UserDAOImpl().save(owner);
                    DataRepository.getInstance().setCurrentUser(owner);
                    SessionManager.getInstance().login(owner);
                    if (onRefresh != null) onRefresh.run();
                }
            }
        });

        photoActionRow.getChildren().add(uploadPhotoBtn);

        if (owner.getProfileImage() != null && !owner.getProfileImage().isEmpty()) {
            Button removePhotoBtn = new Button("✕");
            removePhotoBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 10px; -fx-padding: 3px 6px;");
            removePhotoBtn.setOnAction(e -> {
                if (owner.getProfilePublicId() != null && !owner.getProfilePublicId().isEmpty()) {
                    CloudinaryService.deleteImage(owner.getProfilePublicId());
                }
                owner.setProfileImage("");
                owner.setProfilePublicId("");
                new UserDAOImpl().save(owner);
                DataRepository.getInstance().setCurrentUser(owner);
                SessionManager.getInstance().login(owner);
                showAlert("Photo Removed", "Profile photo removed.");
                if (onRefresh != null) onRefresh.run();
            });
            photoActionRow.getChildren().add(removePhotoBtn);
        }

        avatarCol.getChildren().addAll(avatarPane, photoActionRow);

        // Owner Details
        VBox infoCol = new VBox(4);
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        HBox titleBadgeRow = new HBox(8);
        titleBadgeRow.setAlignment(Pos.CENTER_LEFT);
        Text nameText = new Text(owner.getName() != null && !owner.getName().trim().isEmpty() && !owner.getName().equals("Not provided") ? owner.getName().trim() : "Not provided");
        nameText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 18px; -fx-font-weight: 800;");

        Label verifiedBadge = new Label("✓ Verified Owner");
        verifiedBadge.setStyle(Theme.successBadgeStyle());

        Label roleBadgeLbl = new Label("🏢 OWNER PORTAL");
        roleBadgeLbl.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );

        titleBadgeRow.getChildren().addAll(nameText, verifiedBadge, roleBadgeLbl);

        HBox contactRow = new HBox(18);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        String emailDisplay = (owner.getEmail() != null && !owner.getEmail().trim().isEmpty() && !owner.getEmail().equals("Not provided")) ? owner.getEmail().trim() : "Not provided";
        String phoneDisplay = (owner.getPhone() != null && !owner.getPhone().trim().isEmpty() && !owner.getPhone().equals("Not provided")) ? owner.getPhone().trim() : "Not provided";
        String locationStr = (owner.getCollege() != null && !owner.getCollege().trim().isEmpty() && !owner.getCollege().equals("Not provided")) ? owner.getCollege().trim() : "Not provided";

        Text emailTxt = new Text("✉ " + emailDisplay);
        emailTxt.setStyle(Theme.mutedTextStyle());
        Text phoneTxt = new Text("📞 " + phoneDisplay);
        phoneTxt.setStyle(Theme.mutedTextStyle());
        Text locTxt = new Text("📍 " + locationStr);
        locTxt.setStyle(Theme.mutedTextStyle());

        contactRow.getChildren().addAll(emailTxt, phoneTxt, locTxt);

        infoCol.getChildren().addAll(titleBadgeRow, contactRow);

        // Edit Profile Button
        Button editBtn = new Button("✏ Edit Owner Details");
        editBtn.setStyle(Theme.secondaryBtnStyle());
        editBtn.setOnAction(e -> showEditOwnerDialog(owner, onRefresh));

        mainRow.getChildren().addAll(avatarCol, infoCol, editBtn);
        card.getChildren().add(mainRow);
        return card;
    }

    private void showEditOwnerDialog(User owner, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Property Owner Details");
        dialog.setHeaderText("Update your profile and contact information:");

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setPrefWidth(400);

        String currentName = (owner.getName() != null && !owner.getName().equals("Not provided")) ? owner.getName() : "";
        String currentPhone = (owner.getPhone() != null && !owner.getPhone().equals("Not provided")) ? owner.getPhone() : "";
        String currentLoc = (owner.getCollege() != null && !owner.getCollege().equals("Not provided")) ? owner.getCollege() : "";

        TextField nameField = new TextField(currentName);
        nameField.setPromptText("Enter owner name");
        TextField phoneField = new TextField(currentPhone);
        phoneField.setPromptText("Enter phone number");
        TextField locField = new TextField(currentLoc);
        locField.setPromptText("Enter address / location");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Owner Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Phone Number:"), 0, 1);
        grid.add(phoneField, 1, 1);

        grid.add(new Label("Address / Location:"), 0, 2);
        grid.add(locField, 1, 2);

        content.getChildren().add(grid);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                owner.setName(nameField.getText().trim());
                owner.setPhone(phoneField.getText().trim());
                owner.setCollege(locField.getText().trim());
                new UserDAOImpl().save(owner);
                DataRepository.getInstance().setCurrentUser(owner);
                SessionManager.getInstance().login(owner);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(ok -> {
            if (ok) {
                showAlert("Details Saved", "Your owner profile has been updated successfully.");
                if (onRefresh != null) onRefresh.run();
            }
        });
    }

    private VBox createRentalRequestCard(Rental r, Runnable onRefresh) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16, 20, 16, 20));

        String status = r.getRentalStatus();
        String accentColor = "REQUESTED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status) || "EXTENSION_REQUESTED".equalsIgnoreCase(status) ? "#D97706"
            : "ACCEPTED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status) ? Theme.PRIMARY
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

        Text studentTitle = new Text("👤 Student: " + r.getStudentName() + " (" + r.getStudentEmail() + " | " + r.getStudentPhone() + ")");
        studentTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 800;");

        Text itemLine = new Text("Item: " + r.getItemTitle() + "  •  Category: " + r.getItemCategory());
        itemLine.setStyle(Theme.mutedTextStyle());

        studInfo.getChildren().addAll(studentTitle, itemLine);

        Label statusBadge = new Label(status);
        statusBadge.setStyle("REQUESTED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status) || "EXTENSION_REQUESTED".equalsIgnoreCase(status) ? Theme.warningBadgeStyle() : Theme.successBadgeStyle());

        topRow.getChildren().addAll(studInfo, statusBadge);

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        HBox detailsRow = new HBox(30);
        detailsRow.setPadding(new Insets(8, 12, 8, 12));
        detailsRow.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 6px;");

        detailsRow.getChildren().addAll(
            detailItem("Start Date", r.getStartDate().toString()),
            detailItem("End Date", r.getEndDate().toString()),
            detailItem("Duration", r.getDuration() + " " + r.getDurationUnit()),
            detailItem("Rent Rate", fmt.format(r.getRentAmount()) + " / " + r.getRentType()),
            detailItem("Security Deposit", fmt.format(r.getSecurityDeposit())),
            detailItem("Total Amount", fmt.format(r.getTotalAmount()))
        );

        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        if ("REQUESTED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) {
            Button acceptBtn = new Button("✓ Accept Request");
            acceptBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            acceptBtn.setOnAction(ev -> {
                DataRepository.getInstance().updateRentalStatus(r.getRentalId(), "ACCEPTED");
                new Thread(() -> new com.core2web.dao.RentalDAOImpl().updateStatus(r.getRentalId(), "ACCEPTED")).start();
                showAlert("Request Accepted", "Rental request from " + r.getStudentName() + " has been ACCEPTED. Item is now marked as Currently Rented.");
                if (onRefresh != null) onRefresh.run();
            });

            Button rejectBtn = new Button("✕ Reject Request");
            rejectBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            rejectBtn.setOnAction(ev -> {
                DataRepository.getInstance().updateRentalStatus(r.getRentalId(), "REJECTED");
                new Thread(() -> new com.core2web.dao.RentalDAOImpl().updateStatus(r.getRentalId(), "REJECTED")).start();
                showAlert("Request Rejected", "Rental request from " + r.getStudentName() + " has been REJECTED. Item remains available.");
                if (onRefresh != null) onRefresh.run();
            });

            actionsRow.getChildren().addAll(acceptBtn, rejectBtn);
        } else if ("EXTENSION_REQUESTED".equalsIgnoreCase(status)) {
            Label extNotice = new Label("Student requested extension of + " + r.getExtensionDuration() + " " + r.getRentType() + "(s) until " + r.getNewEndDate());
            extNotice.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-font-size: 12px;");

            Button approveExtBtn = new Button("✓ Approve Extension");
            approveExtBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            approveExtBtn.setOnAction(ev -> {
                boolean ok = DataRepository.getInstance().approveRentalExtension(r.getRentalId());
                if (ok) {
                    showAlert("Extension Approved", "Extension approved! Rental end date updated to " + r.getEndDate());
                    if (onRefresh != null) onRefresh.run();
                }
            });

            Button rejectExtBtn = new Button("✕ Reject Extension");
            rejectExtBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            rejectExtBtn.setOnAction(ev -> {
                DataRepository.getInstance().rejectRentalExtension(r.getRentalId());
                showAlert("Extension Rejected", "Extension request rejected.");
                if (onRefresh != null) onRefresh.run();
            });

            actionsRow.getChildren().addAll(extNotice, approveExtBtn, rejectExtBtn);
        } else {
            Button agreementBtn = new Button("📄 View Digital Agreement");
            agreementBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            agreementBtn.setOnAction(ev -> RentalAgreementDialog.showAgreement(r));
            actionsRow.getChildren().add(agreementBtn);
        }

        card.getChildren().addAll(topRow, detailsRow, actionsRow);
        return card;
    }

    private boolean matchesCategory(RoomItem r, String catFilter) {
        if (catFilter == null || catFilter.isEmpty() || catFilter.equalsIgnoreCase("All Items") || catFilter.equalsIgnoreCase("All Rentals") || catFilter.equalsIgnoreCase("All")) {
            return true;
        }
        String filter = catFilter.toLowerCase().trim();
        String itemType = r.getType() != null ? r.getType().toLowerCase().trim() : "";
        String itemCat = r.getCategory() != null ? r.getCategory().toLowerCase().trim() : "";
        String itemTitle = r.getTitle() != null ? r.getTitle().toLowerCase().trim() : "";

        if (itemType.equals(filter) || itemCat.equals(filter) || itemType.contains(filter) || filter.contains(itemType)) {
            return true;
        }

        if (filter.contains("room") || filter.contains("pg")) {
            return itemType.contains("room") || itemType.contains("pg") || itemCat.contains("room") || itemCat.contains("pg") || itemTitle.contains("room") || itemTitle.contains("flat") || itemTitle.contains("pg") || itemTitle.contains("hostel");
        } else if (filter.contains("furniture") || filter.contains("table") || filter.contains("chair") || filter.contains("bed")) {
            return itemType.contains("furnitur") || itemCat.contains("furnitur") || itemTitle.contains("table") || itemTitle.contains("chair") || itemTitle.contains("bed") || itemTitle.contains("sofa") || itemTitle.contains("desk");
        } else if (filter.contains("electronic") || filter.contains("laptop") || filter.contains("gadget")) {
            return itemType.contains("electron") || itemCat.contains("electron") || itemTitle.contains("laptop") || itemTitle.contains("phone") || itemTitle.contains("macbook") || itemTitle.contains("dell") || itemTitle.contains("gadget");
        } else if (filter.contains("gym") || filter.contains("fitness")) {
            return itemType.contains("gym") || itemType.contains("fitness") || itemCat.contains("gym") || itemCat.contains("fitness") || itemTitle.contains("gym") || itemTitle.contains("dumbbell") || itemTitle.contains("fitness") || itemTitle.contains("bench");
        } else if (filter.contains("appliance") || filter.contains("fridge") || filter.contains("cooler") || filter.contains("oven")) {
            return itemType.contains("appliance") || itemCat.contains("appliance") || itemTitle.contains("fridge") || itemTitle.contains("cooler") || itemTitle.contains("microwave") || itemTitle.contains("geyser") || itemTitle.contains("washing");
        } else if (filter.contains("vehicle") || filter.contains("bike") || filter.contains("cycle") || filter.contains("scooter")) {
            return itemType.contains("vehicle") || itemCat.contains("vehicle") || itemTitle.contains("bike") || itemTitle.contains("cycle") || itemTitle.contains("scooter") || itemTitle.contains("yamaha") || itemTitle.contains("activa");
        } else if (filter.contains("book")) {
            return itemType.contains("book") || itemCat.contains("book") || itemTitle.contains("book") || itemTitle.contains("note");
        }
        return itemTitle.contains(filter) || itemType.contains(filter) || itemCat.contains(filter);
    }

    private List<RoomItem> getFilteredListings(String catFilter, User currentUser) {
        List<RoomItem> result = new ArrayList<>();
        String currentUid = currentUser != null ? currentUser.getUid() : "";
        String currentEmail = currentUser != null ? currentUser.getEmail() : "";
        String currentName = currentUser != null ? currentUser.getName() : "";

        for (RoomItem r : DataRepository.getInstance().getRooms()) {
            boolean isOwner = (r.getOwnerUid() != null && !r.getOwnerUid().isEmpty() && (r.getOwnerUid().equalsIgnoreCase(currentUid) || r.getOwnerUid().equalsIgnoreCase(currentEmail)))
                || (r.getOwnerName() != null && !r.getOwnerName().isEmpty() && !currentName.isEmpty() && !currentName.equals("Not provided") && r.getOwnerName().equalsIgnoreCase(currentName));

            if (isOwner) {
                if (matchesCategory(r, catFilter)) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    private HBox createListingCard(RoomItem r, User currentUser, Runnable onDelete) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(Theme.cardStyle());

        // Category Color Bar
        String catColor = "#4F772D";

        Rectangle colorBar = new Rectangle(5, 46);
        colorBar.setArcWidth(4); colorBar.setArcHeight(4);
        colorBar.setStyle("-fx-fill: " + catColor + ";");

        // Thumbnail Image
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(75, 55);
        imgBox.setMinSize(75, 55);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");

        Image img = ImageUtil.loadImage(r.getImagePath());
        if (img != null && !img.isError()) {
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(75);
            imgView.setFitHeight(55);
            imgView.setPreserveRatio(false);
            Rectangle clip = new Rectangle(75, 55);
            clip.setArcWidth(8); clip.setArcHeight(8);
            imgView.setClip(clip);
            imgBox.getChildren().add(imgView);
        } else {
            Node icon = IconFactory.getIconNode(IconFactory.PATH_KEY, Theme.PRIMARY, 20);
            imgBox.getChildren().add(icon);
        }

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        Text titleText = new Text(r.getTitle() + "   •   " + r.getPrice());
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(r.getType());
        catBadge.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-text-fill: " + Theme.PRIMARY + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );

        int photoCount = (r.getImages() != null && !r.getImages().isEmpty()) ? r.getImages().size() : 1;
        Label photoBadge = new Label("📷 " + photoCount + " Photo" + (photoCount > 1 ? "s" : ""));
        photoBadge.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px; -fx-font-weight: 600;");

        Text locText = new Text("📍 " + r.getLocation() + "   |   Min Stay: " + r.getMinDuration() + " " + r.getRentType() + "(s)   |   Deposit: " + fmt.format(r.getSecurityDeposit()));
        locText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, photoBadge, locText);
        info.getChildren().addAll(titleText, metaRow);

        String availStatus = r.getAvailabilityStatus();
        Label statusLbl = new Label(availStatus);
        statusLbl.setStyle("AVAILABLE".equalsIgnoreCase(availStatus) ? Theme.successBadgeStyle() : Theme.warningBadgeStyle());

        Button delBtn = new Button("Delete");
        delBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_TRASH, "#C62828", 14));
        delBtn.setStyle(Theme.dangerBtnStyle());
        delBtn.setOnAction(ev -> {
            boolean isOwner = (r.getOwnerUid() != null && (r.getOwnerUid().equalsIgnoreCase(currentUser.getUid()) || r.getOwnerUid().equalsIgnoreCase(currentUser.getEmail())))
                || (r.getOwnerName() != null && r.getOwnerName().equalsIgnoreCase(currentUser.getName()));
            if (!isOwner && (r.getOwnerUid() == null || r.getOwnerUid().isEmpty()) && "Owner User".equalsIgnoreCase(r.getOwnerName())) {
                isOwner = true;
            }
            if (!isOwner) {
                showAlert("Access Denied", "You can only delete your own listings.");
                return;
            }
            onDelete.run();
        });

        card.getChildren().addAll(colorBar, imgBox, info, statusLbl, delBtn);
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

    // Comprehensive Post Rental Dialog with Multiple Photo Upload, Preview & ScrollPane
    private void showAddListingDialog(User currentUser, Runnable onAdded) {
        Dialog<RoomItem> dialog = new Dialog<>();
        dialog.setTitle("Post New Property / Item Rental Listing");
        dialog.setHeaderText("Specify full rental parameters and upload listing photos:");

        VBox content = new VBox(14);
        content.setPadding(new Insets(18));
        content.setPrefWidth(540);

        TextField nameField = new TextField();
        nameField.setPromptText("Rental / Item Name (e.g. Deluxe Single Room / Yamaha R15)");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Rooms & PG", "Furniture", "Electronics", "Gym & Fitness", "Appliances", "Vehicles", "Books");
        catBox.setValue("Rooms & PG");
        catBox.setMaxWidth(Double.MAX_VALUE);

        TextField locField = new TextField();
        locField.setPromptText("Enter location (e.g. Area, City)");
        locField.setText(currentUser.getCollege() != null && !currentUser.getCollege().isEmpty() && !currentUser.getCollege().equals("Not provided") ? currentUser.getCollege() : "");

        TextField priceField = new TextField();
        priceField.setPromptText("Rent Amount (e.g. 6000)");

        ComboBox<String> rentTypeCombo = new ComboBox<>();
        rentTypeCombo.getItems().addAll("Monthly", "Daily", "Weekly");
        rentTypeCombo.setValue("Monthly");
        rentTypeCombo.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> minDurSpinner = new Spinner<>(1, 36, 1);
        minDurSpinner.setMaxWidth(Double.MAX_VALUE);

        TextField maxDurField = new TextField();
        maxDurField.setPromptText("Max Duration (Optional, e.g. 12)");

        TextField depositField = new TextField();
        depositField.setPromptText("Security Deposit (e.g. 15000)");

        DatePicker fromDatePicker = new DatePicker(LocalDate.now());
        fromDatePicker.setMaxWidth(Double.MAX_VALUE);

        DatePicker untilDatePicker = new DatePicker();
        untilDatePicker.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> availStatusCombo = new ComboBox<>();
        availStatusCombo.getItems().addAll("AVAILABLE", "CURRENTLY_RENTED", "MAINTENANCE");
        availStatusCombo.setValue("AVAILABLE");
        availStatusCombo.setMaxWidth(Double.MAX_VALUE);

        TextField descField = new TextField();
        descField.setPromptText("Description & Features (e.g. Furnished, Wi-Fi, 24/7 Water)");

        // Photo Upload Section
        VBox photoSection = new VBox(8);
        photoSection.setPadding(new Insets(12));
        photoSection.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 8px; -fx-border-style: dashed; -fx-border-width: 1.5px;");

        HBox photoHeaderRow = new HBox(10);
        photoHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Text photoTitle = new Text("Listing Photos (Support multiple photos)");
        photoTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Region phSpacer = new Region();
        HBox.setHgrow(phSpacer, Priority.ALWAYS);

        Button choosePhotosBtn = new Button("📷 Add Photos");
        choosePhotosBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 4px 10px; -fx-font-size: 11px;");

        photoHeaderRow.getChildren().addAll(photoTitle, phSpacer, choosePhotosBtn);

        FlowPane previewPane = new FlowPane(8, 8);
        previewPane.setPadding(new Insets(4, 0, 0, 0));

        List<File> selectedImageFiles = new ArrayList<>();
        final Runnable[] updatePreviews = new Runnable[1];

        updatePreviews[0] = () -> {
            previewPane.getChildren().clear();
            if (selectedImageFiles.isEmpty()) {
                Text emptyPht = new Text("No photos selected yet. Default category photo will be used if none added.");
                emptyPht.setStyle(Theme.mutedTextStyle());
                previewPane.getChildren().add(emptyPht);
            } else {
                for (int i = 0; i < selectedImageFiles.size(); i++) {
                    final int idx = i;
                    File f = selectedImageFiles.get(i);
                    StackPane thumb = new StackPane();
                    thumb.setPrefSize(70, 52);
                    thumb.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 6px;");

                    try {
                        Image simg = new Image(f.toURI().toString());
                        ImageView iv = new ImageView(simg);
                        iv.setFitWidth(70);
                        iv.setFitHeight(52);
                        iv.setPreserveRatio(false);
                        Rectangle clip = new Rectangle(70, 52);
                        clip.setArcWidth(6); clip.setArcHeight(6);
                        iv.setClip(clip);
                        thumb.getChildren().add(iv);
                    } catch (Exception ignored) {}

                    Button rmBtn = new Button("✕");
                    rmBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 2px 5px; -fx-background-radius: 4px; -fx-cursor: hand;");
                    rmBtn.setOnAction(ev -> {
                        selectedImageFiles.remove(idx);
                        if (updatePreviews[0] != null) updatePreviews[0].run();
                    });
                    StackPane.setAlignment(rmBtn, Pos.TOP_RIGHT);
                    StackPane.setMargin(rmBtn, new Insets(2));
                    thumb.getChildren().add(rmBtn);

                    previewPane.getChildren().add(thumb);
                }
            }
        };

        choosePhotosBtn.setOnAction(ev -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Listing Photos");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            List<File> files = chooser.showOpenMultipleDialog(null);
            if (files != null && !files.isEmpty()) {
                selectedImageFiles.addAll(files);
                if (updatePreviews[0] != null) updatePreviews[0].run();
            }
        });

        updatePreviews[0].run();
        photoSection.getChildren().addAll(photoHeaderRow, previewPane);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.add(new Label("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);

        grid.add(new Label("Location:"), 0, 2);
        grid.add(locField, 1, 2);

        grid.add(new Label("Rent Amount (₹):"), 0, 3);
        grid.add(priceField, 1, 3);

        grid.add(new Label("Rent Type:"), 0, 4);
        grid.add(rentTypeCombo, 1, 4);

        grid.add(new Label("Min Duration:"), 0, 5);
        grid.add(minDurSpinner, 1, 5);

        grid.add(new Label("Max Duration:"), 0, 6);
        grid.add(maxDurField, 1, 6);

        grid.add(new Label("Security Deposit (₹):"), 0, 7);
        grid.add(depositField, 1, 7);

        grid.add(new Label("Available From:"), 0, 8);
        grid.add(fromDatePicker, 1, 8);

        grid.add(new Label("Available Until:"), 0, 9);
        grid.add(untilDatePicker, 1, 9);

        grid.add(new Label("Availability Status:"), 0, 10);
        grid.add(availStatusCombo, 1, 10);

        grid.add(new Label("Description:"), 0, 11);
        grid.add(descField, 1, 11);

        content.getChildren().addAll(grid, photoSection);

        // Wrap inside ScrollPane for full responsiveness without clipping
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
            if (btn == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                double rentAmt = 0.0;
                try { rentAmt = Double.parseDouble(priceField.getText().trim()); } catch (Exception e) {}
                if (rentAmt <= 0) rentAmt = 500.0;

                double deposit = 0.0;
                try { deposit = Double.parseDouble(depositField.getText().trim()); } catch (Exception e) {}

                Integer maxDur = null;
                try {
                    if (!maxDurField.getText().trim().isEmpty()) maxDur = Integer.parseInt(maxDurField.getText().trim());
                } catch (Exception e) {}

                String defaultImgPath = "assets/image/room_single.png";
                String selectedCat = catBox.getValue();
                if (selectedCat.contains("Furniture")) defaultImgPath = "assets/image/table_study.png";
                else if (selectedCat.contains("Electronics")) defaultImgPath = "assets/image/laptop_macbook.png";
                else if (selectedCat.contains("Vehicles")) defaultImgPath = "assets/image/bike_yamaha.png";

                List<String> uploadedUrls = new ArrayList<>();
                String primaryImg = defaultImgPath;

                if (!selectedImageFiles.isEmpty()) {
                    for (File imgF : selectedImageFiles) {
                        try {
                            CloudinaryService.UploadResult ures = CloudinaryService.uploadImage(imgF, "roomImages");
                            if (ures != null && ures.isSuccess()) {
                                uploadedUrls.add(ures.getSecureUrl());
                            } else {
                                uploadedUrls.add(imgF.getAbsolutePath());
                            }
                        } catch (Exception ex) {
                            uploadedUrls.add(imgF.getAbsolutePath());
                        }
                    }
                    if (!uploadedUrls.isEmpty()) {
                        primaryImg = uploadedUrls.get(0);
                    }
                } else {
                    uploadedUrls.add(defaultImgPath);
                }

                String rentType = rentTypeCombo.getValue();
                String priceFormatted = "₹ " + String.format("%.0f", rentAmt) + " / " + rentType.toLowerCase();

                RoomItem newItem = new RoomItem(
                    "r" + System.currentTimeMillis(),
                    nameField.getText().trim(),
                    locField.getText().trim().isEmpty() ? "Pune" : locField.getText().trim(),
                    priceFormatted,
                    "1.0 km",
                    selectedCat,
                    selectedCat,
                    new String[]{"Verified Listing", "Student Rental"},
                    descField.getText().trim().isEmpty() ? "Listed by Property Owner." : descField.getText().trim(),
                    currentUser.getName(),
                    currentUser.getPhone(),
                    primaryImg,
                    rentType,
                    minDurSpinner.getValue(),
                    maxDur,
                    deposit,
                    fromDatePicker.getValue() != null ? fromDatePicker.getValue() : LocalDate.now(),
                    untilDatePicker.getValue(),
                    availStatusCombo.getValue()
                );

                newItem.setOwnerUid(currentUser.getUid());
                newItem.setImages(uploadedUrls);
                return newItem;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newItem -> {
            DataRepository.getInstance().addRoom(newItem);
            new RoomDAOImpl().save(newItem);
            System.out.println("[LISTING] Created: listingId=" + newItem.getId() + ", ownerId=" + newItem.getOwnerUid() + ", category=" + newItem.getCategory());
            showAlert("Success", "'" + newItem.getTitle() + "' added to your rental inventory!");
            onAdded.run();
        });
    }

    private VBox detailItem(String label, String value) {
        VBox b = new VBox(2);
        Text labelTxt = new Text(label);
        labelTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        Text valueTxt = new Text(value);
        valueTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        b.getChildren().addAll(labelTxt, valueTxt);
        return b;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
