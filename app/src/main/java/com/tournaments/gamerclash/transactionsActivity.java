package com.tournaments.gamerclash;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.tournaments.gamerclash.Adapters.txnAdapter;
import com.tournaments.gamerclash.Models.txnModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class transactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<txnModel> txnList;
    private txnAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    Toolbar toolbar;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());
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

        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);

        txnList = new ArrayList<>();
        adapter = new txnAdapter(this,txnList);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {
            loadTransactions();
            swipeRefresh.setRefreshing(false);
        });

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

                if (txnList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }

                // Check and award first add money bonus (one-time only)
                checkAndAwardFirstAddMoneyBonus(userId, txnList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(transactionsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndAwardFirstAddMoneyBonus(String userId, ArrayList<txnModel> transactions) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("firstAddBonusPaid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean paid = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (paid) return; // Already paid

                boolean hasSuccessfulAddMoney = false;
                for (txnModel txn : transactions) {
                    if (txn.getReason() != null && txn.getReason().toLowerCase().contains("add money")
                            && txn.getStatus() != null && txn.getStatus().equalsIgnoreCase("success")) {
                        hasSuccessfulAddMoney = true;
                        break;
                    }
                }

                if (hasSuccessfulAddMoney) {
                    userRef.child("firstAddBonusPaid").setValue(true);

                    userRef.child("bonus").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot bonusSnap) {
                            double currentBonus = 0.0;
                            if (bonusSnap.exists()) {
                                try {
                                    if (bonusSnap.getValue(Double.class) != null) {
                                        currentBonus = bonusSnap.getValue(Double.class);
                                    } else if (bonusSnap.getValue(Long.class) != null) {
                                        currentBonus = bonusSnap.getValue(Long.class).doubleValue();
                                    }
                                } catch (Exception ignored) {}
                            }
                            userRef.child("bonus").setValue(currentBonus + 5.0);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("transactions").child(userId);
                            String timestampKey = String.valueOf(System.currentTimeMillis());
                            String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            String txnId = generateRandomTxnId(6);

                            txnModel bonusModel = new txnModel(date, "Referral Bonus", "5", txnId, "success");
                            ref.child(timestampKey).setValue(bonusModel);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String generateRandomTxnId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder txnId = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            txnId.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return txnId.toString();
    }
}
