package com.unieats.controllers;

import com.unieats.DatabaseManager;
import com.unieats.User;
import com.unieats.OrderRequest;
import com.unieats.OrderItem;
import com.unieats.dao.OrderRequestDao;
import com.unieats.dao.ShopDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;

public class StallController {
    @FXML
    private VBox root;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userCategoryLabel;

    // Header + main card
    @FXML
    private Label totalMenusLabel;
    @FXML
    private Label progressPercentLabel;
    @FXML
    private ProgressBar menuProgress;

    // Order Summary
    @FXML
    private BarChart<String, Number> ordersChart;
    @FXML
    private CategoryAxis ordersXAxis;
    @FXML
    private NumberAxis ordersYAxis;
    @FXML
    private ToggleButton ordersMonthlyBtn;
    @FXML
    private ToggleButton ordersWeeklyBtn;
    @FXML
    private ToggleButton ordersTodayBtn;

    // Revenue
    @FXML
    private BarChart<String, Number> revenueChart;
    @FXML
    private CategoryAxis revenueXAxis;
    @FXML
    private NumberAxis revenueYAxis;
    @FXML
    private ToggleButton revenueMonthlyBtn;
    @FXML
    private ToggleButton revenueWeeklyBtn;
    @FXML
    private ToggleButton revenueTodayBtn;

    // Order notification components
    @FXML
    private StackPane orderNotificationContainer;
    @FXML
    private Button orderNotificationBtn;
    @FXML
    private Label orderCountBadge;

    private final Random rng = new Random();
    private OrderRequestDao orderRequestDao;
    private ShopDao shopDao;
    private int currentShopId;

    private String userName = "Guest";

    public void setUserName(String name) {
        this.userName = name != null ? name : "Guest";
        if (userNameLabel != null) {
            userNameLabel.setText(userName);
        }
    }

    @FXML
    private void initialize() {
        // Initialize DAOs
        orderRequestDao = new OrderRequestDao();
        shopDao = new ShopDao();

        // Get current user from DatabaseManager
        User currentUser = DatabaseManager.getCurrentUser();

        // Set user name if available, otherwise use default
        if (currentUser != null) {
            // Set user name
            if (userNameLabel != null) {
                if (currentUser.getFullName() != null && !currentUser.getFullName().trim().isEmpty()) {
                    userNameLabel.setText(currentUser.getFullName());
                } else {
                    userNameLabel.setText(userName);
                }
            }

            // Set user category
            if (userCategoryLabel != null && currentUser.getUserCategory() != null) {
                String category = currentUser.getUserCategory();
                // Capitalize first letter of category
                category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
                userCategoryLabel.setText(category);
            }

            // Get shop ID for order notifications
            if ("seller".equalsIgnoreCase(currentUser.getUserCategory())) {
                com.unieats.Shop shop = shopDao.getShopByOwnerId(currentUser.getId());
                if (shop != null) {
                    currentShopId = shop.getId();
                    updateOrderNotificationBadge();
                }
            }
        }

        // Load stylesheet programmatically
        try {
            String css = getClass().getResource("/css/dashboard.css").toExternalForm();
            root.getStylesheets().add(css);

            // Load notification styles
            String notificationCss = getClass().getResource("/css/notification.css").toExternalForm();
            root.getStylesheets().add(notificationCss);
        } catch (Exception e) {
            System.err.println("Warning: Could not load stylesheet: " + e.getMessage());
        }
        // Group segmented toggles
        ToggleGroup ordersGroup = new ToggleGroup();
        ordersMonthlyBtn.setToggleGroup(ordersGroup);
        ordersWeeklyBtn.setToggleGroup(ordersGroup);
        ordersTodayBtn.setToggleGroup(ordersGroup);

        ToggleGroup revenueGroup = new ToggleGroup();
        revenueMonthlyBtn.setToggleGroup(revenueGroup);
        revenueWeeklyBtn.setToggleGroup(revenueGroup);
        revenueTodayBtn.setToggleGroup(revenueGroup);

        // Chart axes labels
        ordersYAxis.setLabel("Orders");
        revenueYAxis.setLabel("Revenue (k)");

        // Load initial data asynchronously to keep UI responsive
        loadOrdersAsync("Monthly");
        loadRevenueAsync("Monthly");

        // Listeners for period selection
        ordersMonthlyBtn.setOnAction(e -> loadOrdersAsync("Monthly"));
        ordersWeeklyBtn.setOnAction(e -> loadOrdersAsync("Weekly"));
        ordersTodayBtn.setOnAction(e -> loadOrdersAsync("Today"));

        revenueMonthlyBtn.setOnAction(e -> loadRevenueAsync("Monthly"));
        revenueWeeklyBtn.setOnAction(e -> loadRevenueAsync("Weekly"));
        revenueTodayBtn.setOnAction(e -> loadRevenueAsync("Today"));

        // Animate progress to 75%
        animateProgress(0.75);

        // Add some sample order requests for testing (remove in production)
        addSampleOrderRequests();
    }

