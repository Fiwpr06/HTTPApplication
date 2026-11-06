package com.fiwpr06.websocket.server.core;

import com.fiwpr06.websocket.common.model.AccessLog;

public interface ServerListener {
    void onRequestReceived(AccessLog log);
    void onStatusChanged(boolean running, int port, String ip);
    void onServerError(String message);
}
