//package com.example.nhom6_de3_dacn;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//
//public class AdminMainActivity extends AppCompatActivity {
//
//    private RecyclerView rvAdminRooms;
//    private FloatingActionButton fabAddRoom;
//    private ImageView btnLogout;
//    private Button btnManageBooking; // Khai báo nút mới
//
//    private AdminRoomAdapter adapter;
//    private List<Room> roomList = new ArrayList<>();
//    private FirebaseFirestore db;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_main);
//
//        db = FirebaseFirestore.getInstance();
//        mAuth = FirebaseAuth.getInstance();
//
//        initViews();
//        setupRecyclerView();
//        setupEvents();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadRoomsFromFirebase();
//    }
//
//    private void initViews() {
//        rvAdminRooms = findViewById(R.id.rvAdminRooms);
//        fabAddRoom = findViewById(R.id.fabAddRoom);
//        btnLogout = findViewById(R.id.btnLogoutAdmin);
//
//        // Ánh xạ nút mới
//        btnManageBooking = findViewById(R.id.btnManageBooking);
//    }
//
//    private void setupRecyclerView() {
//        adapter = new AdminRoomAdapter(roomList);
//        rvAdminRooms.setLayoutManager(new LinearLayoutManager(this));
//        rvAdminRooms.setAdapter(adapter);
//    }
//
//    private void setupEvents() {
//        // 1. Thêm phòng
//        fabAddRoom.setOnClickListener(v -> {
//            Intent intent = new Intent(AdminMainActivity.this, AddEditRoomActivity.class);
//            startActivity(intent);
//        });
//
//        // 2. Đăng xuất
//        btnLogout.setOnClickListener(v -> {
//            mAuth.signOut();
//            Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        });
//
//        // 3. MỞ TRANG QUẢN LÝ ĐƠN HÀNG (Mới thêm)
//        btnManageBooking.setOnClickListener(v -> {
//            Intent intent = new Intent(AdminMainActivity.this, AdminBookingActivity.class);
//            startActivity(intent);
//        });
//    }
//
//    private void loadRoomsFromFirebase() {
//        db.collection("rooms").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                roomList.clear();
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    try {
//                        Room room = document.toObject(Room.class);
//                        room.setId(document.getId());
//                        roomList.add(room);
//                    } catch (Exception e) { e.printStackTrace(); }
//                }
//                adapter.notifyDataSetChanged();
//            } else {
//                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // --- ADAPTER ---
//    public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> {
//        private List<Room> list;
//        public AdminRoomAdapter(List<Room> list) { this.list = list; }
//
//        @NonNull @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_list, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            Room room = list.get(position);
//            holder.tvName.setText(room.getName());
//
//            try {
//                long price = Long.parseLong(room.getPrice().replaceAll("[^0-9]", ""));
//                DecimalFormat formatter = new DecimalFormat("#,###");
//                holder.tvPrice.setText(formatter.format(price) + " VNĐ");
//            } catch (Exception e) {
//                holder.tvPrice.setText(room.getPrice());
//            }
//
//            Glide.with(holder.itemView.getContext())
//                    .load(room.getImage())
//                    .centerCrop()
//                    .placeholder(R.drawable.bg_hotel)
//                    .into(holder.imgRoom);
//
//            holder.itemView.setOnClickListener(v -> {
//                Intent intent = new Intent(AdminMainActivity.this, AddEditRoomActivity.class);
//                intent.putExtra("roomId", room.getId());
//                intent.putExtra("name", room.getName());
//                intent.putExtra("price", room.getPrice());
//                intent.putExtra("image", room.getImage());
//                intent.putExtra("description", room.getDescription());
//                intent.putExtra("rating", room.getRating());
//                startActivity(intent);
//            });
//        }
//
//        @Override public int getItemCount() { return list.size(); }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//            TextView tvName, tvPrice; ImageView imgRoom;
//            public ViewHolder(@NonNull View itemView) {
//                super(itemView);
//                tvName = itemView.findViewById(R.id.tvRoomListName);
//                tvPrice = itemView.findViewById(R.id.tvRoomListPrice);
//                imgRoom = itemView.findViewById(R.id.imgRoomList);
//            }
//        }
//    }
//}
//sửa
package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private RecyclerView rvAdminRooms;
    private FloatingActionButton fabAddRoom;
    private ImageView btnLogout;

    private Button btnManageBooking;
    private Button btnReport;
    private Button btnAdminManageRoomStatus; // ✅ Đã thêm khai báo để hết lỗi

    private AdminRoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoomsFromFirebase();
    }

    // ================== INIT VIEW ==================
    private void initViews() {
        rvAdminRooms = findViewById(R.id.rvAdminRooms);
        fabAddRoom = findViewById(R.id.fabAddRoom);
        btnLogout = findViewById(R.id.btnLogoutAdmin);

        btnManageBooking = findViewById(R.id.btnManageBooking);
        btnReport = findViewById(R.id.btnReport);

        // ✅ Đã thêm ánh xạ từ XML
        btnAdminManageRoomStatus = findViewById(R.id.btnAdminManageRoomStatus);
    }

    // ================== RECYCLER VIEW ==================
    private void setupRecyclerView() {
        adapter = new AdminRoomAdapter(roomList);
        rvAdminRooms.setLayoutManager(new LinearLayoutManager(this));
        rvAdminRooms.setAdapter(adapter);
    }

    // ================== EVENTS ==================
    private void setupEvents() {

        // 1. Thêm phòng
        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AddEditRoomActivity.class);
            startActivity(intent);
        });

        // 2. Đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 3. Quản lý đơn đặt phòng (Màn hình cũ)
        btnManageBooking.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminBookingActivity.class);
            startActivity(intent);
        });

        // 4. 📊 Mở báo cáo thống kê
        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // 5. 🏠 MỞ QUẢN LÝ TRẠNG THÁI PHÒNG (Màn hình mới tạo)
        if (btnAdminManageRoomStatus != null) {
            btnAdminManageRoomStatus.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMainActivity.this, AdminBookingManagerActivity.class);
                startActivity(intent);
            });
        }
    }

    // ================== LOAD DATA ==================
    private void loadRoomsFromFirebase() {
        db.collection("rooms").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                roomList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Room room = document.toObject(Room.class);
                        room.setId(document.getId());
                        roomList.add(room);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== ADAPTER ==================
    public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> {

        private List<Room> list;

        public AdminRoomAdapter(List<Room> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_room_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Room room = list.get(position);
            holder.tvName.setText(room.getName());

            try {
                long price = Long.parseLong(room.getPrice().replaceAll("[^0-9]", ""));
                DecimalFormat formatter = new DecimalFormat("#,###");
                holder.tvPrice.setText(formatter.format(price) + " VNĐ");
            } catch (Exception e) {
                holder.tvPrice.setText(room.getPrice());
            }

            Glide.with(holder.itemView.getContext())
                    .load(room.getImage())
                    .centerCrop()
                    .placeholder(R.drawable.bg_hotel)
                    .into(holder.imgRoom);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMainActivity.this, AddEditRoomActivity.class);
                intent.putExtra("roomId", room.getId());
                intent.putExtra("name", room.getName());
                intent.putExtra("price", room.getPrice());
                intent.putExtra("image", room.getImage());
                intent.putExtra("description", room.getDescription());
                intent.putExtra("rating", room.getRating());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            ImageView imgRoom;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRoomListName);
                tvPrice = itemView.findViewById(R.id.tvRoomListPrice);
                imgRoom = itemView.findViewById(R.id.imgRoomList);
            }
        }
    }
}