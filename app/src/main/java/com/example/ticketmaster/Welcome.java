package com.example.ticketmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Welcome extends AppCompatActivity {
    private static final int SPLASH_DURATION = 1000; // 2 seconds
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is logged in
                if (mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid();
                    firestore.collection("users").document(userId).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String role = document.getString("role");
                                        // Check if the user is admin or a regular user
                                        Intent intent;
                                        if ("admin".equals(role)) {
                                            // Redirect to admin dashboard
                                            intent = new Intent(Welcome.this, AdminActivity.class);
                                        } else {
                                            // Redirect to user dashboard
                                            intent = new Intent(Welcome.this, DashboardActivity.class);
                                        }
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Welcome.this, "User details not found.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Welcome.this, MainActivity.class));
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(Welcome.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Welcome.this, MainActivity.class));
                                    finish();
                                }
                            });
                } else {
                    Intent intent = new Intent(Welcome.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_DURATION);
    }
}
