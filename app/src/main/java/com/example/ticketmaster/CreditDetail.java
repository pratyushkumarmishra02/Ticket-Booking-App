package com.example.ticketmaster;

public class CreditDetail {
    public String cardNumber;
    public String cardDate;
    public String cvvNumber;
    public String cardName;
    public String amount;

    public CreditDetail(String cardNumber, String cardDate, String cvvNumber, String cardName,String amount) {
        this.cardNumber = cardNumber;
        this.cardDate = cardDate;
        this.cvvNumber = cvvNumber;
        this.cardName = cardName;
        this.amount = amount;
    }

}
