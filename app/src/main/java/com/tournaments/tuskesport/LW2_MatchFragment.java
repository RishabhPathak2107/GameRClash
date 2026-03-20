package com.tournaments.tuskesport;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.tournaments.tuskesport.Adapters.myAdapter;
import com.tournaments.tuskesport.Models.Match_model;

import java.text.SimpleDateFormat;
import java.util.*;

public class LW2_MatchFragment extends Fragment {

    RecyclerView recyclerView;
    myAdapter mAdapter;
    ArrayList<Match_model> list;
    Set<String> joinedMatchIds = new HashSet<>();
    String category = "Lone Wolf2";

    private Handler handler = new Handler();
    private int lastCheckedMinute = -1;

    public LW2_MatchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lw_match, container, false);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            fetchMatchesWithoutUser();
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
                            fetchMatchesWithUser();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            fetchMatchesWithUser();
                        }
                    });
        }

        startMinuteBasedMatchCheck();

        return view;
    }

    private void fetchMatchesWithUser() {
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

                if (matchIds.isEmpty()) updateAdapter();

                final int[] loadedCount = {0};
                for (String matchId : matchIds) {
                    FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot matchSnap) {
                                    if (matchSnap.exists()) {
                                        Match_model matchModel = matchSnap.getValue(Match_model.class);
                                        if (matchModel != null) {
                                            matchModel.setMatchid(matchId);
                                            matchModel.settUser(matchSnap.child("tUser").getValue(String.class));

                                            FirebaseDatabase.getInstance().getReference("joinings")
                                                    .child(category).child(matchId)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot joinSnap) {
                                                            matchModel.setcUser(String.valueOf(joinSnap.getChildrenCount()));
                                                            list.add(matchModel);
                                                            if (++loadedCount[0] == matchIds.size()) {
                                                                updateAdapter();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            matchModel.setcUser("0");
                                                            list.add(matchModel);
                                                            if (++loadedCount[0] == matchIds.size()) {
                                                                updateAdapter();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            if (++loadedCount[0] == matchIds.size()) updateAdapter();
                                        }
                                    } else {
                                        if (++loadedCount[0] == matchIds.size()) updateAdapter();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (++loadedCount[0] == matchIds.size()) updateAdapter();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchMatchesWithoutUser() {
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

                if (matchIds.isEmpty()) updateAdapter();

                final int[] loadedCount = {0};
                for (String matchId : matchIds) {
                    FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot matchSnap) {
                                    if (matchSnap.exists()) {
                                        Match_model matchModel = matchSnap.getValue(Match_model.class);
                                        if (matchModel != null) {
                                            matchModel.setMatchid(matchId);
                                            matchModel.setcUser("0");
                                            matchModel.settUser(matchSnap.child("tUser").getValue(String.class));
                                            list.add(matchModel);
                                        }
                                    }

                                    if (++loadedCount[0] == matchIds.size()) updateAdapter();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (++loadedCount[0] == matchIds.size()) updateAdapter();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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

        if (mAdapter == null) {
            mAdapter = new myAdapter(list, getContext(), joinedMatchIds, category);
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
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

                handler.postDelayed(this, 10000); // Check every 10 seconds if minute changed
            }
        }, 0);
    }

    private void checkAndUpdateExpiredMatches() {
        DatabaseReference matchListRef = FirebaseDatabase.getInstance().getReference("Matches").child(category);

        matchListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                for (DataSnapshot snap : categorySnapshot.getChildren()) {
                    String matchId = snap.getKey();
                    Boolean isActive = snap.getValue(Boolean.class);

                    if (Boolean.TRUE.equals(isActive)) {
                        FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                                .addValueEventListener(new ValueEventListener() {
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
}
