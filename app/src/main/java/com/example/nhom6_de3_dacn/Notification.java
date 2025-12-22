package com.example.nhom6_de3_dacn;

import com.google.firebase.firestore.Exclude;

public class Notification {
    @Exclude
    private String id;

    private String userId;
    private String title;
    private String message;
    private String type;
    private String targetId;
    private long timestamp;
    private boolean isRead;

    public Notification() { }

    public Notification(String userId, String title, String message, String type, String targetId, long timestamp) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.targetId = targetId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // --- GETTER & SETTER ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}