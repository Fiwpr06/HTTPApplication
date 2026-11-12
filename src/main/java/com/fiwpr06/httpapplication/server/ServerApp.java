package com.fiwpr06.httpapplication.server;

import com.fiwpr06.httpapplication.server.controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {
    private ServerController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fiwpr06/httpapplication/server/server-dashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1050, 720);
        controller = loader.getController();

        stage.setTitle("MediaHub — Local Media & Document Server");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.shutdown();
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
