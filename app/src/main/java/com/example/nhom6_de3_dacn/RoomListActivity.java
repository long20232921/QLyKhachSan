package com.example.nhom6_de3_dacn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends AppCompatActivity {

    private RecyclerView rvRoomList;
    private RoomListAdapter adapter;
    private List<MainActivity.Room> originalList = new ArrayList<>();
    private List<MainActivity.Room> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private MaterialButton btnPrice, btnType, btnGuest;

    // Biến lưu trạng thái lọc hiện tại
    private int currentMinGuest = 0; // 0 nghĩa là không lọc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        initViews();
        setupRecyclerView();

        // --- XỬ LÝ DỮ LIỆU TỪ MAIN ACTIVITY GỬI SANG ---
        if (getIntent().hasExtra("filter_guest")) {
            int guestFromMain = getIntent().getIntExtra("filter_guest", 2);
            currentMinGuest = guestFromMain;

            // Cập nhật text trên nút để người dùng biết đang lọc
            if (guestFromMain == 1) btnGuest.setText("1 khách");
            else if (guestFromMain == 2) btnGuest.setText("2 khách");
            else if (guestFromMain == 4) btnGuest.setText("Gia đình");
        }

        loadDataFromFirebase();
        setupFilterEvents();
    }

    private void initViews() {
        rvRoomList = findViewById(R.id.rvRoomList);
        btnPrice = findViewById(R.id.btnFilterPrice);
        btnType = findViewById(R.id.btnFilterType);
        btnGuest = findViewById(R.id.btnFilterGuest);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new RoomListAdapter(filteredList);
        rvRoomList.setLayoutManager(new LinearLayoutManager(this));
        rvRoomList.setAdapter(adapter);
    }

    private void loadDataFromFirebase() {
        db = FirebaseFirestore.getInstance();
        db.collection("rooms").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                originalList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        MainActivity.Room room = document.toObject(MainActivity.Room.class);
                        room.setId(document.getId());

                        // Nếu dữ liệu cũ chưa có maxGuests, mặc định cho là 2 để test
                        if (room.getMaxGuests() == 0) room.setMaxGuests(2);

                        originalList.add(room);
                    } catch (Exception e) { e.printStackTrace(); }
                    if (currentMinGuest > 0) {
                        filterByOption("GUEST_FILTER");
                    } else {
                        filterByOption("ALL");
                    }
                }
                filterByOption("ALL");
            }
        });
    }

    private void setupFilterEvents() {
        // --- BỘ LỌC GIÁ ---
        btnPrice.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Tất cả mức giá");
            popup.getMenu().add("Dưới 1 triệu");
            popup.getMenu().add("1 - 3 triệu");
            popup.getMenu().add("Trên 3 triệu");
            popup.setOnMenuItemClickListener(item -> {
                btnPrice.setText(item.getTitle());
                String title = item.getTitle().toString();
                if (title.contains("Tất cả")) filterByOption("ALL");
                else if (title.contains("Dưới 1")) filterByOption("CHEAP");
                else if (title.contains("1 - 3")) filterByOption("MEDIUM");
                else if (title.contains("Trên 3")) filterByOption("VIP");
                return true;
            });
            popup.show();
        });

        // --- BỘ LỌC LOẠI PHÒNG (Ví dụ lọc theo sao) ---
        btnType.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Tất cả");
            popup.getMenu().add("Đánh giá cao (> 4.5⭐)");
            popup.setOnMenuItemClickListener(item -> {
                btnType.setText(item.getTitle());
                if (item.getTitle().toString().contains("cao")) filterByOption("RATING");
                else filterByOption("ALL");
                return true;
            });
            popup.show();
        });

        // --- BỘ LỌC SỐ KHÁCH (Đã hoàn thiện) ---
        btnGuest.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Bất kỳ");
            popup.getMenu().add("1 người");
            popup.getMenu().add("2 người");
            popup.getMenu().add("Gia đình (4+ người)");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                btnGuest.setText(title);

                if (title.contains("1")) currentMinGuest = 1;
                else if (title.contains("2")) currentMinGuest = 2;
                else if (title.contains("4")) currentMinGuest = 4;
                else currentMinGuest = 0; // Bất kỳ

                // Gọi lại hàm lọc để áp dụng
                filterByOption("GUEST_FILTER");
                return true;
            });
            popup.show();
        });
    }

    // Logic lọc tổng hợp
    private void filterByOption(String option) {
        filteredList.clear();
        for (MainActivity.Room room : originalList) {
            long price = parsePrice(room.getPrice());
            boolean matches = false;

            // 1. Check điều kiện lọc loại/giá trước
            switch (option) {
                case "ALL": case "GUEST_FILTER": matches = true; break;
                case "CHEAP": if (price < 1000000) matches = true; break;
                case "MEDIUM": if (price >= 1000000 && price <= 3000000) matches = true; break;
                case "VIP": if (price > 3000000) matches = true; break;
                case "RATING": if (room.getRating() >= 4.5) matches = true; break;
            }

            // 2. Check điều kiện số khách (Kết hợp AND)
            if (matches) {
                // Nếu phòng chứa được ít nhất số khách yêu cầu
                if (room.getMaxGuests() >= currentMinGuest) {
                    filteredList.add(room);
                }
            }
        }

        adapter.notifyDataSetChanged();
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy phòng phù hợp!", Toast.LENGTH_SHORT).show();
        }
    }

    private long parsePrice(String priceStr) {
        if (priceStr == null) return 0;
        try { return Long.parseLong(priceStr.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    // --- ADAPTER ---
    public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {
        private List<MainActivity.Room> list;
        public RoomListAdapter(List<MainActivity.Room> list) { this.list = list; }
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MainActivity.Room room = list.get(position);
            holder.tvName.setText(room.getName());

            // Hiển thị Rating động
            if (room.getRating() > 0) {
                holder.tvRating.setText("⭐ " + room.getRating() + " (Tuyệt vời)");
                holder.tvRating.setVisibility(View.VISIBLE);
            } else {
                holder.tvRating.setVisibility(View.GONE);
            }

            try {
                long priceVal = parsePrice(room.getPrice());
                DecimalFormat formatter = new DecimalFormat("#,###");
                holder.tvPrice.setText(formatter.format(priceVal));
            } catch (Exception e) { holder.tvPrice.setText(room.getPrice()); }

            Glide.with(RoomListActivity.this).load(room.getImage()).centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground).into(holder.imgRoom);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(RoomListActivity.this, RoomDetailActivity.class);
                intent.putExtra("id", room.getId());
                intent.putExtra("name", room.getName());
                intent.putExtra("price", room.getPrice());
                intent.putExtra("image", room.getImage());
                intent.putExtra("description", room.getDescription());
                intent.putExtra("rating", room.getRating());
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvRating;
            ImageView imgRoom;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRoomListName);
                tvPrice = itemView.findViewById(R.id.tvRoomListPrice);
                imgRoom = itemView.findViewById(R.id.imgRoomList);
                tvRating = itemView.findViewById(R.id.tvRoomListRating); // Đã có ID này trong XML mới
            }
        }
    }
}