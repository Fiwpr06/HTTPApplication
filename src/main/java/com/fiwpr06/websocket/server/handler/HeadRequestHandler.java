package com.fiwpr06.websocket.server.handler;

import com.fiwpr06.websocket.common.constant.HttpStatus;
import com.fiwpr06.websocket.common.constant.MimeType;
import com.fiwpr06.websocket.common.model.HttpRequest;
import com.fiwpr06.websocket.common.model.HttpResponse;
import com.fiwpr06.websocket.common.util.DateTimeUtil;
import com.fiwpr06.websocket.common.util.FileUtils;

import java.io.File;

public class HeadRequestHandler implements HttpRequestHandler {

    @Override
    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();

        File file = FileUtils.resolveSafePath(webRoot, path);
        if (file == null) {
            HttpResponse res = new HttpResponse(HttpStatus.NOT_FOUND, null, 0);
            res.setHeader("Content-Type", "text/html; charset=UTF-8");
            return res;
        }

        if (file.isDirectory()) {
            file = new File(file, "index.html");
        }

        if (!file.exists() || !file.isFile()) {
            HttpResponse res = new HttpResponse(HttpStatus.NOT_FOUND, null, 0);
            res.setHeader("Content-Type", "text/html; charset=UTF-8");
            return res;
        }

        long fileLength = file.length();
        String mimeType = MimeType.getMimeType(file.getName());

        HttpResponse res = new HttpResponse(HttpStatus.OK, null, fileLength);
        res.setHeader("Content-Type", mimeType);
        res.setHeader("Accept-Ranges", "bytes");
        res.setHeader("Last-Modified", DateTimeUtil.formatHttpDate(file.lastModified()));
        return res;
    }
}
