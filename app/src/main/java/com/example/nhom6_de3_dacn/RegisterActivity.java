package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private ImageView btnBack, btnTogglePass, btnToggleConfirmPass;
    private AppCompatButton btnRegister;
    private TextView tvGoToLogin;

    // Biến trạng thái ẩn/hiện cho 2 ô mật khẩu
    private boolean isPassVisible = false;
    private boolean isConfirmPassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Xử lý Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPass);
        btnTogglePass = findViewById(R.id.btnToggleRegPass);
        btnToggleConfirmPass = findViewById(R.id.btnToggleRegConfirmPass);
        btnRegister = findViewById(R.id.btnRegisterAction);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupEvents() {
        // 1. Nút Back (Góc trái trên) -> Đóng màn hình này
        btnBack.setOnClickListener(v -> finish());

        // 2. Chữ "Đăng nhập" (Cuối trang) -> Đóng màn hình này
        tvGoToLogin.setOnClickListener(v -> finish());

        // 3. Ẩn/Hiện Mật khẩu chính
        btnTogglePass.setOnClickListener(v -> {
            if (isPassVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePass.setImageResource(R.drawable.ic_eye_off);
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePass.setImageResource(R.drawable.ic_eye_on); // Nhớ tạo icon này nếu chưa có
            }
            isPassVisible =!isPassVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        // 4. Ẩn/Hiện Xác nhận Mật khẩu
        btnToggleConfirmPass.setOnClickListener(v -> {
            if (isConfirmPassVisible) {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnToggleConfirmPass.setImageResource(R.drawable.ic_eye_off);
            } else {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnToggleConfirmPass.setImageResource(R.drawable.ic_eye_on);
            }
            isConfirmPassVisible =!isConfirmPassVisible;
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        // 5. Nút Đăng ký (Logic Firebase sẽ thêm sau)
        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Đang xử lý đăng ký...", Toast.LENGTH_SHORT).show();
        });
    }
}