package com.unieats.controllers;

import com.unieats.Shop;
import com.unieats.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdminController {
	// Root content stack (we will add child panes at runtime)
	@FXML private StackPane contentStack;

	// Metric labels
	@FXML private Label totalUsersLabel;
	@FXML private Label activeSellersLabel;
	@FXML private Label pendingSellersLabel;
	@FXML private Label pendingReportsLabel;
	@FXML private Label sessionLabel;

	// Charts
	@FXML private AreaChart<String, Number> userGrowthChart;
	@FXML private PieChart salesPieChart;
	@FXML private PieChart complaintsPieChart;
	@FXML private javafx.scene.chart.LineChart<String, Number> growthChart;

	// Panes for navigation
	@FXML private Node dashboardPane;
	@FXML private Node usersPane;
	@FXML private Node sellersPane;
	@FXML private Node reportsPane;
	@FXML private Node paymentsPane;
	@FXML private Node settingsPane;

	// Drawer overlay
	@FXML private Node drawerContainer;

	// Users table
	@FXML private TableView<User> usersTable;
	@FXML private TableColumn<User, Number> userIdColumn;
	@FXML private TableColumn<User, String> userNameColumn;
	@FXML private TableColumn<User, String> userEmailColumn;
	@FXML private TableColumn<User, String> userCategoryColumn;
	@FXML private TextField userSearchField;

	// Sellers table
	@FXML private TableView<Shop> sellersTable;
	@FXML private TableColumn<Shop, Number> sellerIdColumn;
	@FXML private TableColumn<Shop, String> sellerNameColumn;
	@FXML private TableColumn<Shop, Number> sellerOwnerColumn;
	@FXML private TableColumn<Shop, String> sellerStatusColumn;
	@FXML private TextField sellerSearchField;

	// Dashboard progress elements
	@FXML private ProgressBar orderSuccessBar;
	@FXML private Label orderSuccessLabel;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final Random random = new Random();

	private final ObservableList<User> allUsers = FXCollections.observableArrayList();
	private final ObservableList<Shop> allSellers = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		// Load child panes programmatically to avoid include path issues
		loadChildPanes();

		configureTables();
		loadDummyData();
		populateDashboard();
		populateCharts();
		startAutoRefresh();
		wireSearchFields();
	}

	private void loadChildPanes() {
		try {
			// Dashboard
			FXMLLoader dLoader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
			dashboardPane = dLoader.load();
			contentStack.getChildren().add(dashboardPane);
			AdminDashboardController dCtrl = dLoader.getController();
			if (dCtrl != null) {
				totalUsersLabel = dCtrl.getTotalUsersLabel();
				activeSellersLabel = dCtrl.getActiveSellersLabel();
				pendingSellersLabel = dCtrl.getPendingSellersLabel();
				pendingReportsLabel = dCtrl.getPendingReportsLabel();
				userGrowthChart = dCtrl.getUserGrowthChart();
				complaintsPieChart = dCtrl.getComplaintsPieChart();
				growthChart = dCtrl.getGrowthChart();
			}

			// Users
			FXMLLoader uLoader = new FXMLLoader(getClass().getResource("/fxml/admin_users.fxml"));
			usersPane = uLoader.load();
			contentStack.getChildren().add(usersPane);
			AdminUsersController uCtrl = uLoader.getController();
			if (uCtrl != null) {
				userSearchField = uCtrl.getUserSearchField();
				usersTable = uCtrl.getUsersTable();
				userIdColumn = uCtrl.getUserIdColumn();
				userNameColumn = uCtrl.getUserNameColumn();
				userEmailColumn = uCtrl.getUserEmailColumn();
				userCategoryColumn = uCtrl.getUserCategoryColumn();
			}

			// Sellers
			FXMLLoader sLoader = new FXMLLoader(getClass().getResource("/fxml/admin_sellers.fxml"));
			sellersPane = sLoader.load();
			contentStack.getChildren().add(sellersPane);
			AdminSellersController sCtrl = sLoader.getController();
			if (sCtrl != null) {
				sellerSearchField = sCtrl.getSellerSearchField();
				sellersTable = sCtrl.getSellersTable();
				sellerIdColumn = sCtrl.getSellerIdColumn();
				sellerNameColumn = sCtrl.getSellerNameColumn();
				sellerOwnerColumn = sCtrl.getSellerOwnerColumn();
				sellerStatusColumn = sCtrl.getSellerStatusColumn();
			}

			// Reports, Payments, Settings
			reportsPane = FXMLLoader.load(getClass().getResource("/fxml/admin_reports.fxml"));
			contentStack.getChildren().add(reportsPane);
			paymentsPane = FXMLLoader.load(getClass().getResource("/fxml/admin_payments.fxml"));
			contentStack.getChildren().add(paymentsPane);
			settingsPane = FXMLLoader.load(getClass().getResource("/fxml/admin_settings.fxml"));
			contentStack.getChildren().add(settingsPane);

			showOnly(dashboardPane);
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR);
			a.setHeaderText("Failed to load Admin panes");
			a.setContentText(e.getMessage());
			a.showAndWait();
		}
	}

	private void configureTables() {
		if (userIdColumn != null) userIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
		if (userNameColumn != null) userNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
		if (userEmailColumn != null) userEmailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
		if (userCategoryColumn != null) userCategoryColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserCategory()));

		if (sellerIdColumn != null) sellerIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
		if (sellerNameColumn != null) sellerNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShopName()));
		if (sellerOwnerColumn != null) sellerOwnerColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOwnerId()));
		if (sellerStatusColumn != null) sellerStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

		if (usersTable != null) usersTable.setItems(allUsers);
		if (sellersTable != null) sellersTable.setItems(allSellers);
	}

	private void wireSearchFields() {
		if (userSearchField != null) userSearchField.textProperty().addListener((obs, o, n) -> filterUsers(n));
		if (sellerSearchField != null) sellerSearchField.textProperty().addListener((obs, o, n) -> filterSellers(n));
	}

	private void filterUsers(String term) {
		if (usersTable == null) return;
		if (term == null || term.isBlank()) { usersTable.setItems(allUsers); return; }
		String lower = term.toLowerCase();
		ObservableList<User> filtered = allUsers.filtered(u ->
			String.valueOf(u.getId()).contains(lower)
				|| safe(u.getFullName()).contains(lower)
				|| safe(u.getEmail()).contains(lower)
				|| safe(u.getUserCategory()).contains(lower)
		);
		usersTable.setItems(filtered);
	}

	private void filterSellers(String term) {
		if (sellersTable == null) return;
		if (term == null || term.isBlank()) { sellersTable.setItems(allSellers); return; }
		String lower = term.toLowerCase();
		ObservableList<Shop> filtered = allSellers.filtered(s ->
			String.valueOf(s.getId()).contains(lower)
				|| safe(s.getShopName()).contains(lower)
				|| String.valueOf(s.getOwnerId()).contains(lower)
				|| safe(s.getStatus()).contains(lower)
		);
		sellersTable.setItems(filtered);
	}

	private String safe(String v) { return v == null ? "" : v.toLowerCase(); }

	private void loadDummyData() {
		allUsers.clear();
		for (int i = 1; i <= 25; i++) {
			User u = new User("user" + i + "@mail.com", "pass", "User " + i, i % 5 == 0 ? "seller" : "student");
			u.setId(i);
			u.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(120)));
			allUsers.add(u);
		}

		allSellers.clear();
		for (int i = 1; i <= 10; i++) {
			Shop s = new Shop(i, "Shop " + i, i % 3 == 0 ? "pending" : "active");
			s.setId(i);
			allSellers.add(s);
		}
	}

	private void populateDashboard() {
		int totalUsers = allUsers.size();
		long activeSellers = allSellers.stream().filter(s -> "active".equalsIgnoreCase(s.getStatus())).count();
		int pendingSellers = (int) allSellers.stream().filter(s -> "pending".equalsIgnoreCase(s.getStatus())).count();
		int pendingReports = 3 + random.nextInt(5);

		if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(totalUsers));
		if (activeSellersLabel != null) activeSellersLabel.setText(String.valueOf(activeSellers));
		if (pendingSellersLabel != null) pendingSellersLabel.setText(String.valueOf(pendingSellers));
		if (pendingReportsLabel != null) pendingReportsLabel.setText(String.valueOf(pendingReports));
	}

	private void populateCharts() {
		// Complaints/Reports distribution pie (dummy)
		if (complaintsPieChart != null) {
			complaintsPieChart.setData(FXCollections.observableArrayList(
				new PieChart.Data("Quality", 30),
				new PieChart.Data("Late Delivery", 25),
				new PieChart.Data("Expired Food", 10),
				new PieChart.Data("Pricing", 15),
				new PieChart.Data("Others", 20)
			));
		}

		// User & Order growth line chart (dummy)
		if (growthChart != null) {
			growthChart.getData().clear();
			XYChart.Series<String, Number> usersSeries = new XYChart.Series<>();
			usersSeries.setName("New Users");
			XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
			ordersSeries.setName("Orders");
			int users = 50;
			int orders = 80;
			for (int i = 1; i <= 8; i++) {
				users += 10 + random.nextInt(20);
				orders += 15 + random.nextInt(25);
				String label = "W" + i;
				usersSeries.getData().add(new XYChart.Data<>(label, users));
				ordersSeries.getData().add(new XYChart.Data<>(label, orders));
			}
			growthChart.getData().addAll(usersSeries, ordersSeries);
		}

		// Legacy progress not used now; left for compatibility
	}

	private void startAutoRefresh() {
		scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
			populateDashboard();
			populateCharts();
		}), 60, 60, TimeUnit.SECONDS);
	}

	@FXML private void showDashboard() { showOnly(dashboardPane); }
	@FXML private void showUsers() { showOnly(usersPane); }
	@FXML private void showSellers() { showOnly(sellersPane); }
	@FXML private void showReports() { showOnly(reportsPane); }
	@FXML private void showPayments() { showOnly(paymentsPane); }
	@FXML private void showSettings() { showOnly(settingsPane); }

	private void showOnly(Node nodeToShow) {
		if (nodeToShow == null) return;
		for (Node n : contentStack.getChildren()) { n.setVisible(false); n.setManaged(false); }
		nodeToShow.setVisible(true);
		nodeToShow.setManaged(true);
		closeDrawer();
	}

	@FXML
	private void handleBack(ActionEvent event) {
		try {
			scheduler.shutdownNow();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setTitle("UniEats");
			stage.setScene(scene);
			stage.show();
		} catch (Exception ignored) {}
	}

	@FXML private void toggleDrawer() {
		boolean showing = drawerContainer.isVisible();
		drawerContainer.setVisible(!showing);
		drawerContainer.setManaged(!showing);
	}

	@FXML private void closeDrawer() {
		drawerContainer.setVisible(false);
		drawerContainer.setManaged(false);
	}

	@FXML private void handleApproveShops() { info("Dummy: Approve shop requests view not implemented yet."); }
	@FXML private void handleManageUsers() { showUsers(); }
	@FXML private void handleViewReports() { showReports(); }
	@FXML private void handleLogout() { info("Logged out (dummy)"); }

	private void info(String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}

 