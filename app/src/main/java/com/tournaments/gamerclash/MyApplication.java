package com.tournaments.gamerclash;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {}
        OneSignal.initWithContext(this);
        OneSignal.setAppId("44d8c7c0-32a3-46d6-aaf5-81a162845d36");
    }
}
