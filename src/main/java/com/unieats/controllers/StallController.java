package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class StallController {
	@FXML private void handleBack() { info("Back to previous screen coming soon"); }
	@FXML private void handlePostItem() { info("Post food item coming soon"); }
	@FXML private void handleManageOrders() { info("Manage orders coming soon"); }
	@FXML private void handleInventory() { info("Inventory management coming soon"); }
	@FXML private void handleSalesHistory() { info("Sales history coming soon"); }

	private void info(String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}

