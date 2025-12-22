package com.example.nhom6_de3_dacn;

public class Review {
    private String id;
    private String userId;
    private String roomId;
    private String userName;
    private String bookingId;
    private float rating;
    private String comment;
    private long timestamp;

    private String managerReply;
    private boolean isReplied;

    public Review() {}

    // --- Getter & Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public String getRoomId() { return roomId; }
    public String getUserName() { return userName != null ? userName : "Khách hàng"; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }

    public String getManagerReply() { return managerReply; }
    public void setManagerReply(String managerReply) { this.managerReply = managerReply; }

    public boolean isReplied() { return isReplied; }
    public void setReplied(boolean replied) { isReplied = replied; }
}