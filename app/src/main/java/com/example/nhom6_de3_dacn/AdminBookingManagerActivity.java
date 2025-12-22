package com.example.nhom6_de3_dacn;

// --- CÁC DÒNG IMPORT CẦN THIẾT ---
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
// --------------------------------

public class AdminBookingManagerActivity extends AppCompatActivity {
    private RecyclerView rvBookingList;
    private AdminBookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn đã tạo file activity_admin_booking_manager.xml
        setContentView(R.layout.activity_admin_booking_manager);

        db = FirebaseFirestore.getInstance();
        rvBookingList = findViewById(R.id.rvBookingList);
        rvBookingList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminBookingAdapter(bookingList, new AdminBookingAdapter.OnBookingActionListener() {
            @Override
            public void onEdit(Booking booking) {
                // Bạn có thể xử lý logic sửa ở đây
            }

            @Override
            public void onDelete(Booking booking) {
                // Xóa khỏi Firestore dựa trên bookingId
                if (booking.getBookingId() != null) {
                    db.collection("bookings").document(booking.getBookingId()).delete();
                }
            }
        });
        rvBookingList.setAdapter(adapter);

        loadBookingsFromFirestore();
    }

    private void loadBookingsFromFirestore() {
        db.collection("bookings").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                bookingList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    if (b != null) {
                        // Gán ID của document vào object để sau này dùng để xóa/sửa
                        b.setBookingId(doc.getId());
                        bookingList.add(b);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}