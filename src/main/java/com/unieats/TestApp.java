package com.unieats;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.unieats.util.DatabaseHelper;

/**
 * Simple test application to verify database functionality
 */
public class TestApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize database
        try {
            DatabaseHelper.initializeTables();
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Create simple UI
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label title = new Label("UniEats Database Test");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button testFoodInsert = new Button("Test Food Insert");
        testFoodInsert.setOnAction(e -> {
            boolean success = DatabaseHelper.insertFood("Test Burger", 150.0, 1);
            System.out.println("Food insert result: " + success);
        });
        
        Button testFoodRetrieve = new Button("Test Food Retrieve");
        testFoodRetrieve.setOnAction(e -> {
            var foods = DatabaseHelper.getFoodsBySeller(1);
            System.out.println("Retrieved " + foods.size() + " food items");
            for (var food : foods) {
                System.out.println("- " + food.getName() + " (৳" + food.getPrice() + ")");
            }
        });
        
        Button testOrderInsert = new Button("Test Order Insert");
        testOrderInsert.setOnAction(e -> {
            DatabaseHelper.addSampleOrders(1);
            System.out.println("Sample orders added");
        });
        
        Button testOrderRetrieve = new Button("Test Order Retrieve");
        testOrderRetrieve.setOnAction(e -> {
            var orders = DatabaseHelper.getOrdersBySeller(1);
            System.out.println("Retrieved " + orders.size() + " orders");
            for (var order : orders) {
                System.out.println("- Order #" + order.getId() + ": " + order.getCustomerName());
            }
        });
        
        root.getChildren().addAll(title, testFoodInsert, testFoodRetrieve, testOrderInsert, testOrderRetrieve);
        
        Scene scene = new Scene(root, 300, 300);
        primaryStage.setTitle("UniEats Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        // Add crash prevention
        System.setProperty("javafx.verbose", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("java.awt.headless", "false");
        
        launch(args);
    }
}
