package com.fiwpr06.httpapplication.client.controller;

import com.fiwpr06.httpapplication.client.engine.DownloadEngine;
import com.fiwpr06.httpapplication.common.model.DownloadTask;
import com.fiwpr06.httpapplication.common.model.HistoryItem;
import com.fiwpr06.httpapplication.common.util.AlertHelper;
import com.fiwpr06.httpapplication.server.database.HistoryDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BrowserController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private Button btnReload;
    @FXML private Button btnHome;
    @FXML private TextField txtUrl;
    @FXML private Button btnInspect;
    @FXML private Button btnPostTest;
    @FXML private Button btnDownloads;
    @FXML private Button btnHistory;
    @FXML private Button btnNewTab;
    @FXML private ProgressBar progressBar;
    @FXML private StackPane contentContainer;

    private static final String DEFAULT_HOME = "http://localhost:8080/";
    private final HistoryDao historyDao = new HistoryDao();
    private final ObservableList<DownloadTask> downloadTasks = FXCollections.observableArrayList();
    private final File downloadFolder = new File(System.getProperty("user.home"), "Downloads");

    private final Map<Tab, WebView> tabWebViews = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tab selection change listener
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                WebView webView = tabWebViews.get(newTab);
                if (webView != null) {
                    contentContainer.getChildren().setAll(webView);
                    updateOmniboxAndNav(webView.getEngine());
                }
            }
        });

        // Open initial tab
        handleNewTab();
    }

    private void updateOmniboxAndNav(WebEngine engine) {
        if (engine == null) return;
        String loc = engine.getLocation();
        if (loc != null && !loc.isEmpty() && !loc.equals("about:blank")) {
            txtUrl.setText(loc);
        }

        WebHistory history = engine.getHistory();
        int cur = history.getCurrentIndex();
        btnBack.setDisable(cur <= 0);
        btnForward.setDisable(cur >= history.getEntries().size() - 1);
    }

    @FXML
    public void handleNewTab() {
        Tab tab = new Tab("Trang mới");
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        tabWebViews.put(tab, webView);

        // Location listener
        engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (tabPane.getSelectionModel().getSelectedItem() == tab) {
                txtUrl.setText(newLoc);
                updateOmniboxAndNav(engine);
            }
            if (newLoc != null && !newLoc.isEmpty() && !newLoc.equals("about:blank")) {
                String title = engine.getTitle();
                historyDao.insertHistory(new HistoryItem(title != null ? title : newLoc, newLoc));
            }
        });

        // Title listener
        engine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                tab.setText(newTitle.length() > 20 ? newTitle.substring(0, 18) + "..." : newTitle);
            }
        });

        // Progress listener
        engine.getLoadWorker().progressProperty().addListener((obs, oldVal, newVal) -> {
            if (tabPane.getSelectionModel().getSelectedItem() == tab) {
                progressBar.setProgress(newVal.doubleValue());
            }
        });

        // State listener
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (tabPane.getSelectionModel().getSelectedItem() == tab) {
                if (newState == Worker.State.RUNNING) {
                    progressBar.setVisible(true);
                } else {
                    progressBar.setVisible(false);
                    updateOmniboxAndNav(engine);
                }
            }
        });

        tab.setOnClosed(e -> {
            tabWebViews.remove(tab);
            if (tabPane.getTabs().isEmpty()) {
                handleNewTab();
            }
        });

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        // Load default home
        navigate(DEFAULT_HOME);
    }

    private WebView getCurrentWebView() {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        return selected != null ? tabWebViews.get(selected) : null;
    }

    private WebEngine getCurrentEngine() {
        WebView webView = getCurrentWebView();
        return webView != null ? webView.getEngine() : null;
    }

    public void navigate(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) return;
        String cleanUrl = urlStr.trim();
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") && !cleanUrl.startsWith("file://")) {
            cleanUrl = "http://" + cleanUrl;
        }

        // Check if download link (e.g. ends with .zip)
        if (cleanUrl.toLowerCase().endsWith(".zip")) {
            startDownload(cleanUrl);
            return;
        }

        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            engine.load(cleanUrl);
        }
    }

    private void startDownload(String urlStr) {
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }

        DownloadTask task = DownloadEngine.startDownload(urlStr, downloadFolder,
                completedTask -> AlertHelper.showInfo("Tải thành công", "Đã tải xong tệp: " + completedTask.getFileName() + "\nLưu tại: " + completedTask.getSavePath()),
                errMsg -> AlertHelper.showError("Lỗi tải xuống", errMsg)
        );

        downloadTasks.add(task);
        btnDownloads.setText("📥 Downloads (" + downloadTasks.size() + ")");
        AlertHelper.showInfo("Bắt đầu tải", "Đang tải tệp: " + task.getFileName() + " về thư mục Downloads!");
    }

    @FXML
    public void handleNavigate() {
        navigate(txtUrl.getText());
    }

    @FXML
    public void handleBack() {
        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            WebHistory history = engine.getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
            }
        }
    }

    @FXML
    public void handleForward() {
        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            WebHistory history = engine.getHistory();
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
            }
        }
    }

    @FXML
    public void handleReload() {
        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            engine.reload();
        }
    }

    @FXML
    public void handleHome() {
        navigate(DEFAULT_HOME);
    }

    @FXML
    public void handleBookmarkHome() { navigate(DEFAULT_HOME); }
    @FXML
    public void handleBookmarkVideo() { navigate(DEFAULT_HOME + "media/video.mp4"); }
    @FXML
    public void handleBookmarkAudio() { navigate(DEFAULT_HOME + "media/audio.mp3"); }
    @FXML
    public void handleBookmarkPdf() { navigate(DEFAULT_HOME + "documents/sample.pdf"); }
    @FXML
    public void handleBookmarkZip() { navigate(DEFAULT_HOME + "downloads/project.zip"); }
    @FXML
    public void handleBookmarkForm() { navigate(DEFAULT_HOME + "#upload"); }
    @FXML
    public void handleBookmarkApi() { navigate(DEFAULT_HOME + "api/status"); }

    @FXML
    public void handleOpenInspector() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fiwpr06/httpapplication/client/inspector-modal.fxml"));
            Scene scene = new Scene(loader.load(), 650, 520);
            InspectorController ctrl = loader.getController();
            ctrl.setUrl(txtUrl.getText());

            Stage stage = new Stage();
            stage.setTitle("📋 HTTP Inspector — Tra Cứu Response Headers (HEAD)");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenPostTester() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fiwpr06/httpapplication/client/post-tester-modal.fxml"));
            Scene scene = new Scene(loader.load(), 680, 580);
            PostTesterController ctrl = loader.getController();
            ctrl.setUrl(txtUrl.getText());

            Stage stage = new Stage();
            stage.setTitle("🧪 HTTP POST Tester — Thử Nghiệm Gửi Form & Dữ Liệu");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fiwpr06/httpapplication/client/history-modal.fxml"));
            Scene scene = new Scene(loader.load(), 750, 550);
            HistoryController ctrl = loader.getController();
            ctrl.setOnNavigate(this::navigate);

            Stage stage = new Stage();
            stage.setTitle("🕒 Lịch Sử Duyệt Web (Browsing History)");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenDownloads() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fiwpr06/httpapplication/client/download-modal.fxml"));
            Scene scene = new Scene(loader.load(), 760, 500);
            DownloadController ctrl = loader.getController();
            ctrl.setDownloadTasks(downloadTasks, downloadFolder);

            Stage stage = new Stage();
            stage.setTitle("📥 Trình Quản Lý Tải Xuống (Download Manager)");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
