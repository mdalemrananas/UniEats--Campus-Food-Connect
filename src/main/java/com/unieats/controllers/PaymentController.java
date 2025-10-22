package com.unieats.controllers;

import com.unieats.User;
import com.unieats.CartItemView;
import com.unieats.Shop;
import com.unieats.dao.CartDao;
import com.unieats.dao.OrderDao;
import com.unieats.dao.PaymentDao;
import com.unieats.dao.RewardDao;
import com.unieats.dao.FoodItemDao;
import com.unieats.services.StockUpdateService;
import com.unieats.services.RealTimeStockBroadcaster;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

public class PaymentController {

    @FXML private Button backButton;
    @FXML private Label orderIdLabel;
    @FXML private Label totalAmountLabel;
    @FXML private RadioButton cardPayment;
    @FXML private RadioButton cashPayment;
    @FXML private RadioButton walletPayment;
    @FXML private RadioButton pointsPayment;
    @FXML private VBox cardPaymentDetails;
    @FXML private VBox walletPaymentDetails;
    @FXML private VBox cashPaymentDetails;
    @FXML private VBox pointsPaymentDetails;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private TextField cardholderNameField;
    @FXML private ComboBox<String> walletTypeCombo;
    @FXML private TextField walletIdField;
    @FXML private Label pointsInfoLabel;
    @FXML private Label userPointsLabel;
    @FXML private Label pointsNeededLabel;
    @FXML private Button processPaymentButton;
    
    // Bottom navigation
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;

    private final OrderDao orderDao = new OrderDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final RewardDao rewardDao = new RewardDao();
    private final CartDao cartDao = new CartDao();
    private final FoodItemDao foodItemDao = new FoodItemDao();
    private int orderId;
    private double totalAmount;
    private User currentUser;
    
    // Cart data for order creation
    private int currentUserId;
    private java.util.List<CartItemView> cartItems;
    private Shop currentShop;

    @FXML
    private void initialize() {
        // Set up radio button group
        ToggleGroup paymentGroup = new ToggleGroup();
        cardPayment.setToggleGroup(paymentGroup);
        cashPayment.setToggleGroup(paymentGroup);
        walletPayment.setToggleGroup(paymentGroup);
        pointsPayment.setToggleGroup(paymentGroup);
        cardPayment.setSelected(true);
        
        // Wire bottom navigation
        wireBottomNavigation();

        // Set up payment method change listeners
        cardPayment.setOnAction(e -> updatePaymentDetailsVisibility());
        cashPayment.setOnAction(e -> updatePaymentDetailsVisibility());
        walletPayment.setOnAction(e -> updatePaymentDetailsVisibility());
        pointsPayment.setOnAction(e -> {
            updatePaymentDetailsVisibility();
            updatePointsInformation();
        });

        // Populate wallet type ComboBox
        walletTypeCombo.getItems().addAll("bKash", "Nagad", "Rocket");

        // Initialize visibility
        updatePaymentDetailsVisibility();
    }

