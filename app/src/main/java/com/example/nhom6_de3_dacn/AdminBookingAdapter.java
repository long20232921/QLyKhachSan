package com.example.nhom6_de3_dacn;

// --- CÁC DÒNG IMPORT QUAN TRỌNG ---
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
// ----------------------------------

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onEdit(Booking booking);
        void onDelete(Booking booking);
    }

    public AdminBookingAdapter(List<Booking> bookingList, OnBookingActionListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // SỬA: Thay R.id thành R.layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Đảm bảo class Booking của bạn có các hàm getter này
        holder.tvRoomName.setText(booking.getRoomName());
        holder.tvStatus.setText(booking.getStatus());

        // Đổi màu dấu chấm theo trạng thái (Dựa trên field 'status' trong Firestore của bạn)
        if ("CONFIRMED".equals(booking.getStatus())) {
            holder.tvStatus.setText("Đã đặt");
            holder.viewStatusDot.setBackgroundColor(Color.parseColor("#2196F3")); // Xanh dương
        } else if ("CHECKED_IN".equals(booking.getStatus())) {
            holder.tvStatus.setText("Đang sử dụng");
            holder.viewStatusDot.setBackgroundColor(Color.parseColor("#FF9800")); // Cam
        } else {
            holder.tvStatus.setText("Trống");
            holder.viewStatusDot.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh lá
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(booking));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvStatus;
        View viewStatusDot;
        ImageView btnEdit, btnDelete;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}