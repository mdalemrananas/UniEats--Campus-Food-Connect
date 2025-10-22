package com.unieats.controllers;

import com.unieats.OrderInfo;
import com.unieats.User;
import com.unieats.dao.OrderDao;
import com.unieats.util.ThreadSafeUtils;
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
    
    // Pagination controls
    @FXML private VBox paginationContainer;
    @FXML private Button prevPageButton;
    @FXML private HBox pageNumbersContainer;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    
    // Bottom navigation
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;

    private final OrderDao orderDao = new OrderDao();
    private User currentUser;
    
    // Pagination state
    private static final int ORDERS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalOrders = 0;

    @FXML
    private void initialize() {
        setupOrderLists();
        setupNavigationHandlers();
        setupPagination();
        showCurrentOrdersTab(); // Start with current orders at top
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

    private void setupPagination() {
        if (prevPageButton != null) {
            prevPageButton.setOnAction(e -> goToPreviousPage());
        }
        if (nextPageButton != null) {
            nextPageButton.setOnAction(e -> goToNextPage());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadOrders();
    }

    private void loadOrders() {
        if (currentUser == null) return;
        
        // Load orders in background thread to avoid blocking UI
        ThreadSafeUtils.runAsyncWithErrorHandling(
            () -> {
                // Background task - load current orders
                List<OrderInfo> currentOrders = orderDao.getCurrentOrdersByUserId(currentUser.getId());
                ObservableList<OrderInfo> currentOrdersObservable = FXCollections.observableArrayList(currentOrders);
                
                // Update UI on JavaFX thread
                ThreadSafeUtils.runOnFXThread(() -> {
                    currentOrdersList.setItems(currentOrdersObservable);
                    currentOrdersEmpty.setVisible(currentOrders.isEmpty());
                });
                
                // Load order history with pagination
                loadOrderHistoryPage();
            },
            () -> {
                // UI update completed
            },
            exception -> {
                showAlert("Error", "Failed to load orders: " + exception.getMessage());
            }
        );
    }

    private void loadOrderHistoryPage() {
        if (currentUser == null) return;
        
        // Load order history in background thread
        ThreadSafeUtils.runAsyncWithErrorHandling(
            () -> {
                // Background task
                totalOrders = orderDao.getOrderHistoryCount(currentUser.getId());
                totalPages = (int) Math.ceil((double) totalOrders / ORDERS_PER_PAGE);
                if (totalPages == 0) totalPages = 1;
                
                // Ensure current page is within bounds
                if (currentPage > totalPages) currentPage = totalPages;
                if (currentPage < 1) currentPage = 1;
                
                // Load paginated order history
                int offset = (currentPage - 1) * ORDERS_PER_PAGE;
                List<OrderInfo> orderHistory = orderDao.getOrderHistoryByUserId(currentUser.getId(), offset, ORDERS_PER_PAGE);
                ObservableList<OrderInfo> orderHistoryObservable = FXCollections.observableArrayList(orderHistory);
                
                // Update UI on JavaFX thread
                ThreadSafeUtils.runOnFXThread(() -> {
                    orderHistoryList.setItems(orderHistoryObservable);
                    updatePaginationUI();
                    orderHistoryEmpty.setVisible(orderHistory.isEmpty());
                });
            },
            () -> {
                // UI update completed
            },
            exception -> {
                showAlert("Error", "Failed to load order history: " + exception.getMessage());
            }
        );
    }

    private void updatePaginationUI() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText(String.format("Page %d of %d (%d orders)", currentPage, totalPages, totalOrders));
        }
        
        if (prevPageButton != null) {
            prevPageButton.setDisable(currentPage <= 1);
        }
        
        if (nextPageButton != null) {
            nextPageButton.setDisable(currentPage >= totalPages);
        }
        
        // Update page number buttons
        if (pageNumbersContainer != null) {
            pageNumbersContainer.getChildren().clear();
            
            // Show up to 5 page numbers around current page
            int startPage = Math.max(1, currentPage - 2);
            int endPage = Math.min(totalPages, currentPage + 2);
            
            for (int i = startPage; i <= endPage; i++) {
                Button pageButton = new Button(String.valueOf(i));
                pageButton.setStyle(i == currentPage ? 
                    "-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8;" :
                    "-fx-background-color: #e9ecef; -fx-text-fill: #6c757d; -fx-background-radius: 4; -fx-padding: 4 8;");
                pageButton.setCursor(javafx.scene.Cursor.HAND);
                
                final int pageNum = i;
                pageButton.setOnAction(e -> goToPage(pageNum));
                
                pageNumbersContainer.getChildren().add(pageButton);
            }
        }
        
        // Show/hide pagination container
        if (paginationContainer != null) {
            paginationContainer.setVisible(totalPages > 1);
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadOrderHistoryPage();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadOrderHistoryPage();
        }
    }

    private void goToPage(int page) {
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
            loadOrderHistoryPage();
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
        if (currentOrdersTab != null) {
            currentOrdersTab.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8 0 0 8; -fx-padding: 12 24; -fx-cursor: hand;");
        }
        if (orderHistoryTab != null) {
            orderHistoryTab.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 0 8 8 0; -fx-padding: 12 24; -fx-cursor: hand;");
        }

        if (currentOrdersSection != null) currentOrdersSection.setVisible(true);
        if (orderHistorySection != null) orderHistorySection.setVisible(false);
        if (paginationContainer != null) paginationContainer.setVisible(false);

        // Move current orders section to top
        if (contentContainer != null && currentOrdersSection != null && orderHistorySection != null) {
            // Store references before removing
            VBox currentOrders = currentOrdersSection;
            VBox orderHistory = orderHistorySection;

            contentContainer.getChildren().remove(currentOrdersSection);
            contentContainer.getChildren().remove(orderHistorySection);

            // Add current orders first (at top)
            contentContainer.getChildren().add(0, currentOrders);
            // Add order history second (below)
            contentContainer.getChildren().add(1, orderHistory);
        }
    }

    private void showOrderHistoryTab() {
        if (currentOrdersTab != null) {
            currentOrdersTab.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8 0 0 8; -fx-padding: 12 24; -fx-cursor: hand;");
        }
        if (orderHistoryTab != null) {
            orderHistoryTab.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 0 8 8 0; -fx-padding: 12 24; -fx-cursor: hand;");
        }
        
        if (currentOrdersSection != null) currentOrdersSection.setVisible(false);
        if (orderHistorySection != null) orderHistorySection.setVisible(true);
        if (paginationContainer != null) paginationContainer.setVisible(totalPages > 1);

        // Move order history section to top
        if (contentContainer != null && currentOrdersSection != null && orderHistorySection != null) {
            // Store references before removing
            VBox currentOrders = currentOrdersSection;
            VBox orderHistory = orderHistorySection;

            contentContainer.getChildren().remove(currentOrdersSection);
            contentContainer.getChildren().remove(orderHistorySection);

            // Add order history first (at top)
            contentContainer.getChildren().add(0, orderHistory);
            // Add current orders second (below)
            contentContainer.getChildren().add(1, currentOrders);
        }
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

            // Food items section at the top
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                VBox foodItemsSection = new VBox(6);
                foodItemsSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 12;");

                Label foodTitle = new Label("Items:");
                foodTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");
                foodItemsSection.getChildren().add(foodTitle);

                VBox foodItemsList = new VBox(4);
                for (OrderInfo.OrderItemInfo item : order.getItems()) {
                    HBox foodItem = new HBox();
                    foodItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    foodItem.setSpacing(8);

                    Label itemName = new Label(item.itemName);
                    itemName.setStyle("-fx-font-size: 12px; -fx-text-fill: #2d3436;");
                    itemName.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(itemName, Priority.ALWAYS);

                    Label itemQty = new Label("×" + item.quantity);
                    itemQty.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");

                    foodItem.getChildren().addAll(itemName, itemQty);
                    foodItemsList.getChildren().add(foodItem);
                }
                foodItemsSection.getChildren().add(foodItemsList);
                card.getChildren().add(foodItemsSection);
            }

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

            Label totalLabel = new Label("৳" + String.format("%.2f", order.getTotalPrice()));
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
