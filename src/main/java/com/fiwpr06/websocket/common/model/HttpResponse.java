package com.fiwpr06.websocket.common.model;

import com.fiwpr06.websocket.common.constant.HttpStatus;
import com.fiwpr06.websocket.common.util.DateTimeUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpStatus status;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final InputStream bodyStream;
    private final long bodyLength;

    public HttpResponse(HttpStatus status, InputStream bodyStream, long bodyLength) {
        this.status = status;
        this.bodyStream = bodyStream;
        this.bodyLength = bodyLength;

        // Default HTTP headers
        headers.put("Server", "WebServerPro/1.0 (Java 21)");
        headers.put("Date", DateTimeUtil.formatHttpDate(System.currentTimeMillis()));
        headers.put("Connection", "close");
        if (bodyLength >= 0) {
            headers.put("Content-Length", String.valueOf(bodyLength));
        }
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public long getBodyLength() {
        return bodyLength;
    }

    public HttpResponse setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public void writeTo(OutputStream out, boolean headOnly) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status.getStatusLine()).append("\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        if (!headOnly && bodyStream != null && bodyLength > 0) {
            byte[] buffer = new byte[8192];
            long bytesRemaining = bodyLength;
            int read;
            while (bytesRemaining > 0 && (read = bodyStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                out.write(buffer, 0, read);
                bytesRemaining -= read;
            }
            out.flush();
        }
    }

    public static HttpResponse ok(byte[] body, String mimeType) {
        HttpResponse res = new HttpResponse(HttpStatus.OK, new ByteArrayInputStream(body), body.length);
        res.setHeader("Content-Type", mimeType);
        res.setHeader("Accept-Ranges", "bytes");
        return res;
    }

    public static HttpResponse ok(String text, String mimeType) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return ok(bytes, mimeType);
    }

    public static HttpResponse partialContent(InputStream stream, long start, long end, long totalLength, String mimeType) {
        long rangeLength = (end - start) + 1;
        HttpResponse res = new HttpResponse(HttpStatus.PARTIAL_CONTENT, stream, rangeLength);
        res.setHeader("Content-Type", mimeType);
        res.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + totalLength);
        res.setHeader("Accept-Ranges", "bytes");
        return res;
    }

    public static HttpResponse notFound(String path) {
        String html = "<!DOCTYPE html><html><head><meta charset='utf-8'><title>404 Not Found</title>" +
                "<style>body{background:#0b0e14;color:#f8fafc;font-family:sans-serif;text-align:center;padding-top:80px;}" +
                "h1{color:#ff3b56;font-size:48px;}p{color:#94a3b8;font-size:18px;}</style></head><body>" +
                "<h1>404 Not Found</h1><p>Tài nguyên được yêu cầu không tồn tại trên máy chủ: <code>" + path + "</code></p>" +
                "<a href='/' style='color:#00d2e6;text-decoration:none;'>◀ Quay lại Trang chủ</a></body></html>";
        HttpResponse res = ok(html, "text/html; charset=UTF-8");
        return new HttpResponse(HttpStatus.NOT_FOUND, new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)),
                html.getBytes(StandardCharsets.UTF_8).length)
                .setHeader("Content-Type", "text/html; charset=UTF-8");
    }

    public static HttpResponse badRequest(String message) {
        String text = "400 Bad Request: " + message;
        return new HttpResponse(HttpStatus.BAD_REQUEST, new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                text.getBytes(StandardCharsets.UTF_8).length)
                .setHeader("Content-Type", "text/plain; charset=UTF-8");
    }

    public static HttpResponse internalError(String message) {
        String text = "500 Internal Server Error: " + message;
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                text.getBytes(StandardCharsets.UTF_8).length)
                .setHeader("Content-Type", "text/plain; charset=UTF-8");
    }
}
