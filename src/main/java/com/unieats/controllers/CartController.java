package com.unieats.controllers;

import com.unieats.CartItemView;
import com.unieats.RewardService;
import com.unieats.dao.CartDao;
import com.unieats.dao.CartQueryDao;
import com.unieats.dao.OrderDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

public class CartController {

	@FXML private TableView<CartItemView> cartTable;
	@FXML private TableColumn<CartItemView, String> colName;
	@FXML private TableColumn<CartItemView, Number> colQty;
	@FXML private TableColumn<CartItemView, Number> colPrice;
	@FXML private TableColumn<CartItemView, Number> colPoints;
	@FXML private Label totalLabel;

	private final CartQueryDao cartQueryDao = new CartQueryDao();
	private final CartDao cartDao = new CartDao();
	private final OrderDao orderDao = new OrderDao();
	private int currentUserId;

	@FXML
	private void initialize() {
		// Table column bindings
		colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name));
		colQty.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().quantity));
		colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().price));
		colPoints.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().pointsMultiplier * data.getValue().quantity));
	}

	public void setCurrentUserId(int userId) {
		this.currentUserId = userId;
		refresh();
	}

	private void refresh() {
		List<CartItemView> items = cartQueryDao.listCartItems(currentUserId);
		ObservableList<CartItemView> data = FXCollections.observableArrayList(items);
		cartTable.setItems(data);
		double total = items.stream().mapToDouble(i -> i.price * i.quantity).sum();
		totalLabel.setText(String.format("$%.2f", total));
	}

	@FXML
	private void handleCheckout() {
		List<CartItemView> items = new ArrayList<>(cartTable.getItems());
		if (items.isEmpty()) { info("Cart is empty"); return; }
		// Ensure single-shop per order for points logic simplicity
		int shopId = items.get(0).shopId;
		boolean multiShop = items.stream().anyMatch(i -> i.shopId != shopId);
		if (multiShop) { info("Please checkout items from one shop at a time."); return; }

		double totalPrice = items.stream().mapToDouble(i -> i.price * i.quantity).sum();
		int orderId = orderDao.createOrder(currentUserId, shopId, totalPrice, "pending");
		for (CartItemView i : items) {
			orderDao.addOrderItem(orderId, i.itemId, i.quantity, i.price);
			// Points accrual: multiplier x quantity
			RewardService.addPoints(currentUserId, shopId, i.pointsMultiplier * i.quantity);
		}
		cartDao.clearCart(currentUserId);
		refresh();
		info("Checkout completed. Points updated for shop.");
	}

	@FXML
	private void handleClear() {
		cartDao.clearCart(currentUserId);
		refresh();
	}

	private void info(String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}

