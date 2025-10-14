package com.unieats.controllers;

import com.unieats.Shop;
import com.unieats.User;
import com.unieats.dao.ReviewDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class ShopDetailsController {
	@FXML private Button backButton;
	@FXML private Label shopNameLabel;
	@FXML private Label avgStarsLabel;
	@FXML private Label avgRatingLabel;
	@FXML private Label reviewsCountLabel;
	@FXML private Label detailsLabel;
	@FXML private Button viewMenuButton;
	@FXML private ListView<String> reviewsList;
	@FXML private ComboBox<Integer> ratingCombo;
	@FXML private TextArea commentField;
	@FXML private Button submitReviewButton;

	// Star rating components
	@FXML private Button star1;
	@FXML private Button star2;
	@FXML private Button star3;
	@FXML private Button star4;
	@FXML private Button star5;
	@FXML private FontIcon star1Icon;
	@FXML private FontIcon star2Icon;
	@FXML private FontIcon star3Icon;
	@FXML private FontIcon star4Icon;
	@FXML private FontIcon star5Icon;
	
	// Bottom navigation
	@FXML private VBox navHome;
	@FXML private VBox navOrders;
	@FXML private VBox navCart;
	@FXML private VBox navFav;
	@FXML private VBox navProfile;

	private final ReviewDao reviewDao = new ReviewDao();
	private User currentUser;
	private Shop shop;
	private int selectedRating = 0; // Default to no stars selected

	@FXML
	private void initialize() {
		setupStarRatingHandlers();
		if (submitReviewButton != null) submitReviewButton.setOnAction(e -> handleSubmitReview());
		if (backButton != null) backButton.setOnAction(e -> handleBack());
		if (viewMenuButton != null) viewMenuButton.setOnAction(e -> handleViewMenu());
		updateStarDisplay();
		// Wire bottom navigation
		wireBottomNavigation();
	}

	private void setupStarRatingHandlers() {
		if (star1 != null) star1.setOnAction(e -> setRating(1));
		if (star2 != null) star2.setOnAction(e -> setRating(2));
		if (star3 != null) star3.setOnAction(e -> setRating(3));
		if (star4 != null) star4.setOnAction(e -> setRating(4));
		if (star5 != null) star5.setOnAction(e -> setRating(5));
	}

	private void setRating(int rating) {
		selectedRating = rating;
		updateStarDisplay();
	}

	private void updateStarDisplay() {
		updateStarIcon(star1Icon, 1);
		updateStarIcon(star2Icon, 2);
		updateStarIcon(star3Icon, 3);
		updateStarIcon(star4Icon, 4);
		updateStarIcon(star5Icon, 5);
	}

	private void updateStarIcon(FontIcon icon, int starNumber) {
		if (icon != null) {
			boolean filled = starNumber <= selectedRating;
			icon.setIconLiteral(filled ? "fas-star" : "far-star");
			icon.setIconColor(filled ? javafx.scene.paint.Paint.valueOf("#ffb703") : javafx.scene.paint.Paint.valueOf("#6c757d"));
		}
	}

	public void setData(User user, Shop shop) {
		this.currentUser = user;
		this.shop = shop;
		populate();
		loadReviews();
	}

	private void populate() {
		if (shop == null) return;
		shopNameLabel.setText(shop.getShopName());
		detailsLabel.setText("Shop ID: " + shop.getId() + (shop.getCreatedAt() != null ? "  •  Since " + shop.getCreatedAt().toLocalDate() : ""));
	}

	private void loadReviews() {
		if (shop == null) return;
		double avg = reviewDao.getAverageRatingForShop(shop.getId());
		List<ReviewDao.Review> items = reviewDao.listReviewsForShop(shop.getId(), 50);
		avgRatingLabel.setText(String.format("%.1f", avg));
		reviewsCountLabel.setText("(" + items.size() + " reviews)");
		avgStarsLabel.setText(buildStars(avg));
		ObservableList<String> list = FXCollections.observableArrayList();
		for (ReviewDao.Review r : items) {
			String stars = buildStars(r.rating());
			String who = r.userName() != null ? r.userName() : ("User #" + r.userId());
			String line = stars + "  •  " + who + (r.comment() == null || r.comment().isEmpty() ? "" : (" — " + r.comment()));
			list.add(line);
		}
		reviewsList.setItems(list);
	}

	private String buildStars(double rating) {
		int full = (int)Math.round(rating);
		full = Math.max(0, Math.min(5, full));
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<full;i++) sb.append('★');
		for (int i=full;i<5;i++) sb.append('☆');
		return sb.toString();
	}

	private void handleSubmitReview() {
		if (currentUser == null) { alert("Reviews", "Please sign in to write a review."); return; }
		if (selectedRating == 0) { alert("Reviews", "Please select a rating."); return; }
		String comment = commentField.getText() != null ? commentField.getText().trim() : "";
		try {
			reviewDao.addShopReview(currentUser.getId(), shop.getId(), selectedRating, comment);
			commentField.clear();
			selectedRating = 0; // Reset to no stars selected
			updateStarDisplay();
			loadReviews();
			alert("Reviews", "Thank you for your review!");
		} catch (Exception e) { alert("Reviews", "Failed to submit review: " + e.getMessage()); }
	}

	private void handleBack() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shops.fxml"));
			Parent root = loader.load();
			ShopsController controller = loader.getController();
			if (controller != null) controller.setCurrentUser(currentUser);
			Stage stage = (Stage) backButton.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.show();
		} catch (Exception e) { alert("Navigation Error", e.getMessage()); }
	}

	private void handleViewMenu() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_items.fxml"));
			Parent root = loader.load();
			FoodItemsController controller = loader.getController();
			if (controller != null) {
				controller.setCurrentUser(currentUser);
				try {
					controller.getClass().getMethod("setShopFilter", int.class).invoke(controller, shop.getId());
				} catch (Exception ignored) {}
			}
			Stage stage = (Stage) viewMenuButton.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.show();
		} catch (Exception e) { alert("Navigation Error", e.getMessage()); }
	}

	private void alert(String title, String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle(title);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
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
			alert("Navigation Error", "Failed to navigate to menu: " + e.getMessage());
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
			alert("Navigation Error", "Failed to navigate to orders: " + e.getMessage());
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
			alert("Navigation Error", "Failed to navigate to cart: " + e.getMessage());
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
			alert("Navigation Error", "Failed to navigate to favorites: " + e.getMessage());
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
			alert("Navigation Error", "Failed to navigate to profile: " + e.getMessage());
		}
	}
}
