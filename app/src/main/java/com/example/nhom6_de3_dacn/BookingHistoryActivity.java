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

import java.text.DecimalFormat;
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

    // 👇 Khai báo nút mới
    private MaterialButton btnBookNowEmpty;

    // Data
    private HistoryAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>(); // Danh sách gốc lấy từ DB
    private List<Booking> displayList = new ArrayList<>(); // Danh sách đang hiển thị (đã lọc)

    // State
    private boolean isShowUpcoming = true; // Mặc định là tab Sắp tới

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

        // 👇 Ánh xạ nút đặt phòng ngay
        btnBookNowEmpty = findViewById(R.id.btnBookNowEmpty);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(displayList);
        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBookingHistory.setAdapter(adapter);
    }

    private void setupEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Chuyển Tab
        tabUpcoming.setOnClickListener(v -> switchTab(true));
        tabPast.setOnClickListener(v -> switchTab(false));

        // Tìm kiếm (Lọc theo tên phòng)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 👇 SỰ KIỆN MỚI: Bấm nút "Đặt phòng ngay" ở màn hình trống
        btnBookNowEmpty.setOnClickListener(v -> {
            // Chuyển sang trang danh sách phòng để đặt
            Intent intent = new Intent(BookingHistoryActivity.this, RoomListActivity.class);
            startActivity(intent);
            finish(); // Đóng trang lịch sử lại
        });
    }

    // --- LOGIC 1: TẢI DỮ LIỆU ---
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
                                allBookings.add(booking);
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        // Sắp xếp theo ngày đặt mới nhất lên đầu
                        Collections.sort(allBookings, (b1, b2) -> Long.compare(b2.getCheckInDate(), b1.getCheckInDate()));

                        // Lọc và hiển thị dữ liệu ban đầu
                        filterList(etSearch.getText().toString());
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- LOGIC 2: CHUYỂN TAB ---
    private void switchTab(boolean isUpcoming) {
        this.isShowUpcoming = isUpcoming;

        // Đổi màu giao diện Tab
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

    // --- LOGIC 3: BỘ LỌC ---
    private void filterList(String keyword) {
        displayList.clear();
        long now = System.currentTimeMillis();
        String searchLower = keyword.toLowerCase();

        for (Booking b : allBookings) {
            boolean matchesTab;
            // Lọc theo Tab
            if (isShowUpcoming) {
                matchesTab = b.getCheckOutDate() > now;
            } else {
                matchesTab = b.getCheckOutDate() <= now;
            }

            // Lọc theo Tìm kiếm
            boolean matchesSearch = b.getRoomName().toLowerCase().contains(searchLower) ||
                    b.getBookingId().toLowerCase().contains(searchLower);

            if (matchesTab && matchesSearch) {
                displayList.add(b);
            }
        }

        adapter.notifyDataSetChanged();

        // Xử lý Empty State
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

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String dateStr = "Check-in: " + sdf.format(new Date(item.getCheckInDate())) +
                    " - Check-out: " + sdf.format(new Date(item.getCheckOutDate()));
            holder.tvDate.setText(dateStr);

            Glide.with(BookingHistoryActivity.this)
                    .load(item.getRoomImage())
                    .centerCrop()
                    .placeholder(R.drawable.bg_hotel)
                    .into(holder.imgRoom);

            // Xử lý Trạng thái
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

            // Nút Xem chi tiết (Demo)
            holder.btnDetail.setOnClickListener(v ->
                    Toast.makeText(BookingHistoryActivity.this, "Mã đơn: " + item.getBookingId(), Toast.LENGTH_SHORT).show()
            );
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvStatus;
            ImageView imgRoom;
            View btnCancel, btnDetail;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvHistoryName);
                tvDate = itemView.findViewById(R.id.tvHistoryDate);
                tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
                imgRoom = itemView.findViewById(R.id.imgHistoryRoom);
                btnCancel = itemView.findViewById(R.id.btnCancel);
                btnDetail = itemView.findViewById(R.id.btnDetail);
            }
        }
    }
}