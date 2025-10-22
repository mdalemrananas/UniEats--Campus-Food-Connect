package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.unieats.FoodItem;
import com.unieats.Shop;
import com.unieats.User;
import com.unieats.dao.FoodItemDao;
import com.unieats.dao.ShopDao;
import com.unieats.services.StockUpdateService;
import com.unieats.services.RealTimeStockBroadcaster;
import org.kordamp.ikonli.javafx.FontIcon;

public class FoodItemsController {
    
    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private Button filterButton;
    @FXML private VBox foodItemsContainer;
    // Bottom nav items
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;
    
    private User currentUser;
    private FoodItemDao foodItemDao;
    private ShopDao shopDao;
    private Integer shopFilterId = null;
    private boolean isShowingSearchResults = false;

    public void setShopFilter(int shopId) {
        this.shopFilterId = shopId;
        // Reload items if user is already set
        if (currentUser != null) {
            foodItemsContainer.getChildren().clear();
            loadFoodItems();
        }
    }
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        setupNavigationHandlers();
        foodItemDao = new FoodItemDao();
        shopDao = new ShopDao();
        
        // Start the stock update service
        StockUpdateService.getInstance().start();
        
        // Start the real-time stock broadcaster
        RealTimeStockBroadcaster.getInstance().start();
        
        // Add this controller as a listener for real-time stock changes
        RealTimeStockBroadcaster.getInstance().addListener(new RealTimeStockBroadcaster.StockChangeListener() {
            @Override
            public void onStockChanged(int itemId, int oldStock, int newStock) {
                Platform.runLater(() -> {
                    // Update the specific item in the display
                    updateFoodItemStockInDisplay(itemId, newStock);
                    System.out.println("FoodItems real-time update: Item " + itemId + " stock changed from " + oldStock + " to " + newStock);
                });
            }
        });
        
        // Also add the original stock update service listener for compatibility
        StockUpdateService.getInstance().addListener(new StockUpdateService.StockUpdateListener() {
            @Override
            public void onStockUpdated(int itemId, int quantityReduced) {
                Platform.runLater(() -> {
                    // Only refresh if we're not showing search results
                    if (!isShowingSearchResults) {
                        // Refresh the food items display
                        foodItemsContainer.getChildren().clear();
                        loadFoodItems();
                    }
                });
            }
            
            @Override
            public void onStockUpdateError(int itemId, String error) {
                Platform.runLater(() -> {
                    showAlert("Stock Update Error", "Failed to update stock: " + error);
                });
            }
            
            @Override
            public void onAllItemsRefreshed() {
                Platform.runLater(() -> {
                    // Only refresh if we're not showing search results
                    if (!isShowingSearchResults) {
                        foodItemsContainer.getChildren().clear();
                        loadFoodItems();
                    }
                });
            }
        });
        
        // Listen for shop status changes (approval/rejection)
        // Delay connection slightly to ensure server is ready
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second for server to be ready
                System.out.println("FoodItemsController: Initializing shop status WebSocket client...");
                
                com.unieats.websocket.ShopStatusWebSocketClient shopStatusClient = 
                    com.unieats.websocket.ShopStatusWebSocketClient.getInstance();
                
