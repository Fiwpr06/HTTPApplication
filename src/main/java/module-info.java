module com.fiwpr06.websocket {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires jdk.jsobject;

    opens com.fiwpr06.websocket to javafx.fxml;
    opens com.fiwpr06.websocket.server to javafx.fxml;
    opens com.fiwpr06.websocket.server.controller to javafx.fxml;
    opens com.fiwpr06.websocket.client to javafx.fxml;
    opens com.fiwpr06.websocket.client.controller to javafx.fxml;
    opens com.fiwpr06.websocket.common.model to javafx.base;

    exports com.fiwpr06.websocket;
    exports com.fiwpr06.websocket.server;
    exports com.fiwpr06.websocket.client;
    exports com.fiwpr06.websocket.common.model;
    exports com.fiwpr06.websocket.common.constant;
}
