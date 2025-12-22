package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminMainActivity extends AppCompatActivity {

    private View cardManageRooms, cardManageBooking, cardReviews, cardPromo, cardReport, cardStaff;
    private View layoutLogout, btnNotiHeader;
    private TextView tvStatCheckIn, tvStatCheckOut, tvStatOccupancy;
    private BottomNavigationView bottomNavAdmin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRealtimeStats();
    }

    private void initViews() {
        tvStatCheckIn = findViewById(R.id.tvStatCheckIn);
        tvStatCheckOut = findViewById(R.id.tvStatCheckOut);
        tvStatOccupancy = findViewById(R.id.tvStatOccupancy);
        btnNotiHeader = findViewById(R.id.btnNotiHeader);

        cardManageRooms = findViewById(R.id.cardManageRooms);
        cardManageBooking = findViewById(R.id.cardManageBooking);
        cardReviews = findViewById(R.id.cardReviews);
        cardPromo = findViewById(R.id.cardPromo);
        cardReport = findViewById(R.id.cardReport);
        cardStaff = findViewById(R.id.cardStaff);

        layoutLogout = findViewById(R.id.layoutLogout);
        bottomNavAdmin = findViewById(R.id.bottomNavAdmin);
    }

    private void setupEvents() {
        cardManageRooms.setOnClickListener(v -> startActivity(new Intent(this, AdminRoomListActivity.class)));
        cardManageBooking.setOnClickListener(v -> startActivity(new Intent(this, AdminBookingActivity.class)));
        cardReviews.setOnClickListener(v -> startActivity(new Intent(this, AdminReviewActivity.class)));
        cardPromo.setOnClickListener(v -> startActivity(new Intent(this, AdminSendNotificationActivity.class)));
        cardReport.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
        cardStaff.setOnClickListener(v -> startActivity(new Intent(this, AdminStaffActivity.class)));

        btnNotiHeader.setOnClickListener(v -> Toast.makeText(this, "Hệ thống hoạt động bình thường", Toast.LENGTH_SHORT).show());

        layoutLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNavAdmin.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_home) {
                loadRealtimeStats();
                return true;
            } else if (id == R.id.nav_admin_booking) {
                startActivity(new Intent(this, AdminBookingActivity.class));
                return true;
            } else if (id == R.id.nav_admin_room) {
                startActivity(new Intent(this, AdminRoomListActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadRealtimeStats() {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        db.collection("bookings")
                .whereEqualTo("checkInDate", today)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String status = doc.getString("status");
                        if ("BOOKED".equals(status) || "CONFIRMED".equals(status)) {
                            count++;
                        }
                    }
                    tvStatCheckIn.setText(String.valueOf(count));
                });

        db.collection("bookings")
                .whereEqualTo("checkOutDate", today)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String status = doc.getString("status");
                        if ("OCCUPIED".equals(status)) {
                            count++;
                        }
                    }
                    tvStatCheckOut.setText(String.valueOf(count));
                });

        db.collection("rooms").get().addOnSuccessListener(snapshots -> {
            int totalRooms = snapshots.size();
            int occupiedCount = 0;

            for (QueryDocumentSnapshot doc : snapshots) {
                String status = doc.getString("status");
                if ("OCCUPIED".equals(status) || "BOOKED".equals(status)) {
                    occupiedCount++;
                }
            }

            if (totalRooms > 0) {
                int percent = (occupiedCount * 100) / totalRooms;
                tvStatOccupancy.setText(percent + "%");
            } else {
                tvStatOccupancy.setText("0%");
            }
        });
    }
}