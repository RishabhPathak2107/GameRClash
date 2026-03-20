package com.tournaments.tuskesport;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.tuskesport.Adapters.ViewPagerBonusMatchAdapter;


public class bonus_match extends AppCompatActivity {
    TabLayout tab;
    ViewPager viewPager;
    Toolbar toolbar;
    ImageView back_btn;
    Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus_match);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });
        tab = findViewById(R.id.tab);
        viewPager = findViewById(R.id.viewPager);
        toolbar = findViewById(R.id.toolbar);

        tab.setupWithViewPager(viewPager);
        // Show loading dialog
        showLoadingDialog();

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);

        ViewPagerBonusMatchAdapter adapter = new ViewPagerBonusMatchAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Apply edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Firebase check
        FirebaseDatabase.getInstance().getReference("Matches/Bonus Match")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hideLoadingDialog();

                        boolean hasTrue = false;
                        for (DataSnapshot matchSnap : snapshot.getChildren()) {
                            Boolean isActive = matchSnap.getValue(Boolean.class);
                            if (Boolean.TRUE.equals(isActive)) {
                                hasTrue = true;
                                break;
                            }
                        }

                        if (!hasTrue) {
                            Toast.makeText(bonus_match.this, "No matches available!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideLoadingDialog();
                        Toast.makeText(bonus_match.this, "Failed to load matches!", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void showLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.custom_progress_layout2);
        loadingDialog.setCancelable(false);
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public static class leaderboard extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_leaderboard);
        }
    }
}
