package com.findit.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FindItDatabaseHelper;
import com.findit.app.models.Claim;
import com.findit.app.utils.SessionManager;

public class SubmitClaimActivity extends AppCompatActivity {

    private ImageView btnBack, ivProofImage;
    private TextView tvClaimItemTitle;
    private EditText etProofDescription;
    private View layoutUploadProofPlaceholder;
    private Button btnSubmitClaim;

    private FindItDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int itemId;
    private String itemTitle;
    private String proofImageUri = "";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        proofImageUri = imageUri.toString();
                        ivProofImage.setImageURI(imageUri);
                        ivProofImage.setVisibility(View.VISIBLE);
                        layoutUploadProofPlaceholder.setVisibility(View.GONE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_claim);

        dbHelper = new FindItDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        itemId = getIntent().getIntExtra("ITEM_ID", -1);
        itemTitle = getIntent().getStringExtra("ITEM_TITLE");

        btnBack = findViewById(R.id.btnBack);
        tvClaimItemTitle = findViewById(R.id.tvClaimItemTitle);
        etProofDescription = findViewById(R.id.etProofDescription);
        ivProofImage = findViewById(R.id.ivProofImage);
        layoutUploadProofPlaceholder = findViewById(R.id.layoutUploadProofPlaceholder);
        btnSubmitClaim = findViewById(R.id.btnSubmitClaim);

        btnBack.setOnClickListener(v -> finish());

        if (itemTitle != null) {
            tvClaimItemTitle.setText("Claiming: " + itemTitle);
        }

        View.OnClickListener pickImageListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        };
        layoutUploadProofPlaceholder.setOnClickListener(pickImageListener);
        ivProofImage.setOnClickListener(pickImageListener);

        btnSubmitClaim.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String proofText = etProofDescription.getText().toString().trim();

        if (proofText.isEmpty()) {
            Toast.makeText(this, "Please describe your ownership evidence.", Toast.LENGTH_SHORT).show();
            return;
        }

        Claim claim = new Claim();
        claim.setItemId(itemId);
        claim.setClaimantId(sessionManager.getUserId());
        claim.setProofDescription(proofText);
        claim.setProofImageUri(proofImageUri);
        claim.setStatus("PENDING");

        long id = dbHelper.submitClaim(claim);
        if (id > 0) {
            Toast.makeText(this, "Ownership claim submitted! Check status in 'My Claims'.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to submit claim.", Toast.LENGTH_SHORT).show();
        }
    }
}
