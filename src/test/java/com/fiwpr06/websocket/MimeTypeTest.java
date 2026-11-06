package com.fiwpr06.websocket;

import com.fiwpr06.websocket.common.constant.MimeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MimeTypeTest {

    @Test
    public void testMimeTypeMappings() {
        assertEquals("text/html; charset=UTF-8", MimeType.getMimeType("index.html"));
        assertEquals("text/css; charset=UTF-8", MimeType.getMimeType("style.css"));
        assertEquals("application/javascript; charset=UTF-8", MimeType.getMimeType("app.js"));
        assertEquals("video/mp4", MimeType.getMimeType("movie.mp4"));
        assertEquals("audio/mpeg", MimeType.getMimeType("song.mp3"));
        assertEquals("application/pdf", MimeType.getMimeType("document.pdf"));
        assertEquals("image/png", MimeType.getMimeType("logo.png"));
        assertEquals("application/zip", MimeType.getMimeType("archive.zip"));
        assertEquals("application/octet-stream", MimeType.getMimeType("unknown_file"));
    }
}
