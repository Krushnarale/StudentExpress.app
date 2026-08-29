package com.core2web.view.marketplace;

import com.core2web.Main;
import com.core2web.dao.OrderDAOImpl;
import com.core2web.dao.ProductDAOImpl;
import com.core2web.dao.SellerDAOImpl;
import com.core2web.dao.UserDAOImpl;
import com.core2web.model.Order;
import com.core2web.model.ProductItem;
import com.core2web.model.SellerProfile;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.CloudinaryService;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.collections.FXCollections;
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

public class SellerDashboard {

    private Scene sellerScene;

    public enum SellerTab {
        DASHBOARD, MY_LISTINGS, ADD_ITEM, REQUESTS, PROFILE
    }

    private SellerTab currentTab = SellerTab.DASHBOARD;
    private String listingFilter = "ALL"; // ALL, ACTIVE, SOLD
    private String categoryFilter = "All Categories";

    public Node getPageNode(Runnable onPostItem, Runnable onLogout) {
        return getPageNodeWithTab(SellerTab.DASHBOARD, onPostItem, onLogout);
    }

    public Node getPageNodeWithTab(SellerTab initialTab, Runnable onPostItem, Runnable onLogout) {
        this.currentTab = initialTab != null ? initialTab : SellerTab.DASHBOARD;

        User resolvedUser = DataRepository.getInstance().getCurrentUser();
        if (resolvedUser == null) resolvedUser = com.core2web.util.SessionManager.getInstance().getCurrentUser();
        if (resolvedUser == null) resolvedUser = new User("", "Student Seller", "", "", User.Role.SELLER);
        final User currentUser = resolvedUser;
        final String sellerUid = currentUser.getUid() != null ? currentUser.getUid() : "";

        // Fetch or create seller profile
        SellerProfile sellerProfile = DataRepository.getInstance().getSellerProfile(sellerUid);
        if (sellerProfile == null && !sellerUid.isEmpty()) {
            Optional<SellerProfile> fsProf = new SellerDAOImpl().findBySellerId(sellerUid);
            if (fsProf.isPresent()) {
                sellerProfile = fsProf.get();
                DataRepository.getInstance().addOrUpdateSeller(sellerProfile);
            } else {
                sellerProfile = new SellerProfile(
                    sellerUid,
                    currentUser.getName(),
                    currentUser.getEmail(),
                    currentUser.getPhone(),
                    currentUser.getCollege(),
                    currentUser.getCollege(),
                    "Campus Student Seller",
                    currentUser.getProfileImage(),
                    currentUser.getProfilePublicId(),
                    "ACTIVE",
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                );
                new SellerDAOImpl().save(sellerProfile);
                DataRepository.getInstance().addOrUpdateSeller(sellerProfile);
            }
        }

        System.out.println("========== SELLER ACCESS ==========");
        System.out.println("Firebase UID = " + (sellerUid.isEmpty() ? "Not authenticated" : sellerUid));
        System.out.println("Seller profile found = " + (sellerProfile != null));
        System.out.println("Seller enabled = " + (currentUser.isSellerEnabled()));
        System.out.println("Access granted = true");
        System.out.println("==========================================");

        VBox rootBox = new VBox(18);
        rootBox.setPadding(new Insets(24, 36, 30, 36));
        rootBox.setMaxWidth(Double.MAX_VALUE);

        // ─────────────────────────────────────────────────────────
        // HEADER ROW: Title, Role Badge, Logout (NO Back to App)
        // ─────────────────────────────────────────────────────────
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(42, 42);
        iconBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text bagIcon = new Text("🛍️");
        bagIcon.setStyle("-fx-font-size: 20px;");
        iconBox.getChildren().add(bagIcon);

        VBox titleBox = new VBox(2);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        HBox nameBadgeRow = new HBox(8);
        nameBadgeRow.setAlignment(Pos.CENTER_LEFT);

        Text titleText = new Text("Student Seller Workspace");
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");

        Label badge = new Label("VERIFIED SELLER");
        badge.setStyle(Theme.successBadgeStyle());
        nameBadgeRow.getChildren().addAll(titleText, badge);

        Text subText = new Text("Logged in as " + (sellerProfile != null && !sellerProfile.getName().isEmpty() ? sellerProfile.getName() : currentUser.getName()) + "  •  Manage marketplace listings and buyer requests");
        subText.setStyle(Theme.mutedTextStyle());

        titleBox.getChildren().addAll(nameBadgeRow, subText);

        Button studentPortalBtn = new Button("🎓 Switch to Student Portal");
        studentPortalBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px; -fx-font-weight: 700;");
        studentPortalBtn.setOnAction(e -> {
            System.out.println("[NAVIGATION] Switching from Seller Workspace to Student Portal");
            Main.showHomePage();
        });

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 16px;");
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Seller Logout");
            alert.setHeaderText("Logout from Seller Workspace?");
            alert.setContentText("You will be logged out of your authenticated session.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    if (onLogout != null) onLogout.run();
                    else Main.showWelcomePage();
                }
            });
        });

        headerRow.getChildren().addAll(iconBox, titleBox, studentPortalBtn, logoutBtn);

        // ─────────────────────────────────────────────────────────
        // TAB SWITCHER ROW
        // ─────────────────────────────────────────────────────────
        HBox tabRow = new HBox(8);
        tabRow.setAlignment(Pos.CENTER_LEFT);
        tabRow.setPadding(new Insets(4, 0, 8, 0));

        Button btnDashboard = new Button("📊 Dashboard");
        Button btnListings = new Button("📦 My Listings");
        Button btnAddItem = new Button("➕ Add New Item");

        List<Order> sellerOrders = DataRepository.getInstance().getOrdersForSeller(sellerUid);
        long pendingReqCount = sellerOrders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
        String reqBtnLabel = pendingReqCount > 0 ? "📩 Incoming Requests (" + pendingReqCount + ")" : "📩 Incoming Requests";
        Button btnRequests = new Button(reqBtnLabel);

        Button btnProfile = new Button("👤 Seller Profile");

        tabRow.getChildren().addAll(btnDashboard, btnListings, btnAddItem, btnRequests, btnProfile);

        // Content Area Container
        StackPane contentContainer = new StackPane();
        contentContainer.setMaxWidth(Double.MAX_VALUE);

        final Runnable[] refreshView = new Runnable[1];

        final SellerProfile activeProfile = sellerProfile;

        Runnable styleTabs = () -> {
            String activeStyle = Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 7px 14px;";
            String inactiveStyle = Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 7px 14px;";

            btnDashboard.setStyle(currentTab == SellerTab.DASHBOARD ? activeStyle : inactiveStyle);
            btnListings.setStyle(currentTab == SellerTab.MY_LISTINGS ? activeStyle : inactiveStyle);
            btnAddItem.setStyle(currentTab == SellerTab.ADD_ITEM ? activeStyle : inactiveStyle);
            btnRequests.setStyle(currentTab == SellerTab.REQUESTS ? activeStyle : inactiveStyle);
            btnProfile.setStyle(currentTab == SellerTab.PROFILE ? activeStyle : inactiveStyle);
        };

        refreshView[0] = () -> {
            styleTabs.run();
            if (currentTab == SellerTab.MY_LISTINGS) {
                contentContainer.getChildren().setAll(createMyListingsView(sellerUid, refreshView[0], () -> {
                    currentTab = SellerTab.ADD_ITEM;
                    refreshView[0].run();
                }));
            } else if (currentTab == SellerTab.ADD_ITEM) {
                contentContainer.getChildren().setAll(createAddItemView(sellerUid, activeProfile, () -> {
                    currentTab = SellerTab.MY_LISTINGS;
                    refreshView[0].run();
                }));
            } else if (currentTab == SellerTab.REQUESTS) {
                contentContainer.getChildren().setAll(createRequestsView(sellerUid, refreshView[0]));
            } else if (currentTab == SellerTab.PROFILE) {
                contentContainer.getChildren().setAll(createProfileView(activeProfile, refreshView[0]));
            } else {
                contentContainer.getChildren().setAll(createDashboardOverview(sellerUid, activeProfile,
                    () -> { currentTab = SellerTab.ADD_ITEM; refreshView[0].run(); },
                    () -> { currentTab = SellerTab.MY_LISTINGS; refreshView[0].run(); },
                    () -> { currentTab = SellerTab.REQUESTS; refreshView[0].run(); }
                ));
            }
        };

        btnDashboard.setOnAction(e -> { currentTab = SellerTab.DASHBOARD; refreshView[0].run(); });
        btnListings.setOnAction(e -> { currentTab = SellerTab.MY_LISTINGS; refreshView[0].run(); });
        btnAddItem.setOnAction(e -> { currentTab = SellerTab.ADD_ITEM; refreshView[0].run(); });
        btnRequests.setOnAction(e -> { currentTab = SellerTab.REQUESTS; refreshView[0].run(); });
        btnProfile.setOnAction(e -> { currentTab = SellerTab.PROFILE; refreshView[0].run(); });

        refreshView[0].run();

        rootBox.getChildren().addAll(headerRow, tabRow, contentContainer);

        ScrollPane scrollPane = new ScrollPane(rootBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    // ─────────────────────────────────────────────────────────────
    // 1. DASHBOARD OVERVIEW VIEW
    // ─────────────────────────────────────────────────────────────
    private Node createDashboardOverview(String sellerUid, SellerProfile profile, Runnable onNavigateAdd, Runnable onNavigateListings, Runnable onNavigateRequests) {
        VBox box = new VBox(20);
        box.setMaxWidth(Double.MAX_VALUE);

        List<ProductItem> myProducts = DataRepository.getInstance().getProductsBySeller(sellerUid);
        long activeCount = myProducts.stream().filter(p -> !"SOLD".equalsIgnoreCase(p.getStatus()) && p.isAvailable()).count();
        long soldCount = myProducts.stream().filter(p -> "SOLD".equalsIgnoreCase(p.getStatus()) || !p.isAvailable()).count();

        List<Order> myOrders = DataRepository.getInstance().getOrdersForSeller(sellerUid);
        long pendingOrders = myOrders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();

        int totalEarnings = 0;
        for (ProductItem p : myProducts) {
            if ("SOLD".equalsIgnoreCase(p.getStatus())) {
                try {
                    String clean = p.getPrice().replaceAll("[^0-9]", "");
                    if (!clean.isEmpty()) totalEarnings += Integer.parseInt(clean);
                } catch (Exception ignored) {}
            }
        }

        // Stats Row (4 Cards)
        HBox statsBox = new HBox(16);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_PACKAGE, String.valueOf(activeCount), "Active Listings", Theme.PRIMARY, onNavigateListings),
            createStatCard(IconFactory.PATH_CHECK, String.valueOf(soldCount), "Items Sold", "#2563EB", onNavigateListings),
            createStatCard(IconFactory.PATH_MESSAGE, String.valueOf(pendingOrders), "Buyer Requests", "#D97706", onNavigateRequests),
            createStatCard(IconFactory.PATH_MONEY, "₹ " + totalEarnings, "Total Revenue", "#10B981", null)
        );

        // Recent Listings Header
        HBox secRow = new HBox(12);
        secRow.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("Recent Marketplace Listings");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        HBox.setHgrow(secTitle, Priority.ALWAYS);

        Button postBtn = new Button("➕ Add New Item");
        postBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
        postBtn.setOnAction(e -> { if (onNavigateAdd != null) onNavigateAdd.run(); });

        secRow.getChildren().addAll(secTitle, postBtn);

        VBox recentContainer = new VBox(12);
        if (myProducts.isEmpty()) {
            EmptyStateNode empty = new EmptyStateNode(
                "You haven't listed any items for sale yet",
                "List your used textbooks, electronics, cycle, or hostel essentials to sell to campus peers.",
                onNavigateAdd
            );
            recentContainer.getChildren().add(empty);
        } else {
            int shown = 0;
            for (ProductItem p : myProducts) {
                recentContainer.getChildren().add(createListingCard(p, sellerUid, null));
                shown++;
                if (shown >= 4) break;
            }
        }

        box.getChildren().addAll(statsBox, secRow, recentContainer);
        return box;
    }

    // ─────────────────────────────────────────────────────────────
    // 2. MY LISTINGS VIEW (ACTIVE, SOLD, ALL)
    // ─────────────────────────────────────────────────────────────
    private Node createMyListingsView(String sellerUid, Runnable onRefresh, Runnable onNavigateAdd) {
        VBox container = new VBox(16);
        container.setMaxWidth(Double.MAX_VALUE);

        // Top Filter Bar: Status filter pills & Category filter dropdown
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Button btnAll = new Button("All Listings");
        Button btnActive = new Button("🟢 Active Listings");
        Button btnSold = new Button("🏷️ Sold Items");

        String activePill = Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;";
        String inactivePill = Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;";

        Runnable updatePills = () -> {
            btnAll.setStyle("ALL".equals(listingFilter) ? activePill : inactivePill);
            btnActive.setStyle("ACTIVE".equals(listingFilter) ? activePill : inactivePill);
            btnSold.setStyle("SOLD".equals(listingFilter) ? activePill : inactivePill);
        };
        updatePills.run();

        ComboBox<String> catDropdown = new ComboBox<>(FXCollections.observableArrayList(
            "All Categories", "Books", "Electronics", "Furniture", "Bikes", "Appliances", "Accessories", "Other"
        ));
        catDropdown.setValue(categoryFilter);
        catDropdown.setStyle(Theme.comboBoxStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addNewBtn = new Button("➕ Add New Item");
        addNewBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
        addNewBtn.setOnAction(e -> { if (onNavigateAdd != null) onNavigateAdd.run(); });

        filterBar.getChildren().addAll(btnAll, btnActive, btnSold, new Label("Category:"), catDropdown, spacer, addNewBtn);

        VBox listContainer = new VBox(12);

        Runnable renderList = () -> {
            listContainer.getChildren().clear();
            List<ProductItem> all = DataRepository.getInstance().getProductsBySeller(sellerUid);
            List<ProductItem> filtered = new ArrayList<>();

            for (ProductItem p : all) {
                boolean isSold = "SOLD".equalsIgnoreCase(p.getStatus()) || !p.isAvailable();
                if ("ACTIVE".equals(listingFilter) && isSold) continue;
                if ("SOLD".equals(listingFilter) && !isSold) continue;

                if (!"All Categories".equals(categoryFilter) && (p.getCategory() == null || !p.getCategory().equalsIgnoreCase(categoryFilter))) {
                    continue;
                }
                filtered.add(p);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode empty = new EmptyStateNode(
                    "No listings found",
                    "No items match your selected status and category filter.",
                    onNavigateAdd
                );
                listContainer.getChildren().add(empty);
            } else {
                for (ProductItem p : filtered) {
                    listContainer.getChildren().add(createListingCard(p, sellerUid, onRefresh));
                }
            }
        };

        btnAll.setOnAction(e -> { listingFilter = "ALL"; updatePills.run(); renderList.run(); });
        btnActive.setOnAction(e -> { listingFilter = "ACTIVE"; updatePills.run(); renderList.run(); });
        btnSold.setOnAction(e -> { listingFilter = "SOLD"; updatePills.run(); renderList.run(); });
        catDropdown.setOnAction(e -> { categoryFilter = catDropdown.getValue(); renderList.run(); });

        renderList.run();

        container.getChildren().addAll(filterBar, listContainer);
        return container;
    }

    private HBox createListingCard(ProductItem p, String sellerUid, Runnable onRefresh) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(Theme.cardStyle());

        boolean isSold = "SOLD".equalsIgnoreCase(p.getStatus()) || !p.isAvailable();

        // Image Thumbnail
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(80, 60);
        imgBox.setMinSize(80, 60);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");

        Image img = ImageUtil.loadImage(p.getImagePath());
        if (img != null && !img.isError()) {
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(80);
            imgView.setFitHeight(60);
            imgView.setPreserveRatio(false);
            Rectangle clip = new Rectangle(80, 60);
            clip.setArcWidth(8); clip.setArcHeight(8);
            imgView.setClip(clip);
            imgBox.getChildren().add(imgView);
        } else {
            Node icon = IconFactory.getIconNode(IconFactory.PATH_SHOPPING_BAG, Theme.PRIMARY, 22);
            imgBox.getChildren().add(icon);
        }

        // Info VBox
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Text titleText = new Text(p.getTitle());
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");

        Text priceText = new Text(p.getPrice());
        priceText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: 800;");

        Label statusLbl = new Label(isSold ? "SOLD" : "ACTIVE");
        statusLbl.setStyle(isSold ? Theme.badgeStyle() : Theme.successBadgeStyle());

        titleRow.getChildren().addAll(titleText, new Text("•"), priceText, statusLbl);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(p.getCategory() != null ? p.getCategory() : "General");
        catBadge.setStyle(Theme.badgeStyle() + " -fx-font-size: 10.5px; -fx-padding: 2px 6px;");

        Label condBadge = new Label(p.getCondition() != null ? p.getCondition() : "Good");
        condBadge.setStyle(Theme.badgeStyle() + " -fx-font-size: 10.5px; -fx-padding: 2px 6px;");

        Text locText = new Text("📍 " + p.getLocation() + "  •  Posted " + p.getTimePosted());
        locText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, condBadge, locText);
        info.getChildren().addAll(titleRow, metaRow);

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (!isSold) {
            Button markSoldBtn = new Button("Mark Sold");
            markSoldBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 11px; -fx-padding: 5px 10px;");
            markSoldBtn.setOnAction(e -> {
                p.setStatus("SOLD");
                p.setAvailable(false);
                new Thread(() -> new ProductDAOImpl().updateStatus(p.getId(), "SOLD")).start();
                DataRepository.getInstance().addOrUpdateProduct(p);
                showAlert("Item Marked as Sold", "'" + p.getTitle() + "' is now marked as Sold and will no longer appear as available to students.");
                if (onRefresh != null) onRefresh.run();
            });
            actions.getChildren().add(markSoldBtn);
        } else {
            Button relistBtn = new Button("Relist / Active");
            relistBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 11px; -fx-padding: 5px 10px;");
            relistBtn.setOnAction(e -> {
                p.setStatus("ACTIVE");
                p.setAvailable(true);
                new Thread(() -> new ProductDAOImpl().updateStatus(p.getId(), "ACTIVE")).start();
                DataRepository.getInstance().addOrUpdateProduct(p);
                showAlert("Item Relisted", "'" + p.getTitle() + "' is now active and visible to students in the marketplace.");
                if (onRefresh != null) onRefresh.run();
            });
            actions.getChildren().add(relistBtn);
        }

        Button editBtn = new Button("Edit");
        editBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 11px; -fx-padding: 5px 10px;");
        editBtn.setOnAction(e -> showEditItemDialog(p, onRefresh));

        Button delBtn = new Button("Delete");
        delBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 11px; -fx-padding: 5px 10px;");
        delBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete listing '" + p.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    new Thread(() -> new ProductDAOImpl().delete(p.getId())).start();
                    DataRepository.getInstance().removeProduct(p.getId());
                    showAlert("Listing Deleted", "Product removed from marketplace.");
                    if (onRefresh != null) onRefresh.run();
                }
            });
        });

        actions.getChildren().addAll(editBtn, delBtn);
        card.getChildren().addAll(imgBox, info, actions);
        return card;
    }

    // ─────────────────────────────────────────────────────────────
    // 3. ADD NEW ITEM FORM (Scrollable Form)
    // ─────────────────────────────────────────────────────────────
    private Node createAddItemView(String sellerUid, SellerProfile profile, Runnable onFinish) {
        VBox container = new VBox(18);
        container.setMaxWidth(760);
        container.setPadding(new Insets(24, 30, 24, 30));
        container.setStyle(Theme.elevatedCardStyle() + " -fx-background-radius: 16px;");

        Text formTitle = new Text("Post New Item to Marketplace");
        formTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");

        Text formSub = new Text("Fill in product details and upload photos. The listing will instantly appear for students in the marketplace.");
        formSub.setStyle(Theme.mutedTextStyle());

        VBox titleBox = new VBox(4, formTitle, formSub);

        // Photo Upload Section
        final File[] selectedImageFile = new File[1];
        final String[] uploadedImageUrl = new String[]{""};

        HBox photoRow = new HBox(16);
        photoRow.setAlignment(Pos.CENTER_LEFT);
        photoRow.setPadding(new Insets(8, 0, 8, 0));

        StackPane photoContainer = new StackPane();
        photoContainer.setPrefSize(90, 70);
        photoContainer.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");

        ImageView photoPreview = new ImageView();
        photoPreview.setFitWidth(90);
        photoPreview.setFitHeight(70);
        photoPreview.setPreserveRatio(false);
        Rectangle clip = new Rectangle(90, 70);
        clip.setArcWidth(10); clip.setArcHeight(10);
        photoPreview.setClip(clip);

        Runnable updatePhotoDisplay = () -> {
            photoContainer.getChildren().clear();
            if (selectedImageFile[0] != null) {
                photoPreview.setImage(new Image(selectedImageFile[0].toURI().toString()));
                photoContainer.getChildren().add(photoPreview);
            } else if (!uploadedImageUrl[0].isEmpty()) {
                Image img = ImageUtil.loadImage(uploadedImageUrl[0]);
                if (img != null && !img.isError()) {
                    photoPreview.setImage(img);
                    photoContainer.getChildren().add(photoPreview);
                } else {
                    photoContainer.getChildren().add(IconFactory.getIconNode(IconFactory.PATH_SHOPPING_BAG, Theme.PRIMARY, 30));
                }
            } else {
                photoContainer.getChildren().add(IconFactory.getIconNode(IconFactory.PATH_SHOPPING_BAG, Theme.PRIMARY, 30));
            }
        };
        updatePhotoDisplay.run();

        VBox photoActions = new VBox(6);
        Label photoLbl = new Label("Product Photo");
        photoLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 13px;");

        HBox btnBox = new HBox(8);
        Button choosePhotoBtn = new Button("📷 Choose Image");
        choosePhotoBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;");
        choosePhotoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Product Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(container.getScene().getWindow());
            if (file != null) {
                selectedImageFile[0] = file;
                updatePhotoDisplay.run();
            }
        });

        Button removePhotoBtn = new Button("✕ Remove");
        removePhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;");
        removePhotoBtn.setOnAction(e -> {
            selectedImageFile[0] = null;
            uploadedImageUrl[0] = "";
            updatePhotoDisplay.run();
        });

        btnBox.getChildren().addAll(choosePhotoBtn, removePhotoBtn);
        photoActions.getChildren().addAll(photoLbl, btnBox);
        photoRow.getChildren().addAll(photoContainer, photoActions);

        // Fields Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setMaxWidth(Double.MAX_VALUE);

        // Title
        Label titleLbl = new Label("Item Title / Name *");
        titleLbl.setStyle(formLabelStyle());
        TextField titleField = new TextField();
        titleField.setPromptText("e.g. Engineering Mathematics Textbook / Casio Scientific Calculator");
        titleField.setStyle(Theme.inputFieldStyle());

        // Category
        Label catLbl = new Label("Category *");
        catLbl.setStyle(formLabelStyle());
        ComboBox<String> catBox = new ComboBox<>(FXCollections.observableArrayList(
            "Books", "Electronics", "Furniture", "Bikes", "Appliances", "Accessories", "Other"
        ));
        catBox.setValue("Books");
        catBox.setStyle(Theme.comboBoxStyle());
        catBox.setMaxWidth(Double.MAX_VALUE);

        // Condition
        Label condLbl = new Label("Condition *");
        condLbl.setStyle(formLabelStyle());
        ComboBox<String> condBox = new ComboBox<>(FXCollections.observableArrayList(
            "New", "Like New", "Good", "Used"
        ));
        condBox.setValue("Like New");
        condBox.setStyle(Theme.comboBoxStyle());
        condBox.setMaxWidth(Double.MAX_VALUE);

        // Price
        Label priceLbl = new Label("Price (₹) *");
        priceLbl.setStyle(formLabelStyle());
        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 450 or ₹ 450");
        priceField.setStyle(Theme.inputFieldStyle());

        // Location
        Label locLbl = new Label("Location / Campus Area *");
        locLbl.setStyle(formLabelStyle());
        TextField locField = new TextField(profile != null && !profile.getLocation().isEmpty() ? profile.getLocation() : "Kothrud, Pune");
        locField.setPromptText("e.g. Kothrud, Pune");
        locField.setStyle(Theme.inputFieldStyle());

        // Contact Preference
        Label contactLbl = new Label("Contact Preference");
        contactLbl.setStyle(formLabelStyle());
        ComboBox<String> contactBox = new ComboBox<>(FXCollections.observableArrayList(
            "Mobile Call", "In-App Message", "WhatsApp", "Email"
        ));
        contactBox.setValue("Mobile Call");
        contactBox.setStyle(Theme.comboBoxStyle());
        contactBox.setMaxWidth(Double.MAX_VALUE);

        grid.add(titleLbl, 0, 0, 2, 1);
        grid.add(titleField, 0, 1, 2, 1);

        grid.add(catLbl, 0, 2);
        grid.add(catBox, 0, 3);
        grid.add(condLbl, 1, 2);
        grid.add(condBox, 1, 3);

        grid.add(priceLbl, 0, 4);
        grid.add(priceField, 0, 5);
        grid.add(locLbl, 1, 4);
        grid.add(locField, 1, 5);

        grid.add(contactLbl, 0, 6, 2, 1);
        grid.add(contactBox, 0, 7, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Description
        Label descLbl = new Label("Item Description *");
        descLbl.setStyle(formLabelStyle());
        TextArea descArea = new TextArea();
        descArea.setPromptText("Provide details about the item's condition, warranty, usage duration, and pickup availability...");
        descArea.setPrefRowCount(3);
        descArea.setStyle(Theme.inputFieldStyle());

        // Actions
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(14, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 8px 18px;");
        cancelBtn.setOnAction(e -> { if (onFinish != null) onFinish.run(); });

        Button submitBtn = new Button("✓ Publish Item to Marketplace");
        submitBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 8px 22px; -fx-font-weight: 800;");

        submitBtn.setOnAction(e -> {
            String titleVal = titleField.getText() != null ? titleField.getText().trim() : "";
            String priceVal = priceField.getText() != null ? priceField.getText().trim() : "";
            String locVal = locField.getText() != null ? locField.getText().trim() : "Pune";
            String catVal = catBox.getValue() != null ? catBox.getValue() : "General";
            String condVal = condBox.getValue() != null ? condBox.getValue() : "Good";
            String contactVal = contactBox.getValue() != null ? contactBox.getValue() : "Mobile Call";
            String descVal = descArea.getText() != null ? descArea.getText().trim() : "";

            if (titleVal.isEmpty()) {
                showAlert("Missing Title", "Please enter a title for your item.");
                return;
            }
            if (priceVal.isEmpty()) {
                showAlert("Missing Price", "Please enter the selling price.");
                return;
            }

            String formattedPrice = priceVal.startsWith("₹") ? priceVal : "₹ " + priceVal;

            // Upload image to Cloudinary if selected
            String finalImageUrl = "assets/image/laptop_dell.png";
            if ("Books".equalsIgnoreCase(catVal)) finalImageUrl = "assets/image/book_math.png";
            else if ("Furniture".equalsIgnoreCase(catVal)) finalImageUrl = "assets/image/table_study.png";
            else if ("Bikes".equalsIgnoreCase(catVal)) finalImageUrl = "assets/image/cycle_hero.png";

            String finalPublicId = "";
            if (selectedImageFile[0] != null) {
                try {
                    CloudinaryService.UploadResult res = CloudinaryService.uploadImage(selectedImageFile[0], "productImages");
                    if (res != null && res.isSuccess()) {
                        finalImageUrl = res.getSecureUrl();
                        finalPublicId = res.getPublicId();
                    } else {
                        finalImageUrl = selectedImageFile[0].getAbsolutePath();
                    }
                } catch (Exception ex) {
                    finalImageUrl = selectedImageFile[0].getAbsolutePath();
                }
            }

            String productId = "prod_" + System.currentTimeMillis();
            String sellerName = (profile != null && profile.getName() != null && !profile.getName().isEmpty()) ? profile.getName() : "Student Seller";
            String sellerPhone = (profile != null && profile.getPhone() != null && !profile.getPhone().isEmpty()) ? profile.getPhone() : "";

            ProductItem newItem = new ProductItem(
                productId,
                titleVal,
                formattedPrice,
                locVal,
                "Just now",
                catVal,
                condVal,
                descVal,
                sellerName,
                sellerPhone,
                finalImageUrl,
                finalPublicId,
                sellerUid,
                "ACTIVE",
                true,
                contactVal,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            );

            // Save to Firestore
            new Thread(() -> {
                boolean saved = new ProductDAOImpl().save(newItem);
                System.out.println("========== SELLER LISTING ==========");
                System.out.println("Firebase UID = " + sellerUid);
                System.out.println("Seller ID = " + newItem.getSellerUid());
                System.out.println("Listing ID = " + newItem.getId());
                System.out.println("Category = " + newItem.getCategory());
                System.out.println("Firebase write successful = " + saved);
                System.out.println("==========================================");
            }).start();

            // Save to DataRepository
            DataRepository.getInstance().addOrUpdateProduct(newItem);

            showAlert("Product Listed!", "'" + titleVal + "' has been successfully published to the " + catVal + " marketplace!");
            if (onFinish != null) onFinish.run();
        });

        actionsRow.getChildren().addAll(cancelBtn, submitBtn);

        container.getChildren().addAll(titleBox, photoRow, grid, descLbl, descArea, actionsRow);

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");
        return sp;
    }

    private void showEditItemDialog(ProductItem p, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Marketplace Listing");
        dialog.setHeaderText("Update details for '" + p.getTitle() + "':");

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setPrefWidth(420);

        TextField titleField = new TextField(p.getTitle());
        TextField priceField = new TextField(p.getPrice());
        TextField locField = new TextField(p.getLocation());
        TextArea descArea = new TextArea(p.getDescription());
        descArea.setPrefRowCount(3);

        ComboBox<String> condBox = new ComboBox<>(FXCollections.observableArrayList("New", "Like New", "Good", "Used"));
        condBox.setValue(p.getCondition());

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.add(new Label("Title:"), 0, 0); g.add(titleField, 1, 0);
        g.add(new Label("Price:"), 0, 1); g.add(priceField, 1, 1);
        g.add(new Label("Condition:"), 0, 2); g.add(condBox, 1, 2);
        g.add(new Label("Location:"), 0, 3); g.add(locField, 1, 3);
        g.add(new Label("Description:"), 0, 4); g.add(descArea, 1, 4);

        content.getChildren().add(g);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !titleField.getText().trim().isEmpty()) {
                p.setTitle(titleField.getText().trim());
                p.setPrice(priceField.getText().trim());
                p.setCondition(condBox.getValue());
                p.setLocation(locField.getText().trim());
                p.setDescription(descArea.getText().trim());
                p.setUpdatedAt(System.currentTimeMillis());

                new Thread(() -> new ProductDAOImpl().save(p)).start();
                DataRepository.getInstance().addOrUpdateProduct(p);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(ok -> {
            if (ok) {
                showAlert("Listing Updated", "Changes saved successfully.");
                if (onRefresh != null) onRefresh.run();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // 4. INCOMING REQUESTS VIEW
    // ─────────────────────────────────────────────────────────────
    private Node createRequestsView(String sellerUid, Runnable onRefresh) {
        VBox container = new VBox(16);
        container.setMaxWidth(Double.MAX_VALUE);

        Text secTitle = new Text("Incoming Purchase / Contact Requests");
        secTitle.setStyle(Theme.sectionHeaderStyle());

        Text secSub = new Text("Students interested in buying items you have listed for sale.");
        secSub.setStyle(Theme.mutedTextStyle());

        container.getChildren().addAll(new VBox(2, secTitle, secSub));

        List<Order> orders = DataRepository.getInstance().getOrdersForSeller(sellerUid);
        if (orders.isEmpty()) {
            List<Order> fsOrders = new OrderDAOImpl().findBySellerUid(sellerUid);
            if (fsOrders != null && !fsOrders.isEmpty()) {
                for (Order o : fsOrders) {
                    DataRepository.getInstance().addOrUpdateOrder(o);
                }
                orders = fsOrders;
            }
        }

        if (orders.isEmpty()) {
            EmptyStateNode empty = new EmptyStateNode(
                "No Incoming Buyer Requests",
                "When students browse your marketplace items and click 'Buy / Contact', their requests will appear here.",
                null
            );
            container.getChildren().add(empty);
            return container;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        for (Order o : orders) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(16, 20, 16, 20));

            String status = o.getStatus() != null ? o.getStatus() : "PENDING";
            String accentColor = "PENDING".equalsIgnoreCase(status) ? "#D97706"
                    : "ACCEPTED".equalsIgnoreCase(status) ? Theme.PRIMARY
                    : "COMPLETED".equalsIgnoreCase(status) ? "#2563EB" : "#C62828";

            card.setStyle(
                "-fx-background-color: " + Theme.CARD_BG + ";"
                + "-fx-border-color: " + accentColor + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + ";"
                + "-fx-border-width: 3px 1px 1px 1px;"
                + "-fx-border-radius: 10px;"
                + "-fx-background-radius: 10px;"
            );

            HBox top = new HBox(12);
            top.setAlignment(Pos.CENTER_LEFT);

            VBox studentInfo = new VBox(2);
            HBox.setHgrow(studentInfo, Priority.ALWAYS);

            Text studentName = new Text("👤 Student: " + (o.getStudentName() != null ? o.getStudentName() : "Student Buyer"));
            studentName.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 800;");

            Text itemLine = new Text("Requested Item: " + o.getItemName() + "   •   Price: " + o.getPrice());
            itemLine.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-weight: 700; -fx-font-size: 13px;");

            Text contactLine = new Text("Email: " + o.getStudentEmail() + "   •   Mobile: " + o.getStudentPhone() + "   •   Date: " + sdf.format(new Date(o.getCreatedAt())));
            contactLine.setStyle(Theme.mutedTextStyle());

            studentInfo.getChildren().addAll(studentName, itemLine, contactLine);

            Label badge = new Label(status);
            badge.setStyle("PENDING".equalsIgnoreCase(status) ? Theme.warningBadgeStyle() : Theme.successBadgeStyle());

            top.getChildren().addAll(studentInfo, badge);
            card.getChildren().add(top);

            if (o.getMessage() != null && !o.getMessage().trim().isEmpty()) {
                Label msgLbl = new Label("Buyer Message: \"" + o.getMessage() + "\"");
                msgLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-style: italic; -fx-font-size: 13px;");
                card.getChildren().add(msgLbl);
            }

            if ("PENDING".equalsIgnoreCase(status)) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button acceptBtn = new Button("✓ Accept Request");
                acceptBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
                acceptBtn.setOnAction(e -> {
                    o.setStatus("ACCEPTED");
                    new Thread(() -> new OrderDAOImpl().updateStatus(o.getId(), "ACCEPTED")).start();
                    DataRepository.getInstance().addOrUpdateOrder(o);
                    showAlert("Request Accepted", "You accepted the purchase request for '" + o.getItemName() + "'. You can now connect directly with the student at " + o.getStudentPhone());
                    if (onRefresh != null) onRefresh.run();
                });

                Button rejectBtn = new Button("✕ Decline");
                rejectBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
                rejectBtn.setOnAction(e -> {
                    o.setStatus("REJECTED");
                    new Thread(() -> new OrderDAOImpl().updateStatus(o.getId(), "REJECTED")).start();
                    DataRepository.getInstance().addOrUpdateOrder(o);
                    showAlert("Request Declined", "Purchase request declined.");
                    if (onRefresh != null) onRefresh.run();
                });

                Button soldBtn = new Button("🏷️ Mark as Completed & Sold");
                soldBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
                soldBtn.setOnAction(e -> {
                    o.setStatus("COMPLETED");
                    new Thread(() -> {
                        new OrderDAOImpl().updateStatus(o.getId(), "COMPLETED");
                        if (o.getProductId() != null && !o.getProductId().isEmpty()) {
                            new ProductDAOImpl().updateStatus(o.getProductId(), "SOLD");
                        }
                    }).start();

                    DataRepository.getInstance().addOrUpdateOrder(o);
                    if (o.getProductId() != null && !o.getProductId().isEmpty()) {
                        for (ProductItem p : DataRepository.getInstance().getProducts()) {
                            if (p.getId().equals(o.getProductId())) {
                                p.setStatus("SOLD");
                                p.setAvailable(false);
                                break;
                            }
                        }
                    }

                    showAlert("Deal Completed!", "'" + o.getItemName() + "' marked as Completed & Sold.");
                    if (onRefresh != null) onRefresh.run();
                });

                actions.getChildren().addAll(acceptBtn, rejectBtn, soldBtn);
                card.getChildren().add(actions);
            } else if ("ACCEPTED".equalsIgnoreCase(status)) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button completeBtn = new Button("🏷️ Mark as Completed & Sold");
                completeBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
                completeBtn.setOnAction(e -> {
                    o.setStatus("COMPLETED");
                    new Thread(() -> {
                        new OrderDAOImpl().updateStatus(o.getId(), "COMPLETED");
                        if (o.getProductId() != null && !o.getProductId().isEmpty()) {
                            new ProductDAOImpl().updateStatus(o.getProductId(), "SOLD");
                        }
                    }).start();

                    DataRepository.getInstance().addOrUpdateOrder(o);
                    if (o.getProductId() != null && !o.getProductId().isEmpty()) {
                        for (ProductItem p : DataRepository.getInstance().getProducts()) {
                            if (p.getId().equals(o.getProductId())) {
                                p.setStatus("SOLD");
                                p.setAvailable(false);
                                break;
                            }
                        }
                    }

                    showAlert("Deal Completed!", "'" + o.getItemName() + "' marked as Completed & Sold.");
                    if (onRefresh != null) onRefresh.run();
                });
                actions.getChildren().add(completeBtn);
                card.getChildren().add(actions);
            }

            container.getChildren().add(card);
        }

        return container;
    }

    // ─────────────────────────────────────────────────────────────
    // 5. SELLER PROFILE VIEW (Edit, Photo upload/remove)
    // ─────────────────────────────────────────────────────────────
    private Node createProfileView(SellerProfile profile, Runnable onRefresh) {
        VBox container = new VBox(20);
        container.setMaxWidth(760);
        container.setPadding(new Insets(24, 30, 24, 30));
        container.setStyle(Theme.elevatedCardStyle() + " -fx-background-radius: 16px;");

        Text pTitle = new Text("Seller Profile & Account Details");
        pTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");

        Text pSub = new Text("Manage your seller identity, contact details, and location for campus buyers.");
        pSub.setStyle(Theme.mutedTextStyle());

        // Photo Upload / Change / Remove Row
        HBox photoRow = new HBox(18);
        photoRow.setAlignment(Pos.CENTER_LEFT);
        photoRow.setPadding(new Insets(10, 0, 10, 0));

        final String[] currentPhotoUrl = new String[]{profile != null ? profile.getProfileImage() : ""};
        final File[] newPhotoFile = new File[1];

        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(80, 80);
        avatarContainer.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 40px;");

        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(80);
        avatarView.setFitHeight(80);
        avatarView.setPreserveRatio(false);
        Circle clip = new Circle(40, 40, 40);
        avatarView.setClip(clip);

        Runnable updateAvatarDisplay = () -> {
            avatarContainer.getChildren().clear();
            if (newPhotoFile[0] != null) {
                avatarView.setImage(new Image(newPhotoFile[0].toURI().toString()));
                avatarContainer.getChildren().add(avatarView);
            } else if (!currentPhotoUrl[0].isEmpty()) {
                Image img = ImageUtil.loadImage(currentPhotoUrl[0]);
                if (img != null && !img.isError()) {
                    avatarView.setImage(img);
                    avatarContainer.getChildren().add(avatarView);
                } else {
                    avatarContainer.getChildren().add(IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 36));
                }
            } else {
                avatarContainer.getChildren().add(IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 36));
            }
        };
        updateAvatarDisplay.run();

        VBox photoActions = new VBox(6);
        Label photoLbl = new Label("Seller Profile Photo");
        photoLbl.setStyle(formLabelStyle());

        HBox btnBox = new HBox(8);
        Button uploadPhotoBtn = new Button("📷 Choose Photo");
        uploadPhotoBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;");
        uploadPhotoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(container.getScene().getWindow());
            if (file != null) {
                newPhotoFile[0] = file;
                updateAvatarDisplay.run();
            }
        });

        Button removePhotoBtn = new Button("✕ Remove");
        removePhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;");
        removePhotoBtn.setOnAction(e -> {
            newPhotoFile[0] = null;
            currentPhotoUrl[0] = "";
            updateAvatarDisplay.run();
        });

        btnBox.getChildren().addAll(uploadPhotoBtn, removePhotoBtn);
        photoActions.getChildren().addAll(photoLbl, btnBox);
        photoRow.getChildren().addAll(avatarContainer, photoActions);

        // Fields Grid
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        grid.setMaxWidth(Double.MAX_VALUE);

        Label nameLbl = new Label("Seller Name *");
        nameLbl.setStyle(formLabelStyle());
        TextField nameField = new TextField(profile != null ? profile.getName() : "");
        nameField.setStyle(Theme.inputFieldStyle());

        Label phoneLbl = new Label("Contact Phone / Mobile *");
        phoneLbl.setStyle(formLabelStyle());
        TextField phoneField = new TextField(profile != null ? profile.getPhone() : "");
        phoneField.setStyle(Theme.inputFieldStyle());

        Label emailLbl = new Label("Email Address");
        emailLbl.setStyle(formLabelStyle());
        TextField emailField = new TextField(profile != null ? profile.getEmail() : "");
        emailField.setEditable(false);
        emailField.setStyle(Theme.inputFieldStyle() + " -fx-opacity: 0.8;");

        Label collegeLbl = new Label("College / University");
        collegeLbl.setStyle(formLabelStyle());
        TextField collegeField = new TextField(profile != null ? profile.getCollege() : "");
        collegeField.setStyle(Theme.inputFieldStyle());

        Label locLbl = new Label("Campus Location / City *");
        locLbl.setStyle(formLabelStyle());
        TextField locField = new TextField(profile != null ? profile.getLocation() : "Pune");
        locField.setStyle(Theme.inputFieldStyle());

        grid.add(nameLbl, 0, 0); grid.add(nameField, 0, 1);
        grid.add(phoneLbl, 1, 0); grid.add(phoneField, 1, 1);
        grid.add(emailLbl, 0, 2); grid.add(emailField, 0, 3);
        grid.add(collegeLbl, 1, 2); grid.add(collegeField, 1, 3);
        grid.add(locLbl, 0, 4, 2, 1); grid.add(locField, 0, 5, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        Label descLbl = new Label("Short Seller Bio / Description");
        descLbl.setStyle(formLabelStyle());
        TextArea descArea = new TextArea(profile != null ? profile.getDescription() : "");
        descArea.setPromptText("Describe what kind of student items you sell (books, accessories, gadgets)...");
        descArea.setPrefRowCount(3);
        descArea.setStyle(Theme.inputFieldStyle());

        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(14, 0, 0, 0));

        Button saveBtn = new Button("✓ Save Profile Changes");
        saveBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 8px 22px; -fx-font-weight: 800;");

        saveBtn.setOnAction(e -> {
            String nameVal = nameField.getText() != null ? nameField.getText().trim() : "";
            String phoneVal = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String locVal = locField.getText() != null ? locField.getText().trim() : "Pune";
            String collegeVal = collegeField.getText() != null ? collegeField.getText().trim() : "";
            String descVal = descArea.getText() != null ? descArea.getText().trim() : "";

            if (nameVal.isEmpty()) {
                showAlert("Missing Name", "Please enter your seller name.");
                return;
            }
            if (phoneVal.isEmpty()) {
                showAlert("Missing Phone", "Please enter your mobile phone number.");
                return;
            }

            String finalPhoto = currentPhotoUrl[0];
            String finalPublicId = profile != null ? profile.getProfilePublicId() : "";

            if (newPhotoFile[0] != null) {
                try {
                    CloudinaryService.UploadResult res = CloudinaryService.uploadImage(newPhotoFile[0], "profileImages");
                    if (res != null && res.isSuccess()) {
                        finalPhoto = res.getSecureUrl();
                        finalPublicId = res.getPublicId();
                    } else {
                        finalPhoto = newPhotoFile[0].getAbsolutePath();
                    }
                } catch (Exception ex) {
                    finalPhoto = newPhotoFile[0].getAbsolutePath();
                }
            }

            if (profile != null) {
                profile.setName(nameVal);
                profile.setPhone(phoneVal);
                profile.setLocation(locVal);
                profile.setCollege(collegeVal);
                profile.setDescription(descVal);
                profile.setProfileImage(finalPhoto);
                profile.setProfilePublicId(finalPublicId);
                profile.setUpdatedAt(System.currentTimeMillis());

                new Thread(() -> new SellerDAOImpl().save(profile)).start();
                DataRepository.getInstance().addOrUpdateSeller(profile);

                User curUser = DataRepository.getInstance().getCurrentUser();
                if (curUser != null) {
                    curUser.setName(nameVal);
                    curUser.setPhone(phoneVal);
                    curUser.setCollege(collegeVal);
                    curUser.setProfileImage(finalPhoto);
                    curUser.setProfilePublicId(finalPublicId);
                    new Thread(() -> new UserDAOImpl().save(curUser)).start();
                }
            }

            showAlert("Profile Saved", "Your seller profile details have been updated successfully!");
            if (onRefresh != null) onRefresh.run();
        });

        actionsRow.getChildren().add(saveBtn);
        container.getChildren().addAll(new VBox(4, pTitle, pSub), photoRow, grid, descLbl, descArea, actionsRow);
        return container;
    }

    private VBox createStatCard(String iconPath, String value, String title, String accentColor, Runnable onClick) {
        VBox b = new VBox(8);
        b.setPrefWidth(210);
        b.setPadding(new Insets(18));
        b.setStyle(Theme.statCardStyle(accentColor));
        if (onClick != null) {
            b.setOnMouseClicked(e -> onClick.run());
            b.setStyle(Theme.statCardStyle(accentColor) + " -fx-cursor: hand;");
        }

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(38, 38);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Node iconNode = IconFactory.getIconNode(iconPath, accentColor, 18);
        iconBadge.getChildren().add(iconNode);
        topRow.getChildren().add(iconBadge);

        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");

        Text lblTxt = new Text(title);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 600;");

        b.getChildren().addAll(topRow, valTxt, lblTxt);
        return b;
    }

    private String formLabelStyle() {
        return "-fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";";
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Scene getPageScene(Runnable onPostItem, Runnable onLogout) {
        Node node = getPageNode(onPostItem, onLogout);
        BorderPane rootPane = new BorderPane(node);
        rootPane.setStyle(Theme.rootPaneStyle());
        sellerScene = new Scene(rootPane, 1050, 700);
        return sellerScene;
    }
}
