module com.fiwpr06.httpapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires jdk.jsobject;
    requires java.desktop;

    opens com.fiwpr06.httpapplication.common.model to javafx.base;
    opens com.fiwpr06.httpapplication.server to javafx.fxml, javafx.graphics;
    opens com.fiwpr06.httpapplication.server.controller to javafx.fxml;
    opens com.fiwpr06.httpapplication.client to javafx.fxml, javafx.graphics;
    opens com.fiwpr06.httpapplication.client.controller to javafx.fxml;

    exports com.fiwpr06.httpapplication.common.model;
    exports com.fiwpr06.httpapplication.common.constant;
    exports com.fiwpr06.httpapplication.common.util;
    exports com.fiwpr06.httpapplication.server.database;
    exports com.fiwpr06.httpapplication.server.core;
    exports com.fiwpr06.httpapplication.server.handler;
    exports com.fiwpr06.httpapplication.server;
    exports com.fiwpr06.httpapplication.server.controller;
    exports com.fiwpr06.httpapplication.client;
    exports com.fiwpr06.httpapplication.client.controller;
    exports com.fiwpr06.httpapplication.client.engine;
}
