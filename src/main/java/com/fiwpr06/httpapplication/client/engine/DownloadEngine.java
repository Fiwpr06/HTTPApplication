package com.fiwpr06.httpapplication.client.engine;

import com.fiwpr06.httpapplication.common.model.DownloadTask;
import com.fiwpr06.httpapplication.common.util.FileUtils;
import javafx.application.Platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DownloadEngine {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static DownloadTask startDownload(String urlStr, File saveDir, Consumer<DownloadTask> onComplete, Consumer<String> onError) {
        String fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);
        if (fileName.isEmpty() || fileName.contains("?")) {
            fileName = "download_" + System.currentTimeMillis();
        }

        File destFile = new File(saveDir, fileName);
        DownloadTask task = new DownloadTask(fileName, urlStr, destFile.getAbsolutePath());

        executor.execute(() -> {
            Platform.runLater(() -> task.setStatus(DownloadTask.Status.DOWNLOADING));

            long startTime = System.currentTimeMillis();
            long totalRead = 0;

            try {
                URL url = URI.create(urlStr).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "WebServerPro-Browser/1.0");
                conn.connect();

                long contentLength = conn.getContentLengthLong();
                Platform.runLater(() -> task.setTotalBytes(contentLength));

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(destFile)) {

                    byte[] buffer = new byte[8192];
                    int read;
                    long lastSpeedCalcTime = startTime;
                    long bytesSinceLastCalc = 0;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        totalRead += read;
                        bytesSinceLastCalc += read;

                        long now = System.currentTimeMillis();
                        if (now - lastSpeedCalcTime >= 500) {
                            double seconds = (now - lastSpeedCalcTime) / 1000.0;
                            double speedVal = bytesSinceLastCalc / seconds;
                            String speedStr = FileUtils.formatFileSize((long) speedVal) + "/s";

                            long finalTotalRead = totalRead;
                            Platform.runLater(() -> {
                                task.setDownloadedBytes(finalTotalRead);
                                task.setSpeed(speedStr);
                            });

                            lastSpeedCalcTime = now;
                            bytesSinceLastCalc = 0;
                        }
                    }

                    long finalRead = totalRead;
                    Platform.runLater(() -> {
                        task.setDownloadedBytes(finalRead);
                        task.setStatus(DownloadTask.Status.COMPLETED);
                        task.setSpeed("0 KB/s");
                        if (onComplete != null) onComplete.accept(task);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    task.setStatus(DownloadTask.Status.FAILED);
                    task.setSpeed("Lỗi");
                    if (onError != null) onError.accept(e.getMessage());
                });
            }
        });

        return task;
    }
}
