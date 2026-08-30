package com.core2web.view.marketplace;

import com.core2web.Main;
import com.core2web.model.ProductItem;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import com.core2web.view.component.ListingCardNode;
import java.util.*;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class BuySellPage {

    private Scene buySellScene;

    private ComboBox<String> categoryCombo;
    private ComboBox<String> conditionCombo;
    private ComboBox<String> sortCombo;

    public Node getPageNode(
        Consumer<ProductItem> onSelectProduct,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigatePostItem,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices,
        Runnable onNavigateProfile
    ) {
        VBox mainContent = new VBox(18);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        Text titleText = new Text("Buy & Sell - Student Marketplace");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Buy, sell, and trade pre-owned textbooks, laptops, electronics, cycles, and student essentials.");
        subtitleText.setStyle(Theme.mutedTextStyle());

        VBox headerBox = new VBox(4, titleText, subtitleText);

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().add(headerBox);
        HBox.setHgrow(headerBox, Priority.ALWAYS);

        com.core2web.model.User curUser = DataRepository.getInstance().getCurrentUser();
        String uid = (curUser != null && curUser.getUid() != null) ? curUser.getUid() : "";
        boolean isSeller = (curUser != null && curUser.isSellerEnabled()) || (DataRepository.getInstance().getSellerProfile(uid) != null);

        Button sellerBtn = new Button(isSeller ? "🛍️ Seller Workspace" : "🛍️ Register as Seller");
        sellerBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 7px 14px;");
        sellerBtn.setOnAction(e -> {
            if (isSeller) {
                Main.showSellerDashboard();
            } else {
                Main.showProfilePage();
            }
        });

        Button postBtn = new Button("➕ Post Listing");
        postBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 7px 14px;");
        postBtn.setOnAction(e -> {
            if (isSeller) {
                Main.showSellerDashboard();
            } else if (onNavigatePostItem != null) {
                onNavigatePostItem.run();
            }
        });

        headerRow.getChildren().addAll(sellerBtn, postBtn);

        // Category Pills Bar
        FlowPane categoriesBox = new FlowPane(8, 8);
        categoriesBox.setAlignment(Pos.CENTER_LEFT);
        String[] cats = {"All Items", "Books", "Electronics", "Furniture", "Gym & Fitness", "Appliances", "Cycles", "Fashion"};

        // Inline Filters Bar
        FlowPane filterRow = new FlowPane(10, 10);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList("All Categories", "Books", "Electronics", "Furniture", "Gym & Fitness", "Appliances", "Cycles", "Fashion"));
        categoryCombo.getSelectionModel().select(0);
        categoryCombo.setStyle(Theme.comboBoxStyle());

        conditionCombo = new ComboBox<>(FXCollections.observableArrayList("All Conditions", "Brand New", "Like New", "Good Condition", "Used"));
        conditionCombo.getSelectionModel().select(0);
        conditionCombo.setStyle(Theme.comboBoxStyle());

        sortCombo = new ComboBox<>(FXCollections.observableArrayList("Sort: Latest", "Price: Low to High", "Price: High to Low"));
        sortCombo.getSelectionModel().select(0);
        sortCombo.setStyle(Theme.comboBoxStyle());

        Button resetBtn = new Button("✕ Reset");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E53E3E; -fx-font-weight: 700; -fx-font-size: 12px; -fx-cursor: hand;");

        filterRow.getChildren().addAll(new Label("Filters:"), categoryCombo, conditionCombo, sortCombo, resetBtn);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search products, books, gadgets...");
        searchField.setStyle(Theme.searchFieldStyle());

        // Products Grid
        FlowPane productsGrid = new FlowPane(16, 16);
        productsGrid.setPadding(new Insets(10, 0, 10, 0));

        Text countText = new Text();
        countText.setStyle(Theme.mutedTextStyle());

        Runnable refreshList = () -> {
            productsGrid.getChildren().clear();
            List<ProductItem> filtered = getFilteredProducts(searchField.getText());

            for (ProductItem p : filtered) {
                String badge = "AVAILABLE";
                if (p.getCondition().contains("New")) badge = "VERIFIED";
                else if (p.getPrice().contains("250") || p.getPrice().contains("400")) badge = "POPULAR";

                ListingCardNode card = new ListingCardNode(
                    p.getId(),
                    ListingCardNode.CardType.PRODUCT,
                    badge,
                    p.getTitle(),
                    p.getLocation(),
                    p.getPrice(),
                    p.getTimePosted(),
                    p.getImagePath(),
                    p.getCategory(),
                    () -> { if (onSelectProduct != null) onSelectProduct.accept(p); }
                );
                productsGrid.getChildren().add(card);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Marketplace Products Found",
                    "Try choosing another category or clearing your filters.",
                    () -> {
                        categoryCombo.getSelectionModel().select(0);
                        conditionCombo.getSelectionModel().select(0);
                        sortCombo.getSelectionModel().select(0);
                        searchField.clear();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                productsGrid.getChildren().add(emptyState);
            }

            countText.setText("Showing " + filtered.size() + " of " + DataRepository.getInstance().getProducts().size() + " products");
        };

        for (String c : cats) {
            Button catBtn = new Button(c);
            boolean isAll = c.equals("All Items");
            catBtn.setStyle(Theme.filterPillStyle(isAll));
            catBtn.setOnAction(e -> {
                String selected = c.equals("All Items") ? "All Categories" : c;
                categoryCombo.getSelectionModel().select(selected);
                for (Node n : categoriesBox.getChildren()) {
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        b.setStyle(Theme.filterPillStyle(b.getText().equals(c)));
                    }
                }
            });
            categoriesBox.getChildren().add(catBtn);
        }

        categoryCombo.setOnAction(e -> refreshList.run());
        conditionCombo.setOnAction(e -> refreshList.run());
        sortCombo.setOnAction(e -> refreshList.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList.run());

        resetBtn.setOnAction(e -> {
            categoryCombo.getSelectionModel().select(0);
            conditionCombo.getSelectionModel().select(0);
            sortCombo.getSelectionModel().select(0);
            searchField.clear();
            refreshList.run();
        });

        refreshList.run();

        mainContent.getChildren().addAll(headerRow, categoriesBox, filterRow, countText, productsGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private List<ProductItem> getFilteredProducts(String searchQuery) {
        List<ProductItem> result = new ArrayList<>();
        String query = searchQuery != null ? searchQuery.toLowerCase().trim() : "";

        String selectedCategory = categoryCombo != null && categoryCombo.getValue() != null ? categoryCombo.getValue() : "All Categories";
        String selectedCondition = conditionCombo != null && conditionCombo.getValue() != null ? conditionCombo.getValue() : "All Conditions";
        String selectedSort = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: Latest";

        for (ProductItem p : DataRepository.getInstance().getProducts()) {
            if ("SOLD".equalsIgnoreCase(p.getStatus()) || !p.isAvailable()) {
                continue;
            }

            if (!selectedCategory.equals("All Categories")) {
                if (p.getCategory() == null) continue;
                String cat = p.getCategory().toLowerCase().trim();
                String sel = selectedCategory.toLowerCase().trim();
                if (!cat.equals(sel) && !cat.contains(sel) && !sel.contains(cat)) {
                    continue;
                }
            }

            if (!selectedCondition.equals("All Conditions")) {
                if (p.getCondition() == null) continue;
                String cond = p.getCondition().toLowerCase().trim();
                String selCond = selectedCondition.toLowerCase().trim();
                if (!cond.contains(selCond) && !selCond.contains(cond)) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchTitle = p.getTitle() != null && p.getTitle().toLowerCase().contains(query);
                boolean matchCat = p.getCategory() != null && p.getCategory().toLowerCase().contains(query);
                boolean matchLoc = p.getLocation() != null && p.getLocation().toLowerCase().contains(query);
                boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(query);
                if (!matchTitle && !matchCat && !matchLoc && !matchDesc) continue;
            }

            result.add(p);
        }

        if (selectedSort.equals("Price: Low to High")) {
            result.sort(Comparator.comparingInt(a -> parsePrice(a.getPrice())));
        } else if (selectedSort.equals("Price: High to Low")) {
            result.sort((a, b) -> Integer.compare(parsePrice(b.getPrice()), parsePrice(a.getPrice())));
        }

        return result;
    }

    private int parsePrice(String priceStr) {
        try {
            String clean = priceStr.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0 : Integer.parseInt(clean);
        } catch (Exception e) { return 0; }
    }

    public Scene getPageScene(
        Consumer<ProductItem> onSelectProduct,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigatePostItem,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices,
        Runnable onNavigateProfile
    ) {
        Node node = getPageNode(onSelectProduct, onNavigateHome, onNavigateRent, onNavigatePostItem, onNavigateRoommates, onNavigateServices, onNavigateProfile);
        buySellScene = new Scene(new BorderPane(node), 1050, 700);
        return buySellScene;
    }
}
