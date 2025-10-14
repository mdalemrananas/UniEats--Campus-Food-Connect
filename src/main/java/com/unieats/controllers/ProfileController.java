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
import javafx.scene.layout.HBox;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ProfileController {
    
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileEmailInfoLabel;
    @FXML private Label profilePhoneLabel;
    @FXML private Label profileAddressLabel;
    @FXML private Label profileCategoryLabel;
    @FXML private Label profileCreatedLabel;
    @FXML private Label profileUpdatedLabel;
    @FXML private Button backButton;
    @FXML private Button editProfileButton;
    @FXML private Button changePasswordButton;
    @FXML private Button logoutButton;
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
        logoutButton.setOnAction(e -> handleLogout());
        // Bottom navigation wiring
        if (navHome != null) navHome.setOnMouseClicked(e -> { setActiveNav(navHome); navigateToMenu(); });
        if (navOrders != null) navOrders.setOnMouseClicked(e -> { setActiveNav(navOrders); navigateToOrders(); });
        if (navCart != null) navCart.setOnMouseClicked(e -> { setActiveNav(navCart); navigateToCart(); });
        if (navFav != null) navFav.setOnMouseClicked(e -> { setActiveNav(navFav); navigateToFavourites(); });
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
            if (profileEmailInfoLabel != null) profileEmailInfoLabel.setText(currentUser.getEmail());
            if (profilePhoneLabel != null) profilePhoneLabel.setText(currentUser.getPhoneNo() == null || currentUser.getPhoneNo().isEmpty() ? "Not set" : currentUser.getPhoneNo());
            if (profileAddressLabel != null) profileAddressLabel.setText(currentUser.getAddress() == null || currentUser.getAddress().isEmpty() ? "Not set" : currentUser.getAddress());
            profileCategoryLabel.setText(currentUser.getUserCategory().toUpperCase());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            if (currentUser.getCreatedAt() != null) {
                profileCreatedLabel.setText(currentUser.getCreatedAt().format(formatter));
            }
            // Hide last updated per requirements

            // Load profile picture if available
            if (profileImageView != null) {
                String path = currentUser.getProfilePicture();
                if (path != null && !path.isEmpty()) {
                    try {
                        // First try to load as a resource (for packaged JAR)
                        String resourcePath = path.startsWith("images/") ? "/" + path : "/" + path;
                        try {
                            // Try to load as resource first
                            Image img = new Image(getClass().getResourceAsStream(resourcePath), 110, 110, true, true);
                            profileImageView.setImage(img);
                        } catch (Exception e) {
                            // If resource loading fails, try as file path
                            try {
                                File f = new File(path);
                                if (f.exists()) {
                                    Image img = new Image(f.toURI().toString(), 110, 110, true, true);
                                    profileImageView.setImage(img);
                                } else {
                                    // Try to find in the resources directory
                                    String userDir = System.getProperty("user.dir");
                                    Path imagePath = Paths.get(userDir, "src", "main", "resources", path);
                                    if (Files.exists(imagePath)) {
                                        Image img = new Image(imagePath.toUri().toString(), 110, 110, true, true);
                                        profileImageView.setImage(img);
                                    } else {
                                        profileImageView.setImage(null);
                                    }
                                }
                            } catch (Exception ex) {
                                profileImageView.setImage(null);
                            }
                        }
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
        dialog.setHeaderText("Update your profile");
        // Mobile size dialog
        dialog.getDialogPane().setPrefWidth(320);
        dialog.getDialogPane().setPrefHeight(480);

        // Dialog controls
        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Full name");
        // Email is no longer editable per requirement
        Label emailStatic = new Label(currentUser.getEmail());
        emailStatic.setStyle("-fx-text-fill: #6c757d;");
        TextField phoneField = new TextField(currentUser.getPhoneNo() == null ? "" : currentUser.getPhoneNo());
        phoneField.setPromptText("Phone no");
        TextArea addressArea = new TextArea(currentUser.getAddress() == null ? "" : currentUser.getAddress());
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(3);
        // Picture preview + chooser
        ImageView preview = new ImageView();
        preview.setFitWidth(80);
        preview.setFitHeight(80);
        preview.setPreserveRatio(true);
        String existingPic = currentUser.getProfilePicture();
        if (existingPic != null && !existingPic.isEmpty()) {
            try {
                // Try to load as resource first
                String resourcePath = existingPic.startsWith("images/") ? "/" + existingPic : "/" + existingPic;
                try {
                    preview.setImage(new Image(getClass().getResourceAsStream(resourcePath), 80, 80, true, true));
                } catch (Exception e) {
                    // If resource loading fails, try as file path
                    try {
                        File ef = new File(existingPic);
                        if (ef.exists()) {
                            preview.setImage(new Image(ef.toURI().toString(), 80, 80, true, true));
                        } else {
                            // Try to find in the resources directory
                            String userDir = System.getProperty("user.dir");
                            Path imagePath = Paths.get(userDir, "src", "main", "resources", existingPic);
                            if (Files.exists(imagePath)) {
                                preview.setImage(new Image(imagePath.toUri().toString(), 80, 80, true, true));
                            }
                        }
                    } catch (Exception ex) {
                        // Ignore and leave preview as is
                    }
                }
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
        grid.add(new Label("Profile picture:"), 0, 0);
        grid.add(preview, 1, 0);
        grid.add(choosePicBtn, 1, 1);
        grid.add(new Label("Full name:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailStatic, 1, 3);
        grid.add(new Label("Phone no:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("Address:"), 0, 5);
        grid.add(addressArea, 1, 5);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Validation
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String fullName = nameField.getText() != null ? nameField.getText().trim() : "";
            if (fullName.isEmpty()) {
                ev.consume();
                showAlert("Validation Error", "Full name is required.");
                return;
            }
            String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String addr = addressArea.getText() != null ? addressArea.getText().trim() : "";

            // Attempt update
            try {
                currentUser.setFullName(fullName);
                // Email not changed
                currentUser.setPhoneNo(phone.isEmpty() ? null : phone);
                currentUser.setAddress(addr.isEmpty() ? null : addr);
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
                    try {
                        // Get the resources directory path
                        String userDir = System.getProperty("user.dir");
                        Path targetDir = Paths.get(userDir, "src", "main", "resources", "images");
                        
                        // Create the target directory if it doesn't exist
                        Files.createDirectories(targetDir);
                        
                        // Generate a unique filename
                        String originalFilename = new File(newPic).getName();
                        String fileExtension = "";
                        int lastDot = originalFilename.lastIndexOf('.');
                        if (lastDot > 0) {
                            fileExtension = originalFilename.substring(lastDot);
                        }
                        String newFilename = "profile_" + currentUser.getId() + "_" + UUID.randomUUID().toString() + fileExtension;
                        
                        // Copy the file to the target directory
                        Path sourcePath = Paths.get(newPic);
                        Path targetPath = targetDir.resolve(newFilename);
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Update the user's profile picture path
                        String relativePath = "images/" + newFilename;
                        boolean picOk = dbManager.updateUserProfilePicture(currentUser.getId(), relativePath);
                        if (!picOk) {
                            ev.consume();
                            showAlert("Update Failed", "Profile updated but picture change failed.");
                            return;
                        }
                        currentUser.setProfilePicture(relativePath);
                    } catch (Exception ex) {
                        ev.consume();
                        showAlert("Error", "Failed to save profile picture: " + ex.getMessage());
                        return;
                    }
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
        Button toggleCurrent = new Button();
        toggleCurrent.setGraphic(new FontIcon("fas-eye"));
        Button toggleNew = new Button();
        toggleNew.setGraphic(new FontIcon("fas-eye"));
        Button toggleConfirm = new Button();
        toggleConfirm.setGraphic(new FontIcon("fas-eye"));
        TextField currentText = new TextField(); currentText.setManaged(false); currentText.setVisible(false);
        TextField newText = new TextField(); newText.setManaged(false); newText.setVisible(false);
        TextField confirmText = new TextField(); confirmText.setManaged(false); confirmText.setVisible(false);
        currentText.textProperty().bindBidirectional(currentField.textProperty());
        newText.textProperty().bindBidirectional(newField.textProperty());
        confirmText.textProperty().bindBidirectional(confirmField.textProperty());
        toggleCurrent.setOnAction(e -> toggleFieldVisibility(currentField, currentText, (FontIcon) toggleCurrent.getGraphic()));
        toggleNew.setOnAction(e -> toggleFieldVisibility(newField, newText, (FontIcon) toggleNew.getGraphic()));
        toggleConfirm.setOnAction(e -> toggleFieldVisibility(confirmField, confirmText, (FontIcon) toggleConfirm.getGraphic()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Current:"), 0, 0);
        HBox r0 = new HBox(6, currentField, currentText, toggleCurrent);
        grid.add(r0, 1, 0);
        grid.add(new Label("New:"), 0, 1);
        HBox r1 = new HBox(6, newField, newText, toggleNew);
        grid.add(r1, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        HBox r2 = new HBox(6, confirmField, confirmText, toggleConfirm);
        grid.add(r2, 1, 2);
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

    @FXML
    private void handleLogout() {
        if (currentUser == null) {
            showAlert("Error", "No user is signed in.");
            return;
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to sign in again to access your account.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Navigate to signin page
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) logoutButton.getScene().getWindow();
                    Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
                    stage.setScene(scene);
                    stage.setTitle("UniEats - Sign In");
                    stage.show();

                } catch (IOException e) {
                    showAlert("Navigation Error", "Failed to navigate to sign in page: " + e.getMessage());
                }
            }
        });
    }

    private void toggleFieldVisibility(PasswordField pwd, TextField txt, FontIcon icon) {
        boolean show = !txt.isVisible();
        txt.setManaged(show);
        txt.setVisible(show);
        pwd.setManaged(!show);
        pwd.setVisible(!show);
        icon.setIconLiteral(show ? "fas-eye-slash" : "fas-eye");
        if (show) { txt.requestFocus(); txt.positionCaret(txt.getText() != null ? txt.getText().length() : 0); }
        else { pwd.requestFocus(); pwd.positionCaret(pwd.getText() != null ? pwd.getText().length() : 0); }
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

	private void navigateToOrders() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_orders.fxml"));
			Parent root = loader.load();
			MyOrdersController controller = loader.getController();
			if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
			Stage stage = (Stage) backButton.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.setTitle("UniEats - My Orders");
			stage.show();
		} catch (IOException e) {
			showAlert("Navigation Error", e.getMessage());
		}
	}

	private void navigateToFavourites() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/wishlist.fxml"));
			Parent root = loader.load();
			WishlistController controller = loader.getController();
			if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
			Stage stage = (Stage) backButton.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.setTitle("UniEats - Favourites");
			stage.show();
		} catch (IOException e) {
			showAlert("Navigation Error", e.getMessage());
		}
	}
}
