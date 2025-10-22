package com.unieats.stock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages product stock and notifies observers of changes.
 */
public class StockManager {
    private static StockManager instance;
    private final Map<String, ProductStock> products = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArraySet<StockUpdateListener>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService notificationExecutor = Executors.newCachedThreadPool();

    private StockManager() {
        // Private constructor for singleton
    }

    public static synchronized StockManager getInstance() {
        if (instance == null) {
            instance = new StockManager();
        }
        return instance;
    }

    public void addProduct(ProductStock product) {
        products.put(product.getProductId(), product);
    }

    public ProductStock getProduct(String productId) {
        return products.get(productId);
    }

    public void registerListener(String productId, StockUpdateListener listener) {
        listeners.computeIfAbsent(productId, k -> new CopyOnWriteArraySet<>()).add(listener);
    }

    public void removeListener(String productId, StockUpdateListener listener) {
        if (listeners.containsKey(productId)) {
            listeners.get(productId).remove(listener);
        }
    }

    public void notifyStockUpdate(ProductStock product) {
        if (listeners.containsKey(product.getProductId())) {
            for (StockUpdateListener listener : listeners.get(product.getProductId())) {
                notificationExecutor.submit(() -> listener.onStockUpdate(product));
            }
        }
    }

    public void shutdown() {
        notificationExecutor.shutdown();
    }
}
