package com.findit.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword;
    private Button btnRegister;
    private TextView btnLoginRedirect;
    private FindItDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new FindItDatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLoginRedirect = findViewById(R.id.btnLoginRedirect);

        btnRegister.setOnClickListener(v -> handleRegistration());
        btnLoginRedirect.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setRole("USER");

        long id = dbHelper.registerUser(user);
        if (id > 0) {
            Toast.makeText(this, "Registration Successful! Please Sign In.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Registration Failed. Email may already be in use.", Toast.LENGTH_SHORT).show();
        }
    }
}
