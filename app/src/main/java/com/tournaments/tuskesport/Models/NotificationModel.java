package com.tournaments.tuskesport.Models;

public class NotificationModel {
    String title, time, detail;

    public NotificationModel() {
    }

    public NotificationModel(String title, String time, String detail) {
        this.title = title;
        this.time = time;
        this.detail = detail;
    }

    public NotificationModel(String title, String time) {
        this.title = title;
        this.time = time;
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDetail() {
        return detail;
    }
}
