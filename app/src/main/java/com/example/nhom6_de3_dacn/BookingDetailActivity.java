package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit; // 👈 Import thư viện tính ngày

public class BookingDetailActivity extends AppCompatActivity {

    private ImageView btnBack, imgRoom;
    // 👇 Khai báo thêm tvTotalDays
    private TextView tvBookingId, tvStatus, tvRoomName, tvCheckIn, tvCheckOut, tvTotalDays, tvRoomPriceTotal, tvFinalPrice;
    private MaterialButton btnCancel, btnEdit, btnPayment;

    private LinearLayout layoutServiceSection, containerServices;
    private TextView tvAddService;

    private FirebaseFirestore db;
    private String bookingId;
    private Booking currentBooking;

    private static final long TIER_GOLD_LIMIT = 20_000_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        db = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("bookingId");

        initViews();
        loadBookingDetails();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgRoom = findViewById(R.id.imgRoomDetail);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvStatus = findViewById(R.id.tvStatusTag);
        tvRoomName = findViewById(R.id.tvRoomNameDetail);
        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);

        // 👇 Ánh xạ TextView hiển thị số đêm (QUAN TRỌNG)
        tvTotalDays = findViewById(R.id.tvTotalDays);

        tvRoomPriceTotal = findViewById(R.id.tvRoomPriceTotal);
        tvFinalPrice = findViewById(R.id.tvFinalPrice);

        btnPayment = findViewById(R.id.btnPayment);
        btnCancel = findViewById(R.id.btnCancelBooking);
        btnEdit = findViewById(R.id.btnEditBooking);

        layoutServiceSection = findViewById(R.id.layoutServiceSection);
        containerServices = findViewById(R.id.containerServices);
        tvAddService = findViewById(R.id.tvAddService);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> checkCancellationCondition());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(BookingDetailActivity.this, EditBookingActivity.class);
            intent.putExtra("bookingId", bookingId);
            startActivity(intent);
        });

        if (tvAddService != null) {
            tvAddService.setOnClickListener(v -> Toast.makeText(this, "Vui lòng bấm nút 'Sửa đổi' để thêm dịch vụ", Toast.LENGTH_SHORT).show());
        }

        btnPayment.setOnClickListener(v -> {
            if (currentBooking != null) {
                Intent intent = new Intent(BookingDetailActivity.this, PaymentActivity.class);
                intent.putExtra("bookingId", bookingId);
                intent.putExtra("totalPrice", currentBooking.getTotalPrice());
                intent.putExtra("roomName", currentBooking.getRoomName());
                startActivity(intent);
            }
        });
    }

    private void loadBookingDetails() {
        if (bookingId == null) return;

        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentBooking = document.toObject(Booking.class);
                        if (currentBooking != null) {
                            displayData(currentBooking);
                        }
                    }
                });
    }

    // 👇 HÀM HIỂN THỊ DỮ LIỆU (ĐÃ SỬA LOGIC)
    private void displayData(Booking booking) {
        // 1️⃣ XỬ LÝ MÃ ĐƠN: Lấy 8 số CUỐI để khác biệt
        String displayId = bookingId;
        if (bookingId != null && bookingId.length() > 8) {
            // Lấy từ vị trí (độ dài - 8) đến hết -> Lấy đuôi
            displayId = bookingId.substring(bookingId.length() - 8);
        }
        tvBookingId.setText("Mã đơn: #" + displayId);

        // 2️⃣ XỬ LÝ NGÀY & SỐ ĐÊM
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        tvCheckIn.setText(sdf.format(new Date(booking.getCheckInDate())));
        tvCheckOut.setText(sdf.format(new Date(booking.getCheckOutDate())));

        // 👇 Tính khoảng cách: (CheckOut - CheckIn) / (mili-giây 1 ngày)
        long diff = booking.getCheckOutDate() - booking.getCheckInDate();
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (days < 1) days = 1; // Tối thiểu 1 đêm
        tvTotalDays.setText(days + " đêm"); // Ghi đè lên chữ "3 đêm" cũ

        // 3️⃣ HIỂN THỊ CÁC THÔNG TIN KHÁC
        tvRoomName.setText(booking.getRoomName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        String priceStr = formatter.format(booking.getTotalPrice()) + "đ";
        tvRoomPriceTotal.setText(priceStr);
        tvFinalPrice.setText(priceStr);

        if (booking.getRoomImage() != null && !booking.getRoomImage().isEmpty()) {
            Glide.with(this).load(booking.getRoomImage()).into(imgRoom);
        }

        // --- DỊCH VỤ ---
        List<String> services = booking.getServices();
        if (services == null || services.isEmpty()) {
            layoutServiceSection.setVisibility(View.GONE);
        } else {
            layoutServiceSection.setVisibility(View.VISIBLE);
            containerServices.removeAllViews();
            for (String serviceName : services) {
                addServiceRow(serviceName);
            }
        }

        // --- TRẠNG THÁI ---
        String status = booking.getStatus();
        String payStatus = booking.getPaymentStatus();
        long totalPrice = booking.getTotalPrice();

        if ("PENDING".equals(status)) {
            tvStatus.setText("Chờ xác nhận");
            tvStatus.setTextColor(Color.parseColor("#E65100"));
            tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));

            if (totalPrice > 3000000 && payStatus == null) {
                btnPayment.setVisibility(View.VISIBLE);
                btnPayment.setText("Thanh toán cọc (Bắt buộc)");
            } else {
                btnPayment.setVisibility(View.VISIBLE);
                btnPayment.setText("Thanh toán ngay");
            }
            btnCancel.setVisibility(View.VISIBLE);

        } else if ("CONFIRMED".equals(status)) {
            tvStatus.setText("Đã xác nhận");
            tvStatus.setTextColor(Color.parseColor("#1976D2"));
            tvStatus.setBackgroundColor(Color.parseColor("#E3F2FD"));
            btnPayment.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("Đã hủy");
            tvStatus.setTextColor(Color.parseColor("#D32F2F"));
            tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
            btnPayment.setVisibility(View.GONE);
            btnCancel.setEnabled(false);
            btnEdit.setEnabled(false);
        }

        if ("PAID_FULL".equals(payStatus)) {
            btnPayment.setVisibility(View.GONE);
            tvStatus.setText("Đã thanh toán");
        }
    }

    private void addServiceRow(String serviceName) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 10, 0, 10);
        itemLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.radiobutton_on_background);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(40, 40);
        iconParams.setMargins(0, 0, 16, 0);

        TextView tvName = new TextView(this);
        tvName.setText(serviceName);
        tvName.setTextSize(14);
        tvName.setTextColor(Color.BLACK);

        itemLayout.addView(icon, iconParams);
        itemLayout.addView(tvName);

        containerServices.addView(itemLayout);
    }

    private void checkCancellationCondition() {
        if (currentBooking == null) return;
        if (currentBooking.getAmountPaid() == 0) {
            showConfirmDialog("Xác nhận hủy", "Bạn chưa thanh toán nên được hủy phòng miễn phí.", 0);
            return;
        }
        calculateRefundAmount();
    }

    private void calculateRefundAmount() {
        String userId = FirebaseAuth.getInstance().getUid();
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long totalSpent = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            if (status != null && !"CANCELLED".equals(status)) {
                                Double price = doc.getDouble("totalPrice");
                                if (price != null) totalSpent += price.longValue();
                            }
                        }
                        processRefundBasedOnTier(totalSpent);
                    }
                });
    }

    private void processRefundBasedOnTier(long totalSpent) {
        long amountPaid = currentBooking.getAmountPaid();
        long refundAmount;
        String message;

        if (totalSpent >= TIER_GOLD_LIMIT) {
            refundAmount = amountPaid;
            message = "Bạn là thành viên Vàng trở lên.\nĐược miễn phí hủy phòng.\nHoàn lại: " + formatMoney(refundAmount);
        } else {
            refundAmount = (long) (amountPaid * 0.8);
            long fee = amountPaid - refundAmount;
            message = "Phí hủy phòng (20%): " + formatMoney(fee) +
                    "\nSố tiền hoàn lại: " + formatMoney(refundAmount) +
                    "\n(Nâng hạng Vàng để được miễn phí hủy)";
        }
        showConfirmDialog("Chính sách hoàn tiền", message, refundAmount);
    }

    private void showConfirmDialog(String title, String message, long refundAmount) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đồng ý Hủy", (dialog, which) -> performCancel(refundAmount))
                .setNegativeButton("Giữ phòng", null)
                .show();
    }

    private void performCancel(long refundAmount) {
        db.collection("bookings").document(bookingId)
                .update(
                        "status", "CANCELLED",
                        "refundAmount", refundAmount
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã hủy phòng!", Toast.LENGTH_SHORT).show();
                    loadBookingDetails();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi hủy: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String formatMoney(long amount) {
        return new DecimalFormat("#,###").format(amount) + "đ";
    }
}