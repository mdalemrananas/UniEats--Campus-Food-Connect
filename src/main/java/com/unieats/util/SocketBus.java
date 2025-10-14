package com.unieats.util;

public final class SocketBus {
    private SocketBus() {
    }

    private static InventoryWebSocketServer server;

    public static void setServer(InventoryWebSocketServer s) {
        server = s;
    }

    public static InventoryWebSocketServer getServer() {
        return server;
    }

    public static void broadcast(String json) {
        if (server != null)
            server.broadcastJson(json);
    }
}
