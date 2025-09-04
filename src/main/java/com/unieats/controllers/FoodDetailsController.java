package com.unieats.controllers;

import com.unieats.FoodItem;
import com.unieats.Shop;
import com.unieats.User;
import com.unieats.dao.CartDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;

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

    @FXML private Button addToCartButton;

    private User currentUser;
    private FoodItem foodItem;
    private Shop shop;

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> handleBack());
        addToCartButton.setOnAction(e -> handleAddToCart());
    }

    public void setData(User user, FoodItem item, Shop shop) {
        this.currentUser = user;
        this.foodItem = item;
        this.shop = shop;
        populate();
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
            showAlert("Cart Error", ex.getMessage());
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
