package com.fiwpr06.websocket.common.model;

import java.time.LocalDateTime;

public class HistoryItem {
    private int id;
    private String title;
    private String url;
    private LocalDateTime visitTime;
    private boolean starred;

    public HistoryItem(int id, String title, String url, LocalDateTime visitTime, boolean starred) {
        this.id = id;
        this.title = title != null && !title.isEmpty() ? title : url;
        this.url = url;
        this.visitTime = visitTime != null ? visitTime : LocalDateTime.now();
        this.starred = starred;
    }

    public HistoryItem(String title, String url) {
        this(0, title, url, LocalDateTime.now(), false);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public LocalDateTime getVisitTime() { return visitTime; }
    public boolean isStarred() { return starred; }

    public void setTitle(String title) { this.title = title; }
    public void setStarred(boolean starred) { this.starred = starred; }
}