    private void updatePaymentDetailsVisibility() {
        boolean isCard = cardPayment.isSelected();
        boolean isWallet = walletPayment.isSelected();
        boolean isCash = cashPayment.isSelected();
        boolean isPoints = pointsPayment.isSelected();

        // Fully replace the card input area with wallet input when digital wallet is selected
        if (isWallet) {
            cardPaymentDetails.setVisible(false);
            cardPaymentDetails.setManaged(false);
            walletPaymentDetails.setVisible(true);
            walletPaymentDetails.setManaged(true);
            cashPaymentDetails.setVisible(false);
            cashPaymentDetails.setManaged(false);
            pointsPaymentDetails.setVisible(false);
            pointsPaymentDetails.setManaged(false);
        } else if (isCard) {
            cardPaymentDetails.setVisible(true);
            cardPaymentDetails.setManaged(true);
            walletPaymentDetails.setVisible(false);
            walletPaymentDetails.setManaged(false);
            cashPaymentDetails.setVisible(false);
            cashPaymentDetails.setManaged(false);
            pointsPaymentDetails.setVisible(false);
            pointsPaymentDetails.setManaged(false);
        } else if (isCash) {
            cardPaymentDetails.setVisible(false);
            cardPaymentDetails.setManaged(false);
            walletPaymentDetails.setVisible(false);
            walletPaymentDetails.setManaged(false);
            cashPaymentDetails.setVisible(true);
            cashPaymentDetails.setManaged(true);
            pointsPaymentDetails.setVisible(false);
            pointsPaymentDetails.setManaged(false);
        } else if (isPoints) {
            cardPaymentDetails.setVisible(false);
            cardPaymentDetails.setManaged(false);
            walletPaymentDetails.setVisible(false);
            walletPaymentDetails.setManaged(false);
            cashPaymentDetails.setVisible(false);
            cashPaymentDetails.setManaged(false);
            pointsPaymentDetails.setVisible(true);
            pointsPaymentDetails.setManaged(true);
        }
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        orderIdLabel.setText("#" + orderId);
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        totalAmountLabel.setText(String.format("৳%.2f", totalAmount));
        updatePointsInformation(); // Update points info if points payment is selected
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setCartData(int userId, java.util.List<CartItemView> cartItems, Shop shop, double totalAmount) {
        this.currentUserId = userId;
        this.cartItems = cartItems;
        this.currentShop = shop;
        this.totalAmount = totalAmount;
        this.orderId = -1; // Order not created yet
        orderIdLabel.setText("Pending");
        totalAmountLabel.setText(String.format("৳%.2f", totalAmount));
        updatePointsInformation(); // Update points info if points payment is selected
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/checkout.fxml"));
            Parent root = loader.load();
            CheckoutController controller = loader.getController();
            if (controller != null) controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Checkout");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void handleProcessPayment() {
        if (!validatePaymentForm()) {
            return;
        }

        try {
            // Determine payment method
            String paymentMethod = getSelectedPaymentMethod();
            String transactionId = generateTransactionId();

            System.out.println("Processing payment with method: " + paymentMethod + ", amount: " + totalAmount);

            boolean paymentSuccess = false;
            if ("points".equals(paymentMethod)) {
                // Handle points payment
                paymentSuccess = processPointsPayment();
            } else {
                // Simulate payment processing for other methods
                paymentSuccess = simulatePaymentProcessing(paymentMethod);
            }
            System.out.println("Payment processing result: " + paymentSuccess);

            if (paymentSuccess) {
                // Create order only after successful payment
                int orderId = orderDao.createOrder(currentUserId, currentShop.getId(), totalAmount, "preparing");
                System.out.println("Order created with ID: " + orderId);

                // Add order items and update stock
                for (CartItemView item : cartItems) {
                    System.out.println("Processing item: " + item.itemId + ", quantity: " + item.quantity);
                    orderDao.addOrderItem(orderId, item.itemId, item.quantity, item.price);
                    
                    // Get current stock before updating
                    com.unieats.FoodItem currentItem = foodItemDao.getById(item.itemId);
                    int oldStock = currentItem != null ? currentItem.getStock() : 0;
                    
                    // Update stock using the real-time service
                    try {
                        StockUpdateService.getInstance().updateStock(item.itemId, item.quantity);
                        
                        // Also notify the real-time broadcaster for immediate updates
                        int newStock = oldStock - item.quantity;
                        RealTimeStockBroadcaster.getInstance().notifyStockChange(item.itemId, oldStock, newStock);
                        
                        System.out.println("Real-time stock update: Item " + item.itemId + " stock changed from " + oldStock + " to " + newStock);
                    } catch (Exception e) {
                        System.err.println("Failed to update stock for item " + item.itemId + ": " + e.getMessage());
                        // Continue with other items even if one fails
                    }
                }
                System.out.println("Order items added and stock updated via real-time service with immediate broadcasting");

                // Create payment record
                int paymentId = paymentDao.createPayment(orderId, paymentMethod, totalAmount, transactionId);
                System.out.println("Payment created with ID: " + paymentId);

                // Update payment status
                paymentDao.updatePaymentStatus(paymentId, "completed");
                System.out.println("Payment status updated to completed");

                // Broadcast order update to sellers in real-time via WebSocket hub (port 7071)
                try {
                    String orderUpdateJson = String.format(
                        "{\"type\":\"order_update\",\"orderId\":%d,\"shopId\":%d,\"status\":\"%s\"}",
                        orderId,
                        currentShop != null ? currentShop.getId() : -1,
                        "preparing"
                    );
                    com.unieats.util.SocketBus.broadcast(orderUpdateJson);
                    System.out.println("Broadcasted order_update: " + orderUpdateJson);
                    // Also broadcast a generic payments topic for admin dashboard
                    try {
                        String paymentsTopicJson = String.format(
                            "{\"type\":\"topic\",\"topic\":\"payments\",\"orderId\":%d,\"paymentId\":%d,\"status\":\"completed\"}",
                            orderId, paymentId
                        );
                        com.unieats.util.SocketBus.broadcast(paymentsTopicJson);
                    } catch (Exception ignored) {}
                } catch (Exception ex) {
                    System.err.println("Failed to broadcast order_update: " + ex.getMessage());
                }

                // Clear cart after successful payment
                cartDao.clearCart(currentUserId);
                System.out.println("Cart cleared for user: " + currentUserId);

                // Award reward points
                awardRewardPoints(orderId);

                // Navigate to order confirmation
                navigateToOrderConfirmation(orderId);
            } else {
                System.out.println("Payment failed");
                showAlert("Payment Failed", "Payment could not be processed. Please try again or use a different payment method.");
            }

        } catch (Exception e) {
            System.err.println("Payment error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Payment Error", "Failed to process payment: " + e.getMessage());
        }
    }

    private String getSelectedPaymentMethod() {
        if (cardPayment.isSelected()) return "card";
        if (walletPayment.isSelected()) {
            String walletType = walletTypeCombo.getValue();
            if (walletType != null) {
                return "digital_wallet_" + walletType.toLowerCase().replace(" ", "_");
            }
            return "digital_wallet";
        }
        if (cashPayment.isSelected()) return "cash";
        if (pointsPayment.isSelected()) return "points";
        return "card";
    }

    private String generateTransactionId() {
        Random random = new Random();
        return "TXN" + System.currentTimeMillis() + random.nextInt(1000);
    }

    private void updatePointsInformation() {
        if (pointsPayment.isSelected() && currentUser != null && currentShop != null) {
            try {
                // Get user's points for this shop
                double userPoints = rewardDao.getUserRewardPoints(currentUserId, currentShop.getId());
                userPointsLabel.setText(String.format("%.0f", userPoints));
                
                // Points needed is the total amount
                pointsNeededLabel.setText(String.format("%.0f", totalAmount));
            } catch (Exception e) {
                System.err.println("Error updating points information: " + e.getMessage());
                userPointsLabel.setText("Error");
                pointsNeededLabel.setText("Error");
            }
        }
    }

    private boolean processPointsPayment() {
        try {
            // Get user's points for this shop
            double userPoints = rewardDao.getUserRewardPoints(currentUserId, currentShop.getId());
            
            if (userPoints >= totalAmount) {
                // Deduct points
                boolean deducted = rewardDao.redeemRewardPoints(currentUserId, currentShop.getId(), totalAmount);
                if (deducted) {
                    System.out.println("Deducted " + totalAmount + " points from user " + currentUserId + " for shop " + currentShop.getId());
                    return true;
                } else {
                    showAlert("Payment Error", "Failed to deduct points. Please try again.");
                    return false;
                }
            } else {
                showAlert("Insufficient Points", "You don't have enough points. You have " + userPoints + " points, but need " + totalAmount + " points for this order.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error processing points payment: " + e.getMessage());
            showAlert("Payment Error", "Failed to process points payment: " + e.getMessage());
            return false;
        }
    }

    private boolean simulatePaymentProcessing(String paymentMethod) {
        // Simulate payment processing with 90% success rate
        Random random = new Random();
        return random.nextDouble() < 0.9;
    }

    private boolean validatePaymentForm() {
        if (cardPayment.isSelected()) {
            if (cardNumberField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter your card number.");
                return false;
            }
            if (expiryField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter expiry date.");
                return false;
            }
            if (cvvField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter CVV.");
                return false;
            }
            if (cardholderNameField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter cardholder name.");
                return false;
            }
        } else if (walletPayment.isSelected()) {
            if (walletTypeCombo.getValue() == null) {
                showAlert("Validation Error", "Please select wallet type.");
                return false;
            }
            if (walletIdField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter wallet ID or email.");
                return false;
            }
        }
        // Cash on delivery doesn't need validation
        return true;
    }

    private void navigateToOrderConfirmation(int orderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_confirmation.fxml"));
            Parent root = loader.load();
            OrderConfirmationController controller = loader.getController();
            if (controller != null) {
                controller.setOrderId(orderId);
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) processPaymentButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Order Confirmation");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to order confirmation: " + e.getMessage());
        }
    }

