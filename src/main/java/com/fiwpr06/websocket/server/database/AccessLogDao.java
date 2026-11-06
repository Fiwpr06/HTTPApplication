package com.fiwpr06.websocket.server.database;

import com.fiwpr06.websocket.common.model.AccessLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccessLogDao {
    private final DatabaseManager dbManager;

    public AccessLogDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void insertLog(AccessLog log) {
        String sql = "INSERT INTO web_access_logs (client_ip, method, uri, status_code, content_length, user_agent) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getClientIp());
            ps.setString(2, log.getMethod());
            ps.setString(3, log.getUri());
            ps.setInt(4, log.getStatusCode());
            ps.setLong(5, log.getContentLength());
            ps.setString(6, log.getUserAgent());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AccessLog> getRecentLogs(int limit) {
        List<AccessLog> list = new ArrayList<>();
        String sql = "SELECT id, client_ip, method, uri, status_code, content_length, user_agent, created_at FROM web_access_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String clientIp = rs.getString("client_ip");
                    String method = rs.getString("method");
                    String uri = rs.getString("uri");
                    int statusCode = rs.getInt("status_code");
                    long length = rs.getLong("content_length");
                    String ua = rs.getString("user_agent");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime time = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    list.add(new AccessLog(id, clientIp, method, uri, statusCode, length, ua, time));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public long getTotalRequests() {
        String sql = "SELECT COUNT(*) FROM web_access_logs";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getTotalBytesSent() {
        String sql = "SELECT SUM(content_length) FROM web_access_logs";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
