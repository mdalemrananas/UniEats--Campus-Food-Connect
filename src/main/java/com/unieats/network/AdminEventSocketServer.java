package com.unieats.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lightweight TCP server for broadcasting admin realtime events to multiple clients.
 * Each client connection is handled on its own thread; broadcasts are written
 * concurrently using a thread-safe client set.
 */
public class AdminEventSocketServer {
    private final int port;
    private final ExecutorService acceptorExecutor;
    private final ExecutorService clientExecutor;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    // Track all connected clients' writers for broadcast
    private final Set<ClientConnection> clients = Collections.synchronizedSet(new HashSet<>());

    public AdminEventSocketServer(int port) {
        this.port = port;
        this.acceptorExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "admin-sock-acceptor");
            t.setDaemon(true);
            return t;
        });
        this.clientExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "admin-sock-client");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (running) return;
        running = true;
        acceptorExecutor.submit(this::acceptLoop);
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        synchronized (clients) {
            for (ClientConnection c : clients) { c.closeQuietly(); }
            clients.clear();
        }
        clientExecutor.shutdownNow();
        acceptorExecutor.shutdownNow();
    }

    public void broadcast(String message) {
        String payload = message == null ? "" : message;
        synchronized (clients) {
            clients.removeIf(c -> !c.isOpen());
            for (ClientConnection c : clients) {
                c.sendAsync(payload);
            }
        }
    }

    private void acceptLoop() {
        try (ServerSocket ss = new ServerSocket()) {
            this.serverSocket = ss;
            // Bind to localhost only for safety
            ss.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
            ss.setReuseAddress(true);
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = ss.accept();
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);
                    ClientConnection connection = new ClientConnection(socket);
                    clients.add(connection);
                    // Handle client lifecycle on a worker thread (read loop for graceful close)
                    clientExecutor.submit(connection::run);
                } catch (IOException ignored) {
                    if (!running) break;
                }
            }
        } catch (IOException ignored) {
            // swallow; server stop or bind failure
        }
    }

    private static class ClientConnection {
        private final Socket socket;
        private final BufferedWriter writer;
        private volatile boolean open = true;

        ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        void run() {
            // Optionally read to detect client disconnects; here we just block on read with timeout
            try {
                // Block until the socket is closed by peer
                while (open && !socket.isClosed()) {
                    try {
                        // Sleep a bit; writes occur from broadcast
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (!socket.isConnected()) break;
                }
            } finally {
                closeQuietly();
            }
        }

        boolean isOpen() {
            return open && !socket.isClosed();
        }

        void sendAsync(String message) {
            try {
                writer.write(message);
                writer.write('\n');
                writer.flush();
            } catch (IOException e) {
                closeQuietly();
            }
        }

        void closeQuietly() {
            open = false;
            try { writer.close(); } catch (IOException ignored) {}
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}


