package com.example.ticketmaster;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.atomic.AtomicInteger;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatActivity extends AppCompatActivity {

    GridLayout mainGrid;
    Double seatPrice = 0.0;
    Double totalCost = 0.0;
    int totalSeats = 0;
    TextView totalPrice;
    TextView totalBookedSeats;
    private Button buttonBook;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private List<Integer> selectedSeats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Seat Selection");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        /*
        String displayName = user.getDisplayName();*/
        firestore = FirebaseFirestore.getInstance();

        mainGrid = findViewById(R.id.mainGrid);
        totalBookedSeats = findViewById(R.id.total_seats);
        totalPrice = findViewById(R.id.total_cost);
        buttonBook = findViewById(R.id.btnBook);

        // Get bus details from intent
        final String nameBus = getIntent().getStringExtra("NAME_BUS");
        final String conditionBus = getIntent().getStringExtra("CONDITION_BUS");
        final String amountBus = getIntent().getStringExtra("AMOUNT");
        final String idBus = getIntent().getStringExtra("BUS_ID");
        final String departureBus = getIntent().getStringExtra("DEPARTURE_TIME");
        final String dateBus = getIntent().getStringExtra("DATE_BUS");

        Log.d("SeatActivity", "BusName: " + nameBus + ", Condition: " + conditionBus + ", Amount: " + amountBus + ", BusID: " + idBus + ", DepartureTime: " + departureBus + " Date:" + dateBus);

        // Fetch the seat price for this specific bus from FireStore
        firestore.collection("Buses").document(idBus)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String amountString = documentSnapshot.getString("amount");
                        try {
                            seatPrice = Double.parseDouble(amountString);  // Convert String to Double
                        } catch (NumberFormatException e) {
                            Toast.makeText(SeatActivity.this, "Invalid seat price format", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firestore", "No matching bus found");
                        Toast.makeText(SeatActivity.this, "Bus details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(SeatActivity.this, "Failed to fetch seat price", Toast.LENGTH_SHORT).show());

        // Fetch only booked seats for this specific bus
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String currentDate = SharedData.getInstance().getDate();
        Log.d("SeatActivity", "Current User: " + (user != null ? user.getDisplayName() : "null"));
        Log.d("SeatActivity", "Current Bus ID: " + idBus + " | Current Date: " + currentDate);

        firestore.collection("Users").document(user.getDisplayName())
                .collection("SeatDetails")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        List<Integer> bookedSeats = new ArrayList<>();
                        Log.d("SeatActivity", "SeatDetails found for user: " + user.getDisplayName());

                        for (DocumentSnapshot seatDocument : querySnapshot.getDocuments()) {
                            String busIdInSeatDetails = seatDocument.getString("busId");
                            String dateInSeatDetails = seatDocument.getString("DATE_BUS");

                            Log.d("SeatActivity", "Checking seat document with bookingId: " + busIdInSeatDetails + " | Date: " + dateInSeatDetails);

                            if (busIdInSeatDetails != null && busIdInSeatDetails.equals(idBus)
                                    && dateInSeatDetails != null && dateInSeatDetails.equals(currentDate)) {

                                Log.d("SeatActivity", "Bus ID and Date match. Fetching selected seats.");

                                List<Long> selectedSeats = (List<Long>) seatDocument.get("selectedSeats");
                                if (selectedSeats != null) {
                                    for (Long seatNumber : selectedSeats) {
                                        Log.d("SeatActivity", "Adding booked seat number: " + seatNumber);
                                        bookedSeats.add(seatNumber.intValue());
                                    }
                                } else {
                                    Log.d("SeatActivity", "No selected seats found in this document.");
                                }
                            } else {
                                Log.d("SeatActivity", "Bus ID or Date doesn't match. Skipping document.");
                            }
                        }

                        markSeatsAsBooked(bookedSeats);
                        Log.d("SeatActivity", "Booked seats: " + bookedSeats.toString());

                    } else {
                        Log.d("SeatActivity", "No SeatDetails found for user: " + user.getDisplayName());
                        Toast.makeText(SeatActivity.this, "No previous bookings found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SeatActivity", "Failed to fetch previous bookings", e);
                    Toast.makeText(SeatActivity.this, "Failed to fetch previous bookings", Toast.LENGTH_SHORT).show();
                });




        // Set the event for selecting seats
        setToggleEvent(mainGrid);

        buttonBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String totalPriceI = totalPrice.getText().toString().trim();
                String totalBookedSeatsI = totalBookedSeats.getText().toString().trim();
                String bookingId = SharedData.getInstance().getBookingId();
                String date = SharedData.getInstance().getDate();
                Log.d("Debug", "Booking ID: " + bookingId);

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    String userId = user.getDisplayName();

                    // Check if the selected seats are already booked
                    firestore.collection("Users")
                            .document(userId)
                            .collection("SeatDetails")
                            .whereEqualTo("busId", idBus)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                boolean seatAlreadyBooked = false;

                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                    List<Long> alreadyBookedSeats = (List<Long>) documentSnapshot.get("selectedSeats");
                                    if (alreadyBookedSeats != null) {
                                        for (int seatNumber : selectedSeats) {
                                            if (alreadyBookedSeats.contains((long) seatNumber)) {
                                                seatAlreadyBooked = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (seatAlreadyBooked) break;
                                }

                                if (seatAlreadyBooked) {
                                    Toast.makeText(SeatActivity.this, "Seat is already booked", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Proceed with booking if no seat is already booked
                                    Map<String, Object> seatDetails = new HashMap<>();
                                    seatDetails.put("totalCost", totalPriceI);
                                    seatDetails.put("totalSeats", totalBookedSeatsI);
                                    seatDetails.put("busName", nameBus);
                                    seatDetails.put("busCondition", conditionBus);
                                    seatDetails.put("selectedSeats", selectedSeats);
                                    seatDetails.put("bookingId", bookingId);
                                    seatDetails.put("busId", idBus);
                                    seatDetails.put("DATE_BUS", date);

                                    firestore.collection("Users").document(userId)
                                            .collection("SeatDetails")
                                            .add(seatDetails)
                                            .addOnSuccessListener(documentReference -> {
                                                Intent intent = new Intent(SeatActivity.this, PaybleActivity.class);
                                                intent.putExtra("TOTALCOST", totalPriceI);
                                                intent.putExtra("TOTALSEAT", totalBookedSeatsI);
                                                intent.putExtra("NAME_BUS", nameBus);
                                                intent.putExtra("CONDITION_BUS", conditionBus);
                                                intent.putExtra("DATE_BUS", dateBus);
                                                startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SeatActivity.this, "Failed to save seat details", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SeatActivity.this, "Failed to check seat availability", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
        //fetchAndMarkBookedSeats();

    }

    /*private void fetchAndMarkBookedSeats() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String currentDate = SharedData.getInstance().getDate();
        String idBus = SharedData.getInstance().getBusId();

        Log.d("SeatActivity", "Current User: " + (user != null ? user.getDisplayName() : "null"));
        Log.d("SeatActivity", "Current Bus ID: " + idBus + " | Current Date: " + currentDate);

        List<Integer> bookedSeats = new ArrayList<>();

        // First, fetch booked seats from all users for the given bus
        firestore.collection("Users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("SeatActivity", "Fetched users: " + querySnapshot.size());

                    final int totalUsers = querySnapshot.size();
                    final AtomicInteger processedUsersCount = new AtomicInteger(0);

                    for (DocumentSnapshot userDocument : querySnapshot.getDocuments()) {
                        firestore.collection("Users")
                                .document(userDocument.getId())
                                .collection("SeatDetails")
                                .whereEqualTo("busId", idBus)
                                .get()
                                .addOnSuccessListener(seatSnapshot -> {
                                    for (DocumentSnapshot seatDocument : seatSnapshot.getDocuments()) {
                                        String dateInSeatDetails = seatDocument.getString("DATE_BUS");

                                        // Check if the date matches
                                        if (dateInSeatDetails != null && dateInSeatDetails.equals(currentDate)) {
                                            List<Long> selectedSeats = (List<Long>) seatDocument.get("selectedSeats");
                                            if (selectedSeats != null) {
                                                for (Long seatNumber : selectedSeats) {
                                                    Log.d("SeatActivity", "Adding booked seat number from all users: " + seatNumber);
                                                    bookedSeats.add(seatNumber.intValue());
                                                }
                                            } else {
                                                Log.d("SeatActivity", "No selected seats found in this document.");
                                            }
                                        } else {
                                            Log.d("SeatActivity", "Date doesn't match for user: " + userDocument.getId() + ". Skipping document.");
                                        }
                                    }

                                    // Increment the processed users count
                                    if (processedUsersCount.incrementAndGet() == totalUsers) {
                                        // Mark all booked seats after processing all users' seats
                                        markSeatsAsBooked(bookedSeats);
                                        Log.d("SeatActivity", "Total booked seats after processing all users: " + bookedSeats.toString());

                                        // Now fetch booked seats from the current user's SeatDetails
                                        if (user != null) {
                                            //fetchCurrentUserSeats(bookedSeats, user, currentDate, idBus);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("fetchAndMarkBookedSeats", "Failed to fetch booked seats for user: " + userDocument.getId(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e("fetchAndMarkBookedSeats", "Failed to fetch users", e));
    }

    private void fetchCurrentUserSeats(List<Integer> bookedSeats, FirebaseUser user, String currentDate, String idBus) {
        firestore.collection("Users").document(user.getDisplayName())
                .collection("SeatDetails")
                .get()
                .addOnSuccessListener(userSeatSnapshot -> {
                    if (!userSeatSnapshot.isEmpty()) {
                        Log.d("SeatActivity", "SeatDetails found for user: " + user.getDisplayName());
                        for (DocumentSnapshot seatDocument : userSeatSnapshot.getDocuments()) {
                            String busIdInSeatDetails = seatDocument.getString("busId");
                            String dateInSeatDetails = seatDocument.getString("DATE_BUS");

                            Log.d("SeatActivity", "Checking seat document with bookingId: " + busIdInSeatDetails + " | Date: " + dateInSeatDetails);
                            if (busIdInSeatDetails != null && busIdInSeatDetails.equals(idBus) && dateInSeatDetails != null && dateInSeatDetails.equals(currentDate)) {
                                Log.d("SeatActivity", "Bus ID and Date match. Fetching selected seats.");
                                List<Long> selectedSeats = (List<Long>) seatDocument.get("selectedSeats");
                                if (selectedSeats != null) {
                                    for (Long seatNumber : selectedSeats) {
                                        Log.d("SeatActivity", "Adding booked seat number from current user: " + seatNumber);
                                        bookedSeats.add(seatNumber.intValue());
                                    }
                                } else {
                                    Log.d("SeatActivity", "No selected seats found in this document.");
                                }
                            } else {
                                Log.d("SeatActivity", "Bus ID or Date doesn't match. Skipping document.");
                            }
                        }

                        // Mark booked seats again after processing the current user's seats
                        markSeatsAsBooked(bookedSeats);
                        Log.d("SeatActivity", "Booked seats after processing current user: " + bookedSeats.toString());
                    } else {
                        Log.d("SeatActivity", "No SeatDetails found for user: " + user.getDisplayName());
                        Toast.makeText(SeatActivity.this, "No previous bookings found for current user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SeatActivity", "Failed to fetch previous bookings for current user", e);
                    Toast.makeText(SeatActivity.this, "Failed to fetch previous bookings for current user", Toast.LENGTH_SHORT).show();
                });
    }*/





    // Method to mark seats as booked for a specific bus
    private void markSeatsAsBooked(List<Integer> bookedSeats) {
        Log.d("markSeatsAsBooked", "Booked seats list: " + bookedSeats);

        for (int seatNumber : bookedSeats) {
            Log.d("markSeatsAsBooked", "Processing seat number: " + seatNumber);
            if (seatNumber <= mainGrid.getChildCount()) {
                CardView seatCardView = (CardView) mainGrid.getChildAt(seatNumber - 1);

                if (seatCardView != null) {
                    seatCardView.setCardBackgroundColor(Color.RED);
                    seatCardView.setEnabled(false);
                    Log.d("markSeatsAsBooked", "Seat " + seatNumber + " marked as booked (red) and disabled.");
                } else {
                    Log.e("markSeatsAsBooked", "SeatCardView for seat number " + seatNumber + " is null.");
                }
            } else {
                Log.e("markSeatsAsBooked", "Seat number " + seatNumber + " is out of bounds (max seats: " + mainGrid.getChildCount() + ").");
            }
        }
    }


    // Method to toggle seat selection
    private void setToggleEvent(GridLayout mainGrid) {
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            final CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;

            // Only allow selection if the seat is not already booked (not red)
            if (cardView.getCardBackgroundColor().getDefaultColor() != Color.RED) {
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cardView.getCardBackgroundColor().getDefaultColor() == -1) {
                            cardView.setCardBackgroundColor(Color.GREEN);
                            totalCost += seatPrice;
                            ++totalSeats;
                            selectedSeats.add(finalI + 1);
                        } else {
                            cardView.setCardBackgroundColor(Color.WHITE);
                            totalCost -= seatPrice;
                            --totalSeats;
                            selectedSeats.remove((Integer) (finalI + 1));
                        }
                        totalPrice.setText(String.format("%.2f", totalCost));
                        totalBookedSeats.setText(String.valueOf(totalSeats));
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        }
        return super.onOptionsItemSelected(item);
    }
}
