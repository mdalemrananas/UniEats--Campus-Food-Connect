package com.unieats.controllers;

import com.unieats.Report;
import com.unieats.dao.ReportDao;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class AdminReportsController {
    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, Number> reportIdColumn;
    @FXML private TableColumn<Report, Number> reportUserColumn;
    @FXML private TableColumn<Report, Number> reportShopColumn;
    @FXML private TableColumn<Report, String> reportDescColumn;
    @FXML private TableColumn<Report, String> reportStatusColumn;

    private final ObservableList<Report> allReports = FXCollections.observableArrayList();
    private final ReportDao reportDao = new ReportDao();
    private com.unieats.controllers.AdminController adminController;

    @FXML
    private void initialize() {
        try { reportDao.seedDemoReportsIfEmpty(); } catch (Exception ignored) {}
        reload();
    }

    private void reload() {
        allReports.setAll(reportDao.listAll());
        if (reportsTable != null) reportsTable.setItems(allReports);
        if (reportIdColumn != null) reportIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        if (reportUserColumn != null) reportUserColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getUserId()));
        if (reportShopColumn != null) reportShopColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getShopId()));
        if (reportDescColumn != null) reportDescColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        if (reportStatusColumn != null) {
            reportStatusColumn.setCellFactory(new Callback<>() {
                @Override public TableCell<Report, String> call(TableColumn<Report, String> param) {
                    return new TableCell<>() {
                        private final ChoiceBox<String> choice = new ChoiceBox<>(FXCollections.observableArrayList("pending","completed"));
                        {
                            choice.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
                                Report r = getTableView().getItems().get(getIndex());
                                if (r != null && nv != null && !nv.equalsIgnoreCase(r.getStatus())) {
                                    r.setStatus(nv);
                                    reportDao.updateStatus(r.getId(), nv);
                                    // Refresh dashboard metrics after status change
                                    refreshDashboard();
                                }
                            });
                        }
                        @Override protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) setGraphic(null);
                            else {
                                Report r = getTableView().getItems().get(getIndex());
                                choice.getSelectionModel().select(r.getStatus());
                                setGraphic(choice);
                            }
                        }
                    };
                }
            });
        }
    }

    public void setAdminController(com.unieats.controllers.AdminController adminController) {
        this.adminController = adminController;
    }

    private void refreshDashboard() {
        if (adminController != null) {
            adminController.populateDashboard();
        }
    }
}
