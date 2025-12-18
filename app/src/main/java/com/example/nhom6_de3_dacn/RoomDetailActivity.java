package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;

public class RoomDetailActivity extends AppCompatActivity {

    private ImageView imgRoom, btnBack;
    private TextView tvName, tvPrice, tvDesc, tvRating;
    private MaterialButton btnBookNow;

    // Biến lưu dữ liệu phòng để dùng khi đặt
    private String roomId, roomName, roomPriceStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        initViews();
        loadDataFromIntent();
        setupEvents();
    }

    private void initViews() {
        imgRoom = findViewById(R.id.imgRoomDetail);
        btnBack = findViewById(R.id.btnBackDetail);
        tvName = findViewById(R.id.tvDetailName);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvRating = findViewById(R.id.tvDetailRating);
        btnBookNow = findViewById(R.id.btnBookNow);
    }

    private void loadDataFromIntent() {
        if (getIntent() != null) {
            roomId = getIntent().getStringExtra("id");
            roomName = getIntent().getStringExtra("name");
            roomPriceStr = getIntent().getStringExtra("price");
            String image = getIntent().getStringExtra("image");
            String desc = getIntent().getStringExtra("description");

            // 1. Nhận Rating (Mặc định là 0 nếu không có)
            double rating = getIntent().getDoubleExtra("rating", 0.0);

            // Set Text
            tvName.setText(roomName);
            tvDesc.setText(desc != null ? desc : "Đang cập nhật mô tả...");

            // 2. Xử lý hiển thị Rating thông minh
            if (rating > 0) {
                String label = "";
                if (rating >= 4.8) label = "Xuất sắc";
                else if (rating >= 4.5) label = "Tuyệt vời";
                else if (rating >= 4.0) label = "Rất tốt";
                else label = "Tốt";

                tvRating.setText("⭐ " + rating + " (" + label + ")");
            } else {
                tvRating.setText("⭐ Mới (Chưa có đánh giá)");
            }

            // Format giá tiền
            try {
                // Xử lý chuỗi giá để đảm bảo không lỗi
                String cleanPrice = roomPriceStr.replaceAll("[^0-9]", "");
                if (!cleanPrice.isEmpty()) {
                    long priceVal = Long.parseLong(cleanPrice);
                    java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                    tvPrice.setText(formatter.format(priceVal) + " đ");
                } else {
                    tvPrice.setText(roomPriceStr);
                }
            } catch (Exception e) {
                tvPrice.setText(roomPriceStr);
            }

            // Load ảnh
            Glide.with(this)
                    .load(image)
                    .centerCrop()
                    .placeholder(R.drawable.bg_hotel) // Đổi thành drawable có sẵn nếu lỗi
                    .into(imgRoom);
        }
    }

    private void setupEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Đặt phòng
        btnBookNow.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển sang màn hình thanh toán...", Toast.LENGTH_SHORT).show();

            // Sau này Bro sẽ tạo BookingActivity và gọi:
            // Intent intent = new Intent(this, BookingActivity.class);
            // intent.putExtra("roomId", roomId);
            // ...
            // startActivity(intent);
        });
    }
}