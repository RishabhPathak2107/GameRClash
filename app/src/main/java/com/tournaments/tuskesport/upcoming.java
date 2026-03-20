package com.tournaments.tuskesport;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.tournaments.tuskesport.Adapters.myAdapter;
import com.tournaments.tuskesport.Models.Match_model;

import java.util.*;

public class upcoming extends AppCompatActivity {

    RecyclerView recyclerView;
    myAdapter adapter;
    ArrayList<Match_model> joinedUpcomingMatches;
    Set<String> joinedMatchIds = new HashSet<>();
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);
        toolbar = findViewById(R.id.toolbar);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        toolbar.setPadding(0, statusBarHeight, 0, 0);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar.setClipToPadding(false);
        recyclerView = findViewById(R.id.recyclerViewUpcoming);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        joinedUpcomingMatches = new ArrayList<>();
        loadJoinedMatches();
    }

    private void loadJoinedMatches() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference userJoinedMatchesRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("joinedMatchIds");

        userJoinedMatchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                joinedMatchIds.clear();

                if (snapshot.exists()) {
                    // Assuming joinedMatchIds stored as Map<String, Boolean>
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Boolean joined = child.getValue(Boolean.class);
                        if (Boolean.TRUE.equals(joined)) {
                            joinedMatchIds.add(child.getKey());
                        }
                    }
                }

                fetchAllMatches();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(upcoming.this, "Failed to load joined matches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllMatches() {
        joinedUpcomingMatches.clear();

        if (joinedMatchIds.isEmpty()) {
            Toast.makeText(this, "No joined matches found", Toast.LENGTH_SHORT).show();
            return;
        }

        final int total = joinedMatchIds.size();
        final int[] doneCount = {0};

        for (String matchId : joinedMatchIds) {
            checkMatchInAnyCategory(matchId, () -> {
                if (++doneCount[0] == total) updateAdapter();
            });
        }
    }

    private void checkMatchInAnyCategory(String matchId, Runnable onComplete) {
        DatabaseReference matchesRoot = FirebaseDatabase.getInstance().getReference("Matches");
        matchesRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean existsInCategory = false;
                String foundCategory = null;
                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    Boolean flag = categorySnap.child(matchId).getValue(Boolean.class);
                    if (Boolean.TRUE.equals(flag)) {
                        existsInCategory = true;
                        foundCategory = categorySnap.getKey();
                        break;
                    }
                }
                if (existsInCategory) {
                    fetchFromAllMatches(matchId, foundCategory, onComplete);
                } else onComplete.run();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

    private void fetchFromAllMatches(String matchId, String category, Runnable onComplete) {
        FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        Match_model match = snap.getValue(Match_model.class);
                        if (match != null) {
                            match.setMatchid(matchId);
                            match.setCategory(category);
                            String tUser = snap.child("tUser").getValue(String.class);
                            match.settUser(tUser != null ? tUser : "0");

                            FirebaseDatabase.getInstance().getReference("joinings")
                                    .child(category).child(matchId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override public void onDataChange(@NonNull DataSnapshot joinSnap) {
                                            match.setcUser(String.valueOf(joinSnap.getChildrenCount()));
                                            joinedUpcomingMatches.add(match);
                                            onComplete.run();
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError error) {
                                            match.setcUser("0");
                                            joinedUpcomingMatches.add(match);
                                            onComplete.run();
                                        }
                                    });
                        } else onComplete.run();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        onComplete.run();
                    }
                });
    }

    private void updateAdapter() {
        if (!joinedUpcomingMatches.isEmpty()) {
            adapter = new myAdapter(joinedUpcomingMatches, upcoming.this, joinedMatchIds, "All");
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No valid joined matches found", Toast.LENGTH_SHORT).show();
        }
    }


}
