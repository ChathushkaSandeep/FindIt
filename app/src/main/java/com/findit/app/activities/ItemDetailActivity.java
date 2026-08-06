package com.findit.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.Item;
import com.findit.app.utils.SessionManager;

public class ItemDetailActivity extends AppCompatActivity {

    private ImageView btnBack, ivDetailImage, ivDetailPlaceholder;
    private TextView tvDetailTypeBadge, tvDetailTitle, tvDetailCategory, tvDetailStatus;
    private TextView tvDetailLocation, tvDetailDate, tvDetailReporter, tvDetailDescription;
    private Button btnSubmitClaim, btnContactReporter;

    private FindItDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Item currentItem;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        dbHelper = new FindItDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        btnBack = findViewById(R.id.btnBack);
        ivDetailImage = findViewById(R.id.ivDetailImage);
        ivDetailPlaceholder = findViewById(R.id.ivDetailPlaceholder);
        tvDetailTypeBadge = findViewById(R.id.tvDetailTypeBadge);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailReporter = findViewById(R.id.tvDetailReporter);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        btnSubmitClaim = findViewById(R.id.btnSubmitClaim);
        btnContactReporter = findViewById(R.id.btnContactReporter);

        btnBack.setOnClickListener(v -> finish());

        loadItemDetails();
    }

    private void loadItemDetails() {
        if (itemId == -1) {
            Toast.makeText(this, "Invalid Item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentItem = dbHelper.getItemById(itemId);
        if (currentItem == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailTitle.setText(currentItem.getTitle());
        tvDetailCategory.setText(currentItem.getCategory());
        tvDetailLocation.setText(currentItem.getLocation());
        tvDetailDate.setText(currentItem.getDate());
        tvDetailDescription.setText(currentItem.getDescription());

        String reporterText = (currentItem.getUserName() != null ? currentItem.getUserName() : "User #" + currentItem.getUserId())
                + (currentItem.getUserPhone() != null && !currentItem.getUserPhone().isEmpty() ? " (" + currentItem.getUserPhone() + ")" : "");
        tvDetailReporter.setText(reporterText);

        tvDetailTypeBadge.setText(currentItem.getType());
        tvDetailStatus.setText(currentItem.getStatus());

        if (currentItem.getImageUri() != null && !currentItem.getImageUri().isEmpty()) {
            try {
                ivDetailImage.setImageURI(Uri.parse(currentItem.getImageUri()));
                ivDetailImage.setVisibility(View.VISIBLE);
                ivDetailPlaceholder.setVisibility(View.GONE);
            } catch (Exception e) {
                ivDetailImage.setVisibility(View.GONE);
                ivDetailPlaceholder.setVisibility(View.VISIBLE);
            }
        } else {
            ivDetailImage.setVisibility(View.GONE);
            ivDetailPlaceholder.setVisibility(View.VISIBLE);
        }

        // Show claim button only for FOUND items when current user is not the reporter
        if ("FOUND".equalsIgnoreCase(currentItem.getType()) && currentItem.getUserId() != sessionManager.getUserId()) {
            btnSubmitClaim.setVisibility(View.VISIBLE);
            btnSubmitClaim.setOnClickListener(v -> {
                Intent intent = new Intent(ItemDetailActivity.this, SubmitClaimActivity.class);
                intent.putExtra("ITEM_ID", currentItem.getId());
                intent.putExtra("ITEM_TITLE", currentItem.getTitle());
                startActivity(intent);
            });
        } else {
            btnSubmitClaim.setVisibility(View.GONE);
        }

        // Contact Reporter action (Dialer Intent)
        btnContactReporter.setOnClickListener(v -> {
            if (currentItem.getUserPhone() != null && !currentItem.getUserPhone().isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + currentItem.getUserPhone()));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "Phone number not available.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
