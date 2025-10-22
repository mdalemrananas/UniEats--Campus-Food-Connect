package com.unieats.stock;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * WebSocket Server for broadcasting real-time stock updates to all connected clients.
 * When a user purchases an item, this server broadcasts the new stock to all clients instantly.
 */
public class StockWebSocketServer extends WebSocketServer {
    
    // Thread-safe set to track all connected clients
    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    
    // Reference to stock service
    private final StockService stockService;
    
    public StockWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        this.stockService = StockService.getInstance();
        System.out.println("StockWebSocketServer initialized on port " + port);
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // New client connected
        clients.add(conn);
        System.out.println("New client connected. Total clients: " + clients.size());
        
        // Send welcome message
        conn.send("{\"type\":\"CONNECTED\",\"message\":\"Connected to stock update server\"}");
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Client disconnected
        clients.remove(conn);
        System.out.println("Client disconnected. Total clients: " + clients.size());
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);
        
        try {
            // Parse message type
            if (message.contains("\"type\":\"PURCHASE\"")) {
                handlePurchaseRequest(conn, message);
            } else if (message.contains("\"type\":\"GET_STOCK\"")) {
                handleStockQuery(conn, message);
            } else {
                conn.send("{\"type\":\"ERROR\",\"message\":\"Unknown message type\"}");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            conn.send("{\"type\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Handle purchase request from a client
     */
    private void handlePurchaseRequest(WebSocket conn, String message) {
        try {
            // Extract itemId from JSON message
            int itemIdStart = message.indexOf("\"itemId\":") + 9;
            int itemIdEnd = message.indexOf(",", itemIdStart);
            if (itemIdEnd == -1) {
                itemIdEnd = message.indexOf("}", itemIdStart);
            }
            int itemId = Integer.parseInt(message.substring(itemIdStart, itemIdEnd).trim());
            
            // Attempt to purchase (thread-safe)
            boolean success = stockService.purchaseItem(itemId);
            
            if (success) {
                // Get updated item info
                var item = stockService.getItem(itemId);
                
                // Create stock update message
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                StockUpdateMessage updateMsg = new StockUpdateMessage(
                    itemId, 
                    item.getName(), 
                    item.getStock(),
                    timestamp
                );
                
                // Broadcast to ALL connected clients (including the purchaser)
                broadcastStockUpdate(updateMsg);
                
                System.out.println("Purchase successful. Broadcasted update to " + clients.size() + " clients");
            } else {
                // Send failure response only to requesting client
                conn.send("{\"type\":\"PURCHASE_FAILED\",\"itemId\":" + itemId + ",\"message\":\"Out of stock or item not found\"}");
            }
            
        } catch (Exception e) {
            System.err.println("Error handling purchase: " + e.getMessage());
            e.printStackTrace();
            conn.send("{\"type\":\"ERROR\",\"message\":\"Invalid purchase request\"}");
        }
    }
    
    /**
     * Handle stock query from a client
     */
    private void handleStockQuery(WebSocket conn, String message) {
        try {
            // Extract itemId from JSON message
            int itemIdStart = message.indexOf("\"itemId\":") + 9;
            int itemIdEnd = message.indexOf(",", itemIdStart);
            if (itemIdEnd == -1) {
                itemIdEnd = message.indexOf("}", itemIdStart);
            }
            int itemId = Integer.parseInt(message.substring(itemIdStart, itemIdEnd).trim());
            
            // Get current stock
            var item = stockService.getItem(itemId);
            
            if (item != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                StockUpdateMessage msg = new StockUpdateMessage(
                    itemId, 
                    item.getName(), 
                    item.getStock(),
                    timestamp
                );
                conn.send(msg.toJson());
            } else {
                conn.send("{\"type\":\"ERROR\",\"message\":\"Item not found\"}");
            }
            
        } catch (Exception e) {
            System.err.println("Error handling stock query: " + e.getMessage());
            conn.send("{\"type\":\"ERROR\",\"message\":\"Invalid stock query\"}");
        }
    }
    
    /**
     * Broadcast stock update to all connected clients
     */
    public void broadcastStockUpdate(StockUpdateMessage message) {
        String json = message.toJson();
        
        synchronized (clients) {
            for (WebSocket client : clients) {
                try {
                    client.send(json);
                } catch (Exception e) {
                    System.err.println("Error sending to client: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }
    
    @Override
    public void onStart() {
        System.out.println("✓ StockWebSocketServer started successfully on port " + getPort());
        System.out.println("Waiting for client connections...");
    }
}
