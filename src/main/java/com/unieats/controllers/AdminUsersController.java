package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import com.unieats.User;

public class AdminUsersController {
	@FXML private TextField userSearchField;
	@FXML private Button refreshUsersButton;
	@FXML private TableView<User> usersTable;
	@FXML private TableColumn<User, Number> userIdColumn;
	@FXML private TableColumn<User, String> userNameColumn;
	@FXML private TableColumn<User, String> userEmailColumn;
	@FXML private TableColumn<User, String> userCategoryColumn;

	public TextField getUserSearchField() { return userSearchField; }
	public Button getRefreshUsersButton() { return refreshUsersButton; }
	public TableView<User> getUsersTable() { return usersTable; }
	public TableColumn<User, Number> getUserIdColumn() { return userIdColumn; }
	public TableColumn<User, String> getUserNameColumn() { return userNameColumn; }
	public TableColumn<User, String> getUserEmailColumn() { return userEmailColumn; }
	public TableColumn<User, String> getUserCategoryColumn() { return userCategoryColumn; }
}
