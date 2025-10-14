package com.unieats.util;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InventoryWebSocketServer extends WebSocketServer {
    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());

    public InventoryWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Echo and no-op for now; clients in this app only receive broadcasts
        conn.send("ok");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Log error
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        // Server started
        System.out.println("InventoryWebSocketServer started on port " + getPort());
    }

    public void broadcastJson(String json) {
        synchronized (clients) {
            for (WebSocket ws : clients) {
                try {
                    ws.send(json);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
