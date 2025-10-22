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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    @FXML private Button addToCartButton;
    @FXML private Button favButton;
    @FXML private FontIcon favIcon;

    // Bottom nav items
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;

    private final ReviewDao reviewDao = new ReviewDao();
    private User currentUser;
    private FoodItem foodItem;
    private Shop shop;
    private int selectedRating = 0; // Default to no stars selected

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupNavigationHandlers();
        setupStarRatingHandlers();
        backButton.setOnAction(e -> handleBack());
        addToCartButton.setOnAction(e -> handleAddToCart());
        if (favButton != null) favButton.setOnAction(e -> handleToggleFavourite());
        if (submitReviewButton != null) {
            submitReviewButton.setOnAction(e -> handleSubmitReview());
        }
        // Set active nav state for food details (no direct tab, but highlight orders as context)
        if (navOrders != null) setActiveNav(navOrders);
        updateStarDisplay();
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

    private void setupEventHandlers() {
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

    private void setupNavigationHandlers() {
        if (navHome != null) navHome.setOnMouseClicked(e -> { setActiveNav(navHome); navigateToMenu(); });
        if (navOrders != null) navOrders.setOnMouseClicked(e -> { setActiveNav(navOrders); navigateToOrders(); });
        if (navCart != null) navCart.setOnMouseClicked(e -> { setActiveNav(navCart); navigateToCart(); });
        if (navFav != null) navFav.setOnMouseClicked(e -> { setActiveNav(navFav); navigateToFavourites(); });
        if (navProfile != null) navProfile.setOnMouseClicked(e -> { setActiveNav(navProfile); navigateToProfile(); });
    }

    private void setActiveNav(VBox active) {
        if (navHome != null) applyActive(navHome, navHome == active, "#2e7d32");
        if (navOrders != null) applyActive(navOrders, navOrders == active, "#ff6b35");
        if (navCart != null) applyActive(navCart, navCart == active, "#0d6efd");
        if (navFav != null) applyActive(navFav, navFav == active, "#e63946");
        if (navProfile != null) applyActive(navProfile, navProfile == active, "#6f42c1");
    }

    private void applyActive(VBox tab, boolean active, String colorHex) {
        if (tab == null) return;
        if (tab.getChildren().size() < 2) return;
        StackPane iconWrap = (StackPane) tab.getChildren().get(0);
        Label label = (Label) tab.getChildren().get(1);

        if (!iconWrap.getChildren().isEmpty() && iconWrap.getChildren().get(0) instanceof FontIcon) {
            FontIcon icon = (FontIcon) iconWrap.getChildren().get(0);
            icon.setIconColor(active ? javafx.scene.paint.Paint.valueOf(colorHex) : javafx.scene.paint.Paint.valueOf("#6c757d"));
        }

        String bg = active ? String.format("-fx-background-color: %s1A; -fx-background-radius: 12; -fx-padding: 8;", colorHex.replace("#","#"))
                           : "-fx-background-radius: 12; -fx-padding: 8;";
        iconWrap.setStyle(bg);

        label.setStyle(active ? String.format("-fx-font-size: 10px; -fx-text-fill: %s; -fx-font-weight: bold;", colorHex)
                              : "-fx-font-size: 10px; -fx-text-fill: #6c757d;");
    }

    private void navigateToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();
            MenuController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
            Stage stage = (Stage) navHome.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Menu");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }

    private void navigateToOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_orders.fxml"));
            Parent root = loader.load();
            MyOrdersController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
            Stage stage = (Stage) navOrders.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - My Orders");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }

    private void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Parent root = loader.load();
            com.unieats.controllers.CartController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUserId(currentUser.getId());
            Stage stage = (Stage) navCart.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Cart");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }

    private void navigateToFavourites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/wishlist.fxml"));
            Parent root = loader.load();
            WishlistController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
            Stage stage = (Stage) navFav.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Favourites");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }

    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
            Stage stage = (Stage) navProfile.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Profile");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
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
        priceLabel.setText("৳" + String.format("%.2f", price));

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
            if (isInCart(foodItem.getId())) {
                // Remove from cart
                removeFromCart(foodItem.getId());
                showAlert("Cart", "Removed " + foodItem.getName() + " from cart!");
            } else {
                // Add to cart
                new CartDao().addToCart(currentUser.getId(), foodItem.getId(), 1);
                showAlert("Cart", "Added " + foodItem.getName() + " to cart!");
            }
        } catch (Exception ex) {
            if (ex.getMessage().contains("same shop")) {
                showAlert("Shop Restriction", ex.getMessage() + "\n\nWould you like to clear your cart and add this item?", true, foodItem.getId());
            } else {
                showAlert("Cart Error", ex.getMessage());
            }
        }
    }

    private boolean isInCart(int itemId) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) return false;
        return new CartDao().isInCart(userId, itemId);
    }

    private void removeFromCart(int itemId) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) { showAlert("Cart", "You must be signed in to remove items from cart."); return; }
        try {
            new CartDao().removeFromCart(userId, itemId);
        } catch (Exception ex) {
            showAlert("Cart Error", "Failed to remove item from cart: " + ex.getMessage());
        }
    }

    private void handleSubmitReview() {
        if (currentUser == null) { showAlert("Reviews", "Please sign in to write a review."); return; }
        if (selectedRating == 0) { showAlert("Reviews", "Please select a rating."); return; }
        String comment = commentField.getText() != null ? commentField.getText().trim() : "";
        try {
            reviewDao.addFoodReview(currentUser.getId(), foodItem.getId(), selectedRating, comment);
            commentField.clear();
            selectedRating = 0; // Reset to no stars selected
            updateStarDisplay();
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
