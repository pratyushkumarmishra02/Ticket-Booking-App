package com.example.ticketmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BusActivity extends AppCompatActivity implements ItemClickListener {

    private RecyclerView recyclerView;
    private BusAdapter adapter;
    private List<Bus> busList;
    private FirebaseFirestore firestore;
    private CollectionReference busesCollection;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Select your Bus");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        busList = new ArrayList<>();
        adapter = new BusAdapter(busList, BusActivity.this, false);
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        busesCollection = firestore.collection("Buses");

        String fromBus = getIntent().getStringExtra("FROM_BUS");
        String toBus = getIntent().getStringExtra("TO_BUS");
        String dateBus = getIntent().getStringExtra("DATE_BUS");

        // Fetch bus details based on fromBus, toBus, and dateBus
        fetchBusDetails(fromBus, toBus, dateBus);
    }

    private void fetchBusDetails(String fromBus, String toBus, String dateBus) {
        // Clear the existing bus list
        busList.clear();

        busesCollection.whereEqualTo("from", fromBus)
                .whereEqualTo("to", toBus)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Bus bus = document.toObject(Bus.class);
                                bus.setBusId(document.getId()); // Set FireStore document ID as the bus ID

                                // Check if the bus is daily or matches the selected date
                                if (bus.isDaily() || (bus.getDate() != null && bus.getDate().equals(dateBus))) {
                                    busList.add(bus);
                                }
                            }
                            if (!busList.isEmpty()) {
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(BusActivity.this, "No buses available for the selected date", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(BusActivity.this, "No buses available for the selected route", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BusActivity.this, "Failed to load buses", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onClick(View view, int position) {
        Bus bus = busList.get(position);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String busId = bus.getBusId();
            String busName = bus.getBusName();
            String departureTime = bus.getDepartureTime();
            String busConditionInput = bus.getBusConditionInput();
            String date = bus.getDate();
            String busNumberInput = bus.getBusNumberInput();
            String from = bus.getFrom();
            String to = bus.getTo();
            boolean isDaily = bus.isDaily();

            // Create a Bus object with all necessary details
            Bus busDetail = new Bus(busId, busName,  busNumberInput, departureTime, from, to, busConditionInput,  date, isDaily);

            // Store the selected bus under the current user in a subCollection called "SearchedBuses"
            firestore.collection("Users")
                    .document(Objects.requireNonNull(user.getDisplayName()))
                    .collection("SearchedBuses")
                    .document(busId)  // Store bus details with the bus ID as document ID
                    .set(busDetail)
                    .addOnSuccessListener(aVoid -> {
                        //Toast.makeText(getApplicationContext(), "Bus saved successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Failed to save bus details", Toast.LENGTH_SHORT).show();
                    });


        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBookBusClick(Bus bus) {
        // Logic to save bus details and navigate to SeatActivity
        Intent intent = new Intent(BusActivity.this, SeatActivity.class);
        intent.putExtra("NAME_BUS", bus.getBusName());
        intent.putExtra("DEPARTURE_TIME",bus.getDepartureTime());
        intent.putExtra("CONDITION_BUS", bus.getBusConditionInput());
        intent.putExtra("AMOUNT", bus.getAmount());
        intent.putExtra("BUS_ID",bus.getBusId());
        intent.putExtra("DATE_BUS",bus.getDate());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.only_refresh_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(BusActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}