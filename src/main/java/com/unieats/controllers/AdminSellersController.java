package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class AdminSellersController {
	@FXML private TextField sellerSearchField;
	@FXML private Button refreshSellersButton;
	@FXML private FlowPane shopCardsFlow;

	public TextField getSellerSearchField() { return sellerSearchField; }
	public Button getRefreshSellersButton() { return refreshSellersButton; }
	public FlowPane getShopCardsFlow() { return shopCardsFlow; }
}
