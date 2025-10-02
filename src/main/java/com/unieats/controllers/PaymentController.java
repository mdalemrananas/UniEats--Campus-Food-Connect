package com.unieats.controllers;

import com.unieats.User;
import com.unieats.dao.OrderDao;
import com.unieats.dao.PaymentDao;
import com.unieats.dao.RewardDao;
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
    @FXML private VBox cardPaymentDetails;
    @FXML private VBox walletPaymentDetails;
    @FXML private VBox cashPaymentDetails;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private TextField cardholderNameField;
    @FXML private ComboBox<String> walletTypeCombo;
    @FXML private TextField walletIdField;
    @FXML private Button processPaymentButton;

    private final OrderDao orderDao = new OrderDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final RewardDao rewardDao = new RewardDao();
    private int orderId;
    private double totalAmount;
    private User currentUser;

    @FXML
    private void initialize() {
        // Set up radio button group
        ToggleGroup paymentGroup = new ToggleGroup();
        cardPayment.setToggleGroup(paymentGroup);
        cashPayment.setToggleGroup(paymentGroup);
        walletPayment.setToggleGroup(paymentGroup);
        cardPayment.setSelected(true);

        // Set up payment method change listeners
        cardPayment.setOnAction(e -> updatePaymentDetailsVisibility());
        cashPayment.setOnAction(e -> updatePaymentDetailsVisibility());
        walletPayment.setOnAction(e -> updatePaymentDetailsVisibility());

        // Populate wallet type ComboBox
        walletTypeCombo.getItems().addAll("bKash", "Nagad", "Google Pay", "PayPal", "Apple Pay", "Venmo");

        // Initialize visibility
        updatePaymentDetailsVisibility();
    }

    private void updatePaymentDetailsVisibility() {
        boolean isCard = cardPayment.isSelected();
        boolean isWallet = walletPayment.isSelected();
        boolean isCash = cashPayment.isSelected();

        cardPaymentDetails.setVisible(isCard);
        walletPaymentDetails.setVisible(isWallet);
        cashPaymentDetails.setVisible(isCash);
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        orderIdLabel.setText("#" + orderId);
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        totalAmountLabel.setText(String.format("$%.2f", totalAmount));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
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

            System.out.println("Creating payment for order " + orderId + " with method: " + paymentMethod + ", amount: " + totalAmount);

            // Create payment record
            int paymentId = paymentDao.createPayment(orderId, paymentMethod, totalAmount, transactionId);
            System.out.println("Payment created with ID: " + paymentId);

            // Simulate payment processing
            boolean paymentSuccess = simulatePaymentProcessing(paymentMethod);
            System.out.println("Payment processing result: " + paymentSuccess);

            if (paymentSuccess) {
                // Update payment status
                paymentDao.updatePaymentStatus(paymentId, "completed");
                System.out.println("Payment status updated to completed");
                
                // Update order status
                orderDao.updateOrderStatus(orderId, "preparing");
                System.out.println("Order status updated to preparing");

                // Award reward points
                awardRewardPoints(orderId);

                // Navigate to order confirmation
                navigateToOrderConfirmation(orderId);
            } else {
                // Update payment status
                paymentDao.updatePaymentStatus(paymentId, "failed");
                System.out.println("Payment status updated to failed");
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
        return "card";
    }

    private String generateTransactionId() {
        Random random = new Random();
        return "TXN" + System.currentTimeMillis() + random.nextInt(1000);
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
}
