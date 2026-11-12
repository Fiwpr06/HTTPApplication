package com.fiwpr06.httpapplication.common.model;

import com.fiwpr06.httpapplication.common.constant.HttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;
    private final String clientIp;

    public HttpRequest(HttpMethod method, String path, String version, Map<String, String> queryParams,
                       Map<String, String> headers, byte[] body, String clientIp) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.queryParams = queryParams != null ? Collections.unmodifiableMap(queryParams) : Collections.emptyMap();
        this.headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
        this.body = body != null ? body : new byte[0];
        this.clientIp = clientIp != null ? clientIp : "127.0.0.1";
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        if (name == null) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public String getClientIp() {
        return clientIp;
    }

    /**
     * Parses Range header if present. Format: "bytes=start-end" or "bytes=start-"
     * Returns long[2] with {start, end} or null. If end is omitted, end will be -1.
     */
    public long[] getRangeHeader() {
        String range = getHeader("Range");
        if (range == null || !range.startsWith("bytes=")) {
            return null;
        }
        try {
            String value = range.substring(6).trim();
            String[] parts = value.split("-", 2);
            long start = Long.parseLong(parts[0].trim());
            long end = -1;
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                end = Long.parseLong(parts[1].trim());
            }
            return new long[]{start, end};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses raw InputStream from Client Socket into HttpRequest
     */
    public static HttpRequest parse(InputStream in, String clientIp) throws IOException {
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.trim().isEmpty()) {
            return null;
        }

        String[] lineParts = requestLine.trim().split("\\s+");
        if (lineParts.length < 2) {
            return null;
        }

        HttpMethod method = HttpMethod.fromString(lineParts[0]);
        String rawUri = lineParts[1];
        String version = lineParts.length > 2 ? lineParts[2] : "HTTP/1.1";

        // Parse path and query params
        String path = rawUri;
        Map<String, String> queryParams = new HashMap<>();
        int qIndex = rawUri.indexOf('?');
        if (qIndex != -1) {
            path = rawUri.substring(0, qIndex);
            String queryString = rawUri.substring(qIndex + 1);
            for (String pair : queryString.split("&")) {
                if (pair.isEmpty()) continue;
                String[] kv = pair.split("=", 2);
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                queryParams.put(k, v);
            }
        }
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        if (path.isEmpty()) {
            path = "/";
        }

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = readLine(in)) != null && !headerLine.trim().isEmpty()) {
            int colonIdx = headerLine.indexOf(':');
            if (colonIdx > 0) {
                String headerName = headerLine.substring(0, colonIdx).trim();
                String headerVal = headerLine.substring(colonIdx + 1).trim();
                headers.put(headerName, headerVal);
            }
        }

        // Parse body if Content-Length specified
        byte[] body = new byte[0];
        String contentLengthStr = null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("content-length".equalsIgnoreCase(entry.getKey())) {
                contentLengthStr = entry.getValue();
                break;
            }
        }

        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr.trim());
                if (contentLength > 0 && contentLength < 50 * 1024 * 1024) { // max 50MB
                    body = in.readNBytes(contentLength);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return new HttpRequest(method, path, version, queryParams, headers, body, clientIp);
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                baos.write(b);
            }
        }
        if (b == -1 && baos.size() == 0) {
            return null;
        }
        return baos.toString(StandardCharsets.UTF_8);
    }
}
