package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.RoommateController;
import com.core2web.model.RoommateItem;
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

public class RoommateFinderPage {

    private Scene roommateScene;
    private ComboBox<String> genderCombo;
    private ComboBox<String> locationCombo;
    private final RoommateController roommateController = new RoommateController();

    public Node getPageNode(
        Consumer<RoommateItem> onSelectRoommate,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateServices,
        Runnable onNavigateProfile
    ) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        Text titleText = new Text("Roommate Finder - Flatmates & PG Sharing");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Find compatible college roommates, flatmates, and PG partners matching your budget and lifestyle.");
        subtitleText.setStyle(Theme.mutedTextStyle());

        VBox headerBox = new VBox(4, titleText, subtitleText);

        // Filter Bar
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        genderCombo = new ComboBox<>(FXCollections.observableArrayList("All Genders", "Male", "Female"));
        genderCombo.getSelectionModel().select(0);
        genderCombo.setStyle(Theme.comboBoxStyle());

        locationCombo = new ComboBox<>(FXCollections.observableArrayList("All Locations", "Kothrud", "Hinjewadi", "Baner", "Viman Nagar"));
        locationCombo.getSelectionModel().select(0);
        locationCombo.setStyle(Theme.comboBoxStyle());

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by name, college, or location...");
        searchField.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterRow.getChildren().addAll(new Label("Gender:"), genderCombo, new Label("Location:"), locationCombo, searchField);

        FlowPane rmGrid = new FlowPane(16, 16);
        rmGrid.setPadding(new Insets(10, 0, 10, 0));

        Runnable refreshList = () -> {
            rmGrid.getChildren().clear();
            List<RoommateItem> filtered = getFilteredRoommates(searchField.getText());

            for (RoommateItem rm : filtered) {
                ListingCardNode card = new ListingCardNode(
                    rm.getId(),
                    ListingCardNode.CardType.ROOMMATE,
                    rm.getGender().toUpperCase(),
                    rm.getName(),
                    rm.getLocation(),
                    rm.getBudget(),
                    rm.getPreference(),
                    rm.getImagePath(),
                    "Roommate",
                    () -> { if (onSelectRoommate != null) onSelectRoommate.accept(rm); }
                );
                rmGrid.getChildren().add(card);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Roommate Profiles Found",
                    "Try adjusting your gender or location filters.",
                    () -> {
                        genderCombo.getSelectionModel().select(0);
                        locationCombo.getSelectionModel().select(0);
                        searchField.clear();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                rmGrid.getChildren().add(emptyState);
            }
        };

        genderCombo.setOnAction(e -> refreshList.run());
        locationCombo.setOnAction(e -> refreshList.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList.run());

        refreshList.run();

        mainContent.getChildren().addAll(headerBox, filterRow, rmGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private List<RoommateItem> getFilteredRoommates(String query) {
        List<RoommateItem> result = new ArrayList<>();
        String q = query != null ? query.toLowerCase().trim() : "";

        String selectedGender = genderCombo != null && genderCombo.getValue() != null ? genderCombo.getValue() : "All Genders";
        String selectedLoc = locationCombo != null && locationCombo.getValue() != null ? locationCombo.getValue() : "All Locations";

        for (RoommateItem rm : roommateController.getAllRoommates()) {
            if (!selectedGender.equals("All Genders") && !rm.getGender().equalsIgnoreCase(selectedGender)) {
                continue;
            }

            if (!selectedLoc.equals("All Locations") && !rm.getLocation().toLowerCase().contains(selectedLoc.toLowerCase())) {
                continue;
            }

            if (!q.isEmpty()) {
                boolean matchName = rm.getName().toLowerCase().contains(q);
                boolean matchLoc = rm.getLocation().toLowerCase().contains(q);
                boolean matchBio = rm.getBio().toLowerCase().contains(q);
                if (!matchName && !matchLoc && !matchBio) continue;
            }

            result.add(rm);
        }

        return result;
    }

    public Scene getPageScene(
        Consumer<RoommateItem> onSelectRoommate,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateServices,
        Runnable onNavigateProfile
    ) {
        Node node = getPageNode(onSelectRoommate, onNavigateHome, onNavigateRent, onNavigateBuySell, onNavigateServices, onNavigateProfile);
        roommateScene = new Scene(new BorderPane(node), 1050, 700);
        return roommateScene;
    }
}
