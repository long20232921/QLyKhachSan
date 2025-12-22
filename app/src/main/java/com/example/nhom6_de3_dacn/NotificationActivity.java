package com.example.nhom6_de3_dacn;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        initViews();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackNoti); // Nhớ tạo ID này trong layout
        recyclerView = findViewById(R.id.rvNotifications); // Nhớ tạo ID này trong layout

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Notification noti = doc.toObject(Notification.class);
                            noti.setId(doc.getId());
                            notificationList.add(noti);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show());
    }

    // --- ADAPTER ---
    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private Context context;
        private List<Notification> list;

        public NotificationAdapter(Context context, List<Notification> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Nhớ tạo layout item_notification.xml như đã hướng dẫn trước đó
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification item = list.get(position);

            holder.tvTitle.setText(item.getTitle());
            holder.tvMessage.setText(item.getMessage());

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(item.getTimestamp())));

            // Đổi icon theo loại
            if ("PROMO".equals(item.getType())) {
                holder.imgIcon.setImageResource(R.drawable.ic_discount); // Icon khuyến mãi
            } else if ("REPLY".equals(item.getType())) {
                holder.imgIcon.setImageResource(R.drawable.ic_message); // Icon tin nhắn
            } else {
                holder.imgIcon.setImageResource(R.drawable.ic_notifications); // Mặc định
            }

            // Hiện chấm xanh nếu chưa đọc
            holder.viewUnread.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

            // Sự kiện Click
            holder.itemView.setOnClickListener(v -> {
                // 1. Đánh dấu đã đọc
                if (!item.isRead()) {
                    db.collection("notifications").document(item.getId()).update("isRead", true);
                    item.setRead(true);
                    notifyItemChanged(position);
                }

                // 2. Chuyển hướng
                if ("REPLY".equals(item.getType())) {
                    Intent intent = new Intent(context, BookingDetailActivity.class);
                    intent.putExtra("bookingId", item.getTargetId());
                    context.startActivity(intent);
                } else if ("PROMO".equals(item.getType())) {
                    Intent intent = new Intent(context, RoomListActivity.class);
                    context.startActivity(intent);
                }
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvTime;
            ImageView imgIcon;
            View viewUnread;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvNotiTitle);
                tvMessage = itemView.findViewById(R.id.tvNotiMessage);
                tvTime = itemView.findViewById(R.id.tvNotiTime);
                imgIcon = itemView.findViewById(R.id.imgNotiIcon);
                viewUnread = itemView.findViewById(R.id.viewUnreadDot);
            }
        }
    }
}