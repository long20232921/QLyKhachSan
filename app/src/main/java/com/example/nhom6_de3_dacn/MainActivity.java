package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private RecyclerView rvFeaturedRooms;
    private BottomNavigationView bottomNavigationView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ View
        tvGreeting = findViewById(R.id.tvGreeting);
        rvFeaturedRooms = findViewById(R.id.rvFeaturedRooms);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 2. Chạy logic
        checkUserLogin();
        setupFeaturedRooms();
        setupBottomNav();
        setupSearchButton(); // Logic cho nút tìm kiếm
    }

    private void checkUserLogin() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) name = user.getEmail();
            tvGreeting.setText("Xin chào, " + name);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // --- XỬ LÝ NÚT TÌM KIẾM ---
    private void setupSearchButton() {
        // Tìm nút hoặc CardView có chức năng tìm kiếm trong XML
        // Đảm bảo trong XML Bro đã đặt id là @+id/btnSearch cho nút "Kiểm tra phòng trống"
        View btnSearch = findViewById(R.id.btnSearch);

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                // Chuyển sang màn hình danh sách/tìm kiếm
                Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
                startActivity(intent);
            });
        }
    }

    // --- LẤY DỮ LIỆU TỪ FIRESTORE ---
    private void setupFeaturedRooms() {
        List<Room> roomList = new ArrayList<>();
        RoomAdapter adapter = new RoomAdapter(roomList);

        rvFeaturedRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFeaturedRooms.setAdapter(adapter);

        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        roomList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Room room = document.toObject(Room.class);
                                // QUAN TRỌNG: Lưu ID thật từ Firestore để dùng cho Booking
                                room.setId(document.getId());
                                roomList.add(room);
                            } catch (Exception e) {
                                Log.e("FirebaseError", "Lỗi convert: " + e.getMessage());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_profile) {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    // ==========================================================
    // INNER CLASS: MODEL ROOM (Đã cập nhật đầy đủ)
    // ==========================================================
    public static class Room {
        private String id; // ID document
        private String name;
        private String price;
        private String image;
        private String description;
        private double rating;

        public Room() { } // Constructor rỗng

        public Room(String name, String price, String image) {
            this.name = name;
            this.price = price;
            this.image = image;
        }

        // Getter & Setter
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getImage() { return image; }
        public String getDescription() { return description; }
        public double getRating() { return rating; }
    }

    // ==========================================================
    // INNER CLASS: ADAPTER
    // ==========================================================
    public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
        private List<Room> rooms;

        public RoomAdapter(List<Room> rooms) {
            this.rooms = rooms;
        }

        @NonNull
        @Override
        public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
            return new RoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
            Room room = rooms.get(position);
            holder.tvName.setText(room.getName());
            holder.tvPrice.setText(room.getPrice());

            Glide.with(holder.itemView.getContext())
                    .load(room.getImage())
                    .centerCrop()
                    .placeholder(R.drawable.bg_hotel)
                    .into(holder.imgRoom);

            // --- CLICK VÀO PHÒNG -> SANG CHI TIẾT ---
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RoomDetailActivity.class);
                // Gửi ID và thông tin sang RoomDetailActivity
                intent.putExtra("id", room.getId());
                intent.putExtra("name", room.getName());
                intent.putExtra("price", room.getPrice());
                intent.putExtra("image", room.getImage());
                intent.putExtra("description", room.getDescription());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return rooms.size();
        }

        class RoomViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            ImageView imgRoom;

            public RoomViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRoomName);
                tvPrice = itemView.findViewById(R.id.tvRoomPrice);
                imgRoom = itemView.findViewById(R.id.imgRoom);
            }
        }
    }
}