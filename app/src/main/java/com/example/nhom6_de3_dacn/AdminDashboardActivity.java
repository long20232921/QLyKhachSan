package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private View btnManageReviews; // Dùng View vì là CardView trong XML
    private View btnSendPromo;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupEvents();
    }

    private void initViews() {
        btnManageReviews = findViewById(R.id.btnManageReviews);
        btnSendPromo = findViewById(R.id.btnSendPromo);
        btnLogout = findViewById(R.id.btnLogoutAdmin);
    }

    private void setupEvents() {
        // Chuyển sang trang Quản lý Đánh giá
        btnManageReviews.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminReviewActivity.class);
            startActivity(intent);
        });

        // Chuyển sang trang Gửi Thông báo
        btnSendPromo.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminSendNotificationActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

            // Chuyển về màn hình đăng nhập và xóa lịch sử activity
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}