package com.unieats.controllers;

import com.unieats.DatabaseManager;
import com.unieats.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StallController {
    @FXML private VBox root;
    @FXML private Label userNameLabel;
    @FXML private Label userCategoryLabel;

    // Header + main card
    @FXML private Label totalMenusLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar menuProgress;

    // Order Summary
    @FXML private BarChart<String, Number> ordersChart;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;
    @FXML private ToggleButton ordersMonthlyBtn;
    @FXML private ToggleButton ordersWeeklyBtn;
    @FXML private ToggleButton ordersTodayBtn;

    // Revenue
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private ToggleButton revenueMonthlyBtn;
    @FXML private ToggleButton revenueWeeklyBtn;
    @FXML private ToggleButton revenueTodayBtn;

    private final Random rng = new Random();

    private String userName = "Guest";
    
    public void setUserName(String name) {
        this.userName = name != null ? name : "Guest";
        if (userNameLabel != null) {
            userNameLabel.setText(userName);
        }
    }
    
    @FXML
    private void initialize() {
        // Get current user from DatabaseManager
        User currentUser = DatabaseManager.getCurrentUser();
        
        // Set user name if available, otherwise use default
        if (currentUser != null) {
            // Set user name
            if (userNameLabel != null) {
                if (currentUser.getFullName() != null && !currentUser.getFullName().trim().isEmpty()) {
                    userNameLabel.setText(currentUser.getFullName());
                } else {
                    userNameLabel.setText(userName);
                }
            }
            
            // Set user category
            if (userCategoryLabel != null && currentUser.getUserCategory() != null) {
                String category = currentUser.getUserCategory();
                // Capitalize first letter of category
                category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
                userCategoryLabel.setText(category);
            }
        }
        
        // Load stylesheet programmatically
        try {
            String css = getClass().getResource("/css/dashboard.css").toExternalForm();
            root.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Warning: Could not load stylesheet: " + e.getMessage());
        }
        // Group segmented toggles
        ToggleGroup ordersGroup = new ToggleGroup();
        ordersMonthlyBtn.setToggleGroup(ordersGroup);
        ordersWeeklyBtn.setToggleGroup(ordersGroup);
        ordersTodayBtn.setToggleGroup(ordersGroup);

        ToggleGroup revenueGroup = new ToggleGroup();
        revenueMonthlyBtn.setToggleGroup(revenueGroup);
        revenueWeeklyBtn.setToggleGroup(revenueGroup);
        revenueTodayBtn.setToggleGroup(revenueGroup);

        // Chart axes labels
        ordersYAxis.setLabel("Orders");
        revenueYAxis.setLabel("Revenue (k)");

        // Load initial data asynchronously to keep UI responsive
        loadOrdersAsync("Monthly");
        loadRevenueAsync("Monthly");

        // Listeners for period selection
        ordersMonthlyBtn.setOnAction(e -> loadOrdersAsync("Monthly"));
        ordersWeeklyBtn.setOnAction(e -> loadOrdersAsync("Weekly"));
        ordersTodayBtn.setOnAction(e -> loadOrdersAsync("Today"));

        revenueMonthlyBtn.setOnAction(e -> loadRevenueAsync("Monthly"));
        revenueWeeklyBtn.setOnAction(e -> loadRevenueAsync("Weekly"));
        revenueTodayBtn.setOnAction(e -> loadRevenueAsync("Today"));

        // Animate progress to 75%
        animateProgress(0.75);
    }

    private void animateProgress(double target) {
        menuProgress.setProgress(0);
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(menuProgress.progressProperty(), 0)),
            new KeyFrame(Duration.millis(1200), new KeyValue(menuProgress.progressProperty(), target))
        );
        tl.currentTimeProperty().addListener((obs, oldT, newT) -> {
            double pct = menuProgress.getProgress();
            progressPercentLabel.setText((int) Math.round(pct * 100) + "%");
        });
        tl.play();
        totalMenusLabel.setText("180");
    }

    // Simulated async loads (replace with networking calls to your backend)
    private void loadOrdersAsync(String period) {
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override protected XYChart.Series<String, Number> call() {
                List<String> labels = xLabels(period);
                return series("Orders", labels, 20, 120);
            }
        };
        task.setOnSucceeded(ev -> {
            ordersChart.getData().setAll(task.getValue());
            ordersXAxis.setCategories(javafx.collections.FXCollections.observableArrayList(xLabels(period)));
        });
        new Thread(task, "orders-loader").start();
    }

    private void loadRevenueAsync(String period) {
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override protected XYChart.Series<String, Number> call() {
                List<String> labels = xLabels(period);
                return series("Revenue", labels, 10, 90);
            }
        };
        task.setOnSucceeded(ev -> {
            revenueChart.getData().setAll(task.getValue());
            revenueXAxis.setCategories(javafx.collections.FXCollections.observableArrayList(xLabels(period)));
        });
        new Thread(task, "revenue-loader").start();
    }

    private XYChart.Series<String, Number> series(String name, List<String> labels, int min, int max) {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(name);
        for (String l : labels) {
            int v = rng.nextInt(max - min + 1) + min;
            s.getData().add(new XYChart.Data<>(l, v));
        }
        return s;
    }

    private List<String> xLabels(String period) {
        List<String> list = new ArrayList<>();
        switch (period) {
            case "Today":
                list.add("9am"); list.add("12pm"); list.add("3pm"); list.add("6pm");
                break;
            case "Weekly":
                list.add("Mon"); list.add("Tue"); list.add("Wed"); list.add("Thu"); list.add("Fri");
                break;
            default:
                list.add("Jun24"); list.add("June25"); list.add("June26"); list.add("June27");
        }
        return list;
    }

    @FXML private void handleMenu() { info("Menu coming soon"); }
    @FXML private void handleBack() { info("Back to previous screen coming soon"); }
    @FXML private void handlePostItem() { info("Post food item coming soon"); }
    @FXML private void handleManageOrders() { info("Manage orders coming soon"); }
    @FXML private void handleInventory() { info("Inventory management coming soon"); }
    @FXML private void handleSalesHistory() { info("Sales history coming soon"); }

    private void info(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }
}
