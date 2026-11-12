package com.fiwpr06.httpapplication.server.service;

import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.server.database.DatabaseManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * Service chuyên trách tiếp nhận và lưu trữ tệp tin tải lên (File Upload),
 * đồng thời ghi nhận nhật ký lưu trữ vào cơ sở dữ liệu.
 */
public class FileUploadService {

    public HttpResponse handleUpload(HttpRequest request, File webRoot) {
        String rawFileName = request.getHeader("X-File-Name");
        if (rawFileName == null || rawFileName.trim().isEmpty()) {
            rawFileName = request.getQueryParam("name");
        }

        String fileName = null;
        if (rawFileName != null && !rawFileName.trim().isEmpty()) {
            try {
                fileName = URLDecoder.decode(rawFileName, StandardCharsets.UTF_8);
            } catch (Exception e) {
                fileName = rawFileName;
            }
            // Chống Path Traversal: Chỉ lấy tên tệp, loại bỏ đường dẫn thư mục
            fileName = new File(fileName).getName();
        }

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

        // Ghi nhận vào bảng CSDL uploaded_files
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
              "status": "SUCCESS",
              "message": "Tải lên tệp thành công!",
              "fileName": "%s",
              "savedPath": "/uploads/%s",
              "fileSize": %d,
              "uploadedAt": "%s"
            }
            """, fileName, fileName, destFile.length(), DateTimeUtil.formatHttpDate(System.currentTimeMillis()));

        HttpResponse res = HttpResponse.ok(json, "application/json; charset=UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return res;
    }

    public HttpResponse handleSubmitForm(HttpRequest request) {
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

        HttpResponse res = HttpResponse.ok(json, "application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return res;
    }
}
