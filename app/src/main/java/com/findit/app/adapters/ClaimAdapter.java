package com.findit.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.models.Claim;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ClaimAdapter extends RecyclerView.Adapter<ClaimAdapter.ClaimViewHolder> {

    public interface OnClaimActionListener {
        void onApprove(Claim claim);

        void onReject(Claim claim);

        void onRecover(Claim claim);
    }

    private final Context context;
    private final List<Claim> claimList;
    private final boolean isAdminMode;
    private OnClaimActionListener actionListener;

    public ClaimAdapter(Context context, List<Claim> claimList, boolean isAdminMode) {
        this.context = context;
        this.claimList = claimList;
        this.isAdminMode = isAdminMode;
    }

    public void setActionListener(OnClaimActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.claim_card, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        Claim claim = claimList.get(position);

        holder.tvClaimItemTitle
                .setText(claim.getItemTitle() != null ? claim.getItemTitle() : "Item #" + claim.getItemId());
        holder.tvProofDesc.setText("Proof Evidence: " + claim.getProofDescription());
        holder.tvClaimDate.setText("Submitted: " + claim.getCreatedAt());

        if (claim.getAdminNotes() != null && !claim.getAdminNotes().isEmpty()) {
            holder.tvAdminNotes.setText("Admin Remarks: " + claim.getAdminNotes());
            holder.tvAdminNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvAdminNotes.setVisibility(View.GONE);
        }

        if (claim.getProofImageUri() != null && !claim.getProofImageUri().isEmpty()) {
            holder.ivProofImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(claim.getProofImageUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivProofImage);
        } else {
            holder.ivProofImage.setVisibility(View.GONE);
        }

        String status = claim.getStatus() != null ? claim.getStatus().toUpperCase() : "PENDING";
        holder.tvClaimStatusBadge.setText(status);

        if ("APPROVED".equalsIgnoreCase(status)) {
            holder.tvClaimStatusBadge.setBackgroundColor(Color.parseColor("#D1FAE5"));
            holder.tvClaimStatusBadge.setTextColor(Color.parseColor("#059669"));
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            holder.tvClaimStatusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.tvClaimStatusBadge.setTextColor(Color.parseColor("#DC2626"));
        } else {
            holder.tvClaimStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"));
            holder.tvClaimStatusBadge.setTextColor(Color.parseColor("#D97706"));
        }

        if (isAdminMode) {
            holder.tvClaimantInfo.setVisibility(View.VISIBLE);
            holder.tvClaimantInfo.setText("Claimant: "
                    + (claim.getClaimantName() != null ? claim.getClaimantName() : "User #" + claim.getClaimantId()));

            if ("PENDING".equalsIgnoreCase(status) && actionListener != null) {
                holder.layoutAdminClaimActions.setVisibility(View.VISIBLE);
                holder.btnApproveClaim.setVisibility(View.VISIBLE);
                holder.btnRejectClaim.setVisibility(View.VISIBLE);
                holder.btnRecoverClaim.setVisibility(View.GONE);
                holder.btnApproveClaim.setOnClickListener(v -> actionListener.onApprove(claim));
                holder.btnRejectClaim.setOnClickListener(v -> actionListener.onReject(claim));
            } else if ("APPROVED".equalsIgnoreCase(status) && actionListener != null) {
                holder.layoutAdminClaimActions.setVisibility(View.VISIBLE);
                holder.btnApproveClaim.setVisibility(View.GONE);
                holder.btnRejectClaim.setVisibility(View.GONE);
                holder.btnRecoverClaim.setVisibility(View.VISIBLE);
                holder.btnRecoverClaim.setOnClickListener(v -> actionListener.onRecover(claim));
            } else {
                holder.layoutAdminClaimActions.setVisibility(View.GONE);
            }
        } else {
            holder.tvClaimantInfo.setVisibility(View.GONE);
            holder.layoutAdminClaimActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }

    public static class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView tvClaimItemTitle, tvClaimStatusBadge, tvClaimantInfo, tvProofDesc, tvAdminNotes, tvClaimDate;
        ImageView ivProofImage;
        LinearLayout layoutAdminClaimActions;
        Button btnApproveClaim, btnRejectClaim, btnRecoverClaim;

        public ClaimViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClaimItemTitle = itemView.findViewById(R.id.tvClaimItemTitle);
            tvClaimStatusBadge = itemView.findViewById(R.id.tvClaimStatusBadge);
            tvClaimantInfo = itemView.findViewById(R.id.tvClaimantInfo);
            tvProofDesc = itemView.findViewById(R.id.tvProofDesc);
            ivProofImage = itemView.findViewById(R.id.ivProofImage);
            tvAdminNotes = itemView.findViewById(R.id.tvAdminNotes);
            tvClaimDate = itemView.findViewById(R.id.tvClaimDate);
            layoutAdminClaimActions = itemView.findViewById(R.id.layoutAdminClaimActions);
            btnApproveClaim = itemView.findViewById(R.id.btnApproveClaim);
            btnRejectClaim = itemView.findViewById(R.id.btnRejectClaim);
            btnRecoverClaim = itemView.findViewById(R.id.btnRecoverClaim);
        }
    }
}
