package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.RoomController;
import com.core2web.model.RoomItem;
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

public class RentPage {

    private Scene rentScene;
    private final RoomController roomController = new RoomController();

    // Inline Filter states
    private ComboBox<String> locationCombo;
    private ComboBox<String> typeCombo;
    private ComboBox<String> budgetCombo;
    private ComboBox<String> sortCombo;
    private Set<String> selectedAmenities = new HashSet<>();

    public Node getPageNode(
        Consumer<RoomItem> onSelectRoom,
        Runnable onNavigateHome,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices,
        Runnable onNavigatePostItem,
        Runnable onNavigateProfile
    ) {
        VBox mainContent = new VBox(18);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        Text titleText = new Text("Rent - Rooms & Student Rentals");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Find rooms, furniture, electronics, appliances, books, vehicles & essentials for rent.");
        subtitleText.setStyle(Theme.mutedTextStyle());

        HBox topTitleRow = new HBox(12);
        topTitleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4, titleText, subtitleText);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button ownerPortalBtn = new Button("🏢 Owner Portal / List Property");
        ownerPortalBtn.setStyle(Theme.primaryBtnStyle());
        ownerPortalBtn.setOnAction(e -> Main.showOwnerDashboard());

        topTitleRow.getChildren().addAll(titleBox, ownerPortalBtn);
        VBox headerBox = new VBox(4, topTitleRow);

        // Search and Filter Bar
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search rooms, locations, furniture...");
        searchField.setStyle(Theme.inputStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        String[] locations = {"All Locations", "Kothrud", "Hinjewadi", "Baner", "Viman Nagar", "Aundh"};
        locationCombo = new ComboBox<>(FXCollections.observableArrayList(locations));
        locationCombo.getSelectionModel().select(0);
        locationCombo.setStyle(Theme.comboStyle());

        String[] rentCats = {"All Rentals", "Rooms & PG", "Furniture", "Electronics", "Appliances", "Vehicle", "Stationery"};
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(rentCats));
        typeCombo.getSelectionModel().select(0);
        typeCombo.setStyle(Theme.comboStyle());

        String[] budgets = {"All Budgets", "Under ₹5,000", "₹5,000 - ₹10,000", "Above ₹10,000"};
        budgetCombo = new ComboBox<>(FXCollections.observableArrayList(budgets));
        budgetCombo.getSelectionModel().select(0);
        budgetCombo.setStyle(Theme.comboStyle());

        String[] sortOptions = {"Sort: Default", "Price: Low to High", "Price: High to Low"};
        sortCombo = new ComboBox<>(FXCollections.observableArrayList(sortOptions));
        sortCombo.getSelectionModel().select(0);
        sortCombo.setStyle(Theme.comboStyle());

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle(Theme.secondaryBtnStyle());

        filterRow.getChildren().addAll(searchField, locationCombo, typeCombo, budgetCombo, sortCombo, resetBtn);

        // Category Pills Row
        HBox categoriesBox = new HBox(10);
        categoriesBox.setAlignment(Pos.CENTER_LEFT);

        // Dynamic listing container
        FlowPane roomsGrid = new FlowPane(16, 16);

        Label countText = new Label();
        countText.setStyle(Theme.mutedTextStyle());

