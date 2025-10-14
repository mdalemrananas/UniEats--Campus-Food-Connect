package com.unieats.controllers;

import com.unieats.CartItemView;
import com.unieats.User;
import com.unieats.dao.CartQueryDao;
import com.unieats.dao.CartDao;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class CheckoutController {

    @FXML private Button backButton;
    @FXML private ListView<CartItemView> orderItemsList;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextArea instructionsField;
    @FXML private Button proceedToPaymentButton;
    
    // Bottom navigation
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;

    private final CartQueryDao cartQueryDao = new CartQueryDao();
    private final CartDao cartDao = new CartDao();
    private final OrderDao orderDao = new OrderDao();
    private final ShopDao shopDao = new ShopDao();
    private int currentUserId;
    private User currentUser;
    private List<CartItemView> cartItems;
    private Shop currentShop;

    @FXML
    private void initialize() {
        // Set up order items list
        setupOrderItemsList();
        // Wire bottom navigation
        wireBottomNavigation();
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
                itemBox.setStyle("-fx-padding: 12; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

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
                details.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label qtyLabel = new Label("Qty: " + item.quantity);
                qtyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

                Label unitPriceLabel = new Label(String.format("$%.2f each", item.price));
                unitPriceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

                // Quantity controls
                Button minusBtn = new Button();
                minusBtn.setGraphic(new FontIcon("fas-minus"));
                minusBtn.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 6; -fx-padding: 4; -fx-cursor: hand;");
                minusBtn.setOnAction(e -> {
                    if (item.quantity > 1) {
                        cartDao.updateQuantity(currentUserId, item.itemId, -1);
                        refreshCartData();
                    }
                });

                Label qtyDisplay = new Label(String.valueOf(item.quantity));
                qtyDisplay.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
                qtyDisplay.setMinWidth(24);
                qtyDisplay.setAlignment(javafx.geometry.Pos.CENTER);

                Button plusBtn = new Button();
                plusBtn.setGraphic(new FontIcon("fas-plus"));
                plusBtn.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 6; -fx-padding: 4; -fx-cursor: hand;");
                plusBtn.setOnAction(e -> {
                    cartDao.addToCart(currentUserId, item.itemId, 1);
                    refreshCartData();
                });

                HBox qtyControls = new HBox(4, minusBtn, qtyDisplay, plusBtn);
                qtyControls.setAlignment(javafx.geometry.Pos.CENTER);

                details.getChildren().addAll(qtyLabel, unitPriceLabel);

                HBox bottomRow = new HBox();
                bottomRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Region bottomSpacer = new Region();
                HBox.setHgrow(bottomSpacer, Priority.ALWAYS);
                bottomRow.getChildren().addAll(details, bottomSpacer, qtyControls);

                itemBox.getChildren().addAll(header, bottomRow);
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

    private void refreshCartData() {
        loadCartData();
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
            if (controller != null) controller.setCurrentUserId(currentUserId);

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
            // Calculate totals
            double subtotal = cartItems.stream().mapToDouble(i -> i.price * i.quantity).sum();
            double tax = Math.round(subtotal * 0.02 * 100.0) / 100.0;
            double total = subtotal + tax;

            // Navigate to payment page with cart data (order will be created after successful payment)
            navigateToPayment(total);

        } catch (Exception e) {
            showAlert("Error", "Failed to proceed to payment: " + e.getMessage());
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

    private void navigateToPayment(double totalAmount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Parent root = loader.load();
            PaymentController controller = loader.getController();
            if (controller != null) {
                controller.setCartData(currentUserId, cartItems, currentShop, totalAmount);
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
