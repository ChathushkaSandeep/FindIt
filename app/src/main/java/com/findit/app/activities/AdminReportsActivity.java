package com.findit.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FindItDatabaseHelper;

public class AdminReportsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvReportUsers, tvReportLost, tvReportFound, tvReportRecovered, tvReportPendingClaims;
    private Button btnExportReport;

    private FindItDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        dbHelper = new FindItDatabaseHelper(this);

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
        tvReportUsers.setText(String.valueOf(dbHelper.getTotalUsers()));
        tvReportLost.setText(String.valueOf(dbHelper.getTotalLostItems()));
        tvReportFound.setText(String.valueOf(dbHelper.getTotalFoundItems()));
        tvReportRecovered.setText(String.valueOf(dbHelper.getRecoveredItemsCount()));
        tvReportPendingClaims.setText(String.valueOf(dbHelper.getPendingClaimsCount()));
    }
}
