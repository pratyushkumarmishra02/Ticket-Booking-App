package com.example.ticketmaster;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "NavigationActivity";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private ImageView profileImageView;
    private TextView userNameTextView,userEmailTextView;

    private TextView mDisplayDate;
    private Spinner spinnerFrom, spinnerTo;
    private FirebaseFirestore firestore;
    private CollectionReference busesCollection;;
    private Button angry_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Search Buses");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }


        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        busesCollection = firestore.collection("Buses");

        // Fetch locations to populate spinners
        fetchLocations();


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Check if the user is signed in
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        setupNavigationDrawer();//for side navigation
        setupDatePicker();//for datePicker

        angry_btn = findViewById(R.id.angry_btn);
        angry_btn.setOnClickListener(this);
        loadProfileImage();
    }

    private void fetchLocations() {
        busesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> fromLocations = new ArrayList<>();
                List<String> toLocations = new ArrayList<>();

                // Add default "From" and "To" entries
                fromLocations.add("FROM");
                toLocations.add("TO");

                for (DocumentSnapshot document : task.getResult()) {
                    Bus bus = document.toObject(Bus.class); // Get Bus object
                    if (bus != null) {
                        // Add "from" and "to" locations if not already in the list
                        if (!fromLocations.contains(bus.getFrom())) {
                            fromLocations.add(bus.getFrom());
                        }
                        if (!toLocations.contains(bus.getTo())) {
                            toLocations.add(bus.getTo());
                        }
                    }
                }

                // Populate "From" spinner
                ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(NavigationActivity.this, android.R.layout.simple_spinner_dropdown_item, fromLocations);
                spinnerFrom.setAdapter(fromAdapter);

                // Populate "To" spinner
                ArrayAdapter<String> toAdapter = new ArrayAdapter<>(NavigationActivity.this, android.R.layout.simple_spinner_dropdown_item, toLocations);
                spinnerTo.setAdapter(toAdapter);
            } else {
                Toast.makeText(NavigationActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        // Access the header view of the navigation drawer to display profile info
        View headerView = navigationView.getHeaderView(0);
        profileImageView = headerView.findViewById(R.id.nav_profile_image);
        userNameTextView = headerView.findViewById(R.id.textViewName);
        userEmailTextView  = headerView.findViewById(R.id.textViewEmailEmail);
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
                Toast.makeText(NavigationActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "User not authenticated");
        }



        //display the user's name and email
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            if (userNameTextView != null && currentUser.getDisplayName() != null) {
                userNameTextView.setText(currentUser.getDisplayName());
            }
            String userEmail = currentUser.getEmail();
            if (userEmailTextView != null && userEmail != null) 
            { 
                userEmailTextView.setText(userEmail);
            }
        }
    }



    private void setupDatePicker() {
        mDisplayDate = findViewById(R.id.tvDate);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Date picker dialogue
                DatePickerDialog picker = new DatePickerDialog(NavigationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mDisplayDate.setText(String.format("%d/%d/%d", dayOfMonth, month + 1, year));
                    }
                }, year, month, day);
                picker.show();
            }
        });
    }

    private void searchBuses() {
        String from = spinnerFrom.getSelectedItem().toString().trim();
        String to = spinnerTo.getSelectedItem().toString().trim();
        String date = mDisplayDate.getText().toString().trim();
        SharedData.getInstance().setDate(date);
        String bookingId = UUID.randomUUID().toString();
        SharedData.getInstance().setBookingId(bookingId);

        Log.d(TAG, "User inputs - From: " + from + ", To: " + to + ", Date: " + date);

        if (TextUtils.equals(from, "FROM")) {
            Toast.makeText(this, "Please select departure place", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.equals(to, "TO")) {
            Toast.makeText(this, "Please select destination place", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please select journey date", Toast.LENGTH_SHORT).show();
            return;
        }



        Map<String, Object> bookingDetail = new HashMap<>();
        bookingDetail.put("from", from);
        bookingDetail.put("to", to);
        bookingDetail.put("date", date);
        bookingDetail.put("bookingId", bookingId);
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {

            // Store booking details under the user's profile in FireStore
            firestore.collection("Users")
                    .document(Objects.requireNonNull(user.getDisplayName()))
                    .collection("Searching Details")
                    .add(bookingDetail)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
                        fetchBusDetails(from, to, date); // Fetch bus details after saving the booking
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Failed to save booking details", Toast.LENGTH_SHORT).show();
                    });
        }


    }


    private void fetchBusDetails(String from, String to, String date) {
        List<Bus> busList = new ArrayList<>();

        // Query for buses that match 'from' and 'to' locations
        busesCollection.whereEqualTo("from", from)
                .whereEqualTo("to", to)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Query successful. Number of documents: " + task.getResult().size());
                        for (DocumentSnapshot document : task.getResult()) {
                            Bus bus = document.toObject(Bus.class);
                            Log.d(TAG, "Bus fetched: " + bus);
                            if (bus != null) {
                                // Log the daily and date status
                                Log.d(TAG, "Bus daily: " + bus.isDaily() + ", Bus date: " + bus.getDate());
                                // If a date is selected, include buses that are daily or match the specific date
                                if (bus.isDaily() || (bus.getDate() != null && bus.getDate().equals(date))) {
                                    busList.add(bus);
                                }
                            }
                        }

                        // Process the busList
                        if (busList.isEmpty()) {
                            Toast.makeText(NavigationActivity.this, "No buses found for the selected criteria.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(NavigationActivity.this, BusActivity.class);
                            intent.putExtra("BUS_LIST", (ArrayList<Bus>) busList);
                            intent.putExtra("FROM_BUS", from);
                            intent.putExtra("TO_BUS", to);
                            intent.putExtra("DATE_BUS", date);
                            startActivity(intent);
                        }
                    } else {
                        Log.e(TAG, "Error fetching bus details: " + task.getException());
                        Toast.makeText(NavigationActivity.this, "Error fetching bus details", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch bus details: " + e.getMessage());
                    Toast.makeText(NavigationActivity.this, "Failed to fetch bus details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(NavigationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Map to associate menu item IDs with their corresponding activities
        Map<Integer, Class<?>> activityMap = new HashMap<>();
        activityMap.put(R.id.home, UserProfileActivity.class);
        activityMap.put(R.id.location, MapsActivity.class);
        activityMap.put(R.id.setting, SettingActivity.class);
        //activityMap.put(R.id.help, HelpActivity.class);
        activityMap.put(R.id.detail, DetailActivity.class);
        activityMap.put(R.id.contact, ContactActivity.class);
       // activityMap.put(R.id.event, EventActivity.class);

        // Check for logout action
        if (id == R.id.logout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return true; // Early return to avoid starting logout intent
        }

        // Get the corresponding activity class for the selected menu item
        Class<?> activityClass = activityMap.get(id);
        if (activityClass != null) {
            Intent intent = new Intent(NavigationActivity.this, activityClass);
            startActivity(intent);
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == angry_btn) {
            searchBuses();
        }
    }
}