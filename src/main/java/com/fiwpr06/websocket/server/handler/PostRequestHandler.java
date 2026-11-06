package com.fiwpr06.websocket.server.handler;

import com.fiwpr06.websocket.common.model.HttpRequest;
import com.fiwpr06.websocket.common.model.HttpResponse;
import com.fiwpr06.websocket.common.util.DateTimeUtil;
import com.fiwpr06.websocket.server.database.DatabaseManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class PostRequestHandler implements HttpRequestHandler {

    @Override
    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();

        // 1. Form Submission Endpoint
        if ("/api/submit".equalsIgnoreCase(path)) {
            return handleSubmitForm(request);
        }

        // 2. Direct File Upload Endpoint
        if ("/api/upload".equalsIgnoreCase(path)) {
            return handleFileUpload(request, webRoot);
        }

        // Default POST Echo
        String body = request.getBodyAsString();
        String responseText = "HTTP POST Received at " + path + "\nLength: " + request.getBody().length + " bytes\nBody: " + body;
        return HttpResponse.ok(responseText, "text/plain; charset=UTF-8");
    }

    private HttpResponse handleSubmitForm(HttpRequest request) {
        String body = request.getBodyAsString();
        Map<String, String> formData = new HashMap<>();

        if (body != null && !body.isEmpty()) {
            for (String pair : body.split("&")) {
                if (pair.isEmpty()) continue;
                String[] kv = pair.split("=", 2);
                try {
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                    formData.put(k, v);
                } catch (Exception ignored) {
                }
            }
        }

        String fullName = formData.getOrDefault("fullName", "Khách ẩn danh");
        String studentId = formData.getOrDefault("studentId", "N/A");
        String feedback = formData.getOrDefault("feedback", "");

        String json = String.format("""
            {
              "status": "SUCCESS",
              "message": "Dữ liệu HTTP POST đã được xử lý thành công!",
              "sinhVien": "%s",
              "mssv": "%s",
              "noiDung": "%s",
              "clientIp": "%s",
              "receivedAt": "%s"
            }
            """, fullName, studentId, feedback, request.getClientIp(), DateTimeUtil.formatHttpDate(System.currentTimeMillis()));

        return HttpResponse.ok(json, "application/json; charset=UTF-8");
    }

    private HttpResponse handleFileUpload(HttpRequest request, File webRoot) {
        String fileName = request.getHeader("X-File-Name");
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "upload_" + System.currentTimeMillis() + ".dat";
        }

        File uploadsDir = new File(webRoot, "uploads");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        File destFile = new File(uploadsDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            fos.write(request.getBody());
        } catch (IOException e) {
            return HttpResponse.internalError("Cannot save uploaded file: " + e.getMessage());
        }

        // Record in uploaded_files database table
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO uploaded_files (file_name, file_size, file_type, uploader_ip) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, fileName);
            ps.setLong(2, destFile.length());
            ps.setString(3, request.getHeader("Content-Type"));
            ps.setString(4, request.getClientIp());
            ps.executeUpdate();
        } catch (Exception ignored) {
        }

        String json = String.format("""
            {
              "status": "UPLOAD_SUCCESS",
              "fileName": "%s",
              "savedPath": "/uploads/%s",
              "fileSize": %d,
              "uploadedAt": "%s"
            }
            """, fileName, fileName, destFile.length(), DateTimeUtil.formatHttpDate(System.currentTimeMillis()));

        return HttpResponse.ok(json, "application/json; charset=UTF-8");
    }
}
