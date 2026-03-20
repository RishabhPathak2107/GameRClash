package com.tournaments.tuskesport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.tuskesport.Adapters.ResultAdapter;
import com.tournaments.tuskesport.Models.Result_model;

import java.util.ArrayList;

public class LW2_ResultFragment extends Fragment {

    RecyclerView recyclerView;
    ResultAdapter resultAdapter;
    ArrayList<Result_model> list;
    String category = "Lone Wolf2";

    public LW2_ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lw_result, container, false);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();
        resultAdapter = new ResultAdapter(list, getContext(), category);
        recyclerView.setAdapter(resultAdapter);

        FirebaseDatabase.getInstance().getReference()
                .child("Results").child(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                            String matchId = matchSnapshot.getKey();
                            Boolean isVisible = matchSnapshot.getValue(Boolean.class);

                            if (isVisible != null && isVisible) {
                                fetchMatchData(matchId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        return view;
    }

    // Step 2: Get match data from All_Matches/matchId
    private void fetchMatchData(String matchId) {
        FirebaseDatabase.getInstance().getReference()
                .child("All_Matches").child(matchId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Result_model model = snapshot.getValue(Result_model.class);
                        if (model != null) {
                            list.add(model);
                            resultAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
