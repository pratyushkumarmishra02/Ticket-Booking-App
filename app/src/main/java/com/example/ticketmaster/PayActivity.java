package com.example.ticketmaster;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PayActivity extends AppCompatActivity {
    private Button buttonOffer, buttonCredit, buttonDebit, buttonNetBanking, buttonWallets;
    private TextView textViewTotal, a, b, c;
    private ImageView gPayImg, phonePayImg, paypalPayImg;
    private ActivityResultLauncher<Intent> gPayLauncher, phonePeLauncher;

    // Constants for Google Pay
    public static final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    public static final String PHONE_PAY_PACKAGE_NAME = "com.phonepe.app";
    int PHONE_PAY_REQUEST_CODE = 124;
    int GOOGLE_PAY_REQUEST_CODE = 123;
    String amount1, name = "Highbrow Director", upiId = "hashimads123@oksbi", transactionNote = "pay test", status;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Pay Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        buttonOffer = findViewById(R.id.buttonOffer);
        buttonCredit = findViewById(R.id.buttonCredit);
        buttonDebit = findViewById(R.id.buttonDebit);
        buttonNetBanking = findViewById(R.id.buttonNetBanking);
        buttonWallets = findViewById(R.id.buttonWallets);

        textViewTotal = findViewById(R.id.textViewTotal);
        a = findViewById(R.id.textView1);
        b = findViewById(R.id.textView2);
        c = findViewById(R.id.textView3);

        gPayImg = findViewById(R.id.iconGPay);
        phonePayImg = findViewById(R.id.iconPhonePe);
        paypalPayImg = findViewById(R.id.iconPayPal);

        // Get the bus and payment details from Intent
        final String nameBus = getIntent().getStringExtra("NAME_BUS");
        final String dateBus = getIntent().getStringExtra("DATE_BUS");
        final String conditionBus = getIntent().getStringExtra("CONDITION_BUS");

        a.setText(nameBus);
        b.setText(dateBus);
        c.setText(conditionBus);

        final  String total = getIntent().getStringExtra("TOTALCOST");
        textViewTotal.setText(total);

        // For Google Pay
        gPayImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount1 = textViewTotal.getText().toString();
                uri = getUpiPaymentUri(name, upiId, transactionNote, amount1);
                payWithGPay();
            }
        });

        // For launching PhonePe
        /*phonePeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String response = result.getData().getStringExtra("response");
                        if (response != null) {
                            Toast.makeText(this, "PhonePe response: " + response, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "No response from PhonePe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "PhonePe payment cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );*/

        phonePayImg.setOnClickListener(v -> {
            amount1 = textViewTotal.getText().toString();
            uri = getUpiPaymentUri(name, upiId, transactionNote, amount1);
            payWithPhonePay();
        });

        // PayPal integration
        paypalPayImg.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.paypal.android.p2pmobile");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(PayActivity.this, "PayPal app not found", Toast.LENGTH_SHORT).show();
            }
        });

        buttonOffer.setOnClickListener(view -> {
            Intent intent = new Intent(PayActivity.this, CreditActivity.class);
            intent.putExtra("NAME_BUS", nameBus);
            intent.putExtra("DATE_BUS", dateBus);
            intent.putExtra("CONDITION_BUS", conditionBus);
            intent.putExtra("TOTALCOST", total);
            startActivity(intent);
        });

        buttonCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PayActivity.this,CreditActivity.class);
                intent.putExtra("NAME_BUS",nameBus);
                intent.putExtra("DATE_BUS",dateBus);
                intent.putExtra("CONDITION_BUS",conditionBus);
                intent.putExtra("TOTALCOST", total);
                startActivity(intent);
            }
        });

        buttonDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PayActivity.this,CreditActivity.class);
                intent.putExtra("NAME_BUS",nameBus);
                intent.putExtra("DATE_BUS",dateBus);
                intent.putExtra("CONDITION_BUS",conditionBus);
                intent.putExtra("TOTALCOST", total);
                startActivity(intent);
            }
        });

        buttonNetBanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PayActivity.this,CreditActivity.class);
                intent.putExtra("NAME_BUS",nameBus);
                intent.putExtra("DATE_BUS",dateBus);
                intent.putExtra("CONDITION_BUS",conditionBus);
                intent.putExtra("TOTALCOST", total);
                startActivity(intent);
            }
        });

        buttonWallets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PayActivity.this,CreditActivity.class);
                intent.putExtra("NAME_BUS",nameBus);
                intent.putExtra("DATE_BUS",dateBus);
                intent.putExtra("CONDITION_BUS",conditionBus);
                intent.putExtra("TOTALCOST", total);
                startActivity(intent);
            }
        });

    }

    // For Google Pay UPI
    private void payWithGPay() {
        if (isAppInstalled(this, GOOGLE_PAY_PACKAGE_NAME)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
            startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE);
        } else {
            Toast.makeText(PayActivity.this, "Please Install GPay", Toast.LENGTH_SHORT).show();
        }
    }

    private void payWithPhonePay() {
        if (isAppInstalled(this, GOOGLE_PAY_PACKAGE_NAME)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
            startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE);
        } else {
            Toast.makeText(PayActivity.this, "Please Install GPay", Toast.LENGTH_SHORT).show();
        }
    }

    private static Uri getUpiPaymentUri(String name, String upiId, String transactionNote, String amount1) {
        return new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", transactionNote)
                .appendQueryParameter("am", amount1)
                .appendQueryParameter("cu", "INR")
                .build();
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.only_refresh_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(PayActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
