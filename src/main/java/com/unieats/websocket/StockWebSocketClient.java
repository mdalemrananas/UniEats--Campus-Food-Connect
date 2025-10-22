package com.unieats.websocket;

import com.unieats.stock.StockUpdateMessage;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket client for JavaFX to receive real-time stock updates.
 * Uses Platform.runLater() to safely update JavaFX UI from WebSocket thread.
 */
public class StockWebSocketClient extends WebSocketClient {
    
    // Listeners that will be notified when stock updates arrive
    private final List<StockUpdateListener> listeners = new ArrayList<>();
    
    public StockWebSocketClient(URI serverUri) {
        super(serverUri);
    }
    
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("✓ Connected to WebSocket server");
    }
    
    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        
        // Check message type
        if (message.contains("\"type\":\"STOCK_UPDATE\"")) {
            // Parse stock update message
            StockUpdateMessage update = StockUpdateMessage.fromJson(message);
            
            if (update != null) {
                // Notify all listeners on JavaFX UI thread (thread-safe)
                Platform.runLater(() -> {
                    for (StockUpdateListener listener : listeners) {
                        listener.onStockUpdate(update);
                    }
                });
            }
        } else if (message.contains("\"type\":\"PURCHASE_FAILED\"")) {
            // Notify about purchase failure
            Platform.runLater(() -> {
                for (StockUpdateListener listener : listeners) {
                    listener.onPurchaseFailed(message);
                }
            });
        } else if (message.contains("\"type\":\"CONNECTED\"")) {
            System.out.println("Server confirmed connection");
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from WebSocket server. Reason: " + reason);
        
        // Notify listeners about disconnection on JavaFX thread
        Platform.runLater(() -> {
            for (StockUpdateListener listener : listeners) {
                listener.onDisconnected();
            }
        });
    }
    
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }
    
    /**
     * Add a listener to be notified of stock updates
     */
    public void addStockUpdateListener(StockUpdateListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener
     */
    public void removeStockUpdateListener(StockUpdateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Send purchase request to server
     */
    public void requestPurchase(int itemId) {
        String message = String.format("{\"type\":\"PURCHASE\",\"itemId\":%d}", itemId);
        send(message);
        System.out.println("Sent purchase request for item " + itemId);
    }
    
    /**
     * Query current stock for an item
     */
    public void queryStock(int itemId) {
        String message = String.format("{\"type\":\"GET_STOCK\",\"itemId\":%d}", itemId);
        send(message);
        System.out.println("Queried stock for item " + itemId);
    }
    
    /**
     * Interface for stock update listeners
     */
    public interface StockUpdateListener {
        /**
         * Called when a stock update is received (on JavaFX UI thread)
         */
        void onStockUpdate(StockUpdateMessage update);
        
        /**
         * Called when purchase fails (on JavaFX UI thread)
         */
        void onPurchaseFailed(String message);
        
        /**
         * Called when disconnected from server (on JavaFX UI thread)
         */
        void onDisconnected();
    }
}
