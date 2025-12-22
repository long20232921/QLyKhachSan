package com.example.nhom6_de3_dacn;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditRoomActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etName, etPrice, etImage, etRating, etDescription;
    private Spinner spinnerStatus; // Biến cho Spinner
    private Button btnSave, btnDelete;

    private FirebaseFirestore db;
    private String roomId = null; // Nếu null => Thêm mới, Có ID => Sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupSpinner(); // Cài đặt dữ liệu cho Spinner
        checkIntentData(); // Kiểm tra xem là Thêm hay Sửa
        setupEvents();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etImage = findViewById(R.id.etImage);
        etRating = findViewById(R.id.etRating);
        etDescription = findViewById(R.id.etDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus); // Ánh xạ Spinner
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupSpinner() {
        // Tạo Adapter lấy dữ liệu từ string-array trong strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.room_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void checkIntentData() {
        if (getIntent().hasExtra("roomId")) {
            // CHẾ ĐỘ SỬA
            roomId = getIntent().getStringExtra("roomId");
            tvTitle.setText("Chỉnh sửa phòng");
            btnSave.setText("Cập nhật");
            btnDelete.setVisibility(View.VISIBLE);

            // Fill dữ liệu cũ vào ô
            etName.setText(getIntent().getStringExtra("name"));
            etPrice.setText(getIntent().getStringExtra("price"));
            etImage.setText(getIntent().getStringExtra("image"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etRating.setText(String.valueOf(getIntent().getDoubleExtra("rating", 0)));

            String currentStatus = getIntent().getStringExtra("status");
            if (currentStatus != null) {
                switch (currentStatus) {
                    case "BOOKED":
                        spinnerStatus.setSelection(1); // Đã Đặt
                        break;
                    case "OCCUPIED":
                        spinnerStatus.setSelection(2); // Đang Sử Dụng
                        break;
                    case "CLEANING":
                        spinnerStatus.setSelection(3);
                        break;
                    default:
                        spinnerStatus.setSelection(0); // Phòng Trống (AVAILABLE)
                        break;
                }
            }
        } else {
            // CHẾ ĐỘ THÊM MỚI
            tvTitle.setText("Thêm phòng mới");
            btnSave.setText("Lưu thông tin");
            btnDelete.setVisibility(View.GONE);
            spinnerStatus.setSelection(0); // Mặc định là Trống
        }
    }

    private void setupEvents() {
        // Nút Lưu
        btnSave.setOnClickListener(v -> saveRoom());

        // Nút Xóa
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteRoom())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void saveRoom() {
        String name = etName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        double rating = 0;
        try { rating = Double.parseDouble(ratingStr); } catch (Exception e) {}

        int selectedPosition = spinnerStatus.getSelectedItemPosition();
        String status = "AVAILABLE";
        if (selectedPosition == 1) status = "BOOKED";
        else if (selectedPosition == 2) status = "OCCUPIED";
        else if (selectedPosition == 3) status = "CLEANING";

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", name);
        roomData.put("price", price);
        roomData.put("image", image);
        roomData.put("description", desc);
        roomData.put("rating", rating);
        roomData.put("status", status);

        if (roomId == null) {
            // Thêm mới
            db.collection("rooms").add(roomData)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Cập nhật
            db.collection("rooms").document(roomId).update(roomData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteRoom() {
        if (roomId != null) {
            db.collection("rooms").document(roomId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã xóa phòng!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}