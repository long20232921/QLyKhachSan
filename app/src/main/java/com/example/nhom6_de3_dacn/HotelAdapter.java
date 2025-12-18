package com.example.nhom6_de3_dacn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> mListHotel;

    public HotelAdapter(List<Hotel> listHotel) {
        this.mListHotel = listHotel;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = mListHotel.get(position);
        if (hotel == null) return;

        holder.tvName.setText(hotel.getName());
        holder.tvLocation.setText(hotel.getLocation());
        holder.tvRating.setText(hotel.getRating() + " ⭐");
        holder.tvPrice.setText(hotel.getPrice());
        holder.imgHotel.setImageResource(hotel.getImageResId());
    }

    @Override
    public int getItemCount() {
        return mListHotel != null ? mListHotel.size() : 0;
    }

    public class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel;
        TextView tvName, tvLocation, tvRating, tvPrice;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgHotel);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}