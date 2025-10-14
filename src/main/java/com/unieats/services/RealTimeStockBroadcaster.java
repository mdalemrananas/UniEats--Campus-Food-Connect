package com.unieats.services;

import com.unieats.FoodItem;
import com.unieats.dao.FoodItemDao;
import javafx.application.Platform;

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
 * Real-time stock broadcaster that ensures all connected clients see stock updates immediately
 * This service runs independently and broadcasts stock changes to all registered listeners
 */
public class RealTimeStockBroadcaster {
    private static volatile RealTimeStockBroadcaster instance;
    private final FoodItemDao foodItemDao;
    private final List<StockChangeListener> listeners;
    private final ScheduledExecutorService broadcaster;
    private final Map<Integer, Integer> lastKnownStock; // itemId -> last known stock
    private final ReadWriteLock lock;
    private volatile boolean isRunning = false;
    
    private RealTimeStockBroadcaster() {
        this.foodItemDao = new FoodItemDao();
        this.listeners = new CopyOnWriteArrayList<>();
        this.broadcaster = Executors.newScheduledThreadPool(2);
        this.lastKnownStock = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        initializeStockTracking();
    }
    
    public static RealTimeStockBroadcaster getInstance() {
        if (instance == null) {
            synchronized (RealTimeStockBroadcaster.class) {
                if (instance == null) {
                    instance = new RealTimeStockBroadcaster();
                }
            }
        }
        return instance;
    }
    
    /**
     * Start the real-time stock broadcasting
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        // Check for stock changes every 500ms for near real-time updates
        broadcaster.scheduleAtFixedRate(this::broadcastStockChanges, 0, 500, TimeUnit.MILLISECONDS);
        System.out.println("Real-time stock broadcaster started");
    }
    
    /**
     * Stop the real-time stock broadcasting
     */
    public void stop() {
        isRunning = false;
        broadcaster.shutdown();
        System.out.println("Real-time stock broadcaster stopped");
    }
    
    /**
     * Add a listener for stock changes
     */
    public void addListener(StockChangeListener listener) {
        listeners.add(listener);
        System.out.println("Added stock change listener. Total listeners: " + listeners.size());
    }
    
    /**
     * Remove a listener for stock changes
     */
    public void removeListener(StockChangeListener listener) {
        listeners.remove(listener);
        System.out.println("Removed stock change listener. Total listeners: " + listeners.size());
    }
    
    /**
     * Notify about a stock change immediately
     */
    public void notifyStockChange(int itemId, int oldStock, int newStock) {
        lock.writeLock().lock();
        try {
            lastKnownStock.put(itemId, newStock);
            broadcastStockChangeToListeners(itemId, oldStock, newStock);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Initialize stock tracking by loading current stock levels
     */
    private void initializeStockTracking() {
        try {
            List<FoodItem> items = foodItemDao.getRandomItems(1000);
            lock.writeLock().lock();
            try {
                for (FoodItem item : items) {
                    lastKnownStock.put(item.getId(), item.getStock());
                }
                System.out.println("Initialized stock tracking for " + items.size() + " items");
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            System.err.println("Error initializing stock tracking: " + e.getMessage());
        }
    }
    
    /**
     * Check for stock changes and broadcast them
     */
    private void broadcastStockChanges() {
        if (!isRunning) return;
        
        try {
            lock.readLock().lock();
            try {
                for (Map.Entry<Integer, Integer> entry : lastKnownStock.entrySet()) {
                    int itemId = entry.getKey();
                    int lastStock = entry.getValue();
                    
                    // Get current stock from database
                    FoodItem currentItem = foodItemDao.getById(itemId);
                    if (currentItem != null && currentItem.getStock() != lastStock) {
                        int currentStock = currentItem.getStock();
                        
                        // Update our tracking
                        lastKnownStock.put(itemId, currentStock);
                        
                        // Broadcast the change
                        broadcastStockChangeToListeners(itemId, lastStock, currentStock);
                        
                        System.out.println("Broadcasting stock change: Item " + itemId + " " + lastStock + " -> " + currentStock);
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting stock changes: " + e.getMessage());
        }
    }
    
    /**
     * Broadcast stock change to all listeners
     */
    private void broadcastStockChangeToListeners(int itemId, int oldStock, int newStock) {
        Platform.runLater(() -> {
            for (StockChangeListener listener : listeners) {
                try {
                    listener.onStockChanged(itemId, oldStock, newStock);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Interface for listening to real-time stock changes
     */
    public interface StockChangeListener {
        void onStockChanged(int itemId, int oldStock, int newStock);
    }
}
