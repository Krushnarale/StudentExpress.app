package com.core2web.view.services;

import com.core2web.Main;
import com.core2web.model.ServiceItem;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import java.util.*;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class ServicesPage {

    private Scene servicesScene;
    private String selectedCategory = null; // null = Show Categories Grid; non-null = Show Providers of Category

    public static class CategoryInfo {
        public final String id;
        public final String name;
        public final String icon;
        public final String tagline;
        public final String startingPrice;
        public final String description;

        public CategoryInfo(String id, String name, String icon, String tagline, String startingPrice, String description) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.tagline = tagline;
            this.startingPrice = startingPrice;
            this.description = description;
        }
    }

    public static final List<CategoryInfo> CATEGORIES = Arrays.asList(
        new CategoryInfo(
            "Laundry",
            "Laundry",
            "🧺",
            "Doorstep wash, dry clean & steam ironing",
            "From ₹ 15 / kg",
            "Convenient doorstep laundry pickup, washing, drying, stain removal and hygienic steam ironing."
        ),
        new CategoryInfo(
            "Tiffin / Mess",
            "Tiffin / Mess",
            "🍱",
            "Hygienic daily home-cooked tiffins & meal plans",
            "From ₹ 2,500 / month",
            "Nutritious, authentic home-cooked North & South Indian meals delivered right to your PG or hostel."
        ),
        new CategoryInfo(
            "Cleaning",
            "Cleaning",
            "🧹",
            "Deep room cleaning, bathroom sanitation & housekeeping",
            "From ₹ 299 / session",
            "Professional room scrubbing, floor sanitization, bathroom deep cleaning and regular housekeeping."
        ),
        new CategoryInfo(
            "Wi-Fi",
            "Wi-Fi",
            "📶",
            "High-speed fiber broadband & Wi-Fi router setup",
            "From ₹ 399 / month",
            "Dedicated high-speed fiber internet connection and quick router installation for student apartments."
        ),
        new CategoryInfo(
            "Repair & Maintenance",
            "Repair & Maintenance",
            "🛠️",
            "Electrical, plumbing, appliance repair & tech fixes",
            "From ₹ 149 / visit",
            "On-demand electrician, plumbing, fan, water cooler, geyser and laptop hardware repairs."
        )
    );

    public Node getPageNode(
        Consumer<ServiceItem> onSelectService,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateProfile
    ) {
        StackPane rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: transparent;");

        Runnable refreshView = new Runnable() {
            @Override
            public void run() {
                rootContainer.getChildren().clear();
                if (selectedCategory == null) {
                    rootContainer.getChildren().add(buildCategoriesView(onSelectService, this));
                } else {
                    rootContainer.getChildren().add(buildProvidersView(selectedCategory, onSelectService, this));
                }
            }
        };

        refreshView.run();
        return rootContainer;
    }

    /**
     * MAIN VIEW: Displays all 5 Service Categories
     */
    private Node buildCategoriesView(Consumer<ServiceItem> onSelectService, Runnable refreshParent) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(24, 36, 30, 36));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        // Header Box
        VBox headerBox = new VBox(6);
        Text titleText = new Text("Student Campus Services & Support");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Select a service category to discover verified service providers near your college campus.");
        subtitleText.setStyle(Theme.mutedTextStyle());
        headerBox.getChildren().addAll(titleText, subtitleText);

        // Search Bar (searches categories or directly finds providers)
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search for Laundry, Tiffin / Mess, Cleaning, Wi-Fi, Repairs, or provider name...");
        searchField.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchRow.getChildren().addAll(searchField);

        // Categories Grid
        FlowPane categoriesGrid = new FlowPane(20, 20);
        categoriesGrid.setPadding(new Insets(10, 0, 16, 0));
        categoriesGrid.setMaxWidth(Double.MAX_VALUE);

        Runnable renderCategories = () -> {
            categoriesGrid.getChildren().clear();
            String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

            for (CategoryInfo cat : CATEGORIES) {
                // If search query is provided, check if category matches OR if any provider under it matches
                boolean matchesCategory = q.isEmpty()
                    || cat.name.toLowerCase().contains(q)
                    || cat.tagline.toLowerCase().contains(q)
                    || cat.description.toLowerCase().contains(q);

                List<ServiceItem> providersInCat = getProvidersForCategory(cat.name);
                boolean matchesProvider = false;
                if (!matchesCategory && !q.isEmpty()) {
                    for (ServiceItem s : providersInCat) {
                        if (s.getTitle().toLowerCase().contains(q)
                            || (s.getProviderName() != null && s.getProviderName().toLowerCase().contains(q))
                            || (s.getDescription() != null && s.getDescription().toLowerCase().contains(q))) {
                            matchesProvider = true;
                            break;
                        }
                    }
                }

                if (matchesCategory || matchesProvider) {
                    VBox card = createCategoryCard(cat, providersInCat.size(), () -> {
                        this.selectedCategory = cat.name;
                        refreshParent.run();
                    });
                    categoriesGrid.getChildren().add(card);
                }
            }

            if (categoriesGrid.getChildren().isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Service Categories Match Your Search",
                    "Try searching for 'Laundry', 'Tiffin', 'Cleaning', 'Wi-Fi', or 'Repair'.",
                    () -> {
                        searchField.clear();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                categoriesGrid.getChildren().add(emptyState);
            }
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCategories.run());
        renderCategories.run();

        // Trust & Guarantee Banner
        VBox trustBanner = createTrustBanner();

        mainContent.getChildren().addAll(headerBox, searchRow, categoriesGrid, trustBanner);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    /**
     * CATEGORY CARD: Modern card representing a Service Category
     */
    private VBox createCategoryCard(CategoryInfo cat, int providerCount, Runnable onSelect) {
        VBox card = new VBox(14);
        card.setPrefWidth(330);
        card.setMinWidth(300);
        card.setMaxWidth(360);
        card.setPadding(new Insets(24, 22, 22, 22));
        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 16px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
            + "-fx-cursor: hand;"
        );

        // Top Row: Icon badge + Provider Count Tag
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(52, 52);
        iconBadge.setMinSize(52, 52);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 14px;");
        Text iconTxt = new Text(cat.icon);
        iconTxt.setStyle("-fx-font-size: 26px;");
        iconBadge.getChildren().add(iconTxt);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countBadge = new Label(providerCount + (providerCount == 1 ? " Provider" : " Providers"));
        countBadge.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-text-fill: " + Theme.PRIMARY_DARK + ";"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11.5px;"
            + "-fx-font-weight: 700;"
            + "-fx-padding: 4px 10px;"
            + "-fx-background-radius: 20px;"
        );

        topRow.getChildren().addAll(iconBadge, spacer, countBadge);

        // Title & Tagline
        VBox textBlock = new VBox(4);
        Text titleTxt = new Text(cat.name);
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 18px; -fx-font-weight: 800;");

        Text tagTxt = new Text(cat.tagline);
        tagTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px; -fx-line-spacing: 2px;");
        tagTxt.setWrappingWidth(280);

        textBlock.getChildren().addAll(titleTxt, tagTxt);

        // Price Row
        HBox priceRow = new HBox(6);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Text startingTxt = new Text(cat.startingPrice);
        startingTxt.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 800;");
        priceRow.getChildren().add(startingTxt);

        // Explore Button
        Button exploreBtn = new Button("Explore " + cat.name + " Providers →");
        exploreBtn.setMaxWidth(Double.MAX_VALUE);
        exploreBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12.5px; -fx-padding: 9px; -fx-background-radius: 10px;");
        exploreBtn.setOnAction(e -> onSelect.run());

        card.getChildren().addAll(topRow, textBlock, priceRow, exploreBtn);

        // Click whole card to navigate
        card.setOnMouseClicked(e -> onSelect.run());

        // Hover Effect
        String defaultStyle = card.getStyle();
        String hoverStyle =
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: " + Theme.PRIMARY + ";"
            + "-fx-border-radius: 16px;"
            + "-fx-effect: dropshadow(gaussian, rgba(79, 119, 45, 0.20), 16, 0, 0, 6);"
            + "-fx-translate-y: -3px;"
            + "-fx-cursor: hand;";

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        return card;
    }

    /**
     * PROVIDERS VIEW: Displays all Providers under a chosen Category
     */
    private Node buildProvidersView(String categoryName, Consumer<ServiceItem> onSelectService, Runnable refreshParent) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(22, 36, 30, 36));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        CategoryInfo catInfo = getCategoryInfo(categoryName);
        String catIcon = catInfo != null ? catInfo.icon : "🛠️";
        String catDesc = catInfo != null ? catInfo.description : "Verified student service providers.";

        // Top Navigation & Breadcrumbs Row
        HBox topNavRow = new HBox(14);
        topNavRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back to Service Categories");
        backBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 12.5px; -fx-padding: 7px 14px; -fx-font-weight: 700;");
        backBtn.setOnAction(e -> {
            this.selectedCategory = null;
            refreshParent.run();
        });

        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);

        Label activeCategoryPill = new Label(catIcon + "  Category: " + categoryName);
        activeCategoryPill.setStyle(Theme.badgeStyle() + " -fx-font-size: 12px; -fx-padding: 6px 12px;");

        topNavRow.getChildren().addAll(backBtn, navSpacer, activeCategoryPill);

        // Header Section
        HBox catHeader = new HBox(16);
        catHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane bigIconBadge = new StackPane();
        bigIconBadge.setPrefSize(56, 56);
        bigIconBadge.setMinSize(56, 56);
        bigIconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 16px;");
        Text bigIconTxt = new Text(catIcon);
        bigIconTxt.setStyle("-fx-font-size: 28px;");
        bigIconBadge.getChildren().add(bigIconTxt);

        VBox titleBlock = new VBox(4);
        Text secTitle = new Text(categoryName + " Service Providers");
        secTitle.setStyle(Theme.titleTextStyle());

        Text secSub = new Text(catDesc);
        secSub.setStyle(Theme.mutedTextStyle());
        titleBlock.getChildren().addAll(secTitle, secSub);

        catHeader.getChildren().addAll(bigIconBadge, titleBlock);

        // Horizontal Category Switcher Pills
        HBox categoryPillsRow = new HBox(10);
        categoryPillsRow.setAlignment(Pos.CENTER_LEFT);

        for (CategoryInfo cat : CATEGORIES) {
            boolean isCurrent = cat.name.equalsIgnoreCase(categoryName);
            Button pill = new Button(cat.icon + " " + cat.name);
            pill.setStyle(Theme.filterPillStyle(isCurrent));
            pill.setOnAction(e -> {
                this.selectedCategory = cat.name;
                refreshParent.run();
            });
            categoryPillsRow.getChildren().add(pill);
        }

        // Search within category
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search " + categoryName + " providers by name, features, or location...");
        searchField.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterBar.getChildren().addAll(searchField);

        // Providers Grid / List
        FlowPane providersGrid = new FlowPane(18, 18);
        providersGrid.setPadding(new Insets(8, 0, 16, 0));
        providersGrid.setMaxWidth(Double.MAX_VALUE);

        Runnable renderProviders = () -> {
            providersGrid.getChildren().clear();
            String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
            List<ServiceItem> allCatProviders = getProvidersForCategory(categoryName);
            List<ServiceItem> filtered = new ArrayList<>();

            for (ServiceItem s : allCatProviders) {
                if (q.isEmpty()) {
                    filtered.add(s);
                } else {
                    boolean matches = s.getTitle().toLowerCase().contains(q)
                        || (s.getProviderName() != null && s.getProviderName().toLowerCase().contains(q))
                        || (s.getSubtitle() != null && s.getSubtitle().toLowerCase().contains(q))
                        || (s.getDescription() != null && s.getDescription().toLowerCase().contains(q));
                    if (matches) {
                        filtered.add(s);
                    }
                }
            }

            for (ServiceItem provider : filtered) {
                VBox providerCard = createProviderCard(provider, onSelectService);
                providersGrid.getChildren().add(providerCard);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No " + categoryName + " Providers Found",
                    "No providers match your filter in this category. Try clearing your search query.",
                    () -> {
                        searchField.clear();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                providersGrid.getChildren().add(emptyState);
            }
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderProviders.run());
        renderProviders.run();

        mainContent.getChildren().addAll(topNavRow, catHeader, categoryPillsRow, filterBar, providersGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    /**
     * PROVIDER CARD: Shows an individual Service Provider under a category
     */
    private VBox createProviderCard(ServiceItem s, Consumer<ServiceItem> onSelectService) {
        VBox card = new VBox(12);
        double cardWidth = 320;
        card.setPrefWidth(cardWidth);
        card.setMinWidth(290);
        card.setMaxWidth(350);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 16px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        // Top Row: Provider Icon & Rating Badge
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(44, 44);
        iconBox.setMinSize(44, 44);
        iconBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");
        Text iconTxt = new Text(s.getIcon() != null && !s.getIcon().isEmpty() ? s.getIcon() : "🛠️");
        iconTxt.setStyle("-fx-font-size: 22px;");
        iconBox.getChildren().add(iconTxt);

        VBox provNameBox = new VBox(2);
        String provName = (s.getProviderName() != null && !s.getProviderName().trim().isEmpty() && !s.getProviderName().equals("Not provided"))
            ? s.getProviderName().trim()
            : "Campus Service Provider";
        Label provLbl = new Label(provName);
        provLbl.setMaxWidth(160);
        provLbl.setTextOverrun(OverrunStyle.ELLIPSIS);
        provLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 800;");

        HBox ratingRow = new HBox(4);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node star = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 12);
        Text ratingTxt = new Text("4.9 · Verified Partner");
        ratingTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        ratingRow.getChildren().addAll(star, ratingTxt);
        provNameBox.getChildren().addAll(provLbl, ratingRow);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label verifiedBadge = new Label("VERIFIED");
        verifiedBadge.setStyle(Theme.successBadgeStyle() + " -fx-font-size: 10px; -fx-padding: 3px 7px;");

        topRow.getChildren().addAll(iconBox, provNameBox, topSpacer, verifiedBadge);

        // Service Title & Description
        VBox bodyBox = new VBox(4);
        Label titleLbl = new Label(s.getTitle());
        titleLbl.setMaxWidth(280);
        titleLbl.setTextOverrun(OverrunStyle.ELLIPSIS);
        titleLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 700;");

        String subDesc = s.getSubtitle() != null && !s.getSubtitle().isEmpty() ? s.getSubtitle() : s.getDescription();
        Text descTxt = new Text(subDesc != null ? subDesc : "Doorstep campus service for students");
        descTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-line-spacing: 2px;");
        descTxt.setWrappingWidth(270);

        bodyBox.getChildren().addAll(titleLbl, descTxt);

        // Key Features List
        VBox featuresBox = new VBox(3);
        featuresBox.getChildren().addAll(
            createFeatureBullet("✓ Doorstep Pickup & Service Delivery"),
            createFeatureBullet("✓ Verified Student Express Partner")
        );

        // Price & Actions Row
        HBox footerRow = new HBox(8);
        footerRow.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(1);
        Text rateLbl = new Text("Rate / Price");
        rateLbl.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 10.5px;");
        Text priceText = new Text(s.getPrice() != null ? s.getPrice() : "Contact for Rate");
        priceText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 800;");
        priceBox.getChildren().addAll(rateLbl, priceText);

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        Button viewDetailsBtn = new Button("View Details →");
        viewDetailsBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 7px 12px; -fx-background-radius: 8px;");
        viewDetailsBtn.setOnAction(e -> {
            if (onSelectService != null) onSelectService.accept(s);
        });

        footerRow.getChildren().addAll(priceBox, actionSpacer, viewDetailsBtn);

        card.getChildren().addAll(topRow, bodyBox, featuresBox, footerRow);

        // Hover Effect
        String defaultStyle = card.getStyle();
        String hoverStyle =
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: " + Theme.PRIMARY + ";"
            + "-fx-border-radius: 16px;"
            + "-fx-effect: dropshadow(gaussian, rgba(79, 119, 45, 0.16), 14, 0, 0, 5);"
            + "-fx-translate-y: -2px;";

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        return card;
    }

    private HBox createFeatureBullet(String text) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        Text t = new Text(text);
        t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        row.getChildren().add(t);
        return row;
    }

    private VBox createTrustBanner() {
        VBox banner = new VBox(8);
        banner.setPadding(new Insets(18, 24, 18, 24));
        banner.setStyle(
            "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
            + "-fx-background-radius: 14px;"
            + "-fx-border-color: rgba(79, 119, 45, 0.25);"
            + "-fx-border-radius: 14px;"
        );

        Text bannerTitle = new Text("🛡️ Verified Campus Service Guarantee");
        bannerTitle.setStyle("-fx-fill: " + Theme.PRIMARY_DARK + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 800;");

        Text bannerSub = new Text("All service providers are ID-verified. Enjoy transparent student pricing, on-time delivery, and full in-app booking support.");
        bannerSub.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px;");

        banner.getChildren().addAll(bannerTitle, bannerSub);
        return banner;
    }

    /**
     * Retrieves all providers matching a given category name dynamically from DataRepository.
     * Excludes any removed categories (Printing, Transport).
     */
    public static List<ServiceItem> getProvidersForCategory(String categoryName) {
        List<ServiceItem> result = new ArrayList<>();
        if (categoryName == null || categoryName.trim().isEmpty()) return result;

        List<ServiceItem> allServices = DataRepository.getInstance().getServices();
        for (ServiceItem s : allServices) {
            // Strictly exclude Printing and Transport
            if (isRemovedCategory(s)) {
                continue;
            }

            if (matchesCategory(s, categoryName)) {
                result.add(s);
            }
        }
        return result;
    }

    public static boolean isRemovedCategory(ServiceItem s) {
        if (s == null) return false;
        String cat = (s.getCategory() != null ? s.getCategory() : "").toLowerCase();
        String title = (s.getTitle() != null ? s.getTitle() : "").toLowerCase();
        return cat.contains("print") || cat.contains("xerox") || cat.contains("transport")
            || title.contains("print") || title.contains("xerox") || title.contains("transport");
    }

    public static boolean matchesCategory(ServiceItem s, String targetCategory) {
        if (s == null || targetCategory == null) return false;
        String filter = targetCategory.toLowerCase().trim();
        String itemCat = (s.getCategory() != null ? s.getCategory() : "").toLowerCase().trim();
        String itemTitle = (s.getTitle() != null ? s.getTitle() : "").toLowerCase().trim();
        String itemSub = (s.getSubtitle() != null ? s.getSubtitle() : "").toLowerCase().trim();

        if (filter.contains("laundry")) {
            return itemCat.contains("laundr") || itemCat.contains("wash") || itemCat.contains("iron")
                || itemTitle.contains("laundry") || itemTitle.contains("wash") || itemTitle.contains("iron");
        }
        if (filter.contains("tiffin") || filter.contains("mess")) {
            return itemCat.contains("tiffin") || itemCat.contains("mess") || itemCat.contains("meal") || itemCat.contains("food")
                || itemTitle.contains("tiffin") || itemTitle.contains("mess") || itemTitle.contains("meal") || itemTitle.contains("food");
        }
        if (filter.contains("clean")) {
            return itemCat.contains("clean") || itemCat.contains("maid") || itemCat.contains("housekeep")
                || itemTitle.contains("clean") || itemTitle.contains("maid") || itemTitle.contains("housekeep");
        }
        if (filter.contains("wi-fi") || filter.contains("wifi")) {
            return itemCat.contains("wi-fi") || itemCat.contains("wifi") || itemCat.contains("internet") || itemCat.contains("broadband")
                || itemTitle.contains("wi-fi") || itemTitle.contains("wifi") || itemTitle.contains("fiber") || itemTitle.contains("internet") || itemTitle.contains("broadband");
        }
        if (filter.contains("repair") || filter.contains("maintenance")) {
            return itemCat.contains("repair") || itemCat.contains("maintenance") || itemCat.contains("appliance") || itemCat.contains("electric") || itemCat.contains("plumb")
                || itemTitle.contains("repair") || itemTitle.contains("fix") || itemTitle.contains("plumb") || itemTitle.contains("electric") || itemTitle.contains("appliance");
        }

        return itemCat.equals(filter) || itemCat.contains(filter) || itemTitle.contains(filter) || itemSub.contains(filter);
    }

    private CategoryInfo getCategoryInfo(String name) {
        for (CategoryInfo cat : CATEGORIES) {
            if (cat.name.equalsIgnoreCase(name)) return cat;
        }
        return null;
    }

    public Scene getPageScene(
        Consumer<ServiceItem> onSelectService,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateProfile
    ) {
        Node node = getPageNode(onSelectService, onNavigateHome, onNavigateRent, onNavigateBuySell, onNavigateRoommates, onNavigateProfile);
        servicesScene = new Scene(new BorderPane(node), 1050, 700);
        return servicesScene;
    }
}
