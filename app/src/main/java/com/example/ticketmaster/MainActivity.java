package com.example.ticketmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ticketmaster.AdminActivity;
import com.example.ticketmaster.DashboardActivity;
import com.example.ticketmaster.ForgotPasswordActivity;
import com.example.ticketmaster.R;
import com.example.ticketmaster.registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ProgressBar pb1;
    private ImageView eyeIcon;
    private EditText Email, pwd;
    private FirebaseAuth authProfile;
    private FirebaseFirestore firestore;
    private static final String TAG = "LoginActivity";
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authProfile = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        pb1 = findViewById(R.id.progressbar);
        Email = findViewById(R.id.email);
        pwd = findViewById(R.id.password);

        Button forgot = findViewById(R.id.forget_password);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "You can reset your password now!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
            }
        });

        Button Login = findViewById(R.id.login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = Email.getText().toString();
                String textPwd = pwd.getText().toString();

                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    Email.setError("Email is required!");
                    Email.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(MainActivity.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    Email.setError("Valid email is required!");
                    Email.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(MainActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    pwd.setError("Password is required!");
                    pwd.requestFocus();
                } else {
                    pb1.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPwd);
                }
            }
        });

        // Signup Button
        TextView signupnow = findViewById(R.id.signupNow);
        signupnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, registration.class);
                startActivity(intent);
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
            pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.baseline_visibility_off_24);
        } else {
            // Show Password
            pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.baseline_visibility_24);
        }
        pwd.setSelection(pwd.length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void loginUser(String textEmail, String textPwd) {
        authProfile.signInWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();
                    if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                        checkUserRole(firebaseUser.getUid());
                    } else {
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                        }
                        authProfile.signOut();
                        showAlertDialog();
                    }
                } else {
                    handleLoginError(task);
                }
                pb1.setVisibility(View.GONE);
            }
        });
    }

    private void checkUserRole(String userId) {
        firestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String role = document.getString("role");
                        if ("admin".equals(role)) {
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        }
                        finish();
                    } else {
                        // Document does not exist, so create it with default values
                        //createUserProfileInFireStore(userId);
                        Toast.makeText(MainActivity.this,"Document does not exist",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error getting user role", task.getException());
                    Toast.makeText(MainActivity.this, "Failed to fetch user role", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*private void createUserProfileInFireStore(String userId) {
        Map<String, Object> user = new HashMap<>();
        user.put("role", "user");  // Default role

        firestore.collection("users").document(userId).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User profile created.");
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            } else {
                Log.e(TAG, "Error creating user profile", task.getException());
                Toast.makeText(MainActivity.this, "Failed to create user profile", Toast.LENGTH_LONG).show();
            }
        });
    }*/


    private void handleLoginError(@NonNull Task<AuthResult> task) {
        try {
            throw Objects.requireNonNull(task.getException());
        } catch (FirebaseAuthInvalidUserException e) {
            Email.setError("User doesn't exist. Please register.");
            Email.requestFocus();
        } catch (FirebaseAuthInvalidCredentialsException e) {
            pwd.setError("Invalid credentials. Please check and try again.");
            pwd.requestFocus();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email. You cannot log in without email verification.");
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

    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null) {
            checkUserRole(authProfile.getCurrentUser().getUid());
        } else {
            Toast.makeText(MainActivity.this, "You can log in now!", Toast.LENGTH_SHORT).show();
        }
    }
}
