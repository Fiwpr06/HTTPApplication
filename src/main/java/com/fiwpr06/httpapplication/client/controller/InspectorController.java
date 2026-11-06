package com.fiwpr06.httpapplication.client.controller;

import com.fiwpr06.httpapplication.client.engine.HttpDirectClient;
import com.fiwpr06.httpapplication.common.util.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class InspectorController implements Initializable {

    @FXML private TextField txtUrl;
    @FXML private Button btnSendHead;
    @FXML private Label lblStatusCode;
    @FXML private TableView<HeaderEntry> tblHeaders;
    @FXML private TableColumn<HeaderEntry, String> colHeaderName;
    @FXML private TableColumn<HeaderEntry, String> colHeaderValue;

    public static class HeaderEntry {
        private final String name;
        private final String value;

        public HeaderEntry(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public String getValue() { return value; }
    }

    private final ObservableList<HeaderEntry> headerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colHeaderName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colHeaderValue.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getValue()));
        tblHeaders.setItems(headerList);
    }

    public void setUrl(String url) {
        if (url != null && !url.isEmpty()) {
            txtUrl.setText(url);
            handleSendHead();
        }
    }

    @FXML
    public void handleSendHead() {
        String urlStr = txtUrl.getText().trim();
        if (urlStr.isEmpty()) {
            AlertHelper.showWarning("Thiếu URL", "Vui lòng nhập đường dẫn URL cần kiểm tra HEAD!");
            return;
        }

        lblStatusCode.setText("⏳ Đang gửi HEAD request...");
        lblStatusCode.setStyle("-fx-text-fill: #0284C7; -fx-font-weight: bold;");
        btnSendHead.setDisable(true);

        new Thread(() -> {
            try {
                HttpDirectClient.HttpResult result = HttpDirectClient.executeHead(urlStr);
                Platform.runLater(() -> {
                    btnSendHead.setDisable(false);
                    lblStatusCode.setText(result.statusCode + " " + (result.statusCode == 200 ? "OK" : result.statusLine));
                    if (result.statusCode >= 200 && result.statusCode < 300) {
                        lblStatusCode.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    } else if (result.statusCode >= 300 && result.statusCode < 400) {
                        lblStatusCode.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
                    } else {
                        lblStatusCode.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    }

                    headerList.clear();
                    for (Map.Entry<String, List<String>> entry : result.headers.entrySet()) {
                        String name = entry.getKey() != null ? entry.getKey() : ":status";
                        String val = String.join(", ", entry.getValue());
                        headerList.add(new HeaderEntry(name, val));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnSendHead.setDisable(false);
                    lblStatusCode.setText("Lỗi kết nối: " + e.getMessage());
                    lblStatusCode.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                });
            }
        }).start();
    }
}
