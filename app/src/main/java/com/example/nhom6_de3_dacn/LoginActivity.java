package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView btnTogglePassword;
    private AppCompatButton btnLogin;
    private TextView tvRegister;
    private SignInButton btnGoogleLogin;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Thêm biến Firestore để check quyền
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 1001;

    // --- 1. TỰ ĐỘNG ĐĂNG NHẬP & PHÂN QUYỀN ---
    @Override
    protected void onStart() {
        super.onStart();
        // Nếu đã đăng nhập trước đó -> Check quyền để vào trang tương ứng
        if (mAuth.getCurrentUser() != null) {
            checkUserRole();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Khởi tạo Firestore

        initViews();
        setupGoogle();
        setupEvents();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void setupGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Đảm bảo string này đúng trong strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupEvents() {
        // Ẩn/Hiện mật khẩu
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off); // Đảm bảo có icon này
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_on); // Đảm bảo có icon này
            }
            isPasswordVisible = !isPasswordVisible;
            if (etPassword.getText().length() > 0) {
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // Nút Đăng nhập
        btnLogin.setOnClickListener(v -> loginEmail());

        // Nút Đăng ký
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // Nút Google
        btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_GOOGLE_SIGN_IN);
        });
    }

    private void loginEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    // Thay vì chuyển trang ngay, ta kiểm tra quyền Admin/User
                    checkUserRole();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- 2. HÀM KIỂM TRA ROLE (QUAN TRỌNG) ---
    private void checkUserRole() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Tìm trong collection "users" xem uid này là ai
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");

                    Intent intent;
                    if ("admin".equals(role)) {
                        // Nếu là Admin -> Vào trang quản lý
                        Toast.makeText(this, "Xin chào Quản lý!", Toast.LENGTH_SHORT).show();
                        intent = new Intent(this, AdminMainActivity.class);
                    } else {
                        // Nếu là Customer (hoặc null/không có role) -> Vào trang chủ khách
                        intent = new Intent(this, MainActivity.class);
                    }
                    startActivity(intent);
                    finish(); // Đóng Login lại
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi mạng hoặc không đọc được -> Mặc định cho vào trang khách để không bị kẹt
                    Toast.makeText(this, "Không thể xác thực quyền, vào chế độ Khách.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    // --- XỬ LÝ GOOGLE SIGN IN ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign In thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    // Đăng nhập Google thành công cũng cần check role
                    checkUserRole();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi Firebase Google: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}