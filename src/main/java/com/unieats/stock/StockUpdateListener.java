package com.unieats.stock;

/**
 * Interface for receiving stock update notifications.
 */
@FunctionalInterface
public interface StockUpdateListener {
    /**
     * Called when the stock of a product is updated.
     *
     * @param product The updated product with new stock quantity
     */
    void onStockUpdate(ProductStock product);
}
