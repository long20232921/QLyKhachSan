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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }

    private void setupRecyclerView() {
        adapter = new AdminBookingAdapter(bookingList);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);
    }

    private void loadBookings() {
        db.collection("bookings")
                .orderBy("checkInDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Booking booking = doc.toObject(Booking.class);
                                // Gán ID nếu trong object chưa có (đề phòng)
                                if (booking.getBookingId() == null) {
                                    booking.setBookingId(doc.getId());
                                }
                                bookingList.add(booking);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Lỗi tải đơn hàng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBookingAndRoomStatus(String bookingId, String roomId, String newBookingStatus) {
        if (bookingId == null || roomId == null) return;

        String newRoomStatus = "AVAILABLE";

        switch (newBookingStatus) {
            case "CONFIRMED":
                newRoomStatus = "BOOKED";
                break;
            case "CHECKED_IN":
                newRoomStatus = "OCCUPIED";
                break;
            case "COMPLETED":
                newRoomStatus = "CLEANING";
                break;
            case "CANCELLED":
                newRoomStatus = "AVAILABLE";
                break;
        }

        WriteBatch batch = db.batch();

        DocumentReference bookingRef = db.collection("bookings").document(bookingId);
        batch.update(bookingRef, "status", newBookingStatus);

        DocumentReference roomRef = db.collection("rooms").document(roomId);
        batch.update(roomRef, "status", newRoomStatus);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cập nhật thành công: " + newBookingStatus, Toast.LENGTH_SHORT).show();
            loadBookings();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

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

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(item.getCheckInDate())) + " - " + sdf.format(new Date(item.getCheckOutDate()));
            holder.tvDate.setText(dateStr);

            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvPrice.setText(formatter.format(item.getTotalPrice()) + " VNĐ");

            String cusInfo = item.getCustomerName() != null ? item.getCustomerName() : item.getCustomerPhone();
            if (cusInfo == null) cusInfo = "Khách vãng lai (" + item.getUserId() + ")";
            holder.tvCustomer.setText(cusInfo);

            String status = item.getStatus();
            holder.layoutButtons.setVisibility(View.VISIBLE);

            if ("PENDING".equals(status)) {
                holder.tvStatus.setText("CHỜ DUYỆT");
                holder.tvStatus.setTextColor(Color.parseColor("#E65100")); // Cam

                holder.btnApprove.setText("Duyệt đơn");
                holder.btnApprove.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh lá
                holder.btnApprove.setVisibility(View.VISIBLE);

                holder.btnReject.setText("Hủy");
                holder.btnReject.setVisibility(View.VISIBLE);

                holder.btnApprove.setOnClickListener(v ->
                        updateBookingAndRoomStatus(item.getBookingId(), item.getRoomId(), "CONFIRMED"));

            } else if ("CONFIRMED".equals(status)) {
                holder.tvStatus.setText("ĐÃ ĐẶT");
                holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));

                holder.btnApprove.setText("Check-in");
                holder.btnApprove.setBackgroundColor(Color.parseColor("#2196F3"));
                holder.btnApprove.setVisibility(View.VISIBLE);

                holder.btnReject.setText("Hủy");
                holder.btnReject.setVisibility(View.VISIBLE);

                holder.btnApprove.setOnClickListener(v ->
                        updateBookingAndRoomStatus(item.getBookingId(), item.getRoomId(), "CHECKED_IN"));

            } else if ("CHECKED_IN".equals(status)) {
                holder.tvStatus.setText("ĐANG Ở");
                holder.tvStatus.setTextColor(Color.parseColor("#E64A19"));

                holder.btnApprove.setText("Trả phòng");
                holder.btnApprove.setBackgroundColor(Color.parseColor("#FF9800"));
                holder.btnApprove.setVisibility(View.VISIBLE);

                holder.btnReject.setVisibility(View.GONE);

                holder.btnApprove.setOnClickListener(v ->
                        updateBookingAndRoomStatus(item.getBookingId(), item.getRoomId(), "COMPLETED"));

            } else if ("COMPLETED".equals(status)) {
                holder.tvStatus.setText("HOÀN TẤT");
                holder.tvStatus.setTextColor(Color.GRAY);
                holder.layoutButtons.setVisibility(View.GONE);

            } else {
                holder.tvStatus.setText("ĐÃ HỦY");
                holder.tvStatus.setTextColor(Color.RED);
                holder.layoutButtons.setVisibility(View.GONE);
            }

            holder.btnReject.setOnClickListener(v ->
                    updateBookingAndRoomStatus(item.getBookingId(), item.getRoomId(), "CANCELLED"));
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