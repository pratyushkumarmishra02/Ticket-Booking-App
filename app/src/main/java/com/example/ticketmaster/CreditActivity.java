package com.example.ticketmaster;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class CreditActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;
    private EditText editTextCardNumber;
    private EditText editTextDate;
    private EditText editTextCvvNumber;
    private EditText editTextCardName;
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Pay Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        editTextCardNumber = findViewById(R.id.textViewCardNumber);
        editTextDate = findViewById(R.id.textViewDate);
        editTextCvvNumber = findViewById(R.id.textViewCvvNumber);
        editTextCardName = findViewById(R.id.textViewCardName);
        buttonNext = findViewById(R.id.buttonPay);

        progressDialog = new ProgressDialog(this);
        buttonNext.setOnClickListener(this);
    }

    private void addCredit() {
        String cardNumber = editTextCardNumber.getText().toString().trim();
        String cardDate = editTextDate.getText().toString().trim();
        String cvvNumber = editTextCvvNumber.getText().toString().trim();
        String cardName = editTextCardName.getText().toString().trim();

        final String nameBus = getIntent().getStringExtra("NAME_BUS");
        final String dateBus = getIntent().getStringExtra("DATE_BUS");
        final String conditionBus = getIntent().getStringExtra("CONDITION_BUS");
        final String amount = getIntent().getStringExtra("TOTALCOST");
        Log.d("Intent", "Intent is: " + getIntent());
        Log.d("Amount","Amount is: "+amount);

        if (TextUtils.isEmpty(cardNumber)) {
            editTextCardNumber.setError("Please Enter The Card Number");
            editTextCardNumber.requestFocus();
            return;
        } else if (cardNumber.length() != 16) {
            editTextCardNumber.setError("Card Number must be 16 digits");
            editTextCardNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(cardDate)) {
            editTextDate.setError("Please Enter The Expiry Date");
            editTextDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(cvvNumber)) {
            editTextCvvNumber.setError("Please Enter The CVV Number");
            editTextCvvNumber.requestFocus();
            return;
        } else if (cvvNumber.length() != 3) {
            editTextCvvNumber.setError("CVV must be 3 digits");
            editTextCvvNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(cardName)) {
            editTextCardName.setError("Please Enter The Card Owner Name");
            editTextCardName.requestFocus();
            return;
        }

        // Create CreditDetail object
        CreditDetail creditDetail = new CreditDetail(cardNumber, cardDate, cvvNumber, cardName, amount);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        firestore.collection("Users").document(user.getDisplayName()).collection("PaymentDetails").add(creditDetail)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.setMessage("Making Payment Please Wait...");
                    progressDialog.show();

                    // Call method to show confirmation dialog
                    showConfirmationDialog(cardNumber, cardDate, cvvNumber, cardName, nameBus, dateBus, conditionBus, amount);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreditActivity.this, "Failed to save payment details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
    }

    // Method to show a confirmation dialog before redirecting to ConfirmActivity
    private void showConfirmationDialog(String cardNum, String expiry, String cvv, String name, String nameBus, String dateBus, String conditionBus, String amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreditActivity.this);
        builder.setTitle("Confirm Payment Details");
        builder.setMessage("Card Number: " + cardNum + "\n" +
                "Expiry Date: " + expiry + "\n" +
                "CVV: " + cvv + "\n" +
                "Name: " + name + "\n" +
                "Total Amount: " + amount);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Intent intent = new Intent(CreditActivity.this, ConfirmActivity.class);
            intent.putExtra("NAME_BUS", nameBus);
            intent.putExtra("DATE_BUS", dateBus);
            intent.putExtra("CONDITION_BUS", conditionBus);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        if (view == buttonNext) {
            addCredit();
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
            Toast.makeText(CreditActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}
