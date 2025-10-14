package com.unieats.services;

import com.unieats.FoodItem;
import com.unieats.dao.FoodItemDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing real-time stock updates across multiple users
 * Provides thread-safe, real-time stock synchronization
 */
public class StockUpdateService {
    private static volatile StockUpdateService instance;
    private final FoodItemDao foodItemDao;
    private final List<StockUpdateListener> listeners;
    private final ScheduledExecutorService scheduler;
    private final ObservableList<FoodItem> allFoodItems;
    private final Map<Integer, Integer> stockCache; // itemId -> stock
    private final ReadWriteLock stockLock;
    private volatile boolean isRunning = false;
    
    private StockUpdateService() {
        this.foodItemDao = new FoodItemDao();
        this.listeners = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.allFoodItems = FXCollections.observableArrayList();
        this.stockCache = new ConcurrentHashMap<>();
        this.stockLock = new ReentrantReadWriteLock();
        loadAllFoodItems();
    }
    
    public static StockUpdateService getInstance() {
        if (instance == null) {
            synchronized (StockUpdateService.class) {
                if (instance == null) {
                    instance = new StockUpdateService();
                }
            }
        }
        return instance;
    }
    
    /**
     * Start the real-time stock monitoring
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        // Check for stock updates every 1 second for real-time feel
        scheduler.scheduleAtFixedRate(this::checkForStockUpdates, 0, 1, TimeUnit.SECONDS);
        // Also refresh all items every 5 seconds to catch any missed updates
        scheduler.scheduleAtFixedRate(this::refreshAllItems, 5, 5, TimeUnit.SECONDS);
        System.out.println("Real-time stock update service started");
    }
    
    /**
     * Stop the real-time stock monitoring
     */
    public void stop() {
        isRunning = false;
        scheduler.shutdown();
        System.out.println("Stock update service stopped");
    }
    
    /**
     * Add a listener for stock updates
     */
    public void addListener(StockUpdateListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener for stock updates
     */
    public void removeListener(StockUpdateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get the current list of all food items
     */
    public ObservableList<FoodItem> getAllFoodItems() {
        return allFoodItems;
    }
    
    /**
     * Update stock for a specific item and notify all listeners
     * Thread-safe method that ensures real-time updates across all users
     */
    public void updateStock(int itemId, int quantityToReduce) {
        stockLock.writeLock().lock();
        try {
            // Check current stock in cache first
            Integer currentStock = stockCache.get(itemId);
            if (currentStock != null && currentStock < quantityToReduce) {
                throw new RuntimeException("Insufficient stock. Available: " + currentStock + ", Required: " + quantityToReduce);
            }
            
            // Update stock in database
            foodItemDao.updateStock(itemId, quantityToReduce);
            
            // Update local cache
            if (currentStock != null) {
                int newStock = currentStock - quantityToReduce;
                stockCache.put(itemId, newStock);
            }
            
            // Update UI immediately on JavaFX thread
            Platform.runLater(() -> {
                updateFoodItemInList(itemId);
            });
            
            // Notify all listeners immediately
            notifyStockUpdated(itemId, quantityToReduce);
            
            System.out.println("Real-time stock update: Item " + itemId + " reduced by " + quantityToReduce);
            
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
            notifyStockUpdateError(itemId, e.getMessage());
        } finally {
            stockLock.writeLock().unlock();
        }
    }
    
    /**
     * Update a specific food item in the observable list
     */
    private void updateFoodItemInList(int itemId) {
        for (int i = 0; i < allFoodItems.size(); i++) {
            FoodItem item = allFoodItems.get(i);
            if (item.getId() == itemId) {
                FoodItem updatedItem = foodItemDao.getById(itemId);
                if (updatedItem != null) {
                    allFoodItems.set(i, updatedItem);
                    System.out.println("UI updated for item " + itemId + " with stock: " + updatedItem.getStock());
                }
                break;
            }
        }
    }
    
    /**
     * Refresh all food items from database
     */
    public void refreshAllItems() {
        loadAllFoodItems();
        notifyAllItemsRefreshed();
    }
    
    private void loadAllFoodItems() {
        try {
            List<FoodItem> items = foodItemDao.getRandomItems(1000); // Get all items
            
            // Update stock cache
            stockLock.writeLock().lock();
            try {
                stockCache.clear();
                for (FoodItem item : items) {
                    stockCache.put(item.getId(), item.getStock());
                }
            } finally {
                stockLock.writeLock().unlock();
            }
            
            Platform.runLater(() -> {
                allFoodItems.clear();
                allFoodItems.addAll(items);
                System.out.println("Loaded " + items.size() + " food items with real-time stock tracking");
            });
        } catch (Exception e) {
            System.err.println("Error loading food items: " + e.getMessage());
        }
    }
    
    private void checkForStockUpdates() {
        if (!isRunning) return;
        
        try {
            // Check for stock changes by comparing with database
            stockLock.readLock().lock();
            try {
                for (Map.Entry<Integer, Integer> entry : stockCache.entrySet()) {
                    int itemId = entry.getKey();
                    int cachedStock = entry.getValue();
                    
                    // Get current stock from database
                    FoodItem currentItem = foodItemDao.getById(itemId);
                    if (currentItem != null && currentItem.getStock() != cachedStock) {
                        // Stock has changed, update cache and notify listeners
                        int newStock = currentItem.getStock();
                        int difference = cachedStock - newStock;
                        
                        stockCache.put(itemId, newStock);
                        
                        // Notify listeners about the change
                        Platform.runLater(() -> {
                            updateFoodItemInList(itemId);
                            notifyStockUpdated(itemId, difference);
                        });
                        
                        System.out.println("Detected stock change for item " + itemId + ": " + cachedStock + " -> " + newStock);
                    }
                }
            } finally {
                stockLock.readLock().unlock();
            }
        } catch (Exception e) {
            System.err.println("Error checking for stock updates: " + e.getMessage());
        }
    }
    
    private void notifyStockUpdated(int itemId, int quantityReduced) {
        Platform.runLater(() -> {
            for (StockUpdateListener listener : listeners) {
                try {
                    listener.onStockUpdated(itemId, quantityReduced);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    private void notifyStockUpdateError(int itemId, String error) {
        Platform.runLater(() -> {
            for (StockUpdateListener listener : listeners) {
                try {
                    listener.onStockUpdateError(itemId, error);
                } catch (Exception e) {
                    System.err.println("Error notifying listener of error: " + e.getMessage());
                }
            }
        });
    }
    
    private void notifyAllItemsRefreshed() {
        Platform.runLater(() -> {
            for (StockUpdateListener listener : listeners) {
                try {
                    listener.onAllItemsRefreshed();
                } catch (Exception e) {
                    System.err.println("Error notifying listener of refresh: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Interface for listening to stock updates
     */
    public interface StockUpdateListener {
        void onStockUpdated(int itemId, int quantityReduced);
        void onStockUpdateError(int itemId, String error);
        void onAllItemsRefreshed();
    }
}
