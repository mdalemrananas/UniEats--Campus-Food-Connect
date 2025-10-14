package com.unieats.controllers;

import com.unieats.User;
import com.unieats.WishlistItemView;
import com.unieats.dao.WishlistDao;
import com.unieats.util.ThreadSafeUtils;
import com.unieats.util.UIUtils;
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
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class WishlistController {
	@FXML private Button backButton;
	@FXML private ListView<WishlistItemView> wishlistList;
	@FXML private VBox emptyState;
	@FXML private VBox navHome;
	@FXML private VBox navOrders;
	@FXML private VBox navCart;
	@FXML private VBox navFav;
	@FXML private VBox navProfile;

	private final WishlistDao wishlistDao = new WishlistDao();
	private User currentUser;

	@FXML
	private void initialize() {
		wishlistList.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(WishlistItemView item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) { 
					setGraphic(null); 
					setText(null); 
					return; 
				}
				
				// Create main card container
				VBox card = new VBox(8);
				card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
				
				// Header with item name and unlike button
				HBox header = new HBox();
				header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				header.setSpacing(8);
				
				Label name = new Label(item.getItemName());
				name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
				
				Region spacer = new Region();
				HBox.setHgrow(spacer, Priority.ALWAYS);
				
				// Unlike button
				Button unlikeButton = new Button();
				unlikeButton.setStyle("-fx-background-color: #ff6b6b; -fx-background-radius: 20; -fx-padding: 8; -fx-cursor: hand;");
				FontIcon heartIcon = new FontIcon("fas-heart-broken");
				heartIcon.setIconSize(14);
				heartIcon.setIconColor(javafx.scene.paint.Color.WHITE);
				unlikeButton.setGraphic(heartIcon);
				
				unlikeButton.setOnAction(e -> handleUnlike(item.getItemId()));
				
				// Add hover effect
				unlikeButton.setOnMouseEntered(mouseEvent -> 
					unlikeButton.setStyle("-fx-background-color: #ff5252; -fx-background-radius: 20; -fx-padding: 8; -fx-cursor: hand;"));
				unlikeButton.setOnMouseExited(mouseEvent -> 
					unlikeButton.setStyle("-fx-background-color: #ff6b6b; -fx-background-radius: 20; -fx-padding: 8; -fx-cursor: hand;"));
				
				header.getChildren().addAll(name, spacer, unlikeButton);
				
				// Shop name
				Label shopLabel = new Label("from " + item.getShopName());
				shopLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
				
                // Price, stock, points
				HBox priceContainer = new HBox();
				priceContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				Region priceSpacer = new Region();
				HBox.setHgrow(priceSpacer, Priority.ALWAYS);
				Label price = new Label(String.format("$%.2f", item.getPrice()));
				price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");
                Label stock = new Label();
                stock.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
                Label points = new Label();
                points.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
                // Fetch stock/points on demand
                try {
                    var foodDao = new com.unieats.dao.FoodItemDao();
                    com.unieats.FoodItem fi = foodDao.getById(item.getItemId());
                    if (fi != null) {
                        stock.setText("Stock: " + fi.getStock());
                        points.setText("Points: " + fi.getPointsMultiplier() + "x");
                    } else {
                        stock.setText("Stock: N/A");
                        points.setText("Points: N/A");
                    }
                } catch (Exception ex) {
                    stock.setText("Stock: N/A");
                    points.setText("Points: N/A");
                }
                priceContainer.getChildren().addAll(stock, points, priceSpacer, price);
				
                card.getChildren().addAll(header, shopLabel, priceContainer);
                // Navigate to details on click
				card.setOnMouseClicked(e -> openFoodDetails(item.getItemId(), item.getShopId()));
				setGraphic(card);
				setText(null);
			}
		});
	}

    private void openFoodDetails(int itemId, int shopId) {
        try {
            var foodDao = new com.unieats.dao.FoodItemDao();
            var shopDao = new com.unieats.dao.ShopDao();
            com.unieats.FoodItem item = foodDao.getById(itemId);
            com.unieats.Shop shop = shopDao.getShopById(shopId);
            if (item == null || shop == null) return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_details.fxml"));
            Parent root = loader.load();
            FoodDetailsController controller = loader.getController();
            controller.setData(currentUser, item, shop);
            Stage stage = (Stage) wishlistList.getScene().getWindow();
            Scene newScene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(newScene);
            stage.setTitle("UniEats - Details");
            stage.show();
        } catch (Exception ignored) {}
    }

	public void setCurrentUser(User user) {
		this.currentUser = user;
		loadWishlist();
		setupNav();
	}

	private void loadWishlist() {
		if (currentUser == null) return;
		
		// Load wishlist in background thread to avoid blocking UI
		ThreadSafeUtils.runAsyncWithErrorHandling(
			() -> {
				// Background task
				List<WishlistItemView> items = wishlistDao.listWishlistItemsWithShop(currentUser.getId());
				ObservableList<WishlistItemView> obs = FXCollections.observableArrayList(items);
				
				// Update UI on JavaFX thread with animation
				ThreadSafeUtils.runOnFXThread(() -> {
					wishlistList.setItems(obs);
					emptyState.setVisible(items.isEmpty());
					
					// Apply animation to list items
					if (!items.isEmpty()) {
						UIUtils.fadeIn(wishlistList, Duration.millis(300));
					}
				});
			},
			() -> {
				// UI update completed
			},
			exception -> {
				showAlert("Error", "Failed to load wishlist: " + exception.getMessage());
			}
		);
	}

	private void handleUnlike(int itemId) {
		if (currentUser == null) return;
		
		// Perform unlike operation in background thread
		ThreadSafeUtils.runAsyncWithErrorHandling(
			() -> {
				// Background task - remove from wishlist
				wishlistDao.removeFromWishlist(currentUser.getId(), itemId);
			},
			() -> {
				// UI update on JavaFX thread
				loadWishlist(); // Refresh the list
				showAlert("Success", "Item removed from favorites!");
			},
			exception -> {
				showAlert("Error", "Failed to remove item from favorites: " + exception.getMessage());
			}
		);
	}

	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	@FXML
	private void handleBack() {
		navigateTo("/fxml/menu.fxml", "UniEats - Menu");
	}

	private void setupNav() {
		if (navHome != null) navHome.setOnMouseClicked(e -> navigateTo("/fxml/menu.fxml", "UniEats - Menu"));
		if (navOrders != null) navOrders.setOnMouseClicked(e -> navigateTo("/fxml/my_orders.fxml", "UniEats - My Orders"));
		if (navCart != null) navCart.setOnMouseClicked(e -> navigateTo("/fxml/cart.fxml", "UniEats - Cart"));
		if (navFav != null) navFav.setOnMouseClicked(e -> {/* already here */});
		if (navProfile != null) navProfile.setOnMouseClicked(e -> navigateTo("/fxml/profile.fxml", "UniEats - Profile"));
	}

	private void navigateTo(String fxml, String title) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent root = loader.load();
			Object controller = loader.getController();
			if (controller instanceof MenuController mc && currentUser != null) mc.setCurrentUser(currentUser);
			if (controller instanceof MyOrdersController oc && currentUser != null) oc.setCurrentUser(currentUser);
			if (controller instanceof CartController cc && currentUser != null) cc.setCurrentUserId(currentUser.getId());
			if (controller instanceof ProfileController pc && currentUser != null) pc.setCurrentUser(currentUser);
			Stage stage = (Stage) wishlistList.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.setTitle(title);
			stage.show();
		} catch (Exception e) {
			Alert a = new Alert(Alert.AlertType.ERROR, "Navigation error: " + e.getMessage());
			a.showAndWait();
		}
	}
}
