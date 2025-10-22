package com.unieats.demo;

import com.unieats.DatabaseManager;
import com.unieats.FoodItem;
import com.unieats.dao.FoodItemDao;
import com.unieats.stock.StockWebSocketServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Standalone server application for real-time stock updates demo.
 * This server:
 * 1. Initializes the database
 * 2. Creates a demo food item (Burger) with stock = 1
 * 3. Starts WebSocket server on port 8080
 * 4. Waits for client connections and handles purchase requests
 * 
 * Run this FIRST before starting any clients.
 */
public class RealTimeStockServer {
    
    private static final int WEBSOCKET_PORT = 8080;
    private static final String DEMO_ITEM_NAME = "Demo Burger";
    private static int demoItemId = -1;
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  Real-Time Stock Update Demo - Server");
        System.out.println("=".repeat(60));
        
        try {
            // Step 1: Initialize database
            System.out.println("\n[1/3] Initializing database...");
            DatabaseManager.getInstance();
            System.out.println("✓ Database initialized");
            
            // Step 2: Create or reset demo item
            System.out.println("\n[2/3] Setting up demo food item...");
            demoItemId = setupDemoItem();
            System.out.println("✓ Demo item ready: ID=" + demoItemId + ", Name='" + DEMO_ITEM_NAME + "', Stock=1");
            
            // Step 3: Start WebSocket server
            System.out.println("\n[3/3] Starting WebSocket server...");
            StockWebSocketServer server = new StockWebSocketServer(WEBSOCKET_PORT);
            server.start();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✓ Server is running!");
            System.out.println("=".repeat(60));
            System.out.println("  WebSocket Port: " + WEBSOCKET_PORT);
            System.out.println("  Demo Item ID: " + demoItemId);
            System.out.println("  Demo Item: " + DEMO_ITEM_NAME);
            System.out.println("  Initial Stock: 1");
            System.out.println("=".repeat(60));
            System.out.println("\nWaiting for client connections...");
            System.out.println("Run RealTimeStockDemoClient to connect clients.");
            System.out.println("\nPress Ctrl+C to stop the server.\n");
            
            // Keep server running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("\n✗ Error starting server:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Create or update demo food item with stock = 1
     * @return The item ID
     */
    private static int setupDemoItem() {
        FoodItemDao dao = new FoodItemDao();
        
        try {
            // Check if demo item already exists
            int existingId = findDemoItem();
            
            if (existingId > 0) {
                // Update existing item to reset stock to 1
                FoodItem item = dao.getById(existingId);
                item.setStock(1);
                dao.update(item);
                System.out.println("  → Updated existing demo item (ID: " + existingId + ")");
                return existingId;
            } else {
                // Create new demo item
                // First, ensure we have a shop
                int shopId = ensureDemoShop();
                
                FoodItem newItem = new FoodItem(
                    shopId,
                    DEMO_ITEM_NAME,
                    5.99,  // price
                    1.0,   // points multiplier
                    1      // stock = 1
                );
                newItem.setDescription("A delicious burger for real-time stock demo");
                
                int itemId = dao.create(newItem);
                System.out.println("  → Created new demo item (ID: " + itemId + ")");
                return itemId;
            }
        } catch (Exception e) {
            System.err.println("Error setting up demo item: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to setup demo item", e);
        }
    }
    
    /**
     * Find existing demo item by name
     */
    private static int findDemoItem() {
        String sql = "SELECT id FROM food_items WHERE name = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db");
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DEMO_ITEM_NAME);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            // Item doesn't exist
        }
        return -1;
    }
    
    /**
     * Ensure there's at least one shop in the database for the demo
     */
    private static int ensureDemoShop() {
        String checkSql = "SELECT id FROM shops LIMIT 1";
        String insertSql = "INSERT INTO shops (shop_name, seller_id, location, status, created_at, updated_at) VALUES (?, ?, ?, ?, datetime('now'), datetime('now'))";
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db")) {
            // Check if any shop exists
            try (PreparedStatement ps = conn.prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
            
            // Create demo shop if none exists
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, "Demo Food Court");
                ps.setInt(2, 1); // Assume seller_id = 1 exists (or create one)
                ps.setString(3, "Demo Location");
                ps.setString(4, "approved");
                ps.executeUpdate();
                
                // Get the created shop ID
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error ensuring demo shop: " + e.getMessage());
        }
        
        return 1; // Default fallback
    }
}
