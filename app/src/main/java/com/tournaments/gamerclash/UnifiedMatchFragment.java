package com.tournaments.gamerclash;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.tournaments.gamerclash.Adapters.myAdapter;
import com.tournaments.gamerclash.Models.Match_model;

import java.text.SimpleDateFormat;
import java.util.*;

public class UnifiedMatchFragment extends Fragment {

    RecyclerView recyclerView;
    myAdapter mAdapter;
    ArrayList<Match_model> list;
    Set<String> joinedMatchIds = new HashSet<>();
    String category = "Battle Royale";
    View skeletonLayout;
    TextView emptyView;

    private final Handler handler = new Handler();
    private int lastCheckedMinute = -1;

    public UnifiedMatchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_br_match, container, false);

        if (getArguments() != null) {
            category = getArguments().getString("category", "Battle Royale");
        }

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        skeletonLayout = view.findViewById(R.id.skeletonLayout);
        emptyView = view.findViewById(R.id.emptyView);
        list = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            fetchMatches();
        } else {
            String userId = currentUser.getUid();

            FirebaseDatabase.getInstance().getReference("users").child(userId).child("joinedMatches")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            joinedMatchIds.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                joinedMatchIds.add(ds.getKey());
                            }
                            fetchMatches();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            fetchMatches();
                        }
                    });
        }

        startMinuteBasedMatchCheck();

        return view;
    }

    private void fetchMatches() {
        DatabaseReference matchListRef = FirebaseDatabase.getInstance().getReference("Matches").child(category);

        matchListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                list.clear();
                List<String> matchIds = new ArrayList<>();
                for (DataSnapshot snap : categorySnapshot.getChildren()) {
                    if (Boolean.TRUE.equals(snap.getValue(Boolean.class))) {
                        matchIds.add(snap.getKey());
                    }
                }

                if (matchIds.isEmpty()) {
                    updateAdapter();
                    return;
                }

                final int[] loadedCount = {0};
                for (String matchId : matchIds) {
                    FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot matchSnap) {
                                    if (matchSnap.exists()) {
                                        Match_model matchModel = matchSnap.getValue(Match_model.class);
                                        if (matchModel != null) {
                                            matchModel.setMatchid(matchId);
                                            list.add(matchModel);
                                        }
                                    }
                                    if (++loadedCount[0] == matchIds.size()) {
                                        updateAdapter();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (++loadedCount[0] == matchIds.size()) {
                                        updateAdapter();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (skeletonLayout != null) skeletonLayout.setVisibility(View.GONE);
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isPastDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            sdf.setLenient(false);
            Date matchDate = sdf.parse(dateStr);

            if (matchDate != null) {
                return matchDate.getTime() < System.currentTimeMillis();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateAdapter() {
        if (getActivity() == null) return;

        if (list.isEmpty()) {
            if (skeletonLayout != null) skeletonLayout.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else {
            if (emptyView != null) emptyView.setVisibility(View.GONE);
        }

        if (mAdapter == null) {
            mAdapter = new myAdapter(list, getContext(), joinedMatchIds, category);
            mAdapter.setImageLoadedListener(() -> {
                if (skeletonLayout != null) skeletonLayout.setVisibility(View.GONE);
                if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            });
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
            if (!list.isEmpty() && skeletonLayout != null && skeletonLayout.getVisibility() == View.GONE) {
                if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startMinuteBasedMatchCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int currentMinute = calendar.get(Calendar.MINUTE);

                if (currentMinute != lastCheckedMinute) {
                    lastCheckedMinute = currentMinute;
                    checkAndUpdateExpiredMatches();
                }

                handler.postDelayed(this, 10000);
            }
        }, 0);
    }

    private void checkAndUpdateExpiredMatches() {
        DatabaseReference matchListRef = FirebaseDatabase.getInstance().getReference("Matches").child(category);

        matchListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                for (DataSnapshot snap : categorySnapshot.getChildren()) {
                    String matchId = snap.getKey();
                    Boolean isActive = snap.getValue(Boolean.class);

                    if (Boolean.TRUE.equals(isActive) && matchId != null) {
                        FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot matchSnap) {
                                        String dateStr = matchSnap.child("date").getValue(String.class);
                                        if (dateStr != null && isPastDate(dateStr)) {
                                            FirebaseDatabase.getInstance().getReference("Matches")
                                                    .child(category).child(matchId).setValue(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
