package com.unieats.controllers;

import com.unieats.FoodItem;
import com.unieats.Shop;
import com.unieats.User;
import com.unieats.dao.CartDao;
import com.unieats.dao.ReviewDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FoodDetailsController {

    @FXML private Button backButton;
    @FXML private Label titleLabel;
    @FXML private ImageView imageView;
    @FXML private FontIcon placeholderIcon;

    @FXML private Label nameLabel;
    @FXML private Label shopLabel;
    @FXML private Label priceLabel;
    @FXML private Label discountLabel;
    @FXML private Label pointsLabel;
    @FXML private Label stockLabel;
    @FXML private Label descriptionLabel;

    @FXML private Label avgStarsLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label reviewsCountLabel;
    @FXML private ListView<String> reviewsList;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private TextArea commentField;
    @FXML private Button submitReviewButton;

    @FXML private Button addToCartButton;
    @FXML private Button favButton;
    @FXML private FontIcon favIcon;

    private final ReviewDao reviewDao = new ReviewDao();
    private User currentUser;
    private FoodItem foodItem;
    private Shop shop;

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> handleBack());
        addToCartButton.setOnAction(e -> handleAddToCart());
        if (favButton != null) favButton.setOnAction(e -> handleToggleFavourite());
        if (ratingCombo != null) {
            ratingCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5));
            ratingCombo.getSelectionModel().select(4);
        }
        if (submitReviewButton != null) {
            submitReviewButton.setOnAction(e -> handleSubmitReview());
        }
    }

    public void setData(User user, FoodItem item, Shop shop) {
        this.currentUser = user;
        this.foodItem = item;
        this.shop = shop;
        populate();
        loadReviews();
    }

    private void populate() {
        if (foodItem == null || shop == null) return;
        titleLabel.setText("Details");
        nameLabel.setText(foodItem.getName());
        shopLabel.setText("from " + shop.getShopName());

        double price = foodItem.getPrice();
        priceLabel.setText("$" + String.format("%.2f", price));

        if (foodItem.getDiscount() != null && foodItem.getDiscount() > 0) {
            discountLabel.setText("-" + String.format("%.0f", foodItem.getDiscount()) + "% off");
        } else {
            discountLabel.setText("");
        }

        pointsLabel.setText("Points: " + foodItem.getPointsMultiplier() + "x");
        stockLabel.setText("Stock: " + foodItem.getStock());
        descriptionLabel.setText(foodItem.getDescription() != null ? foodItem.getDescription() : "No description available.");

        // Load first image if available
        String images = foodItem.getImages();
        boolean imageLoaded = false;
        if (images != null && !images.trim().isEmpty()) {
            String first = extractFirstImagePath(images);
            if (first != null && !first.isEmpty()) {
                try {
                    File f = new File(first);
                    Image img = f.exists() ? new Image(f.toURI().toString()) : new Image(first);
                    imageView.setImage(img);
                    imageLoaded = true;
                } catch (Exception ignored) {}
            }
        }
        placeholderIcon.setVisible(!imageLoaded);
        // Initialize favourite icon state
        updateFavouriteIcon();
    }

    private void updateFavouriteIcon() {
        try {
            int userId = currentUser != null ? currentUser.getId() : -1;
            boolean liked = userId > 0 && new com.unieats.dao.WishlistDao().isInWishlist(userId, foodItem.getId());
            if (favIcon != null) {
                favIcon.setIconLiteral(liked ? "fas-heart" : "far-heart");
                favIcon.setIconColor(javafx.scene.paint.Color.web("#e74c3c"));
            }
        } catch (Exception ignored) {}
    }

    private void handleToggleFavourite() {
        if (currentUser == null) { showAlert("Favourite", "Please sign in to save favourites."); return; }
        var dao = new com.unieats.dao.WishlistDao();
        boolean liked = dao.isInWishlist(currentUser.getId(), foodItem.getId());
        if (liked) dao.removeFromWishlist(currentUser.getId(), foodItem.getId()); else dao.addToWishlist(currentUser.getId(), foodItem.getId(), 1);
        updateFavouriteIcon();
    }

    private void loadReviews() {
        if (foodItem == null) return;
        try {
            double avg = reviewDao.getAverageRatingForFood(foodItem.getId());
            List<ReviewDao.Review> items = reviewDao.listReviewsForFood(foodItem.getId(), 50);
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
        } catch (Exception e) {
            // Silent fail to avoid breaking UI
        }
    }

    private String buildStars(double rating) {
        int full = (int)Math.round(rating);
        full = Math.max(0, Math.min(5, full));
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<full;i++) sb.append('★');
        for (int i=full;i<5;i++) sb.append('☆');
        return sb.toString();
    }

    private String extractFirstImagePath(String images) {
        // Supports comma-separated or JSON array-like strings
        String s = images.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length()-1);
        }
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1);
        }
        String[] parts = s.split(",");
        if (parts.length == 0) return null;
        String first = parts[0].trim();
        if (first.startsWith("\"") && first.endsWith("\"")) {
            first = first.substring(1, first.length()-1);
        }
        return first;
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_items.fxml"));
            Parent root = loader.load();
            FoodItemsController controller = loader.getController();
            if (controller != null) controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to go back: " + e.getMessage());
        }
    }

    private void handleAddToCart() {
        if (currentUser == null) {
            showAlert("Cart", "You must be signed in to add items to cart.");
            return;
        }
        try {
            new CartDao().addToCart(currentUser.getId(), foodItem.getId(), 1);
            showAlert("Cart", "Added " + foodItem.getName() + " to cart!");
        } catch (Exception ex) {
            if (ex.getMessage().contains("same shop")) {
                showAlert("Shop Restriction", ex.getMessage() + "\n\nWould you like to clear your cart and add this item?", true, foodItem.getId());
            } else {
                showAlert("Cart Error", ex.getMessage());
            }
        }
    }

    private void handleSubmitReview() {
        if (currentUser == null) { showAlert("Reviews", "Please sign in to write a review."); return; }
        Integer rating = ratingCombo.getValue();
        if (rating == null) { showAlert("Reviews", "Please select a rating."); return; }
        String comment = commentField.getText() != null ? commentField.getText().trim() : "";
        try {
            reviewDao.addFoodReview(currentUser.getId(), foodItem.getId(), rating, comment);
            commentField.clear();
            ratingCombo.getSelectionModel().select(4);
            loadReviews();
            showAlert("Reviews", "Thank you for your review!");
        } catch (Exception e) {
            showAlert("Reviews", "Failed to submit review: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String content, boolean isConfirmation, int itemId) {
        if (isConfirmation) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        new CartDao().clearCart(currentUser.getId());
                        new CartDao().addToCart(currentUser.getId(), itemId, 1);
                        showAlert("Cart", "Cart cleared and item added!");
                    } catch (Exception ex) {
                        showAlert("Cart Error", "Failed to clear cart and add item: " + ex.getMessage());
                    }
                }
            });
        } else {
            showAlert(title, content);
        }
    }
}
