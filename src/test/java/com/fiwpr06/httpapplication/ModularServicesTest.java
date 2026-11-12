package com.fiwpr06.httpapplication;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.server.service.DirectoryIndexService;
import com.fiwpr06.httpapplication.server.service.FileUploadService;
import com.fiwpr06.httpapplication.server.service.RestApiService;
import com.fiwpr06.httpapplication.server.service.StaticFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ModularServicesTest {

    @Test
    public void testDirectoryIndexServiceGeneratesHtml(@TempDir File tempDir) throws IOException {
        new File(tempDir, "document.docx").createNewFile();
        new File(tempDir, "song.mp3").createNewFile();

        DirectoryIndexService service = new DirectoryIndexService();
        try (HttpResponse res = service.render(tempDir, "/downloads/")) {
            assertNotNull(res);
            assertEquals(HttpStatus.OK, res.getStatus());
            assertTrue(res.getHeaders().get("Content-Type").contains("text/html"));
        }
    }

    @Test
    public void testStaticFileServiceNormalAndRange(@TempDir File tempDir) throws IOException {
        File sample = new File(tempDir, "test.txt");
        try (FileOutputStream fos = new FileOutputStream(sample)) {
            fos.write("0123456789".getBytes(StandardCharsets.UTF_8));
        }

        StaticFileService service = new StaticFileService();

        // 1. Normal 200 OK
        try (HttpResponse res200 = service.serveFile(sample, null)) {
            assertEquals(HttpStatus.OK, res200.getStatus());
            assertTrue(res200.getHeaders().get("Content-Type").startsWith("text/plain"));
            assertEquals("bytes", res200.getHeaders().get("Accept-Ranges"));
        }

        // 2. Range 206 Partial Content (bytes=2-6)
        String raw = "GET /test.txt HTTP/1.1\r\nHost: localhost:8080\r\nRange: bytes=2-6\r\n\r\n";
        HttpRequest reqRange = HttpRequest.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");

        try (HttpResponse res206 = service.serveFile(sample, reqRange)) {
            assertEquals(HttpStatus.PARTIAL_CONTENT, res206.getStatus());
            assertEquals("bytes 2-6/10", res206.getHeaders().get("Content-Range"));
        }
    }

    @Test
    public void testRestApiServiceEndpoints(@TempDir File tempDir) throws IOException {
        RestApiService service = new RestApiService();

        // 1. /api/status
        try (HttpResponse statusRes = service.handleApiStatus()) {
            assertEquals(HttpStatus.OK, statusRes.getStatus());
            assertTrue(statusRes.getHeaders().get("Content-Type").contains("application/json"));
        }

        // 2. /api/files
        File mediaDir = new File(tempDir, "media");
        mediaDir.mkdir();
        new File(mediaDir, "beat.mp3").createNewFile();

        String raw = "GET /api/files?dir=media HTTP/1.1\r\nHost: localhost:8080\r\n\r\n";
        HttpRequest reqFiles = HttpRequest.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");

        try (HttpResponse filesRes = service.handleApiFiles(reqFiles, tempDir)) {
            assertEquals(HttpStatus.OK, filesRes.getStatus());
            assertTrue(filesRes.getHeaders().get("Content-Type").contains("application/json"));
        }

        // 3. /api/media-info
        try (HttpResponse mediaInfoRes = service.handleMediaInfo(tempDir)) {
            assertEquals(HttpStatus.OK, mediaInfoRes.getStatus());
            assertTrue(mediaInfoRes.getHeaders().get("Content-Type").contains("application/json"));
        }
    }

    @Test
    public void testFileUploadServiceSubmitAndUpload(@TempDir File tempDir) throws IOException {
        FileUploadService service = new FileUploadService();

        // 1. Submit Form
        String formBody = "fullName=Nguyen+Van+A&studentId=SV001&feedback=Tot";
        String rawPost = "POST /api/submit HTTP/1.1\r\nHost: localhost:8080\r\nContent-Length: " + formBody.length() + "\r\n\r\n" + formBody;
        HttpRequest reqForm = HttpRequest.parse(new ByteArrayInputStream(rawPost.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");

        try (HttpResponse formRes = service.handleSubmitForm(reqForm)) {
            assertEquals(HttpStatus.OK, formRes.getStatus());
            assertTrue(formRes.getHeaders().get("Content-Type").contains("application/json"));
        }

        // 2. Binary File Upload
        String fileContent = "MediaHub Modular Architecture Test";
        byte[] body = fileContent.getBytes(StandardCharsets.UTF_8);
        String rawUpload = "POST /api/upload HTTP/1.1\r\nHost: localhost:8080\r\nX-File-Name: unit_test.txt\r\nContent-Length: " + body.length + "\r\n\r\n" + fileContent;
        HttpRequest reqUpload = HttpRequest.parse(new ByteArrayInputStream(rawUpload.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");

        try (HttpResponse uploadRes = service.handleUpload(reqUpload, tempDir)) {
            assertEquals(HttpStatus.OK, uploadRes.getStatus());
            assertTrue(uploadRes.getHeaders().get("Content-Type").contains("application/json"));
        }

        File saved = new File(tempDir, "uploads/unit_test.txt");
        assertTrue(saved.exists());
        assertEquals(body.length, saved.length());
    }
}
