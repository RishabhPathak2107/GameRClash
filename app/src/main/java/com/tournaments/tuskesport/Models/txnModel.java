package com.tournaments.tuskesport.Models;

public class txnModel {
    String date,reason,amount,status,txnId;
    public txnModel(){

    }
    public txnModel(String date, String reason, String amount, String txnId, String status) {
        this.date = date;
        this.reason = reason;
        this.amount = amount;
        this.txnId = txnId;
        this.status = status;
    }


    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getId() {
        return txnId;
    }

    public void setId(String txnId) {
        this.txnId = txnId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
