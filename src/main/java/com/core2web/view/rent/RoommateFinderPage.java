package com.core2web.view.rent;

import com.core2web.Main;
import com.core2web.dao.RoommateDAOImpl;
import com.core2web.dao.RoommateRequestDAOImpl;
import com.core2web.model.RoommateItem;
import com.core2web.model.RoommateRequest;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.CloudinaryService;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import com.core2web.view.component.ListingCardNode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class RoommateFinderPage {

    private Scene roommateScene;
    private ComboBox<String> genderCombo;
    private ComboBox<String> locationCombo;
    private ComboBox<String> accomTypeCombo;

    public enum ActiveTab {
        BROWSE, REGISTER, REQUESTS
    }

    private ActiveTab activeTab = ActiveTab.BROWSE;

    public Node getPageNode(
        Consumer<RoommateItem> onSelectRoommate,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateServices,
        Runnable onNavigateProfile
    ) {
        return getPageNodeWithTab(ActiveTab.BROWSE, onSelectRoommate, onNavigateHome, onNavigateRent, onNavigateBuySell, onNavigateServices, onNavigateProfile);
    }

    public Node getPageNodeWithTab(
        ActiveTab initialTab,
        Consumer<RoommateItem> onSelectRoommate,
        Runnable onNavigateHome,
        Runnable onNavigateRent,
        Runnable onNavigateBuySell,
        Runnable onNavigateServices,
        Runnable onNavigateProfile
    ) {
        this.activeTab = initialTab != null ? initialTab : ActiveTab.BROWSE;

        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(20, 32, 28, 32));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        User currentUser = DataRepository.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "";

        // Check if current student already has a registered roommate profile
        RoommateItem myExistingProfile = DataRepository.getInstance().getRoommateForUser(currentUid);
        if (myExistingProfile == null && currentUid != null && !currentUid.isEmpty()) {
            Optional<RoommateItem> fsProfile = new RoommateDAOImpl().findByUserUid(currentUid);
            if (fsProfile.isPresent()) {
                myExistingProfile = fsProfile.get();
                DataRepository.getInstance().addOrUpdateRoommate(myExistingProfile);
            }
        }

        // Count pending incoming connection requests for current student
        List<RoommateRequest> incomingReqs = DataRepository.getInstance().getIncomingRoommateRequests(currentUid);
        long pendingReqCount = incomingReqs.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();

        // HEADER ROW: Title & Mode Switcher Buttons
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Text titleText = new Text("Roommate Finder - Flatmates & PG Sharing");
        titleText.setStyle(Theme.titleTextStyle());

        Text subtitleText = new Text("Find compatible college roommates or register your own profile to connect with peers.");
        subtitleText.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleText, subtitleText);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Action Tab Buttons Container
        HBox tabButtonsBox = new HBox(8);
        tabButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnBrowse = new Button("🔍 Find a Roommate");
        String registerBtnLabel = (myExistingProfile != null) ? "✏️ My Roommate Profile" : "➕ Register as Roommate";
        Button btnRegister = new Button(registerBtnLabel);
        String reqBtnLabel = pendingReqCount > 0 ? "📩 Requests (" + pendingReqCount + ")" : "📩 Requests";
        Button btnRequests = new Button(reqBtnLabel);

        styleTabButtons(btnBrowse, btnRegister, btnRequests, activeTab);

        tabButtonsBox.getChildren().addAll(btnBrowse, btnRegister, btnRequests);
        headerRow.getChildren().addAll(titleBox, tabButtonsBox);

        // Content Area Container that flips based on ActiveTab
        StackPane contentContainer = new StackPane();
        contentContainer.setMaxWidth(Double.MAX_VALUE);

        final Runnable[] refreshAll = new Runnable[1];

        Runnable showBrowseView = () -> {
            activeTab = ActiveTab.BROWSE;
            styleTabButtons(btnBrowse, btnRegister, btnRequests, activeTab);
            contentContainer.getChildren().setAll(createBrowseView(onSelectRoommate, () -> {
                activeTab = ActiveTab.REGISTER;
                styleTabButtons(btnBrowse, btnRegister, btnRequests, activeTab);
                refreshAll[0].run();
            }));
        };

        Runnable showRegisterView = () -> {
            activeTab = ActiveTab.REGISTER;
            styleTabButtons(btnBrowse, btnRegister, btnRequests, activeTab);
            contentContainer.getChildren().setAll(createRegisterView(currentUser, showBrowseView));
        };

        Runnable showRequestsView = () -> {
            activeTab = ActiveTab.REQUESTS;
            styleTabButtons(btnBrowse, btnRegister, btnRequests, activeTab);
            contentContainer.getChildren().setAll(createRequestsView(currentUid, refreshAll[0]));
        };

        refreshAll[0] = () -> {
            if (activeTab == ActiveTab.REGISTER) {
                showRegisterView.run();
            } else if (activeTab == ActiveTab.REQUESTS) {
                showRequestsView.run();
            } else {
                showBrowseView.run();
            }
        };

        btnBrowse.setOnAction(e -> showBrowseView.run());
        btnRegister.setOnAction(e -> showRegisterView.run());
        btnRequests.setOnAction(e -> showRequestsView.run());

        // Initialize active tab view
        if (activeTab == ActiveTab.REGISTER) {
            showRegisterView.run();
        } else if (activeTab == ActiveTab.REQUESTS) {
            showRequestsView.run();
        } else {
            showBrowseView.run();
        }

        mainContent.getChildren().addAll(headerRow, contentContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private void styleTabButtons(Button btnBrowse, Button btnRegister, Button btnRequests, ActiveTab current) {
        String activeStyle = Theme.primaryBtnStyle() + " -fx-font-size: 12.5px; -fx-padding: 7px 16px;";
        String inactiveStyle = Theme.secondaryBtnStyle() + " -fx-font-size: 12.5px; -fx-padding: 7px 16px;";

        btnBrowse.setStyle(current == ActiveTab.BROWSE ? activeStyle : inactiveStyle);
        btnRegister.setStyle(current == ActiveTab.REGISTER ? activeStyle : inactiveStyle);
        btnRequests.setStyle(current == ActiveTab.REQUESTS ? activeStyle : inactiveStyle);
    }

    // TAB 1: BROWSE / FIND A ROOMMATE VIEW
    private Node createBrowseView(Consumer<RoommateItem> onSelectRoommate, Runnable onNavigateToRegister) {
        VBox browseBox = new VBox(14);
        browseBox.setMaxWidth(Double.MAX_VALUE);

        // Filter Bar
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        genderCombo = new ComboBox<>(FXCollections.observableArrayList("All Genders", "Male", "Female", "Other"));
        genderCombo.getSelectionModel().select(0);
        genderCombo.setStyle(Theme.comboBoxStyle());

        locationCombo = new ComboBox<>(FXCollections.observableArrayList("All Locations", "Kothrud", "Hinjewadi", "Baner", "Viman Nagar", "Wakad", "Aundh", "Shivajinagar"));
        locationCombo.getSelectionModel().select(0);
        locationCombo.setStyle(Theme.comboBoxStyle());

        accomTypeCombo = new ComboBox<>(FXCollections.observableArrayList("All Accommodations", "Single Room", "2 Sharing Flat", "3 Sharing Flat", "1 BHK Flat", "2 BHK Flat", "PG Sharing"));
        accomTypeCombo.getSelectionModel().select(0);
        accomTypeCombo.setStyle(Theme.comboBoxStyle());

        Label filterLbl = new Label("Filters:");
        filterLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by name, college, course, or location...");
        searchField.setStyle(Theme.searchFieldStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterRow.getChildren().addAll(filterLbl, genderCombo, locationCombo, accomTypeCombo, searchField);

        FlowPane rmGrid = new FlowPane(18, 18);
        rmGrid.setPadding(new Insets(6, 0, 12, 0));
        rmGrid.setMaxWidth(Double.MAX_VALUE);

        User currentUser = DataRepository.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "";

        Runnable refreshList = () -> {
            rmGrid.getChildren().clear();
            List<RoommateItem> filtered = getFilteredRoommates(searchField.getText());

            for (RoommateItem rm : filtered) {
                boolean isMe = currentUid != null && !currentUid.isEmpty() && currentUid.equalsIgnoreCase(rm.getUserUid());
                String badgeText = isMe ? "YOUR PROFILE" : (rm.getGender() != null && !rm.getGender().isEmpty() ? rm.getGender().toUpperCase() : "STUDENT");

                ListingCardNode card = new ListingCardNode(
                    rm.getId(),
                    ListingCardNode.CardType.ROOMMATE,
                    badgeText,
                    rm.getName(),
                    rm.getLocation() != null && !rm.getLocation().isEmpty() ? rm.getLocation() : "Pune",
                    rm.getBudget() != null && !rm.getBudget().isEmpty() ? rm.getBudget() : "Flexible",
                    rm.getPreference() != null && !rm.getPreference().isEmpty() ? rm.getPreference() : "Roommate",
                    rm.getImagePath(),
                    "Roommate",
                    () -> {
                        if (isMe) {
                            if (onNavigateToRegister != null) onNavigateToRegister.run();
                        } else {
                            if (onSelectRoommate != null) onSelectRoommate.accept(rm);
                        }
                    }
                );
                rmGrid.getChildren().add(card);
            }

            if (filtered.isEmpty()) {
                EmptyStateNode emptyState = new EmptyStateNode(
                    "No Registered Roommate Profiles Found",
                    "No students have registered matching your search criteria. Be the first to register as a roommate!",
                    () -> {
                        if (onNavigateToRegister != null) onNavigateToRegister.run();
                    }
                );
                emptyState.setMaxWidth(Double.MAX_VALUE);
                rmGrid.getChildren().add(emptyState);
            }
        };

        genderCombo.setOnAction(e -> refreshList.run());
        locationCombo.setOnAction(e -> refreshList.run());
        accomTypeCombo.setOnAction(e -> refreshList.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList.run());

        refreshList.run();

        browseBox.getChildren().addAll(filterRow, rmGrid);
        return browseBox;
    }

    private List<RoommateItem> getFilteredRoommates(String query) {
        List<RoommateItem> result = new ArrayList<>();
        String q = query != null ? query.toLowerCase().trim() : "";

        String selectedGender = genderCombo != null && genderCombo.getValue() != null ? genderCombo.getValue() : "All Genders";
        String selectedLoc = locationCombo != null && locationCombo.getValue() != null ? locationCombo.getValue() : "All Locations";
        String selectedAccom = accomTypeCombo != null && accomTypeCombo.getValue() != null ? accomTypeCombo.getValue() : "All Accommodations";

        for (RoommateItem rm : DataRepository.getInstance().getRoommates()) {
            if ("INACTIVE".equalsIgnoreCase(rm.getStatus())) {
                continue;
            }

            if (!selectedGender.equals("All Genders") && !rm.getGender().equalsIgnoreCase(selectedGender)) {
                continue;
            }

            if (!selectedLoc.equals("All Locations") && (rm.getLocation() == null || !rm.getLocation().toLowerCase().contains(selectedLoc.toLowerCase()))) {
                continue;
            }

            if (!selectedAccom.equals("All Accommodations") && (rm.getAccommodationType() == null || !rm.getAccommodationType().equalsIgnoreCase(selectedAccom))) {
                continue;
            }

            if (!q.isEmpty()) {
                boolean matchName = rm.getName() != null && rm.getName().toLowerCase().contains(q);
                boolean matchLoc = rm.getLocation() != null && rm.getLocation().toLowerCase().contains(q);
                boolean matchBio = rm.getBio() != null && rm.getBio().toLowerCase().contains(q);
                boolean matchCollege = rm.getCollege() != null && rm.getCollege().toLowerCase().contains(q);
                boolean matchCourse = rm.getCourse() != null && rm.getCourse().toLowerCase().contains(q);
                if (!matchName && !matchLoc && !matchBio && !matchCollege && !matchCourse) continue;
            }

            result.add(rm);
        }

        return result;
    }

    // TAB 2: REGISTER AS ROOMMATE / EDIT PROFILE FORM
    private Node createRegisterView(User currentUser, Runnable onFinish) {
        VBox container = new VBox(20);
        container.setMaxWidth(780);
        container.setPadding(new Insets(24, 30, 24, 30));
        container.setStyle(Theme.elevatedCardStyle() + " -fx-background-radius: 16px;");

        String currentUid = currentUser != null ? currentUser.getUid() : "";
        RoommateItem existing = DataRepository.getInstance().getRoommateForUser(currentUid);
        boolean isEditing = existing != null;

        // Form Title
        Text formTitle = new Text(isEditing ? "Edit Your Roommate Profile" : "Register as Roommate");
        formTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");

        Text formSub = new Text(isEditing
            ? "Update your flatmate preferences, college details, and lifestyle to keep your listing fresh."
            : "Fill in your preferences so other students looking for flatmates and PG partners can connect with you.");
        formSub.setStyle(Theme.mutedTextStyle());

        VBox titleBox = new VBox(4, formTitle, formSub);

        // Profile Photo Upload Section
        HBox photoRow = new HBox(18);
        photoRow.setAlignment(Pos.CENTER_LEFT);
        photoRow.setPadding(new Insets(10, 0, 10, 0));

        final String[] uploadedPhotoUrl = new String[]{existing != null && existing.getImagePath() != null ? existing.getImagePath() : ""};
        final File[] selectedPhotoFile = new File[1];

        StackPane photoContainer = new StackPane();
        photoContainer.setPrefSize(80, 80);
        photoContainer.setMinSize(80, 80);
        photoContainer.setMaxSize(80, 80);
        photoContainer.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 40px;");

        ImageView photoPreview = new ImageView();
        photoPreview.setFitWidth(80);
        photoPreview.setFitHeight(80);
        photoPreview.setPreserveRatio(false);
        Circle clip = new Circle(40, 40, 40);
        photoPreview.setClip(clip);

        Runnable updatePhotoDisplay = () -> {
            photoContainer.getChildren().clear();
            boolean hasImg = false;
            if (selectedPhotoFile[0] != null) {
                photoPreview.setImage(new Image(selectedPhotoFile[0].toURI().toString()));
                hasImg = true;
            } else if (!uploadedPhotoUrl[0].isEmpty()) {
                Image img = ImageUtil.loadImage(uploadedPhotoUrl[0]);
                if (img != null && !img.isError()) {
                    photoPreview.setImage(img);
                    hasImg = true;
                }
            } else if (currentUser != null && currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                Image img = ImageUtil.loadImage(currentUser.getProfileImage());
                if (img != null && !img.isError()) {
                    photoPreview.setImage(img);
                    hasImg = true;
                }
            }

            if (hasImg) {
                photoContainer.getChildren().add(photoPreview);
            } else {
                Node userIcon = IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 36);
                photoContainer.getChildren().add(userIcon);
            }
        };
        updatePhotoDisplay.run();

        VBox photoActions = new VBox(6);
        Label photoLbl = new Label("Roommate Profile Photo");
        photoLbl.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 13px;");

        HBox btnBox = new HBox(8);
        Button uploadBtn = new Button("📷 Choose Photo");
        uploadBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;");
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(container.getScene().getWindow());
            if (file != null) {
                selectedPhotoFile[0] = file;
                updatePhotoDisplay.run();
            }
        });

        Button removePhotoBtn = new Button("✕ Remove");
        removePhotoBtn.setStyle(Theme.outlineBtnStyle() + " -fx-font-size: 12px; -fx-padding: 5px 12px;");
        removePhotoBtn.setOnAction(e -> {
            selectedPhotoFile[0] = null;
            uploadedPhotoUrl[0] = "";
            updatePhotoDisplay.run();
        });

        btnBox.getChildren().addAll(uploadBtn, removePhotoBtn);
        photoActions.getChildren().addAll(photoLbl, btnBox);
        photoRow.getChildren().addAll(photoContainer, photoActions);

        // Personal Details Grid (Safe Auto-fill from Current User)
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setMaxWidth(Double.MAX_VALUE);

        // Name
        Label nameLbl = new Label("Full Name *");
        nameLbl.setStyle(formLabelStyle());
        TextField nameField = new TextField(existing != null ? existing.getName() : (currentUser != null && currentUser.getName() != null && !currentUser.getName().equals("Not provided") ? currentUser.getName() : ""));
        nameField.setPromptText("Enter your name");
        nameField.setStyle(Theme.inputFieldStyle());

        // Contact Phone
        Label phoneLbl = new Label("Contact Mobile Number *");
        phoneLbl.setStyle(formLabelStyle());
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : (currentUser != null && currentUser.getPhone() != null && !currentUser.getPhone().equals("Not provided") ? currentUser.getPhone() : ""));
        phoneField.setPromptText("Enter your mobile number");
        phoneField.setStyle(Theme.inputFieldStyle());

        // Gender
        Label genderLbl = new Label("Gender *");
        genderLbl.setStyle(formLabelStyle());
        ComboBox<String> formGenderCombo = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        if (existing != null && existing.getGender() != null) {
            formGenderCombo.setValue(existing.getGender());
        } else {
            formGenderCombo.getSelectionModel().select(0);
        }
        formGenderCombo.setStyle(Theme.comboBoxStyle());
        formGenderCombo.setMaxWidth(Double.MAX_VALUE);

        // Age
        Label ageLbl = new Label("Age");
        ageLbl.setStyle(formLabelStyle());
        Spinner<Integer> ageSpinner = new Spinner<>(17, 45, existing != null && existing.getAge() > 0 ? existing.getAge() : 20);
        ageSpinner.setEditable(true);
        ageSpinner.setStyle(Theme.inputFieldStyle());
        ageSpinner.setMaxWidth(Double.MAX_VALUE);

        // College
        Label collegeLbl = new Label("College / University *");
        collegeLbl.setStyle(formLabelStyle());
        TextField collegeField = new TextField(existing != null ? existing.getCollege() : (currentUser != null && currentUser.getCollege() != null && !currentUser.getCollege().equals("Not provided") ? currentUser.getCollege() : ""));
        collegeField.setPromptText("e.g. COEP Technological University");
        collegeField.setStyle(Theme.inputFieldStyle());

        // Course
        Label courseLbl = new Label("Course / Department *");
        courseLbl.setStyle(formLabelStyle());
        TextField courseField = new TextField(existing != null ? existing.getCourse() : (currentUser != null && currentUser.getBranch() != null && !currentUser.getBranch().equals("Not provided") ? currentUser.getBranch() : ""));
        courseField.setPromptText("e.g. B.Tech Computer Engineering");
        courseField.setStyle(Theme.inputFieldStyle());

        // Year of Study
        Label yearLbl = new Label("Year of Study");
        yearLbl.setStyle(formLabelStyle());
        ComboBox<String> yearCombo = new ComboBox<>(FXCollections.observableArrayList("1st Year", "2nd Year", "3rd Year", "4th Year", "Postgraduate", "PhD"));
        if (existing != null && existing.getYear() != null && !existing.getYear().isEmpty()) {
            yearCombo.setValue(existing.getYear());
        } else {
            yearCombo.getSelectionModel().select(2); // default 3rd Year
        }
        yearCombo.setStyle(Theme.comboBoxStyle());
        yearCombo.setMaxWidth(Double.MAX_VALUE);

        // Preferred Location
        Label locLbl = new Label("Preferred Location / Area *");
        locLbl.setStyle(formLabelStyle());
        ComboBox<String> formLocCombo = new ComboBox<>(FXCollections.observableArrayList("Kothrud", "Baner", "Hinjewadi", "Viman Nagar", "Wakad", "Aundh", "Shivajinagar", "Pune"));
        formLocCombo.setEditable(true);
        if (existing != null && existing.getLocation() != null && !existing.getLocation().isEmpty()) {
            formLocCombo.setValue(existing.getLocation());
        } else {
            formLocCombo.getSelectionModel().select(0);
        }
        formLocCombo.setStyle(Theme.comboBoxStyle());
        formLocCombo.setMaxWidth(Double.MAX_VALUE);

        // Preferred Accommodation Type
        Label accomLbl = new Label("Preferred Accommodation Type *");
        accomLbl.setStyle(formLabelStyle());
        ComboBox<String> formAccomCombo = new ComboBox<>(FXCollections.observableArrayList("Single Room", "2 Sharing Flat", "3 Sharing Flat", "1 BHK Flat", "2 BHK Flat", "PG Sharing", "Any"));
        if (existing != null && existing.getAccommodationType() != null && !existing.getAccommodationType().isEmpty()) {
            formAccomCombo.setValue(existing.getAccommodationType());
        } else {
            formAccomCombo.getSelectionModel().select(1); // default 2 Sharing Flat
        }
        formAccomCombo.setStyle(Theme.comboBoxStyle());
        formAccomCombo.setMaxWidth(Double.MAX_VALUE);

        // Budget Range
        Label budgetLbl = new Label("Monthly Budget Range *");
        budgetLbl.setStyle(formLabelStyle());
        TextField budgetField = new TextField(existing != null ? existing.getBudget() : "₹ 4,000 - ₹ 6,000 / mo");
        budgetField.setPromptText("e.g. ₹ 5,000 - ₹ 7,500 / mo");
        budgetField.setStyle(Theme.inputFieldStyle());

        // Roommates Needed
        Label neededLbl = new Label("Roommates Needed");
        neededLbl.setStyle(formLabelStyle());
        ComboBox<String> neededCombo = new ComboBox<>(FXCollections.observableArrayList("1 Roommate", "2 Roommates", "3+ Roommates", "Any"));
        if (existing != null && existing.getRoommatesNeeded() != null && !existing.getRoommatesNeeded().isEmpty()) {
            neededCombo.setValue(existing.getRoommatesNeeded());
        } else {
            neededCombo.getSelectionModel().select(0);
        }
        neededCombo.setStyle(Theme.comboBoxStyle());
        neededCombo.setMaxWidth(Double.MAX_VALUE);

        // Lifestyle & Habits Checklist
        Label habitLbl = new Label("Lifestyle Habits & Preferences");
        habitLbl.setStyle(formLabelStyle());

        List<CheckBox> habitBoxes = new ArrayList<>();
        String[] habitTags = {"Clean & Organized", "Non-Smoker", "Non-Drinker", "Vegetarian", "Studious / Quiet Hours", "Early Riser", "Night Owl"};
        FlowPane habitsPane = new FlowPane(10, 8);

        String currentPref = existing != null && existing.getPreference() != null ? existing.getPreference() : "";
        for (String tag : habitTags) {
            CheckBox cb = new CheckBox(tag);
            cb.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";");
            if (currentPref.contains(tag) || (existing == null && (tag.equals("Clean & Organized") || tag.equals("Non-Smoker")))) {
                cb.setSelected(true);
            }
            habitBoxes.add(cb);
            habitsPane.getChildren().add(cb);
        }

        // About Me / Bio
        Label bioLbl = new Label("About Me & Expectations");
        bioLbl.setStyle(formLabelStyle());
        TextArea bioArea = new TextArea(existing != null ? existing.getBio() : "");
        bioArea.setPromptText("Short introduction about your daily habits, study schedule, hobbies, and what you're looking for in a flatmate...");
        bioArea.setPrefRowCount(3);
        bioArea.setStyle(Theme.inputFieldStyle());

        // Place fields in grid
        grid.add(nameLbl, 0, 0);
        grid.add(nameField, 0, 1);
        grid.add(phoneLbl, 1, 0);
        grid.add(phoneField, 1, 1);

        grid.add(genderLbl, 0, 2);
        grid.add(formGenderCombo, 0, 3);
        grid.add(ageLbl, 1, 2);
        grid.add(ageSpinner, 1, 3);

        grid.add(collegeLbl, 0, 4);
        grid.add(collegeField, 0, 5);
        grid.add(courseLbl, 1, 4);
        grid.add(courseField, 1, 5);

        grid.add(yearLbl, 0, 6);
        grid.add(yearCombo, 0, 7);
        grid.add(locLbl, 1, 6);
        grid.add(formLocCombo, 1, 7);

        grid.add(accomLbl, 0, 8);
        grid.add(formAccomCombo, 0, 9);
        grid.add(budgetLbl, 1, 8);
        grid.add(budgetField, 1, 9);

        grid.add(neededLbl, 0, 10);
        grid.add(neededCombo, 0, 11);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Actions Row
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(16, 0, 0, 0));

        Button cancelBtn = new Button("← Back to Browse");
        cancelBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 8px 18px;");
        cancelBtn.setOnAction(e -> { if (onFinish != null) onFinish.run(); });

        Button submitBtn = new Button(isEditing ? "✓ Save Profile Changes" : "✓ Register as Roommate");
        submitBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 8px 22px; -fx-font-weight: 800;");

        submitBtn.setOnAction(e -> {
            String nameVal = nameField.getText() != null ? nameField.getText().trim() : "";
            String phoneVal = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String collegeVal = collegeField.getText() != null ? collegeField.getText().trim() : "";
            String courseVal = courseField.getText() != null ? courseField.getText().trim() : "";
            String locVal = formLocCombo.getValue() != null ? formLocCombo.getValue().trim() : "Pune";
            String budgetVal = budgetField.getText() != null ? budgetField.getText().trim() : "Flexible";
            String genderVal = formGenderCombo.getValue() != null ? formGenderCombo.getValue() : "Any";
            String accomVal = formAccomCombo.getValue() != null ? formAccomCombo.getValue() : "Any";
            String yearVal = yearCombo.getValue() != null ? yearCombo.getValue() : "3rd Year";
            String neededVal = neededCombo.getValue() != null ? neededCombo.getValue() : "1 Roommate";
            int ageVal = ageSpinner.getValue() != null ? ageSpinner.getValue() : 20;
            String bioVal = bioArea.getText() != null ? bioArea.getText().trim() : "";

            if (nameVal.isEmpty()) {
                showAlert("Missing Name", "Please enter your full name.");
                return;
            }
            if (phoneVal.isEmpty()) {
                showAlert("Missing Phone", "Please enter your contact mobile number.");
                return;
            }

            // Gather selected habits
            StringBuilder habitsBuilder = new StringBuilder();
            for (CheckBox cb : habitBoxes) {
                if (cb.isSelected()) {
                    if (habitsBuilder.length() > 0) habitsBuilder.append(", ");
                    habitsBuilder.append(cb.getText());
                }
            }
            String prefVal = habitsBuilder.length() > 0 ? habitsBuilder.toString() : "Clean & Organized";

            // Upload photo if chosen
            String finalPhotoUrl = uploadedPhotoUrl[0];
            String finalPublicId = "";
            if (selectedPhotoFile[0] != null) {
                try {
                    CloudinaryService.UploadResult res = CloudinaryService.uploadImage(selectedPhotoFile[0], "roommateProfiles");
                    if (res != null && res.isSuccess()) {
                        finalPhotoUrl = res.getSecureUrl();
                        finalPublicId = res.getPublicId();
                    } else {
                        finalPhotoUrl = selectedPhotoFile[0].getAbsolutePath();
                    }
                } catch (Exception ex) {
                    finalPhotoUrl = selectedPhotoFile[0].getAbsolutePath();
                }
            }

            String profileId = isEditing ? existing.getId() : "rm_" + System.currentTimeMillis();
            String userUid = (currentUser != null && currentUser.getUid() != null && !currentUser.getUid().isEmpty())
                    ? currentUser.getUid()
                    : (currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "stud_" + System.currentTimeMillis());
            String emailVal = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";

            RoommateItem newOrUpdated = new RoommateItem(
                profileId,
                userUid,
                nameVal,
                emailVal,
                phoneVal,
                genderVal,
                ageVal,
                collegeVal,
                courseVal,
                yearVal,
                locVal,
                budgetVal,
                accomVal,
                neededVal,
                prefVal,
                bioVal,
                finalPhotoUrl,
                finalPublicId,
                "ACTIVE",
                isEditing ? existing.getCreatedAt() : System.currentTimeMillis(),
                System.currentTimeMillis()
            );

            // Save to Firestore
            new RoommateDAOImpl().save(newOrUpdated);
            // Save to repository
            DataRepository.getInstance().addOrUpdateRoommate(newOrUpdated);

            showAlert("Success", isEditing
                ? "Your roommate profile has been successfully updated!"
                : "You are now registered as a roommate! Other students can discover and connect with you."
            );

            if (onFinish != null) onFinish.run();
        });

        if (isEditing) {
            Button deleteBtn = new Button("🗑️ Deactivate / Delete Profile");
            deleteBtn.setStyle(Theme.dangerBtnStyle() + " -fx-padding: 8px 14px;");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to deactivate and remove your roommate profile? Your main student account will remain unaffected.", ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirm Deactivation");
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.YES) {
                        new RoommateDAOImpl().delete(existing.getId());
                        DataRepository.getInstance().removeRoommate(existing.getId());
                        showAlert("Profile Removed", "Your roommate profile has been deactivated and removed from listings.");
                        if (onFinish != null) onFinish.run();
                    }
                });
            });
            actionsRow.getChildren().addAll(deleteBtn, cancelBtn, submitBtn);
        } else {
            actionsRow.getChildren().addAll(cancelBtn, submitBtn);
        }

        container.getChildren().addAll(
            titleBox,
            photoRow,
            grid,
            habitLbl,
            habitsPane,
            bioLbl,
            bioArea,
            actionsRow
        );

        return container;
    }

    // TAB 3: INCOMING ROOMMATE CONNECTION REQUESTS VIEW
    private Node createRequestsView(String currentUid, Runnable onRefresh) {
        VBox container = new VBox(16);
        container.setMaxWidth(Double.MAX_VALUE);

        Text secTitle = new Text("Incoming Roommate Requests");
        secTitle.setStyle(Theme.sectionHeaderStyle());

        Text secSub = new Text("Students who are interested in sharing flat or PG accommodation with you.");
        secSub.setStyle(Theme.mutedTextStyle());

        VBox titleBox = new VBox(2, secTitle, secSub);
        container.getChildren().add(titleBox);

        List<RoommateRequest> requests = DataRepository.getInstance().getIncomingRoommateRequests(currentUid);
        if (requests.isEmpty()) {
            List<RoommateRequest> fsReqs = new RoommateRequestDAOImpl().findByReceiverUid(currentUid);
            if (fsReqs != null && !fsReqs.isEmpty()) {
                for (RoommateRequest r : fsReqs) {
                    DataRepository.getInstance().addRoommateRequest(r);
                }
                requests = fsReqs;
            }
        }

        if (requests.isEmpty()) {
            EmptyStateNode empty = new EmptyStateNode(
                "No Incoming Connection Requests",
                "When other students view your roommate profile and click 'Connect / Request', their requests will appear here.",
                null
            );
            container.getChildren().add(empty);
            return container;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        for (RoommateRequest r : requests) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(16, 20, 16, 20));

            String status = r.getStatus() != null ? r.getStatus() : "PENDING";
            String accentColor = "PENDING".equalsIgnoreCase(status) ? "#D97706"
                    : "ACCEPTED".equalsIgnoreCase(status) ? Theme.PRIMARY : "#C62828";

            card.setStyle(
                "-fx-background-color: " + Theme.CARD_BG + ";"
                + "-fx-border-color: " + accentColor + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + ";"
                + "-fx-border-width: 3px 1px 1px 1px;"
                + "-fx-border-radius: 10px;"
                + "-fx-background-radius: 10px;"
            );

            HBox top = new HBox(12);
            top.setAlignment(Pos.CENTER_LEFT);

            VBox senderInfo = new VBox(2);
            HBox.setHgrow(senderInfo, Priority.ALWAYS);

            Text senderName = new Text("👤 Student: " + r.getSenderName());
            senderName.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 800;");

            Text metaLine = new Text("Email: " + r.getSenderEmail() + "  •  Phone: " + r.getSenderPhone() + "  •  Sent: " + sdf.format(new Date(r.getTimestamp())));
            metaLine.setStyle(Theme.mutedTextStyle());

            senderInfo.getChildren().addAll(senderName, metaLine);

            Label badge = new Label(status);
            badge.setStyle("PENDING".equalsIgnoreCase(status) ? Theme.warningBadgeStyle() : Theme.successBadgeStyle());

            top.getChildren().addAll(senderInfo, badge);

            if (r.getMessage() != null && !r.getMessage().trim().isEmpty()) {
                Label msgLbl = new Label("Message: \"" + r.getMessage() + "\"");
                msgLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-style: italic; -fx-font-size: 13px;");
                card.getChildren().add(msgLbl);
            }

            card.getChildren().add(top);

            if ("PENDING".equalsIgnoreCase(status)) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button acceptBtn = new Button("✓ Accept Request");
                acceptBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
                acceptBtn.setOnAction(e -> {
                    DataRepository.getInstance().updateRoommateRequestStatus(r.getRequestId(), "ACCEPTED");
                    new Thread(() -> new RoommateRequestDAOImpl().updateStatus(r.getRequestId(), "ACCEPTED")).start();
                    showAlert("Request Accepted", "You accepted the connection request from " + r.getSenderName() + "! You can now reach out to them directly at " + r.getSenderPhone());
                    if (onRefresh != null) onRefresh.run();
                });

                Button rejectBtn = new Button("✕ Decline");
                rejectBtn.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 12px; -fx-padding: 6px 14px;");
                rejectBtn.setOnAction(e -> {
                    DataRepository.getInstance().updateRoommateRequestStatus(r.getRequestId(), "REJECTED");
                    new Thread(() -> new RoommateRequestDAOImpl().updateStatus(r.getRequestId(), "REJECTED")).start();
                    showAlert("Request Declined", "Connection request declined.");
                    if (onRefresh != null) onRefresh.run();
                });

                actions.getChildren().addAll(acceptBtn, rejectBtn);
                card.getChildren().add(actions);
            }

            container.getChildren().add(card);
        }

        return container;
    }

    private String formLabelStyle() {
        return "-fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";";
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.show();
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
