package com.fiwpr06.httpapplication.server.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;

    public enum DbType {
        MYSQL,
        SQLITE
    }

    private DbType activeDbType = DbType.SQLITE;
    private String connectionUrl;
    private String username;
    private String password;

    private DatabaseManager() {
        initConfiguration();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public DbType getActiveDbType() {
        return activeDbType;
    }

    private void initConfiguration() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception ignored) {
            }
        }

        String host = props.getProperty("db.host", "127.0.0.1");
        String port = props.getProperty("db.port", "3306");
        String dbName = props.getProperty("db.name", "http_app_db");
        String user = props.getProperty("db.user", "root");
        String pass = props.getProperty("db.password", "");
        String sqliteFile = props.getProperty("db.sqlite.file", "web_database.db");

        // Try MySQL first
        boolean mysqlSuccess = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(serverUrl, user, pass);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                mysqlSuccess = true;
            }

            if (mysqlSuccess) {
                this.connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                this.username = user;
                this.password = pass;
                this.activeDbType = DbType.MYSQL;
                initSchema();
                return;
            }
        } catch (Exception e) {
            // MySQL unavailable -> fallback to SQLite
        }

        // Fallback to SQLite
        try {
            Class.forName("org.sqlite.JDBC");
            this.connectionUrl = "jdbc:sqlite:" + sqliteFile;
            this.username = null;
            this.password = null;
            this.activeDbType = DbType.SQLITE;
            initSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (activeDbType == DbType.MYSQL) {
            return DriverManager.getConnection(connectionUrl, username, password);
        } else {
            return DriverManager.getConnection(connectionUrl);
        }
    }

    private void initSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            if (activeDbType == DbType.MYSQL) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS web_access_logs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        client_ip VARCHAR(50) NOT NULL,
                        method VARCHAR(10) NOT NULL,
                        uri TEXT NOT NULL,
                        status_code INT NOT NULL,
                        content_length BIGINT NOT NULL,
                        user_agent TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS browsing_history (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        url TEXT NOT NULL,
                        visit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        is_starred TINYINT DEFAULT 0
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS uploaded_files (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        file_name VARCHAR(255) NOT NULL,
                        file_size BIGINT NOT NULL,
                        file_type VARCHAR(100),
                        uploader_ip VARCHAR(50),
                        upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);
            } else {
                // SQLite syntax
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS web_access_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        client_ip TEXT NOT NULL,
                        method TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        status_code INTEGER NOT NULL,
                        content_length INTEGER NOT NULL,
                        user_agent TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS browsing_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        url TEXT NOT NULL,
                        visit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        is_starred INTEGER DEFAULT 0
                    );
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS uploaded_files (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        file_name TEXT NOT NULL,
                        file_size INTEGER NOT NULL,
                        file_type TEXT,
                        uploader_ip TEXT,
                        upload_time DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
