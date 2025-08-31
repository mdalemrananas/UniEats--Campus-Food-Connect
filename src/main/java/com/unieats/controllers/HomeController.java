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
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
