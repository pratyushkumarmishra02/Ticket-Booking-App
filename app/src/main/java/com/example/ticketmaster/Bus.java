package com.example.ticketmaster;

import android.os.Parcel;
import android.os.Parcelable;

public class Bus implements Parcelable {
    private String busId;
    private String busName;
    private String from;
    private String to;
    private String departureTime;
    private String arrivalTime;
    private String amount;
    private String busNumberInput;
    private String busConditionInput;
    private String date;
    private boolean isBusDaily;

    public Bus() {
    }

    public Bus(String busId, String busName, String from, String to, String departureTime, String arrivalTime, String amount, String busNumberInput, String busConditionInput, String date, boolean isBusDaily) {
        this.busId = busId;
        this.busName = busName;
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.amount = amount;
        this.busNumberInput = busNumberInput;
        this.busConditionInput = busConditionInput;
        this.date = date;
        this.isBusDaily = isBusDaily;
    }

    public Bus(String busId, String busName, String busNumberInput, String departureTime, String from, String to, String busConditionInput, String date, boolean isBusDaily) {
        this.busId = busId;
        this.busName = busName;
        this.busNumberInput = busNumberInput;
        this.departureTime = departureTime;
        this.from = from;
        this.to = to;
        this.busConditionInput = busConditionInput;
        this.date = date;
        this.isBusDaily = isBusDaily;
    }

    // Getter methods
    public String getBusId() {
        return busId;
    }

    public String getBusName() {
        return busName;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getAmount() {
        return amount;
    }

    public String getBusNumberInput() {
        return busNumberInput;
    }

    public String getBusConditionInput() {
        return busConditionInput;
    }

    public String getDate() {
        return date;
    }

    public boolean isDaily() {
        return isBusDaily;
    }

    // Setter methods
    public void setBusId(String busId) {
        this.busId = busId;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setBusNumberInput(String busNumberInput) {
        this.busNumberInput = busNumberInput;
    }

    public void setBusConditionInput(String busConditionInput) {
        this.busConditionInput = busConditionInput;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDaily(boolean isBusDaily) {
        this.isBusDaily = isBusDaily;
    }

    // Parcelable implementation
    protected Bus(Parcel in) {
        busId = in.readString();
        busName = in.readString();
        from = in.readString();
        to = in.readString();
        departureTime = in.readString();
        arrivalTime = in.readString();
        amount = in.readString();
        busNumberInput = in.readString();
        busConditionInput = in.readString();
        date = in.readString();
        isBusDaily = in.readByte() != 0; // convert byte to boolean
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(busId);
        dest.writeString(busName);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(departureTime);
        dest.writeString(arrivalTime);
        dest.writeString(amount);
        dest.writeString(busNumberInput);
        dest.writeString(busConditionInput);
        dest.writeString(date);
        dest.writeByte((byte) (isBusDaily ? 1 : 0)); // convert boolean to byte
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Bus> CREATOR = new Creator<Bus>() {
        @Override
        public Bus createFromParcel(Parcel in) {
            return new Bus(in);
        }

        @Override
        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };
}
