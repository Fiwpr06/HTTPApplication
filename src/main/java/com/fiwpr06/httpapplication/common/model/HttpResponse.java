package com.fiwpr06.httpapplication.common.model;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse implements AutoCloseable {
    private final HttpStatus status;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final InputStream bodyStream;
    private final long bodyLength;

    public HttpResponse(HttpStatus status, InputStream bodyStream, long bodyLength) {
        this.status = status;
        this.bodyStream = bodyStream;
        this.bodyLength = bodyLength;

        // Default HTTP headers
        headers.put("Server", "MediaHub/1.0 (Java 21)");
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

    public InputStream getBodyStream() {
        return bodyStream;
    }

    @Override
    public void close() {
        if (bodyStream != null) {
            try {
                bodyStream.close();
            } catch (IOException ignored) {
            }
        }
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
            try (bodyStream) {
                byte[] buffer = new byte[8192];
                long bytesRemaining = bodyLength;
                int read;
                while (bytesRemaining > 0 && (read = bodyStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                    out.write(buffer, 0, read);
                    bytesRemaining -= read;
                }
                out.flush();
            }
        } else if (bodyStream != null) {
            try {
                bodyStream.close();
            } catch (IOException ignored) {
            }
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

    public static HttpResponse errorPage(HttpStatus status, String title, String message, String path) {
        String html = "<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>" + status.getCode() + " " + title + " - MediaHub</title>" +
                "<style>" +
                "body{background:#F8FAFC;color:#0F172A;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;margin:0;padding:60px 20px;display:flex;justify-content:center;align-items:center;min-height:70vh;}" +
                ".card{background:#FFFFFF;border:1px solid #CBD5E1;border-radius:12px;padding:40px;max-width:520px;text-align:center;box-shadow:0 4px 6px -1px rgba(0,0,0,0.05);}" +
                ".code{font-size:64px;font-weight:800;color:#DC2626;margin:0;line-height:1;}" +
                "h2{color:#1E3A8A;margin:16px 0 8px;font-size:22px;}" +
                "p{color:#475569;font-size:14px;line-height:1.6;margin:10px 0 20px;}" +
                "code{background:#F1F5F9;color:#0284C7;padding:3px 8px;border-radius:4px;font-family:'Consolas',monospace;font-size:13px;}" +
                ".btn{display:inline-block;background:#1E3A8A;color:#FFFFFF;text-decoration:none;padding:10px 20px;border-radius:6px;font-weight:600;font-size:13px;transition:0.2s;}" +
                ".btn:hover{background:#1E40AF;}" +
                ".footer{margin-top:24px;font-size:12px;color:#94A3B8;}" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='code'>" + status.getCode() + "</div>" +
                "<h2>" + title + "</h2>" +
                "<p>" + message + (path != null ? "<br>Đường dẫn yêu cầu: <code>" + path + "</code>" : "") + "</p>" +
                "<a href='/' class='btn'>Về Trang Chủ MediaHub</a>" +
                "<div class='footer'>MediaHub Server / HTTP/1.1 (Java 21)</div>" +
                "</div></body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(status, new ByteArrayInputStream(bytes), bytes.length)
                .setHeader("Content-Type", "text/html; charset=UTF-8")
                .setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

    public static HttpResponse notFound(String path) {
        return errorPage(HttpStatus.NOT_FOUND, "Không Tìm Thấy Tài Nguyên",
                "Tài nguyên hoặc tệp tin bạn yêu cầu không tồn tại trên máy chủ.", path);
    }

    public static HttpResponse forbidden(String path) {
        return errorPage(HttpStatus.FORBIDDEN, "Truy Cập Bị Từ Chối",
                "Bạn không có quyền truy cập vào tài nguyên này trên máy chủ.", path);
    }

    public static HttpResponse internalError(String message) {
        return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi Máy Chủ Nội Bộ",
                "Máy chủ gặp sự cố trong quá trình xử lý yêu cầu: " + message, null);
    }

    public static HttpResponse badRequest(String message) {
        return errorPage(HttpStatus.BAD_REQUEST, "Yêu Cầu Không Hợp Lệ",
                message, null);
    }
}
