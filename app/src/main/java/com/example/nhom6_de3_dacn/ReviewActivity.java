package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RatingBar ratingBar;
    private TextView tvRatingLabel;
    private TextInputEditText etComment;
    private MaterialButton btnSubmit;

    private String roomId, bookingId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        db = FirebaseFirestore.getInstance();
        roomId = getIntent().getStringExtra("roomId");
        bookingId = getIntent().getStringExtra("bookingId");

        initViews();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackReview);
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingLabel = findViewById(R.id.tvRatingLabel);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmitReview);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (rating >= 5) tvRatingLabel.setText("Tuyệt vời");
            else if (rating >= 4) tvRatingLabel.setText("Hài lòng");
            else if (rating >= 3) tvRatingLabel.setText("Bình thường");
            else if (rating >= 2) tvRatingLabel.setText("Tệ");
            else tvRatingLabel.setText("Rất tệ");
        });

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getUid();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng viết vài dòng cảm nhận!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> review = new HashMap<>();
        review.put("bookingId", bookingId);
        review.put("roomId", roomId);
        review.put("userId", userId);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", System.currentTimeMillis());

        // Lưu vào Firebase
        db.collection("reviews").add(review)
                .addOnSuccessListener(docRef -> {
                    // Cập nhật trạng thái đơn hàng là ĐÃ ĐÁNH GIÁ
                    db.collection("bookings").document(bookingId).update("isReviewed", true);

                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}