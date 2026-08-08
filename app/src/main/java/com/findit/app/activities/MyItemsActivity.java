package com.findit.app.activities;

import android.content.Intent;
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
import com.findit.app.database.FirestoreHelper;
import com.findit.app.models.Item;
import com.findit.app.utils.SessionManager;

import java.util.List;

public class MyItemsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvMyItems;
    private TextView tvEmptyState;

    private FirestoreHelper dbHelper;
    private SessionManager sessionManager;
    private ItemAdapter adapter;
    private List<Item> myItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items);

        dbHelper = new FirestoreHelper();
        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        rvMyItems = findViewById(R.id.rvMyItems);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        btnBack.setOnClickListener(v -> finish());

        rvMyItems.setLayoutManager(new LinearLayoutManager(this));

        loadMyItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyItems();
    }

    private void loadMyItems() {
        String userId = sessionManager.getUserId();
        dbHelper.getItemsByUserId(userId, new FirestoreHelper.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                myItems = items;
                if (myItems.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvMyItems.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvMyItems.setVisibility(View.VISIBLE);

                    adapter = new ItemAdapter(MyItemsActivity.this, myItems, item -> {
                        Intent intent = new Intent(MyItemsActivity.this, ItemDetailActivity.class);
                        intent.putExtra("ITEM_ID", item.getId());
                        startActivity(intent);
                    });

                    adapter.setActionListener("Mark as Recovered", item -> {
                        dbHelper.markItemAsRecovered(item.getId(), new FirestoreHelper.Callback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean success) {
                                if (success) {
                                    Toast.makeText(MyItemsActivity.this, "Item marked as RECOVERED!",
                                            Toast.LENGTH_SHORT).show();
                                    loadMyItems();
                                } else {
                                    Toast.makeText(MyItemsActivity.this, "Failed to update item status.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(MyItemsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    });

                    rvMyItems.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvEmptyState.setText("Failed to load items");
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
