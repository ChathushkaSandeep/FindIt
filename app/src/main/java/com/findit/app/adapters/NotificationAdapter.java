package com.findit.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.models.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final Context context;
    private final List<NotificationItem> notificationList;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_card, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        holder.tvNotifTitle.setText(item.getTitle());
        holder.tvNotifMessage.setText(item.getMessage());
        holder.tvNotifTime.setText(item.getCreatedAt());

        if ("CLAIM_APPROVED".equalsIgnoreCase(item.getType())) {
            holder.ivNotifIcon.setImageResource(android.R.drawable.checkbox_on_background);
            holder.ivNotifIcon.setColorFilter(Color.parseColor("#059669"));
        } else if ("CLAIM_REJECTED".equalsIgnoreCase(item.getType())) {
            holder.ivNotifIcon.setImageResource(android.R.drawable.ic_delete);
            holder.ivNotifIcon.setColorFilter(Color.parseColor("#DC2626"));
        } else if ("MATCH_ALERT".equalsIgnoreCase(item.getType())) {
            holder.ivNotifIcon.setImageResource(android.R.drawable.ic_menu_search);
            holder.ivNotifIcon.setColorFilter(Color.parseColor("#D97706"));
        } else {
            holder.ivNotifIcon.setImageResource(android.R.drawable.ic_popup_reminder);
            holder.ivNotifIcon.setColorFilter(Color.parseColor("#2563EB"));
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNotifIcon;
        TextView tvNotifTitle, tvNotifMessage, tvNotifTime;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNotifIcon = itemView.findViewById(R.id.ivNotifIcon);
            tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvNotifMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvNotifTime = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}
