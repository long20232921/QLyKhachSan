package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private AppCompatButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ImageView btnTogglePassword;
    private boolean isPasswordVisible = false; // Trạng thái mật khẩu

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Đảm bảo tên layout đúng

        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupEvents();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister); // ID của chữ "Đăng ký"
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
    }

    private void setupEvents() {
        // 1. CHUYỂN SANG TRANG ĐĂNG KÝ
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển từ LoginActivity -> RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 2. XỬ LÝ ẨN/HIỆN MẬT KHẨU
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Đang hiện -> Ẩn đi
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // Đang ẩn -> Hiện lên
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btnTogglePassword.setImageResource(R.drawable.ic_eye_on);
                }
                isPasswordVisible =!isPasswordVisible; // Đảo ngược trạng thái

                // Đưa con trỏ về cuối dòng
                if(etPassword.getText().length() > 0) {
                    etPassword.setSelection(etPassword.getText().length());
                }
            }
        });

        // 3. XỬ LÝ ĐĂNG NHẬP
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi Firebase đăng nhập
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển vào màn hình chính
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}