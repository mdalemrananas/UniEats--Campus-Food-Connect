package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import com.unieats.Shop;

public class AdminSellersController {
	@FXML private TextField sellerSearchField;
	@FXML private Button refreshSellersButton;
	@FXML private TableView<Shop> sellersTable;
	@FXML private TableColumn<Shop, Number> sellerIdColumn;
	@FXML private TableColumn<Shop, String> sellerNameColumn;
	@FXML private TableColumn<Shop, Number> sellerOwnerColumn;
	@FXML private TableColumn<Shop, String> sellerStatusColumn;

	public TextField getSellerSearchField() { return sellerSearchField; }
	public Button getRefreshSellersButton() { return refreshSellersButton; }
	public TableView<Shop> getSellersTable() { return sellersTable; }
	public TableColumn<Shop, Number> getSellerIdColumn() { return sellerIdColumn; }
	public TableColumn<Shop, String> getSellerNameColumn() { return sellerNameColumn; }
	public TableColumn<Shop, Number> getSellerOwnerColumn() { return sellerOwnerColumn; }
	public TableColumn<Shop, String> getSellerStatusColumn() { return sellerStatusColumn; }
}
