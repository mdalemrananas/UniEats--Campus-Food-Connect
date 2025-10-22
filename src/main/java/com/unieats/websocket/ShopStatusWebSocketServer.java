package com.unieats.websocket;

import com.google.gson.Gson;
import com.unieats.models.ShopStatusMessage;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * WebSocket server for broadcasting shop status changes (approval/rejection) in real-time
 */
public class ShopStatusWebSocketServer extends WebSocketServer {
    private static final int PORT = 8082;
    private static ShopStatusWebSocketServer instance;
    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private final Gson gson = new Gson();
    
    public ShopStatusWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }
    
    public static synchronized ShopStatusWebSocketServer getInstance() {
        if (instance == null) {
            System.out.println("Creating Shop Status WebSocket Server on port " + PORT + "...");
            instance = new ShopStatusWebSocketServer(PORT);
            try {
                instance.start();
                System.out.println("✓ Shop Status WebSocket Server started on port " + PORT);
            } catch (Exception e) {
                System.err.println("❌ Failed to start Shop Status WebSocket Server: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Shop Status WebSocket Server already running");
        }
        return instance;
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        System.out.println("Shop Status WebSocket: Client connected from " + conn.getRemoteSocketAddress());
        System.out.println("Shop Status WebSocket: Total clients: " + clients.size());
        // Small delay to ensure client is fully registered
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        System.out.println("Shop Status WebSocket: Client disconnected: " + reason);
        System.out.println("Shop Status WebSocket: Total clients: " + clients.size());
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Shop Status WebSocket: Received message: " + message);
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Shop Status WebSocket Error: " + ex.getMessage());
        if (conn != null) {
            clients.remove(conn);
        }
    }
    
    @Override
    public void onStart() {
        System.out.println("Shop Status WebSocket Server started successfully on port " + PORT);
    }
    
    /**
     * Broadcast shop status change to all connected clients
     */
    public void broadcastShopStatusChange(int shopId, int ownerId, String shopName, String newStatus) {
        ShopStatusMessage message = new ShopStatusMessage(shopId, ownerId, shopName, newStatus, "status_changed");
        String json = gson.toJson(message);
        
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║ BROADCASTING SHOP STATUS CHANGE                        ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("  Shop ID:     " + shopId);
        System.out.println("  Shop Name:   " + shopName);
        System.out.println("  New Status:  " + newStatus);
        System.out.println("  Owner ID:    " + ownerId);
        System.out.println("  JSON:        " + json);
        System.out.println("  Connected clients (before broadcast): " + clients.size());
        
        synchronized (clients) {
            Set<WebSocket> disconnected = new HashSet<>();
            int sentCount = 0;
            for (WebSocket client : clients) {
                try {
                    if (client.isOpen()) {
                        client.send(json);
                        sentCount++;
                        System.out.println("  ✓ Sent to client: " + client.getRemoteSocketAddress());
                    } else {
                        System.out.println("  ✗ Skipped closed client: " + client.getRemoteSocketAddress());
                        disconnected.add(client);
                    }
                } catch (Exception e) {
                    System.err.println("  ✗ Failed to send to client: " + e.getMessage());
                    disconnected.add(client);
                }
            }
            clients.removeAll(disconnected);
            System.out.println("  Successfully sent to " + sentCount + "/" + (sentCount + disconnected.size()) + " clients");
        }
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
    }
    
    public int getClientCount() {
        return clients.size();
    }

    /**
     * Broadcast a raw JSON string to all connected clients.
     * Intended for hub-coordinated generic topic sync.
     */
    public void broadcastRaw(String json) {
        synchronized (clients) {
            Set<WebSocket> disconnected = new java.util.HashSet<>();
            for (WebSocket client : clients) {
                try {
                    if (client.isOpen()) client.send(json);
                    else disconnected.add(client);
                } catch (Exception e) {
                    disconnected.add(client);
                }
            }
            clients.removeAll(disconnected);
        }
    }
}
