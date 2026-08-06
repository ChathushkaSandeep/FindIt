package com.findit.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.findit.app.R;
import com.findit.app.fragments.FoundItemsFragment;
import com.findit.app.fragments.LostItemsFragment;
import com.findit.app.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserRoleBadge;
    private ImageView btnHeaderSearch, btnHeaderNotifications, btnHeaderProfile;
    private LinearLayout adminBanner;
    private Button btnOpenAdminPanel;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabReport;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        tvUserRoleBadge = findViewById(R.id.tvUserRoleBadge);
        btnHeaderSearch = findViewById(R.id.btnHeaderSearch);
        btnHeaderNotifications = findViewById(R.id.btnHeaderNotifications);
        btnHeaderProfile = findViewById(R.id.btnHeaderProfile);
        adminBanner = findViewById(R.id.adminBanner);
        btnOpenAdminPanel = findViewById(R.id.btnOpenAdminPanel);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabReport = findViewById(R.id.fabReport);

        setupRoleView();

        // ViewPager Adapter
        viewPager.setAdapter(new MainPagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Lost Items");
            } else {
                tab.setText("Found Items");
            }
        }).attach();

        // Header Listeners
        btnHeaderSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchFilterActivity.class)));
        btnHeaderNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnHeaderProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        if (btnOpenAdminPanel != null) {
            btnOpenAdminPanel.setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));
        }

        // Bottom Navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchFilterActivity.class));
                return true;
            } else if (itemId == R.id.nav_claims) {
                startActivity(new Intent(this, MyClaimsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // FAB Click listener (Bottom Sheet Choice)
        fabReport.setOnClickListener(v -> showReportChoiceDialog());
    }

    private void showReportChoiceDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_report_choice, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        View cardReportLost = bottomSheetView.findViewById(R.id.cardReportLost);
        View cardReportFound = bottomSheetView.findViewById(R.id.cardReportFound);

        cardReportLost.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, ReportItemActivity.class);
            intent.putExtra("REPORT_TYPE", "LOST");
            startActivity(intent);
        });

        cardReportFound.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, ReportItemActivity.class);
            intent.putExtra("REPORT_TYPE", "FOUND");
            startActivity(intent);
        });

        bottomSheetDialog.show();
    }

    private void setupRoleView() {
        if (sessionManager.isAdmin()) {
            tvUserRoleBadge.setText("ADMIN");
            tvUserRoleBadge.setBackgroundResource(R.color.accent);
            adminBanner.setVisibility(View.VISIBLE);
        } else {
            tvUserRoleBadge.setText("USER");
            adminBanner.setVisibility(View.GONE);
        }
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {

        public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new LostItemsFragment();
            } else {
                return new FoundItemsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
