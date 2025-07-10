package com.example.ticketmaster;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextDOB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String fn,eml,DOB,sex,phNumber;
    private FirebaseAuth authProfile;
    private ProgressBar pb5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("UPDATE PROFILE");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        pb5 = findViewById(R.id.pb);

        editTextUpdateName = findViewById(R.id.editText_update_name);
        editTextDOB = findViewById(R.id.editText_update_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_mobile);
        radioGroupUpdateGender = findViewById(R.id.for_radio_button);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //show profile data
        showProfile(firebaseUser);

        //upload profile pic
        Button buttonProfilePic = findViewById(R.id.button_update_pic);
        buttonProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UploadImageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //update email
        Button buttonUpdateEmail = findViewById(R.id.button_update_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the DOB is not empty and is in a valid format (dd/MM/yyyy)
                if (DOB != null && !DOB.isEmpty() && DOB.contains("/")) {
                    try {
                        // Extracting the DOB from the saved date string
                        String[] textADoB = DOB.split("/");

                        // Parse day, month, and year from the DOB string
                        int day = Integer.parseInt(textADoB[0]);
                        int month = Integer.parseInt(textADoB[1]) - 1; // Month index starts from 0
                        int year = Integer.parseInt(textADoB[2]);

                        // Create and show the DatePickerDialog with the extracted date
                        DatePickerDialog picker = new DatePickerDialog(UpdateProfileActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @SuppressLint("DefaultLocale")
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        // Set the selected date in the EditText in dd/MM/yyyy format
                                        editTextDOB.setText(format("%d/%d/%d", dayOfMonth, month + 1, year));
                                    }
                                }, year, month, day);

                        picker.show();

                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        // Handle cases where the DOB string is improperly formatted or parsing fails
                        Toast.makeText(UpdateProfileActivity.this, "Invalid Date of Birth format!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If DOB is not set or invalid, use the current date as a fallback
                    final Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    int year = calendar.get(Calendar.YEAR);

                    // Create and show the DatePickerDialog with the current date
                    DatePickerDialog picker = new DatePickerDialog(UpdateProfileActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    // Set the selected date in the EditText in dd/MM/yyyy format
                                    editTextDOB.setText(format("%d/%d/%d", dayOfMonth, month + 1, year));
                                }
                            }, year, month, day);

                    picker.show();
                }
            }
        });


        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });


    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        String MobileRegex ="[6-9][0-9]{9}";//1st number must be 6,7,8,9 and rest 9 number can be anything
        Matcher mobileMatcher;
        Pattern mobilePattern =Pattern.compile(MobileRegex);
        mobileMatcher = mobilePattern.matcher(phNumber);


        if (TextUtils.isEmpty(fn)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full name is required");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(DOB)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your date of birth", Toast.LENGTH_LONG).show();
            editTextDOB.setError("Date of birth is required");
            editTextDOB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(phNumber)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Phone number is required");
            editTextUpdateMobile.requestFocus();
        } else if (phNumber.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Phone number should be 10 digits");
            editTextUpdateMobile.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Number is already registered or Invalid phone number☹️");
            editTextUpdateMobile.requestFocus();
        } else {
            //obtain the data entered by the user\
            fn = editTextUpdateName.getText().toString();
            DOB = editTextDOB.getText().toString();
            sex = radioButtonUpdateGenderSelected.getText().toString();
            phNumber = editTextUpdateMobile.getText().toString();
            //to store the details in firebase realtime DB (setup dependency)
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(DOB,sex,phNumber);

            //Extract the user details from the "Registered Users" reference
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();
            pb5.setVisibility(View.VISIBLE);
            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(fn).build();
                        firebaseUser.updateProfile(profileUpdates);
                        Toast.makeText(UpdateProfileActivity.this,"Update Successful!",Toast.LENGTH_LONG).show();

                        //to prevent the user
                    Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                    //check stack to prevent coming back to the UserprofileActivity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();//close UpdateProfileActivity
                }else{
                        try{
                            throw task.getException();
                        }catch (Exception e)
                        {
                            Toast.makeText(UpdateProfileActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                        pb5.setVisibility(View.GONE);
                }

            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);  // Inflate the menu items
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // Navigate back to the previous screen
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        //refresh activity
        if(id == R.id.menu_refresh) {
            startActivity(getIntent(), ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle());
            finish();

        }

        else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UpdateProfileActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);
            //check stack to prevent coming back to the UserprofileActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close Userprofile
        }else{
            Toast.makeText(UpdateProfileActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    //fetch data from firebase user
    private void showProfile(FirebaseUser firebaseUser) {
        String userIdOfRegistered = firebaseUser.getUid();
        //Extracting user reference for database "Registered users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        pb5.setVisibility(View.VISIBLE);

        referenceProfile.child(userIdOfRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readWriteUserDetails!=null)
                {
                    fn = firebaseUser.getDisplayName();
                    DOB = readWriteUserDetails.Dob;
                    sex = readWriteUserDetails.Gender;
                    phNumber = readWriteUserDetails.ph_number;

                    editTextUpdateName.setText(fn);
                    editTextDOB.setText(DOB);
                    editTextUpdateMobile.setText(phNumber);
                    //show Gender through radio button
                    if (sex != null) {
                        if (sex.equals("Male")) {
                            RadioButton radioMale = findViewById(R.id.radio_male);
                            if (radioMale != null) {
                                radioMale.setChecked(true);
                            }
                        } else if (sex.equals("Female")) {
                            RadioButton radioFemale = findViewById(R.id.radio_female);
                            if (radioFemale != null) {
                                radioFemale.setChecked(true);
                            }
                        }
                    } else {
                        // Handle case where gender is null or unexpected
                        Toast.makeText(UpdateProfileActivity.this, "Gender information is missing!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                pb5.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
                pb5.setVisibility(View.GONE);
            }
        });

    }
}