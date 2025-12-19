package com.example.nhom6_de3_dacn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.ViewHolder> {
    private List<MembershipTier> list;

    public MembershipAdapter(List<MembershipTier> list) { this.list = list; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_membership, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MembershipTier item = list.get(position);
        holder.tvName.setText(item.getName());
        holder.tvReq.setText(item.getRequirement());
        holder.tvBenefits.setText(item.getBenefits());

        // Đổi màu nền nhạt theo hạng để đẹp hơn
        holder.layoutBg.setBackgroundColor(item.getColorCode());
    }

    @Override public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvReq, tvBenefits;
        LinearLayout layoutBg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTierName);
            tvReq = itemView.findViewById(R.id.tvTierReq);
            tvBenefits = itemView.findViewById(R.id.tvTierBenefits);
            layoutBg = itemView.findViewById(R.id.layoutTierBackground);
        }
    }
}