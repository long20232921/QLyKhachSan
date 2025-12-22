package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class AdminSendNotificationActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private RadioButton rbPromo;
    private Button btnSend;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_notification);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etNotiTitle);
        etMessage = findViewById(R.id.etNotiMessage);
        rbPromo = findViewById(R.id.rbPromo);
        btnSend = findViewById(R.id.btnSendAll);

        btnSend.setOnClickListener(v -> sendNotificationToAll());
    }

    private void sendNotificationToAll() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String type = rbPromo.isChecked() ? "PROMO" : "SYSTEM"; // PROMO hoặc SYSTEM

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false); // Khóa nút để tránh bấm nhiều lần
        Toast.makeText(this, "Đang gửi...", Toast.LENGTH_SHORT).show();

        // 1. Lấy danh sách tất cả Users
        db.collection("users").get()
                .addOnSuccessListener(snapshots -> {
                    // Dùng WriteBatch để gửi hàng loạt cho nhanh và an toàn
                    WriteBatch batch = db.batch();
                    int count = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId(); // Lấy ID của user

                        // Tạo thông báo mới
                        // Lưu ý: targetId để rỗng hoặc link đến trang chủ nếu là Promo chung
                        Notification noti = new Notification(userId, title, message, type, "");

                        // Thêm vào batch
                        batch.set(db.collection("notifications").document(), noti);
                        count++;
                    }

                    // 2. Thực thi Batch (Đẩy lên Firebase 1 lần)
                    int finalCount = count;
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã gửi thành công cho " + finalCount + " khách hàng!", Toast.LENGTH_LONG).show();
                        finish(); // Đóng màn hình
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi gửi batch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSend.setEnabled(true);
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lấy danh sách user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                });
    }
}