package com.fiwpr06.httpapplication.server.handler;

import com.fiwpr06.httpapplication.common.model.HttpRequest;
import com.fiwpr06.httpapplication.common.model.HttpResponse;

import java.io.File;

public interface HttpRequestHandler {
    HttpResponse handle(HttpRequest request, File webRoot);
}
