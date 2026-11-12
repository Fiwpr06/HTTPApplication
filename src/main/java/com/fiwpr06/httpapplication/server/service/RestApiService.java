package com.fiwpr06.httpapplication.server.service;

import com.fiwpr06.httpapplication.common.constant.MimeType;
import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.common.util.FileUtils;
import com.fiwpr06.httpapplication.server.database.DatabaseManager;

import java.io.File;
import java.util.Arrays;

/**
 * Service chuyên trách xử lý các endpoint REST API dữ liệu JSON.
 * Cung cấp:
 * - /api/status: Trạng thái máy chủ, CSDL, Uptime, Bộ nhớ RAM
 * - /api/files: Danh sách tệp tin động phục vụ Web Portal
 * - /api/media-info: Thông tin metadata tệp media phục vụ đồng bộ UI
 */
public class RestApiService {

    public boolean isApiRequest(String path) {
        return path != null && path.startsWith("/api/");
    }

    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();
        if ("/api/status".equalsIgnoreCase(path)) {
            return handleApiStatus();
        }
        if ("/api/files".equalsIgnoreCase(path)) {
            return handleApiFiles(request, webRoot);
        }
        if ("/api/media-info".equalsIgnoreCase(path)) {
            return handleMediaInfo(webRoot);
        }
        return HttpResponse.notFound(path);
    }

    public HttpResponse handleApiStatus() {
        String dbType = DatabaseManager.getInstance().getActiveDbType().name();
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long uptimeSeconds = (System.currentTimeMillis() - DateTimeUtil.getServerStartTime()) / 1000;

        String json = String.format("{\"status\":\"UP\",\"database\":\"%s\",\"uptimeSeconds\":%d,\"memoryMb\":{\"free\":%d,\"total\":%d}}",
                dbType, uptimeSeconds, freeMemory, totalMemory);

        HttpResponse res = HttpResponse.ok(json, "application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Access-Control-Allow-Origin", "*");
        return res;
    }

    public HttpResponse handleApiFiles(HttpRequest request, File webRoot) {
        String dirParam = request.getQueryParam("dir");
        File targetDir = webRoot;
        String urlPrefix = "/";

        if (dirParam != null && !dirParam.trim().isEmpty()) {
            targetDir = FileUtils.resolveSafePath(webRoot, dirParam);
            urlPrefix = "/" + (dirParam.startsWith("/") ? dirParam.substring(1) : dirParam);
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
        }

        if (targetDir == null || !targetDir.exists() || !targetDir.isDirectory()) {
            return HttpResponse.ok("[]", "application/json; charset=UTF-8");
        }

        File[] files = targetDir.listFiles(f -> f.isFile() && !f.getName().startsWith("."));
        if (files == null) {
            files = new File[0];
        }

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            String name = f.getName();
            String url = urlPrefix + name;
            String type = MimeType.getMimeType(name);
            String sizeText = FileUtils.formatFileSize(f.length());

            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"name\":\"%s\",\"size\":%d,\"sizeText\":\"%s\",\"url\":\"%s\",\"mimeType\":\"%s\",\"time\":%d}",
                escapeJson(name), f.length(), sizeText, escapeJson(url), type, f.lastModified()
            ));
        }
        json.append("]");

        HttpResponse res = HttpResponse.ok(json.toString(), "application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Access-Control-Allow-Origin", "*");
        return res;
    }

    public HttpResponse handleMediaInfo(File webRoot) {
        File audioFile = new File(webRoot, "media/audio.mp3");
        File videoFile = new File(webRoot, "media/video.mp4");
        File pdfFile = new File(webRoot, "documents/sample.pdf");

        String json = String.format("{\"audio\":{\"name\":\"%s\",\"size\":%d,\"time\":%d}," +
                        "\"video\":{\"name\":\"%s\",\"size\":%d,\"time\":%d}," +
                        "\"pdf\":{\"name\":\"%s\",\"size\":%d,\"time\":%d}}",
                audioFile.getName(), audioFile.length(), audioFile.lastModified(),
                videoFile.getName(), videoFile.length(), videoFile.lastModified(),
                pdfFile.getName(), pdfFile.length(), pdfFile.lastModified()
        );

        HttpResponse res = HttpResponse.ok(json, "application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
        res.setHeader("Access-Control-Allow-Origin", "*");
        return res;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
