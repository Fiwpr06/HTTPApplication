package com.fiwpr06.httpapplication.common.model;

import javafx.beans.property.*;

public class DownloadTask {
    public enum Status {
        QUEUED("Đang chờ"),
        DOWNLOADING("Đang tải"),
        COMPLETED("Hoàn thành"),
        FAILED("Lỗi");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final StringProperty fileName = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final LongProperty totalBytes = new SimpleLongProperty(0);
    private final LongProperty downloadedBytes = new SimpleLongProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.QUEUED);
    private final StringProperty speed = new SimpleStringProperty("0 KB/s");
    private final StringProperty savePath = new SimpleStringProperty();

    public DownloadTask(String fileName, String url, String savePath) {
        this.fileName.set(fileName);
        this.url.set(url);
        this.savePath.set(savePath);
    }

    public String getFileName() { return fileName.get(); }
    public StringProperty fileNameProperty() { return fileName; }

    public String getUrl() { return url.get(); }
    public StringProperty urlProperty() { return url; }

    public long getTotalBytes() { return totalBytes.get(); }
    public LongProperty totalBytesProperty() { return totalBytes; }
    public void setTotalBytes(long value) { this.totalBytes.set(value); }

    public long getDownloadedBytes() { return downloadedBytes.get(); }
    public LongProperty downloadedBytesProperty() { return downloadedBytes; }
    public void setDownloadedBytes(long value) {
        this.downloadedBytes.set(value);
        if (totalBytes.get() > 0) {
            this.progress.set((double) value / totalBytes.get());
        }
    }

    public double getProgress() { return progress.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double value) { this.progress.set(value); }

    public Status getStatus() { return status.get(); }
    public ObjectProperty<Status> statusProperty() { return status; }
    public void setStatus(Status value) { this.status.set(value); }

    public String getSpeed() { return speed.get(); }
    public StringProperty speedProperty() { return speed; }
    public void setSpeed(String value) { this.speed.set(value); }

    public String getSavePath() { return savePath.get(); }
    public StringProperty savePathProperty() { return savePath; }
}
