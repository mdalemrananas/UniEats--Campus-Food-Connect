package com.unieats;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.Objects;

public class UniEatsApp extends Application {
    
    // Mobile device dimensions (optimized for modern mobile)
    private static final int APP_WIDTH = 360;
    private static final int APP_HEIGHT = 800;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            
            // Create and set the scene with a white background
            Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT, Color.WHITE);
            
            // Set up the stage
            primaryStage.setTitle("UniEats");
            primaryStage.setScene(scene);
            
            // Set fixed size to match mobile device dimensions
            primaryStage.setMinWidth(APP_WIDTH);
            primaryStage.setMaxWidth(APP_WIDTH);
            primaryStage.setMinHeight(APP_HEIGHT);
            primaryStage.setMaxHeight(APP_HEIGHT);
            
            // Remove window decorations for a more app-like experience
            primaryStage.setResizable(false);
            
            // Try to set application icon (not critical if it fails)
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Warning: Could not load application icon: " + e.getMessage());
            }
            
            // Show the stage
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load application");
            alert.setContentText("An error occurred while starting the application: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
