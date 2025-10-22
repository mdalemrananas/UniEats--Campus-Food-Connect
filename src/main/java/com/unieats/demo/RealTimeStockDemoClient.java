package com.unieats.demo;

import com.unieats.stock.StockUpdateMessage;
import com.unieats.websocket.StockWebSocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX Client Application for Real-Time Stock Update Demo.
 * 
 * Features:
 * - Connects to WebSocket server for real-time stock updates
 * - Shows current stock count for a burger item
 * - "Buy" button to place an order
 * - Automatically updates UI when ANY user places an order (no polling!)
 * - Uses Platform.runLater() for thread-safe UI updates
 * 
 * To test: Run multiple instances of this client, then click "Buy" on one.
 * All other clients will immediately see the stock update.
 */
public class RealTimeStockDemoClient extends Application implements StockWebSocketClient.StockUpdateListener {
    
    // WebSocket configuration
    private static final String WEBSOCKET_URL = "ws://localhost:8080";
    private static final int DEMO_ITEM_ID = 0; // Will be determined at runtime
    
    // UI Components
    private Label stockLabel;
    private Label statusLabel;
    private Button buyButton;
    private TextArea activityLog;
    private Label connectionStatus;
    private ProgressIndicator loadingIndicator;
    
    // WebSocket client
    private StockWebSocketClient wsClient;
    
    // Current stock value
    private int currentStock = -1;
    
    // Client identifier (for logging)
    private String clientId;
    
    @Override
    public void start(Stage primaryStage) {
        // Generate unique client ID
        clientId = "Client-" + (int)(Math.random() * 1000);
        
        // Build UI
        VBox root = buildUI();
        
        // Create scene
        Scene scene = new Scene(root, 400, 550);
        
        // Configure stage
        primaryStage.setTitle("Real-Time Stock Demo - " + clientId);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Connect to WebSocket server
        connectToServer();
        
        // Handle window close
        primaryStage.setOnCloseRequest(event -> {
            if (wsClient != null) {
                wsClient.close();
            }
        });
    }
    
    /**
     * Build the JavaFX UI
     */
    private VBox buildUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        Label title = new Label("🍔 Real-Time Stock Demo");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label(clientId);
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        // Connection status
        connectionStatus = new Label("● Connecting...");
        connectionStatus.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        connectionStatus.setTextFill(Color.ORANGE);
        
        // Stock display card
        VBox stockCard = new VBox(10);
        stockCard.setPadding(new Insets(20));
        stockCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        stockCard.setAlignment(Pos.CENTER);
        
        Label itemLabel = new Label("Demo Burger");
        itemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        stockLabel = new Label("Stock: --");
        stockLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        stockLabel.setTextFill(Color.web("#27ae60"));
        
