package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import com.unieats.User;

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
    
    @FXML
    public void initialize() {
        setupEventHandlers();
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
        showAlert("Edit Profile", "Edit profile functionality will be implemented here.");
    }
    
    @FXML
    private void handleChangePassword() {
        showAlert("Change Password", "Change password functionality will be implemented here.");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
