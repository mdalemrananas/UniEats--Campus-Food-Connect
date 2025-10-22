package com.unieats.websocket;

import com.google.gson.Gson;
import com.unieats.models.ShopStatusMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * WebSocket client for receiving real-time shop status updates
 */
public class ShopStatusWebSocketClient extends WebSocketClient {
    private static final String SERVER_URI = "ws://localhost:8082";
    private final Gson gson = new Gson();
    private final List<Consumer<ShopStatusMessage>> listeners = new ArrayList<>();
    private static ShopStatusWebSocketClient instance;
    private boolean isConnected = false;
    
    private ShopStatusWebSocketClient(URI serverUri) {
        super(serverUri);
    }
    
    public static synchronized ShopStatusWebSocketClient getInstance() {
        if (instance == null) {
            try {
                instance = new ShopStatusWebSocketClient(new URI(SERVER_URI));
                // Connect asynchronously to avoid blocking UI thread
                instance.connect();
                System.out.println("Shop Status WebSocket Client: Attempting to connect to " + SERVER_URI);
            } catch (Exception e) {
                System.err.println("Failed to initialize Shop Status WebSocket Client: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return instance;
    }
    
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        isConnected = true;
        System.out.println("✓ Shop Status WebSocket Client: Connected to server");
        System.out.println("  Total listeners registered: " + listeners.size());
        System.out.println("  Connection status: " + (isConnected ? "CONNECTED" : "DISCONNECTED"));
        System.out.println("  Client URI: " + getURI());
        System.out.println("  Client ready state: " + getReadyState());
    }
    
    @Override
    public void onMessage(String message) {
        try {
            System.out.println("\n━━━ WebSocket Client: RECEIVED MESSAGE ━━━");
            System.out.println("Raw message: " + message);
            
            ShopStatusMessage statusMsg = gson.fromJson(message, ShopStatusMessage.class);
            System.out.println("Parsed message: " + statusMsg);
            System.out.println("Notifying " + listeners.size() + " listener(s)...");
            
            // Notify all registered listeners
            synchronized (listeners) {
                int notified = 0;
                for (Consumer<ShopStatusMessage> listener : listeners) {
                    try {
                        listener.accept(statusMsg);
                        notified++;
                    } catch (Exception e) {
                        System.err.println("Error in shop status listener: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                System.out.println("✓ Notified " + notified + " listener(s) successfully");
            }
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        } catch (Exception e) {
            System.err.println("Failed to parse shop status message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected = false;
        System.out.println("Shop Status WebSocket Client: Disconnected - Code: " + code + ", Reason: " + reason);
        // Attempt to reconnect after a delay
        new Thread(() -> {
            try {
                System.out.println("Shop Status WebSocket Client: Attempting to reconnect in 5 seconds...");
                Thread.sleep(5000);
                reconnect();
                System.out.println("Shop Status WebSocket Client: Reconnection attempt initiated");
            } catch (Exception e) {
                System.err.println("Failed to reconnect: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    @Override
    public void onError(Exception ex) {
        System.err.println("Shop Status WebSocket Client Error: " + ex.getMessage());
        ex.printStackTrace();
    }
    
    /**
     * Register a listener to receive shop status updates
     * @param listener Consumer that will be called when shop status changes
     */
    public void addShopStatusListener(Consumer<ShopStatusMessage> listener) {
        synchronized (listeners) {
            listeners.add(listener);
            System.out.println("✓ Listener added. Total listeners: " + listeners.size() + ", Connected: " + isConnected);
        }
    }
    
    /**
     * Remove a listener
     */
    public void removeShopStatusListener(Consumer<ShopStatusMessage> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Check if client is properly connected and ready
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Debug method to check client status
     */
    public void debugConnectionStatus() {
        System.out.println("\n━━━ CLIENT CONNECTION DEBUG ━━━");
        System.out.println("Connected: " + isConnected);
        System.out.println("URI: " + getURI());
        System.out.println("Ready State: " + getReadyState());
        System.out.println("Listeners: " + listeners.size());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
