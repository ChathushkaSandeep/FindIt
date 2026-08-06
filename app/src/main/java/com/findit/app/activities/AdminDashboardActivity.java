package com.findit.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FindItDatabaseHelper;

public class AdminDashboardActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvStatTotalUsers, tvStatLostItems, tvStatPendingClaims, tvStatRecovered;
    private Button btnNavManageUsers, btnNavVerifyReports, btnNavManageClaims, btnNavGenerateReports;

    private FindItDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new FindItDatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);
        tvStatTotalUsers = findViewById(R.id.tvStatTotalUsers);
        tvStatLostItems = findViewById(R.id.tvStatLostItems);
        tvStatPendingClaims = findViewById(R.id.tvStatPendingClaims);
        tvStatRecovered = findViewById(R.id.tvStatRecovered);

        btnNavManageUsers = findViewById(R.id.btnNavManageUsers);
        btnNavVerifyReports = findViewById(R.id.btnNavVerifyReports);
        btnNavManageClaims = findViewById(R.id.btnNavManageClaims);
        btnNavGenerateReports = findViewById(R.id.btnNavGenerateReports);

        btnBack.setOnClickListener(v -> finish());

        btnNavManageUsers.setOnClickListener(v -> startActivity(new Intent(this, ManageUsersActivity.class)));
        btnNavVerifyReports.setOnClickListener(v -> startActivity(new Intent(this, VerifyReportsActivity.class)));
        btnNavManageClaims.setOnClickListener(v -> startActivity(new Intent(this, ManageClaimsAdminActivity.class)));
        btnNavGenerateReports.setOnClickListener(v -> startActivity(new Intent(this, AdminReportsActivity.class)));

        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        tvStatTotalUsers.setText(String.valueOf(dbHelper.getTotalUsers()));
        tvStatLostItems.setText(String.valueOf(dbHelper.getTotalLostItems()));
        tvStatPendingClaims.setText(String.valueOf(dbHelper.getPendingClaimsCount()));
        tvStatRecovered.setText(String.valueOf(dbHelper.getRecoveredItemsCount()));
    }
}
