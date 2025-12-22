package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingHistoryActivity extends AppCompatActivity {

    // Views
    private RecyclerView rvBookingHistory;
    private TextView tabUpcoming, tabPast;
    private View layoutEmpty;
    private EditText etSearch;
    private ImageView btnBack;
    private MaterialButton btnBookNowEmpty;

    // Data
    private HistoryAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();
    private List<Booking> displayList = new ArrayList<>();

    // State
    private boolean isShowUpcoming = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        initViews();
        setupRecyclerView();
        setupEvents();
        loadHistoryDataFromFirebase();
    }

    private void initViews() {
        rvBookingHistory = findViewById(R.id.rvBookingHistory);
        tabUpcoming = findViewById(R.id.tabUpcoming);
        tabPast = findViewById(R.id.tabPast);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        etSearch = findViewById(R.id.etSearchHistory);
        btnBack = findViewById(R.id.btnBackHistory);
        btnBookNowEmpty = findViewById(R.id.btnBookNowEmpty);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(displayList);
        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBookingHistory.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        tabUpcoming.setOnClickListener(v -> switchTab(true));
        tabPast.setOnClickListener(v -> switchTab(false));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnBookNowEmpty.setOnClickListener(v -> {
            Intent intent = new Intent(BookingHistoryActivity.this, RoomListActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadHistoryDataFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) return;

        db.collection("bookings")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allBookings.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Booking booking = document.toObject(Booking.class);
                                if (booking.getBookingId() == null) {
                                    booking.setBookingId(document.getId());
                                }
                                allBookings.add(booking);
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        Collections.sort(allBookings, (b1, b2) -> Long.compare(b2.getCheckInDate(), b1.getCheckInDate()));
                        filterList(etSearch.getText().toString());
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void switchTab(boolean isUpcoming) {
        this.isShowUpcoming = isUpcoming;
        if (isUpcoming) {
            tabUpcoming.setBackgroundResource(R.drawable.bg_tab_selected);
            tabUpcoming.setTextColor(Color.WHITE);
            tabPast.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabPast.setTextColor(Color.parseColor("#757575"));
        } else {
            tabUpcoming.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabUpcoming.setTextColor(Color.parseColor("#757575"));
            tabPast.setBackgroundResource(R.drawable.bg_tab_selected);
            tabPast.setTextColor(Color.WHITE);
        }
        filterList(etSearch.getText().toString());
    }

    private void filterList(String keyword) {
        displayList.clear();
        long now = System.currentTimeMillis();
        String searchLower = keyword.toLowerCase();

        for (Booking b : allBookings) {
            boolean matchesTab;

            if (isShowUpcoming) {
                matchesTab = (b.getCheckOutDate() > now) && !"CANCELLED".equals(b.getStatus());
            } else {
                matchesTab = (b.getCheckOutDate() <= now) || "CANCELLED".equals(b.getStatus());
            }

            boolean matchesSearch = b.getRoomName().toLowerCase().contains(searchLower) ||
                    (b.getBookingId() != null && b.getBookingId().toLowerCase().contains(searchLower));

            if (matchesTab && matchesSearch) {
                displayList.add(b);
            }
        }

        adapter.notifyDataSetChanged();

        if (displayList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvBookingHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvBookingHistory.setVisibility(View.VISIBLE);
        }
    }

    // --- ADAPTER ---
    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<Booking> list;
        public HistoryAdapter(List<Booking> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking item = list.get(position);

            holder.tvName.setText(item.getRoomName());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = "Nhận: " + sdf.format(new Date(item.getCheckInDate())) +
                    " - Trả: " + sdf.format(new Date(item.getCheckOutDate()));
            holder.tvDate.setText(dateStr);

            Glide.with(BookingHistoryActivity.this)
                    .load(item.getRoomImage())
                    .centerCrop()
                    .placeholder(R.drawable.bg_hotel)
                    .into(holder.imgRoom);

            String status = item.getStatus();
            if ("PENDING".equals(status)) {
                holder.tvStatus.setText("Chờ duyệt");
                holder.tvStatus.setTextColor(Color.parseColor("#D89D1C"));
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF8E1"));
            } else if ("CONFIRMED".equals(status)) {
                holder.tvStatus.setText("Đã xác nhận");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
            } else if ("CANCELLED".equals(status)) {
                holder.tvStatus.setText("Đã hủy");
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
            }

            long now = System.currentTimeMillis();

            if ("CANCELLED".equals(status)) {
                // TRƯỜNG HỢP 1: ĐÃ HỦY -> VỀ TRANG DANH SÁCH PHÒNG (RoomListActivity)
                holder.btnDetail.setText("Đặt lại phòng");
                holder.btnDetail.setBackgroundColor(Color.parseColor("#FF9800"));
                holder.btnDetail.setOnClickListener(v -> {
                    Intent intent = new Intent(BookingHistoryActivity.this, RoomListActivity.class);
                    startActivity(intent);
                });

            } else if (now > item.getCheckOutDate()) {
                // TRƯỜNG HỢP 2: ĐÃ TRẢ PHÒNG -> VIẾT ĐÁNH GIÁ
                holder.btnDetail.setText("Viết đánh giá");
                holder.btnDetail.setBackgroundColor(Color.parseColor("#4CAF50"));
                holder.btnDetail.setOnClickListener(v -> {
                    Intent intent = new Intent(BookingHistoryActivity.this, ReviewActivity.class);
                    intent.putExtra("roomId", item.getRoomId());
                    intent.putExtra("bookingId", item.getBookingId());
                    startActivity(intent);
                });

            } else {
                // TRƯỜNG HỢP 3: ĐANG HOẠT ĐỘNG -> XEM CHI TIẾT
                holder.btnDetail.setText("Xem chi tiết");
                holder.btnDetail.setBackgroundColor(Color.parseColor("#005B6F"));
                holder.btnDetail.setOnClickListener(v -> {
                    Intent intent = new Intent(BookingHistoryActivity.this, BookingDetailActivity.class);
                    intent.putExtra("bookingId", item.getBookingId());
                    startActivity(intent);
                });
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvStatus;
            ImageView imgRoom;
            MaterialButton btnDetail;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvHistoryName);
                tvDate = itemView.findViewById(R.id.tvHistoryDate);
                tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
                imgRoom = itemView.findViewById(R.id.imgHistoryRoom);
                btnDetail = itemView.findViewById(R.id.btnDetail);
            }
        }
    }
}