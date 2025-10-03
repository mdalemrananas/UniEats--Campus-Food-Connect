package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import com.unieats.User;
import com.unieats.FoodItem;
import com.unieats.Shop;
import com.unieats.dao.FoodItemDao;
import com.unieats.dao.ShopDao;
import org.kordamp.ikonli.javafx.FontIcon;

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
    @FXML private Label rewardPointsLabel;
    @FXML private VBox foodCategoryButton;
    @FXML private VBox storesCategoryButton;
    @FXML private VBox reportButton;
    // Bottom nav items
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;
    @FXML private VBox foodItemsContainer; // Container for the food items
    @FXML private HBox pageIndicators; // Container for page indicators
    @FXML private ProgressIndicator loadingIndicator; // Loading indicator
    
    private final FoodItemDao foodItemDao = new FoodItemDao();
    private final ShopDao shopDao = new ShopDao();
    private static final int ITEMS_PER_PAGE = 1; // Show one item at a time
    private static final int AUTO_SWITCH_DELAY = 4; // seconds
    private Timeline carouselTimeline;
    private int currentPage = 0;
    private int totalPages = 0;
    private List<FoodItem> allFoodItems = new ArrayList<>();

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
        // Default active tab is Home
        setActiveNav(navHome);
        
        // Load random food items
        loadRandomFoodItems();
    }

    private void setupEventHandlers() {
        // Search functionality
        searchField.setOnAction(e -> handleSearch());

        // Filter functionality
        if (filterButton != null) {
            filterButton.setOnAction(e -> handleFilter());
        }

        // Sliding functionality - make buttons optional
        if (leftSlideButton != null) {
            leftSlideButton.setOnAction(e -> slideLeft());
        }
        if (rightSlideButton != null) {
            rightSlideButton.setOnAction(e -> slideRight());
        }

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
        if (reportButton != null) {
            reportButton.setOnMouseClicked(e -> navigateToReport());
            reportButton.setStyle("-fx-cursor: hand;");
        }
        // Bottom navigation handlers
        if (navHome != null) navHome.setOnMouseClicked(e -> { setActiveNav(navHome); /* already on menu */ });
        if (navOrders != null) navOrders.setOnMouseClicked(e -> { setActiveNav(navOrders); navigateToOrders(); });
        if (navCart != null) navCart.setOnMouseClicked(e -> { setActiveNav(navCart); navigateToCart(); });
        if (navFav != null) navFav.setOnMouseClicked(e -> { setActiveNav(navFav); navigateToFavorites(); });
        if (navProfile != null) navProfile.setOnMouseClicked(e -> { setActiveNav(navProfile); navigateToProfile(); });
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

    // Resolve current Stage from any available node to avoid NPE when optional controls are not present
    private javafx.stage.Stage findStage() {
        try {
            if (logoutButton != null && logoutButton.getScene() != null) return (javafx.stage.Stage) logoutButton.getScene().getWindow();
        } catch (Exception ignored) {}
        try {
            if (navProfile != null && navProfile.getScene() != null) return (javafx.stage.Stage) navProfile.getScene().getWindow();
        } catch (Exception ignored) {}
        try {
            if (navHome != null && navHome.getScene() != null) return (javafx.stage.Stage) navHome.getScene().getWindow();
        } catch (Exception ignored) {}
        try {
            if (cartButton != null && cartButton.getScene() != null) return (javafx.stage.Stage) cartButton.getScene().getWindow();
        } catch (Exception ignored) {}
        return null;
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
        loadRewardPoints();
    }

    private void displayUserInfo() {
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getFullName());
        }
        if (currentUser != null && userTypeLabel != null) {
            userTypeLabel.setText(currentUser.getUserCategory().toUpperCase());
        }
    }

    private void loadRewardPoints() {
        if (currentUser == null || rewardPointsLabel == null) return;
        try {
            double total = com.unieats.RewardService.getTotalPoints(currentUser.getId());
            // Show as integer if it's a whole number
            String text = (Math.floor(total) == total) ? String.valueOf((int) total) : String.valueOf(total);
            rewardPointsLabel.setText(text);
        } catch (Exception ex) {
            System.err.println("Failed to load reward points: " + ex.getMessage());
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
        if (foodSliderContainer == null) return;
        if (currentCardIndex > 0) {
            currentCardIndex--;
            updateSliderPosition();
            System.out.println("Sliding left to card " + (currentCardIndex + 1));
        }
    }

    @FXML
    private void slideRight() {
        if (foodSliderContainer == null) return;
        if (currentCardIndex < totalCards - 1) {
            currentCardIndex++;
            updateSliderPosition();
            System.out.println("Sliding right to card " + (currentCardIndex + 1));
        }
    }

    private void updateSliderPosition() {
        if (foodSliderPane != null && foodSliderContainer != null) {
            try {
                // Calculate the exact position for the current card
                double maxScroll = foodSliderContainer.getWidth() - foodSliderPane.getWidth();
                if (maxScroll <= 0) return; // Prevent division by zero or negative values
                
                double targetScroll = currentCardIndex * cardWidth;

                // Ensure we don't scroll beyond the available content
                targetScroll = Math.min(targetScroll, maxScroll);
                targetScroll = Math.max(0, targetScroll);

                // Set the scroll position
                foodSliderPane.setHvalue(targetScroll / maxScroll);

                // Update button states
                if (leftSlideButton != null && rightSlideButton != null) {
                    updateNavigationButtons();
                }
            } catch (Exception e) {
                System.err.println("Error updating slider position: " + e.getMessage());
            }
        }
    }

    private void updateNavigationButtons() {
        // Return early if navigation buttons are not available
        if (leftSlideButton == null || rightSlideButton == null) {
            return;
        }
        
        // Update left button state
        leftSlideButton.setDisable(currentCardIndex == 0);
        if (currentCardIndex == 0) {
            leftSlideButton.setOpacity(0.5);
            leftSlideButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: default; -fx-effect: none; -fx-min-width: 40; -fx-min-height: 40;");
        } else {
            leftSlideButton.setOpacity(1.0);
            leftSlideButton.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
        }

        // Update right button state
        rightSlideButton.setDisable(currentCardIndex == totalCards - 1);
        if (currentCardIndex == totalCards - 1) {
                rightSlideButton.setOpacity(0.5);
                rightSlideButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: default; -fx-effect: none; -fx-min-width: 40; -fx-min-height: 40;");
            } else {
                rightSlideButton.setOpacity(1.0);
                rightSlideButton.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,107,53,0.3), 4, 0, 0, 2); -fx-min-width: 40; -fx-min-height: 40;");
            }
    }

    private void handleFavorite(int itemNumber) {
		int userId = currentUser != null ? currentUser.getId() : -1;
		if (userId <= 0) { showAlert("Favourite", "You must be signed in to save favorites."); return; }
		int itemId = itemNumber; // TODO: map UI card to actual item id when dynamic loading
		try {
			new com.unieats.dao.WishlistDao().addToWishlist(userId, itemId, 1);
			showAlert("Favourite", "Saved to favourites!");
		} catch (Exception ex) {
			showAlert("Favourite Error", ex.getMessage());
		}
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
            if (ex.getMessage().contains("same shop")) {
                showAlert("Shop Restriction", ex.getMessage() + "\n\nWould you like to clear your cart and add this item?", true, itemId);
            } else {
                showAlert("Cart Error", ex.getMessage());
            }
        }
    }

    private void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Parent root = loader.load();
            com.unieats.controllers.CartController controller = loader.getController();
            if (controller != null && currentUser != null) controller.setCurrentUserId(currentUser.getId());
            javafx.stage.Stage stage = findStage();
            if (stage == null) {
                showAlert("Navigation Error", "Unable to resolve current window to open Cart.");
                return;
            }
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - Cart");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", e.getMessage());
        }
    }

    private void navigateToOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_orders.fxml"));
            Parent root = loader.load();
            com.unieats.controllers.MyOrdersController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            
            Stage stage = findStage();
            if (stage == null) {
                showAlert("Navigation Error", "Unable to resolve current window to open My Orders.");
                return;
            }
            
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("UniEats - My Orders");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load My Orders page: " + e.getMessage());
        }
    }

    private void navigateToFavorites() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/wishlist.fxml"));
			Parent root = loader.load();
			com.unieats.controllers.WishlistController controller = loader.getController();
			if (controller != null && currentUser != null) controller.setCurrentUser(currentUser);
			Stage stage = findStage();
			if (stage == null) { showAlert("Navigation Error", "Unable to resolve current window to open Favourites."); return; }
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.setTitle("UniEats - Favourites");
			stage.show();
		} catch (IOException e) {
			showAlert("Navigation Error", e.getMessage());
		}
	}

    @FXML
    private void handleSeeAllClick(javafx.scene.input.MouseEvent event) {
        navigateToFoodItems();
    }
    
    @FXML
    private void showPreviousItem() {
        if (totalPages <= 1) return;
        int prevPage = (currentPage - 1 + totalPages) % totalPages;
        showPage(prevPage);
        updatePageIndicators();
        resetCarouselTimer();
    }
    
    @FXML
    private void showNextItem() {
        if (totalPages <= 1) return;
        int nextPage = (currentPage + 1) % totalPages;
        showPage(nextPage);
        updatePageIndicators();
        resetCarouselTimer();
    }
    
    private void startCarousel() {
        if (totalPages <= 1) return; // No need for carousel with one page
        
        stopCarousel();
        
        carouselTimeline = new Timeline(
            new KeyFrame(Duration.seconds(AUTO_SWITCH_DELAY), e -> {
                showNextItem();
            })
        );
        carouselTimeline.setCycleCount(Timeline.INDEFINITE);
        carouselTimeline.play();
        
        // Initialize page indicators
        updatePageIndicators();
    }
    
    private void stopCarousel() {
        if (carouselTimeline != null) {
            carouselTimeline.stop();
            carouselTimeline = null;
        }
    }
    
    private void resetCarouselTimer() {
        if (carouselTimeline != null) {
            stopCarousel();
            startCarousel();
        }
    }
    
    private void updatePageIndicators() {
        if (pageIndicators == null || totalPages <= 1) return;
        
        pageIndicators.getChildren().clear();
        
        for (int i = 0; i < totalPages; i++) {
            Circle indicator = new Circle(4);
            indicator.setFill(i == currentPage ? javafx.scene.paint.Color.web("#ff6b35") : javafx.scene.paint.Color.web("#e0e0e0"));
            indicator.setStyle("-fx-cursor: hand;");
            
            final int pageIndex = i;
            indicator.setOnMouseClicked(e -> {
                showPage(pageIndex);
                updatePageIndicators();
                resetCarouselTimer();
            });
            
            pageIndicators.getChildren().add(indicator);
            
            // Add spacing between indicators
            if (i < totalPages - 1) {
                Region spacer = new Region();
                spacer.setPrefWidth(8);
                pageIndicators.getChildren().add(spacer);
            }
        }
    }
    
    private void loadRandomFoodItems() {
        try {
            // Show loading indicator
            loadingIndicator.setVisible(true);
            foodItemsContainer.setVisible(false);
            
            // Load food items in a background thread
            new Thread(() -> {
                try {
                    // Get random food items from the database
                    allFoodItems = foodItemDao.getRandomItems(10); // Load items for carousel
                    totalPages = (int) Math.ceil((double) allFoodItems.size() / ITEMS_PER_PAGE);
                    
                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        foodItemsContainer.setVisible(true);
                        showPage(0);
                        startCarousel();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showAlert("Error", "Failed to load food items: " + e.getMessage());
                    });
                }
            }).start();
        } catch (Exception e) {
            loadingIndicator.setVisible(false);
            showAlert("Error", "Failed to load food items: " + e.getMessage());
        }
    }
    
    private void showPage(int page) {
        if (allFoodItems == null || allFoodItems.isEmpty() || page < 0 || page >= totalPages) {
            return;
        }
        
        currentPage = page;
        foodItemsContainer.getChildren().clear();
        
        int itemIndex = page; // Since we're showing one item per page
        if (itemIndex < allFoodItems.size()) {
            FoodItem item = allFoodItems.get(itemIndex);
            VBox card = createFoodCard(item);
            foodItemsContainer.getChildren().add(card);
        }
        
        // Update page indicators
        updatePageIndicators();
    }
    
    
    private VBox createFoodCard(FoodItem item) {
        VBox card = new VBox();
        card.getStyleClass().add("food-card");
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-padding: 16; " +
            "-fx-spacing: 12; " +
            "-fx-min-width: 248; " +
            "-fx-pref-width: 248; " +
            "-fx-max-width: 248; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        card.setFillWidth(true);
        
        // Food image container with fixed dimensions and shadow
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 12; " +
            "-fx-min-width: 216; " +
            "-fx-pref-width: 216; " +
            "-fx-max-width: 216; " +
            "-fx-min-height: 130; " +
            "-fx-pref-height: 130; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);"
        );
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Food image
        ImageView foodImage = new ImageView();
        try {
            // Try to load the food image, fallback to placeholder if not found
            Image img = new Image(getClass().getResourceAsStream("/images/food_placeholder.jpg"));
            foodImage.setImage(img);
            foodImage.setFitWidth(216);
            foodImage.setFitHeight(130);
            foodImage.setPreserveRatio(true);
            foodImage.setSmooth(true);
            foodImage.setStyle(
                "-fx-background-radius: 12; " +
                "-fx-cursor: hand;"
            );
            foodImage.fitWidthProperty().bind(imageContainer.widthProperty().subtract(2));
            
            // Add click handler to open food details
            foodImage.setOnMouseClicked(e -> navigateToFoodDetails(item.getId()));
            
            imageContainer.getChildren().add(foodImage);
        } catch (Exception e) {
            // If image loading fails, show a placeholder icon
            System.err.println("Could not load food image: " + e.getMessage());
            FontIcon foodIcon = new FontIcon("fas-utensils");
            foodIcon.setIconSize(32);
            foodIcon.setIconColor(javafx.scene.paint.Color.web("#adb5bd"));
            imageContainer.getChildren().add(foodIcon);
        }
        
        // Food name and shop name
        VBox nameBox = new VBox(4);
        nameBox.setStyle("-fx-padding: 8 0 0 0; -fx-alignment: center-left;");
        
        // Food name
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2d3436; " +
            "-fx-wrap-text: true;"
        );
        nameLabel.setMaxWidth(248);
        
        // Shop name
        Label shopLabel = new Label();
        try {
            Shop shop = shopDao.findById(item.getShopId());
            if (shop != null) {
                shopLabel.setText("from " + shop.getShopName());
            }
        } catch (Exception e) {
            System.err.println("Error fetching shop name: " + e.getMessage());
        }
        shopLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #6c757d;"
        );
        
        nameBox.getChildren().addAll(nameLabel, shopLabel);
        
        // Info row (Price, Stock, Points)
        HBox infoRow = new HBox();
        infoRow.setSpacing(12);
        infoRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        infoRow.setStyle("-fx-padding: 4 0 12 0;");
        
        // Price
        Label priceLabel = new Label(String.format("$%.2f", item.getPrice()));
        priceLabel.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #ff6b35;"
        );
        
        // Stock
        Label stockLabel = new Label("• " + (item.getStock() > 0 ? item.getStock() + " in stock" : "Out of stock"));
        stockLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #6c757d;"
        );
        
        // Points
        HBox pointsContainer = new HBox(4);
        pointsContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        FontIcon starIcon = new FontIcon("fas-star");
        starIcon.setIconSize(12);
        starIcon.setIconColor(javafx.scene.paint.Color.web("#ffc107"));
        
        Label pointsLabel = new Label(item.getPointsMultiplier() + "x");
        pointsLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #6c757d; " +
            "-fx-font-weight: bold;"
        );
        
        pointsContainer.getChildren().addAll(starIcon, pointsLabel);
        
        infoRow.getChildren().addAll(priceLabel, stockLabel, pointsContainer);
        
        // Button row
        HBox buttonRow = new HBox();
        buttonRow.setSpacing(12);
        buttonRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonRow.setMinWidth(216);
        buttonRow.setMaxWidth(216);
        
        // Favorite button
        Button favoriteButton = new Button("");
        FontIcon heartIcon = new FontIcon();
        heartIcon.setIconSize(18);
        heartIcon.setIconColor(javafx.scene.paint.Color.web("#e74c3c"));
        favoriteButton.setGraphic(heartIcon);
        favoriteButton.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 8; " +
            "-fx-cursor: hand; " +
            "-fx-min-width: 40; " +
            "-fx-min-height: 40;"
        );
        updateHeartIcon(heartIcon, item.getId());
        favoriteButton.setOnAction(e -> toggleWishlist(item.getId(), heartIcon));
        
        // Add to Cart button (shows Carted if already in cart)
        Button addToCartButton = new Button();
        styleCartButton(addToCartButton);
        updateCartButton(addToCartButton, item.getId());
        addToCartButton.setOnAction(e -> {
            if (isInCart(item.getId())) {
                navigateToCart();
            } else {
                handleAddToCart(item.getId());
                updateCartButton(addToCartButton, item.getId());
            }
        });
        
        buttonRow.getChildren().addAll(favoriteButton, addToCartButton);
        
        // Add all elements to card
        VBox.setVgrow(buttonRow, Priority.ALWAYS);
        VBox.setVgrow(infoRow, Priority.ALWAYS);
        
        // Create a container for the card content to ensure proper sizing
        VBox cardContent = new VBox(imageContainer, nameBox, infoRow, buttonRow);
        cardContent.setSpacing(12);
        cardContent.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        cardContent.setFillWidth(true);
        
        // Add the content to the card
        card.getChildren().add(cardContent);
        
        // Add click handler to open food details when clicking on the card
        card.setOnMouseClicked(e -> {
            if (e.getTarget() == card || e.getTarget() == nameLabel || e.getTarget() == shopLabel) {
                navigateToFoodDetails(item.getId());
            }
        });
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);",
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"
            ));
            card.setTranslateY(-2);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);", 
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            ));
            card.setTranslateY(0);
        });
        
        // Add ripple effect
        card.setOnMousePressed(e -> {
            card.setOpacity(0.9);
        });
        card.setOnMouseReleased(e -> {
            card.setOpacity(1.0);
        });
        
        return card;
    }
    
    private String getFoodIcon(String foodName) {
        // Map food names to appropriate icons
        if (foodName == null) return "fas-utensils";
        
        foodName = foodName.toLowerCase();
        if (foodName.contains("pizza")) return "fas-pizza-slice";
        if (foodName.contains("burger") || foodName.contains("hamburger")) return "fas-hamburger";
        if (foodName.contains("chicken")) return "fas-drumstick-bite";
        if (foodName.contains("salad")) return "fas-leaf";
        if (foodName.contains("sushi")) return "fas-fish";
        if (foodName.contains("pasta")) return "fas-pasta";
        if (foodName.contains("coffee") || foodName.contains("tea")) return "fas-coffee";
        if (foodName.contains("ice cream") || foodName.contains("dessert")) return "fas-ice-cream";
        
        return "fas-utensils"; // default icon
    }
    
    private void navigateToFoodDetails(int foodId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_details.fxml"));
            Parent root = loader.load();
            
            // Pass the food ID to the details controller if the controller supports it
            Object controller = loader.getController();
            try {
                // Use reflection to call setFoodItem if it exists
                controller.getClass().getMethod("setFoodItem", int.class).invoke(controller, foodId);
            } catch (Exception ex) {
                // If the controller doesn't have setFoodItem, just log it
                System.out.println("Food details controller doesn't support setFoodItem");
            }
            
            // Get the current stage and set the new scene
            Stage stage = (Stage) foodItemsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 360, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load food details: " + e.getMessage());
        }
    }
    
    private void navigateToFoodItems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_items.fxml"));
            Parent root = loader.load();

            // Get the current stage safely
            javafx.stage.Stage stage = findStage();
            if (stage == null) {
                showAlert("Navigation Error", "Unable to resolve current window to open Food Items.");
                return;
            }

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

            // Get the current stage safely
            javafx.stage.Stage stage = findStage();
            if (stage == null) {
                showAlert("Navigation Error", "Unable to resolve current window to open Shops.");
                return;
            }

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

            // Get the current stage safely
            javafx.stage.Stage stage = findStage();
            if (stage == null) {
                showAlert("Navigation Error", "Unable to resolve current window to open Report screen.");
                return;
            }

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

        } catch (Exception e) {
            System.err.println("Error navigating to report: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load report form: " + e.getMessage());
        }
    }
    
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            
            // Get the current stage safely
            javafx.stage.Stage stage = findStage();
            if (stage == null) {
                showAlert("Navigation Error", "Unable to resolve current window to open Profile screen.");
                return;
            }
            
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

    private void showAlert(String title, String content, boolean isConfirmation, int itemId) {
        if (isConfirmation) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    // Clear cart and add the new item
                    try {
                        new com.unieats.dao.CartDao().clearCart(currentUser.getId());
                        new com.unieats.dao.CartDao().addToCart(currentUser.getId(), itemId, 1);
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

    // --- Bottom Nav Active State Handling ---
    private void setActiveNav(VBox active) {
        VBox[] navItems = {navHome, navOrders, navCart, navFav, navProfile};
        for (VBox navItem : navItems) {
            if (navItem != null) {
                applyActive(navItem, navItem == active, "#ff6b35");
            }
        }
    }

    private void applyActive(VBox tab, boolean active, String colorHex) {
        if (tab == null) return;
        // children: [0] StackPane -> [0] FontIcon, [1] Label
        if (tab.getChildren().size() < 2) return;
        javafx.scene.layout.StackPane iconWrap = (javafx.scene.layout.StackPane) tab.getChildren().get(0);
        javafx.scene.control.Label label = (javafx.scene.control.Label) tab.getChildren().get(1);

        // Icon color
        if (!iconWrap.getChildren().isEmpty() && iconWrap.getChildren().get(0) instanceof org.kordamp.ikonli.javafx.FontIcon) {
            org.kordamp.ikonli.javafx.FontIcon icon = (org.kordamp.ikonli.javafx.FontIcon) iconWrap.getChildren().get(0);
            icon.setIconColor(active ? javafx.scene.paint.Paint.valueOf(colorHex) : javafx.scene.paint.Paint.valueOf("#6c757d"));
        }

        // Background accent
        String bg = active ? String.format("-fx-background-color: %s1A; -fx-background-radius: 12; -fx-padding: 8;", colorHex.replace("#",""))
                           : "-fx-background-radius: 12; -fx-padding: 8;";
        iconWrap.setStyle(bg);

        // Label color
        if (label != null) {
            label.setStyle(active ? String.format("-fx-font-size: 10px; -fx-text-fill: %s; -fx-font-weight: bold;", colorHex)
                          : "-fx-font-size: 10px; -fx-text-fill: #6c757d;");
        }
    }

    private void updateHeartIcon(org.kordamp.ikonli.javafx.FontIcon heartIcon, int itemId) {
        try {
            int userId = currentUser != null ? currentUser.getId() : -1;
            boolean liked = userId > 0 && new com.unieats.dao.WishlistDao().isInWishlist(userId, itemId);
            heartIcon.setIconLiteral(liked ? "fas-heart" : "far-heart");
        } catch (Exception ignored) {}
    }

    private void toggleWishlist(int itemId, org.kordamp.ikonli.javafx.FontIcon heartIcon) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) { showAlert("Favourite", "You must be signed in to save favourites."); return; }
        com.unieats.dao.WishlistDao dao = new com.unieats.dao.WishlistDao();
        boolean liked = dao.isInWishlist(userId, itemId);
        if (liked) dao.removeFromWishlist(userId, itemId); else dao.addToWishlist(userId, itemId, 1);
        updateHeartIcon(heartIcon, itemId);
    }

    private boolean isInCart(int itemId) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) return false;
        return new com.unieats.dao.CartDao().isInCart(userId, itemId);
    }

    private void updateCartButton(Button btn, int itemId) {
        boolean carted = isInCart(itemId);
        btn.setText(carted ? "Carted" : "Add to Cart");
        btn.setStyle(carted ?
            "-fx-background-color: #e9ecef; -fx-background-radius: 12; -fx-padding: 10 24; -fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;" :
            "-fx-background-color: #ff6b35; -fx-background-radius: 12; -fx-padding: 10 24; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    private void styleCartButton(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0, 0, 1);"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0, 0, 1);", "")));
    }
}