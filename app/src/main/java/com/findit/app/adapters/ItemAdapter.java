package com.findit.app.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.findit.app.R;
import com.findit.app.models.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public interface OnItemActionListener {
        void onActionClick(Item item);
    }

    private final Context context;
    private final List<Item> itemList;
    private final OnItemClickListener clickListener;
    private OnItemActionListener actionListener;
    private String actionButtonText = "";

    public ItemAdapter(Context context, List<Item> itemList, OnItemClickListener clickListener) {
        this.context = context;
        this.itemList = itemList;
        this.clickListener = clickListener;
    }

    public void setActionListener(String buttonText, OnItemActionListener actionListener) {
        this.actionButtonText = buttonText;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.tvItemTitle.setText(item.getTitle());
        holder.tvCategory.setText(item.getCategory());
        holder.tvLocation.setText(item.getLocation());
        holder.tvDate.setText(item.getDate());
        holder.tvDescription.setText(item.getDescription());

        if ("LOST".equalsIgnoreCase(item.getType())) {
            holder.tvTypeBadge.setText("LOST");
            holder.tvTypeBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
            holder.tvTypeBadge.setTextColor(Color.parseColor("#EF4444"));
        } else {
            holder.tvTypeBadge.setText("FOUND");
            holder.tvTypeBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1FAE5")));
            holder.tvTypeBadge.setTextColor(Color.parseColor("#10B981"));
        }

        String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "VERIFIED";
        holder.tvStatusBadge.setText(status);
        if ("RECOVERED".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F2FE")));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#0284C7"));
        } else if ("PENDING".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#D97706"));
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#DC2626"));
        } else {
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E7FF")));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#4338CA"));
        }

        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            holder.ivPlaceholder.setVisibility(View.GONE);
            Glide.with(context)
                    .load(item.getImageUri())
                    .placeholder(R.color.text_input_hint_color)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setVisibility(View.GONE);
            holder.ivPlaceholder.setVisibility(View.VISIBLE);
        }


        if (actionListener != null && !actionButtonText.isEmpty() && !"RECOVERED".equalsIgnoreCase(status)) {
            holder.btnItemAction.setVisibility(View.VISIBLE);
            holder.btnItemAction.setText(actionButtonText);
            holder.btnItemAction.setOnClickListener(v -> actionListener.onActionClick(item));
        } else {
            holder.btnItemAction.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemTitle, tvCategory, tvLocation, tvDate, tvDescription, tvTypeBadge, tvStatusBadge;
        ImageView ivThumbnail, ivPlaceholder;
        Button btnItemAction;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTypeBadge = itemView.findViewById(R.id.tvTypeBadge);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlaceholder = itemView.findViewById(R.id.ivPlaceholder);
            btnItemAction = itemView.findViewById(R.id.btnItemAction);
        }
    }
}
