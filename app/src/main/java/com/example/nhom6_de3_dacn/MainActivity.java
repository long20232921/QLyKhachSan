package com.example.nhom6_de3_dacn;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDate, tvGuest;
    private View layoutDate, layoutGuest;
    private RecyclerView rvFeaturedRooms;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private int selectedGuestCount = 2; // Mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        checkUserLogin();
        setupFeaturedRooms();
        setupBottomNav();
        setupSearchLogic();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        rvFeaturedRooms = findViewById(R.id.rvFeaturedRooms);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        tvDate = findViewById(R.id.tvDate);
        tvGuest = findViewById(R.id.tvGuest);
        layoutDate = findViewById(R.id.layoutDate);
        layoutGuest = findViewById(R.id.layoutGuest);
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

    private void setupSearchLogic() {
        layoutDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String dateStr = dayOfMonth + "/" + (month + 1) + " - " + (dayOfMonth + 1) + "/" + (month + 1);
                        tvDate.setText(dateStr);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        layoutGuest.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("1 khách");
            popup.getMenu().add("2 khách");
            popup.getMenu().add("4 khách (Gia đình)");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                tvGuest.setText(title);
                if (title.contains("1")) selectedGuestCount = 1;
                else if (title.contains("2")) selectedGuestCount = 2;
                else if (title.contains("4")) selectedGuestCount = 4;
                return true;
            });
            popup.show();
        });

        View btnSearch = findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
                intent.putExtra("filter_guest", selectedGuestCount);
                startActivity(intent);
            });
        }
    }

    private void setupFeaturedRooms() {
        List<Room> roomList = new ArrayList<>();
        RoomAdapter adapter = new RoomAdapter(roomList);
        rvFeaturedRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFeaturedRooms.setAdapter(adapter);

        db.collection("rooms").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                roomList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Room room = document.toObject(Room.class);
                        room.setId(document.getId());
                        roomList.add(room);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupBottomNav() {
        // Đặt mục chọn mặc định là Home
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_booking) {
                startActivity(new Intent(this, RoomListActivity.class));
                return true;
            }

            if (id == R.id.nav_history) {
                startActivity(new Intent(this, BookingHistoryActivity.class));
                return true;
            }

            // --- ĐÃ SỬA: Chuyển sang ProfileActivity ---
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return true;
        });
    }

    // --- MODEL ROOM ---
    public static class Room {
        private String id;
        private String name, price, image, description;
        private double rating;
        private int maxGuests;

        public Room() { }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getImage() { return image; }
        public String getDescription() { return description; }
        public double getRating() { return rating; }
        public int getMaxGuests() { return maxGuests; }
    }

    // --- ADAPTER ---
    public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
        private List<Room> rooms;
        public RoomAdapter(List<Room> rooms) { this.rooms = rooms; }

        @NonNull @Override
        public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
            return new RoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
            Room room = rooms.get(position);
            holder.tvName.setText(room.getName());
            holder.tvPrice.setText(room.getPrice());
            Glide.with(holder.itemView.getContext()).load(room.getImage()).centerCrop().placeholder(R.drawable.bg_hotel).into(holder.imgRoom);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RoomDetailActivity.class);
                intent.putExtra("id", room.getId());
                intent.putExtra("name", room.getName());
                intent.putExtra("price", room.getPrice());
                intent.putExtra("image", room.getImage());
                intent.putExtra("description", room.getDescription());
                intent.putExtra("rating", room.getRating());
                startActivity(intent);
            });
        }
        @Override public int getItemCount() { return rooms.size(); }
        class RoomViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice; ImageView imgRoom;
            public RoomViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRoomName);
                tvPrice = itemView.findViewById(R.id.tvRoomPrice);
                imgRoom = itemView.findViewById(R.id.imgRoom);
            }
        }
    }
}