package com.fiwpr06.httpapplication.server.handler;

import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.server.service.FileUploadService;

import java.io.File;

/**
 * Router điều hướng cho HTTP POST requests.
 * Áp dụng nguyên lý Single Responsibility (SOLID):
 * Lớp này chỉ phân luồng, ủy quyền việc lưu trữ file và xử lý form cho {@link FileUploadService}.
 */
public class PostRequestHandler implements HttpRequestHandler {

    private final FileUploadService fileUploadService;

    public PostRequestHandler() {
        this(new FileUploadService());
    }

    public PostRequestHandler(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Override
    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();

        // 1. Endpoint Submit Form ý kiến
        if ("/api/submit".equalsIgnoreCase(path)) {
            return fileUploadService.handleSubmitForm(request);
        }

        // 2. Endpoint Upload tệp tin lên máy chủ
        if ("/api/upload".equalsIgnoreCase(path)) {
            return fileUploadService.handleUpload(request, webRoot);
        }

        // 3. Phản hồi mặc định nếu gửi POST tới endpoint khác
        String body = request.getBodyAsString();
        String responseText = "HTTP POST Received at " + path + "\nLength: " + request.getBody().length + " bytes\nBody: " + body;
        return HttpResponse.ok(responseText, "text/plain; charset=UTF-8");
    }
}
