package com.tournaments.gamerclash;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.gamerclash.Adapters.LeaderboardAdapter;
import com.tournaments.gamerclash.Models.LeaderboardModel;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class leaderboard extends AppCompatActivity {

    private RecyclerView leaderboardList;
    private ArrayList<LeaderboardModel> leaderboardData = new ArrayList<>();
    private LeaderboardAdapter adapter;
    Toolbar toolbar;
    ImageView back_btn;

    private TextView firstName, secondName, thirdName, firstScore, secondScore, thirdScore;
    private LinearLayout rank2Layout, rank3Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());

        toolbar = findViewById(R.id.toolbar);
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

        leaderboardList = findViewById(R.id.leaderboard_list);
        leaderboardList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(this, leaderboardData);
        leaderboardList.setAdapter(adapter);

        // Top 3 views
        firstName = findViewById(R.id.first_name);
        secondName = findViewById(R.id.second_name);
        thirdName = findViewById(R.id.third_name);
        firstScore = findViewById(R.id.first_score);
        secondScore = findViewById(R.id.second_score);
        thirdScore = findViewById(R.id.third_score);

        // Layouts to hide if needed
        rank2Layout = findViewById(R.id.rank2_layout);
        rank3Layout = findViewById(R.id.rank3_layout);

        loadLeaderboard();
    }
    private void loadLeaderboard() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");

        reference.orderByChild("total_coins").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaderboardData.clear();
                ArrayList<LeaderboardModel> tempList = new ArrayList<>();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    LeaderboardModel model = userSnap.getValue(LeaderboardModel.class);
                    if (model != null) {
                        tempList.add(model);
                    }
                }

                // limitToLast returns ascending order, reverse to get descending (highest coins first)
                Collections.reverse(tempList);

                // Top 1
                if (tempList.size() > 0) {
                    LeaderboardModel first = tempList.get(0);
                    firstName.setText(first.getName());
                    firstScore.setText(String.valueOf(first.getTotal_coins()));
                }

                // Top 2
                if (tempList.size() > 1) {
                    LeaderboardModel second = tempList.get(1);
                    secondName.setText(second.getName());
                    secondScore.setText(String.valueOf(second.getTotal_coins()));
                    rank2Layout.setVisibility(View.VISIBLE);
                } else {
                    rank2Layout.setVisibility(View.GONE);
                }

                // Top 3
                if (tempList.size() > 2) {
                    LeaderboardModel third = tempList.get(2);
                    thirdName.setText(third.getName());
                    thirdScore.setText(String.valueOf(third.getTotal_coins()));
                    rank3Layout.setVisibility(View.VISIBLE);
                } else {
                    rank3Layout.setVisibility(View.GONE);
                }

                // Add ranks 4 to 10
                if (tempList.size() > 3) {
                    int limit = Math.min(tempList.size(), 10);
                    leaderboardData.addAll(tempList.subList(3, limit));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle failure
            }
        });
    }

}
