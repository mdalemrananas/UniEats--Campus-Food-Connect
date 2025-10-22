package com.unieats.realtime;

import com.unieats.network.AdminEventSocketServer;
import com.unieats.util.InventoryWebSocketServer;
import com.unieats.util.SocketBus;
import com.unieats.websocket.ShopStatusWebSocketServer;

/**
 * Central coordinator to synchronize realtime broadcasts across admin (TCP),
 * user (inventory WebSocket), and seller (shop status WebSocket) channels.
 */
public final class RealtimeBroadcastHub {
    private static final RealtimeBroadcastHub INSTANCE = new RealtimeBroadcastHub();

    private volatile AdminEventSocketServer adminTcp;

    private RealtimeBroadcastHub() {
    }

    public static RealtimeBroadcastHub getInstance() {
        return INSTANCE;
    }

    public void setAdminTcpServer(AdminEventSocketServer server) {
        this.adminTcp = server;
    }

    public void clearAdminTcpServer() {
        this.adminTcp = null;
    }

    /**
     * Broadcast a simple topic to all channels in a thread-safe, best-effort manner.
     * Format for WebSockets is a small JSON envelope: {"type":"topic","topic":"..."}
     */
    public void broadcastTopic(String topic) {
        String safeTopic = topic == null ? "" : topic;
        String jsonEnvelope = "{\"type\":\"topic\",\"topic\":\"" + escapeJson(safeTopic) + "\"}";

        // Admin TCP (newline-delimited plain text)
        AdminEventSocketServer admin = this.adminTcp;
        if (admin != null) {
            try { admin.broadcast(safeTopic); } catch (Exception ignored) {}
        }

        // Seller WS (shop status server) - broadcast as raw JSON envelope
        try {
            ShopStatusWebSocketServer.getInstance().broadcastRaw(jsonEnvelope);
        } catch (Exception ignored) {}

        // User WS (inventory) via SocketBus
        try {
            InventoryWebSocketServer inv = SocketBus.getServer();
            if (inv != null) inv.broadcastJson(jsonEnvelope);
        } catch (Exception ignored) {}
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}


