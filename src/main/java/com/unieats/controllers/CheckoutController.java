package com.unieats.controllers;

import com.unieats.CartItemView;
import com.unieats.User;
import com.unieats.dao.CartQueryDao;
import com.unieats.dao.OrderDao;
import com.unieats.dao.ShopDao;
import com.unieats.Shop;
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

import java.util.List;

public class CheckoutController {

    @FXML
    private Button backButton;
    @FXML
    private ListView<CartItemView> orderItemsList;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea addressField;
    @FXML
    private RadioButton cardPayment;
    @FXML
    private RadioButton cashPayment;
    @FXML
    private RadioButton walletPayment;
    @FXML
    private TextArea instructionsField;
    @FXML
    private Button proceedToPaymentButton;

    private final CartQueryDao cartQueryDao = new CartQueryDao();
    private final OrderDao orderDao = new OrderDao();
    private final ShopDao shopDao = new ShopDao();
    private int currentUserId;
    private User currentUser;
    private List<CartItemView> cartItems;
    private Shop currentShop;

    @FXML
    private void initialize() {
        // Set up radio button group
        ToggleGroup paymentGroup = new ToggleGroup();
        cardPayment.setToggleGroup(paymentGroup);
        cashPayment.setToggleGroup(paymentGroup);
        walletPayment.setToggleGroup(paymentGroup);
        cardPayment.setSelected(true);

        // Set up order items list
        setupOrderItemsList();
    }

    private void setupOrderItemsList() {
        orderItemsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CartItemView item, boolean empty) {
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

                Label nameLabel = new Label(item.name);
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label priceLabel = new Label(String.format("$%.2f", item.price * item.quantity));
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.currentUserId = user.getId();
        loadCartData();
        populateUserInfo();
    }

    private void loadCartData() {
        cartItems = cartQueryDao.listCartItems(currentUserId);
        if (cartItems.isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty. Please add items before checkout.");
            return;
        }

        // Get shop information
        int shopId = cartItems.get(0).shopId;
        currentShop = shopDao.getShopById(shopId);

        // Set up order items
        ObservableList<CartItemView> items = FXCollections.observableArrayList(cartItems);
        orderItemsList.setItems(items);

        // Calculate totals
        double subtotal = cartItems.stream().mapToDouble(i -> i.price * i.quantity).sum();
        double tax = Math.round(subtotal * 0.02 * 100.0) / 100.0;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void populateUserInfo() {
        if (currentUser != null) {
            nameField.setText(currentUser.getFullName());
            // You could add phone and address fields to User model if needed
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Parent root = loader.load();
            CartController controller = loader.getController();
            if (controller != null)
                controller.setCurrentUserId(currentUserId);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Cart");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void handleProceedToPayment() {
        if (!validateForm()) {
            return;
        }

        try {
            // Create order
            double subtotal = cartItems.stream().mapToDouble(i -> i.price * i.quantity).sum();
            double tax = Math.round(subtotal * 0.02 * 100.0) / 100.0;
            double total = subtotal + tax;

            int orderId = orderDao.createOrder(currentUserId, currentShop.getId(), total, "pending");

            // Add order items
            for (CartItemView item : cartItems) {
                orderDao.addOrderItem(orderId, item.itemId, item.quantity, item.price);
            }

            // Broadcast inventory update via WebSocket
            try {
                StringBuilder payload = new StringBuilder();
                payload.append('{')
                        .append("\"type\":\"inventory_update\",")
                        .append("\"shopId\":" + currentShop.getId() + ",")
                        .append("\"items\":[");
                for (int i = 0; i < cartItems.size(); i++) {
                    CartItemView it = cartItems.get(i);
                    payload.append('{')
                            .append("\"itemId\":" + it.itemId + ",")
                            .append("\"delta\":" + (-it.quantity))
                            .append('}');
                    if (i < cartItems.size() - 1)
                        payload.append(',');
                }
                payload.append("]}");
                com.unieats.util.SocketBus.broadcast(payload.toString());
            } catch (Exception ignored) {
            }

            // Notify order management to refresh
            try {
                String orderMsg = '{' +
                        "\"type\":\"order_update\"," +
                        "\"shopId\":" + currentShop.getId() + ',' +
                        "\"orderId\":" + orderId +
                        '}';
                com.unieats.util.SocketBus.broadcast(orderMsg);
            } catch (Exception ignored) {
            }

            // Navigate to payment page
            navigateToPayment(orderId, total);

        } catch (Exception e) {
            showAlert("Order Error", "Failed to create order: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your name.");
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your phone number.");
            return false;
        }
        if (addressField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your delivery address.");
            return false;
        }
        return true;
    }

    private void navigateToPayment(int orderId, double totalAmount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Parent root = loader.load();
            PaymentController controller = loader.getController();
            if (controller != null) {
                controller.setOrderId(orderId);
                controller.setTotalAmount(totalAmount);
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) proceedToPaymentButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Payment");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to payment: " + e.getMessage());
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
