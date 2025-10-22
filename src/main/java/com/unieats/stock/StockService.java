package com.unieats.stock;

import com.unieats.FoodItem;
import com.unieats.dao.FoodItemDao;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe service for managing food item stock.
 * Uses locking to ensure only one transaction can modify stock at a time.
 */
public class StockService {
    
    // Singleton instance
    private static StockService instance;
    
    // DAO for database operations
    private final FoodItemDao foodItemDao;
    
    // Lock to ensure thread-safe stock operations (prevent race conditions)
    private final ReentrantLock stockLock = new ReentrantLock();
    
    private StockService() {
        this.foodItemDao = new FoodItemDao();
    }
    
    /**
     * Get singleton instance of StockService
     */
    public static synchronized StockService getInstance() {
        if (instance == null) {
            instance = new StockService();
        }
        return instance;
    }
    
    /**
     * Get current stock for a food item
     * @param itemId The food item ID
     * @return Current stock quantity, or -1 if item not found
     */
    public int getStock(int itemId) {
        FoodItem item = foodItemDao.getById(itemId);
        return item != null ? item.getStock() : -1;
    }
    
    /**
     * Attempt to purchase an item (reduce stock by 1).
     * This method is thread-safe and will only succeed if stock is available.
     * 
     * @param itemId The food item ID to purchase
     * @return true if purchase successful, false if out of stock or item not found
     */
    public boolean purchaseItem(int itemId) {
        // Acquire lock to prevent concurrent modifications
        stockLock.lock();
        try {
            // Get current item from database
            FoodItem item = foodItemDao.getById(itemId);
            
            if (item == null) {
                System.err.println("Item not found: " + itemId);
                return false;
            }
            
            // Check if stock is available
            if (item.getStock() <= 0) {
                System.out.println("Out of stock for item: " + itemId);
                return false;
            }
            
            // Reduce stock by 1
            item.setStock(item.getStock() - 1);
            
            // Update in database
            foodItemDao.update(item);
            
            System.out.println("Successfully purchased item " + itemId + 
                             ". New stock: " + item.getStock());
            return true;
            
        } catch (Exception e) {
            System.err.println("Error purchasing item: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Always release the lock
            stockLock.unlock();
        }
    }
    
    /**
     * Get food item by ID
     * @param itemId The food item ID
     * @return FoodItem or null if not found
     */
    public FoodItem getItem(int itemId) {
        return foodItemDao.getById(itemId);
    }
}
