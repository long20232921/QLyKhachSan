package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditRoomActivity extends AppCompatActivity {

    private EditText etName, etPrice, etImage, etRating, etDesc;
    private Button btnSave, btnDelete;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private String roomId = null; // Biến này quan trọng: null = Thêm, có ID = Sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        db = FirebaseFirestore.getInstance();

        initViews();
        checkIntentData(); // Kiểm tra xem là Thêm hay Sửa

        btnSave.setOnClickListener(v -> saveRoom());
        btnDelete.setOnClickListener(v -> deleteRoom());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etImage = findViewById(R.id.etImage);
        etRating = findViewById(R.id.etRating);
        etDesc = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
    }

    // Kiểm tra xem AdminMainActivity có gửi dữ liệu sang không
    private void checkIntentData() {
        if (getIntent().hasExtra("roomId")) {
            // --- CHẾ ĐỘ SỬA ---
            roomId = getIntent().getStringExtra("roomId");

            // Điền dữ liệu cũ vào ô
            etName.setText(getIntent().getStringExtra("name"));
            etPrice.setText(getIntent().getStringExtra("price"));
            etImage.setText(getIntent().getStringExtra("image"));
            etDesc.setText(getIntent().getStringExtra("description")); // Lấy mô tả cũ

            // Rating là số, phải chuyển về chuỗi
            double rating = getIntent().getDoubleExtra("rating", 0);
            etRating.setText(String.valueOf(rating));

            // Đổi giao diện
            tvTitle.setText("Cập nhật phòng");
            btnSave.setText("Cập nhật");
            btnDelete.setVisibility(View.VISIBLE); // Hiện nút Xóa
        } else {
            // --- CHẾ ĐỘ THÊM MỚI ---
            tvTitle.setText("Thêm phòng mới");
            btnDelete.setVisibility(View.GONE); // Ẩn nút Xóa
        }
    }

    private void saveRoom() {
        String name = etName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Vui lòng nhập tên và giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Map dữ liệu để đẩy lên Firebase
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("name", name);
        roomMap.put("price", price);
        roomMap.put("image", image);
        roomMap.put("description", desc);

        try {
            roomMap.put("rating", Double.parseDouble(ratingStr));
        } catch (NumberFormatException e) {
            roomMap.put("rating", 0.0);
        }

        if (roomId == null) {
            // --- THÊM MỚI (Add) ---
            db.collection("rooms").add(roomMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình, quay về danh sách
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // --- CẬP NHẬT (Update) ---
            db.collection("rooms").document(roomId).update(roomMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteRoom() {
        if (roomId != null) {
            // Xác nhận xóa
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Gọi lệnh xóa trên Firebase
                        db.collection("rooms").document(roomId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đã xóa phòng!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }
}