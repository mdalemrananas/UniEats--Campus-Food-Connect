package com.unieats.controllers;

import com.unieats.util.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Food Post popup window
 * Handles adding new food items to the seller's menu
 */
public class FoodPostController implements Initializable {

    @FXML
    private TextField foodNameField;
    @FXML
    private TextField foodPriceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField imageField;
    @FXML
    private TextField discountField;

    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;

    private int sellerId = 1; // Default seller ID, can be set dynamically

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupValidation();
    }

    public void setShopId(int shopId) {
        this.sellerId = shopId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    private void setupEventHandlers() {
        submitButton.setOnAction(e -> handleSubmit());
        cancelButton.setOnAction(e -> handleBack());

        foodNameField.setOnAction(e -> foodPriceField.requestFocus());
        foodPriceField.setOnAction(e -> stockField.requestFocus());
        stockField.setOnAction(e -> descriptionField.requestFocus());
        descriptionField.setOnAction(e -> imageField.requestFocus());
        imageField.setOnAction(e -> discountField.requestFocus());
        discountField.setOnAction(e -> handleSubmit());
    }

    private void setupValidation() {
        // Numeric validation for price, stock, discount
        foodPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?"))
                foodPriceField.setText(oldVal);
        });
        stockField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*"))
                stockField.setText(oldVal);
        });
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?"))
                discountField.setText(oldVal);
        });
    }

    @FXML
    private void handleSubmit() {
        String name = foodNameField.getText().trim();
        String priceText = foodPriceField.getText().trim();
        String stockText = stockField.getText().trim();
        String description = descriptionField.getText().trim();
        String image = imageField.getText().trim();
        String discountText = discountField.getText().trim();

        // Validation
        if (name.isEmpty()) {
            showAlert("Validation Error", "Please enter a food name.");
            foodNameField.requestFocus();
            return;
        }
        if (priceText.isEmpty()) {
            showAlert("Validation Error", "Please enter a price.");
            foodPriceField.requestFocus();
            return;
        }
        if (stockText.isEmpty()) {
            showAlert("Validation Error", "Please enter stock.");
            stockField.requestFocus();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            double discount = discountText.isEmpty() ? 0 : Double.parseDouble(discountText);

            if (price <= 0) {
                showAlert("Validation Error", "Price must be greater than 0.");
                foodPriceField.requestFocus();
                return;
            }
            if (stock < 0) {
                showAlert("Validation Error", "Stock cannot be negative.");
                stockField.requestFocus();
                return;
            }
            if (discount < 0) {
                showAlert("Validation Error", "Discount cannot be negative.");
                discountField.requestFocus();
                return;
            }

            // Insert into database
            boolean success = DatabaseHelper.insertFoodItem(name, price, stock, description, image, discount, sellerId);

            if (success) {
                showAlert("Success", "Food item added successfully!");
                clearFields();
                closeWindow();
            } else {
                showAlert("Error", "Failed to add food item. Please try again.");
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/stall.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) submitButton.getScene().getWindow();
            javafx.scene.Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("Seller Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            closeWindow();
        }
    }

    private void clearFields() {
        foodNameField.clear();
        foodPriceField.clear();
        stockField.clear();
        descriptionField.clear();
        imageField.clear();
        discountField.clear();
    }

    private void closeWindow() {
        try {
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();
        } catch (Exception ignored) {
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
