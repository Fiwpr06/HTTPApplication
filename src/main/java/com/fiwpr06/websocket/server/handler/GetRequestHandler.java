package com.fiwpr06.websocket.server.handler;

import com.fiwpr06.websocket.common.constant.HttpStatus;
import com.fiwpr06.websocket.common.constant.MimeType;
import com.fiwpr06.websocket.common.model.HttpRequest;
import com.fiwpr06.websocket.common.model.HttpResponse;
import com.fiwpr06.websocket.common.util.DateTimeUtil;
import com.fiwpr06.websocket.common.util.FileUtils;
import com.fiwpr06.websocket.server.database.DatabaseManager;

import java.io.*;

public class GetRequestHandler implements HttpRequestHandler {

    @Override
    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();

        // 1. Built-in REST API endpoints
        if ("/api/status".equalsIgnoreCase(path)) {
            return handleApiStatus();
        }

        // 2. Resolve requested static file
        File file = FileUtils.resolveSafePath(webRoot, path);
        if (file == null) {
            return HttpResponse.notFound(path);
        }

        if (file.isDirectory()) {
            file = new File(file, "index.html");
        }

        if (!file.exists() || !file.isFile()) {
            return HttpResponse.notFound(path);
        }

        long fileLength = file.length();
        String mimeType = MimeType.getMimeType(file.getName());

        // 3. Handle HTTP 206 Range Requests (For seekable MP4, MP3, and large PDF streams)
        long[] range = request.getRangeHeader();
        if (range != null) {
            long start = range[0];
            long end = range[1];

            if (end == -1 || end >= fileLength) {
                end = fileLength - 1;
            }

            if (start > end || start >= fileLength || start < 0) {
                HttpResponse res = new HttpResponse(HttpStatus.RANGE_NOT_SATISFIABLE, null, 0);
                res.setHeader("Content-Range", "bytes */" + fileLength);
                return res;
            }

            try {
                FileInputStream fis = new FileInputStream(file);
                long skipped = fis.skip(start);
                if (skipped < start) {
                    fis.close();
                    return HttpResponse.internalError("Cannot seek file stream to offset " + start);
                }

                HttpResponse res = HttpResponse.partialContent(fis, start, end, fileLength, mimeType);
                res.setHeader("Last-Modified", DateTimeUtil.formatHttpDate(file.lastModified()));
                return res;
            } catch (IOException e) {
                return HttpResponse.internalError("Error reading file range: " + e.getMessage());
            }
        }

        // 4. Standard 200 OK Full Content
        try {
            FileInputStream fis = new FileInputStream(file);
            HttpResponse res = new HttpResponse(HttpStatus.OK, fis, fileLength);
            res.setHeader("Content-Type", mimeType);
            res.setHeader("Accept-Ranges", "bytes");
            res.setHeader("Last-Modified", DateTimeUtil.formatHttpDate(file.lastModified()));
            return res;
        } catch (IOException e) {
            return HttpResponse.internalError("Error reading file: " + e.getMessage());
        }
    }

    private HttpResponse handleApiStatus() {
        String dbType = DatabaseManager.getInstance().getActiveDbType().name();
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        String json = String.format("""
            {
              "server": "WebServerPro",
              "status": "ONLINE",
              "protocol": "HTTP/1.1",
              "database": "%s",
              "jvmMemory": "%dMB / %dMB",
              "timestamp": "%s"
            }
            """, dbType, freeMemory, totalMemory, DateTimeUtil.formatHttpDate(System.currentTimeMillis()));

        return HttpResponse.ok(json, "application/json; charset=UTF-8");
    }
}
