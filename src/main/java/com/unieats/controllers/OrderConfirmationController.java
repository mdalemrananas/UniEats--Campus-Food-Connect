package com.unieats.controllers;

import com.unieats.OrderInfo;
import com.unieats.User;
import com.unieats.dao.OrderDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class OrderConfirmationController {

    @FXML private Label orderIdLabel;
    @FXML private Label shopNameLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label statusLabel;
    @FXML private Label orderTimeLabel;
    @FXML private Label estimatedDeliveryLabel;
    @FXML private ListView<OrderInfo.OrderItemInfo> orderItemsList;
    @FXML private Button viewOrderButton;
    @FXML private Button continueShoppingButton;

    private final OrderDao orderDao = new OrderDao();
    private int orderId;
    private User currentUser;
    private OrderInfo orderInfo;

    @FXML
    private void initialize() {
        setupOrderItemsList();
    }

    private void setupOrderItemsList() {
        orderItemsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrderInfo.OrderItemInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Create item display
                VBox itemBox = new VBox(4);
                itemBox.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

                HBox header = new HBox();
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(item.itemName);
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label priceLabel = new Label(String.format("$%.2f", item.totalPrice));
                priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");

                header.getChildren().addAll(nameLabel, spacer, priceLabel);

                HBox details = new HBox(16);
                Label qtyLabel = new Label("Qty: " + item.quantity);
                qtyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

                Label unitPriceLabel = new Label(String.format("$%.2f each", item.price));
                unitPriceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

                details.getChildren().addAll(qtyLabel, unitPriceLabel);

                itemBox.getChildren().addAll(header, details);
                setGraphic(itemBox);
                setText(null);
            }
        });
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadOrderDetails();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadOrderDetails() {
        try {
            orderInfo = orderDao.getOrderById(orderId);
            if (orderInfo == null) {
                showAlert("Error", "Order not found.");
                return;
            }

            // Populate order details
            orderIdLabel.setText("#" + orderInfo.getId());
            shopNameLabel.setText(orderInfo.getShopName());
            totalAmountLabel.setText(String.format("$%.2f", orderInfo.getTotalPrice()));
            statusLabel.setText(capitalizeFirst(orderInfo.getStatus()));
            
            if (orderInfo.getCreatedAt() != null) {
                orderTimeLabel.setText(orderInfo.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            }

            // Set estimated delivery time
            estimatedDeliveryLabel.setText("25-35 minutes");

            // Load order items
            if (orderInfo.getItems() != null) {
                ObservableList<OrderInfo.OrderItemInfo> items = FXCollections.observableArrayList(orderInfo.getItems());
                orderItemsList.setItems(items);
            }

        } catch (Exception e) {
            showAlert("Error", "Failed to load order details: " + e.getMessage());
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @FXML
    private void handleViewOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_details.fxml"));
            Parent root = loader.load();
            OrderDetailsController controller = loader.getController();
            if (controller != null) {
                controller.setOrderId(orderId);
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) viewOrderButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Order Details");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to order details: " + e.getMessage());
        }
    }

    @FXML
    private void handleContinueShopping() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();
            MenuController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) continueShoppingButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Menu");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to menu: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
