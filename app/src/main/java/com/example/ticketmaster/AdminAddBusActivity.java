package com.example.ticketmaster;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminAddBusActivity extends AppCompatActivity {

    private EditText busNameInput, fromInput, toInput, departureTimeInput, arrivalTimeInput, amount, busNumberInput, busConditionInput, dateInput;
    private Button addBusButton;
    private RecyclerView busRecyclerView;

    private FirebaseFirestore firestore;
    private CollectionReference busesCollection;
    private BusAdapter busAdapter;
    private List<Bus> busList;
    private RadioGroup dailyBusRadioGroup;
    private RadioButton radioYes, radioNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_bus);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Add Buses");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Initialize input fields
        busNameInput = findViewById(R.id.busNameInput);
        fromInput = findViewById(R.id.fromInput);
        toInput = findViewById(R.id.toInput);
        departureTimeInput = findViewById(R.id.departureTimeInput);
        arrivalTimeInput = findViewById(R.id.arrivalTimeInput);
        amount = findViewById(R.id.travelsNameInput);
        busNumberInput = findViewById(R.id.busNumberInput);
        busConditionInput = findViewById(R.id.busConditionInput);
        dateInput = findViewById(R.id.dateInput);
        addBusButton = findViewById(R.id.addBusButton);

        busRecyclerView = findViewById(R.id.busRecyclerView);

        // Initialize RecyclerView
        busList = new ArrayList<>();
        busAdapter = new BusAdapter(busList, this, true);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        busRecyclerView.setAdapter(busAdapter);

        firestore = FirebaseFirestore.getInstance();
        busesCollection = firestore.collection("Buses");

        dateInput.setOnClickListener(v -> showDatePickerDialog());

        addBusButton.setOnClickListener(v -> addBus());
        dailyBusRadioGroup = findViewById(R.id.dailyBusRadioGroup);
        radioYes = findViewById(R.id.radioYes);
        radioNo = findViewById(R.id.radioNo);

        // Initially hide the date input if 'Yes' is selected
        dailyBusRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioYes) {
                dateInput.setVisibility(View.INVISIBLE);
            } else if (checkedId == R.id.radioNo) {
                dateInput.setVisibility(View.VISIBLE);
            }
        });

        fetchBuses();
    }

    private void showDatePickerDialog() {
        // Get current date for setting as default in the DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Open DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AdminAddBusActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dateInput.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }



    private void addBus() {
        String busName = busNameInput.getText().toString().trim();
        SharedData.getInstance().setBusName(busName);
        String from = fromInput.getText().toString().trim();
        String to = toInput.getText().toString().trim();
        String departureTime = departureTimeInput.getText().toString().trim();
        String arrivalTime = arrivalTimeInput.getText().toString().trim();
        String amt = amount.getText().toString().trim();
        String busNumber = busNumberInput.getText().toString().trim();
        String busCondition = busConditionInput.getText().toString().trim();
        boolean isBusDaily = radioYes.isChecked(); // true if 'Yes' is checked

        // Set date appropriately: if daily, set an empty string or "Daily"
        String date = isBusDaily ? "Daily" : dateInput.getText().toString().trim();

        if (TextUtils.isEmpty(busName) || TextUtils.isEmpty(from) || TextUtils.isEmpty(to) ||
                TextUtils.isEmpty(departureTime) || TextUtils.isEmpty(arrivalTime) ||
                TextUtils.isEmpty(amt) || TextUtils.isEmpty(busNumber) || TextUtils.isEmpty(busCondition) ||
                (!isBusDaily && TextUtils.isEmpty(date))) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new bus entry
        DocumentReference newBusRef = busesCollection.document(); // Generate a unique ID
        String busId = newBusRef.getId();


        // Create a Bus object with the provided details
        Bus newBus = new Bus(busId, busName, from, to, departureTime, arrivalTime, amt, busNumber, busCondition, date, isBusDaily);

        // Save to FireStore
        newBusRef.set(newBus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AdminAddBusActivity.this, "Bus added successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminAddBusActivity.this, LocationActivity.class);
                intent.putExtra("busId", busId);
                intent.putExtra("busName",busName);
                startActivity(intent);
                clearInputs();
                fetchBuses();
            } else {
                Toast.makeText(AdminAddBusActivity.this, "Failed to add bus", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void clearInputs() {
        busNameInput.setText("");
        fromInput.setText("");
        toInput.setText("");
        departureTimeInput.setText("");
        arrivalTimeInput.setText("");
        amount.setText("");
        busNumberInput.setText("");
        busConditionInput.setText("");
        dateInput.setText("");
        dailyBusRadioGroup.clearCheck();
    }

    private void fetchBuses() {
        // Fetch buses from FireStore
        busesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                busList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Bus bus = document.toObject(Bus.class);
                    if (bus != null) {
                        busList.add(bus);
                    }
                }
                busAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AdminAddBusActivity.this, "Failed to load buses", Toast.LENGTH_SHORT).show();
            }
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
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(AdminAddBusActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
