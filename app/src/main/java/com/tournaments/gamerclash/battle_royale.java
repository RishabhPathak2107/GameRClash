package com.tournaments.gamerclash;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tournaments.gamerclash.Adapters.UnifiedViewPagerAdapter;

public class battle_royale extends AppCompatActivity {

    TabLayout tab;
    ViewPager viewPager;
    Toolbar toolbar;
    TextView toolbarTitle;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_royale);

        String title = getIntent().getStringExtra("title");
        String category = getIntent().getStringExtra("category");
        if (title == null) title = "BATTLE ROYALE";
        if (category == null) category = "Battle Royale";

        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());

        tab = findViewById(R.id.tab);
        viewPager = findViewById(R.id.viewPager);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);

        // Setup ViewPager + TabLayout
        UnifiedViewPagerAdapter adapter = new UnifiedViewPagerAdapter(getSupportFragmentManager(), category);
        viewPager.setAdapter(adapter);
        tab.setupWithViewPager(viewPager);

        // Status bar height adjustment
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);

        // Hide default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Navigation bar black
        getWindow().setNavigationBarColor(Color.parseColor("#000000"));
        getWindow().getDecorView().setSystemUiVisibility(0);

        // Window insets for padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Firebase check
        FirebaseDatabase.getInstance().getReference("Matches").child(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(battle_royale.this, "Failed to load matches!", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
