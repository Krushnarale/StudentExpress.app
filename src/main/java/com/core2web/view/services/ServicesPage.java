package com.core2web.view.services;

import com.core2web.Main;
import com.core2web.model.ServiceItem;
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

public class ServicesPage {

    private Scene servicesScene;
    private ComboBox<String> categoryCombo;

    public Node getPageNode(
        Consumer<ServiceItem> onSelectService,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateProfile
    ) {
        VBox mainContent = new VBox(10);
        mainContent.setPadding(new Insets(18, 30, 20, 30));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        Text titleText = new Text("Student Services & Campus Support");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Hygienic tiffin mess, doorstep laundry, room cleaning, high-speed Wi-Fi, and printing services.");
        subtitleText.setStyle(Theme.mutedTextStyle());

        VBox headerBox = new VBox(2, titleText, subtitleText);

        // Filter Bar
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList("All Services", "Laundry", "Mess", "Cleaning", "Wi-Fi", "Printing"));
        categoryCombo.getSelectionModel().select(0);
        categoryCombo.setStyle(Theme.comboBoxStyle());

        Label catLbl = new Label("Category:");
        catLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search services...");
        searchField.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterRow.getChildren().addAll(catLbl, categoryCombo, searchField);

        FlowPane sGrid = new FlowPane(16, 16);
        sGrid.setPadding(new Insets(4, 0, 10, 0));
        sGrid.setMaxWidth(Double.MAX_VALUE);

        Runnable refreshList = () -> {
            sGrid.getChildren().clear();
            List<ServiceItem> filtered = getFilteredServices(searchField.getText());
            System.out.println("[STUDENT] Loading services... Found: " + filtered.size() + " services.");

            for (ServiceItem s : filtered) {
                ListingCardNode card = new ListingCardNode(
                    s.getId(),
                    ListingCardNode.CardType.SERVICE,
                    "POPULAR",
                    s.getTitle(),
                    "Doorstep Delivery",
                    s.getPrice(),
                    s.getSubtitle(),
                    s.getImagePath(),
                    s.getCategory(),
                    () -> { if (onSelectService != null) onSelectService.accept(s); }
                );
                sGrid.getChildren().add(card);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Campus Services Match Your Query",
                    "Try searching for another service like laundry, mess, or printing.",
                    () -> {
                        categoryCombo.getSelectionModel().select(0);
                        searchField.clear();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                sGrid.getChildren().add(emptyState);
            }
        };

        categoryCombo.setOnAction(e -> refreshList.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList.run());

        refreshList.run();

        mainContent.getChildren().addAll(headerBox, filterRow, sGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private boolean matchesServiceCategory(ServiceItem s, String catFilter) {
        if (catFilter == null || catFilter.isEmpty() || catFilter.equalsIgnoreCase("All Services") || catFilter.equalsIgnoreCase("All")) {
            return true;
        }
        String filter = catFilter.toLowerCase().trim();
        String itemCat = s.getCategory() != null ? s.getCategory().toLowerCase().trim() : "";
        String itemTitle = s.getTitle() != null ? s.getTitle().toLowerCase().trim() : "";
        String itemSub = s.getSubtitle() != null ? s.getSubtitle().toLowerCase().trim() : "";

        if (itemCat.equals(filter) || itemCat.contains(filter) || filter.contains(itemCat)) {
            return true;
        }

        if (filter.contains("laundry")) return itemCat.contains("laundr") || itemCat.contains("wash") || itemCat.contains("iron") || itemTitle.contains("laundry") || itemTitle.contains("iron");
        if (filter.contains("tiffin") || filter.contains("mess")) return itemCat.contains("tiffin") || itemCat.contains("mess") || itemCat.contains("meal") || itemCat.contains("food") || itemTitle.contains("tiffin") || itemTitle.contains("mess");
        if (filter.contains("cleaning") || filter.contains("clean")) return itemCat.contains("clean") || itemCat.contains("maid") || itemCat.contains("housekeep") || itemTitle.contains("clean");
        if (filter.contains("wi-fi") || filter.contains("wifi") || filter.contains("tech")) return itemCat.contains("wi-fi") || itemCat.contains("wifi") || itemCat.contains("tech") || itemCat.contains("internet") || itemTitle.contains("wifi") || itemTitle.contains("tech");
        if (filter.contains("printing") || filter.contains("print")) return itemCat.contains("print") || itemCat.contains("xerox") || itemCat.contains("scan") || itemTitle.contains("print");
        if (filter.contains("tutoring") || filter.contains("tutor")) return itemCat.contains("tutor") || itemCat.contains("teach") || itemCat.contains("class") || itemTitle.contains("tutor");
        if (filter.contains("transport")) return itemCat.contains("transport") || itemCat.contains("cab") || itemCat.contains("ride") || itemCat.contains("bike") || itemTitle.contains("transport");
        if (filter.contains("repair") || filter.contains("appliance")) return itemCat.contains("repair") || itemCat.contains("electric") || itemCat.contains("plumb") || itemTitle.contains("repair");

        return itemTitle.contains(filter) || itemSub.contains(filter);
    }

    private List<ServiceItem> getFilteredServices(String query) {
        List<ServiceItem> result = new ArrayList<>();
        String q = query != null ? query.toLowerCase().trim() : "";
        String selectedCat = categoryCombo != null && categoryCombo.getValue() != null ? categoryCombo.getValue() : "All Services";

        for (ServiceItem s : DataRepository.getInstance().getServices()) {
            if (!selectedCat.equals("All Services")) {
                if (!matchesServiceCategory(s, selectedCat)) {
                    continue;
                }
            }

            if (!q.isEmpty()) {
                boolean matchTitle = s.getTitle().toLowerCase().contains(q);
                boolean matchCat = s.getCategory().toLowerCase().contains(q);
                boolean matchSub = s.getSubtitle().toLowerCase().contains(q);
                if (!matchTitle && !matchCat && !matchSub) continue;
            }

            result.add(s);
        }

        return result;
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
