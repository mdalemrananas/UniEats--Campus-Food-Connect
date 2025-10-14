package com.unieats.controllers;

import com.unieats.Shop;
import com.unieats.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.unieats.util.ReportFileManager;
import java.io.File;
import java.util.List;
<<<<<<< HEAD
=======
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.unieats.services.RealtimeService;
>>>>>>> User-Panel

public class AdminController {
	// Root content stack (we will add child panes at runtime)
	@FXML private StackPane contentStack;

	// Metric labels
	@FXML private Label totalUsersLabel;
	@FXML private Label activeSellersLabel;
	@FXML private Label pendingSellersLabel;
	@FXML private Label pendingReportsLabel;
	@FXML private Label sessionLabel;
    @FXML private Label titleLabel;

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

	// Users cards
	@FXML private FlowPane userCardsFlow;
	@FXML private TextField userSearchField;

	// Shops cards
	@FXML private FlowPane shopCardsFlow;
	@FXML private TextField sellerSearchField;

	// Reports/Payments cards
	private FlowPane reportsFlow;
	private FlowPane paymentsFlow;

	// Dashboard progress elements
	@FXML private ProgressBar orderSuccessBar;
	@FXML private Label orderSuccessLabel;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private final ObservableList<User> allUsers = FXCollections.observableArrayList();
	private final ObservableList<Shop> allSellers = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		// Load child panes programmatically to avoid include path issues
		loadChildPanes();

        // Ensure global stylesheet is applied so drawer button colors take effect
        try {
            if (!contentStack.getStylesheets().contains("/css/styles.css")) {
                contentStack.getStylesheets().add("/css/styles.css");
            }
        } catch (Exception ignored) {}

