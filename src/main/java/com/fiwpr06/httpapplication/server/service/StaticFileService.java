package com.fiwpr06.httpapplication.server.service;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.constant.MimeType;
import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Service chuyên trách phục vụ tệp tĩnh (Static File Serving).
 * Hỗ trợ:
 * - HTTP 200 OK (Full Content)
 * - HTTP 206 Partial Content (Range Requests cho seek video/audio/pdf)
 * - Cấu hình Anti-Cache Headers để nội dung cập nhật tức thì
 */
public class StaticFileService {

    public HttpResponse serveFile(File file, HttpRequest request) {
        if (file == null || !file.exists() || !file.isFile()) {
            return HttpResponse.notFound(request != null ? request.getPath() : "");
        }

        long fileLength = file.length();
        String mimeType = MimeType.getMimeType(file.getName());

        // 1. Xử lý HTTP 206 Range Requests (Phục vụ stream Video, Audio, PDF dung lượng lớn)
        long[] range = request != null ? request.getRangeHeader() : null;
        if (range != null) {
            long start = range[0];
            long end = range[1];

            if (end == -1 || end >= fileLength) {
                end = fileLength - 1;
            }

            if (start > end || start >= fileLength || start < 0) {
                HttpResponse res = new HttpResponse(HttpStatus.RANGE_NOT_SATISFIABLE, null, 0);
                res.setHeader("Content-Range", "bytes */" + fileLength);
                res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                return res;
            }

            try {
                FileInputStream fis = new FileInputStream(file);
                long skipped = fis.skip(start);
                if (skipped < start) {
                    fis.close();
                    return HttpResponse.internalError("Cannot seek file stream to offset " + start);
                }

                HttpResponse res = HttpResponse.partialContent(fis, start, end, fileLength, mimeType);
                res.setHeader("Last-Modified", DateTimeUtil.formatHttpDate(file.lastModified()));
                // Anti-cache headers để trình duyệt luôn nhận phiên bản mới nhất khi đổi tệp
                res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                res.setHeader("Pragma", "no-cache");
                res.setHeader("Expires", "0");
                return res;
            } catch (IOException e) {
                return HttpResponse.internalError("Error reading file range: " + e.getMessage());
            }
        }

        // 2. HTTP 200 OK Full Content
        try {
            FileInputStream fis = new FileInputStream(file);
            HttpResponse res = new HttpResponse(HttpStatus.OK, fis, fileLength);
            res.setHeader("Content-Type", mimeType);
            res.setHeader("Accept-Ranges", "bytes");
            res.setHeader("Last-Modified", DateTimeUtil.formatHttpDate(file.lastModified()));
            // Anti-cache headers
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
            return res;
        } catch (IOException e) {
            return HttpResponse.internalError("Error reading file: " + e.getMessage());
        }
    }
}
