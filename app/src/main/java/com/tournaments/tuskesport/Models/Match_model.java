package com.tournaments.tuskesport.Models;

public class Match_model {

    private String matchid;
    private String title;
    private String type;
    private String map;
    private String entryfee;
    private String date;
    private String prizepool;
    private String perkill;
    private String totalPrize;
    private  String cUser;
    private  String tUser;
    private String image;
    private String category;
    private String roomId;
    private String roomPass;

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Match_model() {
        // Required empty constructor for Firebase
    }

    public String getcUser() {
        return cUser;
    }

    public void setcUser(String cUser) {
        this.cUser = cUser;
    }

    public String gettUser() {
        return tUser;
    }

    public void settUser(String tUser) {
        this.tUser = tUser;
    }

    // Getter and Setter for matchid
    public String getMatchid() {
        return matchid;
    }


    public void setMatchid(String matchid) {
        this.matchid = matchid;
    }

    // Other getters and setters...

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getEntryfee() {
        return entryfee;
    }

    public void setEntryfee(String entryfee) {
        this.entryfee = entryfee;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrizepool() {
        return prizepool;
    }

    public void setPrizepool(String prizepool) {
        this.prizepool = prizepool;
    }

    public String getPerkill() {
        return perkill;
    }

    public void setPerkill(String perkill) {
        this.perkill = perkill;
    }

    public String getTotalPrize() {
        return totalPrize;
    }

    public void setTotalPrize(String totalPrize) {
        this.totalPrize = totalPrize;
    }

}
