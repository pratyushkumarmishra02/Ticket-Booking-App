package com.example.ticketmaster;

public class SharedData {
    private static SharedData instance;
    private String busId;
    private String bookingId;
    private String date;
    private String busName;

    private SharedData() {}

    // Get the singleton instance
    public static SharedData getInstance() {
        if (instance == null) {
            instance = new SharedData();
        }
        return instance;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getBusId() {
        return busId;
    }

    public void setBookingId(String bookingId){
        this.bookingId = bookingId;
    }
    public String getBookingId()
    {
        return bookingId;
    }

    public void setDate(String date)
    {
        this.date = date;
    }
    public String getDate()
    {
        return date;
    }

    public void setBusName(String busName)
    {
        this.busName = busName;
    }
    public String getBusName()
    {
        return busName;
    }
}
