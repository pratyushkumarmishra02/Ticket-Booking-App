package com.example.ticketmaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminViewBookingsActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private RecyclerView bookingRecyclerView;
    private BookingAdapter bookingAdapter;
    private List<TicketDetails> ticketDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_bookings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Admin View Bookings");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        bookingRecyclerView = findViewById(R.id.busRecyclerView1);
        bookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketDetailsList = new ArrayList<>();

        // Initialize the BookingAdapter without cancel functionality for admin view
        bookingAdapter = new BookingAdapter(ticketDetailsList, null, false);
        bookingRecyclerView.setAdapter(bookingAdapter);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userDisplayName = user.getDisplayName();
            fetchAllBookingsForAdmin(userDisplayName);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAllBookingsForAdmin(String userDisplayName) {
        ticketDetailsList.clear();
        Log.d("AdminViewBookings", "Fetching bookings for admin: " + userDisplayName);
        firestore.collection("Users").document(userDisplayName)
                .collection("Successfully BusBookings")
                .get().addOnSuccessListener(bookingSnapshots -> {
                    Log.d("AdminViewBookings", "Fetched " + bookingSnapshots.size() + " bookings.");
                    for (QueryDocumentSnapshot bookingDoc : bookingSnapshots) {
                        String bookingId = bookingDoc.getString("bookingId");
                        String busName = bookingDoc.getString("BUS_NAME");
                        fetchRelatedBookingDetails(userDisplayName, bookingId, busName);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("AdminViewBookings", "Error fetching bookings: " + e.getMessage());
                    Toast.makeText(this, "Error fetching bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchRelatedBookingDetails(String userDisplayName, String bookingId, String busName) {
        // Fetch related searching details by bookingId
        firestore.collection("Users").document(userDisplayName)
                .collection("Searching Details")
                .whereEqualTo("bookingId", bookingId)
                .get().addOnSuccessListener(bookingSnapshots -> {
                    if (!bookingSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot bookingDocument : bookingSnapshots) {
                            String fromLocation = bookingDocument.getString("from");
                            String toLocation = bookingDocument.getString("to");
                            String bookingDate = bookingDocument.getString("date");

                            // Fetch seat details for this booking
                            firestore.collection("Users").document(userDisplayName)
                                    .collection("SeatDetails")
                                    .whereEqualTo("bookingId", bookingId)
                                    .get().addOnSuccessListener(seatSnapshots -> {
                                        if (!seatSnapshots.isEmpty()) {
                                            for (QueryDocumentSnapshot seatDocument : seatSnapshots) {
                                                String noOfSeats = seatDocument.getString("totalSeats");
                                                String ticketPrice = seatDocument.getString("totalCost");

                                                List<String> selectedSeatsList = (List<String>) seatDocument.get("selectedSeats");
                                                String ticketSeats = selectedSeatsList != null ? TextUtils.join(", ", selectedSeatsList) : "";
                                                String busCondition = seatDocument.getString("busCondition");

                                                // Fetch customer details for this booking
                                                firestore.collection("Users").document(userDisplayName)
                                                        .collection("CustomerDetails")
                                                        .whereEqualTo("bookingId", bookingId)
                                                        .get().addOnSuccessListener(customerSnapshots -> {
                                                            if (!customerSnapshots.isEmpty()) {
                                                                for (QueryDocumentSnapshot customerDocument : customerSnapshots) {
                                                                    String customerName = customerDocument.getString("cus_name");
                                                                    String customerEmail = customerDocument.getString("cus_email");
                                                                    String customerPhone = customerDocument.getString("cus_phone");

                                                                    // Combine all data into a single TicketDetails object
                                                                    TicketDetails ticketDetails = new TicketDetails(bookingId, busName, fromLocation, toLocation, bookingDate, noOfSeats, ticketPrice, ticketSeats, busCondition, customerName, customerEmail, customerPhone);

                                                                    // Add to list and notify the adapter
                                                                    ticketDetailsList.add(ticketDetails);
                                                                    bookingAdapter.notifyDataSetChanged();
                                                                }
                                                            }
                                                        }).addOnFailureListener(e ->
                                                                Toast.makeText(AdminViewBookingsActivity.this, "Error fetching customer details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                            }
                                        }
                                    }).addOnFailureListener(e ->
                                            Toast.makeText(AdminViewBookingsActivity.this, "Error fetching seat details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }

                }).addOnFailureListener(e ->
                        Toast.makeText(AdminViewBookingsActivity.this, "Error fetching booking details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(AdminViewBookingsActivity.this, AdminActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
