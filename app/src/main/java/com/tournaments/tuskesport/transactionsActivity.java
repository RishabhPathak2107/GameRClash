package com.tournaments.tuskesport;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.tournaments.tuskesport.Adapters.txnAdapter;
import com.tournaments.tuskesport.Models.txnModel;

import java.util.ArrayList;
import java.util.List;

public class transactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<txnModel> txnList;
    private txnAdapter adapter;
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });
        getSupportActionBar().hide();
        toolbar = findViewById(R.id.toolbar);

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);

        recyclerView = findViewById(R.id.transactionRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        txnList = new ArrayList<>();
        adapter = new txnAdapter(this,txnList);
        recyclerView.setAdapter(adapter);

        loadTransactions();
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        DatabaseReference txnRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        txnRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txnList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    txnModel txn = ds.getValue(txnModel.class);
                    if (txn != null) {
                        txnList.add(txn);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(transactionsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
