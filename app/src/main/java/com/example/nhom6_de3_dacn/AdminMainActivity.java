package com.example.nhom6_de3_dacn;

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

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private RecyclerView rvAdminRooms;
    private FloatingActionButton fabAddRoom;
    private ImageView btnLogout;
    private AdminRoomAdapter adapter;
    private List<MainActivity.Room> roomList = new ArrayList<>(); // Tái sử dụng model Room
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

    // Load lại dữ liệu mỗi khi quay lại màn hình này (Ví dụ: sau khi Thêm/Sửa xong)
    @Override
    protected void onResume() {
        super.onResume();
        loadRoomsFromFirebase();
    }

    private void initViews() {
        rvAdminRooms = findViewById(R.id.rvAdminRooms);
        fabAddRoom = findViewById(R.id.fabAddRoom);
        btnLogout = findViewById(R.id.btnLogoutAdmin);
    }

    private void setupRecyclerView() {
        // Dùng layout Manager dọc (Vertical) để hiện danh sách dài
        adapter = new AdminRoomAdapter(roomList);
        rvAdminRooms.setLayoutManager(new LinearLayoutManager(this));
        rvAdminRooms.setAdapter(adapter);
    }

    private void setupEvents() {
        // 1. Nút Thêm phòng mới (+)
        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AddEditRoomActivity.class);
            // Không gửi data gì cả -> Bên kia sẽ hiểu là THÊM MỚI
            startActivity(intent);
        });

        // 2. Nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng màn hình Admin
        });
    }

    private void loadRoomsFromFirebase() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        roomList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert dữ liệu
                            MainActivity.Room room = document.toObject(MainActivity.Room.class);
                            // QUAN TRỌNG: Lưu ID document vào object để sau này biết mà Sửa/Xóa đúng bài
                            room.setId(document.getId());
                            roomList.add(room);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- ADAPTER RIÊNG CHO ADMIN ---
    // (Dùng layout item_room_list để hiển thị đẹp hơn dạng danh sách dọc)
    public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> {
        private List<MainActivity.Room> list;

        public AdminRoomAdapter(List<MainActivity.Room> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Sử dụng item_room_list.xml (layout danh sách dọc mà Bro đã tạo ở bước Tìm kiếm)
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MainActivity.Room room = list.get(position);
            holder.tvName.setText(room.getName());
            holder.tvPrice.setText(room.getPrice());

            // Load ảnh
            Glide.with(holder.itemView.getContext())
                    .load(room.getImage())
                    .centerCrop()
                    .placeholder(R.drawable.bg_hotel)
                    .into(holder.imgRoom);

            // --- SỰ KIỆN CLICK VÀO ITEM ---
            holder.itemView.setOnClickListener(v -> {
                // Chuyển sang trang Sửa/Xóa
                Intent intent = new Intent(AdminMainActivity.this, AddEditRoomActivity.class);

                // Gửi kèm dữ liệu cũ sang để hiển thị lên form
                intent.putExtra("roomId", room.getId()); // ID rất quan trọng để sửa đúng bài
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
                // Ánh xạ theo ID bên trong file item_room_list.xml
                tvName = itemView.findViewById(R.id.tvRoomListName);
                tvPrice = itemView.findViewById(R.id.tvRoomListPrice);
                imgRoom = itemView.findViewById(R.id.imgRoomList);
            }
        }
    }
}