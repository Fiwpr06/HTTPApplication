package com.fiwpr06.httpapplication.server.handler;

import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.common.util.FileUtils;
import com.fiwpr06.httpapplication.server.service.DirectoryIndexService;
import com.fiwpr06.httpapplication.server.service.RestApiService;
import com.fiwpr06.httpapplication.server.service.StaticFileService;

import java.io.File;

/**
 * Router điều hướng cho HTTP GET requests.
 * Áp dụng nguyên lý Single Responsibility (SOLID):
 * Lớp này chỉ đóng vai trò phân luồng (Router), ủy quyền xử lý thực tế cho các Service chuyên biệt:
 * - {@link RestApiService}: Xử lý các endpoint JSON /api/*
 * - {@link DirectoryIndexService}: Tự động sinh trang HTML duyệt thư mục khi không có index.html
 * - {@link StaticFileService}: Phục vụ tệp tĩnh (200 OK & 206 Partial Content)
 */
public class GetRequestHandler implements HttpRequestHandler {

    private final RestApiService restApiService;
    private final DirectoryIndexService directoryIndexService;
    private final StaticFileService staticFileService;

    public GetRequestHandler() {
        this(new RestApiService(), new DirectoryIndexService(), new StaticFileService());
    }

    public GetRequestHandler(RestApiService restApiService,
                             DirectoryIndexService directoryIndexService,
                             StaticFileService staticFileService) {
        this.restApiService = restApiService;
        this.directoryIndexService = directoryIndexService;
        this.staticFileService = staticFileService;
    }

    @Override
    public HttpResponse handle(HttpRequest request, File webRoot) {
        String path = request.getPath();

        // 1. Phân luồng REST API JSON (/api/status, /api/files, /api/media-info)
        if (restApiService.isApiRequest(path)) {
            return restApiService.handle(request, webRoot);
        }

        // 2. Tìm kiếm tệp hoặc thư mục an toàn
        File file = FileUtils.resolveSafePath(webRoot, path);
        if (file == null) {
            return HttpResponse.notFound(path);
        }

        // 3. Nếu là thư mục: ưu tiên index.html / index.htm, nếu không có thì Auto Directory Indexing
        if (file.isDirectory()) {
            File indexHtml = new File(file, "index.html");
            if (indexHtml.exists() && indexHtml.isFile()) {
                file = indexHtml;
            } else {
                File indexHtm = new File(file, "index.htm");
                if (indexHtm.exists() && indexHtm.isFile()) {
                    file = indexHtm;
                } else {
                    return directoryIndexService.render(file, path);
                }
            }
        }

        // 4. Phục vụ tệp tĩnh (200 OK hoặc 206 Partial Content Range Request)
        return staticFileService.serveFile(file, request);
    }

    /**
     * Ủy quyền tạo trang danh mục thư mục cho DirectoryIndexService.
     */
    public HttpResponse renderDirectoryListing(File dir, String requestPath) {
        return directoryIndexService.render(dir, requestPath);
    }
}
