package com.unieats;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

/**
 * Robust launcher for UniEats application with crash prevention
 */
public class UniEatsLauncher extends Application {
    
    // Logical design size; kept small so it fits most screens and scales down only
    private static final int APP_WIDTH = 320;
    private static final int APP_HEIGHT = 560;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // Initialize database early
        try {
            System.out.println("Initializing database...");
            DatabaseManager.getInstance();
            com.unieats.util.DatabaseInitializer.initializeDatabase();
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - let the app continue
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting UniEats application...");
            
            // Load the main FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            // Create responsive scene that scales the mobile layout for desktop/laptop
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, APP_WIDTH, APP_HEIGHT);
            System.out.println("Scene created successfully");
            
            // Set up the stage
            primaryStage.setTitle("UniEats - Food Delivery App");
            primaryStage.setScene(scene);
            
            // Allow resizing – keep very small minimums to fit tiny displays
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(280);
            primaryStage.setMinHeight(480);
            
            // Set up close handler
            primaryStage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            
            // Try to set application icon (not critical if it fails)
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
                primaryStage.getIcons().add(icon);
                System.out.println("Application icon loaded");
            } catch (Exception e) {
                System.err.println("Warning: Could not load application icon: " + e.getMessage());
            }
            
            // Show the stage
            primaryStage.show();
            System.out.println("Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Startup Error");
                alert.setHeaderText("Failed to load application");
                alert.setContentText("An error occurred while starting the application:\n\n" + e.getMessage() + 
                                   "\n\nPlease check the console for more details.");
                alert.showAndWait();
                Platform.exit();
            });
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== UniEats Launcher Starting ===");
        
        // Set comprehensive crash prevention properties
        setCrashPreventionProperties();
        
        // Set up uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
        });
        
        try {
            System.out.println("Launching JavaFX application...");
            launch(args);
        } catch (Exception e) {
            System.err.println("Failed to launch application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void setCrashPreventionProperties() {
        System.out.println("Setting crash prevention properties...");
        
        // JavaFX rendering properties
        System.setProperty("javafx.verbose", "false"); // Set to false to reduce noise
        System.setProperty("prism.verbose", "false");
        System.setProperty("prism.order", "sw"); // Force software rendering
        System.setProperty("prism.allowhidpi", "false");
        System.setProperty("prism.text", "t2k");
        
        // Java2D properties
        System.setProperty("java.awt.headless", "false");
        System.setProperty("sun.java2d.opengl", "false");
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.d3dtexbpp", "false");
        
        // Memory and GC properties
        System.setProperty("java.awt.useSystemAAFontSettings", "on");
        System.setProperty("sun.java2d.uiScale", "1.0");
        
        // Module system properties
        System.setProperty("java.awt.splashscreen", "false");
        
        System.out.println("Crash prevention properties set successfully");
    }
}
