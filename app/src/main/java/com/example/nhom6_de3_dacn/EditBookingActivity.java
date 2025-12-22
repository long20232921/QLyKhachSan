package com.example.nhom6_de3_dacn;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditBookingActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone;
    private TextView tvCheckIn, tvCheckOut, tvNewTotal;
    private LinearLayout btnCheckIn, btnCheckOut;
    private CheckBox cbBuffet, cbLaundry, cbSpa, cbAirport;
    private Button btnSave;

    private FirebaseFirestore db;
    private String bookingId, roomId;
    private Booking currentBooking;
    private long baseRoomPricePerNight = 0; // Giá gốc của phòng (lấy từ DB Rooms)

    // Biến tạm để tính toán
    private long newCheckInDate, newCheckOutDate;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booking);

        db = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("bookingId");

        initViews();
        loadBookingData();
        setupEvents();
    }

    private void initViews() {
        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        tvCheckIn = findViewById(R.id.tvEditCheckIn);
        tvCheckOut = findViewById(R.id.tvEditCheckOut);
        tvNewTotal = findViewById(R.id.tvNewTotal);

        btnCheckIn = findViewById(R.id.btnPickCheckIn);
        btnCheckOut = findViewById(R.id.btnPickCheckOut);

        cbBuffet = findViewById(R.id.cbBuffet);
        cbLaundry = findViewById(R.id.cbLaundry);
        cbSpa = findViewById(R.id.cbSpa);
        cbAirport = findViewById(R.id.cbAirport);

        btnSave = findViewById(R.id.btnSaveChanges);
    }

    private void setupEvents() {
        btnCheckIn.setOnClickListener(v -> showDatePicker(true));
        btnCheckOut.setOnClickListener(v -> showDatePicker(false));

        // Khi tích vào dịch vụ -> Tính lại tiền ngay
        cbBuffet.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbLaundry.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbSpa.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbAirport.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void loadBookingData() {
        if (bookingId == null) return;

        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentBooking = document.toObject(Booking.class);
                        if (currentBooking != null) {
                            // Fill data cũ
                            etName.setText(currentBooking.getCustomerName());
                            etPhone.setText(currentBooking.getCustomerPhone());

                            newCheckInDate = currentBooking.getCheckInDate();
                            newCheckOutDate = currentBooking.getCheckOutDate();
                            roomId = currentBooking.getRoomId();

                            updateDateUI();

                            // Check các dịch vụ đã chọn
                            List<String> services = currentBooking.getServices();
                            if (services != null) {
                                if (services.contains("Buffet Sáng")) cbBuffet.setChecked(true);
                                if (services.contains("Giặt ủi")) cbLaundry.setChecked(true);
                                if (services.contains("Spa Thư giãn")) cbSpa.setChecked(true);
                                if (services.contains("Đưa đón sân bay")) cbAirport.setChecked(true);
                            }

                            // QUAN TRỌNG: Phải lấy giá gốc của phòng để tính lại tiền
                            fetchRoomBasePrice(roomId);
                        }
                    }
                });
    }

    private void fetchRoomBasePrice(String roomId) {
        db.collection("rooms").document(roomId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String priceStr = doc.getString("price"); // VD: "2.500.000"
                        // Chuyển chuỗi giá về số Long
                        try {
                            baseRoomPricePerNight = Long.parseLong(priceStr.replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                            baseRoomPricePerNight = 1000000; // Giá mặc định nếu lỗi
                        }
                        calculateTotal(); // Có giá phòng rồi mới tính tổng
                    }
                });
    }

    private void showDatePicker(boolean isCheckIn) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if (isCheckIn) {
                newCheckInDate = calendar.getTimeInMillis();
            } else {
                newCheckOutDate = calendar.getTimeInMillis();
            }
            updateDateUI();
            calculateTotal();
        };

        // Set ngày hiện tại vào picker để dễ chọn
        calendar.setTimeInMillis(isCheckIn ? newCheckInDate : newCheckOutDate);
        new DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvCheckIn.setText(sdf.format(new Date(newCheckInDate)));
        tvCheckOut.setText(sdf.format(new Date(newCheckOutDate)));
    }

    private void calculateTotal() {
        if (baseRoomPricePerNight == 0) return;

        // 1. Tính số đêm
        long diff = newCheckOutDate - newCheckInDate;
        long days = diff / (1000 * 60 * 60 * 24);
        if (days <= 0) days = 1; // Tối thiểu 1 đêm

        long roomTotal = days * baseRoomPricePerNight;
        long serviceTotal = 0;

        // 2. Tính dịch vụ (Giá cứng Demo)
        if (cbBuffet.isChecked()) serviceTotal += 500000 * days; // Buffet tính theo ngày
        if (cbLaundry.isChecked()) serviceTotal += 150000;
        if (cbSpa.isChecked()) serviceTotal += 300000;
        if (cbAirport.isChecked()) serviceTotal += 200000;

        long finalTotal = roomTotal + serviceTotal;

        DecimalFormat formatter = new DecimalFormat("#,###");
        tvNewTotal.setText(formatter.format(finalTotal) + " đ");
    }

    private void validateAndSave() {
        if (newCheckInDate >= newCheckOutDate) {
            Toast.makeText(this, "Ngày trả phòng phải sau ngày nhận phòng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem phòng có trống trong ngày mới chọn không
        checkAvailabilityAndSave();
    }

    private void checkAvailabilityAndSave() {
        Toast.makeText(this, "Đang kiểm tra phòng trống...", Toast.LENGTH_SHORT).show();

        db.collection("bookings")
                .whereEqualTo("roomId", roomId)
                .whereIn("status", java.util.Arrays.asList("PENDING", "CONFIRMED")) // Chỉ check đơn chưa hủy
                .get()
                .addOnCompleteListener(task -> {
                    boolean isAvailable = true;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Bỏ qua chính đơn hàng hiện tại (đang sửa)
                            if (doc.getId().equals(bookingId)) continue;

                            long start = doc.getLong("checkInDate");
                            long end = doc.getLong("checkOutDate");

                            // Công thức check trùng lịch: (StartA < EndB) && (EndA > StartB)
                            if (newCheckInDate < end && newCheckOutDate > start) {
                                isAvailable = false;
                                break;
                            }
                        }

                        if (isAvailable) {
                            saveDataToFirestore();
                        } else {
                            Toast.makeText(this, "Rất tiếc, phòng đã kín lịch trong ngày bạn chọn!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveDataToFirestore() {
        // Gom danh sách dịch vụ mới
        List<String> newServices = new ArrayList<>();
        if (cbBuffet.isChecked()) newServices.add("Buffet Sáng");
        if (cbLaundry.isChecked()) newServices.add("Giặt ủi");
        if (cbSpa.isChecked()) newServices.add("Spa Thư giãn");
        if (cbAirport.isChecked()) newServices.add("Đưa đón sân bay");

        // Tính lại giá lần cuối cho chắc
        String totalString = tvNewTotal.getText().toString().replace(" đ", "").replace(".", "").replace(",", "");
        long finalPrice = Long.parseLong(totalString.trim());

        db.collection("bookings").document(bookingId)
                .update(
                        "customerName", etName.getText().toString(),
                        "customerPhone", etPhone.getText().toString(),
                        "checkInDate", newCheckInDate,
                        "checkOutDate", newCheckOutDate,
                        "services", newServices,
                        "totalPrice", finalPrice
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại trang chi tiết
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}