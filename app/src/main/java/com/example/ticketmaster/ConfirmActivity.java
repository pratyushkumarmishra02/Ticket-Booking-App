package com.example.ticketmaster;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseFirestore firestore; // Firestore instance
    private FirebaseAuth firebaseAuth;
    private EditText emailId;
    private EditText phoneNumber;
    private EditText nameCustomer;
    private EditText ageCustomer;
    private ProgressDialog progressDialog;
    private Button confirmBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Contact Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        emailId = findViewById(R.id.editTextEmail);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        nameCustomer = findViewById(R.id.editTextName);
        ageCustomer = findViewById(R.id.editTextAge);
        confirmBook = findViewById(R.id.btnBook);

        progressDialog = new ProgressDialog(this);
        confirmBook.setOnClickListener(this);

        // Initialize FireStore
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void contactBook() {
        String cus_email = emailId.getText().toString().trim();
        String cus_phone = phoneNumber.getText().toString().trim();
        String cus_name = nameCustomer.getText().toString().trim();
        String cus_age = ageCustomer.getText().toString().trim();


        // Validate mobile number using matcher/pattern (RegEx)
        String MobileRegex = "[6-9][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(MobileRegex);
        mobileMatcher = mobilePattern.matcher(cus_phone);

        // Get bus details from the intent
        final String nameBus = getIntent().getStringExtra("NAME_BUS");
        final String dateBus = getIntent().getStringExtra("DATE_BUS");
        final String conditionBus = getIntent().getStringExtra("CONDITION_BUS");

        // Validate input
        if (TextUtils.isEmpty(cus_email)) {
            emailId.setError("Email is required");
            emailId.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(cus_email).matches()) {
            emailId.setError("Valid email is required");
            emailId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(cus_phone)) {
            phoneNumber.setError("Phone number is required");
            phoneNumber.requestFocus();
            return;
        } else if (cus_phone.length() != 10 || !mobileMatcher.find()) {
            phoneNumber.setError("Valid phone number is required");
            phoneNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(cus_name)) {
            nameCustomer.setError("Phone number is required");
            nameCustomer.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(cus_age)) {
            ageCustomer.setError("Phone number is required");
            ageCustomer.requestFocus();
            return;
        }

        String bookingId = SharedData.getInstance().getBookingId();

        Map<String, Object> customerDetail = new HashMap<>();
        customerDetail.put("cus_email", cus_email);
        customerDetail.put("cus_phone", cus_phone);
        customerDetail.put("cus_name", cus_name);
        customerDetail.put("cus_age", cus_age);
        customerDetail.put("bookingId", bookingId);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // Store customer details in FireStore
            firestore.collection("Users")
                    .document(Objects.requireNonNull(user.getDisplayName()))
                    .collection("CustomerDetails")
                    .add(customerDetail).addOnSuccessListener(documentReference -> {
                        progressDialog.setMessage("Saving Contact Details. Please Wait...");
                        progressDialog.show();


                        Intent intent = new Intent(ConfirmActivity.this, FinishActivity.class);
                        intent.putExtra("NAME_BUS", nameBus);
                        intent.putExtra("DATE_BUS", dateBus);
                        intent.putExtra("CONDITION_BUS", conditionBus);
                        startActivity(intent);

                        progressDialog.dismiss();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(ConfirmActivity.this, "Failed to save details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == confirmBook) {
            contactBook();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.only_refresh_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(ConfirmActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
