package com.tournaments.gamerclash.Models;

public class mainResultModel {
    String position;
    String username;
    String kills;
    String winnings;

    public mainResultModel() {
    }

    public mainResultModel(String position, String username, String kills, String winnings) {
        this.position = position;
        this.username = username;
        this.kills = kills;
        this.winnings = winnings;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKills() {
        return kills;
    }

    public void setKills(String kills) {
        this.kills = kills;
    }

    public String getWinnings() {
        return winnings;
    }

    public void setWinnings(String winnings) {
        this.winnings = winnings;
    }
}
