package com.fiwpr06.websocket.server.controller;

import com.fiwpr06.websocket.common.model.AccessLog;
import com.fiwpr06.websocket.common.util.AlertHelper;
import com.fiwpr06.websocket.common.util.DateTimeUtil;
import com.fiwpr06.websocket.common.util.FileUtils;
import com.fiwpr06.websocket.common.util.NetworkUtil;
import com.fiwpr06.websocket.server.core.HttpServerEngine;
import com.fiwpr06.websocket.server.core.ServerListener;
import com.fiwpr06.websocket.server.database.AccessLogDao;
import com.fiwpr06.websocket.server.database.DatabaseManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServerController implements Initializable, ServerListener {

    @FXML private Label lblDbBadge;
    @FXML private Label lblStatusBadge;
    @FXML private TextField txtPort;
    @FXML private TextField txtWebRoot;
    @FXML private Button btnBrowseWebRoot;
    @FXML private Button btnStartServer;
    @FXML private Button btnStopServer;
    @FXML private Button btnCopyUrl;
    @FXML private Button btnOpenBrowser;

    @FXML private Text lblTotalRequests;
    @FXML private Text lblTotalBytes;
    @FXML private Text lblLocalIp;

    @FXML private TextField txtSearch;
    @FXML private TableView<AccessLog> tblLogs;
    @FXML private TableColumn<AccessLog, Integer> colId;
    @FXML private TableColumn<AccessLog, String> colTime;
    @FXML private TableColumn<AccessLog, String> colIp;
    @FXML private TableColumn<AccessLog, String> colMethod;
    @FXML private TableColumn<AccessLog, String> colUri;
    @FXML private TableColumn<AccessLog, Integer> colStatus;
    @FXML private TableColumn<AccessLog, String> colLength;
    @FXML private TableColumn<AccessLog, String> colUserAgent;

    private final HttpServerEngine serverEngine = new HttpServerEngine();
    private final AccessLogDao logDao = new AccessLogDao();
    private final ObservableList<AccessLog> logList = FXCollections.observableArrayList();
    private FilteredList<AccessLog> filteredLogs;

    private long totalRequestsCount = 0;
    private long totalBytesCount = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverEngine.setListener(this);

        // Database badge
        String dbType = DatabaseManager.getInstance().getActiveDbType().name();
        lblDbBadge.setText("DATABASE: " + dbType);

        // Local IP
        String localIp = NetworkUtil.getLocalIpv4Address();
        lblLocalIp.setText(localIp);

        // Setup TableView Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(DateTimeUtil.formatDisplay(cell.getValue().getTimestamp())));
        colIp.setCellValueFactory(new PropertyValueFactory<>("clientIp"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colUri.setCellValueFactory(new PropertyValueFactory<>("uri"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusCode"));
        colLength.setCellValueFactory(cell -> new SimpleStringProperty(FileUtils.formatFileSize(cell.getValue().getContentLength())));
        colUserAgent.setCellValueFactory(new PropertyValueFactory<>("userAgent"));

        // Custom Cell Factory for HTTP Status Code coloring
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer statusCode, boolean empty) {
                super.updateItem(statusCode, empty);
                if (empty || statusCode == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(statusCode));
                    if (statusCode == 200) {
                        setStyle("-fx-text-fill: #2EE68A; -fx-font-weight: bold;"); // Green
                    } else if (statusCode == 206) {
                        setStyle("-fx-text-fill: #00D2E6; -fx-font-weight: bold;"); // Cyan
                    } else if (statusCode == 404) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); // Orange
                    } else {
                        setStyle("-fx-text-fill: #FF3B56; -fx-font-weight: bold;"); // Red
                    }
                }
            }
        });

        // Search Filter
        filteredLogs = new FilteredList<>(logList, p -> true);
        tblLogs.setItems(filteredLogs);

        // Load initial records from database
        handleReloadLogs();

        // Auto-start server on launch
        Platform.runLater(this::handleStartServer);
    }

    @FXML
    public void handleStartServer() {
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertHelper.showError("Cổng không hợp lệ", "Vui lòng nhập cổng port từ 1 đến 65535!");
            return;
        }

        File root = new File(txtWebRoot.getText().trim());
        if (!root.exists()) {
            root.mkdirs();
        }

        boolean success = serverEngine.start(port, root);
        if (success) {
            txtPort.setDisable(true);
            btnBrowseWebRoot.setDisable(true);
            btnStartServer.setDisable(true);
            btnStopServer.setDisable(false);
        }
    }

    @FXML
    public void handleStopServer() {
        serverEngine.stop();
        txtPort.setDisable(false);
        btnBrowseWebRoot.setDisable(false);
        btnStartServer.setDisable(false);
        btnStopServer.setDisable(true);
    }

    @FXML
    public void handleBrowseWebRoot() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Chọn Thư mục Web Root");
        chooser.setInitialDirectory(new File("."));
        File selected = chooser.showDialog(txtPort.getScene().getWindow());
        if (selected != null) {
            txtWebRoot.setText(selected.getPath());
        }
    }

    @FXML
    public void handleCopyUrl() {
        String url = "http://localhost:" + txtPort.getText().trim() + "/";
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(url);
        clipboard.setContent(content);
        AlertHelper.showInfo("Đã sao chép", "Đã sao chép đường dẫn: " + url + " vào Clipboard!");
    }

    @FXML
    public void handleOpenBrowser() {
        String url = "http://localhost:" + txtPort.getText().trim() + "/";
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            AlertHelper.showWarning("Thông báo", "Vui lòng mở trình duyệt và truy cập: " + url);
        }
    }

    @FXML
    public void handleFilterLogs() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        filteredLogs.setPredicate(log -> {
            if (keyword.isEmpty()) return true;
            return (log.getClientIp() != null && log.getClientIp().toLowerCase().contains(keyword))
                    || (log.getUri() != null && log.getUri().toLowerCase().contains(keyword))
                    || (log.getMethod() != null && log.getMethod().toLowerCase().contains(keyword))
                    || String.valueOf(log.getStatusCode()).contains(keyword);
        });
    }

    @FXML
    public void handleReloadLogs() {
        List<AccessLog> recent = logDao.getRecentLogs(100);
        logList.clear();
        logList.addAll(recent);

        totalRequestsCount = logDao.getTotalRequests();
        totalBytesCount = logDao.getTotalBytesSent();
        updateStatDisplays();
    }

    @FXML
    public void handleClearScreen() {
        logList.clear();
    }

    private void updateStatDisplays() {
        lblTotalRequests.setText(String.valueOf(totalRequestsCount));
        lblTotalBytes.setText(FileUtils.formatFileSize(totalBytesCount));
    }

    @Override
    public void onRequestReceived(AccessLog log) {
        Platform.runLater(() -> {
            logList.add(0, log);
            if (logList.size() > 500) {
                logList.remove(logList.size() - 1);
            }
            totalRequestsCount++;
            totalBytesCount += log.getContentLength();
            updateStatDisplays();
        });
    }

    @Override
    public void onStatusChanged(boolean running, int port, String ip) {
        Platform.runLater(() -> {
            if (running) {
                lblStatusBadge.setText("🟢 SERVER ONLINE: " + ip + ":" + port);
                lblStatusBadge.getStyleClass().setAll("badge-online");
            } else {
                lblStatusBadge.setText("🔴 SERVER STOPPED");
                lblStatusBadge.getStyleClass().setAll("badge-offline");
            }
        });
    }

    @Override
    public void onServerError(String message) {
        Platform.runLater(() -> AlertHelper.showError("Lỗi Máy Chủ", message));
    }

    public void shutdown() {
        serverEngine.stop();
    }
}
