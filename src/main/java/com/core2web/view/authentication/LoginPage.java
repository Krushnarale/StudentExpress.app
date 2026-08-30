package com.core2web.view.authentication;

import com.core2web.Main;
import com.core2web.controller.AuthController;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class LoginPage {

    private Scene loginScene;

    public Scene getPageScene(Consumer<User.Role> onLoginSuccess) {
        return getPageScene(User.Role.STUDENT, onLoginSuccess, null, null);
    }

    public javafx.scene.Node getPageNode(User.Role initialRole, Consumer<User.Role> onLoginSuccess, Runnable onChangeRole) {
        return getPageNode(initialRole, onLoginSuccess, onChangeRole, null);
    }

    public javafx.scene.Node getPageNode(User.Role initialRole, Consumer<User.Role> onLoginSuccess, Runnable onChangeRole, Runnable onNavigateToSignUp) {
        if (initialRole == User.Role.SELLER) {
        }

        // Root — horizontal split
        HBox rootPane = new HBox();
        rootPane.setStyle("-fx-background-color: " + Theme.BG_COLOR + ";");

        // LEFT PANEL — green gradient branding (~46% width)
        VBox leftPanel = new VBox(32);
        leftPanel.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.46));
        leftPanel.setMinWidth(460);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(50, 52, 50, 52));
        leftPanel.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #2E4A18, #4F772D, #6A9E45);"
        );

        // Brand logo area
        VBox logoBlock = new VBox(14);
        logoBlock.setAlignment(Pos.CENTER_LEFT);

        HBox logoRow = new HBox(14);
        logoRow.setAlignment(Pos.CENTER_LEFT);

        // Round logo badge
        StackPane logoBadge = new StackPane();
        Circle badgeBg = new Circle(34, Color.web("#FFFFFF20"));
        Text badgeIcon = new Text("🎓");
        badgeIcon.setStyle("-fx-font-size: 32px;");
        logoBadge.getChildren().addAll(badgeBg, badgeIcon);

        Text brandName = new Text("StudentExpress");
        brandName.setStyle(
            "-fx-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 30px;"
            + "-fx-font-weight: 800;"
        );

        logoRow.getChildren().addAll(logoBadge, brandName);

        Text brandSub = new Text("Smart Student Rental, Buying & Selling Platform");
        brandSub.setStyle("-fx-fill: rgba(255,255,255,0.78); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px;");
        logoBlock.getChildren().addAll(logoRow, brandSub);

        // Role-specific badge description card
        VBox roleInfoCard = new VBox(10);
        roleInfoCard.setPadding(new Insets(20));
        roleInfoCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.12);"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: rgba(255, 255, 255, 0.25);"
            + "-fx-border-radius: 16px;"
        );

        Text rolePortalTitle = new Text(getRolePortalTitle(initialRole));
        rolePortalTitle.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 18px; -fx-font-weight: 800;");

        Text rolePortalDesc = new Text(getRolePortalDesc(initialRole));
        rolePortalDesc.setStyle("-fx-fill: rgba(255,255,255,0.85); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        rolePortalDesc.setWrappingWidth(380);

        roleInfoCard.getChildren().addAll(rolePortalTitle, rolePortalDesc);

        // Features bullets list
        VBox featuresList = new VBox(20);
        featuresList.getChildren().addAll(
            featureRow("🏠", "Student Rentals & PG Rooms", "Verified listings near college campuses"),
            featureRow("🛍️", "Buy & Sell Peer Marketplace", "Books, electronics, cycles & furniture"),
            featureRow("🛠️", "Campus Student Services", "Laundry, mess, room cleaning & repairs")
        );

        leftPanel.getChildren().addAll(logoBlock, roleInfoCard, featuresList);

        // RIGHT PANEL — Clean login form card (~54% width)
        StackPane rightWrapper = new StackPane();
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.setPadding(new Insets(20, 30, 20, 30));

        VBox card = new VBox(13);
        card.setMaxWidth(480);
        card.setPrefWidth(480);
        card.setPadding(new Insets(24, 40, 24, 40));

        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 20px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 24, 0, 0, 8);"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 20px;"
        );

        // Top Back Button to Portal Selection
        Button changeRoleLink = new Button("← Back to Portal Selection");
        changeRoleLink.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 11px; -fx-padding: 5px 12px;");
        changeRoleLink.setOnAction(e -> {
            if (onChangeRole != null) onChangeRole.run();
        });

        // Form Title Block
        VBox titleBox = new VBox(3);
        String titleString = (initialRole == User.Role.SELLER) 
                ? "Student Seller Login" 
                : ((initialRole == User.Role.STUDENT) ? "Student Login" : "Welcome back 👋");
        Text loginTitle = new Text(titleString);
        loginTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");

        String subString = (initialRole == User.Role.SELLER)
                ? "Sign in to manage your listings and purchase requests."
                : "Sign in to access your StudentExpress account.";
        Text loginSub = new Text(subString);
        loginSub.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(loginTitle, loginSub);

        // Email Field
        VBox emailGroup = new VBox(5);
        Label emailLbl = new Label("Email Address / Student ID");
        emailLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email or student ID");
        emailField.setStyle(Theme.inputFieldStyle());
        emailGroup.getChildren().addAll(emailLbl, emailField);

        // Password Field
        VBox passGroup = new VBox(5);
        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passField.setStyle(Theme.inputFieldStyle());
        passGroup.getChildren().addAll(passLbl, passField);

        // Forgot password
        HBox forgotBox = new HBox();
        forgotBox.setAlignment(Pos.CENTER_RIGHT);
        Hyperlink forgotLink = new Hyperlink("Forgot password?");
        forgotLink.setStyle("-fx-text-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 600; -fx-underline: false;");
        forgotBox.getChildren().add(forgotLink);

        // Submit Button
        String submitBtnText = (initialRole == User.Role.SELLER)
                ? "Sign In as Seller  →"
                : ((initialRole == User.Role.STUDENT) ? "Sign In as Student  →" : "Sign In as " + initialRole.name() + "  →");
        Button loginBtn = new Button(submitBtnText);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(Theme.primaryBtnStyle()
            + "-fx-font-size: 14px;"
            + "-fx-font-weight: 800;"
            + "-fx-padding: 10px;"
            + "-fx-background-radius: 12px;"
        );

        loginBtn.setOnAction(e -> {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String password = passField.getText() == null ? "" : passField.getText();
            if (email.isEmpty() || password.isEmpty()) {
                showError(loginBtn, "Please enter email and password.");
                return;
            }
            AuthController authController = new AuthController();
            AuthController.AuthResult result = authController.login(email, password, initialRole);
            if (!result.isSuccess()) {
                showError(loginBtn, result.getMessage());
                return;
            }
            if (result.getUser() != null) {
                DataRepository.getInstance().setCurrentUser(result.getUser());
            }
            if (onLoginSuccess != null) onLoginSuccess.accept(initialRole);
        });

        // Switch button between Student Login and Seller Login
        VBox switchBox = new VBox(6);
        switchBox.setAlignment(Pos.CENTER);
        switchBox.setPadding(new Insets(10, 0, 4, 0));

        if (initialRole == User.Role.STUDENT) {
            Text switchPrompt = new Text("Are you a Seller?");
            switchPrompt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
            Button switchBtn = new Button("Login as Seller");
            switchBtn.setMaxWidth(Double.MAX_VALUE);
            switchBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 13px; -fx-padding: 9px 14px; -fx-font-weight: 800; -fx-background-radius: 12px;");
            switchBtn.setOnAction(e -> {
                Main.showLoginPageWithRole(User.Role.SELLER);
            });
            switchBox.getChildren().addAll(switchPrompt, switchBtn);
        } else if (initialRole == User.Role.SELLER) {
            Text switchPrompt = new Text("Are you a Student?");
            switchPrompt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
            Button switchBtn = new Button("Login as Student");
            switchBtn.setMaxWidth(Double.MAX_VALUE);
            switchBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-font-size: 13px; -fx-padding: 9px 14px; -fx-font-weight: 800; -fx-background-radius: 12px;");
            switchBtn.setOnAction(e -> {
                Main.showLoginPageWithRole(User.Role.STUDENT);
            });
            switchBox.getChildren().addAll(switchPrompt, switchBtn);

        }

        HBox footerBox = new HBox(6);
        footerBox.setAlignment(Pos.CENTER);
        Text noAccountText = new Text("Don't have an account?");
        noAccountText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        Hyperlink signUpLink = new Hyperlink("Sign Up Free");
        signUpLink.setStyle("-fx-text-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 800; -fx-font-size: 13px; -fx-underline: false;");
        signUpLink.setOnAction(e -> {
            if (onNavigateToSignUp != null) onNavigateToSignUp.run();
        });
        footerBox.getChildren().addAll(noAccountText, signUpLink);

        card.getChildren().addAll(
            changeRoleLink, titleBox, emailGroup, passGroup,
            forgotBox, loginBtn, switchBox, footerBox
        );

        rightWrapper.getChildren().add(card);
        rootPane.getChildren().addAll(leftPanel, rightWrapper);

        ScrollPane scrollPane = new ScrollPane(rootPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(User.Role initialRole, Consumer<User.Role> onLoginSuccess, Runnable onChangeRole) {
        return getPageScene(initialRole, onLoginSuccess, onChangeRole, null);
    }

    public Scene getPageScene(User.Role initialRole, Consumer<User.Role> onLoginSuccess, Runnable onChangeRole, Runnable onNavigateToSignUp) {
        javafx.scene.Node node = getPageNode(initialRole, onLoginSuccess, onChangeRole, onNavigateToSignUp);
        loginScene = new Scene(new BorderPane(node), 1000, 650);
        return loginScene;
    }

    private String getRolePortalTitle(User.Role role) {
        if (role == User.Role.SELLER) return "Student Seller Portal Access";
        if (role == User.Role.STUDENT) return "Student Portal Access";
        return role.name() + " Portal Access";
    }

    private String getRolePortalDesc(User.Role role) {
        if (role == User.Role.SELLER) return "Sell textbooks, cycles, electronics, and stationery directly to other students on campus.";
        if (role == User.Role.STUDENT) return "Find rentals, buy study materials, locate roommates, and book student campus services.";
        return "Secure workspace for all your " + role.name() + " related operations and management.";
    }

    private HBox featureRow(String icon, String title, String desc) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(46, 46);
        iconBadge.setMinSize(46, 46);
        iconBadge.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12px;");
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 20px;");
        iconBadge.getChildren().add(iconTxt);

        VBox textBlock = new VBox(3);
        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");
        Text descTxt = new Text(desc);
        descTxt.setStyle("-fx-fill: rgba(255,255,255,0.7); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        textBlock.getChildren().addAll(titleTxt, descTxt);

        row.getChildren().addAll(iconBadge, textBlock);
        return row;
    }

    private void showError(Button source, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("StudentExpress Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
