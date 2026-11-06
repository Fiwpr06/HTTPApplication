package com.fiwpr06.httpapplication.server.controller;

import com.fiwpr06.httpapplication.common.model.AccessLog;
import com.fiwpr06.httpapplication.common.util.AlertHelper;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.common.util.FileUtils;
import com.fiwpr06.httpapplication.common.util.NetworkUtil;
import com.fiwpr06.httpapplication.server.core.HttpServerEngine;
import com.fiwpr06.httpapplication.server.core.ServerListener;
import com.fiwpr06.httpapplication.server.database.AccessLogDao;
import com.fiwpr06.httpapplication.server.database.DatabaseManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ServerController implements Initializable, ServerListener {

    @FXML private Label lblDbBadge;
    @FXML private Label lblStatusBadge;
    @FXML private Label lblLocalUrl;
    @FXML private Label lblLanUrl;
    @FXML private TextField txtPort;
    @FXML private Button btnStartServer;
    @FXML private Button btnStopServer;
    @FXML private TextArea txtConsoleLog;
    @FXML private Label lblSummary;

    private final HttpServerEngine serverEngine = new HttpServerEngine();
    private final AccessLogDao logDao = new AccessLogDao();
    private final File webRoot = new File("web_root");

    private long totalRequests = 0;
    private long totalBytes = 0;
    private String localIp = "127.0.0.1";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverEngine.setListener(this);

        // Database status
        String dbType = DatabaseManager.getInstance().getActiveDbType().name();
        lblDbBadge.setText("CSDL: " + dbType);

        localIp = NetworkUtil.getLocalIpv4Address();
        updateUrlCards("8080");

        // Initial stats
        totalRequests = logDao.getTotalRequests();
        totalBytes = logDao.getTotalBytesSent();
        updateSummary();

        appendLog("=== MÁY CHỦ PHÁT NHẠC & WEB HTTP SẴN SÀNG ===");
        appendLog("Môi trường: Windows Java 21 | CSDL: " + dbType);
        appendLog("Thư mục lưu trữ: " + webRoot.getAbsolutePath());
        appendLog("Địa chỉ LAN cho điện thoại/máy khác: http://" + localIp + ":8080/");

        // Auto start server on launch
        Platform.runLater(this::handleStartServer);
    }

    private void updateUrlCards(String port) {
        lblLocalUrl.setText("http://localhost:" + port + "/");
        lblLanUrl.setText("http://" + localIp + ":" + port + "/");
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

        boolean success = serverEngine.start(port, webRoot);
        if (success) {
            updateUrlCards(String.valueOf(port));
            txtPort.setDisable(true);
            btnStartServer.setDisable(true);
            btnStopServer.setDisable(false);
            appendLog("🚀 [KHỞI ĐỘNG] Máy chủ đang lắng nghe tại cổng " + port);
            appendLog("📱 [HƯỚNG DẪN KẾT NỐI] Mở điện thoại / máy khác (cùng Wi-Fi) truy cập: http://" + localIp + ":" + port + "/");
        }
    }

    @FXML
    public void handleStopServer() {
        serverEngine.stop();
        txtPort.setDisable(false);
        btnStartServer.setDisable(false);
        btnStopServer.setDisable(true);
        appendLog("⏹ [DỪNG] Máy chủ HTTP đã dừng hoạt động.");
    }

    @FXML
    public void handleCopyLanUrl() {
        ClipboardContent content = new ClipboardContent();
        content.putString(lblLanUrl.getText());
        Clipboard.getSystemClipboard().setContent(content);
        AlertHelper.showInfo("Đã sao chép", "Đã sao chép liên kết vào Clipboard:\n" + lblLanUrl.getText()
                + "\n\nHãy gửi link này qua Zalo/Tin nhắn cho bạn bè cùng Wi-Fi để vào nghe nhạc thử!");
    }

    @FXML
    public void handleBrowseWebRoot() {
        try {
            if (webRoot.exists()) {
                Desktop.getDesktop().open(webRoot);
            }
        } catch (Exception e) {
            AlertHelper.showError("Lỗi", "Không thể mở thư mục: " + e.getMessage());
        }
    }

    @FXML
    public void handleOpenBrowser() {
        String url = lblLocalUrl.getText();
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            AlertHelper.showWarning("Thông báo", "Vui lòng mở trình duyệt và truy cập: " + url);
        }
    }

    @FXML
    public void handleClearScreen() {
        txtConsoleLog.clear();
        appendLog("=== Đã làm sạch màn hình nhật ký ===");
    }

    private void appendLog(String message) {
        String time = DateTimeUtil.formatTimeOnly(LocalDateTime.now());
        txtConsoleLog.appendText("[" + time + "] " + message + "\n");
    }

    private void updateSummary() {
        lblSummary.setText(String.format("📊 Tổng yêu cầu đã nhận: %d | Tổng lưu lượng: %s | Địa chỉ IP máy chủ: %s",
                totalRequests, FileUtils.formatFileSize(totalBytes), localIp));
    }

    @Override
    public void onRequestReceived(AccessLog log) {
        Platform.runLater(() -> {
            totalRequests++;
            totalBytes += log.getContentLength();
            updateSummary();

            String actionIcon = "👉";
            String uri = log.getUri();
            if (uri.contains(".mp3") || uri.contains(".mp4")) {
                actionIcon = "🎵 [STREAM NHẠC]";
            } else if (uri.contains("/downloads/") || uri.contains(".zip")) {
                actionIcon = "📥 [TẢI FILE]";
            } else if (uri.equals("/") || uri.contains(".html")) {
                actionIcon = "🌐 [XEM WEB]";
            }

            String desc = switch (log.getStatusCode()) {
                case 200 -> "200 OK";
                case 206 -> "206 Partial Content (Tua nhạc)";
                case 404 -> "404 Not Found";
                default -> log.getStatusCode() + "";
            };

            appendLog(String.format("%s [%s] %s %s -> %s | %s",
                    actionIcon, log.getClientIp(), log.getMethod(), log.getUri(), desc, FileUtils.formatFileSize(log.getContentLength())));
        });
    }

    @Override
    public void onStatusChanged(boolean running, int port, String ip) {
        Platform.runLater(() -> {
            if (running) {
                lblStatusBadge.setText("🟢 ĐANG HOẠT ĐỘNG (" + port + ")");
                lblStatusBadge.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 12;");
                updateUrlCards(String.valueOf(port));
            } else {
                lblStatusBadge.setText("🔴 MÁY CHỦ ĐANG TẮT");
                lblStatusBadge.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 12;");
            }
        });
    }

    @Override
    public void onServerError(String message) {
        Platform.runLater(() -> {
            appendLog("❌ [LỖI]: " + message);
            AlertHelper.showError("Lỗi Máy Chủ", message);
        });
    }

    public void shutdown() {
        serverEngine.stop();
    }
}
