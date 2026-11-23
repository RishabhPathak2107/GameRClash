package com.tournaments.gamerclash.Models;
public class WithdrawModel {
    String type;
    int amount;

    public WithdrawModel() {}

    public WithdrawModel(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}

