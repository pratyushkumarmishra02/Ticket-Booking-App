package com.example.ticketmaster;

public class TicketDetails {
    private String bookingId;
    private String busName;
    private String fromLocation;
    private String toLocation;
    private String bookingDate;
    private String noOfSeats;
    private String ticketPrice;
    private String ticketSeats;
    private String busCondition;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    public TicketDetails() {}

    public TicketDetails(String bookingId,String busName, String fromLocation, String toLocation, String bookingDate, String noOfSeats, String ticketPrice, String ticketSeats, String busCondition,String customerName, String customerEmail, String customerPhone) {
        this.bookingId = bookingId;
        this.busName = busName;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.bookingDate = bookingDate;
        this.noOfSeats = noOfSeats;
        this.ticketPrice = ticketPrice;
        this.ticketSeats = ticketSeats;
        this.busCondition = busCondition;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;

    }

    // Getters and setters
    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getTicketNumber() {
        return noOfSeats;
    }

    public void setTicketNumber(String ticketNumber) {
        this.noOfSeats = ticketNumber;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getTicketSeats() {
        return ticketSeats;
    }

    public void setTicketSeats(String ticketSeats) {
        this.ticketSeats = ticketSeats;
    }

    public String getBusCondition()
    {
        return busCondition;
    }

    public void setBusCondition(String busCondition)
    {
        this.busCondition = busCondition;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getBookingId() {
        return bookingId;
    }
}
