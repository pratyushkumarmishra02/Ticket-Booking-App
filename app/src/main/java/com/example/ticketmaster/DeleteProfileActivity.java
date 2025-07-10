package com.example.ticketmaster;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DeleteProfileActivity extends AppCompatActivity {

    private Button buttonDeleteProfile;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        // Initialize Firebase Auth
        authProfile = FirebaseAuth.getInstance();

        // Initialize UI components
        buttonDeleteProfile = findViewById(R.id.btn_delete_profile);
        progressBar = findViewById(R.id.progress_bar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set the Toolbar as the ActionBar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Delete Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Set button click listener to initiate delete profile process
        buttonDeleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    // Show confirmation dialog before deleting the profile
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action is irreversible.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProfile();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
    // Re-authenticate user before deleting profile
    private void reauthenticateUserAndDeleteProfile() {
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser != null) {
            String userEmail = firebaseUser.getEmail();
            String currentPassword = "user's_current_password"; // You should collect this from the user in a real app.

            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);

            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        deleteProfile(); // Call deleteProfile after successful re-authentication
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DeleteProfileActivity.this, "Re-authentication failed. Please check your password and try again.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(DeleteProfileActivity.this, "No user is currently logged in.", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteProfile() {
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser != null) {
            progressBar.setVisibility(View.VISIBLE);

            // Delete the user profile from Firebase Auth
            firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(DeleteProfileActivity.this, "Profile deleted successfully.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Close DeleteProfileActivity
                    } else {
                        Toast.makeText(DeleteProfileActivity.this, "Failed to delete profile. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No user is currently logged in.", Toast.LENGTH_SHORT).show();
        }
    }
    //Creating Actionbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        //It will back to the previous page
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            onBackPressed();
            return true;
        }
        //refresh activity
        if(id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        }
        else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
         else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            //Intent intent = new Intent(UserProfileActivity.this, SettingsActivity.class);
            //startActivity(intent);
            Toast.makeText(DeleteProfileActivity.this, "Setting Opened", Toast.LENGTH_LONG).show();
        }
        else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(DeleteProfileActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(DeleteProfileActivity.this, "Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
            //redirect to the MainActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close Userprofile
        }else{
            Toast.makeText(DeleteProfileActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
