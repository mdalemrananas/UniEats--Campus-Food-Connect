package com.unieats.controllers;

import com.unieats.util.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import com.unieats.util.ReconnectingWebSocketClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.converter.DoubleStringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    @FXML
    private TableView<DatabaseHelper.FoodItem_seller> inventoryTable;
    @FXML
    private TableColumn<DatabaseHelper.FoodItem_seller, Number> idColumn;
    @FXML
    private TableColumn<DatabaseHelper.FoodItem_seller, String> nameColumn;
    @FXML
    private TableColumn<DatabaseHelper.FoodItem_seller, Double> priceColumn;
    @FXML
    private TableColumn<DatabaseHelper.FoodItem_seller, String> actionsColumn;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addNewButton;
    @FXML
    private Label statusLabel;

    private ObservableList<DatabaseHelper.FoodItem_seller> foodItems;
    private int shop_id; // Dynamic shop ID
    private ReconnectingWebSocketClient wsClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupEventHandlers();
        loadInventory();
        startSocketListener();
    }

    /** Set the shop ID dynamically and load inventory */
    public void setShopId(int shop_id) {
        this.shop_id = shop_id;
        loadInventory();
    }

    private void setupTable() {
        foodItems = FXCollections.observableArrayList();
        inventoryTable.setItems(foodItems);

        // Bind columns to the correct properties
        idColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()));
        nameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        priceColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceColumn.setOnEditCommit(event -> {
            DatabaseHelper.FoodItem_seller item = event.getRowValue();
            double newPrice = event.getNewValue();
            if (newPrice > 0) {
                boolean success = DatabaseHelper.updateFoodPrice(item.getId(), newPrice);
                if (success) {
                    item.setPrice(newPrice);
                    showStatus("Price updated successfully!");
                } else {
                    showStatus("Failed to update price. Reloading...");
                    loadInventory();
                }
            } else {
                showStatus("Price must be greater than 0.");
                loadInventory();
            }
        });

        // Add Delete button in each row
        actionsColumn.setCellFactory(param -> new TableCell<DatabaseHelper.FoodItem_seller, String>() {
            private final Button deleteButton = new Button("Delete");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    deleteButton.setStyle(
                            "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    deleteButton.setOnAction(event -> handleDeleteFood(getTableView().getItems().get(getIndex())));
                    setGraphic(deleteButton);
                }
            }
        });

        idColumn.setPrefWidth(60);
        nameColumn.setPrefWidth(150);
        priceColumn.setPrefWidth(100);
        actionsColumn.setPrefWidth(80);

        inventoryTable.setEditable(true);
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadInventory());
        addNewButton.setOnAction(e -> navigateToFoodPost());
        // optional back button from FXML
        try {
            Button backButton = (Button) refreshButton.getScene().lookup("#backButton");
            if (backButton != null)
                backButton.setOnAction(e -> handleBack());
        } catch (Exception ignored) {
        }
    }

    private void loadInventory() {
        try {
            foodItems.clear();
            foodItems.addAll(DatabaseHelper.getFoodsBySeller(shop_id));

            for (DatabaseHelper.FoodItem_seller f : foodItems) {
                System.out.println("Loaded: " + f.getId() + " | " + f.getName() + " | " + f.getPrice());
            }

            if (foodItems.isEmpty()) {
                showStatus("No food items found. Click 'Add New' to add your first item.");
            } else {
                showStatus("Loaded " + foodItems.size() + " food items.");
            }

            inventoryTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error loading inventory: " + e.getMessage());
        }
    }

    private void startSocketListener() {
        wsClient = new ReconnectingWebSocketClient("ws://localhost:7071", message -> {
            if (message == null || !message.contains("inventory_update"))
                return;
            if (shop_id > 0 && !message.contains("\"shopId\":" + shop_id))
                return;
            javafx.application.Platform.runLater(this::loadInventory);
        });
        wsClient.start();
    }

    private void handleDeleteFood(DatabaseHelper.FoodItem_seller foodItem) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Food Item");
        confirmAlert.setContentText("Are you sure you want to delete '" + foodItem.getName() + "'?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DatabaseHelper.deleteFood(foodItem.getId());
                if (success) {
                    foodItems.remove(foodItem);
                    showStatus("Food item deleted successfully!");
                } else {
                    showStatus("Failed to delete food item.");
                }
            }
        });
    }

    private void navigateToFoodPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food_post.fxml"));
            Parent root = loader.load();

            FoodPostController controller = loader.getController();
            controller.setShopId(shop_id);

            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 720);
            stage.setScene(scene);
            stage.setTitle("Add New Food Item");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open food post: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stall.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("Seller Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
