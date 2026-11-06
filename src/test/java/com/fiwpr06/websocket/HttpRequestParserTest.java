package com.fiwpr06.websocket;

import com.fiwpr06.websocket.common.constant.HttpMethod;
import com.fiwpr06.websocket.common.model.HttpRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestParserTest {

    @Test
    public void testParseGetRequest() throws IOException {
        String raw = "GET /documents/sample.pdf?view=full&zoom=100 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "Accept: */*\r\n\r\n";

        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.parse(in, "192.168.1.15");

        assertNotNull(req);
        assertEquals(HttpMethod.GET, req.getMethod());
        assertEquals("/documents/sample.pdf", req.getPath());
        assertEquals("HTTP/1.1", req.getVersion());
        assertEquals("192.168.1.15", req.getClientIp());
        assertEquals("localhost:8080", req.getHeader("Host"));
        assertEquals("full", req.getQueryParam("view"));
        assertEquals("100", req.getQueryParam("zoom"));
    }

    @Test
    public void testParsePostWithBody() throws IOException {
        String body = "username=testuser&score=100";
        String raw = "POST /api/submit HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: " + body.length() + "\r\n\r\n" +
                body;

        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.parse(in, "127.0.0.1");

        assertNotNull(req);
        assertEquals(HttpMethod.POST, req.getMethod());
        assertEquals("/api/submit", req.getPath());
        assertEquals(body, req.getBodyAsString());
    }

    @Test
    public void testParseRangeHeader() throws IOException {
        String raw = "GET /media/video.mp4 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Range: bytes=1024-2048\r\n\r\n";

        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.parse(in, "127.0.0.1");

        assertNotNull(req);
        long[] range = req.getRangeHeader();
        assertNotNull(range);
        assertEquals(1024L, range[0]);
        assertEquals(2048L, range[1]);
    }

    @Test
    public void testParseOpenEndedRange() throws IOException {
        String raw = "GET /media/audio.mp3 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Range: bytes=5000-\r\n\r\n";

        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.parse(in, "127.0.0.1");

        assertNotNull(req);
        long[] range = req.getRangeHeader();
        assertNotNull(range);
        assertEquals(5000L, range[0]);
        assertEquals(-1L, range[1]);
    }
}
