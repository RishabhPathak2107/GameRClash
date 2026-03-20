package com.tournaments.tuskesport.Models;

public class Result_model {
    String title, date, prizepool, perkill, entryfee, type, version, map, matchid;

    public Result_model() {
        // Required empty constructor for Firebase
    }

    public Result_model(String title, String date, String prizepool, String perkill, String entryfee, String type, String map, String matchId) {
        this.title = title;
        this.date = date;
        this.prizepool = prizepool;
        this.perkill = perkill;
        this.entryfee = entryfee;
        this.type = type;
        this.map = map;
        this.matchid = matchId; // ✅ Correct assignment
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getPrizepool() {
        return prizepool;
    }

    public String getPerkill() {
        return perkill;
    }

    public String getEntryfee() {
        return entryfee;
    }

    public String getType() {
        return type;
    }

    public String getMap() {
        return map;
    }

    public String getMatchId() {
        return matchid;
    }

    // Setters (optional, but helpful if you're updating data)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPrizepool(String prizepool) {
        this.prizepool = prizepool;
    }

    public void setPerkill(String perkill) {
        this.perkill = perkill;
    }

    public void setEntryfee(String entryfee) {
        this.entryfee = entryfee;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public void setMatchId(String matchId) {
        this.matchid = matchId;
    }
}
