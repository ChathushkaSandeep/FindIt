package com.findit.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.NotificationAdapter;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.NotificationItem;
import com.findit.app.utils.SessionManager;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifs;

    private FindItDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbHelper = new FindItDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyNotifs = findViewById(R.id.tvEmptyNotifs);

        btnBack.setOnClickListener(v -> finish());

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();
    }

    private void loadNotifications() {
        List<NotificationItem> list = dbHelper.getUserNotifications(sessionManager.getUserId());

        if (list.isEmpty()) {
            tvEmptyNotifs.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmptyNotifs.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);

            adapter = new NotificationAdapter(this, list);
            rvNotifications.setAdapter(adapter);
        }
    }
}
