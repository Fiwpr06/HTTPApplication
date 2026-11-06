package com.fiwpr06.httpapplication.client.controller;

import com.fiwpr06.httpapplication.client.engine.HttpDirectClient;
import com.fiwpr06.httpapplication.common.util.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class PostTesterController implements Initializable {

    @FXML private TextField txtUrl;
    @FXML private ComboBox<String> cmbContentType;
    @FXML private TextArea txtRequestBody;
    @FXML private Button btnSendPost;
    @FXML private Label lblStatus;
    @FXML private TextArea txtResponseBody;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbContentType.getItems().addAll(
                "application/x-www-form-urlencoded",
                "application/json; charset=UTF-8",
                "text/plain; charset=UTF-8"
        );
        cmbContentType.getSelectionModel().select(0);
    }

    public void setUrl(String url) {
        if (url != null && !url.isEmpty()) {
            txtUrl.setText(url);
        }
    }

    @FXML
    public void handleSendPost() {
        String urlStr = txtUrl.getText().trim();
        if (urlStr.isEmpty()) {
            AlertHelper.showWarning("Thiếu URL", "Vui lòng nhập URL đích cho yêu cầu POST!");
            return;
        }

        String contentType = cmbContentType.getValue();
        String body = txtRequestBody.getText();

        lblStatus.setText("⏳ Đang gửi yêu cầu POST...");
        lblStatus.setStyle("-fx-text-fill: #0284C7; -fx-font-weight: bold;");
        btnSendPost.setDisable(true);

        new Thread(() -> {
            try {
                HttpDirectClient.HttpResult result = HttpDirectClient.executePost(urlStr, contentType, body);
                Platform.runLater(() -> {
                    btnSendPost.setDisable(false);
                    lblStatus.setText("Mã phản hồi: " + result.statusCode + " " + result.statusLine);
                    if (result.statusCode >= 200 && result.statusCode < 300) {
                        lblStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    } else {
                        lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    }
                    txtResponseBody.setText(result.body);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnSendPost.setDisable(false);
                    lblStatus.setText("Lỗi: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    txtResponseBody.setText("Chi tiết lỗi khi gửi POST: " + e.getMessage());
                });
            }
        }).start();
    }
}
