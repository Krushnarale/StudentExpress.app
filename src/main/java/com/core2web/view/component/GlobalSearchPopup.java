package com.core2web.view.component;

import com.core2web.Main;
import com.core2web.dao.*;
import com.core2web.model.*;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class GlobalSearchPopup extends VBox {

    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final ServiceDAO serviceDAO = new ServiceDAOImpl();
    private final RoommateDAO roommateDAO = new RoommateDAOImpl();

    public GlobalSearchPopup() {
        super(10);
        setPadding(new Insets(14));
        setStyle(
            "-fx-background-color: white;"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 12px;"
            + "-fx-background-radius: 12px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 6);"
        );
        setMaxHeight(380);
    }

    public void updateSearch(String query) {
        getChildren().clear();
        if (query == null || query.trim().length() < 2) {
            setVisible(false);
            return;
        }

        String q = query.toLowerCase().trim();

        VBox resultsBox = new VBox(8);
        resultsBox.setPadding(new Insets(4));

        int totalCount = 0;

        // Search Rooms
        List<RoomItem> rooms = roomDAO.findAll();
        int roomHits = 0;
        for (RoomItem r : rooms) {
            if (r.getTitle().toLowerCase().contains(q) || r.getLocation().toLowerCase().contains(q) || (r.getType() != null && r.getType().toLowerCase().contains(q))) {
                resultsBox.getChildren().add(createSearchRow(IconFactory.PATH_KEY, r.getTitle(), "Room & PG · " + r.getLocation(), r.getPrice(), () -> Main.showRoomDetailsPage(r)));
                roomHits++;
                totalCount++;
                if (roomHits >= 3) break;
            }
        }

        // Search Products
        List<ProductItem> products = productDAO.findAll();
        int prodHits = 0;
        for (ProductItem p : products) {
            if (p.getTitle().toLowerCase().contains(q) || p.getCategory().toLowerCase().contains(q) || p.getLocation().toLowerCase().contains(q)) {
                resultsBox.getChildren().add(createSearchRow(IconFactory.PATH_SHOPPING_BAG, p.getTitle(), "Marketplace · " + p.getCategory(), p.getPrice(), () -> Main.showProductDetailsPage(p)));
                prodHits++;
                totalCount++;
                if (prodHits >= 3) break;
            }
        }

        // Search Services
        List<ServiceItem> services = serviceDAO.findAll();
        int serviceHits = 0;
        for (ServiceItem s : services) {
            if (s.getTitle().toLowerCase().contains(q) || s.getCategory().toLowerCase().contains(q) || (s.getSubtitle() != null && s.getSubtitle().toLowerCase().contains(q))) {
                resultsBox.getChildren().add(createSearchRow(IconFactory.PATH_WRENCH, s.getTitle(), "Services · " + s.getCategory(), s.getPrice(), () -> Main.showServiceDetailsPage(s)));
                serviceHits++;
                totalCount++;
                if (serviceHits >= 3) break;
            }
        }

        // Search Roommates
        List<RoommateItem> roommates = roommateDAO.findAll();
        int rmHits = 0;
        for (RoommateItem rm : roommates) {
            if (rm.getName().toLowerCase().contains(q) || rm.getLocation().toLowerCase().contains(q) || (rm.getPreference() != null && rm.getPreference().toLowerCase().contains(q))) {
                resultsBox.getChildren().add(createSearchRow(IconFactory.PATH_USERS, rm.getName(), "Roommate · " + rm.getLocation(), rm.getBudget(), () -> Main.showRoommateDetailsPage(rm)));
                rmHits++;
                totalCount++;
                if (rmHits >= 3) break;
            }
        }

        if (totalCount == 0) {
            VBox emptyBox = new VBox(6);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(16));
            Text noRes = new Text("No matches found for '" + query + "'");
            noRes.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px;");
            emptyBox.getChildren().add(noRes);
            getChildren().add(emptyBox);
        } else {
            Text header = new Text("SEARCH RESULTS (" + totalCount + ")");
            header.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 10px;");
            
            ScrollPane scroll = new ScrollPane(resultsBox);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: white;");

            getChildren().addAll(header, scroll);
        }

        setVisible(true);
    }

    private HBox createSearchRow(String iconPath, String title, String subtitle, String price, Runnable onSelect) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px; -fx-cursor: hand;");

        Node icon = IconFactory.getIconNode(iconPath, Theme.PRIMARY, 18);

        VBox textBox = new VBox(2);
        Text titleText = new Text(title);
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-font-size: 13px;");

        Text subText = new Text(subtitle);
        subText.setStyle(Theme.mutedTextStyle());
        textBox.getChildren().addAll(titleText, subText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text priceText = new Text(price);
        priceText.setStyle(Theme.priceTextStyle());

        row.getChildren().addAll(icon, textBox, spacer, priceText);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> {
            setVisible(false);
            if (onSelect != null) onSelect.run();
        });

        return row;
    }
}
