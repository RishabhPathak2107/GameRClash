package com.tournaments.gamerclash.Models;

public class AddMoneyModel {
    String amount;

    public AddMoneyModel() {}

    public AddMoneyModel(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
