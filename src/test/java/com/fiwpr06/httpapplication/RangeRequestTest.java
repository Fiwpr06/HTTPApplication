package com.fiwpr06.httpapplication;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.server.handler.GetRequestHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RangeRequestTest {

    @Test
    public void testPartialContentHandling(@TempDir File tempDir) throws IOException {
        // Create a test file of 100 bytes
        File testFile = new File(tempDir, "test.dat");
        byte[] dummyData = new byte[100];
        for (int i = 0; i < 100; i++) {
            dummyData[i] = (byte) i;
        }
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(dummyData);
        }

        GetRequestHandler handler = new GetRequestHandler();

        // Request bytes 10-19 (10 bytes)
        String raw = "GET /test.dat HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Range: bytes=10-19\r\n\r\n";
        HttpRequest req = HttpRequest.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)), "127.0.0.1");
        assertNotNull(req);

        HttpResponse res = handler.handle(req, tempDir);
        assertNotNull(res);
        assertEquals(HttpStatus.PARTIAL_CONTENT, res.getStatus());
        assertEquals(10, res.getBodyLength());
        assertEquals("bytes 10-19/100", res.getHeaders().get("Content-Range"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out, false);

        byte[] output = out.toByteArray();
        assertTrue(output.length > 10);
    }
}
