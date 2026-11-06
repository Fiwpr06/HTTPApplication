package com.fiwpr06.websocket.common.model;

import java.time.LocalDateTime;

public class AccessLog {
    private int id;
    private String clientIp;
    private String method;
    private String uri;
    private int statusCode;
    private long contentLength;
    private String userAgent;
    private LocalDateTime timestamp;

    public AccessLog(int id, String clientIp, String method, String uri, int statusCode,
                     long contentLength, String userAgent, LocalDateTime timestamp) {
        this.id = id;
        this.clientIp = clientIp;
        this.method = method;
        this.uri = uri;
        this.statusCode = statusCode;
        this.contentLength = contentLength;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }

    public AccessLog(String clientIp, String method, String uri, int statusCode,
                     long contentLength, String userAgent) {
        this(0, clientIp, method, uri, statusCode, contentLength, userAgent, LocalDateTime.now());
    }

    public int getId() { return id; }
    public String getClientIp() { return clientIp; }
    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public int getStatusCode() { return statusCode; }
    public long getContentLength() { return contentLength; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
