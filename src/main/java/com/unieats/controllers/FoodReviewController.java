package com.unieats.controllers;

import com.unieats.util.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FoodReviewController implements Initializable {

    @FXML
    private TableView<DatabaseHelper.Review> reviewTable;
    @FXML
    private TableColumn<DatabaseHelper.Review, Integer> idColumn;
    @FXML
    private TableColumn<DatabaseHelper.Review, Integer> userIdColumn;
    @FXML
    private TableColumn<DatabaseHelper.Review, Integer> foodIdColumn;
    @FXML
    private TableColumn<DatabaseHelper.Review, Integer> ratingColumn;
    @FXML
    private TableColumn<DatabaseHelper.Review, String> commentColumn;
    @FXML
    private TableColumn<DatabaseHelper.Review, String> createdAtColumn;
    @FXML
    private Button backButton;

    private ObservableList<DatabaseHelper.Review> reviews;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadReviews();

        backButton.setOnAction(e -> handleBack());
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/stall.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) reviewTable.getScene().getWindow();
            javafx.scene.Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
            stage.setScene(scene);
            stage.setTitle("Seller Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userId"));
        foodIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("foodId"));
        ratingColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("comment"));
        createdAtColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("createdAt"));

        reviews = FXCollections.observableArrayList();
        reviewTable.setItems(reviews);
    }

    private void loadReviews() {
        reviews.clear();
        reviews.addAll(DatabaseHelper.getAllReviews());
        System.out.println("✅ Loaded " + reviews.size() + " reviews into table");
    }

}
