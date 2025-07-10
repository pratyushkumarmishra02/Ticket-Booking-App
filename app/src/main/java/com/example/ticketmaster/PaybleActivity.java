package com.example.ticketmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PaybleActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private Button buttonPay;
    TextView totalCost;
    TextView totalSeat;
    private TextView a, b, c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payble);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize FireStore

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("You can pay");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        a = findViewById(R.id.textView11);
        b = findViewById(R.id.textView21);
        c = findViewById(R.id.textView31);

        totalCost = findViewById(R.id.totalCostFinal);
        totalSeat = findViewById(R.id.totalSeatsFinal);

        final String total = getIntent().getStringExtra("TOTALCOST");
        final String seats = getIntent().getStringExtra("TOTALSEAT");
        final String dateBus = getIntent().getStringExtra("DATE_BUS");
        final String nameBus = getIntent().getStringExtra("NAME_BUS");
        final String conditionBus = getIntent().getStringExtra("CONDITION_BUS");

        a.setText(nameBus);
        b.setText(dateBus);
        c.setText(conditionBus);

        totalCost.setText("Payable : Rs." + total);
        totalSeat.setText("Number Of Seats : " + seats);

        buttonPay = findViewById(R.id.btnPay);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prepare the data to be stored
                Map<String, Object> paymentData = new HashMap<>();
                paymentData.put("busName", nameBus);
                paymentData.put("busCondition", conditionBus);
                paymentData.put("totalCost", total);
                paymentData.put("totalSeats", seats);
                paymentData.put("userId", firebaseAuth.getCurrentUser().getUid());

                // Store the data in FireStore
                db.collection("Confirmation")
                        .add(paymentData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(PaybleActivity.this, "Payment details stored successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PaybleActivity.this, PayActivity.class);
                            intent.putExtra("TOTALCOST", total);
                            Log.d("TotalCost", "Total Cost being passed: " + total);
                            intent.putExtra("NAME_BUS", nameBus);
                            intent.putExtra("CONDITION_BUS", conditionBus);
                            intent.putExtra("DATE_BUS", dateBus);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PaybleActivity.this, "Error storing payment details", Toast.LENGTH_SHORT).show();
                        });
            }
        });
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
            Toast.makeText(PaybleActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
