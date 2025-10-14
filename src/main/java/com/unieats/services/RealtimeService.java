package com.unieats.services;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Polls the database and watches the attachments directory to emit lightweight
 * change events to listeners so the admin UI can refresh in near real-time
 * without blocking the JavaFX thread.
 */
public class RealtimeService {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";
    private static final Path ATTACHMENTS_DIR = Paths.get("src/main/resources/reports/attachments");

    private static RealtimeService instance;

    private final ScheduledExecutorService scheduler;
    private final ExecutorService worker;
    private final List<Consumer<String>> listeners;
    private DatagramSocketReceiver udpReceiver;
    private volatile boolean started = false;

    // State snapshots for change detection
    private volatile String usersSig = "";
    private volatile String shopsSig = "";
    private volatile String reportsSig = "";
    private volatile String paymentsSig = "";

    private RealtimeService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rt-scheduler");
            t.setDaemon(true);
            return t;
        });
        this.worker = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "rt-worker");
            t.setDaemon(true);
            return t;
        });
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public static synchronized RealtimeService getInstance() {
        if (instance == null) instance = new RealtimeService();
        return instance;
    }

    public void onEvent(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    public synchronized void start() {
        if (started) return;
        started = true;

        // Poll DB every 3 seconds
        scheduler.scheduleAtFixedRate(this::pollDatabase, 0, 3, TimeUnit.SECONDS);

        // Start filesystem watcher in background
        worker.submit(this::watchAttachments);

        // Start UDP listener for cross-process notifications
        udpReceiver = new DatagramSocketReceiver(this::emit);
        worker.submit(udpReceiver);
    }

    public synchronized void stop() {
        scheduler.shutdownNow();
        worker.shutdownNow();
        if (udpReceiver != null) udpReceiver.close();
        started = false;
    }

    private void pollDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String u = signature(conn, "users");
            String s = signature(conn, "shops");
            String r = signature(conn, "reports");
            String p = signature(conn, "payments");

            if (!Objects.equals(u, usersSig)) { usersSig = u; emit("users"); }
            if (!Objects.equals(s, shopsSig)) { shopsSig = s; emit("shops"); }
            if (!Objects.equals(r, reportsSig)) { reportsSig = r; emit("reports"); }
            if (!Objects.equals(p, paymentsSig)) { paymentsSig = p; emit("payments"); }
        } catch (SQLException ignored) {}
    }

    private String signature(Connection conn, String table) {
        String sql = "SELECT COUNT(*) c, COALESCE(MAX(updated_at), '') m FROM " + table;
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("c") + "|" + rs.getString("m");
            }
        } catch (SQLException ignored) {}
        return "";
    }

    private void watchAttachments() {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            Files.createDirectories(ATTACHMENTS_DIR);
            ATTACHMENTS_DIR.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = ws.poll(5, TimeUnit.SECONDS);
                if (key == null) continue;
                for (WatchEvent<?> ev : key.pollEvents()) {
                    emit("reports"); // attachments affect reports rendering
                }
                key.reset();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ignored) {
        }
    }

    private void emit(String topic) {
        for (Consumer<String> l : new ArrayList<>(listeners)) {
            try { l.accept(topic); } catch (Exception ignored) {}
        }
    }
}

class DatagramSocketReceiver implements Runnable {
    private final java.util.function.Consumer<String> handler;
    private volatile boolean running = true;
    private java.net.DatagramSocket socket;
    private static final int PORT = 46877;

    DatagramSocketReceiver(java.util.function.Consumer<String> handler) {
        this.handler = handler;
    }

    @Override public void run() {
        try {
            socket = new java.net.DatagramSocket(PORT, java.net.InetAddress.getByName("127.0.0.1"));
            socket.setSoTimeout(1000);
            byte[] buf = new byte[256];
            while (running && !Thread.currentThread().isInterrupted()) {
                java.net.DatagramPacket packet = new java.net.DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength(), java.nio.charset.StandardCharsets.UTF_8);
                    if (msg != null && !msg.isBlank()) handler.accept(msg);
                } catch (java.net.SocketTimeoutException ignored) {}
            }
        } catch (Exception ignored) {
        } finally {
            if (socket != null) socket.close();
        }
    }

    void close() {
        running = false;
        if (socket != null) socket.close();
    }
}


