package com.tournaments.tuskesport.Models;

public class MatchModel {
    public String matchId;
    public String title;
    public String image;
    public String map;
    public String date;
    public String entryfee;
    public String perkill;
    public String prizepool;
    public String totalPrize;
    public String type;
    public String category;
    public String matchCategory;
    public String roomId;
    public String roomPass;
    public String tUser;

    public MatchModel() {}

    public MatchModel(String matchId, String title, String image, String map, String date,
                      String entryfee, String perkill, String prizepool, String totalPrize,
                      String type, String category, String matchCategory,
                      String roomId, String roomPass, String tUser) {
        this.matchId = matchId;
        this.title = title;
        this.image = image;
        this.map = map;
        this.date = date;
        this.entryfee = entryfee;
        this.perkill = perkill;
        this.prizepool = prizepool;
        this.totalPrize = totalPrize;
        this.type = type;
        this.category = category;
        this.matchCategory = matchCategory;
        this.roomId = roomId;
        this.roomPass = roomPass;
        this.tUser = tUser;
    }
}
