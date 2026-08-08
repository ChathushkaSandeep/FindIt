package com.findit.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.ItemAdapter;
import com.findit.app.database.FirestoreHelper;
import com.findit.app.models.Item;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchFilterActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etKeyword, etLocationFilter, etDateFilter;
    private Spinner spTypeFilter, spCategoryFilter, spSortBy;
    private Button btnReset, btnApplySearch;
    private TextView tvResultCount, tvEmptyState;
    private RecyclerView rvSearchResults;

    private FirestoreHelper dbHelper;
    private ItemAdapter adapter;
    private Calendar calendar = Calendar.getInstance();

    private final String[] typeOptions = { "ALL", "LOST", "FOUND" };
    private final String[] categoryOptions = {
            "All",
            "Electronics & Accessories",
            "Documents & IDs",
            "Clothing & Bags",
            "Keys & Locksets",
            "Jewelry & Watches",
            "Books & Stationery",
            "Others"
    };
    private final String[] sortOptions = { "Newest", "Oldest", "Name A-Z" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);

        dbHelper = new FirestoreHelper();

        btnBack = findViewById(R.id.btnBack);
        etKeyword = findViewById(R.id.etKeyword);
        etLocationFilter = findViewById(R.id.etLocationFilter);
        etDateFilter = findViewById(R.id.etDateFilter);
        spTypeFilter = findViewById(R.id.spTypeFilter);
        spCategoryFilter = findViewById(R.id.spCategoryFilter);
        spSortBy = findViewById(R.id.spSortBy);
        btnReset = findViewById(R.id.btnReset);
        btnApplySearch = findViewById(R.id.btnApplySearch);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        btnBack.setOnClickListener(v -> finish());

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // Spinners Setup
        spTypeFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeOptions));
        spCategoryFilter
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions));
        spSortBy.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions));

        // Date Picker for filter
        etDateFilter.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        etDateFilter.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // Real-time search as user types
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnApplySearch.setOnClickListener(v -> performSearch());
        btnReset.setOnClickListener(v -> resetFilters());

        performSearch();
    }

    private void performSearch() {
        String keyword = etKeyword.getText().toString().trim();
        String type = spTypeFilter.getSelectedItem().toString();
        String category = spCategoryFilter.getSelectedItem().toString();
        String location = etLocationFilter.getText().toString().trim();
        String date = etDateFilter.getText().toString().trim();
        String sortBy = spSortBy.getSelectedItem().toString();

        dbHelper.searchAndFilterItems(keyword, category, location, date, type, "VERIFIED", sortBy,
                new FirestoreHelper.Callback<List<Item>>() {
                    @Override
                    public void onSuccess(List<Item> results) {
                        tvResultCount.setText(results.size() + " Item(s) Found");

                        if (results.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvSearchResults.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvSearchResults.setVisibility(View.VISIBLE);

                            adapter = new ItemAdapter(SearchFilterActivity.this, results, item -> {
                                Intent intent = new Intent(SearchFilterActivity.this, ItemDetailActivity.class);
                                intent.putExtra("ITEM_ID", item.getId());
                                startActivity(intent);
                            });
                            rvSearchResults.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        tvResultCount.setText("Failed to search items");
                    }
                });
    }

    private void resetFilters() {
        etKeyword.setText("");
        etLocationFilter.setText("");
        etDateFilter.setText("");
        spTypeFilter.setSelection(0);
        spCategoryFilter.setSelection(0);
        spSortBy.setSelection(0);
        performSearch();
    }
}
