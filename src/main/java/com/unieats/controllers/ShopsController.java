package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.List;
import com.unieats.Shop;
import com.unieats.User;
import com.unieats.dao.ShopDao;
import com.unieats.dao.FoodItemDao;
import org.kordamp.ikonli.javafx.FontIcon;

public class ShopsController {
    
    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private Button filterButton;
    @FXML private VBox shopsContainer;
    // Bottom nav items
    @FXML private VBox navHome;
    @FXML private VBox navOrders;
    @FXML private VBox navCart;
    @FXML private VBox navFav;
    @FXML private VBox navProfile;
    
    private User currentUser;
    private ShopDao shopDao;
    private FoodItemDao foodItemDao;
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        setupNavigationHandlers();
        shopDao = new ShopDao();
        foodItemDao = new FoodItemDao();
        // Default active tab: Shops (no direct tab, but highlight orders as context)
        if (navOrders != null) setActiveNav(navOrders);
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
    
    private void setupEventHandlers() {
        backButton.setOnAction(e -> handleBack());
        searchField.setOnAction(e -> handleSearch());
        filterButton.setOnAction(e -> handleFilter());
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadShops();
    }
    
    private void loadShops() {
        try {
            List<Shop> shops = shopDao.getApprovedShops();
            
            if (shops.isEmpty()) {
                showNoShopsMessage();
            } else {
                for (Shop shop : shops) {
                    createShopCard(shop);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading shops: " + e.getMessage());
            showAlert("Error", "Failed to load shops: " + e.getMessage());
        }
    }
    
    private void createShopCard(Shop shop) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-padding: 16; -fx-min-width: 320;");
        
        // Shop header
        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Shop icon
        StackPane iconContainer = new StackPane();
        iconContainer.setStyle("-fx-background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%); -fx-background-radius: 12; -fx-pref-height: 60; -fx-pref-width: 60;");
        FontIcon shopIcon = new FontIcon("fas-store");
        shopIcon.setIconSize(24);
        shopIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        iconContainer.getChildren().add(shopIcon);
        
        // Shop info
        VBox shopInfo = new VBox(4);
        Label nameLabel = new Label(shop.getShopName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        
        Label statusLabel = new Label("Status: " + shop.getStatus().toUpperCase());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        shopInfo.getChildren().addAll(nameLabel, statusLabel);
        
        header.getChildren().addAll(iconContainer, shopInfo);
        
        // Shop details
        VBox details = new VBox(8);
        
        Label idLabel = new Label("Shop ID: " + shop.getId());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        Label ownerLabel = new Label("Owner ID: " + shop.getOwnerId());
        ownerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        if (shop.getCreatedAt() != null) {
            Label createdLabel = new Label("Established: " + shop.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            createdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
            details.getChildren().add(createdLabel);
        }
        
        details.getChildren().addAll(idLabel, ownerLabel);
        
        // Action buttons
        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button viewMenuButton = new Button("View Menu");
        viewMenuButton.setStyle("-fx-background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 12; -fx-cursor: hand;");
        viewMenuButton.setOnAction(e -> handleViewMenu(shop));
        
        Button detailsButton = new Button("Details");
        detailsButton.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #2d3436; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: #e9ecef; -fx-border-width: 1;");
        detailsButton.setOnAction(e -> openShopDetails(shop));
        
        actions.getChildren().addAll(detailsButton, viewMenuButton);
        
        card.getChildren().addAll(header, details, actions);
        shopsContainer.getChildren().add(card);
    }
    
    private void showNoShopsMessage() {
        VBox noShopsBox = new VBox(16);
        noShopsBox.setAlignment(javafx.geometry.Pos.CENTER);
        noShopsBox.setStyle("-fx-padding: 40;");
        
        FontIcon noShopsIcon = new FontIcon("fas-store");
        noShopsIcon.setIconSize(48);
        noShopsIcon.setIconColor(javafx.scene.paint.Color.web("#6c757d"));
        
        Label noShopsLabel = new Label("No shops available");
        noShopsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        
        noShopsBox.getChildren().addAll(noShopsIcon, noShopsLabel);
        shopsContainer.getChildren().add(noShopsBox);
    }
    
    @FXML
    private void handleBack() {
        navigateToMenu();
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
    
    private void handleViewMenu(Shop shop) {
        try {
            // Load the food items FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_items.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Create new responsive scene and set it
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();

            // Set the current user and shop filter in the food items controller
            FoodItemsController foodItemsController = loader.getController();
            if (foodItemsController != null) {
                foodItemsController.setCurrentUser(currentUser);
                try {
                    foodItemsController.getClass().getMethod("setShopFilter", int.class).invoke(foodItemsController, shop.getId());
                } catch (Exception ignored) {}
            }

        } catch (IOException e) {
            System.err.println("Error navigating to food items: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to food items: " + e.getMessage());
        }
    }
    
    private void handleContact(Shop shop) {
        showAlert("Contact", "Contact information for " + shop.getShopName() + " will be displayed here.");
    }

    private void openShopDetails(Shop shop) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shop_details.fxml"));
            Parent root = loader.load();
            ShopDetailsController controller = loader.getController();
            if (controller != null) controller.setData(currentUser, shop);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to open shop details: " + e.getMessage());
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
