package com.tournaments.tuskesport;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.tuskesport.Adapters.mainResultAdapter;
import com.tournaments.tuskesport.Models.mainResultModel;

import java.util.ArrayList;

public class result extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<mainResultModel> list;
    mainResultAdapter adapter;

    TextView title, date, prizepool, perkill, entryfee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getSupportActionBar().hide();

        // Match Info Views
        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        prizepool = findViewById(R.id.prizepool);
        perkill = findViewById(R.id.perkill);
        entryfee = findViewById(R.id.entryfee);

        // Get Intent data
        String matchTitle = getIntent().getStringExtra("title");
        String matchDate = getIntent().getStringExtra("date");
        String matchPrize = getIntent().getStringExtra("prizepool");
        String matchPerKill = getIntent().getStringExtra("perkill");
        String matchEntry = getIntent().getStringExtra("entryfee");
        String matchId = getIntent().getStringExtra("matchId");

        // Set data in TextViews (handle nulls)
        title.setText(matchTitle != null ? matchTitle : "");
        date.setText(matchDate != null ? "Organized On " + matchDate : "");
        prizepool.setText(matchPrize != null ? matchPrize : "");
        perkill.setText(matchPerKill != null ? matchPerKill : "");
        entryfee.setText(matchEntry != null ? matchEntry : "");

        // RecyclerView setup
        recyclerView = findViewById(R.id.resultRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new mainResultAdapter(this, list);
        recyclerView.setAdapter(adapter);

        // Check if matchId is valid
        if (matchId == null || matchId.isEmpty()) {
            return; // Don't proceed if matchId is invalid
        }

        // Firebase data load
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("All_Matches")
                .child(matchId)
                .child("result");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged(); // Blank list
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    String position = child.getKey();

                    Object nameObj = child.child("username").getValue();
                    Object killsObj = child.child("kills").getValue();
                    Object winningsObj = child.child("winnings").getValue();

                    String username = nameObj != null ? String.valueOf(nameObj) : "";
                    String kills = killsObj != null ? String.valueOf(killsObj) : "0";
                    String winnings = winningsObj != null ? String.valueOf(winningsObj) : "0";

                    if (!username.isEmpty()) {
                        list.add(new mainResultModel(position, username, kills, winnings));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                list.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }
}
