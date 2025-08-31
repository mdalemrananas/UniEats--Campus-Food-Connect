package com.unieats.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;

public class AdminDashboardController {
	@FXML private Label totalUsersLabel;
	@FXML private Label activeSellersLabel;
	@FXML private Label pendingSellersLabel;
	@FXML private Label pendingReportsLabel;
	@FXML private Label orderSuccessLabel; // legacy optional
	@FXML private ProgressBar orderSuccessBar; // legacy optional
	@FXML private AreaChart<String, Number> userGrowthChart; // legacy optional

	// New charts
	@FXML private PieChart complaintsPieChart;
	@FXML private LineChart<String, Number> growthChart;

	public Label getTotalUsersLabel() { return totalUsersLabel; }
	public Label getActiveSellersLabel() { return activeSellersLabel; }
	public Label getPendingSellersLabel() { return pendingSellersLabel; }
	public Label getPendingReportsLabel() { return pendingReportsLabel; }
	public Label getOrderSuccessLabel() { return orderSuccessLabel; }
	public ProgressBar getOrderSuccessBar() { return orderSuccessBar; }
	public AreaChart<String, Number> getUserGrowthChart() { return userGrowthChart; }
	public PieChart getComplaintsPieChart() { return complaintsPieChart; }
	public LineChart<String, Number> getGrowthChart() { return growthChart; }
}
