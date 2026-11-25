package com.tournaments.gamerclash;

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

import com.tournaments.gamerclash.Adapters.myAdapter;
import com.tournaments.gamerclash.Models.Match_model;

import java.util.*;

public class ongoing extends AppCompatActivity {

    RecyclerView recyclerView;
    myAdapter mAdapter;
    ArrayList<Match_model> ongoingMatches = new ArrayList<>();
    Set<String> joinedMatchIds = new HashSet<>();
    Toolbar toolbar;
    ImageView back_btn;

    DatabaseReference allMatchesRef, resultsRef, matchesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        toolbar = findViewById(R.id.toolbar);
        int statusBarHeight = getStatusBarHeight();
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchJoinedMatchIds();
    }

    private void fetchJoinedMatchIds() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference joinedRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("joinedMatchIds");

        joinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                joinedMatchIds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Boolean isJoined = ds.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isJoined)) {
                        joinedMatchIds.add(ds.getKey());
                    }
                }
                fetchOngoingMatches();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ongoing.this, "Failed to fetch joined match IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOngoingMatches() {
        allMatchesRef = FirebaseDatabase.getInstance().getReference("All_Matches");
        resultsRef = FirebaseDatabase.getInstance().getReference("Results");
        matchesRef = FirebaseDatabase.getInstance().getReference("Matches");

        resultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot resultsSnapshot) {
                matchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot matchesSnapshot) {
                        allMatchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot allMatchesSnapshot) {
                                ongoingMatches.clear();

                                for (String matchId : joinedMatchIds) {
                                    boolean isInResults = false;
                                    boolean isInMatches = false;

                                    // 🔍 Check if match is in Results
                                    for (DataSnapshot resultCategory : resultsSnapshot.getChildren()) {
                                        if (resultCategory.hasChild(matchId)) {
                                            isInResults = true;
                                            break;
                                        }
                                    }

                                    // 🔍 Check if match is in Matches
                                    for (DataSnapshot matchCategory : matchesSnapshot.getChildren()) {
                                        if (matchCategory.hasChild(matchId)) {
                                            Boolean value = matchCategory.child(matchId).getValue(Boolean.class);
                                            if (Boolean.TRUE.equals(value)) {
                                                isInMatches = true; // only count as "in Matches" if true
                                                break;
                                            }
                                        }

                                    }

                                    // ✅ Only add if NOT in Results or Matches, but exists in All_Matches
                                    if (!isInResults && !isInMatches && allMatchesSnapshot.hasChild(matchId)) {
                                        Match_model model = allMatchesSnapshot.child(matchId).getValue(Match_model.class);
                                        if (model != null) {
                                            model.setMatchid(matchId);
                                            ongoingMatches.add(model);
                                        }
                                    }
                                }

                                if (mAdapter == null) {
                                    mAdapter = new myAdapter(ongoingMatches, ongoing.this, joinedMatchIds, "Ongoing");
                                    recyclerView.setAdapter(mAdapter);
                                } else {
                                    mAdapter.notifyDataSetChanged();
                                }

                                if (ongoingMatches.isEmpty()) {
                                    Toast.makeText(ongoing.this, "No ongoing matches found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ongoing.this, "Failed to load All_Matches", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ongoing.this, "Failed to load Matches", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ongoing.this, "Failed to load Results", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0) ? getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
