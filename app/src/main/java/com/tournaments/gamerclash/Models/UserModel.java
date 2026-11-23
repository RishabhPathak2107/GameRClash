package com.tournaments.gamerclash.Models;

public class UserModel {
    private String email;
    private String fname;
    private String lname;
    private String phone;
    private String username;
    private double bonus;
    private double deposit;
    private double winnings;
    private long matches_played;
    private long kills;
    private long total_coins;

    // Firebase ke liye empty constructor zaruri hota hai
    public UserModel() {
    }

    public UserModel(String email, String fname, String lname, String phone, String username, long bonus, long deposit, long winnings, long matches_played, long kills, long total_coins) {
        this.email = email;
        this.fname = fname;
        this.lname = lname;
        this.phone = phone;
        this.username = username;
        this.bonus = bonus;
        this.deposit = deposit;
        this.winnings = winnings;
        this.matches_played = matches_played;
        this.kills = kills;
        this.total_coins = total_coins;

    }

    public long getKills() {
        return kills;
    }

    public void setKills(long kills) {
        this.kills = kills;
    }

    public long getMatches_played() {
        return matches_played;
    }

    public void setMatches_played(long matches_played) {
        this.matches_played = matches_played;
    }

    public long getTotal_coins() {
        return total_coins;
    }

    public void setTotal_coins(long total_coins) {
        this.total_coins = total_coins;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getPhone() {
        return phone;
    }

    public String getUsername() {
        return username;
    }


    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public double getWinnings() {
        return winnings;
    }

    public void setWinnings(double winnings) {
        this.winnings = winnings;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
