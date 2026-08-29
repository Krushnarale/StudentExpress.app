package com.core2web.view.rent;

import com.core2web.Main;
import com.core2web.model.RoomItem;
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

public class RentPage {

    private Scene rentScene;

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
        VBox mainContent = new VBox(10);
        mainContent.setPadding(new Insets(18, 30, 20, 30));

        Text titleText = new Text("Rent - Rooms & Student Rentals");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Find rooms, furniture, electronics, appliances, books, vehicles & essentials for rent.");
        subtitleText.setStyle(Theme.mutedTextStyle());

        VBox titleBox = new VBox(2, titleText, subtitleText);

        // Category Pills Bar
        FlowPane categoriesBox = new FlowPane(6, 6);
        categoriesBox.setAlignment(Pos.CENTER_LEFT);
        String[] rentCats = {"All Rentals", "Rooms & PG", "Furniture", "Electronics", "Appliances", "Books", "Gym & Fitness", "Vehicles"};

        // Inline Filter Bar with ComboBoxes
        FlowPane filterRow = new FlowPane(8, 8);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        locationCombo = new ComboBox<>(FXCollections.observableArrayList("All Locations", "Kothrud", "Hinjewadi", "Baner", "Viman Nagar", "Wakad"));
        locationCombo.getSelectionModel().select(0);

        typeCombo = new ComboBox<>(FXCollections.observableArrayList("All Categories", "Rooms & PG", "Furniture", "Electronics", "Appliances", "Books", "Gym & Fitness", "Vehicles"));
        typeCombo.getSelectionModel().select(0);

        budgetCombo = new ComboBox<>(FXCollections.observableArrayList("All Budgets", "Under ₹5,000", "₹5,000 - ₹8,000", "Above ₹8,000"));
        budgetCombo.getSelectionModel().select(0);

        sortCombo = new ComboBox<>(FXCollections.observableArrayList("Sort: Default", "Price: Low to High", "Price: High to Low", "Nearest Distance"));
        sortCombo.getSelectionModel().select(0);

        // Apply consistent styling and hover feedback for filter ComboBoxes
        for (ComboBox<String> combo : Arrays.asList(locationCombo, typeCombo, budgetCombo, sortCombo)) {
            combo.setPrefHeight(32);
            combo.setStyle(filterComboStyle());
            combo.setOnMouseEntered(ev -> combo.setStyle(filterComboHoverStyle()));
            combo.setOnMouseExited(ev -> combo.setStyle(filterComboStyle()));
        }

        Button resetBtn = new Button("✕ Reset");
        resetBtn.setPrefHeight(32);
        resetBtn.setStyle(resetBtnStyle());
        resetBtn.setOnMouseEntered(ev -> resetBtn.setStyle(resetBtnHoverStyle()));
        resetBtn.setOnMouseExited(ev -> resetBtn.setStyle(resetBtnStyle()));

