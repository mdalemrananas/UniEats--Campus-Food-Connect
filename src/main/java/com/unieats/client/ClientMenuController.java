package com.unieats.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class ClientMenuController {
    @FXML private VBox menuContainer;
    @FXML private Label usernameLabel;

    private ClientHandler clientHandler;
    private Map<Integer, MenuItem> items = new HashMap<>();
    private String username;

    public void setClientHandler(ClientHandler handler) {
        this.clientHandler = handler;
        clientHandler.setMessageListener(this::handleServerMessage);
    }

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText("Welcome, " + username);
    }

    public void loadMenuItems() {
        // For demo, simulate initial items; in real scenario, request from server
        items.put(1, new MenuItem(1, "Burger", 250.0, 1));
        items.put(2, new MenuItem(2, "Pizza", 400.0, 5));
        displayMenu();
    }

    private void displayMenu() {
        Platform.runLater(() -> {
            menuContainer.getChildren().clear();
            for (MenuItem item : items.values()) {
                VBox itemBox = createItemUI(item);
                menuContainer.getChildren().add(itemBox);
            }
        });
    }

    private VBox createItemUI(MenuItem item) {
        VBox box = new VBox(10);
        Label nameLabel = new Label(item.getName());
        Label priceLabel = new Label("Price: $" + item.getPrice());
        Label stockLabel = new Label("Stock: " + item.getStock());
        Button orderButton = new Button("Order");
        orderButton.setOnAction(e -> placeOrder(item.getId()));

        box.getChildren().addAll(nameLabel, priceLabel, stockLabel, orderButton);
        return box;
    }

    private void placeOrder(int itemId) {
        if (items.get(itemId).getStock() > 0) {
            clientHandler.sendOrder(itemId);
        }
    }

    private void handleServerMessage(String message) {
        // Parse server messages, e.g., "UPDATE:1:0"
        String[] parts = message.split(":");
        if (parts.length == 3 && "UPDATE".equals(parts[0])) {
            int itemId = Integer.parseInt(parts[1]);
            int newStock = Integer.parseInt(parts[2]);
            if (items.containsKey(itemId)) {
                items.get(itemId).setStock(newStock);
                Platform.runLater(this::displayMenu);  // Refresh UI
            }
        }
    }
}
