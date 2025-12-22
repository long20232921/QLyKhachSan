package com.example.nhom6_de3_dacn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private List<Employee> list;
    private Context context;
    private OnEmployeeAction listener;

    public interface OnEmployeeAction {
        void onEdit(Employee emp);
        void onDelete(Employee emp);
    }

    public EmployeeAdapter(Context context, List<Employee> list, OnEmployeeAction listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee emp = list.get(position);
        holder.tvName.setText(emp.getName());
        holder.tvRole.setText(emp.getRole());

        if (emp.getImage() != null && !emp.getImage().isEmpty()) {
            Glide.with(context)
                    .load(emp.getImage())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        // Xử lý nút 3 chấm
        holder.btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnOptions);
            popup.getMenu().add("Sửa thông tin / Phân việc");
            popup.getMenu().add("Xóa nhân viên");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Sửa thông tin / Phân việc")) {
                    listener.onEdit(emp);
                } else {
                    listener.onDelete(emp);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole;
        ImageView btnOptions, imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEmpName);
            tvRole = itemView.findViewById(R.id.tvEmpRole);
            btnOptions = itemView.findViewById(R.id.btnOptions);
            imgAvatar = itemView.findViewById(R.id.imgEmpAvatar);
        }
    }
}