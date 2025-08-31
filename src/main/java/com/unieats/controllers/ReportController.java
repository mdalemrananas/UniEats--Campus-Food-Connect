package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.io.File;
import java.util.List;
import com.unieats.Shop;
import com.unieats.FoodItem;
import com.unieats.User;
import com.unieats.dao.ShopDao;
import com.unieats.dao.FoodItemDao;
import com.unieats.dao.ReportDao;
import org.kordamp.ikonli.javafx.FontIcon;

public class ReportController {
    
    @FXML private Button backButton;
    @FXML private RadioButton shopReportRadio;
    @FXML private RadioButton foodItemReportRadio;
    @FXML private ToggleGroup reportTypeGroup;
    @FXML private VBox shopSelectionBox;
    @FXML private VBox foodItemSelectionBox;
    @FXML private ComboBox<Shop> shopComboBox;
    @FXML private ComboBox<FoodItem> foodItemComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private Button uploadImageButton;
    @FXML private Label imageStatusLabel;
    @FXML private Button submitReportButton;
    
    private User currentUser;
    private ShopDao shopDao;
    private FoodItemDao foodItemDao;
    private ReportDao reportDao;
    private File selectedImageFile;
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        shopDao = new ShopDao();
        foodItemDao = new FoodItemDao();
        reportDao = new ReportDao();
        loadData();
    }
    
    private void setupEventHandlers() {
        backButton.setOnAction(e -> handleBack());
        uploadImageButton.setOnAction(e -> handleImageUpload());
        submitReportButton.setOnAction(e -> handleSubmitReport());
        
        // Report type change listener
        reportTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == shopReportRadio) {
                shopSelectionBox.setVisible(true);
                shopSelectionBox.setManaged(true);
                foodItemSelectionBox.setVisible(false);
                foodItemSelectionBox.setManaged(false);
            } else if (newValue == foodItemReportRadio) {
                shopSelectionBox.setVisible(false);
                shopSelectionBox.setManaged(false);
                foodItemSelectionBox.setVisible(true);
                foodItemSelectionBox.setManaged(true);
            }
        });
        
        // Shop selection change listener
        shopComboBox.setOnAction(e -> {
            if (foodItemReportRadio.isSelected()) {
                loadFoodItemsForShop();
            }
        });
    }
    
    private void loadData() {
        try {
            // Load shops
            List<Shop> shops = shopDao.getApprovedShops();
            ObservableList<Shop> shopList = FXCollections.observableArrayList(shops);
            shopComboBox.setItems(shopList);
            
            // Set cell factory for shop display
            shopComboBox.setCellFactory(param -> new ListCell<Shop>() {
                @Override
                protected void updateItem(Shop shop, boolean empty) {
                    super.updateItem(shop, empty);
                    if (empty || shop == null) {
                        setText(null);
                    } else {
                        setText(shop.getShopName());
                    }
                }
            });
            
            shopComboBox.setButtonCell(shopComboBox.getCellFactory().call(null));
            
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            showAlert("Error", "Failed to load data: " + e.getMessage());
        }
    }
    
    private void loadFoodItemsForShop() {
        Shop selectedShop = shopComboBox.getValue();
        if (selectedShop != null) {
            try {
                List<FoodItem> foodItems = foodItemDao.listByShop(selectedShop.getId());
                ObservableList<FoodItem> foodItemList = FXCollections.observableArrayList(foodItems);
                foodItemComboBox.setItems(foodItemList);
                
                // Set cell factory for food item display
                foodItemComboBox.setCellFactory(param -> new ListCell<FoodItem>() {
                    @Override
                    protected void updateItem(FoodItem foodItem, boolean empty) {
                        super.updateItem(foodItem, empty);
                        if (empty || foodItem == null) {
                            setText(null);
                        } else {
                            setText(foodItem.getName() + " - $" + String.format("%.2f", foodItem.getPrice()));
                        }
                    }
                });
                
                foodItemComboBox.setButtonCell(foodItemComboBox.getCellFactory().call(null));
                
            } catch (Exception e) {
                System.err.println("Error loading food items: " + e.getMessage());
                showAlert("Error", "Failed to load food items: " + e.getMessage());
            }
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
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
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        
        if (selectedImageFile != null) {
            // Check file size (5MB limit)
            long fileSizeInMB = selectedImageFile.length() / (1024 * 1024);
            if (fileSizeInMB > 5) {
                showAlert("File Too Large", "Please select an image smaller than 5MB.");
                selectedImageFile = null;
                imageStatusLabel.setText("No image selected");
                return;
            }
            
            imageStatusLabel.setText("Selected: " + selectedImageFile.getName());
        }
    }
    
    @FXML
    private void handleSubmitReport() {
        if (currentUser == null) {
            showAlert("Error", "You must be signed in to submit a report.");
            return;
        }
        
        // Validate form
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a report title.");
            return;
        }
        
        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a report description.");
            return;
        }
        
        try {
            int shopId = -1;
            Integer itemId = null;
            
            if (shopReportRadio.isSelected()) {
                Shop selectedShop = shopComboBox.getValue();
                if (selectedShop == null) {
                    showAlert("Validation Error", "Please select a shop.");
                    return;
                }
                shopId = selectedShop.getId();
            } else if (foodItemReportRadio.isSelected()) {
                Shop selectedShop = shopComboBox.getValue();
                FoodItem selectedFoodItem = foodItemComboBox.getValue();
                if (selectedShop == null || selectedFoodItem == null) {
                    showAlert("Validation Error", "Please select both a shop and a food item.");
                    return;
                }
                shopId = selectedShop.getId();
                itemId = selectedFoodItem.getId();
            }
            
            // Submit report
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            if (selectedImageFile != null) {
                description += "\n\nImage attached: " + selectedImageFile.getName();
            }
            
            reportDao.submitReport(currentUser.getId(), shopId, itemId, title, description);
            
            showAlert("Success", "Your report has been submitted successfully. Our team will review it shortly.");
            
            // Clear form
            clearForm();
            
        } catch (Exception e) {
            System.err.println("Error submitting report: " + e.getMessage());
            showAlert("Error", "Failed to submit report: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        selectedImageFile = null;
        imageStatusLabel.setText("No image selected");
        shopComboBox.setValue(null);
        foodItemComboBox.setValue(null);
        shopReportRadio.setSelected(true);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
