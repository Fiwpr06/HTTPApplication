package com.fiwpr06.websocket.server.core;

import com.fiwpr06.websocket.common.constant.HttpMethod;
import com.fiwpr06.websocket.common.constant.HttpStatus;
import com.fiwpr06.websocket.common.model.AccessLog;
import com.fiwpr06.websocket.common.model.HttpRequest;
import com.fiwpr06.websocket.common.model.HttpResponse;
import com.fiwpr06.websocket.server.database.AccessLogDao;
import com.fiwpr06.websocket.server.handler.GetRequestHandler;
import com.fiwpr06.websocket.server.handler.HeadRequestHandler;
import com.fiwpr06.websocket.server.handler.PostRequestHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final File webRoot;
    private final AccessLogDao logDao;
    private final ServerListener listener;

    private final GetRequestHandler getHandler = new GetRequestHandler();
    private final HeadRequestHandler headHandler = new HeadRequestHandler();
    private final PostRequestHandler postHandler = new PostRequestHandler();

    public ClientHandler(Socket socket, File webRoot, AccessLogDao logDao, ServerListener listener) {
        this.socket = socket;
        this.webRoot = webRoot;
        this.logDao = logDao;
        this.listener = listener;
    }

    @Override
    public void run() {
        String clientIp = socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : "127.0.0.1";
        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            HttpRequest request = HttpRequest.parse(in, clientIp);
            if (request == null) {
                return;
            }

            HttpResponse response;
            if (request.getMethod() == HttpMethod.GET) {
                response = getHandler.handle(request, webRoot);
            } else if (request.getMethod() == HttpMethod.HEAD) {
                response = headHandler.handle(request, webRoot);
            } else if (request.getMethod() == HttpMethod.POST) {
                response = postHandler.handle(request, webRoot);
            } else {
                response = new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, null, 0)
                        .setHeader("Allow", "GET, HEAD, POST");
            }

            // Send response back to Client
            response.writeTo(out, request.getMethod() == HttpMethod.HEAD);

            // Create AccessLog
            AccessLog log = new AccessLog(
                    clientIp,
                    request.getMethod().name(),
                    request.getPath(),
                    response.getStatus().getCode(),
                    response.getBodyLength(),
                    request.getHeader("User-Agent")
            );

            // Persist log to Database (MySQL or SQLite)
            if (logDao != null) {
                logDao.insertLog(log);
            }

            // Notify UI listener for real-time display
            if (listener != null) {
                listener.onRequestReceived(log);
            }

        } catch (IOException ignored) {
        }
    }
}
