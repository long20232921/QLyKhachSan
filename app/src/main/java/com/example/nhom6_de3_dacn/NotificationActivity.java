package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rcNotification;
    private NotificationAdapter adapter;
    private List<Notification> mList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        if (mAuth.getCurrentUser() != null) {
            setupRecyclerView();
            loadNotifications();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        rcNotification = findViewById(R.id.rcNotification);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(mList);
        rcNotification.setLayoutManager(new LinearLayoutManager(this));
        rcNotification.setAdapter(adapter);
    }

    private void loadNotifications() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        mList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Notification noti = doc.toObject(Notification.class);
                                noti.setId(doc.getId());
                                mList.add(noti);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // --- ADAPTER ---
    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Notification> list;

        public NotificationAdapter(List<Notification> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification item = list.get(position);

            holder.tvTitle.setText(item.getTitle());
            holder.tvMessage.setText(item.getMessage());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            try {
                holder.tvTime.setText(sdf.format(new Date(item.getTimestamp())));
            } catch (Exception e) {
                holder.tvTime.setText("");
            }

            if (item.isRead()) {
                holder.tvTitle.setTypeface(null, Typeface.NORMAL);
                holder.itemView.setBackgroundColor(Color.WHITE);
            } else {
                holder.tvTitle.setTypeface(null, Typeface.BOLD);
                holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
            }

            holder.itemView.setOnClickListener(v -> {
                // 1. Cập nhật trạng thái đã đọc lên Firebase
                if (!item.isRead()) {
                    db.collection("notifications").document(item.getId()).update("isRead", true);
                    item.setRead(true);
                    notifyItemChanged(position);
                }

                // 2. Chuyển hướng
                if (item.getTargetId() != null && !item.getTargetId().isEmpty()) {
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvTime;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvNotiTitle);
                tvMessage = itemView.findViewById(R.id.tvNotiMessage);
                tvTime = itemView.findViewById(R.id.tvNotiTime);
            }
        }
    }
}