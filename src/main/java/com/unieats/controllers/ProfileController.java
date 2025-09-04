package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.io.File;
import java.time.format.DateTimeFormatter;
import com.unieats.User;
import com.unieats.DatabaseManager;
import com.unieats.util.PasswordUtil;
import org.kordamp.ikonli.javafx.FontIcon;

public class ProfileController {
    
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileIdLabel;
    @FXML private Label profileCategoryLabel;
    @FXML private Label profileCreatedLabel;
    @FXML private Label profileUpdatedLabel;
    @FXML private Button backButton;
    @FXML private Button editProfileButton;
    @FXML private Button changePasswordButton;
    @FXML private ImageView profileImageView;
    // Bottom nav items on profile screen
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;
    
    private User currentUser;
    private DatabaseManager dbManager;
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        dbManager = DatabaseManager.getInstance();
        // Default active tab: Profile
        setActiveNav(navProfile);
    }
    
    private void setupEventHandlers() {
        backButton.setOnAction(e -> handleBack());
        editProfileButton.setOnAction(e -> handleEditProfile());
        changePasswordButton.setOnAction(e -> handleChangePassword());
        // Bottom navigation wiring
        if (navHome != null) navHome.setOnMouseClicked(e -> { setActiveNav(navHome); navigateToMenu(); });
        if (navOrders != null) navOrders.setOnMouseClicked(e -> { setActiveNav(navOrders); showAlert("Orders", "Orders screen coming soon."); });
        if (navCart != null) navCart.setOnMouseClicked(e -> { setActiveNav(navCart); navigateToCart(); });
        if (navFav != null) navFav.setOnMouseClicked(e -> { setActiveNav(navFav); showAlert("Favourite", "Favorites screen coming soon."); });
        if (navProfile != null) navProfile.setOnMouseClicked(e -> setActiveNav(navProfile));
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        displayUserInfo();
    }
    
    private void displayUserInfo() {
        if (currentUser != null) {
            profileNameLabel.setText(currentUser.getFullName());
            profileEmailLabel.setText(currentUser.getEmail());
            profileIdLabel.setText(String.valueOf(currentUser.getId()));
            profileCategoryLabel.setText(currentUser.getUserCategory().toUpperCase());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            if (currentUser.getCreatedAt() != null) {
                profileCreatedLabel.setText(currentUser.getCreatedAt().format(formatter));
            }
            if (currentUser.getUpdatedAt() != null) {
                profileUpdatedLabel.setText(currentUser.getUpdatedAt().format(formatter));
            }

            // Load profile picture if available
            if (profileImageView != null) {
                String path = currentUser.getProfilePicture();
                if (path != null && !path.isEmpty()) {
                    try {
                        File f = new File(path);
                        Image img = f.exists() ? new Image(f.toURI().toString(), 110, 110, true, true) : new Image(path, 110, 110, true, true);
                        profileImageView.setImage(img);
                    } catch (Exception ignored) {
                        profileImageView.setImage(null);
                    }
                } else {
                    profileImageView.setImage(null);
                }
            }
        }
    }
    
    

    @FXML
    private void handleBack() {
        navigateToMenu();
    }

    private void navigateToMenu() {
        try {
            // Load the menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();

            // Set the current user in the menu controller
            MenuController menuController = loader.getController();
            if (menuController != null) {
                menuController.setCurrentUser(currentUser);
            }
        } catch (IOException e) {
            System.err.println("Error navigating back to menu: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate back to menu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditProfile() {
        if (currentUser == null) {
            showAlert("Error", "No user is signed in.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your name and email");

        // Dialog controls
        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Full name");
        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email address");
        // Picture preview + chooser
        ImageView preview = new ImageView();
        preview.setFitWidth(80);
        preview.setFitHeight(80);
        preview.setPreserveRatio(true);
        String existingPic = currentUser.getProfilePicture();
        if (existingPic != null && !existingPic.isEmpty()) {
            try {
                File ef = new File(existingPic);
                Image ei = ef.exists() ? new Image(ef.toURI().toString(), 80, 80, true, true) : new Image(existingPic, 80, 80, true, true);
                preview.setImage(ei);
            } catch (Exception ignored) { }
        }
        Button choosePicBtn = new Button("Choose...");
        final String[] selectedPicPath = { existingPic };
        choosePicBtn.setOnAction(ev2 -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Picture");
            chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            Stage stage = (Stage) editProfileButton.getScene().getWindow();
            File selected = chooser.showOpenDialog(stage);
            if (selected != null) {
                selectedPicPath[0] = selected.getAbsolutePath();
                try {
                    preview.setImage(new Image(selected.toURI().toString(), 80, 80, true, true));
                } catch (Exception ignored) { }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Full name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Profile picture:"), 0, 2);
        grid.add(preview, 1, 2);
        grid.add(choosePicBtn, 1, 3);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Validation
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String fullName = nameField.getText() != null ? nameField.getText().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            if (fullName.isEmpty()) {
                ev.consume();
                showAlert("Validation Error", "Full name is required.");
                return;
            }
            if (email.isEmpty() || !email.contains("@")) {
                ev.consume();
                showAlert("Validation Error", "Enter a valid email address.");
                return;
            }

            // Attempt update
            try {
                currentUser.setFullName(fullName);
                currentUser.setEmail(email);
                boolean ok = dbManager.updateUser(currentUser);
                if (!ok) {
                    ev.consume();
                    showAlert("Update Failed", "Could not update profile. Please try again.");
                    return;
                }
                // Update profile picture if changed
                String newPic = selectedPicPath[0];
                String oldPic = existingPic;
                if ((newPic != null && !newPic.isEmpty()) && (oldPic == null || !newPic.equals(oldPic))) {
                    boolean picOk = dbManager.updateUserProfilePicture(currentUser.getId(), newPic);
                    if (!picOk) {
                        ev.consume();
                        showAlert("Update Failed", "Profile updated but picture change failed.");
                        return;
                    }
                    currentUser.setProfilePicture(newPic);
                }
            } catch (Exception ex) {
                ev.consume();
                String msg = ex.getMessage();
                if (msg != null && msg.toLowerCase().contains("unique")) {
                    showAlert("Email In Use", "This email is already registered. Try another.");
                } else {
                    showAlert("Error", "Failed to update profile: " + ex.getMessage());
                }
                return;
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                // Refresh from DB (ensures timestamps updated)
                User refreshed = dbManager.getUserByEmail(currentUser.getEmail());
                if (refreshed != null) {
                    this.currentUser = refreshed;
                }
                displayUserInfo();
                showAlert("Success", "Profile updated successfully.");
            }
        });
    }
    
    @FXML
    private void handleChangePassword() {
        if (currentUser == null) {
            showAlert("Error", "No user is signed in.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Update your password");

        PasswordField currentField = new PasswordField();
        currentField.setPromptText("Current password");
        PasswordField newField = new PasswordField();
        newField.setPromptText("New password (min 6 chars)");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Current:"), 0, 0);
        grid.add(currentField, 1, 0);
        grid.add(new Label("New:"), 0, 1);
        grid.add(newField, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        Button changeBtn = (Button) dialog.getDialogPane().lookupButton(changeButtonType);
        changeBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String current = currentField.getText();
            String next = newField.getText();
            String confirm = confirmField.getText();

            if (current == null || current.isEmpty()) {
                ev.consume();
                showAlert("Validation Error", "Enter your current password.");
                return;
            }
            if (!PasswordUtil.verifyPassword(current, currentUser.getPassword())) {
                ev.consume();
                showAlert("Invalid Password", "Current password is incorrect.");
                return;
            }
            if (next == null || next.length() < 6) {
                ev.consume();
                showAlert("Validation Error", "New password must be at least 6 characters.");
                return;
            }
            if (!next.equals(confirm)) {
                ev.consume();
                showAlert("Validation Error", "New password and confirmation do not match.");
                return;
            }

            try {
                currentUser.setPassword(PasswordUtil.hashPassword(next));
                boolean ok = dbManager.updateUser(currentUser);
                if (!ok) {
                    ev.consume();
                    showAlert("Update Failed", "Could not change password. Please try again.");
                    return;
                }
            } catch (Exception ex) {
                ev.consume();
                showAlert("Error", "Failed to change password: " + ex.getMessage());
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == changeButtonType) {
                // Refresh current user from DB
                User refreshed = dbManager.getUserByEmail(currentUser.getEmail());
                if (refreshed != null) {
                    this.currentUser = refreshed;
                }
                displayUserInfo();
                showAlert("Success", "Password changed successfully.");
            }
        });
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Bottom Nav Active State Handling ---
    private void setActiveNav(VBox active) {
        if (navHome != null) applyActive(navHome, navHome == active, "#2e7d32");
        if (navOrders != null) applyActive(navOrders, navOrders == active, "#ff6b35");
        if (navCart != null) applyActive(navCart, navCart == active, "#0d6efd");
        if (navFav != null) applyActive(navFav, navFav == active, "#e63946");
        if (navProfile != null) applyActive(navProfile, navProfile == active, "#6f42c1");
    }

    private void applyActive(VBox tab, boolean active, String colorHex) {
        if (tab == null) return;
        if (tab.getChildren().size() < 2) return;
        StackPane iconWrap = (StackPane) tab.getChildren().get(0);
        Label label = (Label) tab.getChildren().get(1);

        if (!iconWrap.getChildren().isEmpty() && iconWrap.getChildren().get(0) instanceof FontIcon) {
            FontIcon icon = (FontIcon) iconWrap.getChildren().get(0);
            icon.setIconColor(active ? javafx.scene.paint.Paint.valueOf(colorHex) : javafx.scene.paint.Paint.valueOf("#6c757d"));
        }

        String bg = active ? String.format("-fx-background-color: %s1A; -fx-background-radius: 12; -fx-padding: 8;", colorHex.replace("#","#"))
                           : "-fx-background-radius: 12; -fx-padding: 8;";
        iconWrap.setStyle(bg);

        label.setStyle(active ? String.format("-fx-font-size: 10px; -fx-text-fill: %s; -fx-font-weight: bold;", colorHex)
                              : "-fx-font-size: 10px; -fx-text-fill: #6c757d;");
    }

    private void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Parent root = loader.load();
            com.unieats.controllers.CartController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUserId(currentUser.getId());
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Cart");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }
}
