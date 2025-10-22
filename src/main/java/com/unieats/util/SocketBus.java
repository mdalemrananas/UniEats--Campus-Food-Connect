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
        // Also attempt network publish so cross-process clients receive the event even
        // when this JVM does not host the hub server instance.
        try {
            publishOverNetwork(json);
        } catch (Exception ignored) {}
    }

    private static void publishOverNetwork(String json) {
        try {
            final java.util.concurrent.CountDownLatch opened = new java.util.concurrent.CountDownLatch(1);
            final java.util.concurrent.atomic.AtomicBoolean sent = new java.util.concurrent.atomic.AtomicBoolean(false);
            org.java_websocket.client.WebSocketClient client = new org.java_websocket.client.WebSocketClient(java.net.URI.create("ws://localhost:7071")) {
                @Override public void onOpen(org.java_websocket.handshake.ServerHandshake handshakedata) {
                    try { this.send(json); sent.set(true); } catch (Exception ignored) {}
                    try { this.close(); } catch (Exception ignored) {}
                    opened.countDown();
                }
                @Override public void onMessage(String message) { }
                @Override public void onClose(int code, String reason, boolean remote) { }
                @Override public void onError(Exception ex) { opened.countDown(); }
            };
            client.connect();
            // Wait briefly for connection attempt to avoid blocking caller
            opened.await(800, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!sent.get()) {
                try { client.close(); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
        }
    }
}