		configureTables();
		loadDummyData();
		populateDashboard();
		renderReportsAndPayments();
		populateCharts();
		startAutoRefresh();
		startRealtime();
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
				userCardsFlow = uCtrl.getUserCardsFlow();
			}

			// Sellers
			FXMLLoader sLoader = new FXMLLoader(getClass().getResource("/fxml/admin_sellers.fxml"));
			sellersPane = sLoader.load();
			contentStack.getChildren().add(sellersPane);
			AdminSellersController sCtrl = sLoader.getController();
			if (sCtrl != null) {
				sellerSearchField = sCtrl.getSellerSearchField();
				shopCardsFlow = sCtrl.getShopCardsFlow();
			}

			// Reports, Payments, Settings
			FXMLLoader rLoader = new FXMLLoader(getClass().getResource("/fxml/admin_reports.fxml"));
			reportsPane = rLoader.load();
			contentStack.getChildren().add(reportsPane);
			AdminReportsController rCtrl = rLoader.getController();
			if (rCtrl != null) { this.reportsFlow = rCtrl.getReportsFlow(); }

			FXMLLoader pLoader = new FXMLLoader(getClass().getResource("/fxml/admin_payments.fxml"));
			paymentsPane = pLoader.load();
			contentStack.getChildren().add(paymentsPane);
			AdminPaymentsController pCtrl = pLoader.getController();
			if (pCtrl != null) { this.paymentsFlow = pCtrl.getPaymentsFlow(); }
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
		// users and shops now displayed as cards; rendering handled elsewhere
	}

	private void wireSearchFields() {
		if (userSearchField != null) userSearchField.textProperty().addListener((obs, o, n) -> filterUsers(n));
		if (sellerSearchField != null) sellerSearchField.textProperty().addListener((obs, o, n) -> filterSellers(n));
	}

	private void filterUsers(String term) {
		if (userCardsFlow == null) return;
		ObservableList<User> source = allUsers;
		if (term != null && !term.isBlank()) {
			String lower = term.toLowerCase();
			source = allUsers.filtered(u ->
				String.valueOf(u.getId()).contains(lower)
					|| safe(u.getFullName()).contains(lower)
					|| safe(u.getEmail()).contains(lower)
					|| safe(u.getUserCategory()).contains(lower)
			);
		}
		renderUserCards(source);
	}

	private void filterSellers(String term) {
		if (shopCardsFlow == null) return;
		ObservableList<Shop> source = allSellers;
		if (term != null && !term.isBlank()) {
			String lower = term.toLowerCase();
			source = allSellers.filtered(s ->
				String.valueOf(s.getId()).contains(lower)
					|| safe(s.getShopName()).contains(lower)
					|| String.valueOf(s.getOwnerId()).contains(lower)
					|| safe(s.getStatus()).contains(lower)
			);
		}
		renderShopCards(source);
	}

	private String safe(String v) { return v == null ? "" : v.toLowerCase(); }

	private void loadDummyData() {
		allUsers.clear();
		// Load real users from DB
<<<<<<< HEAD
		allUsers.addAll(com.unieats.DatabaseManager.getInstance().getAllUsers());
=======
        // Only students shown in Manage Users (sellers are handled in Manage Shops)
        for (User u : com.unieats.DatabaseManager.getInstance().getAllUsers()) {
            if ("student".equalsIgnoreCase(u.getUserCategory())) {
                allUsers.add(u);
            }
        }
>>>>>>> User-Panel
		renderUserCards(allUsers);

		allSellers.clear();
		allSellers.addAll(new com.unieats.dao.ShopDao().listAll());
		renderShopCards(allSellers);
	}

	private void populateDashboard() {
		// Get metrics as requested: Total Users, Active Shops, Total Reports, Total Payments
		int totalUsers = allUsers.size();
		long activeShops = allSellers.stream().filter(s -> "approved".equalsIgnoreCase(s.getStatus())).count();
		
		// Get total reports count and total payments sum from database
		com.unieats.dao.ReportDao reportDao = new com.unieats.dao.ReportDao();
		com.unieats.dao.PaymentDao paymentDao = new com.unieats.dao.PaymentDao();
		int totalReports = reportDao.getTotalReportsCount();
		double totalPaymentsSum = paymentDao.getTotalPaymentsSum();

		// Update dashboard labels with the correct metrics
		if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(totalUsers));
		if (activeSellersLabel != null) activeSellersLabel.setText(String.valueOf(activeShops));
		if (pendingSellersLabel != null) pendingSellersLabel.setText(String.valueOf(totalReports));
		if (pendingReportsLabel != null) pendingReportsLabel.setText(String.format("৳%.2f", totalPaymentsSum));
	}

    private void renderReportsAndPayments() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db")) {
            // Reports
            if (this.reportsFlow != null) {
                this.reportsFlow.getChildren().clear();
                ResultSet rs = new com.unieats.dao.ReportDao().listAll(conn);
                while (rs.next()) {
                    VBox card = new VBox();
                    card.setSpacing(6);
                    card.setPadding(new Insets(10));
                    card.getStyleClass().add("card");
                    card.setPrefWidth(300);
                    
                    Text title = new Text(rs.getString("title"));
                    title.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");
                    Text user = new Text("User: " + rs.getString("user_name"));
                    user.setStyle("-fx-fill: #6b7280;");
                    Text shop = new Text("Shop: " + rs.getString("shop_name"));
                    shop.setStyle("-fx-fill: #6b7280;");
                    Text statusText = new Text("Status: " + rs.getString("status"));
                    statusText.getStyleClass().add("tag");
                    
                    // Add attachments section if any exist
                    String attachmentsJson = rs.getString("attachments");
                    List<String> attachments = ReportFileManager.parseAttachments(attachmentsJson);
                    
                    VBox attachmentsContainer = new VBox();
                    attachmentsContainer.setSpacing(4);
                    
<<<<<<< HEAD
=======
                    // Only show buttons that point to existing files
>>>>>>> User-Panel
                    if (!attachments.isEmpty()) {
                        Text attachmentsLabel = new Text("Attachments:");
                        attachmentsLabel.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-fill: #374151;");
                        attachmentsContainer.getChildren().add(attachmentsLabel);
                        
                        HBox buttonsContainer = new HBox();
                        buttonsContainer.setSpacing(6);
                        buttonsContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        
<<<<<<< HEAD
                        for (int i = 0; i < attachments.size(); i++) {
                            String attachmentPath = attachments.get(i);
                            Button downloadBtn = new Button("📎 Download " + (i + 1));
                            downloadBtn.getStyleClass().addAll("download-btn", "download-btn-" + ((i % 4) + 1));
                            downloadBtn.setStyle("-fx-font-size: 10px; -fx-padding: 4 8 4 8; -fx-cursor: hand;");
                            
                            // Add download functionality
                            downloadBtn.setOnAction(e -> downloadAttachment(attachmentPath));
                            
                            buttonsContainer.getChildren().add(downloadBtn);
=======
                        int shown = 0;
                        for (int i = 0; i < attachments.size(); i++) {
                            String attachmentPath = attachments.get(i);
                            if (ReportFileManager.attachmentExists(attachmentPath)) {
                                Button downloadBtn = new Button("📎 Download " + (shown + 1));
                                downloadBtn.getStyleClass().addAll("download-btn", "download-btn-" + ((shown % 4) + 1));
                                downloadBtn.setStyle("-fx-font-size: 10px; -fx-padding: 4 8 4 8; -fx-cursor: hand;");
                                downloadBtn.setOnAction(e -> downloadAttachment(attachmentPath));
                                buttonsContainer.getChildren().add(downloadBtn);
                                shown++;
                            }
                        }
                        if (shown == 0) {
                            // no existing attachments -> don't show container
                            buttonsContainer.setManaged(false);
                            buttonsContainer.setVisible(false);
>>>>>>> User-Panel
                        }
                        
                        attachmentsContainer.getChildren().add(buttonsContainer);
                    }
                    
                    card.getChildren().addAll(title, user, shop, statusText, attachmentsContainer);
                    this.reportsFlow.getChildren().add(card);
                }
            }

        // Payments
        if (this.paymentsFlow != null) {
                this.paymentsFlow.getChildren().clear();
<<<<<<< HEAD
                ResultSet pr = new com.unieats.dao.PaymentDao().listLatest(conn, 50);
                while (pr.next()) {
                    VBox card = new VBox();
                    card.setSpacing(6);
                    card.setPadding(new Insets(10));
                    card.getStyleClass().add("card");
                    card.setPrefWidth(300);
                    Text method = new Text("Method: " + pr.getString("payment_method"));
                    method.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");
                    Text amount = new Text("Amount: $" + pr.getDouble("amount"));
                    amount.setStyle("-fx-fill: #6b7280;");
                Text statusText = new Text("Status: " + pr.getString("status"));
                    statusText.getStyleClass().add("tag");
                    Text details = new Text(pr.getString("payment_details") == null ? "" : pr.getString("payment_details"));
                    details.setStyle("-fx-fill: #6b7280;");
                    card.getChildren().addAll(method, amount, statusText, details);
                    this.paymentsFlow.getChildren().add(card);
=======
                // Join orders, users, shops, order_items, food_items, payments to show rich card for completed payments
                String sql = """
                    SELECT p.id as payment_id, p.amount, p.payment_method, p.status as payment_status,
                           o.id as order_id, u.full_name as user_name, s.shop_name,
                           GROUP_CONCAT(fi.name || ' x' || oi.quantity || ' ($' || printf('%.2f', oi.price) || ')', '\n') as items
                    FROM payments p
                    JOIN orders o ON p.order_id = o.id
                    JOIN users u ON o.user_id = u.id
                    JOIN shops s ON o.shop_id = s.id
                    LEFT JOIN order_items oi ON oi.order_id = o.id
                    LEFT JOIN food_items fi ON fi.id = oi.item_id
                    WHERE p.status IN ('completed','success')
                    GROUP BY p.id, p.amount, p.payment_method, p.status, o.id, u.full_name, s.shop_name
                    ORDER BY p.created_at DESC
                    LIMIT 50
                """;
                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet pr = ps.executeQuery()) {
                    while (pr.next()) {
                        VBox card = new VBox();
                        card.setSpacing(6);
                        card.setPadding(new Insets(10));
                        card.getStyleClass().add("card");
                        card.setPrefWidth(300);
                        Text header = new Text("Payment #" + pr.getInt("payment_id") + " - $" + String.format("%.2f", pr.getDouble("amount")));
                        header.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");
                        Text user = new Text("User: " + pr.getString("user_name"));
                        user.setStyle("-fx-fill: #6b7280;");
                        Text shop = new Text("Shop: " + pr.getString("shop_name"));
                        shop.setStyle("-fx-fill: #6b7280;");
                        Text method = new Text("Method: " + pr.getString("payment_method"));
                        Text statusText = new Text("Status: " + pr.getString("payment_status"));
                        statusText.getStyleClass().add("tag");
                        String items = pr.getString("items");
                        Text itemsText = new Text(items == null ? "Items: (none)" : ("Items:\n" + items));
                        itemsText.setStyle("-fx-fill: #374151;");
                        card.getChildren().addAll(header, user, shop, method, statusText, itemsText);
                        this.paymentsFlow.getChildren().add(card);
                    }
>>>>>>> User-Panel
                }
            }
        } catch (Exception ignored) {}
    }

    private void populateCharts() {
        if (growthChart != null) {
            growthChart.getData().clear();
            XYChart.Series<String, Number> usersSeries = new XYChart.Series<>();
            usersSeries.setName("New Users");
            XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
            ordersSeries.setName("Orders");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db")) {
                String usersSql = "SELECT strftime('%Y-%m', created_at) AS ym, COUNT(*) c FROM users WHERE created_at IS NOT NULL AND created_at>=date('now','-6 months') GROUP BY ym ORDER BY ym";
                try (PreparedStatement ps = conn.prepareStatement(usersSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        usersSeries.getData().add(new XYChart.Data<>(rs.getString("ym"), rs.getInt("c")));
                    }
                }

                String ordersSql = "SELECT strftime('%Y-%m', created_at) AS ym, COUNT(*) c FROM orders WHERE created_at IS NOT NULL AND created_at>=date('now','-6 months') GROUP BY ym ORDER BY ym";
                try (PreparedStatement ps = conn.prepareStatement(ordersSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ordersSeries.getData().add(new XYChart.Data<>(rs.getString("ym"), rs.getInt("c")));
                    }
                }
            } catch (Exception ignored) {}

            growthChart.getData().addAll(usersSeries, ordersSeries);
        }

        // Pie chart: reports status distribution
        if (complaintsPieChart != null) {
            complaintsPieChart.getData().clear();
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:unieats.db");
                 PreparedStatement ps = conn.prepareStatement("SELECT status, COUNT(*) c FROM reports GROUP BY status");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaintsPieChart.getData().add(new PieChart.Data(rs.getString("status"), rs.getInt("c")));
                }
            } catch (Exception ignored) {}
        }
    }

	private void startAutoRefresh() {
		scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
		populateDashboard();
		renderReportsAndPayments();
			populateCharts();
		}), 60, 60, TimeUnit.SECONDS);
	}

    private void startRealtime() {
        RealtimeService rt = RealtimeService.getInstance();
        rt.onEvent(topic -> Platform.runLater(() -> {
            switch (topic) {
                case "users" -> {
                    allUsers.clear();
                    for (User u : com.unieats.DatabaseManager.getInstance().getAllUsers()) {
                        if ("student".equalsIgnoreCase(u.getUserCategory())) allUsers.add(u);
                    }
                    renderUserCards(allUsers);
                    populateDashboard();
                }
                case "shops" -> {
                    allSellers.clear();
                    allSellers.addAll(new com.unieats.dao.ShopDao().listAll());
                    renderShopCards(allSellers);
                    populateDashboard();
                }
                case "reports" -> renderReportsAndPayments();
                case "payments" -> renderReportsAndPayments();
            }
        }));
        rt.start();
    }

	@FXML private void showDashboard() { showOnly(dashboardPane); }
	@FXML private void showUsers() { showOnly(usersPane); }
	@FXML private void showSellers() { showOnly(sellersPane); }
    @FXML private void showReports() { showOnly(reportsPane); renderReportsAndPayments(); }
    @FXML private void showPayments() { showOnly(paymentsPane); renderReportsAndPayments(); }
	@FXML private void showSettings() { showOnly(settingsPane); }

    private void showOnly(Node nodeToShow) {
		if (nodeToShow == null) return;
		for (Node n : contentStack.getChildren()) { n.setVisible(false); n.setManaged(false); }
		nodeToShow.setVisible(true);
		nodeToShow.setManaged(true);
		closeDrawer();
        // Update title
        Node n = nodeToShow;
        String title = "Dashboard";
        if (n == usersPane) title = "Manage Users";
        else if (n == sellersPane) title = "Manage Shops";
        else if (n == reportsPane) title = "Reports";
        else if (n == paymentsPane) title = "Payments";
        else if (n == settingsPane) title = "Settings";
        if (titleLabel != null) titleLabel.setText(title);
	}
	
