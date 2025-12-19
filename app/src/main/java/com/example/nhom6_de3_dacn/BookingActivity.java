package com.example.nhom6_de3_dacn;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class BookingActivity extends AppCompatActivity {

    // Views
    private EditText etName, etEmail, etPhone;
    private TextView tvCheckIn, tvCheckOut, tvTotalDays;
    private TextView tvRoomName, tvBookingPrice, tvRoomCharge, tvServiceCharge, tvTax, tvFinalTotal, lblRoomCharge;
    private ImageView imgRoom, btnBack;
    private SwitchMaterial switchBreakfast, switchService;
    private View btnConfirm;

    // Data Variables
    private long checkInTime = 0, checkOutTime = 0;
    private long roomPricePerNight = 0;
    private long servicePrice = 0;
    private long taxPrice = 0;
    private long finalTotalPrice = 0;

    // Giá dịch vụ thêm (Cố định để demo)
    private final long BREAKFAST_PRICE = 450000; // 450k/người (giả sử tính cho 2 người = 900k/ngày)
    private final long TRANSPORT_PRICE = 200000; // Xe đưa đón

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        initViews();
        loadUserInfo(); // Điền sẵn tên, email
        loadRoomData(); // Lấy dữ liệu phòng từ Intent
        setupDatePickers();
        setupSwitches(); // Xử lý bật tắt dịch vụ

        btnConfirm.setOnClickListener(v -> handleConfirmBooking());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etName = findViewById(R.id.etCusName);
        etEmail = findViewById(R.id.etCusEmail);
        etPhone = findViewById(R.id.etCusPhone);

        tvCheckIn = findViewById(R.id.tvCheckInDate);
        tvCheckOut = findViewById(R.id.tvCheckOutDate);
        tvTotalDays = findViewById(R.id.tvTotalDays);

        tvRoomName = findViewById(R.id.tvBookingRoomName);
        tvBookingPrice = findViewById(R.id.tvBookingPrice);
        imgRoom = findViewById(R.id.imgBookingRoom);

        switchBreakfast = findViewById(R.id.switchBreakfast);
        switchService = findViewById(R.id.switchService);

        lblRoomCharge = findViewById(R.id.lblRoomCharge);
        tvRoomCharge = findViewById(R.id.tvRoomCharge);
        tvServiceCharge = findViewById(R.id.tvServiceCharge);
        tvTax = findViewById(R.id.tvTax);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);

        btnConfirm = findViewById(R.id.btnConfirmBooking);
        btnBack = findViewById(R.id.btnBackBooking);
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // 1. Điền thông tin cơ bản từ Auth
            etName.setText(user.getDisplayName());
            etEmail.setText(user.getEmail());

            // 2. Lấy Số điện thoại từ Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Lấy chuỗi phone từ database
                            String savedPhone = documentSnapshot.getString("phone");
                            if (savedPhone != null && !savedPhone.isEmpty()) {
                                etPhone.setText(savedPhone); // Tự động điền
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Lỗi thì khách tự nhập
                    });
        }
    }

    private void loadRoomData() {
        if (getIntent() != null) {
            String name = getIntent().getStringExtra("name");
            String priceStr = getIntent().getStringExtra("price");
            String image = getIntent().getStringExtra("image");

            tvRoomName.setText(name);
            Glide.with(this).load(image).centerCrop().into(imgRoom);

            // 1. Parse giá phòng sang số (xóa hết chữ, chỉ lấy số)
            try {
                // Xóa mọi ký tự không phải số
                String cleanPrice = priceStr.replaceAll("[^0-9]", "");
                roomPricePerNight = Long.parseLong(cleanPrice);
            } catch (Exception e) {
                roomPricePerNight = 0;
            }

            // 2. Format lại thành tiền đẹp và gán vào TextView
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvBookingPrice.setText(formatter.format(roomPricePerNight) + "đ / đêm");
        }
    }

    private void setupDatePickers() {
        tvCheckIn.setOnClickListener(v -> showDatePicker(true));
        tvCheckOut.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth, 0, 0, 0); // Reset giờ về 0

                    String dateStr = dayOfMonth + " Th" + (month + 1) + ", " + year;

                    if (isCheckIn) {
                        checkInTime = selectedDate.getTimeInMillis();
                        tvCheckIn.setText(dateStr);
                    } else {
                        checkOutTime = selectedDate.getTimeInMillis();
                        tvCheckOut.setText(dateStr);
                    }
                    calculateTotal();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        // Không cho chọn ngày quá khứ
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void setupSwitches() {
        // Khi bật tắt switch thì tính lại tiền
        switchBreakfast.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        switchService.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
    }

    private void calculateTotal() {
        if (checkInTime == 0 || checkOutTime == 0 || checkOutTime <= checkInTime) {
            tvFinalTotal.setText("0 đ");
            return;
        }

        // 1. Tính số đêm
        long diff = checkOutTime - checkInTime;
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days == 0) days = 1;

        tvTotalDays.setText("Tổng cộng: " + days + " đêm");
        lblRoomCharge.setText("Tiền phòng (" + days + " đêm)");

        // 2. Tính tiền phòng
        long totalRoomPrice = days * roomPricePerNight;

        // 3. Tính tiền dịch vụ
        long totalService = 0;
        if (switchBreakfast.isChecked()) {
            // Giả sử tính cho 2 người mỗi ngày
            totalService += (BREAKFAST_PRICE * 2 * days);
        }
        if (switchService.isChecked()) {
            totalService += TRANSPORT_PRICE;
        }

        // 4. Tính thuế (10%)
        long subTotal = totalRoomPrice + totalService;
        long tax = (long) (subTotal * 0.1);

        // 5. Tổng cuối
        finalTotalPrice = subTotal + tax;

        // Hiển thị
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvRoomCharge.setText(formatter.format(totalRoomPrice) + " đ");
        tvServiceCharge.setText(formatter.format(totalService) + " đ");
        tvTax.setText(formatter.format(tax) + " đ");
        tvFinalTotal.setText(formatter.format(finalTotalPrice) + " đ");
    }

    private void handleConfirmBooking() {
        // 1. Kiểm tra dữ liệu đầu vào (Validate)
        if (finalTotalPrice == 0) {
            Toast.makeText(this, "Vui lòng chọn ngày nhận và trả phòng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String cusName = etName.getText().toString().trim();
        String cusPhone = etPhone.getText().toString().trim();
        String cusEmail = etEmail.getText().toString().trim();

        if (cusName.isEmpty() || cusPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền tên và số điện thoại liên hệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Chuẩn bị dữ liệu để lưu
        // Lấy User ID hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "GUEST";

        // Tạo mã đơn hàng duy nhất (Dùng thời gian hiện tại để không bao giờ trùng)
        String bookingId = "BOOK-" + System.currentTimeMillis();

        // Lấy thông tin phòng từ Intent (đã gửi từ trang trước)
        String roomId = getIntent().getStringExtra("id");
        String roomImg = getIntent().getStringExtra("image");
        String rName = tvRoomName.getText().toString();

        // Tạo đối tượng Booking từ class chúng ta vừa viết ở Bước 1
        Booking newBooking = new Booking(
                bookingId,
                userId,
                roomId,
                rName,
                roomImg,
                cusName,
                cusPhone,
                cusEmail,
                checkInTime,
                checkOutTime,
                finalTotalPrice,
                "PENDING" // Mặc định là Chờ duyệt
        );

        // 3. Gửi lên Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Khóa nút lại để tránh user bấm liên tục 2 lần
        btnConfirm.setEnabled(false);
        Toast.makeText(this, "Đang xử lý đặt phòng...", Toast.LENGTH_SHORT).show();

        // Lưu vào collection "bookings"
        db.collection("bookings").document(bookingId)
                .set(newBooking)
                .addOnSuccessListener(aVoid -> {
                    // --- THÀNH CÔNG ---
                    Toast.makeText(this, "🎉 Đặt phòng thành công!", Toast.LENGTH_LONG).show();

                    // Chuyển hướng về Trang chủ (Xóa lịch sử Back để không quay lại trang đặt được nữa)
                    Intent intent = new Intent(BookingActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // --- THẤT BẠI ---
                    btnConfirm.setEnabled(true); // Mở lại nút cho bấm lại
                    Toast.makeText(this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}