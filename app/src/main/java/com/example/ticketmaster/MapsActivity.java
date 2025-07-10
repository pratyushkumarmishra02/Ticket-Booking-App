package com.example.ticketmaster;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.ticketmaster.SharedData;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText placeInput;
    private Button searchLocationButton;
    private GoogleMap mMap;
    private FirebaseFirestore firestore;
    private static final LatLng DEFAULT_LOCATION = new LatLng(20.2961, 85.8245);
    private static final String DEFAULT_LOCATION_TITLE = "Bhubaneswar, Odisha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        placeInput = findViewById(R.id.placeInput);
        searchLocationButton = findViewById(R.id.searchLocationButton);
        firestore = FirebaseFirestore.getInstance();

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        searchLocationButton.setOnClickListener(v -> {
            String busName = placeInput.getText().toString();
            if (!busName.isEmpty()) {
                fetchBusLocation(busName);
            } else {
                Toast.makeText(this, "Please enter a bus name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBusLocation(String busName) {
        // Query FireStore to find the bus by name
        String busName1 = SharedData.getInstance().getBusName();
        firestore.collection("Bus_locations")
                .whereEqualTo("busName",busName1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean busFound = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String latitude = document.getString("latitude");
                            String longitude = document.getString("longitude");

                            // Update the map with the bus location
                            LatLng busLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(busLocation).title(busName));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 15));

                            busFound = true;
                            break; // Exit after finding the first matching bus
                        }
                        if (!busFound) {
                            Toast.makeText(this, "Bus not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching bus location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(DEFAULT_LOCATION).title(DEFAULT_LOCATION_TITLE));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 50));
    }
}
