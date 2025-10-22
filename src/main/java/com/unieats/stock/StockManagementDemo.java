package com.unieats.stock;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the difference between unsafe and safe stock management
 * with multiple concurrent users.
 */
public class StockManagementDemo {
    private static final String BURGER_ID = "BURGER-001";
    private static final int INITIAL_STOCK = 5;
    private static final int NUM_USERS = 5;
    private static final Random random = new Random();
    private static final StockManager stockManager = StockManager.getInstance();

    public static void main(String[] args) {
        System.out.println("=== Real-Time Stock Management System ===\n");
        
        // Demo unsafe version first
        System.out.println("=== UNSAFE VERSION (Race Condition Demo) ===");
        runDemo(false);
        
        // Reset and demo safe version
        System.out.println("\n=== SAFE VERSION (Thread-Safe) ===");
        runDemo(true);
        
        stockManager.shutdown();
    }
    
    private static void runDemo(boolean useSafeVersion) {
        // Create product with initial stock
        ProductStock burgerStock = new ProductStock(BURGER_ID, "Deluxe Burger", INITIAL_STOCK);
        stockManager.addProduct(burgerStock);
        
        // Register a global listener for stock updates
        stockManager.registerListener(BURGER_ID, product -> 
            System.out.printf("[System] Notifying all users: %s stock updated to %d%n", 
                product.getName(), product.getStockQuantity())
        );
        
        System.out.printf("Initial Stock: %d%n", burgerStock.getStockQuantity());
        
        // Create a thread pool for users
        ExecutorService executor = Executors.newFixedThreadPool(NUM_USERS);
        
        // Simulate multiple users trying to order
        for (int i = 0; i < NUM_USERS; i++) {
            String userName = "User-" + (char)('A' + i);
            int quantity = 1 + random.nextInt(3); // Random quantity between 1-3
            
            executor.submit(() -> {
                try {
                    // Simulate users viewing the product at slightly different times
                    Thread.sleep(100 + random.nextInt(400));
                    
                    // Get current stock (simulating page load)
                    int currentStock = burgerStock.getStockQuantity();
                    System.out.printf("[%s] sees stock: %d%n", userName, currentStock);
                    
                    // Simulate user thinking before ordering
                    Thread.sleep(200 + random.nextInt(300));
                    
                    System.out.printf("[%s] is ordering %d %s...%n", 
                            userName, quantity, burgerStock.getName());
                    
                    boolean success = useSafeVersion ? 
                            burgerStock.safeOrder(quantity) : 
                            burgerStock.unsafeOrder(quantity);
                    
                    if (success) {
                        System.out.printf("[%s] order successful! Remaining stock: %d%n", 
                                userName, burgerStock.getStockQuantity());
                        // Notify all listeners about the stock update
                        stockManager.notifyStockUpdate(burgerStock);
                    } else {
                        System.out.printf("[%s] order failed! Not enough stock for %d. Current stock: %d%n", 
                                userName, quantity, burgerStock.getStockQuantity());
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.printf("Final Stock: %d%n\n", burgerStock.getStockQuantity());
    }
}
