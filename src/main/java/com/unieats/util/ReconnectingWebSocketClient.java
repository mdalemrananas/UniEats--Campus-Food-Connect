package com.unieats.util;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Minimal reconnecting WebSocket client with backoff.
 */
public final class ReconnectingWebSocketClient {
    private final URI uri;
    private final Consumer<String> messageHandler;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ws-reconnect");
        t.setDaemon(true);
        return t;
    });
    private volatile WebSocketClient client;
    private volatile boolean stopped = false;

    public ReconnectingWebSocketClient(String url, Consumer<String> messageHandler) {
        this.uri = URI.create(Objects.requireNonNull(url));
        this.messageHandler = Objects.requireNonNull(messageHandler);
    }

    public void start() {
        stopped = false;
        connectNow(0);
    }

    public void stop() {
        stopped = true;
        try {
            if (client != null)
                client.close();
        } catch (Exception ignored) {
        }
        scheduler.shutdownNow();
    }

    private void connectNow(long delayMs) {
        if (stopped)
            return;
        scheduler.schedule(() -> {
            try {
                client = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        /* connected */ }

                    @Override
                    public void onMessage(String s) {
                        messageHandler.accept(s);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        scheduleReconnect();
                    }

                    @Override
                    public void onError(Exception e) {
                        scheduleReconnect();
                    }
                };
                client.connect();
            } catch (Exception e) {
                scheduleReconnect();
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private void scheduleReconnect() {
        if (stopped)
            return;
        connectNow(1000); // 1s backoff; can be increased if needed
    }
}
