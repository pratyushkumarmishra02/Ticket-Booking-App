package com.example.ticketmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private Button button_bus, button_train, button_flight;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("TRANSPORT DASHBOARD");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the listener for navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(DashboardActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    return true;

                } else if (itemId == R.id.nav_tickets) {
                    Toast.makeText(DashboardActivity.this, "Booked Tickets opened", Toast.LENGTH_SHORT).show();
                    return true;

                } else {
                    return false;
                }
            }
        });

        // Check if the user is signed in
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        // Check if the user is a normal user
        checkUserRole(user.getUid());

        button_bus = findViewById(R.id.btn_book_bus);
        button_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkUserRole(String userId) {
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("user".equals(role)) {
                                // User is admin, check email verification status
                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                if (currentUser != null) {
                                    currentUser.reload().addOnCompleteListener(reloadTask -> {
                                        if (!currentUser.isEmailVerified()) {
                                            showEmailVerificationDialog(currentUser);
                                        } else {
                                            Toast.makeText(DashboardActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                redirectToAdminActivity();
                            }
                        } else {
                            Toast.makeText(DashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            redirectToAdminActivity();
                        }
                    } else {
                        Toast.makeText(DashboardActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmailVerificationDialog(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Email Not Verified")
                .setMessage("Your email address is not verified. Please verify your email to continue.")
                .setPositiveButton("Resend Email", (dialog, which) -> {
                    sendEmailVerification(user);
                })
                .setNegativeButton("Check Inbox", (dialog, which) -> {
                    Toast.makeText(DashboardActivity.this, "Please check your inbox for the verification email.", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }



    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DashboardActivity.this, "Verification email sent. Please check your inbox and verify your email.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToAdminActivity() {
        Intent intent = new Intent(DashboardActivity.this, AdminActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.only_refresh_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // Navigate back to the previous screen
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        // Refresh activity
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(DashboardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
