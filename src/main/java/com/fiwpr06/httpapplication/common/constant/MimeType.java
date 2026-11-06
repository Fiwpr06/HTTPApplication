package com.fiwpr06.httpapplication.common.constant;

import java.util.HashMap;
import java.util.Map;

public class MimeType {
    private static final Map<String, String> EXT_TO_MIME = new HashMap<>();

    static {
        EXT_TO_MIME.put("html", "text/html; charset=UTF-8");
        EXT_TO_MIME.put("htm", "text/html; charset=UTF-8");
        EXT_TO_MIME.put("css", "text/css; charset=UTF-8");
        EXT_TO_MIME.put("js", "application/javascript; charset=UTF-8");
        EXT_TO_MIME.put("json", "application/json; charset=UTF-8");
        EXT_TO_MIME.put("xml", "application/xml; charset=UTF-8");
        EXT_TO_MIME.put("txt", "text/plain; charset=UTF-8");

        // Images
        EXT_TO_MIME.put("png", "image/png");
        EXT_TO_MIME.put("jpg", "image/jpeg");
        EXT_TO_MIME.put("jpeg", "image/jpeg");
        EXT_TO_MIME.put("gif", "image/gif");
        EXT_TO_MIME.put("svg", "image/svg+xml");
        EXT_TO_MIME.put("ico", "image/x-icon");
        EXT_TO_MIME.put("webp", "image/webp");

        // Media & Documents
        EXT_TO_MIME.put("pdf", "application/pdf");
        EXT_TO_MIME.put("mp3", "audio/mpeg");
        EXT_TO_MIME.put("wav", "audio/wav");
        EXT_TO_MIME.put("ogg", "audio/ogg");
        EXT_TO_MIME.put("mp4", "video/mp4");
        EXT_TO_MIME.put("webm", "video/webm");
        EXT_TO_MIME.put("zip", "application/zip");
        EXT_TO_MIME.put("rar", "application/x-rar-compressed");
        EXT_TO_MIME.put("tar", "application/x-tar");
        EXT_TO_MIME.put("gz", "application/gzip");
    }

    public static String getMimeType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return EXT_TO_MIME.getOrDefault(ext, "application/octet-stream");
    }
}
