package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.BookingController;
import com.core2web.controller.OwnerController;
import com.core2web.controller.RoomController;
import com.core2web.model.RoomItem;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class OwnerDashboard {

    private Scene ownerScene;
    private String activeCategory = "All Items";
    private final OwnerController ownerController = new OwnerController();
    private final RoomController roomController = new RoomController();
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

        Button logoutBtn = new Button("Logout / Switch Portal");
        logoutBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_LOGOUT, "#C62828", 14));
        logoutBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-weight: 800; -fx-padding: 8px 16px;");
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        topBar.getChildren().addAll(backToAppBtn, topSpacer, logoutBtn);

        // Heading + Primary Action
        HBox headingBox = new HBox(16);
        headingBox.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Text titleTxt = new Text("Property & Rental Management Dashboard");
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 24px; -fx-font-weight: 800;");
        Text subTxt = new Text("Manage your PG rooms, flats, furniture, appliances & rental listings");
        subTxt.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleTxt, subTxt);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button addListingBtn = new Button("+ Add New Rental Listing");
        addListingBtn.setStyle(Theme.primaryBtnStyle());

        Button ownerHeaderLogoutBtn = new Button("Logout");
        ownerHeaderLogoutBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_LOGOUT, "#C62828", 14));
        ownerHeaderLogoutBtn.setStyle(Theme.dangerBtnStyle());
        ownerHeaderLogoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        headingBox.getChildren().addAll(titleBox, addListingBtn, ownerHeaderLogoutBtn);


        // Quick Stats Row (4 Cards)
        HBox statsBox = new HBox(16);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_KEY, String.valueOf(ownerController.getOwnerRooms().size()), "Total Listings", Theme.PRIMARY),
            createStatCard(IconFactory.PATH_USERS, "14", "Active Tenants", "#2563EB"),
            createStatCard(IconFactory.PATH_MESSAGE, "8", "Tenant Inquiries", "#D97706"),
            createStatCard(IconFactory.PATH_MONEY, "₹ 48,500", "Monthly Revenue", "#10B981")
        );

        // Section Title + Category Filters
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("My Rental Listings");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        sectionHeader.getChildren().add(secTitle);

        HBox categoryPills = new HBox(10);
        String[] cats = {"All Items", "Rooms", "Furniture", "Electronics", "Appliances", "Vehicles"};
        for (int i = 0; i < cats.length; i++) {
            String cat = cats[i];
            Button pill = new Button(cat);
            pill.setStyle(Theme.filterPillStyle(i == 0));
            pill.setOnAction(e -> {
                for (javafx.scene.Node n : categoryPills.getChildren()) {
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        b.setStyle(Theme.filterPillStyle(b.getText().equals(cat)));
                    }
                }
                activeCategory = cat;
            });
            categoryPills.getChildren().add(pill);
        }

        VBox listingsContainer = new VBox(16);

        final Runnable[] refreshListings = new Runnable[1];
        refreshListings[0] = () -> {
            listingsContainer.getChildren().clear();
            List<RoomItem> filtered = getFilteredListings(activeCategory);

            if (filtered.isEmpty()) {
                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                empty.setStyle(Theme.cardStyle());
                Text icon = new Text("🏘️");
                icon.setStyle("-fx-font-size: 40px;");
                Text emptyTxt = new Text("No rental listings in this category yet.");
                emptyTxt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().addAll(icon, emptyTxt);
                listingsContainer.getChildren().add(empty);
            } else {
                for (RoomItem room : filtered) {
                    HBox card = createListingCard(room, () -> {
                        roomController.removeRoom(room.getId());
                        if (refreshListings[0] != null) refreshListings[0].run();
                    });
                    listingsContainer.getChildren().add(card);
                }
            }
        };

        addListingBtn.setOnAction(e -> showAddListingDialog(refreshListings[0]));
        refreshListings[0].run();

        mainContent.getChildren().addAll(headingBox, statsBox, sectionHeader, categoryPills, listingsContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onLogout) {
        Node node = getPageNode(onLogout);
        
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle(Theme.rootPaneStyle());

        // ─── Top Bar ────────────────────────────────────────────
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

        Button backAppBtn = new Button("← Back to App");
        backAppBtn.setStyle(Theme.outlineBtnStyle());
        backAppBtn.setOnAction(e -> Main.showHomePage());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Owner Logout");
            alert.setHeaderText("Logout from Property Owner Workspace?");
            alert.setContentText("You will return to the login screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });

        topBar.getChildren().addAll(logoRow, backAppBtn, logoutBtn);
        rootPane.setTop(topBar);


        // ─── Main Content ───────────────────────────────────────
        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28, 40, 28, 40));

        // Page Header
        VBox headingBox = new VBox(4);
        Text heading = new Text("Property Owner Workspace");
        heading.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 30px; -fx-font-weight: 800;");
        Text sub = new Text("Manage your student room listings, furniture, electronics, appliances, and gym equipment rentals.");
        sub.setStyle(Theme.mutedTextStyle());
        headingBox.getChildren().addAll(heading, sub);

        // Stats Row
        HBox statsBox = new HBox(18);
        statsBox.getChildren().addAll(
            createStatCard("🏢", "Rooms & PG", countByCategory("Rooms"), "#4F772D", "Rooms Listed"),
            createStatCard("🪑", "Furniture Rentals", countByCategory("Furniture"), "#D97706", "Tables, Chairs & Beds"),
            createStatCard("💻", "Electronics", countByCategory("Electronics"), "#2563EB", "Laptops & Devices"),
            createStatCard("🏋️", "Gym & Fitness", countByCategory("Gym"), "#7C3AED", "Dumbbells & Fitness")
        );

        // Section Header with Add Item button
        HBox sectionHeader = new HBox(20);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("My Inventory Listings");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        HBox.setHgrow(secTitle, Priority.ALWAYS);

        Button addListingBtn = new Button("➕  Add New Listing");
        addListingBtn.setStyle(Theme.primaryBtnStyle());

        sectionHeader.getChildren().addAll(secTitle, addListingBtn);

        // Category Pills Filter Row
        HBox categoryPills = new HBox(10);
        categoryPills.setAlignment(Pos.CENTER_LEFT);
        String[] ownerCats = {"All Items", "🏠 Rooms & PG", "🪑 Furniture", "💻 Electronics", "🏋️ Gym & Fitness", "🔌 Appliances & Vehicles", "📚 Books"};

        VBox listingsContainer = new VBox(16);

        final Runnable[] refreshListings = new Runnable[1];
        refreshListings[0] = () -> {
            listingsContainer.getChildren().clear();
            List<RoomItem> items = getFilteredListings(activeCategory);

            for (RoomItem r : items) {
                listingsContainer.getChildren().add(createListingCard(r, () -> {
                    roomController.removeRoom(r.getId());
                    showAlert("Deleted", "'" + r.getTitle() + "' removed from your workspace.");
                }));
            }

            if (items.isEmpty()) {
                VBox emptyBox = new VBox(10);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(40));
                emptyBox.setStyle(Theme.cardStyle());
                Text emptyIcon = new Text("🔍");
                emptyIcon.setFont(Font.font(36));
                Text emptyTitle = new Text("No Listings in " + activeCategory);
                emptyTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                Text emptySub = new Text("Click 'Add New Listing' to post new rooms, furniture, electronics, or gym gear.");
                emptySub.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 13px;");
                emptyBox.getChildren().addAll(emptyIcon, emptyTitle, emptySub);
                listingsContainer.getChildren().add(emptyBox);
            }

            // Update category pills styles
            for (javafx.scene.Node n : categoryPills.getChildren()) {
                if (n instanceof Button) {
                    Button b = (Button) n;
                    b.setStyle(Theme.filterPillStyle(b.getText().contains(activeCategory) || (activeCategory.equals("All Items") && b.getText().equals("All Items"))));
                }
            }
        };

        for (String cat : ownerCats) {
            Button pill = new Button(cat);
            pill.setStyle(Theme.filterPillStyle(cat.equals("All Items")));
            pill.setOnAction(e -> {
                if (cat.contains("Rooms")) activeCategory = "Rooms";
                else if (cat.contains("Furniture")) activeCategory = "Furniture";
                else if (cat.contains("Electronics")) activeCategory = "Electronics";
                else if (cat.contains("Gym")) activeCategory = "Gym";
                else if (cat.contains("Appliances")) activeCategory = "Appliances";
                else if (cat.contains("Books")) activeCategory = "Books";
                else activeCategory = "All Items";
                refreshListings[0].run();
            });
            categoryPills.getChildren().add(pill);
        }

        addListingBtn.setOnAction(e -> showAddListingDialog(refreshListings[0]));

        // Initial populate
        refreshListings[0].run();

        VBox ownerBookingsSection = createOwnerBookingsSection();

        mainContent.getChildren().addAll(headingBox, statsBox, ownerBookingsSection, sectionHeader, categoryPills, listingsContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        rootPane.setCenter(scrollPane);
        ownerScene = new Scene(rootPane, 1050, 700);
        return ownerScene;
    }

    private List<RoomItem> getFilteredListings(String catFilter) {
        List<RoomItem> result = new ArrayList<>();
        for (RoomItem r : ownerController.getOwnerRooms()) {
            if (catFilter.equals("All Items")) {
                result.add(r);
            } else if (catFilter.equals("Rooms")) {
                if (isRoomCategory(r)) result.add(r);
            } else if (catFilter.equals("Furniture")) {
                if (isFurnitureCategory(r)) result.add(r);
            } else if (catFilter.equals("Electronics")) {
                if (isElectronicsCategory(r)) result.add(r);
            } else if (catFilter.equals("Gym")) {
                if (isGymCategory(r)) result.add(r);
            } else if (catFilter.equals("Appliances")) {
                if (isApplianceOrVehicleCategory(r)) result.add(r);
            } else if (catFilter.equals("Books")) {
                if (isBookCategory(r)) result.add(r);
            }
        }
        return result;
    }

    private boolean isRoomCategory(RoomItem r) {
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String title = r.getTitle().toLowerCase();
        return type.contains("room") || type.contains("pg") || type.contains("studio") || type.contains("flat")
            || title.contains("room") || title.contains("pg") || title.contains("flat") || title.contains("studio");
    }

    private boolean isFurnitureCategory(RoomItem r) {
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String title = r.getTitle().toLowerCase();
        return type.contains("furniture") || title.contains("table") || title.contains("chair") || title.contains("bed") || title.contains("bookshelf");
    }

    private boolean isElectronicsCategory(RoomItem r) {
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String title = r.getTitle().toLowerCase();
        return type.contains("electronics") || title.contains("laptop") || title.contains("macbook") || title.contains("calculator");
    }

    private boolean isGymCategory(RoomItem r) {
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String title = r.getTitle().toLowerCase();
        return type.contains("gym") || type.contains("fitness") || title.contains("dumbbell") || title.contains("treadmill") || title.contains("exercise bike");
    }

    private boolean isApplianceOrVehicleCategory(RoomItem r) {
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String title = r.getTitle().toLowerCase();
        return type.contains("appliance") || type.contains("vehicle") || title.contains("fridge") || title.contains("cooler") || title.contains("bike") || title.contains("cycle") || title.contains("microwave");
    }

    private boolean isBookCategory(RoomItem r) {
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String title = r.getTitle().toLowerCase();
        return type.contains("book") || title.contains("book") || title.contains("math") || title.contains("gate");
    }

    private String countByCategory(String cat) {
        return String.valueOf(getFilteredListings(cat).size());
    }

    private HBox createListingCard(RoomItem r, Runnable onDelete) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(Theme.cardStyle());

        // Category Color Bar
        String catColor = "#4F772D"; // Default Green
        String catLabelText = "Rooms & PG";

        if (isFurnitureCategory(r)) {
            catColor = "#D97706"; catLabelText = "Furniture";
        } else if (isElectronicsCategory(r)) {
            catColor = "#2563EB"; catLabelText = "Electronics";
        } else if (isGymCategory(r)) {
            catColor = "#7C3AED"; catLabelText = "Gym & Fitness";
        } else if (isApplianceOrVehicleCategory(r)) {
            catColor = "#0D9488"; catLabelText = "Appliance / Vehicle";
        } else if (isBookCategory(r)) {
            catColor = "#EA580C"; catLabelText = "Books";
        }

        Rectangle colorBar = new Rectangle(5, 42);
        colorBar.setArcWidth(4); colorBar.setArcHeight(4);
        colorBar.setStyle("-fx-fill: " + catColor + ";");

        // Thumbnail Image
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(75, 55);
        imgBox.setMinSize(75, 55);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");

        Image img = com.core2web.util.ImageUtil.loadImage(r.getImagePath());
        if (img != null) {
            try {
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(75);
                imgView.setFitHeight(55);
                imgView.setPreserveRatio(false);
                Rectangle clip = new Rectangle(75, 55);
                clip.setArcWidth(8); clip.setArcHeight(8);
                imgView.setClip(clip);
                imgBox.getChildren().add(imgView);
            } catch (Exception e) {}
        }

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text titleText = new Text(r.getTitle() + "   •   " + r.getPrice());
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(catLabelText);
        catBadge.setStyle(
            "-fx-background-color: " + catColor + "1E;"
            + "-fx-text-fill: " + catColor + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );

        Text locText = new Text("📍 " + r.getLocation() + "   |   👤 " + r.getOccupants());
        locText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, locText);
        info.getChildren().addAll(titleText, metaRow);

        Label statusLbl = new Label("Active");
        statusLbl.setStyle(Theme.successBadgeStyle());

        Button editBtn = new Button("Edit");
        editBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_EDIT, Theme.PRIMARY, 14));
        editBtn.setStyle(Theme.secondaryBtnStyle());
        editBtn.setOnAction(ev -> showAlert("Edit Listing", "Edit details for: " + r.getTitle()));

        Button delBtn = new Button("Delete");
        delBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_TRASH, "#C62828", 14));
        delBtn.setStyle(Theme.dangerBtnStyle());
        delBtn.setOnAction(ev -> onDelete.run());

        card.getChildren().addAll(colorBar, imgBox, info, statusLbl, editBtn, delBtn);
        return card;
    }

    private VBox createStatCard(String iconPath, String value, String title, String accentColor) {
        return createStatCard(iconPath, title, value, accentColor, "");
    }

    private VBox createStatCard(String iconPath, String title, String value, String accentColor, String subtext) {
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

    private void showAddListingDialog(Runnable onAdded) {
        Dialog<RoomItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Property / Inventory Listing");
        dialog.setHeaderText("Choose category and enter item listing details:");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(420);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Rooms & PG", "Furniture", "Electronics", "Gym & Fitness", "Appliances", "Vehicles", "Books");
        catBox.setValue("Rooms & PG");
        catBox.setMaxWidth(Double.MAX_VALUE);

        TextField titleField = new TextField();
        titleField.setPromptText("Title (e.g. Single PG Room / Rent Study Table)");

        TextField locField = new TextField();
        locField.setPromptText("Location (e.g. Kothrud, Pune)");
        locField.setText("Kothrud, Pune");

        TextField priceField = new TextField();
        priceField.setPromptText("Price (e.g. ₹ 5,500 / month)");

        TextField descField = new TextField();
        descField.setPromptText("Short Description / Amenities");

        final File[] selectedFile = new File[1];
        Button chooseImgBtn = new Button("📷 Choose Image (Optional)");
        chooseImgBtn.setStyle(Theme.outlineBtnStyle());
        Label imgNameLbl = new Label("No file chosen");
        imgNameLbl.setStyle(Theme.mutedTextStyle());
        HBox imgBox = new HBox(10, chooseImgBtn, imgNameLbl);
        imgBox.setAlignment(Pos.CENTER_LEFT);
        chooseImgBtn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Room Image");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File f = chooser.showOpenDialog(null);
            if (f != null) {
                selectedFile[0] = f;
                imgNameLbl.setText(f.getName());
            }
        });

        content.getChildren().addAll(
            new Label("Section Category:"), catBox,
            new Label("Listing Title:"), titleField,
            new Label("Location:"), locField,
            new Label("Price:"), priceField,
            new Label("Description:"), descField,
            new Label("Image Photo:"), imgBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !titleField.getText().trim().isEmpty()) {
                String finalImgPath = "";
                String selectedCat = catBox.getValue();
                if (selectedFile[0] != null && selectedFile[0].exists()) {
                    com.core2web.service.CloudinaryService.UploadResult uploadRes =
                        com.core2web.service.CloudinaryService.uploadImage(selectedFile[0], "roomImages");
                    if (uploadRes != null && uploadRes.isSuccess()) {
                        finalImgPath = uploadRes.getSecureUrl();
                    } else {
                        String base64 = ImageUtil.compressAndEncode(selectedFile[0]);
                        if (base64 != null && !base64.isEmpty()) {
                            finalImgPath = base64;
                        }
                    }
                }

                String priceVal = priceField.getText().trim().isEmpty() ? "₹ 499 / month" : priceField.getText().trim();
                String activeUserUid = SessionManager.getInstance().getUid();
                String activeUserName = SessionManager.getInstance().getName();

                return new RoomItem(
                    "r_" + System.currentTimeMillis(),
                    titleField.getText().trim(),
                    locField.getText().trim().isEmpty() ? "Kothrud, Pune" : locField.getText().trim(),
                    priceVal.startsWith("₹") ? priceVal : "₹ " + priceVal,
                    "1.5 km",
                    selectedCat,
                    selectedCat,
                    new String[]{"Furnished", "Student Friendly"},
                    descField.getText().trim().isEmpty() ? "Listed by Property Owner." : descField.getText().trim(),
                    activeUserName != null ? activeUserName : "Property Owner",
                    "+91 99000 11222",
                    finalImgPath,
                    activeUserUid
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newItem -> {
            roomController.addRoom(newItem);
            showAlert("Success", "'" + newItem.getTitle() + "' added to your " + newItem.getType() + " inventory!");
            onAdded.run();
        });
    }

    private VBox createOwnerBookingsSection() {
        VBox section = new VBox(14);
        Text title = new Text("Room Booking Requests Received");
        title.setStyle(Theme.sectionHeaderStyle());

        VBox bookingsList = new VBox(12);

        Runnable refresh = () -> {
            bookingsList.getChildren().clear();
            List<com.core2web.model.Booking> list = bookingController.getOwnerBookings();

            if (list.isEmpty()) {
                VBox empty = new VBox(8);
                empty.setPadding(new Insets(18));
                empty.setStyle(Theme.cardStyle());
                Text txt = new Text("No room booking requests received yet.");
                txt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().add(txt);
                bookingsList.getChildren().add(empty);
            } else {
                for (com.core2web.model.Booking b : list) {
                    HBox card = new HBox(16);
                    card.setPadding(new Insets(14, 18, 14, 18));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(Theme.cardStyle());

                    VBox info = new VBox(4);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Text bTitle = new Text(b.getItemOrServiceName() + "   •   Date: " + b.getDate());
                    bTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: 700; -fx-font-size: 14px;");
                    Text bSub = new Text("Booking ID: " + b.getId() + "   |   User: " + (b.getUserEmail() != null && !b.getUserEmail().isEmpty() ? b.getUserEmail() : b.getUserUid()));
                    bSub.setStyle(Theme.mutedTextStyle());
                    info.getChildren().addAll(bTitle, bSub);

                    Label statusBadge = new Label(b.getStatus());
                    statusBadge.setStyle("CONFIRMED".equalsIgnoreCase(b.getStatus()) || "ACCEPTED".equalsIgnoreCase(b.getStatus()) ? Theme.successBadgeStyle() : "REJECTED".equalsIgnoreCase(b.getStatus()) ? Theme.dangerBtnStyle() : Theme.warningBadgeStyle());

                    Button acceptBtn = new Button("Accept");
                    acceptBtn.setStyle(Theme.primaryBtnStyle());
                    acceptBtn.setOnAction(ev -> {
                        bookingController.updateBookingStatus(b.getId(), "CONFIRMED");
                        showAlert("Accepted", "Booking for '" + b.getItemOrServiceName() + "' accepted.");
                    });

                    Button rejectBtn = new Button("Reject");
                    rejectBtn.setStyle(Theme.dangerBtnStyle());
                    rejectBtn.setOnAction(ev -> {
                        bookingController.updateBookingStatus(b.getId(), "REJECTED");
                        showAlert("Rejected", "Booking for '" + b.getItemOrServiceName() + "' rejected.");
                    });

                    card.getChildren().addAll(info, statusBadge, acceptBtn, rejectBtn);
                    bookingsList.getChildren().add(card);
                }
            }
        };

        refresh.run();
        section.getChildren().addAll(title, bookingsList);
        return section;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