    private void addSampleOrderRequests() {
        if (currentShopId == 0)
            return;

        try {
            // Check if there are already pending orders
            int existingCount = orderRequestDao.getPendingOrderCount(currentShopId);
            if (existingCount > 0)
                return; // Don't add samples if orders already exist

            // Create sample order requests
            List<OrderItem> items1 = new ArrayList<>();
            items1.add(new OrderItem(1, "Chicken Burger", 2, 250.0));
            items1.add(new OrderItem(2, "French Fries", 1, 80.0));

            OrderRequest order1 = new OrderRequest(1, "John Doe", currentShopId, items1, 580.0);
            orderRequestDao.createOrderRequest(order1);

            List<OrderItem> items2 = new ArrayList<>();
            items2.add(new OrderItem(3, "Pizza Margherita", 1, 400.0));
            items2.add(new OrderItem(4, "Coca Cola", 2, 50.0));

            OrderRequest order2 = new OrderRequest(2, "Jane Smith", currentShopId, items2, 500.0);
            orderRequestDao.createOrderRequest(order2);

            // Update notification badge
            updateOrderNotificationBadge();

        } catch (Exception e) {
            System.err.println("Error adding sample order requests: " + e.getMessage());
        }
    }

    private void animateProgress(double target) {
        menuProgress.setProgress(0);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(menuProgress.progressProperty(), 0)),
                new KeyFrame(Duration.millis(1200), new KeyValue(menuProgress.progressProperty(), target)));
        tl.currentTimeProperty().addListener((obs, oldT, newT) -> {
            double pct = menuProgress.getProgress();
            progressPercentLabel.setText((int) Math.round(pct * 100) + "%");
        });
        tl.play();
        totalMenusLabel.setText("180");
    }

    // Simulated async loads (replace with networking calls to your backend)
    private void loadOrdersAsync(String period) {
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override
            protected XYChart.Series<String, Number> call() {
                List<String> labels = xLabels(period);
                return series("Orders", labels, 20, 120);
            }
        };
        task.setOnSucceeded(ev -> {
            ordersChart.getData().setAll(task.getValue());
            ordersXAxis.setCategories(javafx.collections.FXCollections.observableArrayList(xLabels(period)));
        });
        new Thread(task, "orders-loader").start();
    }

    private void loadRevenueAsync(String period) {
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override
            protected XYChart.Series<String, Number> call() {
                List<String> labels = xLabels(period);
                return series("Revenue", labels, 10, 90);
            }
        };
        task.setOnSucceeded(ev -> {
            revenueChart.getData().setAll(task.getValue());
            revenueXAxis.setCategories(javafx.collections.FXCollections.observableArrayList(xLabels(period)));
        });
        new Thread(task, "revenue-loader").start();
    }

    private XYChart.Series<String, Number> series(String name, List<String> labels, int min, int max) {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(name);
        for (String l : labels) {
            int v = rng.nextInt(max - min + 1) + min;
            s.getData().add(new XYChart.Data<>(l, v));
        }
        return s;
    }

    private List<String> xLabels(String period) {
        List<String> list = new ArrayList<>();
        switch (period) {
            case "Today":
                list.add("9am");
                list.add("12pm");
                list.add("3pm");
                list.add("6pm");
                break;
            case "Weekly":
                list.add("Mon");
                list.add("Tue");
                list.add("Wed");
                list.add("Thu");
                list.add("Fri");
                break;
            default:
                list.add("Jun24");
                list.add("June25");
                list.add("June26");
                list.add("June27");
        }
        return list;
    }

    @FXML
    private void handleMenu() {
        info("Menu coming soon");
    }

    @FXML
    private void handleBack() {
        info("Back to previous screen coming soon");
    }

    @FXML
    private void handlePostItem() {
        openFoodPostWindow();
    }

    @FXML
    private void handleManageOrders() {
        openOrderManagementWindow();
    }

    @FXML
    private void handleInventory() {
        openInventoryManagementWindow();
    }

    @FXML
    private void handleFoodReview() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FoodReview.fxml"));
            Parent reviewsRoot = loader.load();
            Stage stage = this.root != null && this.root.getScene() != null
                    ? (Stage) this.root.getScene().getWindow()
                    : new Stage();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(reviewsRoot, 360, 720);
            stage.setTitle("Food Reviews");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Food Reviews");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleProfile() {
        // Get current user from DatabaseManager
        User currentUser = DatabaseManager.getCurrentUser();

        if (currentUser == null) {
            showAlert("Error", "No user logged in. Please sign in first.");
            return;
        }

        // Check if user is a seller
        if ("seller".equalsIgnoreCase(currentUser.getUserCategory())) {
            showSellerProfileDialog(currentUser);
        } else {
            // For regular users, show a simple profile info
            showUserProfileDialog(currentUser);
        }
    }

    @FXML
    private void handleOrderRequests() {
        if (currentShopId == 0) {
            showAlert("Error", "No shop found for this seller account.");
            return;
        }

        showOrderRequestsDialog();
    }

    private void updateOrderNotificationBadge() {
        if (currentShopId == 0)
            return;

        try {
            int pendingCount = orderRequestDao.getPendingOrderCount(currentShopId);

            Platform.runLater(() -> {
                if (orderCountBadge != null) {
                    if (pendingCount > 0) {
                        orderCountBadge.setText(String.valueOf(pendingCount));
                        orderCountBadge.setVisible(true);
                        if (orderNotificationBtn != null) {
                            orderNotificationBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        }
                    } else {
                        orderCountBadge.setVisible(false);
                        if (orderNotificationBtn != null) {
                            orderNotificationBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error updating order notification badge: " + e.getMessage());
        }
    }

    private void showOrderRequestsDialog() {
        try {
            List<OrderRequest> pendingOrders = orderRequestDao.getPendingOrdersByShopId(currentShopId);

            if (pendingOrders.isEmpty()) {
                showAlert("No Orders", "No pending order requests at this time.");
                return;
            }

            // Create order requests dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Pending Order Requests");
            dialog.setHeaderText("Incoming Food Order Requests");

            // Create scrollable content
            ScrollPane scrollPane = new ScrollPane();
            VBox ordersContainer = new VBox(10);
            ordersContainer.setPadding(new Insets(10));

            for (OrderRequest order : pendingOrders) {
                VBox orderCard = createOrderCard(order);
                ordersContainer.getChildren().add(orderCard);
            }

            scrollPane.setContent(ordersContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().setPrefWidth(500);

            // Add close button
            ButtonType closeButtonType = new ButtonType("Close", ButtonType.CANCEL.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);

            dialog.showAndWait();

            // Refresh notification badge after dialog closes
            updateOrderNotificationBadge();

        } catch (Exception e) {
            System.err.println("Error showing order requests dialog: " + e.getMessage());
            showAlert("Error", "Failed to load order requests. Please try again.");
        }
    }

    private VBox createOrderCard(OrderRequest order) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");

        // Order header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Text orderIdText = new Text("Order #" + order.getId());
        orderIdText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text timeText = new Text(order.getOrderTime().toString().substring(11, 16)); // HH:MM format
        timeText.setStyle("-fx-fill: #6c757d; -fx-font-size: 12;");

        header.getChildren().addAll(orderIdText, new Text("  •  "), timeText);

        // Customer info
        Text customerText = new Text("Customer: " + order.getCustomerName());
        customerText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Order items
        VBox itemsContainer = new VBox(4);
        for (OrderItem item : order.getItems()) {
            HBox itemRow = new HBox();
            itemRow.setAlignment(Pos.CENTER_LEFT);

            Text itemText = new Text(item.getQuantity() + "x " + item.getFoodItemName());
            Text priceText = new Text("৳" + String.format("%.2f", item.getTotalPrice()));
            priceText.setStyle("-fx-fill: #28a745; -fx-font-weight: bold;");

            HBox.setMargin(priceText, new Insets(0, 0, 0, 10));
            itemRow.getChildren().addAll(itemText, priceText);
            itemsContainer.getChildren().add(itemRow);
        }

        // Total price
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_RIGHT);
        Text totalText = new Text("Total: ৳" + String.format("%.2f", order.getTotalPrice()));
        totalText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        totalText.setStyle("-fx-fill: #e74c3c;");
        totalRow.getChildren().add(totalText);

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        Button acceptBtn = new Button("✅ Accept");
        acceptBtn.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4;");
        acceptBtn.setOnAction(e -> handleAcceptOrder(order));

        Button declineBtn = new Button("❌ Decline");
        declineBtn.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4;");
        declineBtn.setOnAction(e -> handleDeclineOrder(order));

        actionButtons.getChildren().addAll(declineBtn, acceptBtn);

        card.getChildren().addAll(header, customerText, itemsContainer, totalRow, actionButtons);

        return card;
    }

    private void handleAcceptOrder(OrderRequest order) {
        try {
            boolean success = orderRequestDao.updateOrderStatus(order.getId(), "accepted");
            if (success) {
                showAlert("Success", "Order #" + order.getId() + " accepted successfully.");
                updateOrderNotificationBadge();
                // Refresh the dialog
                showOrderRequestsDialog();
            } else {
                showAlert("Error", "Failed to accept order. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error accepting order: " + e.getMessage());
            showAlert("Error", "Failed to accept order. Please try again.");
        }
    }

    private void handleDeclineOrder(OrderRequest order) {
        try {
            boolean success = orderRequestDao.updateOrderStatus(order.getId(), "declined");
            if (success) {
                showAlert("Success", "Order #" + order.getId() + " has been declined.");
                updateOrderNotificationBadge();
                // Refresh the dialog
                showOrderRequestsDialog();
            } else {
                showAlert("Error", "Failed to decline order. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error declining order: " + e.getMessage());
            showAlert("Error", "Failed to decline order. Please try again.");
        }
    }

    private void showSellerProfileDialog(User seller) {
        // Create a custom dialog for seller profile
        javafx.scene.control.Dialog<Boolean> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Shop Profile");
        dialog.setHeaderText("Your Shop Information");

        // Create the dialog content
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        // Get shop information
        com.unieats.dao.ShopDao shopDao = new com.unieats.dao.ShopDao();
        com.unieats.Shop shop = shopDao.getShopByOwnerId(seller.getId());

        // Create form fields
        javafx.scene.control.TextField shopNameField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField ownerNameField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField();
        javafx.scene.control.TextArea descriptionField = new javafx.scene.control.TextArea();
        javafx.scene.control.TextField addressField = new javafx.scene.control.TextField();
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label();

        // Populate fields
        if (shop != null) {
            shopNameField.setText(shop.getShopName() != null ? shop.getShopName() : "");
            descriptionField.setText(shop.getDescription() != null ? shop.getDescription() : "");
            addressField.setText(shop.getAddress() != null ? shop.getAddress() : "");
            statusLabel.setText("Status: " + (shop.getStatus() != null ? shop.getStatus() : "Unknown"));
        }

        ownerNameField.setText(seller.getFullName() != null ? seller.getFullName() : "");
        emailField.setText(seller.getEmail() != null ? seller.getEmail() : "");

        // Make some fields read-only
        emailField.setEditable(false);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // Add fields to grid
        grid.add(new javafx.scene.control.Label("Shop Name:"), 0, 0);
        grid.add(shopNameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Owner Name:"), 0, 1);
        grid.add(ownerNameField, 1, 1);
        grid.add(new javafx.scene.control.Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new javafx.scene.control.Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new javafx.scene.control.Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(statusLabel, 0, 5, 2, 1);

        // Set text area properties
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Handle save action
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return saveSellerProfile(seller, shop, shopNameField.getText(), ownerNameField.getText(),
                        addressField.getText(), descriptionField.getText());
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void showUserProfileDialog(User user) {
        // Simple profile dialog for regular users
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("User Profile");
        dialog.setHeaderText("Your Profile Information");

        // Create the dialog content
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        // Create form fields
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField categoryField = new javafx.scene.control.TextField();

        // Populate fields
        nameField.setText(user.getFullName() != null ? user.getFullName() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        categoryField.setText(user.getUserCategory() != null ? user.getUserCategory() : "");

        // Make fields read-only for regular users
        nameField.setEditable(false);
        emailField.setEditable(false);
        categoryField.setEditable(false);

        // Add fields to grid
        grid.add(new javafx.scene.control.Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new javafx.scene.control.Label("User Type:"), 0, 2);
        grid.add(categoryField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Add close button
        javafx.scene.control.ButtonType closeButtonType = new javafx.scene.control.ButtonType("Close",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);

        dialog.showAndWait();
    }

    private boolean saveSellerProfile(User seller, com.unieats.Shop shop,
            String shopName, String ownerName,
            String address, String description) {
        try {
            // Update user information
            seller.setFullName(ownerName);
            DatabaseManager.getInstance().updateUser(seller);

            // Update shop information
            if (shop != null) {
                shop.setShopName(shopName);
                shop.setAddress(address);
                shop.setDescription(description);
                com.unieats.dao.ShopDao shopDao = new com.unieats.dao.ShopDao();
                shopDao.updateShop(shop);
            }

            // Update the display name in the dashboard
            if (userNameLabel != null) {
                userNameLabel.setText(ownerName);
            }

            showAlert("Success", "Profile updated successfully!");
            return true;
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to update profile. Please try again.");
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void info(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }

    /**
     * Open the Food Post window
     */
    private void openFoodPostWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_post.fxml"));
            Parent root = loader.load();

            FoodPostController controller = loader.getController();
            controller.setShopId(currentShopId > 0 ? currentShopId : 1);

            Stage stage = this.root != null && this.root.getScene() != null
                    ? (Stage) this.root.getScene().getWindow()
                    : null;
            if (stage == null)
                stage = new Stage();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setTitle("Add New Food Item");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Error opening food post window: " + e.getMessage());
            showAlert("Error", "Failed to open food post window: " + e.getMessage());
        }
    }

    /**
     * Open the Inventory Management window
     */
    private void openInventoryManagementWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory_management.fxml"));
            Parent root = loader.load();

            InventoryController controller = loader.getController();
            controller.setShopId(currentShopId > 0 ? currentShopId : 1);

            Stage stage = this.root != null && this.root.getScene() != null
                    ? (Stage) this.root.getScene().getWindow()
                    : null;
            if (stage == null)
                stage = new Stage();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setTitle("Inventory Management");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Error opening inventory management window: " + e.getMessage());
            showAlert("Error", "Failed to open inventory management window: " + e.getMessage());
        }
    }

    /**
     * Open the Order Management window
     */
    private void openOrderManagementWindow() {
        try {
            java.net.URL omUrl = getClass().getResource("/fxml/order_management.fxml");
            if (omUrl == null) {
                String msg = "order_management.fxml not found on classpath at /fxml/order_management.fxml";
                System.err.println(msg);
                showAlert("Error", msg);
                return;
            }
            FXMLLoader loader = new FXMLLoader(omUrl);
            Parent root = loader.load();

            OrderManagementController controller = loader.getController();
            controller.setShopId(currentShopId > 0 ? currentShopId : 1);

            Stage stage = this.root != null && this.root.getScene() != null
                    ? (Stage) this.root.getScene().getWindow()
                    : null;
            if (stage == null)
                stage = new Stage();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 720);
            stage.setTitle("Order Management");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Error opening order management window");
            e.printStackTrace();
            showAlert("Error", "Failed to open order management window: "
                    + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
