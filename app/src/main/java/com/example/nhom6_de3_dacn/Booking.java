package com.example.nhom6_de3_dacn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Booking implements Serializable {
    private String bookingId;    // Mã đơn
    private String userId;       // ID người đặt
    private String roomId;       // ID phòng
    private String roomName;     // Tên phòng
    private String roomImage;    // Ảnh phòng
    private String customerName; // Tên người ở
    private String customerPhone;// SĐT
    private String customerEmail;// Email
    private long checkInDate;    // Ngày đến
    private long checkOutDate;   // Ngày đi
    private long totalPrice;     // Tổng tiền
    private String status;       // Trạng thái: PENDING, CONFIRMED, CANCELLED

    // 👇 CÁC TRƯỜNG MỚI (Thanh toán & Dịch vụ)
    private String paymentStatus; // "PAID_FULL", "PAID_DEPOSIT"
    private long amountPaid;      // Số tiền đã thanh toán
    private List<String> services; // Danh sách dịch vụ đi kèm

    // Constructor rỗng (Bắt buộc cho Firebase)
    public Booking() {
        this.services = new ArrayList<>();
    }

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
        this.services = new ArrayList<>();
    }

    // --- GETTER & SETTER ---
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRoomImage() { return roomImage; }
    public void setRoomImage(String roomImage) { this.roomImage = roomImage; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public long getCheckInDate() { return checkInDate; }
    public void setCheckInDate(long checkInDate) { this.checkInDate = checkInDate; }

    public long getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(long checkOutDate) { this.checkOutDate = checkOutDate; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // 👇 Getter/Setter cho 3 biến mới
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public long getAmountPaid() { return amountPaid; }
    public void setAmountPaid(long amountPaid) { this.amountPaid = amountPaid; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    public void addService(String service) {
        if (this.services == null) this.services = new ArrayList<>();
        this.services.add(service);
    }
}