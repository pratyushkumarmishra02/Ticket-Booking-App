package com.example.ticketmaster;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FinishActivity extends AppCompatActivity {
    private Button buttonHome;
    private TextView a, b, c;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final String CHANNEL_ID = "Booking_Notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Pay Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        a = findViewById(R.id.textView11);
        b = findViewById(R.id.textView21);
        c = findViewById(R.id.textView31);
        buttonHome = findViewById(R.id.btnHome);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            String busName = getIntent().getStringExtra("NAME_BUS");
            String busDate = getIntent().getStringExtra("DATE_BUS");
            String busCondition = getIntent().getStringExtra("CONDITION_BUS");

            a.setText(busName);
            b.setText(busDate);
            c.setText(busCondition);

            // Store booking details directly in FireStore
            storeBookingDetails(user.getDisplayName(), busName, busDate, busCondition);
        }

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Your Ticket Booking Success";
                sendNotification(message);
                startActivity(new Intent(getApplicationContext(), DetailActivity.class));

            }
        });

        createNotificationChannel();
    }

    private void storeBookingDetails(String userId, String busName, String busDate, String busCondition) {
        String bookingId = SharedData.getInstance().getBookingId();
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("BUS_NAME", busName);
        bookingDetails.put("DATE_BUS", busDate);
        bookingDetails.put("BUS_CONDITION", busCondition);
        bookingDetails.put("bookingId", bookingId);

        firestore.collection("Users").document(userId)
                .collection("Successfully BusBookings")
                .add(bookingDetails)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FinishActivity.this, "Successfully Booked", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FinishActivity", "Error saving booking details: " + e.getMessage());
                });
    }

    private void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.detail)
                .setContentTitle("New Notification")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Booking Notifications";
            String description = "Channel for Booking Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
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
            Toast.makeText(FinishActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