<<<<<<< HEAD
	private void downloadAttachment(String attachmentPath) {
		try {
			String fullPath = ReportFileManager.getAttachmentPath(attachmentPath);
			if (fullPath != null && ReportFileManager.attachmentExists(attachmentPath)) {
				File file = new File(fullPath);
				
				// Show file location dialog with copy option
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Attachment Location");
				alert.setHeaderText("Attachment Found");
				alert.setContentText("File location: " + file.getAbsolutePath() + 
									"\n\nClick OK to copy the path to clipboard, then navigate to this location in your file explorer.");
				
				// Add copy to clipboard functionality
				alert.setOnHidden(e -> {
					try {
						javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
						javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
						content.putString(file.getAbsolutePath());
						clipboard.setContent(content);
					} catch (Exception ex) {
						System.err.println("Failed to copy to clipboard: " + ex.getMessage());
					}
				});
				
				alert.showAndWait();
=======
    private void downloadAttachment(String attachmentPath) {
		try {
			String fullPath = ReportFileManager.getAttachmentPath(attachmentPath);
			if (fullPath != null && ReportFileManager.attachmentExists(attachmentPath)) {
                File file = new File(fullPath);
                javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
                chooser.setTitle("Save Attachment");
                chooser.setInitialFileName(file.getName());
                javafx.stage.Window w = contentStack.getScene() == null ? null : contentStack.getScene().getWindow();
                File target = chooser.showSaveDialog(w);
                if (target != null) {
                    java.nio.file.Files.copy(file.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText("Downloaded");
                    ok.setContentText("Saved to: " + target.getAbsolutePath());
                    ok.showAndWait();
                }
>>>>>>> User-Panel
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("File Not Found");
				alert.setHeaderText("Attachment Not Found");
				alert.setContentText("The attachment file could not be found at: " + attachmentPath);
				alert.showAndWait();
			}
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Download Error");
			alert.setHeaderText("Failed to Access Attachment");
			alert.setContentText("Error: " + e.getMessage());
			alert.showAndWait();
		}
	}

	@FXML private void toggleDrawer() {
		boolean showing = drawerContainer.isVisible();
		drawerContainer.setVisible(!showing);
		drawerContainer.setManaged(!showing);
	}
	
	/**
	 * Refresh dashboard data immediately after shop status changes
	 */
	private void refreshDashboardData() {
		// Reload shop data from database
		allSellers.clear();
		allSellers.addAll(new com.unieats.dao.ShopDao().listAll());
		
		// Re-render shop cards with updated data
		renderShopCards(allSellers);
		
		// Update dashboard metrics
		populateDashboard();
		
		// Update charts if visible
		populateCharts();
	}

	@FXML private void closeDrawer() {
		drawerContainer.setVisible(false);
		drawerContainer.setManaged(false);
	}

<<<<<<< HEAD
=======
	@FXML private void handleLogout() {
		try {
			Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
			confirm.setHeaderText("Logout Confirmation");
			confirm.setContentText("Are you sure you want to logout?");
			java.util.Optional<ButtonType> res = confirm.showAndWait();
			if (res.isEmpty() || res.get() != ButtonType.OK) {
				return;
			}
			// Clear current session
			com.unieats.DatabaseManager.setCurrentUser(null);
			// Navigate to signin page
			java.net.URL url = getClass().getResource("/fxml/signin.fxml");
			if (url == null) {
				Alert a = new Alert(Alert.AlertType.ERROR);
				a.setHeaderText("Sign In View Not Found");
				a.setContentText("Could not locate signin.fxml");
				a.showAndWait();
				return;
			}
			FXMLLoader loader = new FXMLLoader(url);
			Parent root = loader.load();
			Stage stage = (Stage) contentStack.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.setTitle("UniEats - Sign In");
			stage.show();
		} catch (Exception e) {
			Alert a = new Alert(Alert.AlertType.ERROR);
			a.setHeaderText("Logout Failed");
			a.setContentText(e.getMessage());
			a.showAndWait();
		}
	}

>>>>>>> User-Panel
	@FXML private void handleManageUsers() { showUsers(); }

	private void info(String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}

	private void renderUserCards(ObservableList<User> users) {
		if (userCardsFlow == null) return;
		userCardsFlow.getChildren().clear();
		for (User u : users) {
			VBox card = createUserCard(u);
			userCardsFlow.getChildren().add(card);
		}
	}

	private VBox createUserCard(User user) {
        VBox card = new VBox();
		card.setSpacing(6);
		card.setPadding(new Insets(10));
        card.getStyleClass().add("card");
		card.setPrefWidth(300);

		Text name = new Text(user.getFullName());
		name.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");
		Text email = new Text(user.getEmail());
		email.setStyle("-fx-fill: #6b7280;");
		Text category = new Text(user.getUserCategory());
		category.setStyle("-fx-fill: #6b7280;");

		HBox actions = new HBox(8);
		actions.setAlignment(Pos.CENTER_RIGHT);
		Region spacer = new Region();
		HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("btn-primary");
<<<<<<< HEAD
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger");
		actions.getChildren().addAll(spacer, viewBtn, deleteBtn);

		viewBtn.setOnAction(e -> showUserDetails(user));
		deleteBtn.setOnAction(e -> confirmAndDeleteUser(user));
=======
        javafx.scene.control.ComboBox<String> status = new javafx.scene.control.ComboBox<>();
        status.getItems().addAll("approved", "pending", "rejected");
        String currentStatus = user.getStatus() == null ? "pending" : user.getStatus();
        status.getSelectionModel().select(currentStatus);
        actions.getChildren().addAll(spacer, status, viewBtn);

        viewBtn.setOnAction(e -> showUserDetails(user));
        status.valueProperty().addListener((obs, oldV, newV) -> {
            if (oldV != null && !oldV.equals(newV)) {
                user.setStatus(newV);
                boolean ok = com.unieats.DatabaseManager.getInstance().updateUser(user);
                if (!ok) {
                    user.setStatus(oldV);
                    status.getSelectionModel().select(oldV);
                    info("Failed to update user status");
                } else {
                    info("User status updated to: " + newV.toUpperCase());
                }
            }
        });
>>>>>>> User-Panel

		card.getChildren().addAll(name, email, category, actions);
		return card;
	}

<<<<<<< HEAD
	private void showUserDetails(User user) {
		Alert dialog = new Alert(AlertType.INFORMATION);
		dialog.setHeaderText("User Details");
		dialog.setContentText("Name: " + user.getFullName() + "\nCategory: " + user.getUserCategory() + "\nEmail: " + user.getEmail());
		dialog.showAndWait();
	}

	private void confirmAndDeleteUser(User user) {
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setHeaderText("Delete user?");
		confirm.setContentText("Are you sure you want to delete " + user.getFullName() + "?");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.OK) {
				boolean ok = com.unieats.DatabaseManager.getInstance().deleteUser(user.getId());
				if (ok) {
					allUsers.removeIf(u -> u.getId() == user.getId());
					renderUserCards(allUsers);
				} else {
					info("Failed to delete user");
				}
			}
		});
	}
=======
    private void showUserDetails(User user) {
        Alert dialog = new Alert(AlertType.INFORMATION);
        dialog.setHeaderText("User Details");
        String phone = user.getPhoneNo() == null ? "-" : user.getPhoneNo();
        String address = user.getAddress() == null ? "-" : user.getAddress();
        String status = user.getStatus() == null ? "pending" : user.getStatus();
        dialog.setContentText(
            "Name: " + user.getFullName() +
            "\nCategory: " + user.getUserCategory() +
            "\nEmail: " + user.getEmail() +
            "\nPhone: " + phone +
            "\nAddress: " + address +
            "\nStatus: " + status.toUpperCase()
        );
        dialog.showAndWait();
    }
>>>>>>> User-Panel

	private void renderShopCards(ObservableList<Shop> shops) {
		if (shopCardsFlow == null) return;
		shopCardsFlow.getChildren().clear();
		for (Shop s : shops) {
        VBox card = new VBox();
			card.setSpacing(6);
			card.setPadding(new Insets(10));
        card.getStyleClass().add("card");
			card.setPrefWidth(300);

			Text name = new Text(s.getShopName());
			name.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");
			Text owner = new Text("Owner ID: " + s.getOwnerId());
			owner.setStyle("-fx-fill: #6b7280;");

			HBox statusBox = new HBox(8);
			statusBox.setAlignment(Pos.CENTER_LEFT);
            javafx.scene.control.ComboBox<String> status = new javafx.scene.control.ComboBox<>();
            status.getItems().addAll("approved", "pending", "rejected");
            status.getSelectionModel().select(s.getStatus());
            Button details = new Button("Details");
            details.getStyleClass().add("btn-primary");
            statusBox.getChildren().addAll(status, details);

            status.valueProperty().addListener((obs, oldV, newV) -> {
                if (oldV != null && !oldV.equals(newV)) { // Only update if actually changed
                    try {
                        new com.unieats.dao.ShopDao().updateStatus(s.getId(), newV);
                        s.setStatus(newV);
                        
                        // Immediately refresh the dashboard and shop data
                        refreshDashboardData();
                        
                        // Show success feedback
                        info("Shop status updated to: " + newV.toUpperCase());
                        
                    } catch (Exception ex) {
                        info("Failed to update status: " + ex.getMessage());
                        // Revert the ComboBox selection on failure
                        status.getSelectionModel().select(oldV);
                    }
                }
            });

            details.setOnAction(e -> {
                Alert d = new Alert(AlertType.INFORMATION);
                d.setHeaderText("Shop Details");
                d.setContentText("Shop: " + s.getShopName() + "\nOwner ID: " + s.getOwnerId() + "\nStatus: " + s.getStatus());
                d.showAndWait();
            });

			card.getChildren().addAll(name, owner, statusBox);
			shopCardsFlow.getChildren().add(card);
		}
	}
}

 