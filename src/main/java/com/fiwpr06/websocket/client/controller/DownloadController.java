package com.fiwpr06.websocket.client.controller;

import com.fiwpr06.websocket.common.model.DownloadTask;
import com.fiwpr06.websocket.common.util.FileUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DownloadController implements Initializable {

    @FXML private TableView<DownloadTask> tblDownloads;
    @FXML private TableColumn<DownloadTask, String> colFileName;
    @FXML private TableColumn<DownloadTask, String> colStatus;
    @FXML private TableColumn<DownloadTask, Double> colProgress;
    @FXML private TableColumn<DownloadTask, String> colSpeed;
    @FXML private TableColumn<DownloadTask, String> colSize;

    private ObservableList<DownloadTask> tasks;
    private File downloadFolder = new File(System.getProperty("user.home"), "Downloads");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().getLabel()));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colProgress.setCellFactory(ProgressBarTableCell.forTableColumn());
        colSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        colSize.setCellValueFactory(cell -> {
            DownloadTask task = cell.getValue();
            String downloaded = FileUtils.formatFileSize(task.getDownloadedBytes());
            String total = task.getTotalBytes() > 0 ? FileUtils.formatFileSize(task.getTotalBytes()) : "Không rõ";
            return new SimpleStringProperty(downloaded + " / " + total);
        });
    }

    public void setDownloadTasks(ObservableList<DownloadTask> tasks, File downloadFolder) {
        this.tasks = tasks;
        if (downloadFolder != null) {
            this.downloadFolder = downloadFolder;
        }
        tblDownloads.setItems(tasks);
    }

    @FXML
    public void handleOpenFolder() {
        try {
            if (downloadFolder.exists()) {
                Desktop.getDesktop().open(downloadFolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClearCompleted() {
        if (tasks != null) {
            tasks.removeIf(t -> t.getStatus() == DownloadTask.Status.COMPLETED || t.getStatus() == DownloadTask.Status.FAILED);
        }
    }
}
