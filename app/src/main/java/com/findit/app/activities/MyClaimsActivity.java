package com.findit.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.findit.app.R;
import com.findit.app.adapters.ClaimAdapter;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.Claim;
import com.findit.app.utils.SessionManager;

import java.util.List;

public class MyClaimsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvClaims;
    private TextView tvEmptyClaims;

    private FindItDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ClaimAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_claims);

        dbHelper = new FindItDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        rvClaims = findViewById(R.id.rvClaims);
        tvEmptyClaims = findViewById(R.id.tvEmptyClaims);

        btnBack.setOnClickListener(v -> finish());

        rvClaims.setLayoutManager(new LinearLayoutManager(this));

        loadUserClaims();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserClaims();
    }

    private void loadUserClaims() {
        List<Claim> claims = dbHelper.getClaimsByUserId(sessionManager.getUserId());

        if (claims.isEmpty()) {
            tvEmptyClaims.setVisibility(View.VISIBLE);
            rvClaims.setVisibility(View.GONE);
        } else {
            tvEmptyClaims.setVisibility(View.GONE);
            rvClaims.setVisibility(View.VISIBLE);

            adapter = new ClaimAdapter(this, claims, false);
            rvClaims.setAdapter(adapter);
        }
    }
}
