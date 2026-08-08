package com.findit.app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.UserAdapter;
import com.findit.app.database.FirestoreHelper;
import com.findit.app.models.User;

import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvUsers;
    private FirestoreHelper dbHelper;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        dbHelper = new FirestoreHelper();

        btnBack = findViewById(R.id.btnBack);
        rvUsers = findViewById(R.id.rvUsers);

        btnBack.setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    private void loadUsers() {
        dbHelper.getAllUsers(new FirestoreHelper.Callback<List<User>>() {
            @Override
            public void onSuccess(List<User> userList) {
                adapter = new UserAdapter(ManageUsersActivity.this, userList, user -> {
                    String nextStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "SUSPENDED" : "ACTIVE";
                    dbHelper.updateUserStatus(user.getId(), nextStatus, new FirestoreHelper.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            if (success) {
                                Toast.makeText(ManageUsersActivity.this,
                                        "User " + user.getFullName() + " status updated to " + nextStatus,
                                        Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(ManageUsersActivity.this, "Failed to update user status.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ManageUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                });
                rvUsers.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageUsersActivity.this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
