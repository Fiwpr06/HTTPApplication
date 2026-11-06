package com.fiwpr06.httpapplication.client.controller;

import com.fiwpr06.httpapplication.common.model.HistoryItem;
import com.fiwpr06.httpapplication.common.util.AlertHelper;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.server.database.HistoryDao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class HistoryController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<HistoryItem> tblHistory;
    @FXML private TableColumn<HistoryItem, String> colTitle;
    @FXML private TableColumn<HistoryItem, String> colUrl;
    @FXML private TableColumn<HistoryItem, String> colTime;

    private final HistoryDao historyDao = new HistoryDao();
    private final ObservableList<HistoryItem> historyList = FXCollections.observableArrayList();
    private FilteredList<HistoryItem> filteredList;
    private Consumer<String> onNavigate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colUrl.setCellValueFactory(new PropertyValueFactory<>("url"));
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(DateTimeUtil.formatDisplay(cell.getValue().getVisitTime())));

        filteredList = new FilteredList<>(historyList, p -> true);
        tblHistory.setItems(filteredList);

        // Double click to open
        tblHistory.setRowFactory(tv -> {
            TableRow<HistoryItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleOpenSelected();
                }
            });
            return row;
        });

        loadHistory();
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void loadHistory() {
        List<HistoryItem> items = historyDao.getAllHistory();
        historyList.setAll(items);
    }

    @FXML
    public void handleFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        filteredList.setPredicate(item -> {
            if (keyword.isEmpty()) return true;
            return (item.getTitle() != null && item.getTitle().toLowerCase().contains(keyword))
                    || (item.getUrl() != null && item.getUrl().toLowerCase().contains(keyword));
        });
    }

    @FXML
    public void handleOpenSelected() {
        HistoryItem selected = tblHistory.getSelectionModel().getSelectedItem();
        if (selected != null && onNavigate != null) {
            onNavigate.accept(selected.getUrl());
            Stage stage = (Stage) tblHistory.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleDeleteSelected() {
        HistoryItem selected = tblHistory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            historyDao.deleteHistoryItem(selected.getId());
            historyList.remove(selected);
        }
    }

    @FXML
    public void handleClearAll() {
        if (AlertHelper.showConfirmation("Xác nhận xóa", "Bạn có chắc chắn muốn xóa toàn bộ lịch sử duyệt web?")) {
            historyDao.clearHistory();
            historyList.clear();
        }
    }
}
