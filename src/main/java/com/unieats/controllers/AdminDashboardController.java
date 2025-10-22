package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class AdminDashboardController {
	@FXML private Label totalUsersLabel;
	@FXML private Label activeSellersLabel;
	@FXML private Label pendingSellersLabel;
	@FXML private Label pendingReportsLabel;
	@FXML private Label orderSuccessLabel; // legacy optional
	@FXML private ProgressBar orderSuccessBar; // legacy optional
	@FXML private AreaChart<String, Number> userGrowthChart; // legacy optional

	// New charts and components
	@FXML private PieChart complaintsPieChart;
	@FXML private LineChart<String, Number> growthChart;
	@FXML private BarChart<String, Number> topShopsChart;
	@FXML private TableView<RecentActivity> recentActivitiesTable;
	@FXML private TableColumn<RecentActivity, String> activityTimeColumn;
	@FXML private TableColumn<RecentActivity, String> activityTypeColumn;
	@FXML private TableColumn<RecentActivity, String> activityDescriptionColumn;

	// Recent activities data - limited to 3 items
	private final ObservableList<RecentActivity> recentActivities = FXCollections.observableArrayList();
	private final LinkedList<RecentActivity> activityHistory = new LinkedList<>();

	@FXML
	public void initialize() {
		// Initialize recent activities table
		if (recentActivitiesTable != null) {
			recentActivitiesTable.setItems(recentActivities);
		}
	}

	public Label getTotalUsersLabel() { return totalUsersLabel; }
	public Label getActiveSellersLabel() { return activeSellersLabel; }
	public Label getPendingSellersLabel() { return pendingSellersLabel; }
	public Label getPendingReportsLabel() { return pendingReportsLabel; }
	public Label getOrderSuccessLabel() { return orderSuccessLabel; }
	public ProgressBar getOrderSuccessBar() { return orderSuccessBar; }
	public AreaChart<String, Number> getUserGrowthChart() { return userGrowthChart; }
	public PieChart getComplaintsPieChart() { return complaintsPieChart; }
	public LineChart<String, Number> getGrowthChart() { return growthChart; }
	public BarChart<String, Number> getTopShopsChart() { return topShopsChart; }
	public TableView<RecentActivity> getRecentActivitiesTable() { return recentActivitiesTable; }

	public void populateTopShopsChart() {
		if (topShopsChart != null) {
			topShopsChart.getData().clear();
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Orders");

			try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db")) {
				String sql = """
					SELECT s.shop_name, COUNT(o.id) as order_count
					FROM shops s
					LEFT JOIN orders o ON s.id = o.shop_id
					WHERE s.status = 'approved'
					GROUP BY s.id, s.shop_name
					ORDER BY order_count DESC
					LIMIT 5
				""";
				try (PreparedStatement ps = conn.prepareStatement(sql); 
					 ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String shopName = rs.getString("shop_name");
						int orderCount = rs.getInt("order_count");
						series.getData().add(new XYChart.Data<>(shopName.length() > 15 ? 
							shopName.substring(0, 12) + "..." : shopName, orderCount));
					}
				}
			} catch (Exception e) {
				System.err.println("Error loading top shops data: " + e.getMessage());
			}

			topShopsChart.getData().add(series);
		}
	}

	public void addRecentActivity(String type, String description) {
		RecentActivity activity = new RecentActivity(type, description);
		
		// Add to history
		activityHistory.addLast(activity);
		
		// Keep only last 3 activities
		if (activityHistory.size() > 3) {
			activityHistory.removeFirst();
		}
		
		// Update observable list
		recentActivities.clear();
		recentActivities.addAll(activityHistory);
	}

	public void loadRecentActivities() {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db")) {
			// Get recent users
			String userSql = "SELECT full_name, created_at FROM users WHERE created_at >= datetime('now', '-24 hours') ORDER BY created_at DESC LIMIT 3";
			try (PreparedStatement ps = conn.prepareStatement(userSql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					try {
						String time = LocalDateTime.parse(rs.getString("created_at").replace(" ", "T"))
							.format(DateTimeFormatter.ofPattern("HH:mm"));
						addRecentActivity("New User", rs.getString("full_name") + " registered");
					} catch (Exception e) {
						// Skip invalid date entries
						System.err.println("Skipping user with invalid date: " + e.getMessage());
					}
				}
			} catch (Exception e) {
				System.err.println("Error loading recent users: " + e.getMessage());
			}

			// Get recent shops
			String shopSql = "SELECT s.shop_name, s.created_at FROM shops s WHERE s.created_at >= datetime('now', '-24 hours') ORDER BY s.created_at DESC LIMIT 2";
			try (PreparedStatement ps = conn.prepareStatement(shopSql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					try {
						String time = LocalDateTime.parse(rs.getString("created_at").replace(" ", "T"))
							.format(DateTimeFormatter.ofPattern("HH:mm"));
						addRecentActivity("New Shop", rs.getString("shop_name") + " opened");
					} catch (Exception e) {
						// Skip invalid date entries
						System.err.println("Skipping shop with invalid date: " + e.getMessage());
					}
				}
			} catch (Exception e) {
				System.err.println("Error loading recent shops: " + e.getMessage());
			}

			// Get recent reports
			String reportSql = "SELECT r.title, r.created_at FROM reports r WHERE r.created_at >= datetime('now', '-24 hours') ORDER BY r.created_at DESC LIMIT 2";
			try (PreparedStatement ps = conn.prepareStatement(reportSql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					try {
						String time = LocalDateTime.parse(rs.getString("created_at").replace(" ", "T"))
							.format(DateTimeFormatter.ofPattern("HH:mm"));
						addRecentActivity("New Report", rs.getString("title"));
					} catch (Exception e) {
						// Skip invalid date entries
						System.err.println("Skipping report with invalid date: " + e.getMessage());
					}
				}
			} catch (Exception e) {
				System.err.println("Error loading recent reports: " + e.getMessage());
			}
		} catch (Exception e) {
			System.err.println("Error connecting to database for recent activities: " + e.getMessage());
			// Add some default activities if database is not available
			addRecentActivity("System", "Dashboard initialized");
		}
	}
}
