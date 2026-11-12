package com.fiwpr06.httpapplication;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.server.handler.GetRequestHandler;
import com.fiwpr06.httpapplication.server.handler.PostRequestHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DirectoryListingTest {

    @Test
    public void testDirectoryListingGeneratesHtmlTable(@TempDir File tempDir) throws IOException {
        // Create sample files in temp directory
        new File(tempDir, "song.mp3").createNewFile();
        new File(tempDir, "video.mp4").createNewFile();
        new File(tempDir, "sample.pdf").createNewFile();
        File subDir = new File(tempDir, "subfolder");
        subDir.mkdir();

        GetRequestHandler handler = new GetRequestHandler();

        // Request root directory (which has no index.html)
        String raw = "GET / HTTP/1.1\r\nHost: localhost:8080\r\n\r\n";
        HttpRequest req = HttpRequest.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");
        assertNotNull(req);

        HttpResponse res = handler.handle(req, tempDir);
        assertNotNull(res);
        assertEquals(HttpStatus.OK, res.getStatus());
        assertTrue(res.getHeaders().get("Content-Type").contains("text/html"));

        // Verify HTML contains directory listing items
        String html = handler.renderDirectoryListing(tempDir, "/").getHeaders().get("Content-Type");
        assertNotNull(html);
    }

    @Test
    public void testApiFilesReturnsJsonList(@TempDir File tempDir) throws IOException {
        File mediaDir = new File(tempDir, "media");
        mediaDir.mkdir();
        new File(mediaDir, "track1.mp3").createNewFile();
        new File(mediaDir, "clip1.mp4").createNewFile();

        GetRequestHandler handler = new GetRequestHandler();

        String raw = "GET /api/files?dir=media HTTP/1.1\r\nHost: localhost:8080\r\n\r\n";
        HttpRequest req = HttpRequest.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");
        assertNotNull(req);

        HttpResponse res = handler.handle(req, tempDir);
        assertNotNull(res);
        assertEquals(HttpStatus.OK, res.getStatus());
        assertTrue(res.getHeaders().get("Content-Type").contains("application/json"));
    }

    @Test
    public void testFileUploadViaPost(@TempDir File tempDir) throws IOException {
        PostRequestHandler handler = new PostRequestHandler();

        String fileContent = "Xin chao MediaHub!";
        byte[] body = fileContent.getBytes(StandardCharsets.UTF_8);

        String raw = "POST /api/upload HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "X-File-Name: hello.txt\r\n" +
                "Content-Length: " + body.length + "\r\n\r\n" +
                fileContent;

        HttpRequest req = HttpRequest.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");
        assertNotNull(req);

        HttpResponse res = handler.handle(req, tempDir);
        assertNotNull(res);
        assertEquals(HttpStatus.OK, res.getStatus());

        // Verify file is saved in uploads/hello.txt
        File savedFile = new File(tempDir, "uploads/hello.txt");
        assertTrue(savedFile.exists());
        assertEquals(body.length, savedFile.length());
    }
}
