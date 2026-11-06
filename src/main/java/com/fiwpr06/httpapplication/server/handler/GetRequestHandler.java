package com.fiwpr06.httpapplication.server.handler;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.constant.MimeType;
import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.common.util.FileUtils;
import com.fiwpr06.httpapplication.server.database.DatabaseManager;

import java.io.*;

public class GetRequestHandler implements HttpRequestHandler {

    @Override
    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();

        // 1. Built-in REST API endpoints
        if ("/api/status".equalsIgnoreCase(path)) {
            return handleApiStatus();
        }
        if ("/api/media-info".equalsIgnoreCase(path)) {
            return handleMediaInfo(webRoot);
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
                res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
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
                // Anti-cache headers to ensure immediate media update without server restart
                res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                res.setHeader("Pragma", "no-cache");
                res.setHeader("Expires", "0");
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
            // Anti-cache headers to prevent browser disk caching
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
            return res;
        } catch (IOException e) {
            return HttpResponse.internalError("Error reading file: " + e.getMessage());
        }
    }

    private HttpResponse handleMediaInfo(File webRoot) {
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

    private HttpResponse handleApiStatus() {
        String dbType = DatabaseManager.getInstance().getActiveDbType().name();
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long uptimeSeconds = (System.currentTimeMillis() - DateTimeUtil.getServerStartTime()) / 1000;

        String json = String.format("{\"status\":\"UP\",\"database\":\"%s\",\"uptimeSeconds\":%d,\"memoryMb\":{\"free\":%d,\"total\":%d}}",
                dbType, uptimeSeconds, freeMemory, totalMemory);

        HttpResponse res = HttpResponse.ok(json, "application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return res;
    }
}
