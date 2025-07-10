package com.example.ticketmaster;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.ticketmaster.SharedData;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationActivity extends AppCompatActivity {

    private EditText latitudeInput, longitudeInput, placeInput;
    private Button fetchCoordinatesButton, updateLocationButton;

    private FirebaseFirestore firestore;
    private String busId;
    private String busName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        latitudeInput = findViewById(R.id.latitudeInput);
        longitudeInput = findViewById(R.id.longitudeInput);
        placeInput = findViewById(R.id.placeInput);
        fetchCoordinatesButton = findViewById(R.id.fetchCoordinatesButton);
        updateLocationButton = findViewById(R.id.updateLocationButton);

        firestore = FirebaseFirestore.getInstance();

        // Get busId from the AdminAddBusActivity.java
        busId = getIntent().getStringExtra("busId");
        //shared the busId
        SharedData.getInstance().setBusId(busId);

        busName = getIntent().getStringExtra("busName");
        SharedData.getInstance().setBusName(busName);

        fetchCoordinatesButton.setOnClickListener(v -> fetchCoordinates());

        updateLocationButton.setOnClickListener(v -> {
            String latitude = latitudeInput.getText().toString();
            String longitude = longitudeInput.getText().toString();

            if (!latitude.isEmpty() && !longitude.isEmpty()) {
                updateLocationInFirestore(latitude, longitude);
                Intent intent = new Intent(LocationActivity.this,AdminActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please fetch coordinates first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCoordinates() {
        String placeName = placeInput.getText().toString();
        if (placeName.isEmpty()) {
            Toast.makeText(this, "Please enter a place name", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(placeName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                latitudeInput.setText(String.valueOf(latitude));
                longitudeInput.setText(String.valueOf(longitude));
            } else {
                Toast.makeText(this, "Place not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching coordinates", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocationInFirestore(String latitude, String longitude) {
        String placeName = placeInput.getText().toString();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("place", placeName);
        locationData.put("Bus_Id",busId);
        locationData.put("busName",busName);

        // Use the busId to store the location data for this specific bus
        firestore.collection("Bus_locations").document(busId)
                .set(locationData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Location updated in Firestore", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show());
    }
}
