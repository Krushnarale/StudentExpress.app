package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.ServiceController;
import com.core2web.model.ServiceItem;
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
    private final ServiceController serviceController = new ServiceController();

    public Node getPageNode(
        Consumer<ServiceItem> onSelectService,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateRoommates,
        Runnable onNavigateProfile
    ) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        Text titleText = new Text("Student Services & Campus Support");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Hygienic tiffin mess, doorstep laundry, room cleaning, high-speed Wi-Fi, and printing services.");
        subtitleText.setStyle(Theme.mutedTextStyle());

        VBox headerBox = new VBox(4, titleText, subtitleText);

        // Filter Bar
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList("All Services", "Laundry", "Mess", "Cleaning", "Wi-Fi", "Printing"));
        categoryCombo.getSelectionModel().select(0);
        categoryCombo.setStyle(Theme.comboBoxStyle());

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search services...");
        searchField.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterRow.getChildren().addAll(new Label("Category:"), categoryCombo, searchField);

        FlowPane sGrid = new FlowPane(16, 16);
        sGrid.setPadding(new Insets(10, 0, 10, 0));

        Runnable refreshList = () -> {
            sGrid.getChildren().clear();
            List<ServiceItem> filtered = getFilteredServices(searchField.getText());

            for (ServiceItem s : filtered) {
                ListingCardNode card = new ListingCardNode(
                    s.getId(),
                    ListingCardNode.CardType.SERVICE,
                    "VERIFIED",
                    s.getTitle(),
                    s.getSubtitle(),
                    s.getPrice(),
                    "Instant Booking",
                    s.getImagePath(),
                    s.getCategory(),
                    () -> { if (onSelectService != null) onSelectService.accept(s); }
                );
                sGrid.getChildren().add(card);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Student Services Found",
                    "Try choosing another category or clearing your search keywords.",
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

    private List<ServiceItem> getFilteredServices(String query) {
        List<ServiceItem> result = new ArrayList<>();
        String q = query != null ? query.toLowerCase().trim() : "";
        String selectedCat = categoryCombo != null && categoryCombo.getValue() != null ? categoryCombo.getValue() : "All Services";

        for (ServiceItem s : serviceController.getAllServices()) {
            if (!selectedCat.equals("All Services") && !s.getCategory().equalsIgnoreCase(selectedCat)) {
                continue;
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
