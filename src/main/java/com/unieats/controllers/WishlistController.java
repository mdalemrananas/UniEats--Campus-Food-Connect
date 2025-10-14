package com.unieats.controllers;

import com.unieats.FoodItem;
import com.unieats.User;
import com.unieats.dao.WishlistDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class WishlistController {
	@FXML private Button backButton;
	@FXML private ListView<FoodItem> wishlistList;
	@FXML private VBox emptyState;
	@FXML private VBox navHome;
	@FXML private VBox navOrders;
	@FXML private VBox navCart;
	@FXML private VBox navFav;
	@FXML private VBox navProfile;

	private final WishlistDao wishlistDao = new WishlistDao();
	private User currentUser;

	@FXML
	private void initialize() {
		wishlistList.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(FoodItem item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) { setGraphic(null); setText(null); return; }
				VBox card = new VBox(6);
				card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
				Label name = new Label(item.getName());
				name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
				HBox line = new HBox();
				Region spacer = new Region();
				HBox.setHgrow(spacer, Priority.ALWAYS);
				Label price = new Label(String.format("$%.2f", item.getPrice()));
				price.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6b35;");
				line.getChildren().addAll(spacer, price);
				card.getChildren().addAll(name, line);
				setGraphic(card);
				setText(null);
			}
		});
	}

	public void setCurrentUser(User user) {
		this.currentUser = user;
		loadWishlist();
		setupNav();
	}

	private void loadWishlist() {
		if (currentUser == null) return;
		List<FoodItem> items = wishlistDao.listWishlistItems(currentUser.getId());
		ObservableList<FoodItem> obs = FXCollections.observableArrayList(items);
		wishlistList.setItems(obs);
		emptyState.setVisible(items.isEmpty());
	}

	@FXML
	private void handleBack() {
		navigateTo("/fxml/menu.fxml", "UniEats - Menu");
	}

	private void setupNav() {
		if (navHome != null) navHome.setOnMouseClicked(e -> navigateTo("/fxml/menu.fxml", "UniEats - Menu"));
		if (navOrders != null) navOrders.setOnMouseClicked(e -> navigateTo("/fxml/my_orders.fxml", "UniEats - My Orders"));
		if (navCart != null) navCart.setOnMouseClicked(e -> navigateTo("/fxml/cart.fxml", "UniEats - Cart"));
		if (navFav != null) navFav.setOnMouseClicked(e -> {/* already here */});
		if (navProfile != null) navProfile.setOnMouseClicked(e -> navigateTo("/fxml/profile.fxml", "UniEats - Profile"));
	}

	private void navigateTo(String fxml, String title) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent root = loader.load();
			Object controller = loader.getController();
			if (controller instanceof MenuController mc && currentUser != null) mc.setCurrentUser(currentUser);
			if (controller instanceof MyOrdersController oc && currentUser != null) oc.setCurrentUser(currentUser);
			if (controller instanceof CartController cc && currentUser != null) cc.setCurrentUserId(currentUser.getId());
			if (controller instanceof ProfileController pc && currentUser != null) pc.setCurrentUser(currentUser);
			Stage stage = (Stage) wishlistList.getScene().getWindow();
			Scene scene = com.unieats.util.ResponsiveSceneFactory.createResponsiveScene(root, 360, 800);
			stage.setScene(scene);
			stage.setTitle(title);
			stage.show();
		} catch (Exception e) {
			Alert a = new Alert(Alert.AlertType.ERROR, "Navigation error: " + e.getMessage());
			a.showAndWait();
		}
	}
}
