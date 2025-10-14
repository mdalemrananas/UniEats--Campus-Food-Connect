package com.unieats.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database initialization utility
 * Handles database setup and table creation
 */
public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";
    
    /**
     * Initialize the database with required tables
     * This method should be called once at application startup
     */
    public static void initializeDatabase() {
        try {
            // Initialize tables using DatabaseHelper
            DatabaseHelper.initializeTables();
            
            // Add sample data if tables are empty
            addSampleDataIfNeeded();
            
            System.out.println("Database initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add sample data if the tables are empty
     */
    private static void addSampleDataIfNeeded() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Check if food_items table is empty
            var foodCountResult = stmt.executeQuery("SELECT COUNT(*) as count FROM food_items");
            int foodCount = foodCountResult.getInt("count");
            
            // Check if orders table is empty
            var orderCountResult = stmt.executeQuery("SELECT COUNT(*) as count FROM orders");
            int orderCount = orderCountResult.getInt("count");
            
            // Add sample food items if table is empty
            if (foodCount == 0) {
                String[] foodItems = {
                    "INSERT INTO food_items (name, price, shop_id, stock) VALUES ('Chicken Burger', 250.00, 1, 100)",
                    "INSERT INTO food_items (name, price, shop_id, stock) VALUES ('Pizza Margherita', 400.00, 1, 50)",
                    "INSERT INTO food_items (name, price, shop_id, stock) VALUES ('French Fries', 80.00, 1, 200)",
                    "INSERT INTO food_items (name, price, shop_id, stock) VALUES ('Coca Cola', 50.00, 1, 300)",
                    "INSERT INTO food_items (name, price, shop_id, stock) VALUES ('Chicken Wings', 300.00, 1, 75)"
                };
                
                for (String sql : foodItems) {
                    stmt.execute(sql);
                }
                System.out.println("Sample food items added to database");
            }
            
            // Add sample orders to seller_orders table
            try {
                var sellerOrderCountResult = stmt.executeQuery("SELECT COUNT(*) as count FROM seller_orders");
                int sellerOrderCount = sellerOrderCountResult.getInt("count");
                
                if (sellerOrderCount == 0) {
                    String[] orders = {
                        "INSERT INTO seller_orders (customer_name, food_name, quantity, total_price, status, shop_id) VALUES ('John Doe', 'Chicken Burger', 2, 500.00, 'Pending', 1)",
                        "INSERT INTO seller_orders (customer_name, food_name, quantity, total_price, status, shop_id) VALUES ('Jane Smith', 'Pizza Margherita', 1, 400.00, 'In Progress', 1)",
                        "INSERT INTO seller_orders (customer_name, food_name, quantity, total_price, status, shop_id) VALUES ('Bob Wilson', 'French Fries', 3, 240.00, 'Completed', 1)",
                        "INSERT INTO seller_orders (customer_name, food_name, quantity, total_price, status, shop_id) VALUES ('Alice Johnson', 'Chicken Wings', 1, 300.00, 'Pending', 1)",
                        "INSERT INTO seller_orders (customer_name, food_name, quantity, total_price, status, shop_id) VALUES ('Charlie Brown', 'Coca Cola', 4, 200.00, 'Completed', 1)"
                    };
                    
                    for (String sql : orders) {
                        stmt.execute(sql);
                    }
                    System.out.println("Sample seller orders added to database");
                }
            } catch (SQLException e) {
                System.out.println("seller_orders table not found, will be created by DatabaseHelper");
            }
            
        } catch (Exception e) {
            System.err.println("Error adding sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
