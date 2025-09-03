package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import com.unieats.User;
import com.unieats.DatabaseManager;
import com.unieats.util.PasswordUtil;

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
    
    private User currentUser;
    private DatabaseManager dbManager;
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        dbManager = DatabaseManager.getInstance();
    }
    
    private void setupEventHandlers() {
        backButton.setOnAction(e -> handleBack());
        editProfileButton.setOnAction(e -> handleEditProfile());
        changePasswordButton.setOnAction(e -> handleChangePassword());
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
        }
    }
    
    @FXML
    private void handleBack() {
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

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Full name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
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
}
