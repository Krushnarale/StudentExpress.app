package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.ProductController;
import com.core2web.model.ProductItem;
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
    private final ProductController productController = new ProductController();

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

        conditionCombo = new ComboBox<>(FXCollections.observableArrayList("All Conditions", "Like New", "Used - Good", "Fair"));
        conditionCombo.getSelectionModel().select(0);
        conditionCombo.setStyle(Theme.comboBoxStyle());

        sortCombo = new ComboBox<>(FXCollections.observableArrayList("Sort: Latest", "Price: Low to High", "Price: High to Low"));
        sortCombo.getSelectionModel().select(0);
        sortCombo.setStyle(Theme.comboBoxStyle());

        Button resetBtn = new Button("✕ Reset");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E53E3E; -fx-font-weight: 700; -fx-font-size: 12px; -fx-cursor: hand;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search products...");
        searchField.setStyle(Theme.searchFieldStyle());

        filterRow.getChildren().addAll(new Label("Filters:"), categoryCombo, conditionCombo, sortCombo, searchField, resetBtn);

        // Products Grid
        FlowPane productsGrid = new FlowPane(16, 16);
        productsGrid.setPadding(new Insets(10, 0, 10, 0));

        Text countText = new Text();
        countText.setStyle(Theme.mutedTextStyle());

        Runnable refreshList = () -> {
            productsGrid.getChildren().clear();
            List<ProductItem> filtered = getFilteredProducts(searchField.getText());

            for (ProductItem p : filtered) {
                String badge = "VERIFIED";
                if (p.getPrice().contains("450") || p.getPrice().contains("350")) badge = "BESTSELLER";

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
                    "Try selecting another category, condition, or resetting filters.",
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

            countText.setText("Showing " + filtered.size() + " of " + productController.getAllProducts().size() + " products");
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

        mainContent.getChildren().addAll(headerBox, categoriesBox, filterRow, countText, productsGrid);

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

        for (ProductItem p : productController.getAllProducts()) {
            if (!selectedCategory.equals("All Categories") && !p.getCategory().equalsIgnoreCase(selectedCategory)) {
                continue;
            }

            if (!selectedCondition.equals("All Conditions") && !p.getCondition().equalsIgnoreCase(selectedCondition)) {
                continue;
            }

            if (!query.isEmpty()) {
                boolean matchTitle = p.getTitle().toLowerCase().contains(query);
                boolean matchCat = p.getCategory().toLowerCase().contains(query);
                boolean matchLoc = p.getLocation().toLowerCase().contains(query);
                if (!matchTitle && !matchCat && !matchLoc) continue;
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
