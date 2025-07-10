package com.example.ticketmaster;

import android.view.View;

public interface ItemClickListener {
    void onClick(View view, int position);

    void onBookBusClick(Bus bus);
}

