package com.fiwpr06.httpapplication.server.core;

import com.fiwpr06.httpapplication.common.model.AccessLog;

public interface ServerListener {
    void onRequestReceived(AccessLog log);
    void onStatusChanged(boolean running, int port, String ip);
    void onServerError(String message);
}
