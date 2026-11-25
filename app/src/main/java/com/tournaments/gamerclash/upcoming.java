package com.tournaments.gamerclash;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.tournaments.gamerclash.Adapters.myAdapter;
import com.tournaments.gamerclash.Models.Match_model;

import java.util.*;

public class upcoming extends AppCompatActivity {

    RecyclerView recyclerView;
    myAdapter adapter;
    ArrayList<Match_model> joinedUpcomingMatches;
    Set<String> joinedMatchIds = new HashSet<>();
    SwipeRefreshLayout swipeRefresh;
    TextView emptyView;
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);
        toolbar = findViewById(R.id.toolbar);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());
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

        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);

        swipeRefresh.setOnRefreshListener(() -> {
            loadJoinedMatches();
            swipeRefresh.setRefreshing(false);
        });

        joinedUpcomingMatches = new ArrayList<>();
        loadJoinedMatches();
    }

    private void loadJoinedMatches() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            emptyView.setVisibility(View.VISIBLE);
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
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Boolean joined = child.getValue(Boolean.class);
                        if (Boolean.TRUE.equals(joined)) {
                            joinedMatchIds.add(child.getKey());
                        }
                    }
                }

                if (joinedMatchIds.isEmpty()) {
                    joinedUpcomingMatches.clear();
                    updateAdapter();
                } else {
                    fetchAllMatches();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchAllMatches() {
        joinedUpcomingMatches.clear();

        if (joinedMatchIds.isEmpty()) {
            updateAdapter();
            return;
        }

        DatabaseReference matchesRoot = FirebaseDatabase.getInstance().getReference("Matches");
        matchesRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot matchesSnapshot) {
                List<Match_model> tempList = Collections.synchronizedList(new ArrayList<>());
                final int total = joinedMatchIds.size();
                final int[] doneCount = {0};

                for (String matchId : joinedMatchIds) {
                    String foundCategory = null;
                    for (DataSnapshot categorySnap : matchesSnapshot.getChildren()) {
                        Boolean flag = categorySnap.child(matchId).getValue(Boolean.class);
                        if (Boolean.TRUE.equals(flag)) {
                            foundCategory = categorySnap.getKey();
                            break;
                        }
                    }

                    if (foundCategory != null) {
                        String category = foundCategory;
                        FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snap) {
                                        Match_model match = snap.getValue(Match_model.class);
                                        if (match != null) {
                                            match.setMatchid(matchId);
                                            match.setCategory(category);
                                            String tUser = snap.child("tUser").getValue(String.class);
                                            match.settUser(tUser != null ? tUser : "0");

                                            FirebaseDatabase.getInstance().getReference("joinings")
                                                    .child(matchId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot joinSnap) {
                                                            match.setcUser(String.valueOf(joinSnap.getChildrenCount()));
                                                            tempList.add(match);
                                                            if (++doneCount[0] == total) {
                                                                joinedUpcomingMatches.clear();
                                                                joinedUpcomingMatches.addAll(tempList);
                                                                updateAdapter();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            match.setcUser("0");
                                                            tempList.add(match);
                                                            if (++doneCount[0] == total) {
                                                                joinedUpcomingMatches.clear();
                                                                joinedUpcomingMatches.addAll(tempList);
                                                                updateAdapter();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            if (++doneCount[0] == total) {
                                                joinedUpcomingMatches.clear();
                                                joinedUpcomingMatches.addAll(tempList);
                                                updateAdapter();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        if (++doneCount[0] == total) {
                                            joinedUpcomingMatches.clear();
                                            joinedUpcomingMatches.addAll(tempList);
                                            updateAdapter();
                                        }
                                    }
                                });
                    } else {
                        if (++doneCount[0] == total) {
                            joinedUpcomingMatches.clear();
                            joinedUpcomingMatches.addAll(tempList);
                            updateAdapter();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateAdapter() {
        if (!joinedUpcomingMatches.isEmpty()) {
            emptyView.setVisibility(View.GONE);
            adapter = new myAdapter(joinedUpcomingMatches, upcoming.this, joinedMatchIds, "All");
            recyclerView.setAdapter(adapter);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            if (adapter != null) {
                joinedUpcomingMatches.clear();
                adapter.notifyDataSetChanged();
            }
        }
    }
}
