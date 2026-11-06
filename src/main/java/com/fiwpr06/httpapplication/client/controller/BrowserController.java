package com.fiwpr06.httpapplication.client.controller;

import com.fiwpr06.httpapplication.client.engine.DownloadEngine;
import com.fiwpr06.httpapplication.client.engine.HttpDirectClient;
import com.fiwpr06.httpapplication.common.model.DownloadTask;
import com.fiwpr06.httpapplication.common.model.HistoryItem;
import com.fiwpr06.httpapplication.common.util.AlertHelper;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.common.util.FileUtils;
import com.fiwpr06.httpapplication.server.database.HistoryDao;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BrowserController implements Initializable {

    // Main TabPane
    @FXML private TabPane mainTabPane;

    // Tab 1: Web View Controls
    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private TextField txtUrl;
    @FXML private ProgressBar progressBar;
    @FXML private WebView webView;

    // Tab 2: HTTP Tester Controls
    @FXML private ComboBox<String> cmbMethod;
    @FXML private TextField txtHttpTesterUrl;
    @FXML private VBox boxPostData;
    @FXML private TextField txtPostPayload;
    @FXML private Label lblHttpStatus;
    @FXML private TextArea txtResponseHeaders;
    @FXML private TextArea txtResponseBody;

    // Tab 3: History Controls
    @FXML private TableView<HistoryItem> tblHistory;
    @FXML private TableColumn<HistoryItem, String> colHistoryTitle;
    @FXML private TableColumn<HistoryItem, String> colHistoryUrl;
    @FXML private TableColumn<HistoryItem, String> colHistoryTime;

    // Tab 4: Downloads Controls
    @FXML private TableView<DownloadTask> tblDownloads;
    @FXML private TableColumn<DownloadTask, String> colDownloadName;
    @FXML private TableColumn<DownloadTask, String> colDownloadStatus;
    @FXML private TableColumn<DownloadTask, Double> colDownloadProgress;
    @FXML private TableColumn<DownloadTask, String> colDownloadSpeed;
    @FXML private TableColumn<DownloadTask, String> colDownloadSize;

    private static final String DEFAULT_HOME = "http://localhost:8080/";
    private final HistoryDao historyDao = new HistoryDao();
    private final ObservableList<HistoryItem> historyList = FXCollections.observableArrayList();
    private final ObservableList<DownloadTask> downloadList = FXCollections.observableArrayList();
    private final File downloadFolder = new File(System.getProperty("user.home"), "Downloads");

    private WebEngine engine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initWebView();
        initHttpTester();
        initHistory();
        initDownloads();

        // Load home page
        navigate(DEFAULT_HOME);
    }

    private void initWebView() {
        engine = webView.getEngine();

        // Location listener
        engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc != null && !newLoc.isEmpty() && !newLoc.equals("about:blank")) {
                txtUrl.setText(newLoc);
                String title = engine.getTitle();
                HistoryItem item = new HistoryItem(title != null ? title : newLoc, newLoc);
                historyDao.insertHistory(item);
                loadHistory();
            }
        });

        // Progress listener
        engine.getLoadWorker().progressProperty().addListener((obs, oldVal, newVal) -> {
            progressBar.setProgress(newVal.doubleValue());
        });

        // State listener
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                progressBar.setVisible(true);
            } else {
                progressBar.setVisible(false);
                updateNavButtons();
            }
        });
    }

    private void initHttpTester() {
        cmbMethod.getItems().addAll("GET", "HEAD", "POST");
        cmbMethod.setValue("HEAD");

        // Toggle POST payload visibility
        cmbMethod.valueProperty().addListener((obs, oldVal, newVal) -> {
            boxPostData.setVisible("POST".equals(newVal));
            boxPostData.setManaged("POST".equals(newVal));
            if ("POST".equals(newVal)) {
                txtHttpTesterUrl.setText("http://localhost:8080/api/submit");
            } else if ("HEAD".equals(newVal)) {
                txtHttpTesterUrl.setText("http://localhost:8080/documents/sample.pdf");
            } else {
                txtHttpTesterUrl.setText("http://localhost:8080/api/status");
            }
        });
        boxPostData.setVisible(false);
        boxPostData.setManaged(false);
    }

    private void initHistory() {
        colHistoryTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colHistoryUrl.setCellValueFactory(new PropertyValueFactory<>("url"));
        colHistoryTime.setCellValueFactory(cell -> new SimpleStringProperty(DateTimeUtil.formatDisplay(cell.getValue().getVisitTime())));
        tblHistory.setItems(historyList);

        // Double-click to navigate
        tblHistory.setRowFactory(tv -> {
            TableRow<HistoryItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleOpenSelectedHistory();
                }
            });
            return row;
        });

        loadHistory();
    }

    private void initDownloads() {
        colDownloadName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colDownloadStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().getLabel()));
        colDownloadProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colDownloadProgress.setCellFactory(ProgressBarTableCell.forTableColumn());
        colDownloadSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        colDownloadSize.setCellValueFactory(cell -> {
            DownloadTask task = cell.getValue();
            String cur = FileUtils.formatFileSize(task.getDownloadedBytes());
            String tot = task.getTotalBytes() > 0 ? FileUtils.formatFileSize(task.getTotalBytes()) : "Không rõ";
            return new SimpleStringProperty(cur + " / " + tot);
        });
        tblDownloads.setItems(downloadList);
    }

    public void navigate(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) return;
        String cleanUrl = urlStr.trim();
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") && !cleanUrl.startsWith("file://")) {
            cleanUrl = "http://" + cleanUrl;
        }

        // Check if download link
        if (cleanUrl.toLowerCase().endsWith(".zip")) {
            startDownload(cleanUrl);
            return;
        }

        if (engine != null) {
            engine.load(cleanUrl);
        }
    }

    private void startDownload(String urlStr) {
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }

        DownloadTask task = DownloadEngine.startDownload(urlStr, downloadFolder,
                done -> Platform.runLater(() -> AlertHelper.showInfo("Tải thành công", "Đã tải xong: " + done.getFileName())),
                err -> Platform.runLater(() -> AlertHelper.showError("Lỗi tải xuống", err))
        );

        downloadList.add(0, task);
        mainTabPane.getSelectionModel().select(3); // Switch to Downloads tab
        AlertHelper.showInfo("Bắt đầu tải", "Đang tải tệp: " + task.getFileName() + " về thư mục Downloads!");
    }

    private void updateNavButtons() {
        if (engine == null) return;
        WebHistory history = engine.getHistory();
        int cur = history.getCurrentIndex();
        btnBack.setDisable(cur <= 0);
        btnForward.setDisable(cur >= history.getEntries().size() - 1);
    }

    // Tab 1 Actions
    @FXML public void handleNavigate() { navigate(txtUrl.getText()); }
    @FXML public void handleBack() { if (engine != null && engine.getHistory().getCurrentIndex() > 0) engine.getHistory().go(-1); }
    @FXML public void handleForward() { if (engine != null && engine.getHistory().getCurrentIndex() < engine.getHistory().getEntries().size() - 1) engine.getHistory().go(1); }
    @FXML public void handleReload() { if (engine != null) engine.reload(); }
    @FXML public void handleHome() { navigate(DEFAULT_HOME); }

    @FXML public void handleBookmarkHome() { navigate(DEFAULT_HOME); }
    @FXML public void handleBookmarkVideo() { navigate(DEFAULT_HOME + "media/video.mp4"); }
    @FXML public void handleBookmarkAudio() { navigate(DEFAULT_HOME + "media/audio.mp3"); }
    @FXML public void handleBookmarkPdf() { navigate(DEFAULT_HOME + "documents/sample.pdf"); }
    @FXML public void handleBookmarkZip() { navigate(DEFAULT_HOME + "downloads/project.zip"); }

    // Tab 2 Actions: HTTP Tester (GET, HEAD, POST)
    @FXML
    public void handleSendHttpRequest() {
        String method = cmbMethod.getValue();
        String urlStr = txtHttpTesterUrl.getText().trim();
        String postBody = txtPostPayload.getText();

        if (urlStr.isEmpty()) {
            AlertHelper.showWarning("Thiếu URL", "Vui lòng nhập URL cần kiểm tra!");
            return;
        }

        lblHttpStatus.setText("⏳ Đang gửi yêu cầu " + method + "...");
        lblHttpStatus.setStyle("-fx-text-fill: #0284C7; -fx-font-weight: bold;");
        txtResponseHeaders.setText("Đang chờ phản hồi...");
        txtResponseBody.setText("");

        new Thread(() -> {
            try {
                HttpDirectClient.HttpResult result;
                if ("HEAD".equalsIgnoreCase(method)) {
                    result = HttpDirectClient.executeHead(urlStr);
                } else if ("POST".equalsIgnoreCase(method)) {
                    result = HttpDirectClient.executePost(urlStr, "application/x-www-form-urlencoded", postBody);
                } else {
                    result = HttpDirectClient.executeGet(urlStr);
                }

                Platform.runLater(() -> {
                    lblHttpStatus.setText(result.statusCode + " " + result.statusLine);
                    if (result.statusCode >= 200 && result.statusCode < 300) {
                        lblHttpStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 14px;");
                    } else if (result.statusCode >= 300 && result.statusCode < 400) {
                        lblHttpStatus.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-font-size: 14px;");
                    } else {
                        lblHttpStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }

                    // Build formatted headers string
                    StringBuilder headersSb = new StringBuilder();
                    for (Map.Entry<String, List<String>> entry : result.headers.entrySet()) {
                        String k = entry.getKey() != null ? entry.getKey() : ":status";
                        headersSb.append(k).append(": ").append(String.join(", ", entry.getValue())).append("\n");
                    }
                    txtResponseHeaders.setText(headersSb.toString());

                    // Display body
                    if (result.body != null && !result.body.isEmpty()) {
                        txtResponseBody.setText(result.body);
                    } else {
                        txtResponseBody.setText("(Phần thân Response Body rỗng — đúng theo chuẩn của yêu cầu HEAD)");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblHttpStatus.setText("Lỗi kết nối");
                    lblHttpStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 14px;");
                    txtResponseHeaders.setText("Lỗi: " + e.getMessage());
                    txtResponseBody.setText("Không thể kết nối tới máy chủ. Vui lòng đảm bảo Server đang bật!");
                });
            }
        }).start();
    }

    // Tab 3 Actions: History
    private void loadHistory() {
        List<HistoryItem> items = historyDao.getAllHistory();
        historyList.setAll(items);
    }

    @FXML
    public void handleOpenSelectedHistory() {
        HistoryItem selected = tblHistory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            navigate(selected.getUrl());
            mainTabPane.getSelectionModel().select(0); // Switch to Web View tab
        }
    }

    @FXML
    public void handleClearHistory() {
        if (AlertHelper.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa toàn bộ lịch sử?")) {
            historyDao.clearHistory();
            historyList.clear();
        }
    }

    // Tab 4 Actions: Downloads
    @FXML
    public void handleOpenDownloadsFolder() {
        try {
            if (downloadFolder.exists()) {
                Desktop.getDesktop().open(downloadFolder);
            }
        } catch (Exception e) {
            AlertHelper.showError("Lỗi", "Không thể mở thư mục Downloads: " + e.getMessage());
        }
    }
}
