package com.tournaments.gamerclash.Models;

public class chatModel {
    private String senderName;
    private String message;

    // 🔹 Empty constructor required for Firebase
    public chatModel() {
    }

    // 🔹 Parameterized constructor
    public chatModel(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    // 🔹 Getter and Setter methods
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
