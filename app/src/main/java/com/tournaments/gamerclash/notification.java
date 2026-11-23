package com.tournaments.gamerclash;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.gamerclash.Adapters.NotificationAdapter;
import com.tournaments.gamerclash.Models.NotificationModel;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class notification extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<NotificationModel> list = new ArrayList<>();
    NotificationAdapter adapter;
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });

        toolbar = findViewById(R.id.toolbar);

        // Adjust for status bar
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, list); // Pass context
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseDatabase.getInstance().getReference().child("Updates")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String dateTime = snap.getKey();
                            String title = snap.child("title").getValue(String.class);
                            String detail = snap.child("detail").getValue(String.class);

                            if (title != null && detail != null && dateTime != null) {
                                list.add(new NotificationModel(title, dateTime, detail));
                            }
                        }
                        Collections.reverse(list); // latest first
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

}