        // Refresh List Logic
        Runnable refreshList = () -> {
            roomsGrid.getChildren().clear();
            List<RoomItem> filtered = getFilteredRooms(searchField.getText());

            for (RoomItem room : filtered) {
                String badge = "r1".equals(room.getId()) ? "FEATURED" : ("r10".equals(room.getId()) ? "HOT DEAL" : "VERIFIED");
                String cat = room.getType() != null ? room.getType() : "Rental";
                roomsGrid.getChildren().add(new ListingCardNode(
                    room.getId(), ListingCardNode.CardType.ROOM, badge,
                    room.getTitle(), room.getLocation(), room.getPrice(), room.getDistance(),
                    room.getImagePath(), cat, () -> Main.showRoomDetailsPage(room)
                ));
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No rental listings found",
                    "Try adjusting your filters or search keywords.",
                    () -> {
                        searchField.clear();
                        locationCombo.getSelectionModel().select(0);
                        typeCombo.getSelectionModel().select(0);
                        budgetCombo.getSelectionModel().select(0);
                        sortCombo.getSelectionModel().select(0);
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                roomsGrid.getChildren().add(emptyState);
            }

            countText.setText("Showing " + filtered.size() + " of " + roomController.getAllRooms().size() + " rental listings");
        };

        // Category Pills Events
        for (String cat : rentCats) {
            Button catBtn = new Button(cat);
            boolean isAll = cat.equals("All Rentals");
            catBtn.setStyle(Theme.filterPillStyle(isAll));
            catBtn.setOnAction(e -> {
                typeCombo.getSelectionModel().select(cat);
                for (Node node : categoriesBox.getChildren()) {
                    if (node instanceof Button) {
                        Button b = (Button) node;
                        boolean active = b.getText().equals(cat);
                        b.setStyle(Theme.filterPillStyle(active));
                    }
                }
            });
            categoriesBox.getChildren().add(catBtn);
        }

        // Inline filter listeners
        locationCombo.setOnAction(e -> refreshList.run());
        typeCombo.setOnAction(e -> refreshList.run());
        budgetCombo.setOnAction(e -> refreshList.run());
        sortCombo.setOnAction(e -> refreshList.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList.run());

        resetBtn.setOnAction(e -> {
            locationCombo.getSelectionModel().select(0);
            typeCombo.getSelectionModel().select(0);
            budgetCombo.getSelectionModel().select(0);
            sortCombo.getSelectionModel().select(0);
            searchField.clear();
            refreshList.run();
        });

        // Initial populate
        refreshList.run();

        // Footer / Count bar
        HBox countRow = new HBox(countText);
        countRow.setPadding(new Insets(10, 0, 10, 0));

        mainContent.getChildren().addAll(headerBox, categoriesBox, filterRow, countRow, roomsGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private List<RoomItem> getFilteredRooms(String searchQuery) {
        List<RoomItem> result = new ArrayList<>();
        String query = searchQuery != null ? searchQuery.toLowerCase().trim() : "";

        String selectedLocation = locationCombo != null && locationCombo.getValue() != null ? locationCombo.getValue() : "All Locations";
        String selectedType = typeCombo != null && typeCombo.getValue() != null ? typeCombo.getValue() : "All Categories";
        String selectedBudget = budgetCombo != null && budgetCombo.getValue() != null ? budgetCombo.getValue() : "All Budgets";
        String selectedSort = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: Default";

        for (RoomItem r : roomController.getAllRooms()) {
            if (!selectedLocation.equals("All Locations") && !r.getLocation().toLowerCase().contains(selectedLocation.toLowerCase())) {
                continue;
            }

            if (!selectedType.equals("All Categories") && !selectedType.equals("All Rentals")) {
                String target = selectedType.toLowerCase();
                boolean matchTitle = r.getTitle().toLowerCase().contains(target);
                boolean matchType = r.getType() != null && r.getType().toLowerCase().contains(target);
                boolean matchOccupants = r.getOccupants() != null && r.getOccupants().toLowerCase().contains(target);
                if (!matchTitle && !matchType && !matchOccupants) continue;
            }

            int priceNum = parsePrice(r.getPrice());
            if (selectedBudget.equals("Under ₹5,000") && priceNum > 5000) continue;
            if (selectedBudget.equals("₹5,000 - ₹8,000") && (priceNum < 5000 || priceNum > 8000)) continue;
            if (selectedBudget.equals("Above ₹8,000") && priceNum <= 8000) continue;

            if (!query.isEmpty()) {
                boolean matchTitle = r.getTitle().toLowerCase().contains(query);
                boolean matchLoc = r.getLocation().toLowerCase().contains(query);
                boolean matchDesc = r.getDescription().toLowerCase().contains(query);
                if (!matchTitle && !matchLoc && !matchDesc) continue;
            }

            result.add(r);
        }

        if (selectedSort.equals("Price: Low to High")) {
            result.sort(Comparator.comparingInt(a -> parsePrice(a.getPrice())));
        } else if (selectedSort.equals("Price: High to Low")) {
            result.sort((a, b) -> Integer.compare(parsePrice(b.getPrice()), parsePrice(a.getPrice())));
        } else if (selectedSort.equals("Nearest Distance")) {
            result.sort(Comparator.comparingDouble(a -> parseDistance(a.getDistance())));
        }

        return result;
    }

    private int parsePrice(String priceStr) {
        try {
            String clean = priceStr.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0 : Integer.parseInt(clean);
        } catch (Exception e) { return 0; }
    }

    private double parseDistance(String distStr) {
        try {
            String[] parts = distStr.split(" ");
            return Double.parseDouble(parts[0]);
        } catch (Exception e) { return 99.0; }
    }

    public Scene getPageScene(
        Consumer<RoomItem> onSelectRoom,
        Runnable onNavigateHome,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateServices,
        Runnable onNavigatePostItem,
        Runnable onNavigateProfile
    ) {
        Node node = getPageNode(onSelectRoom, onNavigateHome, onNavigateBuySell, onNavigateRoommates, onNavigateServices, onNavigatePostItem, onNavigateProfile);
        rentScene = new Scene(new BorderPane(node), 1050, 700);
        return rentScene;
    }
}
