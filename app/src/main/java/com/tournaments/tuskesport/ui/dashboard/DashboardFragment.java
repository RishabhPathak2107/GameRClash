package com.tournaments.tuskesport.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.tuskesport.Models.CustomSliderModel;
import com.tournaments.tuskesport.Models.NotificationModel;
import com.tournaments.tuskesport.R;
import com.tournaments.tuskesport.battle_royale;
import com.tournaments.tuskesport.bonus_match;
import com.tournaments.tuskesport.clash_squad;
import com.tournaments.tuskesport.clash_squad2;
import com.tournaments.tuskesport.completed;
import com.tournaments.tuskesport.databinding.FragmentDashboardBinding;
import com.tournaments.tuskesport.lone_wolf;
import com.tournaments.tuskesport.lone_wolf2;
import com.tournaments.tuskesport.notification;
import com.tournaments.tuskesport.ongoing;
import com.tournaments.tuskesport.survival;
import com.tournaments.tuskesport.upcoming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ImageSlider imageSlider;
    private List<String> updatesList = new ArrayList<>();
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    DatabaseReference linkRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageSlider = binding.imageSlider;

        DatabaseReference sliderRef = FirebaseDatabase.getInstance().getReference("SliderImages");
        sliderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SlideModel> slideModels = new ArrayList<>();
                List<String> slideUrls = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    CustomSliderModel model = snap.getValue(CustomSliderModel.class);
                    if (model != null && model.getImg() != null && model.getUrl() != null) {
                        slideModels.add(new SlideModel(model.getImg(), ScaleTypes.FIT));
                        slideUrls.add(model.getUrl());
                    }
                }

                imageSlider.setImageList(slideModels, ScaleTypes.FIT);

                imageSlider.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onItemSelected(int position) {
                        if (position < slideUrls.size()) {
                            String url = slideUrls.get(position);
                            if (url != null && !url.isEmpty()) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                        }
                    }

                    @Override
                    public void doubleClick(int position) {
                        // optional
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });


        // Load updates from Firebase
        loadUpdates();

        // Navigation Click Listeners
        binding.br.setOnClickListener(view -> startActivity(new Intent(getActivity(), battle_royale.class)));
        binding.notification.setOnClickListener(view -> startActivity(new Intent(getActivity(), notification.class)));
        binding.cs.setOnClickListener(view -> startActivity(new Intent(getActivity(), clash_squad.class)));
        binding.cs2.setOnClickListener(view -> startActivity(new Intent(getActivity(), clash_squad2.class)));
        binding.lw1.setOnClickListener(view -> startActivity(new Intent(getActivity(), lone_wolf.class)));
        binding.lw2.setOnClickListener(view -> startActivity(new Intent(getActivity(), lone_wolf2.class)));
        binding.surv.setOnClickListener(view -> startActivity(new Intent(getActivity(), survival.class)));
        binding.upcoming.setOnClickListener(view -> startActivity(new Intent(getActivity(), upcoming.class)));
        binding.ongoing.setOnClickListener(view -> startActivity(new Intent(getActivity(), ongoing.class)));
        binding.completed.setOnClickListener(view -> startActivity(new Intent(getActivity(), completed.class)));
        binding.bonus.setOnClickListener(view -> startActivity(new Intent(getActivity(), bonus_match.class)));

        return root;
    }

    private void loadUpdates() {
        FirebaseDatabase.getInstance().getReference().child("Updates")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded() || binding == null) return;

                        updatesList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            NotificationModel update = snap.getValue(NotificationModel.class);
                            if (update != null && update.getTitle() != null) {
                                updatesList.add(update.getTitle());
                            }
                        }
                        Collections.reverse(updatesList);
                        if (!updatesList.isEmpty()) {
                            startAutoUpdateText();
                        } else {
                            binding.updatesId.setText("No updates found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!isAdded() || binding == null) return;
                        binding.updatesId.setText("Failed to load updates.");
                    }
                });
    }

    private void startAutoUpdateText() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && !updatesList.isEmpty()) {
                    binding.updatesId.setText(updatesList.get(currentIndex));
                    currentIndex = (currentIndex + 1) % updatesList.size();
                    handler.postDelayed(this, 5000);
                }
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        binding = null;
    }
}
