package com.example.ticketmaster;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {

    private EditText editTextCurrentPassword, editTextNewEmail;
    private Button buttonUpdateEmail;
    private ImageView eyeIcon;
    private boolean isPasswordVisible = false;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        // Initialize Firebase Authentication instance
        authProfile = FirebaseAuth.getInstance();

        // Initialize UI elements
        editTextCurrentPassword = findViewById(R.id.editText_current_password);
        editTextNewEmail = findViewById(R.id.editText_new_email);
        buttonUpdateEmail = findViewById(R.id.button_update_email);
        progressBar = findViewById(R.id.progress_bar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set the Toolbar as the ActionBar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Update Email");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = editTextCurrentPassword.getText().toString().trim();
                String newEmail = editTextNewEmail.getText().toString().trim();

                // Validate input
                if (TextUtils.isEmpty(currentPassword)) {
                    editTextCurrentPassword.setError("Current password is required");
                    editTextCurrentPassword.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    editTextNewEmail.setError("Please enter a valid email");
                    editTextNewEmail.requestFocus();
                } else {
                    // Re-authenticate user and update email
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();
                    if (firebaseUser != null) {
                        reauthenticateUser(firebaseUser, currentPassword, newEmail);
                    }
                }
            }
        });

        eyeIcon = findViewById(R.id.eyeIcon);
        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            editTextCurrentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.baseline_visibility_off_24);
        } else {
            // Show Password
            editTextCurrentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.baseline_visibility_24);
        }
        // Move cursor to the end of the text
        editTextCurrentPassword.setSelection(editTextCurrentPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }

    // Method to re-authenticate the user
    private void reauthenticateUser(FirebaseUser firebaseUser, String currentPassword, final String newEmail) {
        progressBar.setVisibility(View.VISIBLE);

        // Get credentials for re-authentication
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);

        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Now update the email
                    updateEmail(firebaseUser,newEmail,currentPassword);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UpdateEmailActivity.this, "Re-authentication failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Method to update the email of the current user
    private void updateEmail(FirebaseUser firebaseUser, String newEmail, String currentPassword) {
        // Get the user's current credentials (password in this case)
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);

        // Re-authenticate the user
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Re-authentication successful, now update the email
                firebaseUser.updateEmail(newEmail).addOnCompleteListener(emailUpdateTask -> {
                    if (emailUpdateTask.isSuccessful()) {
                        firebaseUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                            if (verificationTask.isSuccessful()) {
                                Toast.makeText(UpdateEmailActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                showAlertDialog();
                            } else {
                                Toast.makeText(UpdateEmailActivity.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        handleUpdateEmailError(emailUpdateTask);
                    }
                });
            } else {
                Toast.makeText(UpdateEmailActivity.this, "Re-authentication failed. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method to handle email update errors
    private void handleUpdateEmailError(@NonNull Task<Void> task) {
        try {
            throw task.getException();
        } catch (FirebaseAuthInvalidCredentialsException e) {
            Toast.makeText(UpdateEmailActivity.this, "Invalid email format.", Toast.LENGTH_LONG).show();
        } catch (FirebaseAuthUserCollisionException e) {
            Toast.makeText(UpdateEmailActivity.this, "This email is already in use by another account.", Toast.LENGTH_LONG).show();
        } catch (FirebaseNetworkException e) {
            Toast.makeText(UpdateEmailActivity.this, "Network error. Please try again later.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateEmailActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You cannot log in without email verification.");
        // Open the mail box by clicking the Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Creating Actionbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
            startActivity(intent);
            onBackPressed();
            return true;
        }
        // Refresh activity
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            // Use new API to override the transition if necessary
            overridePendingTransition(0, 0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Toast.makeText(UpdateEmailActivity.this, "Setting Opened", Toast.LENGTH_LONG).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UpdateEmailActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateEmailActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);
            // Check stack to prevent coming back to the UserProfileActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // close UserProfile
        } else {
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