        statusLabel = new Label("Connecting to server...");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.web("#95a5a6"));
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        
        stockCard.getChildren().addAll(itemLabel, stockLabel, statusLabel, loadingIndicator);
        
        // Buy button
        buyButton = new Button("🛒 Buy Now");
        buyButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        buyButton.setPrefHeight(50);
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8;");
        buyButton.setDisable(true); // Disabled until connected
        buyButton.setOnAction(e -> handleBuyClick());
        
        // Activity log
        Label logLabel = new Label("📋 Activity Log:");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        activityLog = new TextArea();
        activityLog.setEditable(false);
        activityLog.setPrefRowCount(8);
        activityLog.setStyle("-fx-control-inner-background: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        activityLog.setWrapText(true);
        
        // Assemble layout
        root.getChildren().addAll(
            title,
            subtitle,
            connectionStatus,
            stockCard,
            buyButton,
            logLabel,
            activityLog
        );
        
        return root;
    }
    
    /**
     * Connect to WebSocket server
     */
    private void connectToServer() {
        try {
            log("Connecting to server at " + WEBSOCKET_URL + "...");
            
            // Create WebSocket client
            wsClient = new StockWebSocketClient(new URI(WEBSOCKET_URL));
            wsClient.addStockUpdateListener(this);
            
            // Connect
            wsClient.connect();
            
            // Wait a bit for connection, then query initial stock
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    
                    // Query stock for demo item (we'll use item ID from server response)
                    // For demo, we'll query item 1 initially
                    Platform.runLater(() -> {
                        if (wsClient.isOpen()) {
                            wsClient.queryStock(getDemoItemId());
                            log("Querying initial stock...");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            log("ERROR: Failed to connect - " + e.getMessage());
            e.printStackTrace();
            
            Platform.runLater(() -> {
                connectionStatus.setText("● Connection Failed");
                connectionStatus.setTextFill(Color.RED);
                statusLabel.setText("Failed to connect to server");
                loadingIndicator.setVisible(false);
            });
        }
    }
    
    /**
     * Handle Buy button click
     */
    private void handleBuyClick() {
        if (wsClient != null && wsClient.isOpen()) {
            log("Sending purchase request...");
            
            // Disable button temporarily
            buyButton.setDisable(true);
            
            // Send purchase request to server
            wsClient.requestPurchase(getDemoItemId());
            
            // Re-enable after delay (to prevent spam)
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(() -> {
                        if (currentStock > 0) {
                            buyButton.setDisable(false);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Get demo item ID from server startup log or default
     */
    private int getDemoItemId() {
        // For simplicity, we'll query all items and use the first one
        // In a real app, this would be passed as a parameter
        // For this demo, we'll hardcode the ID that was printed by the server
        // The server prints: "Demo Item ID: X"
        // We'll use a default of 1, which works for most cases
        return 1;
    }
    
    // ===== WebSocket Listener Methods =====
    
    @Override
    public void onStockUpdate(StockUpdateMessage update) {
        // This is called on JavaFX UI thread (thanks to Platform.runLater in client)
        currentStock = update.getNewStock();
        
        // Update stock display
        stockLabel.setText("Stock: " + currentStock);
        
        // Update status and color
        if (currentStock > 0) {
            stockLabel.setTextFill(Color.web("#27ae60")); // Green
            statusLabel.setText("✓ Available");
            statusLabel.setTextFill(Color.web("#27ae60"));
            buyButton.setDisable(false);
            buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8;");
        } else {
            stockLabel.setTextFill(Color.web("#e74c3c")); // Red
            statusLabel.setText("✗ Out of Stock");
            statusLabel.setTextFill(Color.web("#e74c3c"));
            buyButton.setDisable(true);
            buyButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 8;");
        }
        
        // Hide loading indicator
        loadingIndicator.setVisible(false);
        
        // Update connection status
        connectionStatus.setText("● Connected");
        connectionStatus.setTextFill(Color.web("#27ae60"));
        
        // Log the update
        log("STOCK UPDATE: " + update.getItemName() + " → " + currentStock + " units");
    }
    
    @Override
    public void onPurchaseFailed(String message) {
        // This is called on JavaFX UI thread
        log("PURCHASE FAILED: " + message);
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Purchase Failed");
        alert.setHeaderText("Unable to complete purchase");
        alert.setContentText("The item is out of stock or unavailable.");
        alert.showAndWait();
    }
    
    @Override
    public void onDisconnected() {
        // This is called on JavaFX UI thread
        log("Disconnected from server");
        
        connectionStatus.setText("● Disconnected");
        connectionStatus.setTextFill(Color.RED);
        statusLabel.setText("Disconnected from server");
        buyButton.setDisable(true);
        stockLabel.setText("Stock: --");
        loadingIndicator.setVisible(false);
    }
    
    /**
     * Add message to activity log with timestamp
     */
    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = String.format("[%s] %s\n", timestamp, message);
        
        Platform.runLater(() -> {
            activityLog.appendText(logEntry);
            activityLog.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
