package com.unieats.controllers;

import com.unieats.CartItemView;
import com.unieats.RewardService;
import com.unieats.dao.CartDao;
import com.unieats.dao.CartQueryDao;
import com.unieats.dao.OrderDao;
import com.unieats.DatabaseManager;
import com.unieats.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.Cursor;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.Stage;

import java.util.*;

public class CartController {

    // Legacy table-based UI (kept compatible if referenced by older FXML)
    @FXML private TableView<CartItemView> cartTable;
    @FXML private TableColumn<CartItemView, String> colName;
    @FXML private TableColumn<CartItemView, Number> colQty;
    @FXML private TableColumn<CartItemView, Number> colPrice;
    @FXML private TableColumn<CartItemView, Number> colPoints;

    // New image-style UI
    @FXML private ListView<CartItemView> cartList;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    // Bottom navigation (from cart.fxml)
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;

    private final CartQueryDao cartQueryDao = new CartQueryDao();
    private final CartDao cartDao = new CartDao();
    private final OrderDao orderDao = new OrderDao();
    private int currentUserId;

    @FXML
    private void initialize() {
        // Table column bindings if table exists in the loaded FXML
        if (colName != null) colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name));
        if (colQty != null) colQty.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().quantity));
        if (colPrice != null) colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().price));
        if (colPoints != null) colPoints.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().pointsMultiplier * data.getValue().quantity));

        // ListView graphic cell for image-style interface
        if (cartList != null) {
            cartList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(CartItemView item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }

                    // Thumbnail placeholder using a circle icon
                    StackPane thumb = new StackPane();
                    thumb.setStyle("-fx-background-color: #f1f3f4; -fx-background-radius: 12;");
                    thumb.setMinSize(48, 48);
                    thumb.setPrefSize(48, 48);
                    FontIcon foodIcon = new FontIcon("fas-utensils");
                    foodIcon.setIconSize(18);
                    foodIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#6c757d"));
                    thumb.getChildren().add(foodIcon);

                    // Texts
                    Label name = new Label(item.name);
                    name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                    Label price = new Label(String.format("$ %.2f", item.price));
                    price.setStyle("-fx-text-fill: #2d3436; -fx-font-size: 12px;");
                    VBox textBox = new VBox(4, name, price);
                    textBox.setAlignment(Pos.CENTER_LEFT);

                    // Quantity control
                    Button minus = new Button();
                    minus.setGraphic(new FontIcon("fas-minus"));
                    minus.setOnAction(e -> {
                        cartDao.updateQuantity(currentUserId, item.itemId, -1);
                        refresh();
                    });
                    Button plus = new Button();
                    plus.setGraphic(new FontIcon("fas-plus"));
                    plus.setOnAction(e -> {
                        cartDao.addToCart(currentUserId, item.itemId, 1);
                        refresh();
                    });
                    Label qty = new Label(String.valueOf(item.quantity));
                    qty.setMinWidth(24);
                    qty.setAlignment(Pos.CENTER);
                    HBox qtyBox = new HBox(8, minus, qty, plus);
                    qtyBox.setAlignment(Pos.CENTER);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    HBox row = new HBox(12, thumb, textBox, spacer, qtyBox);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8));
                    row.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(0,0,0,0.06); -fx-border-width: 0 0 1 0;");
                    setGraphic(row);
                    setText(null);
                }
            });
        }

        // Wire bottom nav clicks
        wireBottomNav();
        // Highlight Cart as active and ensure pointer cursor on its icon/label
        highlightActiveCart();
    }

    private void wireBottomNav() {
        if (navHome != null) navHome.setOnMouseClicked(e -> navigateTo("/fxml/menu.fxml", "UniEats - Menu"));
        if (navOrders != null) navOrders.setOnMouseClicked(e -> navigateTo("/fxml/my_orders.fxml", "UniEats - My Orders"));
        if (navCart != null) navCart.setOnMouseClicked(e -> {/* already here */});
        if (navFav != null) navFav.setOnMouseClicked(e -> navigateTo("/fxml/wishlist.fxml", "UniEats - Favourites"));
        if (navProfile != null) navProfile.setOnMouseClicked(e -> navigateTo("/fxml/profile.fxml", "UniEats - Profile"));
    }

    private void highlightActiveCart() {
        if (navCart == null) return;
        try {
            // Apply pointer cursor on the whole tab and its children
            navCart.setCursor(Cursor.HAND);
            for (javafx.scene.Node child : navCart.getChildren()) {
                child.setCursor(Cursor.HAND);
                if (child instanceof StackPane sp) {
                    for (javafx.scene.Node n : sp.getChildren()) {
                        n.setCursor(Cursor.HAND);
                    }
                }
            }

            // Active styles: blue accent for Cart
            if (navCart.getChildren().size() >= 2) {
                javafx.scene.Node iconWrap = navCart.getChildren().get(0);
                javafx.scene.Node labelNode = navCart.getChildren().get(1);

                if (iconWrap instanceof StackPane sp) {
                    sp.setStyle("-fx-background-color: rgba(13,110,253,0.1); -fx-background-radius: 12; -fx-padding: 8;");
                    if (!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof FontIcon fi) {
                        fi.setIconColor(javafx.scene.paint.Paint.valueOf("#0d6efd"));
                    }
                }
                if (labelNode instanceof Label lbl) {
                    lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #0d6efd; -fx-font-weight: bold;");
                }
            }
        } catch (Exception ignored) {}
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = findStage();
            if (stage == null) { info("Unable to resolve current window."); return; }

            Scene newScene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            copyCurrentStyles(newScene);
            stage.setScene(newScene);
            stage.setTitle(title);
            stage.show();

            // Pass user to target controllers when possible
            Object controller = loader.getController();
            User user = currentUserId > 0 ? DatabaseManager.getInstance().getUserById(currentUserId) : null;
            if (controller instanceof com.unieats.controllers.MenuController mc) {
                if (user != null) mc.setCurrentUser(user);
            } else if (controller instanceof com.unieats.controllers.ProfileController pc) {
                if (user != null) pc.setCurrentUser(user);
            } else if (controller instanceof com.unieats.controllers.WishlistController wc) {
                if (user != null) wc.setCurrentUser(user);
            } else if (controller instanceof com.unieats.controllers.MyOrdersController oc) {
                if (user != null) oc.setCurrentUser(user);
            }
        } catch (Exception ex) {
            info("Navigation error: " + ex.getMessage());
        }
    }

    private Stage findStage() {
        if (cartList != null && cartList.getScene() != null) {
            return (Stage) cartList.getScene().getWindow();
        }
        if (totalLabel != null && totalLabel.getScene() != null) {
            return (Stage) totalLabel.getScene().getWindow();
        }
        return null;
    }

    private void copyCurrentStyles(Scene newScene) {
        Stage stage = findStage();
        if (stage != null && stage.getScene() != null) {
            var current = stage.getScene().getStylesheets();
            if (current != null && !current.isEmpty()) {
                newScene.getStylesheets().addAll(current);
            }
        }
    }
    

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        refresh();
    }

    private void refresh() {
        List<CartItemView> items = cartQueryDao.listCartItems(currentUserId);
        ObservableList<CartItemView> data = FXCollections.observableArrayList(items);
        if (cartTable != null) cartTable.setItems(data);
        if (cartList != null) cartList.setItems(data);

        double subtotal = items.stream().mapToDouble(i -> i.price * i.quantity).sum();
        double tax = Math.round(subtotal * 0.02 * 100.0) / 100.0; // 2%
        double total = subtotal + tax;

        if (subtotalLabel != null) subtotalLabel.setText(String.format("$%.2f", subtotal));
        if (taxLabel != null) taxLabel.setText(String.format("$%.2f", tax));
        if (totalLabel != null) totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleCheckout() {
        List<CartItemView> items = cartList != null ? new ArrayList<>(cartList.getItems())
                : (cartTable != null ? new ArrayList<>(cartTable.getItems()) : new ArrayList<>());
        if (items.isEmpty()) { info("Cart is empty"); return; }
        // Ensure single-shop per order for points logic simplicity
        int shopId = items.get(0).shopId;
        boolean multiShop = items.stream().anyMatch(i -> i.shopId != shopId);
        if (multiShop) { info("Please checkout items from one shop at a time."); return; }

        // Navigate to checkout page
        navigateToCheckout();
    }

    private void navigateToCheckout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/checkout.fxml"));
            Parent root = loader.load();
            CheckoutController controller = loader.getController();
            if (controller != null) {
                User user = currentUserId > 0 ? DatabaseManager.getInstance().getUserById(currentUserId) : null;
                if (user != null) controller.setCurrentUser(user);
            }

            Stage stage = findStage();
            if (stage == null) { info("Unable to resolve current window."); return; }

            Scene newScene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            copyCurrentStyles(newScene);
            stage.setScene(newScene);
            stage.setTitle("UniEats - Checkout");
            stage.show();
        } catch (Exception ex) {
            info("Navigation error: " + ex.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        cartDao.clearCart(currentUserId);
        refresh();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
