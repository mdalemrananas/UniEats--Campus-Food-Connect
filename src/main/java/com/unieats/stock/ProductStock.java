package com.unieats.stock;

/**
 * Represents a product with stock quantity that needs to be managed in a thread-safe manner.
 */
public class ProductStock {
    private String productId;
    private String name;
    private volatile int stockQuantity;

    public ProductStock(String productId, String name, int initialStock) {
        this.productId = productId;
        this.name = name;
        this.stockQuantity = initialStock;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    // Unsafe method - no synchronization
    public boolean unsafeOrder(int quantity) {
        if (stockQuantity >= quantity) {
            // Simulate processing delay
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    // Safe method using synchronized block
    public synchronized boolean safeOrder(int quantity) {
        if (stockQuantity >= quantity) {
            // Simulate processing delay
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    // Method to simulate real-time stock updates
    public synchronized void updateStock(int newQuantity) {
        this.stockQuantity = newQuantity;
    }
}