    private void awardRewardPoints(int orderId) {
        try {
            // Get order details to find user and shop
            com.unieats.OrderInfo orderInfo = orderDao.getOrderById(orderId);
            if (orderInfo == null) {
                System.err.println("Order not found for reward points: " + orderId);
                return;
            }

            // Calculate reward points based on order items and their multipliers
            double rewardPoints = rewardDao.calculateRewardPoints(orderId);
            
            if (rewardPoints > 0) {
                // Award points to the user for this shop
                rewardDao.awardRewardPoints(orderInfo.getUserId(), orderInfo.getShopId(), rewardPoints);
                System.out.println("Awarded " + rewardPoints + " reward points to user " + orderInfo.getUserId() + " for shop " + orderInfo.getShopId());
            } else {
                System.out.println("No reward points to award for order " + orderId);
            }
            
        } catch (Exception e) {
            System.err.println("Error awarding reward points for order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - reward points failure shouldn't break payment flow
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void wireBottomNavigation() {
        if (navHome != null) {
            navHome.setOnMouseClicked(e -> navigateToHome());
        }
        if (navOrders != null) {
            navOrders.setOnMouseClicked(e -> navigateToOrders());
        }
        if (navCart != null) {
            navCart.setOnMouseClicked(e -> navigateToCart());
        }
        if (navFav != null) {
            navFav.setOnMouseClicked(e -> navigateToFavorites());
        }
        if (navProfile != null) {
            navProfile.setOnMouseClicked(e -> navigateToProfile());
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();
            MenuController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            Stage stage = (Stage) navHome.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Menu");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to menu: " + e.getMessage());
        }
    }

    private void navigateToOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_orders.fxml"));
            Parent root = loader.load();
            MyOrdersController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            Stage stage = (Stage) navOrders.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - My Orders");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to orders: " + e.getMessage());
        }
    }

    private void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Parent root = loader.load();
            CartController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUserId(currentUser.getId());
            }
            Stage stage = (Stage) navCart.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Cart");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to cart: " + e.getMessage());
        }
    }

    private void navigateToFavorites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/wishlist.fxml"));
            Parent root = loader.load();
            WishlistController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            Stage stage = (Stage) navFav.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Favorites");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to favorites: " + e.getMessage());
        }
    }

    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            Stage stage = (Stage) navProfile.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Profile");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate to profile: " + e.getMessage());
        }
    }
}
