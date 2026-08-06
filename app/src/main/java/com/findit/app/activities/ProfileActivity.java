package com.findit.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.User;
import com.findit.app.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etOldPassword, etNewPassword;
    private Button btnUpdateProfile, btnChangePassword, btnLogout;

    private FindItDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new FindItDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);

        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        loadUserData();

        btnUpdateProfile.setOnClickListener(v -> handleUpdateProfile());
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        currentUser = dbHelper.getUserById(userId);
        if (currentUser != null) {
            etProfileName.setText(currentUser.getFullName());
            etProfileEmail.setText(currentUser.getEmail());
            etProfilePhone.setText(currentUser.getPhone());
        }
    }

    private void handleUpdateProfile() {
        String name = etProfileName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateProfile(sessionManager.getUserId(), name, email, phone);
        if (success) {
            sessionManager.updateUserDetails(name, email, phone);
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleChangePassword() {
        String oldPwd = etOldPassword.getText().toString().trim();
        String newPwd = etNewPassword.getText().toString().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty()) {
            Toast.makeText(this, "Please enter current and new password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.changePassword(sessionManager.getUserId(), oldPwd, newPwd);
        if (success) {
            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
            etOldPassword.setText("");
            etNewPassword.setText("");
        } else {
            Toast.makeText(this, "Incorrect current password.", Toast.LENGTH_SHORT).show();
        }
    }
}