        Label filterLbl = new Label("Filters:");
        filterLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";");

        filterRow.getChildren().addAll(
            filterLbl, locationCombo, typeCombo, budgetCombo, sortCombo, resetBtn
        );

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search rooms or items...");
        searchField.setStyle(Theme.searchFieldStyle());

        // Rooms Grid/List Container
        FlowPane roomsGrid = new FlowPane(16, 16);
        roomsGrid.setPadding(new Insets(4, 0, 10, 0));

        Text countText = new Text();
        countText.setStyle(Theme.mutedTextStyle());

        // Refresh List Logic
        Runnable refreshList = () -> {
            roomsGrid.getChildren().clear();
            List<RoomItem> filtered = getFilteredRooms(searchField.getText());
            System.out.println("[STUDENT] Loading rentals/rooms... Found: " + filtered.size() + " listings.");

            for (RoomItem room : filtered) {
                String badge = "VERIFIED";
                if (room.getPrice().contains("6,000")) badge = "POPULAR";
                else if (room.getPrice().contains("299") || room.getPrice().contains("199")) badge = "PRICE DROP";

                ListingCardNode card = new ListingCardNode(
                    room.getId(),
                    ListingCardNode.CardType.ROOM,
                    badge,
                    room.getTitle(),
                    room.getLocation(),
                    room.getPrice(),
                    room.getDistance(),
                    room.getImagePath(),
                    room.getType(),
                    () -> { if (onSelectRoom != null) onSelectRoom.accept(room); }
                );
                roomsGrid.getChildren().add(card);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Rental Items Match Your Filters",
                    "Try selecting a different location, price range, or category.",
                    () -> {
                        locationCombo.getSelectionModel().select(0);
                        typeCombo.getSelectionModel().select(0);
                        budgetCombo.getSelectionModel().select(0);
                        sortCombo.getSelectionModel().select(0);
                        searchField.clear();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                roomsGrid.getChildren().add(emptyState);
            }

            countText.setText("Showing " + filtered.size() + " of " + DataRepository.getInstance().getRooms().size() + " rental listings");
        };

        // Helper to update active category pill styles
        Runnable updatePills = () -> {
            String currentType = typeCombo.getValue();
            String currentActiveCat = (currentType == null || currentType.equals("All Categories")) ? "All Rentals" : currentType;

            for (Node node : categoriesBox.getChildren()) {
                if (node instanceof Button) {
                    Button b = (Button) node;
                    boolean active = b.getText().equals(currentActiveCat);
                    b.setStyle(categoryBtnStyle(active));
                }
            }
        };

        // Category Pills Events & Sizing
        for (String cat : rentCats) {
            Button catBtn = new Button(cat);
            catBtn.setPrefHeight(32);
            boolean isAll = cat.equals("All Rentals");
            catBtn.setStyle(categoryBtnStyle(isAll));

            catBtn.setOnMouseEntered(ev -> {
                String currentType = typeCombo.getValue();
                String currentActiveCat = (currentType == null || currentType.equals("All Categories")) ? "All Rentals" : currentType;
                if (!cat.equals(currentActiveCat)) {
                    catBtn.setStyle(categoryBtnHoverStyle());
                }
            });

            catBtn.setOnMouseExited(ev -> {
                String currentType = typeCombo.getValue();
                String currentActiveCat = (currentType == null || currentType.equals("All Categories")) ? "All Rentals" : currentType;
                boolean active = cat.equals(currentActiveCat);
                catBtn.setStyle(categoryBtnStyle(active));
            });

            catBtn.setOnAction(e -> {
                String selectedType = cat.equals("All Rentals") ? "All Categories" : cat;
                typeCombo.getSelectionModel().select(selectedType);
                updatePills.run();
            });

            categoriesBox.getChildren().add(catBtn);
        }

        // Inline filter listeners
        locationCombo.setOnAction(e -> refreshList.run());
        typeCombo.setOnAction(e -> {
            updatePills.run();
            refreshList.run();
        });
        budgetCombo.setOnAction(e -> refreshList.run());
        sortCombo.setOnAction(e -> refreshList.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList.run());

        resetBtn.setOnAction(e -> {
            locationCombo.getSelectionModel().select(0);
            typeCombo.getSelectionModel().select(0);
            budgetCombo.getSelectionModel().select(0);
            sortCombo.getSelectionModel().select(0);
            searchField.clear();
            updatePills.run();
            refreshList.run();
        });

        // Initial populate
        refreshList.run();

        // Footer / Count bar immediately below filters
        HBox countRow = new HBox(countText);
        countRow.setPadding(new Insets(2, 0, 2, 0));

        mainContent.getChildren().addAll(titleBox, categoriesBox, filterRow, countRow, roomsGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private String categoryBtnStyle(boolean active) {
        if (active) {
            return "-fx-background-color: " + Theme.PRIMARY + ";"
                 + "-fx-text-fill: #FFFFFF;"
                 + "-fx-font-family: " + Theme.FONT + ";"
                 + "-fx-font-weight: 700;"
                 + "-fx-font-size: 12px;"
                 + "-fx-border-color: " + Theme.PRIMARY + ";"
                 + "-fx-border-radius: 20px;"
                 + "-fx-background-radius: 20px;"
                 + "-fx-padding: 6px 16px;"
                 + "-fx-cursor: hand;"
                 + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.25), 6, 0, 0, 2);";
        }
        return "-fx-background-color: " + Theme.CARD_BG + ";"
             + "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";"
             + "-fx-font-family: " + Theme.FONT + ";"
             + "-fx-font-weight: 600;"
             + "-fx-font-size: 12px;"
             + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
             + "-fx-border-radius: 20px;"
             + "-fx-background-radius: 20px;"
             + "-fx-padding: 6px 16px;"
             + "-fx-cursor: hand;";
    }

    private String categoryBtnHoverStyle() {
        return "-fx-background-color: " + Theme.PRIMARY_LIGHT + ";"
             + "-fx-text-fill: " + Theme.PRIMARY + ";"
             + "-fx-font-family: " + Theme.FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 12px;"
             + "-fx-border-color: " + Theme.PRIMARY + ";"
             + "-fx-border-radius: 20px;"
             + "-fx-background-radius: 20px;"
             + "-fx-padding: 6px 16px;"
             + "-fx-cursor: hand;";
    }

    private String filterComboStyle() {
        return "-fx-background-color: " + Theme.CARD_BG + ";"
             + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
             + "-fx-border-radius: 10px;"
             + "-fx-background-radius: 10px;"
             + "-fx-padding: 3px 10px;"
             + "-fx-font-family: " + Theme.FONT + ";"
             + "-fx-font-size: 12px;"
             + "-fx-font-weight: 600;"
             + "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";"
             + "-fx-cursor: hand;";
    }

    private String filterComboHoverStyle() {
        return "-fx-background-color: #FAFCF8;"
             + "-fx-border-color: " + Theme.PRIMARY + ";"
             + "-fx-border-radius: 10px;"
             + "-fx-background-radius: 10px;"
             + "-fx-padding: 3px 10px;"
             + "-fx-font-family: " + Theme.FONT + ";"
             + "-fx-font-size: 12px;"
             + "-fx-font-weight: 600;"
             + "-fx-text-fill: " + Theme.PRIMARY + ";"
             + "-fx-cursor: hand;";
    }

    private String resetBtnStyle() {
        return "-fx-background-color: transparent;"
             + "-fx-text-fill: #C62828;"
             + "-fx-font-family: " + Theme.FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 12px;"
             + "-fx-padding: 5px 12px;"
             + "-fx-border-radius: 8px;"
             + "-fx-background-radius: 8px;"
             + "-fx-cursor: hand;";
    }

    private String resetBtnHoverStyle() {
        return "-fx-background-color: #FFF5F5;"
             + "-fx-text-fill: #C62828;"
             + "-fx-font-family: " + Theme.FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 12px;"
             + "-fx-padding: 5px 12px;"
             + "-fx-border-color: #FEB2B2;"
             + "-fx-border-radius: 8px;"
             + "-fx-background-radius: 8px;"
             + "-fx-cursor: hand;";
    }

    private List<RoomItem> getFilteredRooms(String searchQuery) {
        List<RoomItem> result = new ArrayList<>();
        String query = searchQuery != null ? searchQuery.toLowerCase().trim() : "";

        String selectedLocation = locationCombo != null && locationCombo.getValue() != null ? locationCombo.getValue() : "All Locations";
        String selectedType = typeCombo != null && typeCombo.getValue() != null ? typeCombo.getValue() : "All Categories";
        String selectedBudget = budgetCombo != null && budgetCombo.getValue() != null ? budgetCombo.getValue() : "All Budgets";
        String selectedSort = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: Default";

        for (RoomItem r : DataRepository.getInstance().getRooms()) {
            if (!selectedLocation.equals("All Locations") && !r.getLocation().toLowerCase().contains(selectedLocation.toLowerCase())) {
                continue;
            }

            if (!selectedType.equals("All Categories") && !selectedType.equals("All Rentals")) {
                if (!matchesRentCategory(r, selectedType)) continue;
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

    private boolean matchesRentCategory(RoomItem r, String targetCat) {
        if (targetCat == null || targetCat.isEmpty() || targetCat.equalsIgnoreCase("All Categories") || targetCat.equalsIgnoreCase("All Rentals") || targetCat.equalsIgnoreCase("All")) {
            return true;
        }
        String filter = targetCat.toLowerCase().trim();
        String itemType = r.getType() != null ? r.getType().toLowerCase().trim() : "";
        String itemCat = r.getCategory() != null ? r.getCategory().toLowerCase().trim() : "";
        String itemTitle = r.getTitle() != null ? r.getTitle().toLowerCase().trim() : "";
        String itemOcc = r.getOccupants() != null ? r.getOccupants().toLowerCase().trim() : "";

        if (itemType.equals(filter) || itemCat.equals(filter) || itemType.contains(filter) || filter.contains(itemType)) {
            return true;
        }

        if (filter.contains("room") || filter.contains("pg")) {
            return itemType.contains("room") || itemType.contains("pg") || itemCat.contains("room") || itemCat.contains("pg") || itemTitle.contains("room") || itemTitle.contains("flat") || itemTitle.contains("pg") || itemOcc.contains("occupant") || itemOcc.contains("sharing") || itemOcc.contains("single");
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
