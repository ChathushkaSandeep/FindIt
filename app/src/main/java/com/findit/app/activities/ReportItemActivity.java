package com.findit.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.findit.app.R;
import com.findit.app.database.FirestoreHelper;
import com.findit.app.models.Item;
import com.findit.app.utils.CloudinaryHelper;
import com.findit.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportItemActivity extends AppCompatActivity {

    private ImageView btnBack, ivItemImage;
    private RadioGroup rgType;
    private RadioButton rbLost, rbFound;
    private EditText etTitle, etLocation, etDate, etDescription;
    private Spinner spCategory;
    private View layoutUploadPlaceholder;
    private Button btnSubmitReport;
    private TextView tvReportTitle;

    private FirestoreHelper dbHelper;
    private SessionManager sessionManager;
    private String selectedImageUri = "";
    private Calendar calendar = Calendar.getInstance();

    private final String[] categories = {
            "Electronics & Accessories",
            "Documents & IDs",
            "Clothing & Bags",
            "Keys & Locksets",
            "Jewelry & Watches",
            "Books & Stationery",
            "Others"
    };

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImageUri = imageUri.toString();
                        ivItemImage.setImageURI(imageUri);
                        ivItemImage.setVisibility(View.VISIBLE);
                        layoutUploadPlaceholder.setVisibility(View.GONE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_item);

        dbHelper = new FirestoreHelper();
        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        rgType = findViewById(R.id.rgType);
        rbLost = findViewById(R.id.rbLost);
        rbFound = findViewById(R.id.rbFound);
        etTitle = findViewById(R.id.etTitle);
        spCategory = findViewById(R.id.spCategory);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etDescription = findViewById(R.id.etDescription);
        ivItemImage = findViewById(R.id.ivItemImage);
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);

        btnBack.setOnClickListener(v -> finish());

        // Check if pre-set type from intent
        String initialType = getIntent().getStringExtra("REPORT_TYPE");
        if ("FOUND".equalsIgnoreCase(initialType)) {
            rbFound.setChecked(true);
            tvReportTitle.setText("Report Found Item");
        } else {
            rbLost.setChecked(true);
            tvReportTitle.setText("Report Lost Item");
        }

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFound) {
                tvReportTitle.setText("Report Found Item");
            } else {
                tvReportTitle.setText("Report Lost Item");
            }
        });

        // Setup Category Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                categories);
        spCategory.setAdapter(adapter);

        // Setup Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Setup Image Upload Click
        View.OnClickListener pickImageListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        };
        layoutUploadPlaceholder.setOnClickListener(pickImageListener);
        ivItemImage.setOnClickListener(pickImageListener);

        btnSubmitReport.setOnClickListener(v -> handleSubmitReport());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void handleSubmitReport() {
        String title = etTitle.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String location = etLocation.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String type = rbFound.isChecked() ? "FOUND" : "LOST";

        if (title.isEmpty() || location.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReport.setEnabled(false);
        Toast.makeText(this, "Submitting report...", Toast.LENGTH_SHORT).show();

        if (!selectedImageUri.isEmpty()) {
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
            CloudinaryHelper.uploadImage(this, Uri.parse(selectedImageUri),
                    new CloudinaryHelper.UploadCallbackListener() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            saveItemToFirestore(title, category, location, date, description, type, imageUrl);
                        }

                        @Override
                        public void onError(String error) {
                            btnSubmitReport.setEnabled(true);
                            Toast.makeText(ReportItemActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } else {
            saveItemToFirestore(title, category, location, date, description, type, "");
        }
    }

    private void saveItemToFirestore(String title, String category, String location, String date, String description,
            String type, String imageUrl) {
        Item item = new Item();
        item.setUserId(sessionManager.getUserId());
        item.setTitle(title);
        item.setCategory(category);
        item.setLocation(location);
        item.setDate(date);
        item.setDescription(description);
        item.setType(type);
        item.setStatus("VERIFIED"); // Verified & active
        item.setImageUri(imageUrl);

        dbHelper.insertItem(item, new FirestoreHelper.Callback<String>() {
            @Override
            public void onSuccess(String id) {
                btnSubmitReport.setEnabled(true);
                if (id != null) {
                    Toast.makeText(ReportItemActivity.this, type + " item reported successfully!", Toast.LENGTH_LONG)
                            .show();
                    finish();
                } else {
                    Toast.makeText(ReportItemActivity.this, "Failed to submit report. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                btnSubmitReport.setEnabled(true);
                Toast.makeText(ReportItemActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
