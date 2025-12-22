package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminBookingManagerActivity extends AppCompatActivity {
    private RecyclerView rvBookingList;
    private AdminBookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_booking_manager);

        db = FirebaseFirestore.getInstance();
        rvBookingList = findViewById(R.id.rvBookingList);
        rvBookingList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminBookingAdapter(bookingList, new AdminBookingAdapter.OnBookingActionListener() {
            @Override
            public void onEdit(Booking booking) {
            }

            @Override
            public void onDelete(Booking booking) {
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
                        b.setBookingId(doc.getId());
                        bookingList.add(b);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}