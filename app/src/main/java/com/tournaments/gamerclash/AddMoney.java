package com.tournaments.gamerclash;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.tournaments.gamerclash.Adapters.AddMoneyAdapter;
import com.tournaments.gamerclash.Models.AddMoneyModel;

import java.util.ArrayList;

public class AddMoney extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<AddMoneyModel> list;
    AddMoneyAdapter adapter;
    DatabaseReference dbRef;
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });

        recyclerView = findViewById(R.id.recyclerViewAddMoney);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new AddMoneyAdapter(this, list);
        recyclerView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference().child("AppDetails").child("AddMoney");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    list.add(new AddMoneyModel(snap.getKey()));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddMoney.this, "Failed to load!", Toast.LENGTH_SHORT).show();
            }
        });
        toolbar = findViewById(R.id.toolbar);

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
