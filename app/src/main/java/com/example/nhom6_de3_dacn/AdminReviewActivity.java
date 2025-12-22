package com.example.nhom6_de3_dacn;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView btnBack;
    private AdminReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        db = FirebaseFirestore.getInstance();
        initViews();
        loadReviews();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackAdminReview);
        recyclerView = findViewById(R.id.rvAdminReviews);

        btnBack.setOnClickListener(v -> finish());

        adapter = new AdminReviewAdapter(this, reviewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadReviews() {
        db.collection("reviews")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Review review = doc.toObject(Review.class);
                            review.setId(doc.getId()); // Lưu ID document
                            reviewList.add(review);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showReplyDialog(Review review) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trả lời khách hàng: " + review.getUserName());

        final EditText input = new EditText(this);
        input.setHint("Nhập nội dung phản hồi...");
        input.setPadding(40, 40, 40, 40);
        input.setBackgroundResource(android.R.color.transparent);
        input.setMinLines(3);
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String replyText = input.getText().toString().trim();
            if (!replyText.isEmpty()) {
                submitReply(review, replyText);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void submitReply(Review review, String replyText) {
        // 1. Cập nhật review
        db.collection("reviews").document(review.getId())
                .update("managerReply", replyText, "isReplied", true)
                .addOnSuccessListener(aVoid -> {
                    // 2. Tạo thông báo gửi khách hàng
                    Notification noti = new Notification(
                            review.getUserId(),
                            "💬 Phản hồi đánh giá",
                            "Quản lý đã trả lời đánh giá của bạn về phòng " + (review.getRoomId() != null ? review.getRoomId() : "đã đặt"),
                            "REPLY",
                            review.getBookingId()
                    );

                    db.collection("notifications").add(noti);

                    Toast.makeText(this, "Đã phản hồi thành công!", Toast.LENGTH_SHORT).show();
                    loadReviews(); // Refresh list
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- ADAPTER ---
    class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {
        private Context context;
        private List<Review> list;

        public AdminReviewAdapter(Context context, List<Review> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_admin_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Review item = list.get(position);

            holder.tvName.setText(item.getUserName());
            holder.tvContent.setText(item.getComment());
            holder.ratingBar.setRating(item.getRating());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(item.getTimestamp())));

            if (item.isReplied()) {
                holder.layoutReply.setVisibility(View.VISIBLE);
                holder.tvReplyContent.setText(item.getManagerReply());
                holder.btnReply.setVisibility(View.GONE);
            } else {
                holder.layoutReply.setVisibility(View.GONE);
                holder.btnReply.setVisibility(View.VISIBLE);
                holder.btnReply.setOnClickListener(v -> showReplyDialog(item));
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContent, tvDate, tvReplyContent;
            RatingBar ratingBar;
            LinearLayout layoutReply;
            MaterialButton btnReply;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvUserReviewName);
                tvContent = itemView.findViewById(R.id.tvReviewContent);
                tvDate = itemView.findViewById(R.id.tvReviewDate);
                ratingBar = itemView.findViewById(R.id.rbReviewRating);
                layoutReply = itemView.findViewById(R.id.layoutAdminReply);
                tvReplyContent = itemView.findViewById(R.id.tvReplyContent);
                btnReply = itemView.findViewById(R.id.btnReply);
            }
        }
    }
}