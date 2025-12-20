package com.example.nhom6_de3_dacn;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminBookingActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private AdminBookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private FirebaseFirestore db;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_booking);

        db = FirebaseFirestore.getInstance();
        rvBookings = findViewById(R.id.rvAdminBookings);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadBookings();
    }

    private void setupRecyclerView() {
        adapter = new AdminBookingAdapter(bookingList);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);
    }

    private void loadBookings() {
        // Lấy tất cả đơn, sắp xếp mới nhất lên đầu
        db.collection("bookings")
                .orderBy("checkInDate", Query.Direction.DESCENDING) // Cần tạo index nếu lỗi, hoặc bỏ orderBy đi để test trước
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Booking booking = doc.toObject(Booking.class);
                                // Booking class cần có field bookingId (document ID) để update
                                // Nếu class Booking chưa có field id riêng, ta gán thủ công:
                                // booking.setBookingId(doc.getId());
                                // (Lưu ý: Bro kiểm tra lại class Booking xem đã lưu bookingId vào trong data chưa,
                                // nếu lúc tạo đơn Bro đã lưu bookingId vào map thì ok).
                                bookingList.add(booking);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Lỗi tải đơn hàng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        if (bookingId == null) return;

        db.collection("bookings").document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    loadBookings(); // Load lại danh sách để thấy thay đổi
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- ADAPTER ---
    public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {
        private List<Booking> list;
        public AdminBookingAdapter(List<Booking> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking item = list.get(position);

            holder.tvRoomName.setText(item.getRoomName());

            // Format ngày
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String dateStr = sdf.format(new Date(item.getCheckInDate())) + " - " + sdf.format(new Date(item.getCheckOutDate()));
            holder.tvDate.setText("Lịch: " + dateStr);

            // Format tiền
            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvPrice.setText("Tổng: " + formatter.format(item.getTotalPrice()) + " đ");

            // Hiển thị thông tin khách (Cần đảm bảo lúc tạo đơn Bro đã lưu userId hoặc phone)
            // Tạm thời hiển thị User ID nếu chưa có tên
            holder.tvCustomer.setText("Mã KH: " + item.getUserId());

            // Xử lý trạng thái
            String status = item.getStatus();
            holder.tvStatus.setText(status);

            if ("PENDING".equals(status)) {
                holder.tvStatus.setTextColor(Color.parseColor("#E65100")); // Cam
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
                holder.layoutButtons.setVisibility(View.VISIBLE); // Hiện nút duyệt/hủy
            } else if ("CONFIRMED".equals(status)) {
                holder.tvStatus.setText("ĐÃ DUYỆT");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Xanh
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.layoutButtons.setVisibility(View.GONE); // Đã duyệt rồi thì ẩn nút đi cho gọn
            } else {
                holder.tvStatus.setText("ĐÃ HỦY");
                holder.tvStatus.setTextColor(Color.parseColor("#C62828")); // Đỏ
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                holder.layoutButtons.setVisibility(View.GONE);
            }

            // Sự kiện nút bấm
            holder.btnApprove.setOnClickListener(v -> updateBookingStatus(item.getBookingId(), "CONFIRMED"));
            holder.btnReject.setOnClickListener(v -> updateBookingStatus(item.getBookingId(), "CANCELLED"));
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRoomName, tvStatus, tvCustomer, tvDate, tvPrice;
            Button btnApprove, btnReject;
            LinearLayout layoutButtons;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRoomName = itemView.findViewById(R.id.tvRoomName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvCustomer = itemView.findViewById(R.id.tvCustomerInfo);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvPrice = itemView.findViewById(R.id.tvTotalPrice);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
                layoutButtons = itemView.findViewById(R.id.layoutActionButtons);
            }
        }
    }
}