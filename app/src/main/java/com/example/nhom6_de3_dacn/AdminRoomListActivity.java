package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminRoomListActivity extends AppCompatActivity {

    private RecyclerView rvAdminRooms;
    private ImageView btnAddRoom, btnBack;
    private AdminRoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_room_list);

        db = FirebaseFirestore.getInstance();

        rvAdminRooms = findViewById(R.id.rvAdminRooms);
        btnAddRoom = findViewById(R.id.btnAddRoomHeader);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRoomActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms();
    }

    private void setupRecyclerView() {
        adapter = new AdminRoomAdapter(roomList);
        rvAdminRooms.setLayoutManager(new LinearLayoutManager(this));
        rvAdminRooms.setAdapter(adapter);
    }

    private void loadRooms() {
        db.collection("rooms").get().addOnSuccessListener(snapshots -> {
            roomList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                try {
                    Room room = doc.toObject(Room.class);
                    room.setId(doc.getId());
                    roomList.add(room);
                } catch (Exception e) { e.printStackTrace(); }
            }

            Collections.sort(roomList, (r1, r2) -> {
                String n1 = r1.getName();
                String n2 = r2.getName();
                boolean isNum1 = n1.matches("\\d+");
                boolean isNum2 = n2.matches("\\d+");

                if (isNum1 && isNum2) return Integer.compare(Integer.parseInt(n1), Integer.parseInt(n2));
                else if (isNum1) return -1;
                else if (isNum2) return 1;
                else return n1.compareTo(n2);
            });

            adapter.notifyDataSetChanged();
        });
    }

    private void deleteRoom(String roomId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phòng?")
                .setMessage("Bạn có chắc chắn muốn xóa phòng này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("rooms").document(roomId).delete()
                            .addOnSuccessListener(aVoid -> {
                                roomList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null).show();
    }

    class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> {
        List<Room> list;
        public AdminRoomAdapter(List<Room> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_room, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Room room = list.get(position);
            holder.tvName.setText(room.getName());

            String status = room.getStatus();
            if ("BOOKED".equals(status)) {
                holder.tvStatus.setText("Đã đặt");
                holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));
                holder.imgDot.setColorFilter(Color.parseColor("#1976D2"));
            } else if ("OCCUPIED".equals(status)) {
                holder.tvStatus.setText("Đang sử dụng");
                holder.tvStatus.setTextColor(Color.parseColor("#E64A19"));
                holder.imgDot.setColorFilter(Color.parseColor("#E64A19"));
            } else if ("CLEANING".equals(status)) {
                holder.tvStatus.setText("Đang dọn dẹp");
                holder.tvStatus.setTextColor(Color.parseColor("#FBC02D"));
                holder.imgDot.setColorFilter(Color.parseColor("#FBC02D"));
            } else {
                holder.tvStatus.setText("Trống");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.imgDot.setColorFilter(Color.parseColor("#4CAF50"));
            }

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), AddEditRoomActivity.class);
                intent.putExtra("roomId", room.getId());
                intent.putExtra("name", room.getName());
                intent.putExtra("price", room.getPrice());
                intent.putExtra("description", room.getDescription());
                intent.putExtra("image", room.getImage());
                intent.putExtra("status", room.getStatus());
                intent.putExtra("rating", room.getRating());
                holder.itemView.getContext().startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> deleteRoom(room.getId(), position));
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            ImageView imgDot, btnEdit, btnDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRoomName);
                tvStatus = itemView.findViewById(R.id.tvRoomStatus);
                imgDot = itemView.findViewById(R.id.imgStatusDot);
                btnEdit = itemView.findViewById(R.id.btnEditRoom);
                btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
            }
        }
    }
}