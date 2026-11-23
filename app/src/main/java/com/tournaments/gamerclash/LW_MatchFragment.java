package com.tournaments.gamerclash;

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

import com.tournaments.gamerclash.Adapters.myAdapter;
import com.tournaments.gamerclash.Models.Match_model;

import java.text.SimpleDateFormat;
import java.util.*;

public class LW_MatchFragment extends Fragment {

    RecyclerView recyclerView;
    myAdapter mAdapter;
    ArrayList<Match_model> list;
    Set<String> joinedMatchIds = new HashSet<>();
    String category = "Lone Wolf";

    private Handler handler = new Handler();
    private int lastCheckedMinute = -1;

    public LW_MatchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cs_match, container, false);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            fetchMatchesWithoutUser();
        } else {
            String userId = currentUser.getUid();

            FirebaseDatabase.getInstance().getReference("users").child(userId).child("joinedMatches")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
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

        matchListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                List<String> matchIds = new ArrayList<>();
                for (DataSnapshot snap : categorySnapshot.getChildren()) {
                    if (Boolean.TRUE.equals(snap.getValue(Boolean.class))) {
                        matchIds.add(snap.getKey());
                    }
                }

                if (matchIds.isEmpty()) {
                    list.clear();
                    updateAdapter();
                    return;
                }

                List<Match_model> tempList = Collections.synchronizedList(new ArrayList<>());
                final int[] loadedCount = {0};
                int totalMatches = matchIds.size();

                for (String matchId : matchIds) {
                    FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot matchSnap) {
                                    if (matchSnap.exists()) {
                                        Match_model matchModel = matchSnap.getValue(Match_model.class);
                                        if (matchModel != null) {
                                            matchModel.setMatchid(matchId);
                                            matchModel.settUser(matchSnap.child("tUser").getValue(String.class));

                                            FirebaseDatabase.getInstance().getReference("joinings")
                                                    .child(matchId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot joinSnap) {
                                                            matchModel.setcUser(String.valueOf(joinSnap.getChildrenCount()));
                                                            tempList.add(matchModel);
                                                            if (++loadedCount[0] == totalMatches) {
                                                                list.clear();
                                                                list.addAll(tempList);
                                                                updateAdapter();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            matchModel.setcUser("0");
                                                            tempList.add(matchModel);
                                                            if (++loadedCount[0] == totalMatches) {
                                                                list.clear();
                                                                list.addAll(tempList);
                                                                updateAdapter();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            if (++loadedCount[0] == totalMatches) {
                                                list.clear();
                                                list.addAll(tempList);
                                                updateAdapter();
                                            }
                                        }
                                    } else {
                                        if (++loadedCount[0] == totalMatches) {
                                            list.clear();
                                            list.addAll(tempList);
                                            updateAdapter();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (++loadedCount[0] == totalMatches) {
                                        list.clear();
                                        list.addAll(tempList);
                                        updateAdapter();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchMatchesWithoutUser() {
        DatabaseReference matchListRef = FirebaseDatabase.getInstance().getReference("Matches").child(category);

        matchListRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                handler.postDelayed(this, 10000);
            }
        }, 0);
    }

    private void checkAndUpdateExpiredMatches() {
        if (list == null || list.isEmpty()) return;

        for (Match_model match : new ArrayList<>(list)) {
            String dateStr = match.getDate();

            if (dateStr != null && isPastDate(dateStr)) {
                String matchId = match.getMatchid();

                DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("Matches").child(category).child(matchId);
                matchRef.setValue(false);

                DatabaseReference ongoingRef = FirebaseDatabase.getInstance().getReference("ongoing").child(category).child(matchId);
                ongoingRef.setValue(true);

                list.remove(match);
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            }
        }
    }
}
