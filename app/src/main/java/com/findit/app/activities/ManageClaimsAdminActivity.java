package com.findit.app.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.ClaimAdapter;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.Claim;

import java.util.List;

public class ManageClaimsAdminActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvAdminClaims;
    private TextView tvEmptyAdminClaims;

    private FindItDatabaseHelper dbHelper;
    private ClaimAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_claims_admin);

        dbHelper = new FindItDatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);
        rvAdminClaims = findViewById(R.id.rvAdminClaims);
        tvEmptyAdminClaims = findViewById(R.id.tvEmptyAdminClaims);

        btnBack.setOnClickListener(v -> finish());

        rvAdminClaims.setLayoutManager(new LinearLayoutManager(this));

        loadClaims();
    }

    private void loadClaims() {
        List<Claim> allClaims = dbHelper.getAllClaims();

        if (allClaims.isEmpty()) {
            tvEmptyAdminClaims.setVisibility(View.VISIBLE);
            rvAdminClaims.setVisibility(View.GONE);
        } else {
            tvEmptyAdminClaims.setVisibility(View.GONE);
            rvAdminClaims.setVisibility(View.VISIBLE);

            adapter = new ClaimAdapter(this, allClaims, true);
            adapter.setActionListener(new ClaimAdapter.OnClaimActionListener() {
                @Override
                public void onApprove(Claim claim) {
                    showAdminNoteDialog(claim, "APPROVED");
                }

                @Override
                public void onReject(Claim claim) {
                    showAdminNoteDialog(claim, "REJECTED");
                }
            });

            rvAdminClaims.setAdapter(adapter);
        }
    }

    private void showAdminNoteDialog(Claim claim, String newStatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(newStatus + " Claim");
        builder.setMessage("Add admin remarks/notes for the claimant:");

        final EditText etInput = new EditText(this);
        etInput.setHint("e.g. Evidence verified successfully / Proof inadequate.");
        builder.setView(etInput);

        builder.setPositiveButton("Confirm " + newStatus, (dialog, which) -> {
            String notes = etInput.getText().toString().trim();
            boolean success = dbHelper.updateClaimStatus(claim.getId(), newStatus, notes);
            if (success) {
                Toast.makeText(ManageClaimsAdminActivity.this, "Claim marked as " + newStatus, Toast.LENGTH_SHORT).show();
                loadClaims();
            } else {
                Toast.makeText(ManageClaimsAdminActivity.this, "Failed to update claim status.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