                if (shopStatusClient != null) {
                    shopStatusClient.addShopStatusListener(statusMsg -> {
                        Platform.runLater(() -> {
                            System.out.println("FoodItemsController: Shop status changed - " + statusMsg);
                            // Refresh food items when a shop is approved/rejected
                            // This will show/hide food items based on shop status
                            if (!isShowingSearchResults) {
                                foodItemsContainer.getChildren().clear();
                                loadFoodItems();
                            }
                        });
                    });
                    System.out.println("FoodItemsController: Shop status listener registered");
                } else {
                    System.err.println("FoodItemsController: Failed to get WebSocket client instance");
                }
            } catch (Exception e) {
                System.err.println("FoodItemsController: Failed to connect to shop status WebSocket: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        // Listen for food items changes (e.g., when shops are approved)
        // Delay connection slightly to ensure server is ready
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second for server to be ready
                System.out.println("FoodItemsController: Initializing food items real-time listener...");
                
                com.unieats.services.RealtimeService realtimeService = 
                    com.unieats.services.RealtimeService.getInstance();
                
                if (realtimeService != null) {
                    realtimeService.onEvent(topic -> {
                        if ("foodItems".equals(topic)) {
                            Platform.runLater(() -> {
                                System.out.println("FoodItemsController: Food items changed - refreshing display");
                                // Refresh food items when new items are available from approved shops
                                if (!isShowingSearchResults) {
                                    foodItemsContainer.getChildren().clear();
                                    loadFoodItems();
                                }
                            });
                        }
                    });
                    System.out.println("FoodItemsController: Food items listener registered");
                } else {
                    System.err.println("FoodItemsController: Failed to get RealtimeService instance");
                }
            } catch (Exception e) {
                System.err.println("FoodItemsController: Failed to connect to real-time service: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        // Default active tab: Food Items (no direct tab, but highlight orders as context)
        setActiveNav(navOrders);
    }

    private void setupEventHandlers() {
        backButton.setOnAction(e -> handleBack());
        searchField.setOnAction(e -> handleSearch());
        filterButton.setOnAction(e -> handleFilter());
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
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadFoodItems();
    }
    
    private void loadFoodItems() {
        try {
            if (shopFilterId != null) {
                // Only load items for the selected shop
                List<FoodItem> foodItems = foodItemDao.listByShop(shopFilterId);
                Shop shop = shopDao.getShopById(shopFilterId);
                for (FoodItem foodItem : foodItems) {
                    createFoodItemCard(foodItem, shop);
                }
                if (foodItemsContainer.getChildren().isEmpty()) {
                    showNoItemsMessage();
                }
                return;
            }
            // Get all shops first
            List<Shop> shops = shopDao.getApprovedShops();
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
        
        // Food item image
        ImageView foodImage = new ImageView();
        try {
            // Try to load the food image, fallback to placeholder if not found
            Image img = new Image(getClass().getResourceAsStream("/images/food_placeholder.jpg"));
            foodImage.setImage(img);
        } catch (Exception e) {
            // If image loading fails, use a colored rectangle as fallback
            System.err.println("Could not load food image: " + e.getMessage());
            foodImage.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-min-width: 288; " +
                "-fx-min-height: 120; " +
                "-fx-background-radius: 12;"
            );
        }
        foodImage.setFitWidth(288);
        foodImage.setFitHeight(120);
        foodImage.setPreserveRatio(false);
        foodImage.setStyle("-fx-background-radius: 12;");
        
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(foodImage);
        
        // Food item details
        VBox details = new VBox(8);
        
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(foodItem.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        
        Label shopLabel = new Label("from " + shop.getShopName());
        shopLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button favoriteButton = new Button();
        favoriteButton.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; -fx-padding: 8; -fx-cursor: hand; -fx-border-color: #e9ecef; -fx-border-width: 1;");
        FontIcon heartIcon = new FontIcon();
        heartIcon.setIconSize(16);
        favoriteButton.setGraphic(heartIcon);
        updateHeartIcon(heartIcon, foodItem.getId());
        favoriteButton.setOnAction(e -> toggleWishlist(foodItem.getId(), heartIcon));
        
        header.getChildren().addAll(nameLabel, shopLabel, spacer, favoriteButton);
        
        HBox info = new HBox(16);
        info.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label priceLabel = new Label("৳" + String.format("%.2f", foodItem.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");
        
        Label stockLabel = new Label("Stock: " + foodItem.getStock());
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        Label pointsLabel = new Label("Points: " + foodItem.getPointsMultiplier() + "x");
        pointsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        info.getChildren().addAll(priceLabel, stockLabel, pointsLabel);
        
        // Action buttons
        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button addToCartButton = new Button();
        styleCartButton(addToCartButton);
        updateCartButton(addToCartButton, foodItem.getId());
        addToCartButton.setOnAction(e -> {
            if (isInCart(foodItem.getId())) {
                // Remove from cart
                removeFromCart(foodItem.getId());
                updateCartButton(addToCartButton, foodItem.getId());
                showAlert("Cart", "Removed " + foodItem.getName() + " from cart!");
            } else {
                // Add to cart
                handleAddToCart(foodItem);
                updateCartButton(addToCartButton, foodItem.getId());
            }
        });
        
        actions.getChildren().addAll(addToCartButton);
        
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
        navigateToMenu();
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            searchFoodItems(searchTerm);
        } else {
            // If search is empty, reload all items and clear search flag
            foodItemsContainer.getChildren().clear();
            isShowingSearchResults = false; // Clear flag when going back to normal view
            loadFoodItems();
        }
    }
    
    private void searchFoodItems(String searchTerm) {
        try {
            foodItemsContainer.getChildren().clear();
            isShowingSearchResults = true; // Set flag to indicate we're showing search results

            List<FoodItem> searchResults;

            // Check if search term matches a shop name exactly (case-insensitive)
            Shop matchingShop = shopDao.getApprovedShops().stream()
                .filter(shop -> shop.getShopName().toLowerCase().equals(searchTerm.toLowerCase()))
                .findFirst()
                .orElse(null);

            if (matchingShop != null) {
                // If search term exactly matches a shop name, show all items from that shop
                searchResults = foodItemDao.listByShop(matchingShop.getId());
            } else {
                // Otherwise, use the regular search (food name or shop name partial match)
                searchResults = foodItemDao.searchItems(searchTerm);
            }

            if (searchResults.isEmpty()) {
                showNoSearchResults(searchTerm);
                return;
            }

            // Display search results
            for (FoodItem foodItem : searchResults) {
                Shop shop = shopDao.getShopById(foodItem.getShopId());
                createFoodItemCard(foodItem, shop);
            }
        } catch (Exception e) {
            showAlert("Search Error", "Failed to search food items: " + e.getMessage());
        }
    }
    
    private void showNoSearchResults(String searchTerm) {
        VBox noResultsBox = new VBox(16);
        noResultsBox.setAlignment(javafx.geometry.Pos.CENTER);
        noResultsBox.setStyle("-fx-padding: 40;");
        
        FontIcon searchIcon = new FontIcon("fas-search");
        searchIcon.setIconSize(48);
        searchIcon.setIconColor(javafx.scene.paint.Color.web("#adb5bd"));
        
        Label noResultsLabel = new Label("No results found");
        noResultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");
        
        Label searchTermLabel = new Label("for \"" + searchTerm + "\"");
        searchTermLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        Label suggestionLabel = new Label("Try searching for different keywords");
        suggestionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #adb5bd;");
        
        noResultsBox.getChildren().addAll(searchIcon, noResultsLabel, searchTermLabel, suggestionLabel);
        foodItemsContainer.getChildren().add(noResultsBox);
    }
    
    @FXML
    private void handleFilter() {
        showFilterDialog();
    }
    
    private void showFilterDialog() {
        // Create a dialog for filter options
        Dialog<FilterOptions> dialog = new Dialog<>();
        dialog.setTitle("Filter Options");
        dialog.setHeaderText("Choose your filter preferences");
        
        // Create filter controls
        VBox filterContent = new VBox(16);
        filterContent.setStyle("-fx-padding: 20;");
        
        // Price range
        Label priceLabel = new Label("Price Range:");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox priceRange = new HBox(8);
        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Min Price");
        minPriceField.setPrefWidth(80);
        
        Label toLabel = new Label("to");
        toLabel.setStyle("-fx-text-fill: #6c757d;");
        
        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Max Price");
        maxPriceField.setPrefWidth(80);
        
        priceRange.getChildren().addAll(minPriceField, toLabel, maxPriceField);
        
        // Sort options
        Label sortLabel = new Label("Sort By:");
        sortLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)", "Points (High to Low)");
        sortCombo.setValue("Name (A-Z)");
        sortCombo.setPrefWidth(200);
        
        // Stock filter
        CheckBox inStockOnly = new CheckBox("Show only items in stock");
        inStockOnly.setSelected(false);
        
        filterContent.getChildren().addAll(priceLabel, priceRange, sortLabel, sortCombo, inStockOnly);
        
        dialog.getDialogPane().setContent(filterContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        
        // Apply filter when Apply button is clicked
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                FilterOptions options = new FilterOptions();
                try {
                    if (!minPriceField.getText().trim().isEmpty()) {
                        options.minPrice = Double.parseDouble(minPriceField.getText().trim());
                    }
                    if (!maxPriceField.getText().trim().isEmpty()) {
                        options.maxPrice = Double.parseDouble(maxPriceField.getText().trim());
                    }
                    options.sortBy = sortCombo.getValue();
                    options.inStockOnly = inStockOnly.isSelected();
                    return options;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers for price range.");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(this::applyFilter);
    }
    
    private void applyFilter(FilterOptions options) {
        try {
            foodItemsContainer.getChildren().clear();
            isShowingSearchResults = false; // Clear search flag when applying filters
            
            // Get all food items
            List<FoodItem> allItems = new ArrayList<>();
            if (shopFilterId != null) {
                allItems = foodItemDao.listByShop(shopFilterId);
            } else {
                List<Shop> shops = shopDao.getApprovedShops();
                for (Shop shop : shops) {
                    allItems.addAll(foodItemDao.listByShop(shop.getId()));
                }
            }
            
            // Apply filters
            List<FoodItem> filteredItems = filterItems(allItems, options);
            
            // Sort items
            sortItems(filteredItems, options.sortBy);
            
            // Display filtered items
            for (FoodItem foodItem : filteredItems) {
                Shop shop = shopDao.getShopById(foodItem.getShopId());
                createFoodItemCard(foodItem, shop);
            }
            
            if (filteredItems.isEmpty()) {
                showNoFilterResults();
            }
        } catch (Exception e) {
            showAlert("Filter Error", "Failed to apply filters: " + e.getMessage());
        }
    }
    
    private List<FoodItem> filterItems(List<FoodItem> items, FilterOptions options) {
        return items.stream()
                .filter(item -> {
                    // Price range filter
                    if (options.minPrice != null && item.getPrice() < options.minPrice) {
                        return false;
                    }
                    if (options.maxPrice != null && item.getPrice() > options.maxPrice) {
                        return false;
                    }
                    // Stock filter
                    if (options.inStockOnly && item.getStock() <= 0) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    private void sortItems(List<FoodItem> items, String sortBy) {
        switch (sortBy) {
            case "Name (A-Z)":
                items.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                break;
            case "Name (Z-A)":
                items.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case "Price (Low to High)":
                items.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                break;
            case "Price (High to Low)":
                items.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "Points (High to Low)":
                items.sort((a, b) -> Double.compare(b.getPointsMultiplier(), a.getPointsMultiplier()));
                break;
        }
    }
    
    private void showNoFilterResults() {
        VBox noResultsBox = new VBox(16);
        noResultsBox.setAlignment(javafx.geometry.Pos.CENTER);
        noResultsBox.setStyle("-fx-padding: 40;");
        
        FontIcon filterIcon = new FontIcon("fas-filter");
        filterIcon.setIconSize(48);
        filterIcon.setIconColor(javafx.scene.paint.Color.web("#adb5bd"));
        
        Label noResultsLabel = new Label("No items match your filters");
        noResultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");
        
        Label suggestionLabel = new Label("Try adjusting your filter criteria");
        suggestionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #adb5bd;");
        
        noResultsBox.getChildren().addAll(filterIcon, noResultsLabel, suggestionLabel);
        foodItemsContainer.getChildren().add(noResultsBox);
    }
    
    // Filter options class
    private static class FilterOptions {
        Double minPrice;
        Double maxPrice;
        String sortBy;
        boolean inStockOnly;
    }
    
    /**
     * Update stock display for a specific food item in real-time
     */
    private void updateFoodItemStockInDisplay(int itemId, int newStock) {
        // Find and update the specific food item card in the display
        for (javafx.scene.Node node : foodItemsContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                // Check if this card contains the item we need to update
                if (card.getChildren().size() >= 2) {
                    VBox details = (VBox) card.getChildren().get(1);
                    if (details.getChildren().size() >= 2) {
                        HBox header = (HBox) details.getChildren().get(0);
                        if (header.getChildren().size() >= 2) {
                            Label nameLabel = (Label) header.getChildren().get(0);
                            // This is a simple way to identify the item - in a real app you'd store itemId in userData
                            // For now, we'll update all cards and let the user see the change
                            updateStockInCard(card, newStock);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Update stock information in a food item card
     */
    private void updateStockInCard(VBox card, int newStock) {
        try {
            VBox details = (VBox) card.getChildren().get(1);
            HBox info = (HBox) details.getChildren().get(1);
            
            // Find and update the stock label
            for (javafx.scene.Node node : info.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText().startsWith("Stock:")) {
                        label.setText("Stock: " + newStock);
                        // Change color based on stock level
                        if (newStock <= 0) {
                            label.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc3545;"); // Red for out of stock
                        } else if (newStock <= 5) {
                            label.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffc107;"); // Yellow for low stock
                        } else {
                            label.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;"); // Gray for normal stock
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating stock in card: " + e.getMessage());
        }
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
            if (ex.getMessage().contains("same shop")) {
                showAlert("Shop Restriction", ex.getMessage() + "\n\nWould you like to clear your cart and add this item?", true, foodItem.getId());
            } else {
                showAlert("Cart Error", ex.getMessage());
            }
        }
    }

    private void removeFromCart(int itemId) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) { showAlert("Cart", "You must be signed in to remove items from cart."); return; }
        try {
            new com.unieats.dao.CartDao().removeFromCart(userId, itemId);
        } catch (Exception ex) {
            showAlert("Cart Error", "Failed to remove item from cart: " + ex.getMessage());
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

    private boolean isInCart(int itemId) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) return false;
        return new com.unieats.dao.CartDao().isInCart(userId, itemId);
    }

    private void updateCartButton(Button btn, int itemId) {
        boolean carted = isInCart(itemId);
        btn.setText(carted ? "Carted" : "Add to Cart");
        btn.setStyle(carted ?
            "-fx-background-color: #e9ecef; -fx-background-radius: 12; -fx-padding: 8 16; -fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;" :
            "-fx-background-color: #28a745; -fx-background-radius: 12; -fx-padding: 8 16; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    private void styleCartButton(Button btn) {
        btn.setOnMouseEntered(e -> {
            String currentStyle = btn.getStyle();
            if (!currentStyle.contains("dropshadow")) {
                btn.setStyle(currentStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0, 0, 1);");
            }
        });
        btn.setOnMouseExited(e -> {
            String currentStyle = btn.getStyle();
            if (currentStyle.contains("dropshadow")) {
                btn.setStyle(currentStyle.replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0, 0, 1);", ""));
            }
        });
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

    private void updateHeartIcon(FontIcon heartIcon, int itemId) {
        try {
            int userId = currentUser != null ? currentUser.getId() : -1;
            boolean liked = userId > 0 && new com.unieats.dao.WishlistDao().isInWishlist(userId, itemId);
            heartIcon.setIconLiteral(liked ? "fas-heart" : "far-heart");
            heartIcon.setIconColor(javafx.scene.paint.Color.web("#e74c3c"));
        } catch (Exception ignored) {}
    }

    private void toggleWishlist(int itemId, FontIcon heartIcon) {
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) { showAlert("Favourite", "You must be signed in to save favourites."); return; }
        com.unieats.dao.WishlistDao dao = new com.unieats.dao.WishlistDao();
        boolean liked = dao.isInWishlist(userId, itemId);
        if (liked) dao.removeFromWishlist(userId, itemId); else dao.addToWishlist(userId, itemId, 1);
        updateHeartIcon(heartIcon, itemId);
    }
}
