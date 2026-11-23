package com.tournaments.gamerclash;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.gamerclash.Adapters.WithdrawAdapter;
import com.tournaments.gamerclash.Models.WithdrawModel;

import java.util.ArrayList;

public class Withdraw extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<WithdrawModel> list;
    WithdrawAdapter adapter;
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });

        recyclerView = findViewById(R.id.recyclerViewWithdraw);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new WithdrawAdapter(this, list);
        recyclerView.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AppDetails/Withdraw");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot methodSnap : snapshot.getChildren()) {
                    String method = methodSnap.getKey(); // e.g. "Google Play", "UPI"

                    for (DataSnapshot amountSnap : methodSnap.getChildren()) {
                        Boolean isEnabled = amountSnap.getValue(Boolean.class);
                        if (isEnabled != null && isEnabled) {
                            try {
                                int amount = Integer.parseInt(amountSnap.getKey());
                                list.add(new WithdrawModel(method, amount));
                            } catch (NumberFormatException e) {
                                // Skip invalid keys like "note" or others
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Withdraw.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        toolbar =findViewById(R.id.toolbar);

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
    }
}
