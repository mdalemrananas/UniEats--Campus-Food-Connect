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

	private final ReviewDao reviewDao = new ReviewDao();
	private User currentUser;
	private Shop shop;

	@FXML
	private void initialize() {
		if (ratingCombo != null) {
			ratingCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5));
			ratingCombo.getSelectionModel().select(4);
		}
		if (submitReviewButton != null) submitReviewButton.setOnAction(e -> handleSubmitReview());
		if (backButton != null) backButton.setOnAction(e -> handleBack());
		if (viewMenuButton != null) viewMenuButton.setOnAction(e -> handleViewMenu());
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
		Integer rating = ratingCombo.getValue();
		if (rating == null) { alert("Reviews", "Please select a rating."); return; }
		String comment = commentField.getText() != null ? commentField.getText().trim() : "";
		try {
			reviewDao.addShopReview(currentUser.getId(), shop.getId(), rating, comment);
			commentField.clear();
			ratingCombo.getSelectionModel().select(4);
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
}
