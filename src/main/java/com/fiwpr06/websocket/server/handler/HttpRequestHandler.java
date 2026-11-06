package com.fiwpr06.websocket.server.handler;

import com.fiwpr06.websocket.common.model.HttpRequest;
import com.fiwpr06.websocket.common.model.HttpResponse;

import java.io.File;

public interface HttpRequestHandler {
    HttpResponse handle(HttpRequest request, File webRoot);
}
