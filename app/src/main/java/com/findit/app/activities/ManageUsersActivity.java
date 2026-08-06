package com.findit.app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.UserAdapter;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.User;

import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvUsers;
    private FindItDatabaseHelper dbHelper;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        dbHelper = new FindItDatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);
        rvUsers = findViewById(R.id.rvUsers);

        btnBack.setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    private void loadUsers() {
        List<User> userList = dbHelper.getAllUsers();
        adapter = new UserAdapter(this, userList, user -> {
            String nextStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "SUSPENDED" : "ACTIVE";
            boolean success = dbHelper.updateUserStatus(user.getId(), nextStatus);
            if (success) {
                Toast.makeText(this, "User " + user.getFullName() + " status updated to " + nextStatus, Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, "Failed to update user status.", Toast.LENGTH_SHORT).show();
            }
        });
        rvUsers.setAdapter(adapter);
    }
}
