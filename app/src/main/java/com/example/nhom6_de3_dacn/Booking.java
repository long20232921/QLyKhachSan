//package com.example.nhom6_de3_dacn;
//
//public class Booking {
//    private String bookingId;    // Mã đơn (VD: BOOK-172839...)
//    private String userId;       // ID người đặt (Lấy từ Firebase Auth)
//    private String roomId;       // ID phòng
//    private String roomName;     // Tên phòng (Lưu lại để hiển thị lịch sử cho nhanh)
//    private String roomImage;    // Ảnh phòng
//    private String customerName; // Tên người ở
//    private String customerPhone;// SĐT người ở
//    private String customerEmail;// Email
//    private long checkInDate;    // Ngày đến (dạng số milisecond)
//    private long checkOutDate;   // Ngày đi
//    private long totalPrice;     // Tổng tiền
//    private String status;       // Trạng thái: "PENDING" (Chờ duyệt), "CONFIRMED" (Đã duyệt), "CANCELLED" (Hủy)
//
//    // ⚠️ QUAN TRỌNG: Firebase bắt buộc phải có Constructor rỗng này
//    public Booking() { }
//
//    // Constructor đầy đủ để mình tạo đơn cho dễ
//    public Booking(String bookingId, String userId, String roomId, String roomName, String roomImage, String customerName, String customerPhone, String customerEmail, long checkInDate, long checkOutDate, long totalPrice, String status) {
//        this.bookingId = bookingId;
//        this.userId = userId;
//        this.roomId = roomId;
//        this.roomName = roomName;
//        this.roomImage = roomImage;
//        this.customerName = customerName;
//        this.customerPhone = customerPhone;
//        this.customerEmail = customerEmail;
//        this.checkInDate = checkInDate;
//        this.checkOutDate = checkOutDate;
//        this.totalPrice = totalPrice;
//        this.status = status;
//    }
//
//    // Getter (Firebase dùng mấy cái này để đọc dữ liệu)
//    public String getBookingId() { return bookingId; }
//    public String getUserId() { return userId; }
//    public String getRoomId() { return roomId; }
//    public String getRoomName() { return roomName; }
//    public String getRoomImage() { return roomImage; }
//    public String getCustomerName() { return customerName; }
//    public String getCustomerPhone() { return customerPhone; }
//    public String getCustomerEmail() { return customerEmail; }
//    public long getCheckInDate() { return checkInDate; }
//    public long getCheckOutDate() { return checkOutDate; }
//    public long getTotalPrice() { return totalPrice; }
//    public String getStatus() { return status; }
//}
// sửa
package com.example.nhom6_de3_dacn;

import java.io.Serializable;

public class Booking implements Serializable {
    private String bookingId;    // ID tài liệu trên Firestore
    private String userId;
    private String roomId;
    private String roomName;
    private String roomImage;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private long checkInDate;
    private long checkOutDate;
    private long totalPrice;
    private String status;

    // Constructor rỗng bắt buộc cho Firebase
    public Booking() { }

    // Constructor đầy đủ
    public Booking(String bookingId, String userId, String roomId, String roomName, String roomImage, String customerName, String customerPhone, String customerEmail, long checkInDate, long checkOutDate, long totalPrice, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomImage = roomImage;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // --- GETTERS ---
    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getRoomImage() { return roomImage; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerEmail() { return customerEmail; }
    public long getCheckInDate() { return checkInDate; }
    public long getCheckOutDate() { return checkOutDate; }
    public long getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }

    // --- SETTERS (Phần bạn cần cập nhật để hết lỗi) ---
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public void setRoomImage(String roomImage) { this.roomImage = roomImage; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCheckInDate(long checkInDate) { this.checkInDate = checkInDate; }
    public void setCheckOutDate(long checkOutDate) { this.checkOutDate = checkOutDate; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
}