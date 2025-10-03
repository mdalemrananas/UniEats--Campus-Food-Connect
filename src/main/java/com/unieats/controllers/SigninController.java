package com.unieats.controllers;

import com.unieats.DatabaseManager;
import com.unieats.User;
import com.unieats.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class SigninController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    private DatabaseManager dbManager;
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        setupErrorLabel();
    }
    
    private void setupErrorLabel() {
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void handleSignIn() {
        // Clear previous error
        errorLabel.setVisible(false);
        
        // Validate fields
        if (!validateFields()) {
            return;
        }
        
        // Authenticate user
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        User user = dbManager.getUserByEmail(email);
        
        if (user == null) {
            showError("No account found with this email address.");
            return;
        }
        
        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            showError("Invalid password. Please try again.");
            return;
        }
        
        // Check shop status if user is a seller
        if ("seller".equalsIgnoreCase(user.getUserCategory())) {
            String shopStatus = dbManager.getShopStatus(user.getId());
            
            if (shopStatus == null) {
                showError("No shop found for this seller account. Please contact support.");
                return;
            }
            
            switch (shopStatus.toLowerCase()) {
                case "pending" -> {
                    showError("Your shop approval is pending. Please wait for admin approval.");
                    return;
                }
                case "rejected" -> {
                    showError("Your shop approval has been rejected. Please contact support for more information.");
                    return;
                }
                case "approved" -> {
                    // Continue with login
                }
                default -> {
                    showError("Invalid shop status. Please contact support.");
                    return;
                }
            }
        }
        
        // Authentication successful
        showSuccess("Sign in successful! Welcome back, " + user.getFullName() + "!");
        
        // Set the current user in DatabaseManager
        DatabaseManager.setCurrentUser(user);

        // Route based on role
        switch (user.getUserCategory().toLowerCase()) {
            case "admin" -> navigateTo("/fxml/admin.fxml", "UniEats - Admin");
            case "seller" -> navigateTo("/fxml/stall.fxml", "UniEats - Stall");
            default -> navigateToMenu();
        }
    }
    
    private boolean validateFields() {
        // Email validation
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            showError("Email is required.");
            return false;
        }
        
        // Password validation
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            showError("Password is required.");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        errorLabel.setTextFill(Color.GREEN);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    @FXML
    private void handleBack() {
        navigateToHome();
    }
    
    @FXML
    private void handleSignUp() {
        navigateToSignup();
    }
    
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            
            // Get the current stage from any scene in the current window
            Stage stage = null;
            if (emailField != null && emailField.getScene() != null) {
                stage = (Stage) emailField.getScene().getWindow();
            } else if (passwordField != null && passwordField.getScene() != null) {
                stage = (Stage) passwordField.getScene().getWindow();
            }
            
            if (stage != null) {
                Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
                
                stage.setScene(scene);
                stage.show();
            } else {
                System.err.println("Could not find current stage for navigation");
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to home: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
            Parent root = loader.load();
            
            // Get the current stage from any scene in the current window
            Stage stage = null;
            if (emailField != null && emailField.getScene() != null) {
                stage = (Stage) emailField.getScene().getWindow();
            } else if (passwordField != null && passwordField.getScene() != null) {
                stage = (Stage) passwordField.getScene().getWindow();
            }
            
            if (stage != null) {
                Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
                
                stage.setScene(scene);
                stage.show();
            } else {
                System.err.println("Could not find current stage for navigation");
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to signup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Failed to navigate: " + e.getMessage());
        }
    }
    
    private void navigateToMenu() {
        try {
            System.out.println("Attempting to load menu.fxml...");
            
            // Load the menu FXML
            java.net.URL url = getClass().getResource("/fxml/menu.fxml");
            if (url == null) {
                throw new IOException("Cannot find menu.fxml in the classpath");
            }
            System.out.println("Found menu.fxml at: " + url.toString());
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            System.out.println("Successfully loaded menu.fxml");
            
            // Get the controller and pass user data
            MenuController menuController = loader.getController();
            if (menuController != null) {
                // Get the current user from database
                User currentUser = dbManager.getUserByEmail(emailField.getText().trim());
                if (currentUser != null) {
                    menuController.setCurrentUser(currentUser);
                }
            }
            
            // Get the current stage from the scene of any node in the current window
            if (emailField == null || emailField.getScene() == null) {
                throw new IllegalStateException("Email field or its scene is null. Cannot determine current stage.");
            }
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            System.out.println("Current stage: " + stage);
            
            // Create and set the new scene
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            
            // Set the new scene and show the stage
            stage.setScene(scene);
            stage.setTitle("UniEats - Menu");
            stage.show();
            
            System.out.println("Successfully navigated to menu");
            
        } catch (Exception e) {
            System.err.println("Error navigating to menu: " + e.getMessage());
            e.printStackTrace();
            // Show error to user with more details
            showError("Failed to load menu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClear() {
        emailField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
    }
} 