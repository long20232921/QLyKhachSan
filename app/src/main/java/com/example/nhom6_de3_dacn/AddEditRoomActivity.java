package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditRoomActivity extends AppCompatActivity {

    private EditText etName, etPrice, etImage, etRating, etDesc;
    private Button btnSave, btnDelete;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private String roomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        db = FirebaseFirestore.getInstance();

        initViews();
        checkIntentData();

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

    private void checkIntentData() {
        if (getIntent().hasExtra("roomId")) {
            // Chế độ Sửa
            roomId = getIntent().getStringExtra("roomId");
            etName.setText(getIntent().getStringExtra("name"));
            etPrice.setText(getIntent().getStringExtra("price"));
            etImage.setText(getIntent().getStringExtra("image"));
            etDesc.setText(getIntent().getStringExtra("description"));
            etRating.setText(String.valueOf(getIntent().getDoubleExtra("rating", 0)));

            tvTitle.setText("Cập nhật phòng");
            btnSave.setText("Cập nhật");
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // Chế độ Thêm
            tvTitle.setText("Thêm phòng mới");
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void saveRoom() {
        String name = etName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Tên và Giá không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("name", name);
        roomMap.put("price", price);
        roomMap.put("image", image);
        roomMap.put("description", desc);
        // Mặc định maxGuests là 2 nếu không nhập (hoặc Bro thêm ô nhập vào layout nếu muốn)
        roomMap.put("maxGuests", 2);

        try {
            roomMap.put("rating", Double.parseDouble(ratingStr));
        } catch (NumberFormatException e) {
            roomMap.put("rating", 0.0);
        }

        if (roomId == null) {
            // Thêm mới
            db.collection("rooms").add(roomMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Cập nhật
            db.collection("rooms").document(roomId).update(roomMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteRoom() {
        if (roomId != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa phòng")
                    .setMessage("Bạn chắc chắn muốn xóa phòng này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.collection("rooms").document(roomId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }
}