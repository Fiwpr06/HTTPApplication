package com.fiwpr06.httpapplication.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BrowserApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fiwpr06/httpapplication/client/browser-main.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 760);

        stage.setTitle("🌐 WebBrowser Pro — Trình Duyệt Web (Chrome / Cốc Cốc Style)");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
