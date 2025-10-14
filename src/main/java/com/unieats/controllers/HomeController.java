package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HomeController {
    // Main illustration
    @FXML private ImageView mainIllustration;

    @FXML
    private void initialize() {
        loadImages();
    }
    
    private void loadImages() {
        // Load the local Chief image
        try {
            String imagePath = getClass().getResource("/images/Cheif.png").toExternalForm();
            Image image = new Image(imagePath, true); // true = load in background
            mainIllustration.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading Chief image: " + e.getMessage());
            e.printStackTrace();
            // Fallback to a placeholder if the image fails to load
            loadImage(mainIllustration, "https://images.unsplash.com/photo-1504674900247-087703934869?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        }
    }
    
    private void loadImage(ImageView imageView, String url) {
        if (imageView == null) {
            System.err.println("Warning: ImageView is null. Cannot load image: " + url);
            return;
        }
        try {
            Image image = new Image(url, true); // true = load in background
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + url);
            e.printStackTrace();
        }
    }

    // Onboarding action handlers
    @FXML
    private void handleSkip() {
        showAlert("Skipped", "You've chosen to skip the onboarding process.");
        // TODO: Navigate to main app screen
    }

    @FXML
    private void handleSignUp() {
        try {
            // Load the signup FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) mainIllustration.getScene().getWindow();
            
            // Create new responsive scene with signup content
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            
            // Set the new scene
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading signup page: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load signup page. Please try again.");
        }
    }

    @FXML
    private void handleSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
            Parent root = loader.load();
            
            // Create new responsive scene with signin content
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            
            // Get current stage and set new scene
            Stage stage = (Stage) mainIllustration.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading signin page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileAction() {
        // Get current user from DatabaseManager
        com.unieats.User currentUser = com.unieats.DatabaseManager.getCurrentUser();
        
        if (currentUser == null) {
            showAlert("Error", "No user logged in. Please sign in first.");
            return;
        }
        
        // Check if user is a seller
        if ("seller".equalsIgnoreCase(currentUser.getUserCategory())) {
            showSellerProfileDialog(currentUser);
        } else {
            // For regular users, navigate to profile page
            navigateToProfile();
        }
    }
    
    @FXML
    private void handleOrderNow() {
        // Navigate to menu page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) mainIllustration.getScene().getWindow();
            
            // Create new responsive scene
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            
            // Set the new scene
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading menu page: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load menu page. Please try again.");
        }
    }
    
    @FXML
    private void handleHomeAction() {
        // Already on home page, do nothing or refresh
    }
    
    private void showSellerProfileDialog(com.unieats.User seller) {
        // Create a custom dialog for seller profile
        javafx.scene.control.Dialog<Boolean> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Shop Profile");
        dialog.setHeaderText("Your Shop Information");
        
        // Create the dialog content
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
        
        // Get shop information
        com.unieats.dao.ShopDao shopDao = new com.unieats.dao.ShopDao();
        com.unieats.Shop shop = shopDao.getShopByOwnerId(seller.getId());
        
        // Create form fields
        javafx.scene.control.TextField shopNameField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField ownerNameField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField();
        javafx.scene.control.TextArea descriptionField = new javafx.scene.control.TextArea();
        javafx.scene.control.TextField addressField = new javafx.scene.control.TextField();
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label();
        
        // Populate fields
        if (shop != null) {
            shopNameField.setText(shop.getShopName() != null ? shop.getShopName() : "");
            descriptionField.setText(shop.getDescription() != null ? shop.getDescription() : "");
            addressField.setText(shop.getAddress() != null ? shop.getAddress() : "");
            statusLabel.setText("Status: " + (shop.getStatus() != null ? shop.getStatus() : "Unknown"));
        }
        
        ownerNameField.setText(seller.getFullName() != null ? seller.getFullName() : "");
        emailField.setText(seller.getEmail() != null ? seller.getEmail() : "");
        
        // Make some fields read-only
        emailField.setEditable(false);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Add fields to grid
        grid.add(new javafx.scene.control.Label("Shop Name:"), 0, 0);
        grid.add(shopNameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Owner Name:"), 0, 1);
        grid.add(ownerNameField, 1, 1);
        grid.add(new javafx.scene.control.Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new javafx.scene.control.Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new javafx.scene.control.Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(statusLabel, 0, 5, 2, 1);
        
        // Set text area properties
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        
        dialog.getDialogPane().setContent(grid);
        
        // Add buttons
        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Handle save action
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return saveSellerProfile(seller, shop, shopNameField.getText(), ownerNameField.getText(), 
                                       addressField.getText(), descriptionField.getText());
            }
            return false;
        });
        
        dialog.showAndWait();
    }
    
    private boolean saveSellerProfile(com.unieats.User seller, com.unieats.Shop shop, 
                                    String shopName, String ownerName, 
                                    String address, String description) {
        try {
            // Update user information
            seller.setFullName(ownerName);
            com.unieats.DatabaseManager.getInstance().updateUser(seller);
            
            // Update shop information
            if (shop != null) {
                shop.setShopName(shopName);
                shop.setAddress(address);
                shop.setDescription(description);
                com.unieats.dao.ShopDao shopDao = new com.unieats.dao.ShopDao();
                shopDao.updateShop(shop);
            }
            
            showAlert("Success", "Profile updated successfully!");
            return true;
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to update profile. Please try again.");
            return false;
        }
    }
    
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) mainIllustration.getScene().getWindow();
            
            // Create new responsive scene
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            
            // Set the new scene
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading profile page: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load profile page. Please try again.");
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
