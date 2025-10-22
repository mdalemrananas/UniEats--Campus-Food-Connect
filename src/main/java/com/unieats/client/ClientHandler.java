package com.unieats.client;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ClientHandler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Consumer<String> messageListener;  // For real-time updates

    public ClientHandler(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void start() {
        executor.submit(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    if (messageListener != null) {
                        final String msgCopy = message;
                        Platform.runLater(() -> messageListener.accept(msgCopy));
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    // Show alert on disconnection
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setContentText("Server disconnected. Please restart the app.");
                    alert.show();
                });
            }
        });
    }

    public void sendOrder(int itemId) {
        out.println("ORDER:" + itemId);
    }

    public void setMessageListener(Consumer<String> listener) {
        this.messageListener = listener;
    }

    public void stop() {
        executor.shutdown();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
