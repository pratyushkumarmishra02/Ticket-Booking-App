package com.example.ticketmaster;

public class BookingDetail {
    private String from;
    private String to;
    private String date;

    // Default constructor required for calls to DataSnapshot.getValue(BookingDetail.class)
    public BookingDetail() {}

    public BookingDetail(String from, String to, String date) {
        this.from = from;
        this.to = to;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDate() {
        return date;
    }
}
