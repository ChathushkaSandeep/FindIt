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
import com.findit.app.database.FirestoreHelper;
import com.findit.app.models.Claim;

import java.util.List;

public class ManageClaimsAdminActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvAdminClaims;
    private TextView tvEmptyAdminClaims;

    private FirestoreHelper dbHelper;
    private ClaimAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_claims_admin);

        dbHelper = new FirestoreHelper();

        btnBack = findViewById(R.id.btnBack);
        rvAdminClaims = findViewById(R.id.rvAdminClaims);
        tvEmptyAdminClaims = findViewById(R.id.tvEmptyAdminClaims);

        btnBack.setOnClickListener(v -> finish());

        rvAdminClaims.setLayoutManager(new LinearLayoutManager(this));

        loadClaims();
    }

    private void loadClaims() {
        dbHelper.getAllClaims(new FirestoreHelper.Callback<List<Claim>>() {
            @Override
            public void onSuccess(List<Claim> allClaims) {
                if (allClaims.isEmpty()) {
                    tvEmptyAdminClaims.setVisibility(View.VISIBLE);
                    rvAdminClaims.setVisibility(View.GONE);
                } else {
                    tvEmptyAdminClaims.setVisibility(View.GONE);
                    rvAdminClaims.setVisibility(View.VISIBLE);

                    adapter = new ClaimAdapter(ManageClaimsAdminActivity.this, allClaims, true);
                    adapter.setActionListener(new ClaimAdapter.OnClaimActionListener() {
                        @Override
                        public void onApprove(Claim claim) {
                            showAdminNoteDialog(claim, "APPROVED");
                        }

                        @Override
                        public void onReject(Claim claim) {
                            showAdminNoteDialog(claim, "REJECTED");
                        }

                        @Override
                        public void onRecover(Claim claim) {
                            dbHelper.markItemAsRecovered(claim.getItemId(), new FirestoreHelper.Callback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean success) {
                                    if (success) {
                                        Toast.makeText(ManageClaimsAdminActivity.this, "Item marked as RECOVERED!",
                                                Toast.LENGTH_SHORT).show();
                                        loadClaims();
                                    } else {
                                        Toast.makeText(ManageClaimsAdminActivity.this,
                                                "Failed to mark item as recovered.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(ManageClaimsAdminActivity.this, "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    rvAdminClaims.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvEmptyAdminClaims.setText("Failed to load claims");
                tvEmptyAdminClaims.setVisibility(View.VISIBLE);
            }
        });
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
            dbHelper.updateClaimStatus(claim.getId(), newStatus, notes, new FirestoreHelper.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        Toast.makeText(ManageClaimsAdminActivity.this, "Claim marked as " + newStatus,
                                Toast.LENGTH_SHORT).show();
                        loadClaims();
                    } else {
                        Toast.makeText(ManageClaimsAdminActivity.this, "Failed to update claim status.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ManageClaimsAdminActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
