package com.fiwpr06.websocket.server.database;

import com.fiwpr06.websocket.common.model.HistoryItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryDao {
    private final DatabaseManager dbManager;

    public HistoryDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void insertHistory(HistoryItem item) {
        String sql = "INSERT INTO browsing_history (title, url) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getTitle());
            ps.setString(2, item.getUrl());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<HistoryItem> getAllHistory() {
        List<HistoryItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, visit_time, is_starred FROM browsing_history ORDER BY id DESC LIMIT 500";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String url = rs.getString("url");
                Timestamp ts = rs.getTimestamp("visit_time");
                LocalDateTime time = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                boolean starred = rs.getInt("is_starred") == 1;

                list.add(new HistoryItem(id, title, url, time, starred));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void clearHistory() {
        String sql = "DELETE FROM browsing_history";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteHistoryItem(int id) {
        String sql = "DELETE FROM browsing_history WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
