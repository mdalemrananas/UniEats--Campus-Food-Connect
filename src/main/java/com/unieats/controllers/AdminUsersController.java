package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class AdminUsersController {
	@FXML private TextField userSearchField;
	@FXML private Button refreshUsersButton;
	@FXML private FlowPane userCardsFlow;

	public TextField getUserSearchField() { return userSearchField; }
	public Button getRefreshUsersButton() { return refreshUsersButton; }
	public FlowPane getUserCardsFlow() { return userCardsFlow; }
}
