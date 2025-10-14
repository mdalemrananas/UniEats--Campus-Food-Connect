package com.unieats.controllers;

import com.unieats.util.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.unieats.util.ReconnectingWebSocketClient;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Order Management window
 * Handles displaying and updating order statuses
 */
public class OrderManagementController implements Initializable {

    @FXML
    private TableView<DatabaseHelper.Order_seller> ordersTable;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, Integer> idColumn;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, String> customerNameColumn;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, String> foodNameColumn;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, Integer> quantityColumn;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, Double> totalPriceColumn;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, String> statusColumn;
    @FXML
    private TableColumn<DatabaseHelper.Order_seller, String> createdAtColumn;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addSampleButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Button backButton;

    private ObservableList<DatabaseHelper.Order_seller> orders;
    private int shopId = 1; // default; can be set dynamically
    private ReconnectingWebSocketClient wsClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupEventHandlers();
        loadOrders();
        startSocketListener();
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
        loadOrders();
    }

    private void setupTable() {
        orders = FXCollections.observableArrayList();
        ordersTable.setItems(orders);

        // Map columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // ComboBox for status
        statusColumn.setCellFactory(param -> new TableCell<DatabaseHelper.Order_seller, String>() {
            private final ComboBox<String> statusComboBox = new ComboBox<>();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    if (statusComboBox.getItems().isEmpty()) {
                        statusComboBox.getItems().addAll("pending", "preparing", "delivered", "cancelled", "completed");
                        statusComboBox.setStyle("-fx-font-size: 12px; -fx-pref-width: 120;");
                    }

                    DatabaseHelper.Order_seller order = getTableView().getItems().get(getIndex());
                    statusComboBox.setValue(order.getStatus());

                    statusComboBox.setOnAction(event -> {
                        String newStatus = statusComboBox.getValue();
                        if (newStatus != null && !newStatus.equals(order.getStatus())) {
                            handleStatusChange(order, newStatus);
                        }
                    });

                    setGraphic(statusComboBox);
                }
            }
        });

        // Pretty display for total price
        totalPriceColumn.setCellFactory(column -> new TableCell<DatabaseHelper.Order_seller, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.2f", price));
                }
            }
        });

        // Format datetime
        createdAtColumn.setCellFactory(column -> new TableCell<DatabaseHelper.Order_seller, String>() {
            @Override
            protected void updateItem(String dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    try {
                        setText(dateTime.substring(0, 16).replace("T", " "));
                    } catch (Exception e) {
                        setText(dateTime);
                    }
                }
            }
        });

        // Column widths
        idColumn.setPrefWidth(60);
        customerNameColumn.setPrefWidth(120);
        foodNameColumn.setPrefWidth(150);
        quantityColumn.setPrefWidth(80);
        totalPriceColumn.setPrefWidth(100);
        statusColumn.setPrefWidth(130);
        createdAtColumn.setPrefWidth(150);
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadOrders());
        addSampleButton.setOnAction(e -> addSampleOrders());
        if (backButton != null)
            backButton.setOnAction(e -> handleBack());
    }

    private void loadOrders() {
        try {
            orders.clear();

            // 🔹 Updated line: Load from seller_orders instead of orders table
            orders.addAll(DatabaseHelper.getSellerOrders());

            if (orders.isEmpty()) {
                showStatus("No seller orders found in database.");
            } else {
                showStatus("Loaded " + orders.size() + " seller orders.");
            }
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            showStatus("Error loading orders: " + e.getMessage());
        }
    }

    private void handleStatusChange(DatabaseHelper.Order_seller order, String newStatus) {
        boolean success = DatabaseHelper.updateOrderStatus(order.getId(), newStatus);
        if (success) {
            order.setStatus(newStatus);
            showStatus("Order #" + order.getId() + " updated to: " + newStatus);
        } else {
            showStatus("Failed to update order. Reloading...");
            loadOrders();
        }
    }

    private void addSampleOrders() {
        try {
            DatabaseHelper.addSampleOrders(shopId);
            loadOrders();
            showStatus("Sample orders added successfully!");
        } catch (Exception e) {
            System.err.println("Error adding sample orders: " + e.getMessage());
            showAlert("Error", "Failed to add sample orders: " + e.getMessage());
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void startSocketListener() {
        wsClient = new ReconnectingWebSocketClient("ws://localhost:7071", message -> {
            if (message == null || !message.contains("order_update"))
                return;
            if (shopId > 0 && !message.contains("\"shopId\":" + shopId))
                return;
            javafx.application.Platform.runLater(this::loadOrders);
        });
        wsClient.start();
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/stall.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ordersTable.getScene().getWindow();
            javafx.scene.Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("Seller Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
