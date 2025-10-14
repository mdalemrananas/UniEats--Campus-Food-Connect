package com.unieats.util;

import java.sql.*;

/**
 * Database test utility to debug database issues
 */
public class DatabaseTest {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";
    
    public static void main(String[] args) {
        testDatabaseConnection();
        testTables();
        testFoodItems();
        testSellerOrders();
    }
    
    public static void testDatabaseConnection() {
        System.out.println("=== Testing Database Connection ===");
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("✅ Database connection successful!");
            System.out.println("Database URL: " + DB_URL);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }
    
    public static void testTables() {
        System.out.println("\n=== Testing Tables ===");
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Test food_items table
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM food_items");
                int count = rs.getInt("count");
                System.out.println("✅ food_items table exists with " + count + " records");
                
                // Check table structure
                rs = stmt.executeQuery("PRAGMA table_info(food_items)");
                System.out.println("food_items columns:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + " (" + rs.getString("type") + ")");
                }
            } catch (SQLException e) {
                System.err.println("❌ food_items table error: " + e.getMessage());
            }
            
            // Test seller_orders table
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM seller_orders");
                int count = rs.getInt("count");
                System.out.println("✅ seller_orders table exists with " + count + " records");
                
                // Check table structure
                rs = stmt.executeQuery("PRAGMA table_info(seller_orders)");
                System.out.println("seller_orders columns:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + " (" + rs.getString("type") + ")");
                }
            } catch (SQLException e) {
                System.err.println("❌ seller_orders table error: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Table test failed: " + e.getMessage());
        }
    }
    
    public static void testFoodItems() {
        System.out.println("\n=== Testing Food Items ===");
        try {
            var foodItems = DatabaseHelper.getFoodsBySeller(1);
            System.out.println("✅ Retrieved " + foodItems.size() + " food items for shop_id=1");
            
            for (var item : foodItems) {
                System.out.println("  - " + item.getName() + " (৳" + item.getPrice() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Food items test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void testSellerOrders() {
        System.out.println("\n=== Testing Seller Orders ===");
        try {
            var orders = DatabaseHelper.getOrdersBySeller(1);
            System.out.println("✅ Retrieved " + orders.size() + " orders for shop_id=1");
            
            for (var order : orders) {
                System.out.println("  - Order #" + order.getId() + ": " + order.getCustomerName() + 
                                 " - " + order.getFoodName() + " (" + order.getStatus() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Seller orders test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void testInsertFood() {
        System.out.println("\n=== Testing Food Insert ===");
        // Using default values for optional parameters
        boolean success = DatabaseHelper.insertFoodItem(
            "Test Burger",  // name
            150.0,          // price
            10,             // stock
            "Delicious test burger",  // description
            "",             // images (empty string for test)
            0.0,            // discount
            1               // shop_id (assuming 1 is a valid shop ID)
        );
        if (success) {
            System.out.println("✅ Food insert successful!");
        } else {
            System.err.println("❌ Food insert failed!");
        }
    }
}
