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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // Khai báo biến (Sử dụng đúng tên ID của Bro)
    private EditText etEmail, etPassword;
    private ImageView btnTogglePassword;
    private AppCompatButton btnLogin;
    private TextView tvRegister;
    private SignInButton btnGoogleLogin;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 1001;

    // --- 1. TỰ ĐỘNG ĐĂNG NHẬP ---
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupGoogle();
        setupEvents();
    }

    private void initViews() {
        // Ánh xạ đúng ID từ file XML của Bro
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void setupGoogle() {
        // Đảm bảo default_web_client_id đã có trong strings.xml (Lấy từ google-services.json)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupEvents() {
        // Ẩn/Hiện mật khẩu
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off); // Đảm bảo Bro đã có icon này
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_on); // Đảm bảo Bro đã có icon này
            }
            isPasswordVisible = !isPasswordVisible;
            // Đưa con trỏ về cuối dòng
            if (etPassword.getText().length() > 0) {
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // Nút Đăng nhập Email
        btnLogin.setOnClickListener(v -> loginEmail());

        // Chuyển trang Đăng ký
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // Đăng nhập Google
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
                    checkUserRoleAndRedirect();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- 2. HÀM CHECK QUYỀN THÔNG MINH (LOGIC QUAN TRỌNG) ---
    private void checkUserRoleAndRedirect() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Cửa hậu (Backdoor): Luôn cho phép email này làm Admin để test nhanh
        if ("admin@hotel.com".equals(user.getEmail())) {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        // Check Firestore cho các user khác
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User đã tồn tại -> Check Role
                        String role = documentSnapshot.getString("role");
                        if ("admin".equals(role)) {
                            startActivity(new Intent(this, AdminMainActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                    } else {
                        // User chưa tồn tại (Trường hợp Google Login lần đầu) -> Tạo mới là Customer
                        createUserProfile(user);
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Lỗi mạng -> Vào chế độ khách cho an toàn
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    // --- 3. TẠO PROFILE CHO GOOGLE LOGIN ---
    private void createUserProfile(FirebaseUser user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("role", "customer"); // Mặc định là khách
        userMap.put("name", user.getDisplayName());

        // Nếu có ảnh Google thì lưu luôn để Profile hiển thị đẹp
        if (user.getPhotoUrl() != null) {
            userMap.put("avatarUrl", user.getPhotoUrl().toString());
        }

        db.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Lưu xong thì vào trang chủ
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    // --- XỬ LÝ KẾT QUẢ GOOGLE SIGN IN ---
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
                    // Đăng nhập thành công -> Check quyền & Tạo profile nếu cần
                    checkUserRoleAndRedirect();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi Firebase Google: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}