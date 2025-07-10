package com.example.ticketmaster;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder> {

    private List<Bus> busList;
    private Context context;
    private boolean isAdmin;
    private ItemClickListener clickListener;

    public BusAdapter(List<Bus> busList, Context context, boolean isAdmin) {
        this.busList = busList;
        this.context = context;
        this.isAdmin = isAdmin;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new BusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus bus = busList.get(position);
        holder.busNameTextView.setText(bus.getBusName());
        holder.fromTextView.setText(bus.getFrom());
        holder.toTextView.setText(bus.getTo());
        holder.departureTimeTextView.setText(bus.getDepartureTime());
        holder.arrivalTimeTextView.setText(bus.getArrivalTime());
        holder.amountTextView.setText(bus.getAmount());
        holder.busNumberTextView.setText(bus.getBusNumberInput());
        holder.busConditionTextView.setText(bus.getBusConditionInput());
        // Set the date text based on whether it's a daily bus
        holder.dateTextView.setText(bus.isDaily() ? "Daily" : bus.getDate());

        // Hide or show the booking button based on whether it's an admin view or not
        if (isAdmin) {
            holder.bookingButton.setVisibility(View.GONE);
        } else {
            holder.bookingButton.setVisibility(View.VISIBLE);
            holder.bookingButton.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBookBusClick(bus);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return busList.size();
    }

    public static class BusViewHolder extends RecyclerView.ViewHolder {
        TextView busNameTextView, fromTextView, toTextView, departureTimeTextView, arrivalTimeTextView, dateTextView, amountTextView, busNumberTextView, busConditionTextView;
        Button bookingButton;

        public BusViewHolder(@NonNull View itemView) {
            super(itemView);
            busNameTextView = itemView.findViewById(R.id.busNameTextView);
            fromTextView = itemView.findViewById(R.id.fromTextView);
            toTextView = itemView.findViewById(R.id.toTextView);
            departureTimeTextView = itemView.findViewById(R.id.departureTimeTextView);
            arrivalTimeTextView = itemView.findViewById(R.id.arrivalTimeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            amountTextView = itemView.findViewById(R.id.travelsNameTextView);
            busNumberTextView = itemView.findViewById(R.id.busNumberTextView);
            busConditionTextView = itemView.findViewById(R.id.busConditionTextView);
            bookingButton = itemView.findViewById(R.id.bookingButton);
        }
    }
}
