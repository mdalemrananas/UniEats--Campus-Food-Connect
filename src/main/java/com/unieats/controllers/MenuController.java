package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.unieats.User;
import com.unieats.controllers.FoodItemsController;
import com.unieats.controllers.ShopsController;
import com.unieats.controllers.ReportController;
import com.unieats.controllers.ProfileController;

public class MenuController {
    
    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    @FXML private TextField searchField;
    @FXML private Button filterButton;
    @FXML private Button leftSlideButton;
    @FXML private Button rightSlideButton;
    @FXML private ScrollPane foodSliderPane;
    @FXML private HBox foodSliderContainer;
    @FXML private Button logoutButton;
    @FXML private Button cartButton;
    @FXML private Label sessionTimeLabel;
    @FXML private VBox foodCategoryButton;
    @FXML private VBox storesCategoryButton;
    @FXML private VBox reportButton;
    @FXML private VBox profileButton;
    
    // Food item buttons
    @FXML private Button favoriteButton1;
    @FXML private Button favoriteButton2;
    @FXML private Button favoriteButton3;
    @FXML private Button favoriteButton4;
    @FXML private Button addToCartButton1;
    @FXML private Button addToCartButton2;
    @FXML private Button addToCartButton3;
    @FXML private Button addToCartButton4;
    
    private User currentUser;
    private int currentCardIndex = 0;
    private final int totalCards = 4;
    private final double cardWidth = 216; // 200px card width + 16px spacing
    
