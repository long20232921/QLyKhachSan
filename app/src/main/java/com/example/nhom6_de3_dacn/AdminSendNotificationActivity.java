package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class AdminSendNotificationActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private RadioButton rbPromo;
    private Button btnSend;
    private ImageView btnBack;
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
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnSend.setOnClickListener(v -> sendNotificationToAll());
    }

    private void sendNotificationToAll() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String type = rbPromo.isChecked() ? "PROMO" : "SYSTEM";

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);
        btnSend.setText("Đang gửi...");

        long currentTimestamp = System.currentTimeMillis();

        db.collection("users").get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        Toast.makeText(this, "Không tìm thấy user nào!", Toast.LENGTH_SHORT).show();
                        btnSend.setEnabled(true);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    int count = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();

                        Notification noti = new Notification(
                                userId,
                                title,
                                message,
                                type,
                                "",
                                currentTimestamp
                        );

                        batch.set(db.collection("notifications").document(), noti);
                        count++;
                    }

                    int finalCount = count;
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã gửi cho " + finalCount + " khách!", Toast.LENGTH_LONG).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSend.setEnabled(true);
                        btnSend.setText("Gửi ngay");
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                });
    }
}