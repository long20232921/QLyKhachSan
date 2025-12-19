package com.example.nhom6_de3_dacn;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // Views
    private ImageView imgAvatar, btnBackProfile;
    private TextView tvName, tvMembership, tvTotalSpending, tvSaveTop;
    private TextInputEditText etEmail, etPhone, etAddress;
    private TextView btnLogout;
    private View btnSupport;
    private BottomNavigationView bottomNavigationView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String userId;

    // State Variables
    private String originalPhone = "";
    private String originalAddress = "";
    private boolean isDataChanged = false;

    // Image Picker
    private Uri selectedImageUri;
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgAvatar.setImageURI(uri);
                    uploadImageToFirebase();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }
        userId = mAuth.getCurrentUser().getUid();

        initViews();
        loadUserProfile();
        calculateMembership();
        setupEvents();
        setupBottomNav();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgProfileAvatar);
        btnBackProfile = findViewById(R.id.btnBackProfile);
        tvSaveTop = findViewById(R.id.tvSaveTop);

        tvName = findViewById(R.id.tvProfileName);
        tvMembership = findViewById(R.id.tvMembershipTier);
        tvTotalSpending = findViewById(R.id.tvTotalSpending);

        etEmail = findViewById(R.id.etProfileEmail);
        etPhone = findViewById(R.id.etProfilePhone);
        etAddress = findViewById(R.id.etProfileAddress);

        btnLogout = findViewById(R.id.btnLogout);
        btnSupport = findViewById(R.id.btnSupport);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupEvents() {
        // Back
        btnBackProfile.setOnClickListener(v -> handleBackPress());

        // Save
        tvSaveTop.setOnClickListener(v -> saveProfileData(null));

        // Change Avatar
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Watch changes
        TextWatcher changeListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkForChanges(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etPhone.addTextChangedListener(changeListener);
        etAddress.addTextChangedListener(changeListener);

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Support
        btnSupport.setOnClickListener(v -> Toast.makeText(this, "Đang kết nối nhân viên hỗ trợ...", Toast.LENGTH_SHORT).show());

        // 👇 MỞ BẢNG XẾP HẠNG THÀNH VIÊN 👇
        tvMembership.setOnClickListener(v -> showMembershipInfo());
    }

    // --- LOGIC MỚI: HIỂN THỊ BOTTOM SHEET MEMBERSHIP ---
    private void showMembershipInfo() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        // Nạp layout từ file layout_membership_sheet.xml
        View view = getLayoutInflater().inflate(R.layout.layout_membership_sheet, null);
        dialog.setContentView(view);

        // Set background trong suốt cho container để bo góc hoạt động đẹp
        try {
            ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } catch (Exception e) { e.printStackTrace(); }

        RecyclerView rvTiers = view.findViewById(R.id.rvMembershipTiers);
        MaterialButton btnClose = view.findViewById(R.id.btnCloseSheet);

        // Tạo dữ liệu
        List<MembershipTier> tiers = new ArrayList<>();
        tiers.add(new MembershipTier("🌱 Thành viên Mới", "0 đ", "• Tích điểm đổi quà", 0xFFF5F5F5));
        tiers.add(new MembershipTier("🥈 Thành viên Bạc", "> 5.000.000 đ", "• Giảm 3% giá phòng\n• Check-in sớm 1 giờ", 0xFFE3F2FD));
        tiers.add(new MembershipTier("🥇 Thành viên Vàng", "> 20.000.000 đ", "• Giảm 7% giá phòng\n• Miễn phí ăn sáng\n• Hủy phòng miễn phí", 0xFFFFF8E1));
        tiers.add(new MembershipTier("💎 Kim Cương", "> 50.000.000 đ", "• Giảm 12% giá phòng\n• Xe đưa đón sân bay\n• Nâng hạng phòng miễn phí", 0xFFE0F7FA));
        tiers.add(new MembershipTier("👑 V.I.P", "> 100.000.000 đ", "• Giảm 20% trọn đời\n• Quản gia riêng 24/7\n• Tất cả dịch vụ miễn phí", 0xFFECEFF1));

        // Setup Adapter
        MembershipAdapter adapter = new MembershipAdapter(tiers);
        rvTiers.setLayoutManager(new LinearLayoutManager(this));
        rvTiers.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- LOGIC KIỂM TRA THAY ĐỔI ---
    private void checkForChanges() {
        String currentPhone = etPhone.getText().toString().trim();
        String currentAddress = etAddress.getText().toString().trim();

        if (!currentPhone.equals(originalPhone) || !currentAddress.equals(originalAddress)) {
            isDataChanged = true;
            tvSaveTop.setVisibility(View.VISIBLE);
        } else {
            isDataChanged = false;
            tvSaveTop.setVisibility(View.GONE);
        }
    }

    private void checkChangesAndNavigate(Runnable navigationAction) {
        if (isDataChanged) {
            new AlertDialog.Builder(this)
                    .setTitle("Lưu thay đổi?")
                    .setMessage("Bạn có muốn lưu thông tin trước khi rời đi không?")
                    .setPositiveButton("Có", (dialog, which) -> saveProfileData(navigationAction))
                    .setNegativeButton("Không", (dialog, which) -> {
                        isDataChanged = false;
                        navigationAction.run();
                    })
                    .setNeutralButton("Hủy", null)
                    .show();
        } else {
            navigationAction.run();
        }
    }

    private void handleBackPress() {
        checkChangesAndNavigate(this::finish);
    }

    @Override public void onBackPressed() { handleBackPress(); }

    private void setupBottomNav() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;

            Runnable action = null;
            if (id == R.id.nav_home) action = () -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            };
            else if (id == R.id.nav_booking) action = () -> startActivity(new Intent(this, RoomListActivity.class));
            else if (id == R.id.nav_history) action = () -> startActivity(new Intent(this, BookingHistoryActivity.class));

            if (action != null) checkChangesAndNavigate(action);
            return true;
        });
    }

    // --- FIREBASE LOGIC ---
    private void loadUserProfile() {
        db.collection("users").document(userId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String email = document.getString("email");
                String phone = document.getString("phone");
                String address = document.getString("address");
                String avatarUrl = document.getString("avatarUrl");
                String name = mAuth.getCurrentUser().getDisplayName();

                originalPhone = phone != null ? phone : "";
                originalAddress = address != null ? address : "";

                etEmail.setText(email);
                etPhone.setText(originalPhone);
                etAddress.setText(originalAddress);
                tvName.setText(name != null && !name.isEmpty() ? name : "Khách hàng");

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this).load(avatarUrl).circleCrop().into(imgAvatar);
                }
                isDataChanged = false;
                tvSaveTop.setVisibility(View.GONE);
            }
        });
    }

    private void calculateMembership() {
        db.collection("bookings").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalSpent = 0;
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Double price = doc.getDouble("totalPrice");
                    if (price != null) totalSpent += price.longValue();
                }
                updateMembershipUI(totalSpent);
            }
        });
    }

    private void updateMembershipUI(long totalSpent) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvTotalSpending.setText("Chi tiêu: " + formatter.format(totalSpent) + " đ");

        String tierName = "Thành viên Mới";
        int colorCode = 0xFF9E9E9E;

        if (totalSpent >= 100_000_000) {
            tierName = "👑 Thành viên V.I.P";
            colorCode = 0xFF000000;
        } else if (totalSpent >= 50_000_000) {
            tierName = "💎 Kim Cương";
            colorCode = 0xFF00BCD4;
        } else if (totalSpent >= 20_000_000) {
            tierName = "🥇 Vàng";
            colorCode = 0xFFFFD700;
        } else if (totalSpent >= 5_000_000) {
            tierName = "🥈 Bạc";
            colorCode = 0xFFC0C0C0;
        }

        tvMembership.setText(tierName);
        tvMembership.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorCode));
    }

    private void saveProfileData(Runnable onComplete) {
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", phone);
        updates.put("address", address);

        Toast.makeText(this, "Đang lưu...", Toast.LENGTH_SHORT).show();

        db.collection("users").document(userId).update(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
            originalPhone = phone;
            originalAddress = address;
            isDataChanged = false;
            tvSaveTop.setVisibility(View.GONE);
            if (onComplete != null) onComplete.run();
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri == null) return;
        Toast.makeText(this, "Đang tải ảnh...", Toast.LENGTH_SHORT).show();
        StorageReference fileRef = storageRef.child("profile_images/" + userId + ".jpg");
        fileRef.putFile(selectedImageUri).addOnSuccessListener(task -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            db.collection("users").document(userId).update("avatarUrl", uri.toString())
                    .addOnSuccessListener(a -> Toast.makeText(this, "Đổi ảnh thành công!", Toast.LENGTH_SHORT).show());
        })).addOnFailureListener(e -> Toast.makeText(this, "Lỗi upload!", Toast.LENGTH_SHORT).show());
    }
}