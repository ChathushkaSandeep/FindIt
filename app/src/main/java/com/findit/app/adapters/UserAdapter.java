package com.findit.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserStatusToggleListener {
        void onToggle(User user);
    }

    private final Context context;
    private final List<User> userList;
    private final OnUserStatusToggleListener toggleListener;

    public UserAdapter(Context context, List<User> userList, OnUserStatusToggleListener toggleListener) {
        this.context = context;
        this.userList = userList;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getFullName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserPhone.setText(user.getPhone());
        holder.tvUserRoleStatus.setText("Role: " + user.getRole() + " | Status: " + user.getStatus());

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            holder.btnToggleStatus.setVisibility(View.GONE);
        } else {
            holder.btnToggleStatus.setVisibility(View.VISIBLE);
            if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
                holder.btnToggleStatus.setText("Suspend");
                holder.btnToggleStatus.setBackgroundColor(Color.parseColor("#DC2626"));
            } else {
                holder.btnToggleStatus.setText("Activate");
                holder.btnToggleStatus.setBackgroundColor(Color.parseColor("#059669"));
            }

            holder.btnToggleStatus.setOnClickListener(v -> {
                if (toggleListener != null) {
                    toggleListener.onToggle(user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserPhone, tvUserRoleStatus;
        Button btnToggleStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserRoleStatus = itemView.findViewById(R.id.tvUserRoleStatus);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
        }
    }
}
