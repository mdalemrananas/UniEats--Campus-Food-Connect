package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import java.io.IOException;
import java.util.List;
import com.unieats.FoodItem;
import com.unieats.Shop;
import com.unieats.User;
import com.unieats.dao.FoodItemDao;
import com.unieats.dao.ShopDao;
import org.kordamp.ikonli.javafx.FontIcon;

public class FoodItemsController {
    
    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private Button filterButton;
    @FXML private VBox foodItemsContainer;
    
    private User currentUser;
    private FoodItemDao foodItemDao;
    private ShopDao shopDao;
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        foodItemDao = new FoodItemDao();
        shopDao = new ShopDao();
    }
    
    private void setupEventHandlers() {
        backButton.setOnAction(e -> handleBack());
        searchField.setOnAction(e -> handleSearch());
        filterButton.setOnAction(e -> handleFilter());
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadFoodItems();
    }
    
    private void loadFoodItems() {
        try {
            // Get all shops first
            List<Shop> shops = shopDao.getApprovedShops();
            
            // For each shop, get its food items
            for (Shop shop : shops) {
                List<FoodItem> foodItems = foodItemDao.listByShop(shop.getId());
                for (FoodItem foodItem : foodItems) {
                    createFoodItemCard(foodItem, shop);
                }
            }
            
            if (foodItemsContainer.getChildren().isEmpty()) {
                showNoItemsMessage();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading food items: " + e.getMessage());
            showAlert("Error", "Failed to load food items: " + e.getMessage());
        }
    }
    
    private void createFoodItemCard(FoodItem foodItem, Shop shop) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-padding: 16; -fx-min-width: 320;");
        
        // Food item image placeholder
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); -fx-background-radius: 12; -fx-pref-height: 120; -fx-pref-width: 288;");
        FontIcon foodIcon = new FontIcon("fas-utensils");
        foodIcon.setIconSize(48);
        foodIcon.setIconColor(javafx.scene.paint.Color.web("#ff6b35"));
        imageContainer.getChildren().add(foodIcon);
        
        // Food item details
        VBox details = new VBox(8);
        
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(foodItem.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        
        Label shopLabel = new Label("from " + shop.getShopName());
        shopLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        header.getChildren().addAll(nameLabel, shopLabel);
        
        HBox info = new HBox(16);
        info.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label priceLabel = new Label("$" + String.format("%.2f", foodItem.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");
        
        Label stockLabel = new Label("Stock: " + foodItem.getStock());
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        Label pointsLabel = new Label("Points: " + foodItem.getPointsMultiplier() + "x");
        pointsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        info.getChildren().addAll(priceLabel, stockLabel, pointsLabel);
        
        // Action buttons
        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 12; -fx-cursor: hand;");
        addToCartButton.setOnAction(e -> handleAddToCart(foodItem));
        
        Button favoriteButton = new Button();
        favoriteButton.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; -fx-padding: 8; -fx-cursor: hand; -fx-border-color: #e9ecef; -fx-border-width: 1;");
        FontIcon heartIcon = new FontIcon("fas-heart");
        heartIcon.setIconSize(16);
        heartIcon.setIconColor(javafx.scene.paint.Color.web("#e74c3c"));
        favoriteButton.setGraphic(heartIcon);
        favoriteButton.setOnAction(e -> handleFavorite(foodItem));
        
        actions.getChildren().addAll(favoriteButton, addToCartButton);
        
        details.getChildren().addAll(header, info, actions);
        card.getChildren().addAll(imageContainer, details);
        // Open details on click anywhere on the card
        card.setOnMouseClicked(e -> openFoodDetails(foodItem, shop));
        imageContainer.setOnMouseClicked(e -> openFoodDetails(foodItem, shop));
        nameLabel.setOnMouseClicked(e -> openFoodDetails(foodItem, shop));
        
        foodItemsContainer.getChildren().add(card);
    }

    private void openFoodDetails(FoodItem foodItem, Shop shop) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_details.fxml"));
            Parent root = loader.load();
            FoodDetailsController controller = loader.getController();
            controller.setData(currentUser, foodItem, shop);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.err.println("Error opening details: " + ex.getMessage());
            showAlert("Error", "Failed to open food details: " + ex.getMessage());
        }
    }
    
    private void showNoItemsMessage() {
        VBox noItemsBox = new VBox(16);
        noItemsBox.setAlignment(javafx.geometry.Pos.CENTER);
        noItemsBox.setStyle("-fx-padding: 40;");
        
        FontIcon noItemsIcon = new FontIcon("fas-utensils");
        noItemsIcon.setIconSize(48);
        noItemsIcon.setIconColor(javafx.scene.paint.Color.web("#6c757d"));
        
        Label noItemsLabel = new Label("No food items available");
        noItemsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        
        noItemsBox.getChildren().addAll(noItemsIcon, noItemsLabel);
        foodItemsContainer.getChildren().add(noItemsBox);
    }
    
    @FXML
    private void handleBack() {
        try {
            // Load the menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();
            
            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();
            
            // Set the current user in the menu controller
            MenuController menuController = loader.getController();
            if (menuController != null) {
                menuController.setCurrentUser(currentUser);
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating back to menu: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate back to menu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            // TODO: Implement search functionality
            showAlert("Search", "Searching for: " + searchTerm);
        }
    }
    
    @FXML
    private void handleFilter() {
        showAlert("Filter", "Filter options will be displayed here");
    }
    
    private void handleAddToCart(FoodItem foodItem) {
        if (currentUser == null) {
            showAlert("Cart", "You must be signed in to add items to cart.");
            return;
        }
        
        try {
            new com.unieats.dao.CartDao().addToCart(currentUser.getId(), foodItem.getId(), 1);
            showAlert("Cart", "Added " + foodItem.getName() + " to cart!");
        } catch (Exception ex) {
            showAlert("Cart Error", ex.getMessage());
        }
    }
    
    private void handleFavorite(FoodItem foodItem) {
        showAlert("Favorite", "Added " + foodItem.getName() + " to favorites!");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
