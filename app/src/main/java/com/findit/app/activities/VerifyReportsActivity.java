package com.findit.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.ItemAdapter;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.Item;

import java.util.List;

public class VerifyReportsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvReports;
    private TextView tvEmptyReports;

    private FindItDatabaseHelper dbHelper;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_reports);

        dbHelper = new FindItDatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);
        rvReports = findViewById(R.id.rvReports);
        tvEmptyReports = findViewById(R.id.tvEmptyReports);

        btnBack.setOnClickListener(v -> finish());

        rvReports.setLayoutManager(new LinearLayoutManager(this));

        loadReports();
    }

    private void loadReports() {
        // Load all active items for moderation / verification
        List<Item> items = dbHelper.searchAndFilterItems("", "All", "", "", "ALL", "ALL", "Newest");

        if (items.isEmpty()) {
            tvEmptyReports.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
        } else {
            tvEmptyReports.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);

            adapter = new ItemAdapter(this, items, item -> {
                // Click action
            });

            adapter.setActionListener("Remove Fraudulent Report", item -> {
                boolean deleted = dbHelper.deleteItem(item.getId());
                if (deleted) {
                    Toast.makeText(this, "Fraudulent report removed!", Toast.LENGTH_SHORT).show();
                    loadReports();
                } else {
                    Toast.makeText(this, "Failed to remove report.", Toast.LENGTH_SHORT).show();
                }
            });

            rvReports.setAdapter(adapter);
        }
    }
}
