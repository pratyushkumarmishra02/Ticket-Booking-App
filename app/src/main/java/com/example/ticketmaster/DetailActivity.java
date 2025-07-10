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

public class DetailActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private RecyclerView bookingRecyclerView;
    private BookingAdapter bookingAdapter;
    private List<TicketDetails> ticketDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Ticket Details");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        bookingRecyclerView = findViewById(R.id.busRecyclerView);
        bookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketDetailsList = new ArrayList<>();

        // Create a cancel listener for the BookingAdapter
        BookingAdapter.OnCancelBookingListener cancelListener = new BookingAdapter.OnCancelBookingListener() {
            @Override
            public void onCancelBooking(TicketDetails bookingDetail,int position) {
                deleteBooking(bookingDetail,position);
            }
        };

        // Initialize the BookingAdapter with the listener
        bookingAdapter = new BookingAdapter(ticketDetailsList, cancelListener,true);
        bookingRecyclerView.setAdapter(bookingAdapter);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userDisplayName = user.getDisplayName();
            fetchBookingHistory(userDisplayName);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchBookingHistory(String userDisplayName) {
        ticketDetailsList.clear();

        // Fetch from Successfully BusBookings collection for bus names
        firestore.collection("Users").document(userDisplayName)
                .collection("Successfully BusBookings")
                .get().addOnSuccessListener(userDisplaySnapshots -> {
                    if (!userDisplaySnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot nameDocument : userDisplaySnapshots) {
                            String busName = nameDocument.getString("BUS_NAME");
                            String bookingId = nameDocument.getString("bookingId");

                            // Fetch related details using a separate method
                            fetchRelatedBookingDetails(userDisplayName, bookingId, busName);
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, "No Bus Bookings Found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(DetailActivity.this, "Error fetching bus bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                                                                    TicketDetails ticketDetails = new TicketDetails(bookingId,busName, fromLocation, toLocation, bookingDate, noOfSeats, ticketPrice, ticketSeats, busCondition, customerName, customerEmail, customerPhone);

                                                                    // Add to list and notify the adapter
                                                                    ticketDetailsList.add(ticketDetails);
                                                                    bookingAdapter.notifyDataSetChanged();
                                                                    saveBookingDetails(ticketDetails, userDisplayName);
                                                                }
                                                            }
                                                        }).addOnFailureListener(e ->
                                                                Toast.makeText(DetailActivity.this, "Error fetching customer details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                            }
                                        }
                                    }).addOnFailureListener(e ->
                                            Toast.makeText(DetailActivity.this, "Error fetching seat details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }

                }).addOnFailureListener(e ->
                        Toast.makeText(DetailActivity.this, "Error fetching booking details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteBooking(TicketDetails bookingDetail,int position) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userDisplayName = user.getDisplayName();
            bookingDetail = ticketDetailsList.get(position);
            String bookingId = bookingDetail.getBookingId();

            firestore.collection("Users").document(userDisplayName)
                    .collection("Successfully BusBookings")
                    .whereEqualTo("bookingId", bookingId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            deleteRelatedData(userDisplayName, bookingId, position);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(DetailActivity.this, "Error deleting booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(DetailActivity.this, "No matching booking found to delete.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DetailActivity.this, "Error fetching booking for deletion: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteRelatedData(String userDisplayName, String bookingId, int position) {
        firestore.collection("Users").document(userDisplayName)
                .collection("Searching Details")
                .whereEqualTo("bookingId", bookingId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    deleteSeatDetails(userDisplayName, bookingId, position);
                });
    }

    private void deleteSeatDetails(String userDisplayName, String bookingId, int position) {
        firestore.collection("Users").document(userDisplayName)
                .collection("SeatDetails")
                .whereEqualTo("bookingId", bookingId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    deleteCustomerDetails(userDisplayName, bookingId, position);
                });
    }

    private void deleteCustomerDetails(String userDisplayName, String bookingId, int position) {
        firestore.collection("Users").document(userDisplayName)
                .collection("CustomerDetails")
                .whereEqualTo("bookingId", bookingId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }

                    // Finally, remove the booking from the list and notify the adapter
                    ticketDetailsList.remove(position);
                    bookingAdapter.notifyItemRemoved(position);
                    Toast.makeText(DetailActivity.this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveBookingDetails(TicketDetails ticketDetails, String userDisplayName) {
        // 1st check whether the booking id is available before
        firestore.collection("Users").document(userDisplayName)
                .collection("Booking Details")
                .whereEqualTo("busName", ticketDetails.getBusName())
                .whereEqualTo("bookingDate", ticketDetails.getBookingDate())
                .whereEqualTo("fromLocation", ticketDetails.getFromLocation())
                .whereEqualTo("toLocation", ticketDetails.getToLocation())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If booking doesn't exist, save the new booking details
                    if (queryDocumentSnapshots.isEmpty()) {
                        String bookingEntryId = firestore.collection("Users").document(userDisplayName)
                                .collection("Booking Details").document().getId();

                        // Create a Map to store the ticket details
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("busName", ticketDetails.getBusName());
                        bookingData.put("fromLocation", ticketDetails.getFromLocation());
                        bookingData.put("toLocation", ticketDetails.getToLocation());
                        bookingData.put("bookingDate", ticketDetails.getBookingDate());
                        bookingData.put("totalSeats", ticketDetails.getTicketNumber());
                        bookingData.put("totalCost", ticketDetails.getTicketPrice());
                        bookingData.put("selectedSeats", ticketDetails.getTicketSeats());
                        bookingData.put("busCondition", ticketDetails.getBusCondition());
                        bookingData.put("customerName", ticketDetails.getCustomerName());
                        bookingData.put("customerEmail", ticketDetails.getCustomerEmail());
                        bookingData.put("customerPhone", ticketDetails.getCustomerPhone());
                        bookingData.put("bookingId",ticketDetails.getBookingId());

                        firestore.collection("Users").document(userDisplayName)
                                .collection("Booking Details").document(bookingEntryId)
                                .set(bookingData)
                                .addOnSuccessListener(aVoid -> Log.d("DetailActivity", "Booking details saved successfully"))
                                .addOnFailureListener(e -> Log.e("DetailActivity", "Error saving booking details: " + e.getMessage()));
                    } else {
                        Log.d("DetailActivity", "Booking details already exist, skipping save.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DetailActivity", "Error checking booking details: " + e.getMessage()));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(DetailActivity.this,NavigationActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
