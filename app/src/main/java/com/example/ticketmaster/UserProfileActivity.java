package com.example.ticketmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private TextView welcome, fullName, email, dob, gender, mobile;
    private ProgressBar pb;
    private ImageView imageview;
    private String fn, eml, DOB, sex, phNumber;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("PROFILE");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }


        welcome = findViewById(R.id.show_welcome);
        fullName = findViewById(R.id.show_full_name);
        email = findViewById(R.id.show_email);
        dob = findViewById(R.id.show_dob);
        gender = findViewById(R.id.show_gender);
        mobile = findViewById(R.id.show_mobile);
        pb = findViewById(R.id.progressbar);

        imageview = findViewById(R.id.image_view_profile);
        imageview.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, UploadImageActivity.class);
            startActivity(intent);
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(UserProfileActivity.this, "Something went wrong! User's details are not available at this moment", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            pb.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
    }

    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        firebaseUser.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!firebaseUser.isEmailVerified()) {
                    showAlertDialog();
                } else {
                    //Toast.makeText(UserProfileActivity.this, "Email Verified", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(UserProfileActivity.this, "Failed to reload user", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email. You will not be able to login without verification next time.");
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("users");
        referenceProfile.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    fn = firebaseUser.getDisplayName();
                    eml = firebaseUser.getEmail();
                    DOB = readUserDetails.Dob;
                    sex = readUserDetails.Gender;
                    phNumber = readUserDetails.ph_number;

                    welcome.setText("Welcome, " + fn + "!");
                    fullName.setText(fn);
                    email.setText(eml);
                    dob.setText(DOB);
                    gender.setText(sex);
                    mobile.setText(phNumber);

                    Uri uri = firebaseUser.getPhotoUrl();
                    if (uri != null) {
                        Picasso.with(UserProfileActivity.this).load(uri).into(imageview);

                        // Save the image URI in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profile_image", uri.toString());  // Save the image URI
                        editor.apply();  // Apply changes
                    }
                } else {
                    Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                pb.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_settings) {
            Toast.makeText(UserProfileActivity.this, "Setting Opened", Toast.LENGTH_LONG).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UserProfileActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UserProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
