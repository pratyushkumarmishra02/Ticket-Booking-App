package com.example.ticketmaster;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<TicketDetails> bookingDetails;
    private OnCancelBookingListener onCancelBookingListener;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private boolean showCancelButton;

    public BookingAdapter(List<TicketDetails> bookingDetails, OnCancelBookingListener listener,boolean showCancelButton) {
        this.bookingDetails = bookingDetails;
        this.onCancelBookingListener = listener;
        this.showCancelButton = showCancelButton;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_detail, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        TicketDetails bookingDetail = bookingDetails.get(position);

        // Bind ticket details to views
        holder.busName.setText(bookingDetail.getBusName());
        holder.fromLocation.setText("FROM       :" + " " + bookingDetail.getFromLocation());
        holder.toLocation.setText("TO       :" + " " + bookingDetail.getToLocation());
        holder.bookingDate.setText("DATE        :" + " " + bookingDetail.getBookingDate());
        holder.noOfSeats.setText("TOTAL NO OF SEATS         :" + " " + bookingDetail.getTicketNumber());
        holder.ticketPrice.setText("TOTAL PRICE         :" + " " + bookingDetail.getTicketPrice());
        holder.ticketSeats.setText("SEATS       :" + " " + bookingDetail.getTicketSeats());
        holder.busCondition.setText("BUS CONDITION          :" + " " + bookingDetail.getBusCondition());
        holder.customerName.setText("BOOKED BY       :" + " " + bookingDetail.getCustomerName());
        holder.customerEmail.setText("EMAIL         :" + " " + bookingDetail.getCustomerEmail());
        holder.customerPhone.setText("BOOKED PHONE NUMBER       :" + " " + bookingDetail.getCustomerPhone());

        if (showCancelButton) {
            holder.cancelBookingButton.setVisibility(View.VISIBLE);
            holder.cancelBookingButton.setOnClickListener(v -> {
                if (onCancelBookingListener != null) {
                    onCancelBookingListener.onCancelBooking(bookingDetail, position);
                }
            });
        } else {
            holder.cancelBookingButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookingDetails.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView busName, fromLocation, toLocation, bookingDate, noOfSeats, ticketPrice, ticketSeats, busCondition;
        TextView customerName, customerEmail, customerPhone;
        Button cancelBookingButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            busName = itemView.findViewById(R.id.busDetailName1);
            fromLocation = itemView.findViewById(R.id.bookingDetailFrom1);
            toLocation = itemView.findViewById(R.id.bookingDetailTo1);
            bookingDate = itemView.findViewById(R.id.bookingDetailDate1);
            noOfSeats = itemView.findViewById(R.id.ticketDetailNumber1);
            ticketPrice = itemView.findViewById(R.id.ticketDetailPrice1);
            ticketSeats = itemView.findViewById(R.id.ticketDetailSeats1);
            busCondition = itemView.findViewById(R.id.ticketDetailCondition1);
            customerName = itemView.findViewById(R.id.customerDetailName1);
            customerEmail = itemView.findViewById(R.id.customerDetailEmail1);
            customerPhone = itemView.findViewById(R.id.customerDetailPhone1);
            cancelBookingButton = itemView.findViewById(R.id.cancelBookingButton);
        }
    }

    public interface OnCancelBookingListener {
        void onCancelBooking(TicketDetails bookingDetail, int position);
    }

    // Method to remove item from the list after cancellation
    public void removeItem(int position) {
        bookingDetails.remove(position);
        notifyItemRemoved(position);
    }
}
