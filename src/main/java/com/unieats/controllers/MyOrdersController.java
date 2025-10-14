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
import java.util.List;

public class MyOrdersController {

    @FXML private Button backButton;
    @FXML private Button currentOrdersTab;
    @FXML private Button orderHistoryTab;
    @FXML private VBox contentContainer;
    @FXML private VBox currentOrdersSection;
    @FXML private VBox orderHistorySection;
    @FXML private ListView<OrderInfo> currentOrdersList;
    @FXML private ListView<OrderInfo> orderHistoryList;
    @FXML private VBox currentOrdersEmpty;
    @FXML private VBox orderHistoryEmpty;
    
    // Bottom navigation
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;

    private final OrderDao orderDao = new OrderDao();
    private User currentUser;

    @FXML
    private void initialize() {
        setupOrderLists();
        setupNavigationHandlers();
        showCurrentOrdersTab();
    }

    private void setupOrderLists() {
        // Setup current orders list
        currentOrdersList.setCellFactory(listView -> new OrderListCell());
        
        // Setup order history list
        orderHistoryList.setCellFactory(listView -> new OrderListCell());
    }

    private void setupNavigationHandlers() {
        // Bottom navigation handlers
        navHome.setOnMouseClicked(e -> navigateTo("/fxml/menu.fxml", "UniEats - Menu"));
        navCart.setOnMouseClicked(e -> navigateTo("/fxml/cart.fxml", "UniEats - Cart"));
        navFav.setOnMouseClicked(e -> navigateTo("/fxml/menu.fxml", "UniEats - Menu"));
        navProfile.setOnMouseClicked(e -> navigateTo("/fxml/profile.fxml", "UniEats - Profile"));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadOrders();
    }

    private void loadOrders() {
        if (currentUser == null) return;
        
        try {
            // Load current orders (pending, preparing, ready, out_for_delivery)
            List<OrderInfo> currentOrders = orderDao.getCurrentOrdersByUserId(currentUser.getId());
            ObservableList<OrderInfo> currentOrdersObservable = FXCollections.observableArrayList(currentOrders);
            currentOrdersList.setItems(currentOrdersObservable);
            
            // Load order history (delivered, cancelled)
            List<OrderInfo> orderHistory = orderDao.getOrderHistoryByUserId(currentUser.getId());
            ObservableList<OrderInfo> orderHistoryObservable = FXCollections.observableArrayList(orderHistory);
            orderHistoryList.setItems(orderHistoryObservable);
            
            // Show/hide empty states
            currentOrdersEmpty.setVisible(currentOrders.isEmpty());
            orderHistoryEmpty.setVisible(orderHistory.isEmpty());
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load orders: " + e.getMessage());
        }
    }

    @FXML
    private void handleCurrentOrdersTab() {
        showCurrentOrdersTab();
    }

    @FXML
    private void handleOrderHistoryTab() {
        showOrderHistoryTab();
    }

    private void showCurrentOrdersTab() {
        // Update tab styles
        currentOrdersTab.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8 0 0 8; -fx-padding: 12 24; -fx-cursor: hand;");
        orderHistoryTab.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 0 8 8 0; -fx-padding: 12 24; -fx-cursor: hand;");
        
        // Show current orders section
        currentOrdersSection.setVisible(true);
        orderHistorySection.setVisible(false);
    }

    private void showOrderHistoryTab() {
        // Update tab styles
        currentOrdersTab.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8 0 0 8; -fx-padding: 12 24; -fx-cursor: hand;");
        orderHistoryTab.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 0 8 8 0; -fx-padding: 12 24; -fx-cursor: hand;");
        
        // Show order history section
        currentOrdersSection.setVisible(false);
        orderHistorySection.setVisible(true);
    }

    @FXML
    private void handleBack() {
        navigateTo("/fxml/menu.fxml", "UniEats - Menu");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

            // Pass user to target controllers
            Object controller = loader.getController();
            if (controller instanceof MenuController mc && currentUser != null) {
                mc.setCurrentUser(currentUser);
            } else if (controller instanceof CartController cc && currentUser != null) {
                cc.setCurrentUserId(currentUser.getId());
            } else if (controller instanceof ProfileController pc && currentUser != null) {
                pc.setCurrentUser(currentUser);
            }

        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Custom cell for order display
    private class OrderListCell extends ListCell<OrderInfo> {
        @Override
        protected void updateItem(OrderInfo order, boolean empty) {
            super.updateItem(order, empty);
            
            if (empty || order == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox orderCard = createOrderCard(order);
                setGraphic(orderCard);
                setText(null);
            }
        }

        private VBox createOrderCard(OrderInfo order) {
            VBox card = new VBox();
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;");
            card.setSpacing(8);

            // Header with order ID and status
            HBox header = new HBox();
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setSpacing(8);

            Label orderIdLabel = new Label("#" + order.getId());
            orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label statusLabel = new Label(capitalizeFirst(order.getStatus()));
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getStatusColor(order.getStatus()) + ";");

            header.getChildren().addAll(orderIdLabel, spacer, statusLabel);

            // Shop name
            Label shopLabel = new Label(order.getShopName());
            shopLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

            // Order details
            HBox details = new HBox();
            details.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            details.setSpacing(16);

            Label timeLabel = new Label();
            if (order.getCreatedAt() != null) {
                timeLabel.setText(order.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
            }
            timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

            Label totalLabel = new Label("$" + String.format("%.2f", order.getTotalPrice()));
            totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");

            details.getChildren().addAll(timeLabel, spacer, totalLabel);

            card.getChildren().addAll(header, shopLabel, details);

            // Add click handler to view order details
            card.setOnMouseClicked(e -> viewOrderDetails(order));

            return card;
        }

        private String getStatusColor(String status) {
            return switch (status.toLowerCase()) {
                case "pending" -> "#ff9800";
                case "preparing" -> "#2196f3";
                case "ready" -> "#4caf50";
                case "out_for_delivery" -> "#9c27b0";
                case "delivered" -> "#4caf50";
                case "cancelled" -> "#f44336";
                default -> "#6c757d";
            };
        }

        private void viewOrderDetails(OrderInfo order) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_details.fxml"));
                Parent root = loader.load();
                OrderDetailsController controller = loader.getController();
                if (controller != null) {
                    controller.setOrderId(order.getId());
                    controller.setCurrentUser(currentUser);
                }

                Stage stage = (Stage) getScene().getWindow();
                Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
                stage.setScene(scene);
                stage.setTitle("UniEats - Order Details");
                stage.show();
            } catch (Exception e) {
                showAlert("Error", "Failed to load order details: " + e.getMessage());
            }
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase().replace("_", " ");
    }
}
