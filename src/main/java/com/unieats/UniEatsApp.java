package com.unieats;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;
import java.net.Socket;
import java.net.InetSocketAddress;

public class UniEatsApp extends Application {

    // Logical design size; kept small so it fits most screens and scales down only
    private static final int APP_WIDTH = 320;
    private static final int APP_HEIGHT = 560;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database and run migrations
            DatabaseManager.getInstance();

            // Start the real-time service for database polling and event broadcasting
            try {
                com.unieats.services.RealtimeService.getInstance().start();
                System.out.println("✓ RealtimeService started for real-time updates");
            } catch (Exception e) {
                System.err.println("❌ Failed to start RealtimeService: " + e.getMessage());
                e.printStackTrace();
            }

            // Start lightweight WebSocket server for inventory broadcasts
            try {
                com.unieats.util.InventoryWebSocketServer wsServer = new com.unieats.util.InventoryWebSocketServer(
                        7071);
                wsServer.start();
                // Store in a singleton for access from controllers
                com.unieats.util.SocketBus.setServer(wsServer);
            } catch (Exception ignored) {
            }
            
            // Start Shop Status WebSocket Server for real-time shop approval/rejection updates
            try {
                // Only start server if not already running (check if port is available)
                if (!isPortInUse(8082)) {
                    com.unieats.websocket.ShopStatusWebSocketServer.getInstance();
                    System.out.println("✓ Shop Status WebSocket Server initialized on port 8082");
                } else {
                    System.out.println("✓ Shop Status WebSocket Server already running on port 8082");
                }
                // Give server time to fully start before loading UI
                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("❌ Failed to start Shop Status WebSocket Server: " + e.getMessage());
                e.printStackTrace();
            }

            // Load the main FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            // Create responsive scene that scales the mobile layout for desktop/laptop
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, APP_WIDTH, APP_HEIGHT);

            // Set up the stage
            primaryStage.setTitle("UniEats");
            primaryStage.setScene(scene);

            // Allow resizing – keep very small minimums to fit tiny displays
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(280);
            primaryStage.setMinHeight(480);

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

    /**
     * Check if a port is already in use
     * @param port The port number to check
     * @return true if port is in use, false otherwise
     */
    private static boolean isPortInUse(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return true; // Port is in use
        } catch (Exception e) {
            return false; // Port is available
        }
    }
}
