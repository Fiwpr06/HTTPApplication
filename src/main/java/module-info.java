module com.fiwpr06.websocket {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires jdk.jsobject;

    opens com.fiwpr06.websocket.common.model to javafx.base;

    exports com.fiwpr06.websocket.common.model;
    exports com.fiwpr06.websocket.common.constant;
    exports com.fiwpr06.websocket.common.util;
    exports com.fiwpr06.websocket.server.database;
}
