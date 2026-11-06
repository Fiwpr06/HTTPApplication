package com.fiwpr06.httpapplication.server.core;

import com.fiwpr06.httpapplication.common.util.NetworkUtil;
import com.fiwpr06.httpapplication.server.database.AccessLogDao;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpServerEngine {
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService threadPool;
    private Thread acceptorThread;

    private int port = 8080;
    private File webRoot = new File("web_root");
    private final AccessLogDao logDao = new AccessLogDao();
    private ServerListener listener;

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public synchronized boolean start(int port, File webRoot) {
        if (running.get()) {
            return true;
        }

        this.port = port;
        this.webRoot = webRoot != null ? webRoot : new File("web_root");
        if (!this.webRoot.exists()) {
            this.webRoot.mkdirs();
        }

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            running.set(true);
            threadPool = Executors.newCachedThreadPool();

            acceptorThread = new Thread(this::listenLoop, "HttpServer-Acceptor");
            acceptorThread.setDaemon(true);
            acceptorThread.start();

            if (listener != null) {
                listener.onStatusChanged(true, port, NetworkUtil.getLocalIpv4Address());
            }
            return true;
        } catch (IOException e) {
            if (listener != null) {
                listener.onServerError("Không thể khởi động Server tại cổng " + port + ": " + e.getMessage());
            }
            return false;
        }
    }

    private void listenLoop() {
        while (running.get() && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(30000); // 30s timeout
                threadPool.execute(new ClientHandler(clientSocket, webRoot, logDao, listener));
            } catch (IOException e) {
                if (!running.get()) {
                    break;
                }
            }
        }
    }

    public synchronized void stop() {
        if (!running.get()) {
            return;
        }
        running.set(false);

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }

        if (listener != null) {
            listener.onStatusChanged(false, port, NetworkUtil.getLocalIpv4Address());
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }

    public File getWebRoot() {
        return webRoot;
    }
}
