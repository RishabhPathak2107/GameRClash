package com.tournaments.tuskesport.ui.notifications;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.tuskesport.R;
import com.tournaments.tuskesport.chatMessage;
import com.tournaments.tuskesport.contactus;
import com.tournaments.tuskesport.databinding.FragmentNotificationsBinding;
import com.tournaments.tuskesport.leaderboard;
import com.tournaments.tuskesport.login;
import com.tournaments.tuskesport.notification;
import com.tournaments.tuskesport.profile;
import com.tournaments.tuskesport.wallet;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    FirebaseUser auth;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.myWallet.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), wallet.class);
            startActivity(intent);
        });
        binding.myprofile.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), profile.class);
            startActivity(intent);
        });
        binding.leaderboard.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), leaderboard.class);
            startActivity(intent);
        });
        binding.contactus.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), contactus.class);
            startActivity(intent);
        });
        binding.notifications.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), notification.class);
            startActivity(intent);
        });
        binding.chats.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), chatMessage.class);
            startActivity(intent);
        });
        binding.logout.setOnClickListener(view -> {
            showLogoutDialog();
        });

        // Set notification status initially
        updateNotificationStatus();

        updateNotificationStatus();

// Optional: take user to app notification settings when tapped
        binding.notificationOnOff.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                startActivity(intent);
            }
        });





        auth = FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(auth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!isAdded() || binding == null) return;

                            String name1 = snapshot.child("fname").getValue(String.class);
                            String name2 = snapshot.child("lname").getValue(String.class);

                            if (name1 != null && name2 != null) {
                                binding.name.setText(name1 + " " + name2);
                            }

                            // Matches Played
                            Long matchesPlayed = snapshot.child("matches_played").getValue(Long.class);
                            binding.matchesPlayed.setText(matchesPlayed != null ? String.valueOf(matchesPlayed) : "0");

                            // Total Kills
                            Long totalKills = snapshot.child("kills").getValue(Long.class);
                            binding.totalKills.setText(totalKills != null ? String.valueOf(totalKills) : "0");

                            // Total Coins
                            Long totalDeaths = snapshot.child("total_coins").getValue(Long.class);
                            binding.totalCoins.setText(totalDeaths != null ? String.valueOf(totalDeaths) : "0");
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Error handling
                        }
                    });
        }


        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        updateNotificationStatus();
    }

    private void updateNotificationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean isGranted = requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            binding.notificationOnOff.setChecked(isGranted);
        } else {
            // For Android 12 and below, assume permission is granted
            binding.notificationOnOff.setChecked(true);
        }
    }

    private void showLogoutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        // Buttons
        dialogView.findViewById(R.id.cancel_btn).setOnClickListener(v -> alertDialog.dismiss());
        dialogView.findViewById(R.id.logout_btn).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
