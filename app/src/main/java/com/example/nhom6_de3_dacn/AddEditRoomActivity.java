package com.example.nhom6_de3_dacn;

import android.os.Bundle;
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

    private EditText etName, etPrice, etImage;
    private Button btnSave, btnDelete;
    private FirebaseFirestore db;
    private String roomId = null; // Nếu null = Thêm mới, Có ID = Sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        db = FirebaseFirestore.getInstance();
        initViews();
        checkIntent(); // Kiểm tra xem là Thêm hay Sửa

        btnSave.setOnClickListener(v -> saveRoom());
        btnDelete.setOnClickListener(v -> deleteRoom());
    }

    private void initViews() {
        etName = findViewById(R.id.etRoomName);
        etPrice = findViewById(R.id.etRoomPrice);
        etImage = findViewById(R.id.etRoomImage);
        btnSave = findViewById(R.id.btnSaveRoom);
        btnDelete = findViewById(R.id.btnDeleteRoom);
    }

    private void checkIntent() {
        if (getIntent().hasExtra("roomId")) {
            // Chế độ Sửa
            roomId = getIntent().getStringExtra("roomId");
            etName.setText(getIntent().getStringExtra("name"));
            etPrice.setText(getIntent().getStringExtra("price"));
            etImage.setText(getIntent().getStringExtra("image"));

            ((TextView)findViewById(R.id.tvTitle)).setText("Chỉnh sửa phòng");
            btnDelete.setVisibility(View.VISIBLE);
        }
    }

    private void saveRoom() {
        String name = etName.getText().toString();
        String price = etPrice.getText().toString();
        String image = etImage.getText().toString();

        Map<String, Object> room = new HashMap<>();
        room.put("name", name);
        room.put("price", price);
        room.put("image", image);
        // room.put("name_lowercase", name.toLowerCase()); // Để tìm kiếm

        if (roomId == null) {
            // Thêm mới
            db.collection("rooms").add(room)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // Cập nhật
            db.collection("rooms").document(roomId).update(room)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void deleteRoom() {
        if (roomId != null) {
            db.collection("rooms").document(roomId).delete()
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Đã xóa phòng!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}