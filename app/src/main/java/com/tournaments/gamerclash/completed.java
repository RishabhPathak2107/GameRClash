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

import com.tournaments.gamerclash.Adapters.ResultAdapter;
import com.tournaments.gamerclash.Models.Result_model;

import java.util.*;

public class completed extends AppCompatActivity {

    RecyclerView recyclerView;
    ResultAdapter mAdapter;
    ArrayList<Result_model> matchList = new ArrayList<>();
    Set<String> joinedMatchIds = new HashSet<>();
    SwipeRefreshLayout swipeRefresh;
    TextView emptyView;
    Toolbar toolbar;
    ImageView back_btn;

    DatabaseReference allMatchesRef, resultsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());
        getSupportActionBar().hide();

        toolbar = findViewById(R.id.toolbar);
        int statusBarHeight = getStatusBarHeight();
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);

        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);

        swipeRefresh.setOnRefreshListener(() -> {
            loadCompletedData();
            swipeRefresh.setRefreshing(false);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadCompletedData();
    }

    private void loadCompletedData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference joinedRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("joinedMatchIds");

            joinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    joinedMatchIds.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Boolean joined = ds.getValue(Boolean.class);
                        if (Boolean.TRUE.equals(joined)) {
                            joinedMatchIds.add(ds.getKey());
                        }
                    }
                    fetchCompletedMatches();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(completed.this, "Failed to fetch joined matches", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchCompletedMatches() {
        allMatchesRef = FirebaseDatabase.getInstance().getReference().child("All_Matches");
        resultsRef = FirebaseDatabase.getInstance().getReference().child("Results");

        resultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot resultsSnapshot) {
                allMatchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot allMatchSnapshot) {
                        matchList.clear();

                        // Loop over categories in Results
                        for (DataSnapshot categorySnap : resultsSnapshot.getChildren()) {
                            // Loop over each match in category
                            for (DataSnapshot matchSnap : categorySnap.getChildren()) {
                                String matchId = matchSnap.getKey();

                                if (joinedMatchIds.contains(matchId) && allMatchSnapshot.hasChild(matchId)) {
                                    Result_model model = allMatchSnapshot.child(matchId).getValue(Result_model.class);
                                    if (model != null) {
                                        model.setMatchId(matchId);
                                        matchList.add(model);
                                    }
                                }
                            }
                        }

                        if (mAdapter == null) {
                            mAdapter = new ResultAdapter(matchList, completed.this);
                            recyclerView.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }

                        if (matchList.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(completed.this, "Failed to load matches", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(completed.this, "Failed to load results", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0) ? getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