    // Session management
    private LocalDateTime sessionStartTime;
    private Timeline sessionTimer;
    // formatter reserved for future use when persisting/formatting times
    // private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        startSession();
    }
    
    private void setupEventHandlers() {
        // Search functionality
        searchField.setOnAction(e -> handleSearch());
        
        // Filter functionality
        filterButton.setOnAction(e -> handleFilter());
        
        // Sliding functionality
        leftSlideButton.setOnAction(e -> slideLeft());
        rightSlideButton.setOnAction(e -> slideRight());
        
        // Logout functionality
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogout());
        }
        
        // Add hover effects for navigation buttons
        setupButtonHoverEffects();
        
        // Food item interactions
        if (favoriteButton1 != null) favoriteButton1.setOnAction(e -> handleFavorite(1));
        if (favoriteButton2 != null) favoriteButton2.setOnAction(e -> handleFavorite(2));
        if (favoriteButton3 != null) favoriteButton3.setOnAction(e -> handleFavorite(3));
        if (favoriteButton4 != null) favoriteButton4.setOnAction(e -> handleFavorite(4));
        
        if (addToCartButton1 != null) addToCartButton1.setOnAction(e -> handleAddToCart(1));
        if (addToCartButton2 != null) addToCartButton2.setOnAction(e -> handleAddToCart(2));
        if (addToCartButton3 != null) addToCartButton3.setOnAction(e -> handleAddToCart(3));
        if (addToCartButton4 != null) addToCartButton4.setOnAction(e -> handleAddToCart(4));

        if (cartButton != null) cartButton.setOnAction(e -> navigateToCart());
        
        // Category navigation
        if (foodCategoryButton != null) foodCategoryButton.setOnMouseClicked(e -> navigateToFoodItems());
        if (storesCategoryButton != null) storesCategoryButton.setOnMouseClicked(e -> navigateToShops());
        if (reportButton != null) reportButton.setOnMouseClicked(e -> navigateToReport());
        if (profileButton != null) profileButton.setOnMouseClicked(e -> navigateToProfile());
    }
    
    private void startSession() {
        sessionStartTime = LocalDateTime.now();
        updateSessionTime();
        
        // Create timer to update session time every second
        sessionTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateSessionTime()));
        sessionTimer.setCycleCount(Timeline.INDEFINITE);
        sessionTimer.play();
        
        System.out.println("Session started at: " + sessionStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    private void updateSessionTime() {
        if (sessionTimeLabel != null && sessionStartTime != null) {
            LocalDateTime now = LocalDateTime.now();
            long seconds = java.time.Duration.between(sessionStartTime, now).getSeconds();
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            
            String sessionTime = String.format("Session: %02d:%02d:%02d", hours, minutes, secs);
            sessionTimeLabel.setText(sessionTime);
        }
    }
    
    private void setupButtonHoverEffects() {
        // Left button hover effects
        if (leftSlideButton != null) {
            leftSlideButton.setOnMouseEntered(e -> {
                if (!leftSlideButton.isDisabled()) {
                    leftSlideButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
                }
            });
            leftSlideButton.setOnMouseExited(e -> {
                if (!leftSlideButton.isDisabled()) {
                    leftSlideButton.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
                }
            });
        }
        
        // Right button hover effects
        if (rightSlideButton != null) {
            rightSlideButton.setOnMouseEntered(e -> {
                if (!rightSlideButton.isDisabled()) {
                    rightSlideButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
                }
            });
            rightSlideButton.setOnMouseExited(e -> {
                if (!rightSlideButton.isDisabled()) {
                    rightSlideButton.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
                }
            });
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        displayUserInfo();
    }
    
    private void displayUserInfo() {
        if (currentUser != null && userNameLabel != null && userTypeLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
            userTypeLabel.setText(currentUser.getUserCategory().toUpperCase());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            System.out.println("Searching for: " + searchTerm);
            // TODO: Implement search functionality
            showAlert("Search", "Searching for: " + searchTerm);
        }
    }
    
    @FXML
    private void handleFilter() {
        System.out.println("Filter button clicked");
        showAlert("Filter", "Filter options will be displayed here");
    }
    
    @FXML
    private void handleLogout() {
        // Calculate session duration
        LocalDateTime sessionEndTime = LocalDateTime.now();
        long sessionDuration = java.time.Duration.between(sessionStartTime, sessionEndTime).getSeconds();
        
        // Show logout confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Session duration: " + formatDuration(sessionDuration));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performLogout();
            }
        });
    }
    
    private void performLogout() {
        // Stop the session timer
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        
        // Log session information
        LocalDateTime sessionEndTime = LocalDateTime.now();
        long sessionDuration = java.time.Duration.between(sessionStartTime, sessionEndTime).getSeconds();
        
        System.out.println("User logged out: " + (currentUser != null ? currentUser.getFullName() : "Unknown"));
        System.out.println("Session ended at: " + sessionEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Total session duration: " + formatDuration(sessionDuration));
        
        // Clear session data
        currentUser = null;
        sessionStartTime = null;
        sessionTimer = null;
        
        // Navigate back to home page
        navigateToHome();
    }
    
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, secs);
        } else {
            return String.format("%d seconds", secs);
        }
    }
    
    private void navigateToHome() {
        try {
            // Load the home FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();
            
            System.out.println("Successfully logged out and navigated to home");
            
        } catch (IOException e) {
            System.err.println("Error navigating to home: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to home page: " + e.getMessage());
        }
    }
    
    @FXML
    private void slideLeft() {
        if (currentCardIndex > 0) {
            currentCardIndex--;
            updateSliderPosition();
            System.out.println("Sliding left to card " + (currentCardIndex + 1));
        }
    }
    
    @FXML
    private void slideRight() {
        if (currentCardIndex < totalCards - 1) {
            currentCardIndex++;
            updateSliderPosition();
            System.out.println("Sliding right to card " + (currentCardIndex + 1));
        }
    }
    
    private void updateSliderPosition() {
        if (foodSliderPane != null) {
            // Calculate the exact position for the current card
            double maxScroll = foodSliderContainer.getWidth() - foodSliderPane.getWidth();
            double targetScroll = currentCardIndex * cardWidth;
            
            // Ensure we don't scroll beyond the available content
            targetScroll = Math.min(targetScroll, maxScroll);
            targetScroll = Math.max(0, targetScroll);
            
            // Set the scroll position
            foodSliderPane.setHvalue(targetScroll / maxScroll);
            
            // Update button states
            updateNavigationButtons();
        }
    }
    
    private void updateNavigationButtons() {
        // Disable left button if at first card
        if (leftSlideButton != null) {
            leftSlideButton.setDisable(currentCardIndex == 0);
            if (currentCardIndex == 0) {
                leftSlideButton.setOpacity(0.5);
                leftSlideButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: default; -fx-effect: none; -fx-min-width: 40; -fx-min-height: 40;");
            } else {
                leftSlideButton.setOpacity(1.0);
                leftSlideButton.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
            }
        }
        
        // Disable right button if at last card
        if (rightSlideButton != null) {
            rightSlideButton.setDisable(currentCardIndex == totalCards - 1);
            if (currentCardIndex == totalCards - 1) {
                rightSlideButton.setOpacity(0.5);
                rightSlideButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: default; -fx-effect: none; -fx-min-width: 40; -fx-min-height: 40;");
            } else {
                rightSlideButton.setOpacity(1.0);
                rightSlideButton.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
            }
        }
    }
    
    private void handleFavorite(int itemNumber) {
        System.out.println("Favorite button clicked for item " + itemNumber);
        showAlert("Favorite", "Added item " + itemNumber + " to favorites!");
    }
    
    private void handleAddToCart(int itemNumber) {
        // Demo mapping: itemNumber 1..4 -> existing sample item ids (1..4)
        int itemId = itemNumber; // adjust when loading items dynamically
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) { showAlert("Cart", "You must be signed in to add items to cart."); return; }
        try {
            new com.unieats.dao.CartDao().addToCart(userId, itemId, 1);
            showAlert("Cart", "Added to cart!");
        } catch (Exception ex) {
            showAlert("Cart Error", ex.getMessage());
        }
    }

    private void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Parent root = loader.load();
            com.unieats.controllers.CartController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUserId(currentUser.getId());
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Cart");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }
    
    private void navigateToFoodItems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_items.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Food Items");
            stage.show();
            
            // Set the current user in the food items controller
            FoodItemsController foodItemsController = loader.getController();
            if (foodItemsController != null) {
                foodItemsController.setCurrentUser(currentUser);
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to food items: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to food items: " + e.getMessage());
        }
    }
    
    private void navigateToShops() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shops.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Shops");
            stage.show();
            
            // Set the current user in the shops controller
            ShopsController shopsController = loader.getController();
            if (shopsController != null) {
                shopsController.setCurrentUser(currentUser);
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to shops: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to shops: " + e.getMessage());
        }
    }
    
    private void navigateToReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/report.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Submit Report");
            stage.show();
            
            // Set the current user in the report controller
            ReportController reportController = loader.getController();
            if (reportController != null) {
                reportController.setCurrentUser(currentUser);
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to report: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to report: " + e.getMessage());
        }
    }
    
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Profile");
            stage.show();
            
            // Set the current user in the profile controller
            ProfileController profileController = loader.getController();
            if (profileController != null) {
                profileController.setCurrentUser(currentUser);
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to profile: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to profile: " + e.getMessage());
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