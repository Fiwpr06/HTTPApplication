package com.fiwpr06.websocket.client.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpDirectClient {
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static class HttpResult {
        public int statusCode;
        public String statusLine;
        public Map<String, List<String>> headers;
        public String body;

        public HttpResult(int statusCode, String statusLine, Map<String, List<String>> headers, String body) {
            this.statusCode = statusCode;
            this.statusLine = statusLine;
            this.headers = headers != null ? headers : Collections.emptyMap();
            this.body = body != null ? body : "";
        }
    }

    public static HttpResult executeHead(String urlStr) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "WebServerPro-Browser/1.0 (JavaFX)")
                .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        int code = response.statusCode();
        return new HttpResult(code, "HTTP/1.1 " + code, response.headers().map(), "");
    }

    public static HttpResult executePost(String urlStr, String contentType, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "WebServerPro-Browser/1.0 (JavaFX)");

        if (contentType != null && !contentType.isEmpty()) {
            builder.header("Content-Type", contentType);
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        return new HttpResult(code, "HTTP/1.1 " + code, response.headers().map(), response.body());
    }

    public static HttpResult executeGet(String urlStr) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .GET()
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "WebServerPro-Browser/1.0 (JavaFX)")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        return new HttpResult(code, "HTTP/1.1 " + code, response.headers().map(), response.body());
    }
}
