package com.example.nhom6_de3_dacn;

public class Notification {
    private String id;
    private String userId;      // Thông báo gửi cho ai
    private String title;       // Tiêu đề (VD: Quản lý đã trả lời...)
    private String message;     // Nội dung
    private String type;        // "REPLY" (trả lời), "PROMO" (khuyến mãi), "BOOKING" (đặt phòng)
    private String targetId;    // ID của đơn hàng/phòng để khi bấm vào sẽ chuyển hướng
    private boolean isRead;     // Đã đọc chưa (để hiện chấm đỏ)
    private long timestamp;

    public Notification() {} // Constructor rỗng

    public Notification(String userId, String title, String message, String type, String targetId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.targetId = targetId;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getTargetId() { return targetId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public long getTimestamp() { return timestamp; }
}