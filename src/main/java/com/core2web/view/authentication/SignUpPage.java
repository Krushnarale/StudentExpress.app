package com.core2web.view.authentication;

import com.core2web.model.User;
import com.core2web.controller.AuthController;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class SignUpPage {

    private Scene signUpScene;
    private User.Role selectedRole;

    public Scene getPageScene(Consumer<User.Role> onSignUpSuccess) {
        return getPageScene(User.Role.STUDENT, onSignUpSuccess, null, null);
    }

    public javafx.scene.Node getPageNode(
        User.Role initialRole,
        Consumer<User.Role> onSignUpSuccess,
        Runnable onChangeRole,
        Runnable onNavigateToLogin
    ) {
        this.selectedRole = initialRole != null ? initialRole : User.Role.STUDENT;

        // Root — horizontal split
        HBox rootPane = new HBox();
        rootPane.setStyle("-fx-background-color: " + Theme.BG_COLOR + ";");

        // ─────────────────────────────────────────────
        // LEFT PANEL — green gradient branding (~44% width)
        // ─────────────────────────────────────────────
        VBox leftPanel = new VBox(28);
        leftPanel.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.44));
        leftPanel.setMinWidth(440);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(40, 48, 40, 48));
        leftPanel.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #2E4A18, #4F772D, #6A9E45);"
        );

        // Brand logo area
        VBox logoBlock = new VBox(12);
        logoBlock.setAlignment(Pos.CENTER_LEFT);

        HBox logoRow = new HBox(14);
        logoRow.setAlignment(Pos.CENTER_LEFT);

        StackPane logoBadge = new StackPane();
        Circle badgeBg = new Circle(32, Color.web("#FFFFFF20"));
        Text badgeIcon = new Text("🎓");
        badgeIcon.setStyle("-fx-font-size: 30px;");
        logoBadge.getChildren().addAll(badgeBg, badgeIcon);

        Text brandName = new Text("StudentExpress");
        brandName.setStyle(
            "-fx-fill: white;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 28px;"
            + "-fx-font-weight: 800;"
        );

        logoRow.getChildren().addAll(logoBadge, brandName);

        Text brandSub = new Text("Smart Student Rental, Buying & Selling Platform");
        brandSub.setStyle("-fx-fill: rgba(255,255,255,0.78); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px;");
        logoBlock.getChildren().addAll(logoRow, brandSub);

        // Role Info Card
        VBox roleInfoCard = new VBox(8);
        roleInfoCard.setPadding(new Insets(18));
        roleInfoCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.12);"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: rgba(255, 255, 255, 0.25);"
            + "-fx-border-radius: 16px;"
        );

        Text rolePortalTitle = new Text(selectedRole.name() + " Account Registration");
        rolePortalTitle.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 17px; -fx-font-weight: 800;");

        Text rolePortalDesc = new Text("Create your verified account to start using StudentExpress services as a " + selectedRole.name().toLowerCase().replace('_', ' ') + ".");
        rolePortalDesc.setStyle("-fx-fill: rgba(255,255,255,0.85); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px;");
        rolePortalDesc.setWrappingWidth(360);

        roleInfoCard.getChildren().addAll(rolePortalTitle, rolePortalDesc);

        // Features list
        VBox featuresList = new VBox(18);
        featuresList.getChildren().addAll(
            featureRow("⚡", "Quick Registration", "Takes less than 1 minute to setup your campus profile"),
            featureRow("🔒", "100% Secure & Verified", "Strictly verified student and provider credentials"),
            featureRow("🚀", "Instant Access", "Connect immediately with verified rooms, sellers & services")
        );

        leftPanel.getChildren().addAll(logoBlock, roleInfoCard, featuresList);

        // ─────────────────────────────────────────────
        // RIGHT PANEL — Vertical Sign Up form card (~56% width)
        // ─────────────────────────────────────────────
        StackPane rightWrapper = new StackPane();
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.setPadding(new Insets(24, 40, 24, 40));

        VBox card = new VBox(12);
        card.setMaxWidth(440);
        card.setPadding(new Insets(24, 32, 24, 32));
        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 20px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 24, 0, 0, 8);"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 20px;"
        );

        // Back link row
        HBox navRow = new HBox();
        navRow.setAlignment(Pos.CENTER_LEFT);

        Button changeRoleLink = new Button("← Back to Portal Selection");
        changeRoleLink.setStyle(Theme.dangerBtnStyle() + " -fx-font-size: 11px; -fx-padding: 4px 10px;");
        changeRoleLink.setOnAction(e -> {
            if (onChangeRole != null) onChangeRole.run();
        });
        navRow.getChildren().add(changeRoleLink);

        // Form Title Block
        VBox titleBox = new VBox(3);
        Text signUpTitle = new Text("Create Account ✨");
        signUpTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");

        Text signUpSub = new Text("Join StudentExpress to unlock campus rentals and marketplace.");
        signUpSub.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px;");
        titleBox.getChildren().addAll(signUpTitle, signUpSub);

        // 1. Full Name Field
        VBox nameGroup = new VBox(4);
        Label nameLbl = new Label("Full Name");
        nameLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 700;");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setStyle(Theme.inputFieldStyle() + " -fx-padding: 8px 12px;");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameGroup.getChildren().addAll(nameLbl, nameField);

        // 2. Email Address Field
        VBox emailGroup = new VBox(4);
        Label emailLbl = new Label("Email Address");
        emailLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 700;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle(Theme.inputFieldStyle() + " -fx-padding: 8px 12px;");
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailGroup.getChildren().addAll(emailLbl, emailField);

        // 3. Phone Number Field
        VBox phoneGroup = new VBox(4);
        Label phoneLbl = new Label("Phone Number");
        phoneLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 700;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        phoneField.setStyle(Theme.inputFieldStyle() + " -fx-padding: 8px 12px;");
        phoneField.setMaxWidth(Double.MAX_VALUE);
        phoneGroup.getChildren().addAll(phoneLbl, phoneField);

        // 4. Password Field
        VBox passGroup = new VBox(4);
        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 700;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.setStyle(Theme.inputFieldStyle() + " -fx-padding: 8px 12px;");
        passField.setMaxWidth(Double.MAX_VALUE);
        passGroup.getChildren().addAll(passLbl, passField);

        // 5. Confirm Password Field
        VBox confirmGroup = new VBox(4);
        Label confirmLbl = new Label("Confirm Password");
        confirmLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 700;");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm password");
        confirmPassField.setStyle(Theme.inputFieldStyle() + " -fx-padding: 8px 12px;");
        confirmPassField.setMaxWidth(Double.MAX_VALUE);
        confirmGroup.getChildren().addAll(confirmLbl, confirmPassField);

        // 6. Terms & Conditions CheckBox
        CheckBox termsCheck = new CheckBox("I agree to the Terms of Service & Privacy Policy");
        termsCheck.setStyle("-fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-text-fill: " + Theme.TEXT_PRIMARY + ";");

        // Error message label
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #C62828; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 600;");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);

        // 7. Create Account Submit Button
        Button signUpBtn = new Button("Create Account  →");
        signUpBtn.setMaxWidth(Double.MAX_VALUE);
        signUpBtn.setStyle(Theme.primaryBtnStyle()
            + "-fx-font-size: 14px;"
            + "-fx-font-weight: 800;"
            + "-fx-padding: 10px;"
            + "-fx-background-radius: 12px;"
        );

        signUpBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String pass = passField.getText();
            String confirmPass = confirmPassField.getText();

            if (name.isEmpty()) {
                showError(errorLbl, "Please enter your full name.");
                return;
            }
            if (email.isEmpty() || !email.contains("@")) {
                showError(errorLbl, "Please enter a valid email address.");
                return;
            }
            if (phone.isEmpty()) {
                showError(errorLbl, "Please enter your mobile phone number.");
                return;
            }
            if (pass.length() < 4) {
                showError(errorLbl, "Password must be at least 4 characters.");
                return;
            }
            if (!pass.equals(confirmPass)) {
                showError(errorLbl, "Passwords do not match.");
                return;
            }
            if (!termsCheck.isSelected()) {
                showError(errorLbl, "Please accept the Terms of Service & Privacy Policy.");
                return;
            }

            errorLbl.setVisible(false);
            errorLbl.setManaged(false);

            User newUser = new User(UUID.randomUUID().toString(), name, email, phone, selectedRole, pass);
            AuthController authController = new AuthController();
            boolean registered = authController.register(newUser, pass);
            if (!registered) {
                showError(errorLbl, "Registration failed. The email may already be registered or the backend is unavailable.");
                return;
            }
            DataRepository.getInstance().setCurrentUser(authController.getCurrentUser() != null ? authController.getCurrentUser() : newUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Welcome to StudentExpress!");
            alert.setContentText("Account created successfully as " + selectedRole.name() + " (" + email + ").");
            alert.showAndWait();

            if (onSignUpSuccess != null) onSignUpSuccess.accept(selectedRole);
        });

        // 8. Footer navigation link to Sign In
        HBox footerBox = new HBox(6);
        footerBox.setAlignment(Pos.CENTER);
        Text hasAccountText = new Text("Already have an account?");
        hasAccountText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12.5px;");
        Hyperlink signInLink = new Hyperlink("Sign In");
        signInLink.setStyle("-fx-text-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 800; -fx-font-size: 12.5px; -fx-underline: false;");
        signInLink.setOnAction(e -> {
            if (onNavigateToLogin != null) onNavigateToLogin.run();
        });
        footerBox.getChildren().addAll(hasAccountText, signInLink);

        // Add all elements vertically in order
        card.getChildren().addAll(
            navRow, titleBox, nameGroup, emailGroup, phoneGroup,
            passGroup, confirmGroup, termsCheck, errorLbl,
            signUpBtn, footerBox
        );

        rightWrapper.getChildren().add(card);
        rootPane.getChildren().addAll(leftPanel, rightWrapper);

        ScrollPane scrollPane = new ScrollPane(rootPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(
        User.Role initialRole,
        Consumer<User.Role> onSignUpSuccess,
        Runnable onChangeRole,
        Runnable onNavigateToLogin
    ) {
        javafx.scene.Node node = getPageNode(initialRole, onSignUpSuccess, onChangeRole, onNavigateToLogin);
        signUpScene = new Scene(new BorderPane(node), 1000, 650);
        return signUpScene;
    }

    private void showError(Label errorLbl, String message) {
        errorLbl.setText("⚠ " + message);
        errorLbl.setVisible(true);
        errorLbl.setManaged(true);
    }

    private HBox featureRow(String icon, String title, String desc) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(42, 42);
        iconBadge.setMinSize(42, 42);
        iconBadge.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12px;");
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 18px;");
        iconBadge.getChildren().add(iconTxt);

        VBox textBlock = new VBox(2);
        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 700;");
        Text descTxt = new Text(desc);
        descTxt.setStyle("-fx-fill: rgba(255,255,255,0.7); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px;");
        descTxt.setWrappingWidth(320);
        textBlock.getChildren().addAll(titleTxt, descTxt);

        row.getChildren().addAll(iconBadge, textBlock);
        return row;
    }
}
