package com.example.ticketmaster;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";

    private ImageView profileImageView;
    private TextView userNameTextView, userEmailTextView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    private Button btnManageRoutes, btnViewBookings, btnAddBus, btnDeleteBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Ensure this is the correct layout file

        // Initialize Firebase Auth and FireStore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupNavigationDrawer();



        // Initialize buttons
        btnManageRoutes = findViewById(R.id.btn_manage_routes);
        btnViewBookings = findViewById(R.id.btn_view_bookings);
        btnAddBus = findViewById(R.id.btn_add_bus);
        btnDeleteBus = findViewById(R.id.btn_delete_bus);

        btnAddBus.setOnClickListener(v -> openAddBus());
        btnDeleteBus.setOnClickListener(v -> openDeleteBus());

        btnViewBookings.setOnClickListener(v -> openViewBookings());



        // btnManageRoutes.setOnClickListener(v -> openManageRoutes());
        // btnViewBookings.setOnClickListener(v -> openViewBookings());

        // Verify if the current user is an admin
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            checkAdminStatus(currentUser.getUid());
        } else {
            redirectToLogin();
        }


    }
    private void openViewBookings() {
        Intent intent = new Intent(this, AdminViewBookingsActivity.class);
        startActivity(intent);
    }

    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.admin_drawer);
        // ActionBarDrawerToggle to manage the drawer toggle button
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView to handle menu items
        navigationView = findViewById(R.id.admin_navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                handleNavigationItemSelected(item);
                return true;
            }
        });
        View headerView = navigationView.getHeaderView(0);
        profileImageView = headerView.findViewById(R.id.nav_profile_image);
        userNameTextView = headerView.findViewById(R.id.textViewName);
        userEmailTextView  = headerView.findViewById(R.id.textViewEmailEmail);
        loadProfileImage();
    }

    private void loadProfileImage() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            Log.d(TAG, "UserId: " + userId);

            // Get a reference to the user's profile image in Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("DisplayPics").child(userId + ".jpg");

            // Load the image into the ImageView using Picasso
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Image URL: " + uri.toString());
                Picasso.with(this).load(uri).into(profileImageView);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load image: " + e.getMessage());
                Toast.makeText(AdminActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "User not authenticated");
        }



        //display the user's name and email
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            userNameTextView.setText(currentUser.getDisplayName());
        }
        String userEmail = currentUser.getEmail();
        if (userEmail != null) {
            userEmailTextView.setText(userEmail);
        }
    }

    // Method to check if the current user is an admin
    private void checkAdminStatus(String userId) {
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                // User is admin, check email verification status
                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                if (currentUser != null) {
                                    currentUser.reload().addOnCompleteListener(reloadTask -> {
                                        if (!currentUser.isEmailVerified()) {
                                            showEmailVerificationDialog(currentUser);
                                        } else {
                                            Toast.makeText(AdminActivity.this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                redirectToUserActivity();
                            }
                        } else {
                            Toast.makeText(AdminActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            redirectToUserActivity();
                        }
                    } else {
                        Toast.makeText(AdminActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminActivity.this, "Please check your inbox for the verification email.", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminActivity.this, "Verification email sent. Please check your inbox and verify your email.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AdminActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToUserActivity() {
        Intent intent = new Intent(AdminActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Map to associate menu item IDs with their corresponding activities
        Map<Integer, Class<?>> activityMap = new HashMap<>();
        activityMap.put(R.id.home, UserProfileActivity.class);
        activityMap.put(R.id.setting, SettingActivity.class);
        //activityMap.put(R.id.help, HelpActivity.class);
        activityMap.put(R.id.contact, ContactActivity.class);
        //activityMap.put(R.id.event, EventActivity.class);

        // Check for logout action
        if (id == R.id.logout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }

        // Get the corresponding activity class for the selected menu item
        Class<?> activityClass = activityMap.get(id);
        if (activityClass != null) {
            Intent intent = new Intent(AdminActivity.this, activityClass);
            startActivity(intent);
        }

        return true;
    }

    private void openAddBus() {
        Intent intent = new Intent(this, AdminAddBusActivity.class);
        startActivity(intent);
    }

    private void openDeleteBus() {
        Intent intent = new Intent(this, AdminDeleteBusActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }
}
