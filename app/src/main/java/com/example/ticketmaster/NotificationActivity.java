package com.example.ticketmaster;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationActivity extends AppCompatActivity {
    private TextView a, b, c, textView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        getSupportActionBar().setTitle("Notification Alert");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        a = findViewById(R.id.textView111);
        b = findViewById(R.id.textView222);
        c = findViewById(R.id.textView333);
        textView = findViewById(R.id.text_View);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // User is signed in, proceed to get data
            databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child(user.getUid()).child("BusBookingDetails");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Safely retrieve data
                        String busDetailName = dataSnapshot.child("travelsName").getValue(String.class);
                        String busDetailDate = dataSnapshot.child("date").getValue(String.class);
                        String busDetailCondition = dataSnapshot.child("busCondition").getValue(String.class);

                        // Set text safely
                        a.setText(busDetailName != null ? busDetailName : "N/A");
                        b.setText(busDetailDate != null ? busDetailDate : "N/A");
                        c.setText(busDetailCondition != null ? busDetailCondition : "N/A");
                    } else {
                        // Handle case where no booking details exist
                        a.setText("No booking details available");
                        b.setText("");
                        c.setText("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Toast.makeText(NotificationActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Optional: Close the activity if user is not logged in
        }

        // Get message from intent
        String message = getIntent().getStringExtra("message");
        textView.setText(message != null ? message : "No message received");
    }
}
