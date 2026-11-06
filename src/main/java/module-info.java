module com.fiwpr06.websocket {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires jdk.jsobject;
    requires java.desktop;

    opens com.fiwpr06.websocket.common.model to javafx.base;
    opens com.fiwpr06.websocket.server to javafx.fxml, javafx.graphics;
    opens com.fiwpr06.websocket.server.controller to javafx.fxml;

    exports com.fiwpr06.websocket.common.model;
    exports com.fiwpr06.websocket.common.constant;
    exports com.fiwpr06.websocket.common.util;
    exports com.fiwpr06.websocket.server.database;
    exports com.fiwpr06.websocket.server.core;
    exports com.fiwpr06.websocket.server.handler;
    exports com.fiwpr06.websocket.server;
    exports com.fiwpr06.websocket.server.controller;
}
