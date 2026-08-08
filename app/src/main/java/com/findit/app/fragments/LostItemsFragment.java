package com.findit.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.findit.app.R;
import com.findit.app.activities.ItemDetailActivity;
import com.findit.app.adapters.ItemAdapter;
import com.findit.app.database.FirestoreHelper;
import com.findit.app.models.Item;

import java.util.List;

public class LostItemsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvItems;
    private TextView tvEmptyState;

    private FirestoreHelper dbHelper;
    private ItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        dbHelper = new FirestoreHelper();

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvItems = view.findViewById(R.id.rvItems);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        swipeRefresh.setOnRefreshListener(this::loadLostItems);

        loadLostItems();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLostItems();
    }

    private void loadLostItems() {
        swipeRefresh.setRefreshing(true);
        dbHelper.getItemsByType("LOST", new FirestoreHelper.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> lostItems) {
                if (lostItems.isEmpty()) {
                    tvEmptyState.setText("No lost item reports currently available.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvItems.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvItems.setVisibility(View.VISIBLE);

                    adapter = new ItemAdapter(requireContext(), lostItems, item -> {
                        Intent intent = new Intent(requireContext(), ItemDetailActivity.class);
                        intent.putExtra("ITEM_ID", item.getId());
                        startActivity(intent);
                    });
                    rvItems.setAdapter(adapter);
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Exception e) {
                swipeRefresh.setRefreshing(false);
                tvEmptyState.setText("Error loading items.");
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
