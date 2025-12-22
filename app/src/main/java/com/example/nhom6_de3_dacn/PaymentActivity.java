package com.example.nhom6_de3_dacn;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvTotal, tvRoomName, tvRedirect;
    private RadioButton rbFull, rbDeposit;
    private LinearLayout btnCard, btnMomo, btnBank, layoutCardInput;
    private MaterialButton btnPay;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private String bookingId;
    private long totalPrice;
    private long amountToPay;

    private int selectedMethod = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();

        bookingId = getIntent().getStringExtra("bookingId");
        totalPrice = getIntent().getLongExtra("totalPrice", 0);
        String roomName = getIntent().getStringExtra("roomName");

        initViews();
        setupData(roomName);
        setupEvents();
    }

    private void initViews() {
        tvTotal = findViewById(R.id.tvTotalAmount);
        tvRoomName = findViewById(R.id.tvRoomNamePayment);
        tvRedirect = findViewById(R.id.tvRedirectMessage);

        rbFull = findViewById(R.id.rbPayFull);
        rbDeposit = findViewById(R.id.rbPayDeposit);

        btnCard = findViewById(R.id.btnMethodCard);
        btnMomo = findViewById(R.id.btnMethodMomo);
        btnBank = findViewById(R.id.btnMethodBank);
        layoutCardInput = findViewById(R.id.layoutCardInput);

        btnPay = findViewById(R.id.btnConfirmPayment);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupData(String roomName) {
        tvRoomName.setText(roomName);
        updatePayButton(totalPrice);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        // 1. Chọn loại thanh toán
        rbFull.setOnClickListener(v -> updatePayButton(totalPrice));
        rbDeposit.setOnClickListener(v -> updatePayButton(totalPrice / 2));

        // 2. Chọn phương thức
        btnCard.setOnClickListener(v -> switchMethod(1));
        btnMomo.setOnClickListener(v -> switchMethod(2));
        btnBank.setOnClickListener(v -> switchMethod(3));

        // 3. Xử lý nút Thanh toán
        btnPay.setOnClickListener(v -> processPayment());
    }

    private void updatePayButton(long amount) {
        amountToPay = amount;
        DecimalFormat formatter = new DecimalFormat("#,###");
        btnPay.setText("Thanh toán " + formatter.format(amount) + " đ");
    }

    private void switchMethod(int method) {
        selectedMethod = method;

        btnCard.setBackgroundColor(Color.parseColor(method == 1 ? "#E3F2FD" : "#FFFFFF"));
        btnMomo.setBackgroundColor(Color.parseColor(method == 2 ? "#E3F2FD" : "#FFFFFF"));
        btnBank.setBackgroundColor(Color.parseColor(method == 3 ? "#E3F2FD" : "#FFFFFF"));

        if (method == 1) {
            layoutCardInput.setVisibility(View.VISIBLE);
            tvRedirect.setVisibility(View.GONE);
        } else {
            layoutCardInput.setVisibility(View.GONE);
            tvRedirect.setVisibility(View.VISIBLE);
        }
    }

    private void processPayment() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Đang xử lý giao dịch an toàn...");
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();

            // Cập nhật trạng thái thanh toán lên Firebase
            updatePaymentStatus();

        }, 2000);
    }

    private void updatePaymentStatus() {
        if (bookingId == null) return;

        String statusNote = rbFull.isChecked() ? "PAID_FULL" : "PAID_DEPOSIT";

        db.collection("bookings").document(bookingId)
                .update("paymentStatus", statusNote,
                        "amountPaid", amountToPay)
                .addOnSuccessListener(aVoid -> showSuccessDialog())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thanh toán thành công! 🎉")
                .setMessage("Cảm ơn bạn đã thanh toán. Đơn đặt phòng của bạn đã được đảm bảo.")
                .setPositiveButton("Về trang chủ", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}