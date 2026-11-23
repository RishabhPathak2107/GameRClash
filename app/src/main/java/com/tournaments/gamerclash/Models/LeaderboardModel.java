package com.tournaments.gamerclash.Models;

public class LeaderboardModel {
    String fname, lname, username, profile;
    int total_coins;

    public LeaderboardModel() {

    }

    public LeaderboardModel(String fname, String lname, String username, String profile, int total_coins) {
        this.fname = fname;
        this.lname = lname;
        this.username = username;
        this.profile = profile;
        this.total_coins = total_coins;
    }

    public String getFname() { return fname; }
    public String getLname() { return lname; }
    public String getUsername() { return username; }
    public String getProfile() { return profile; }
    public int getTotal_coins() { return total_coins; }

    public String getName() {
        return fname + " " + lname;
    }
}
