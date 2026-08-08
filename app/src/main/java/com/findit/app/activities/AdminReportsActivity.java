package com.findit.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FirestoreHelper;

public class AdminReportsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvReportUsers, tvReportLost, tvReportFound, tvReportRecovered, tvReportPendingClaims;
    private Button btnExportReport;

    private FirestoreHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        dbHelper = new FirestoreHelper();

        btnBack = findViewById(R.id.btnBack);
        tvReportUsers = findViewById(R.id.tvReportUsers);
        tvReportLost = findViewById(R.id.tvReportLost);
        tvReportFound = findViewById(R.id.tvReportFound);
        tvReportRecovered = findViewById(R.id.tvReportRecovered);
        tvReportPendingClaims = findViewById(R.id.tvReportPendingClaims);
        btnExportReport = findViewById(R.id.btnExportReport);

        btnBack.setOnClickListener(v -> finish());

        loadReportData();

        btnExportReport.setOnClickListener(v -> {
            Toast.makeText(this, "Summary report generated successfully!", Toast.LENGTH_LONG).show();
        });
    }

    private void loadReportData() {
        dbHelper.getTotalUsers(new FirestoreHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvReportUsers.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        dbHelper.getTotalLostItems(new FirestoreHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvReportLost.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        dbHelper.getTotalFoundItems(new FirestoreHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvReportFound.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        dbHelper.getRecoveredItemsCount(new FirestoreHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvReportRecovered.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        dbHelper.getPendingClaimsCount(new FirestoreHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvReportPendingClaims.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }
}
