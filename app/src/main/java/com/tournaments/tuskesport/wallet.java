package com.tournaments.tuskesport;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tournaments.tuskesport.Models.UserModel;

import java.util.Locale;

public class wallet extends AppCompatActivity {

    TextView totalBalance, depositedBalance, bonusBalance, winningBalance;
    LinearLayout txn, withdraw, addMoney;
    FirebaseAuth auth;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });

        // Initialize views
        totalBalance = findViewById(R.id.totalBalance);
        depositedBalance = findViewById(R.id.depositedBalance);
        bonusBalance = findViewById(R.id.bonusBalance);
        winningBalance = findViewById(R.id.winningBalance);
        txn = findViewById(R.id.txn);
        withdraw = findViewById(R.id.withdraw);
        addMoney = findViewById(R.id.addMoney);

        auth = FirebaseAuth.getInstance();

        // Load user wallet data
        if (auth.getUid() != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(auth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                if (snapshot.exists()) {
                                    UserModel userModel = snapshot.getValue(UserModel.class);
                                    if (userModel != null) {
                                        double bonus = userModel.getBonus();
                                        double deposit = userModel.getDeposit();
                                        double winnings = userModel.getWinnings();
                                        double total = bonus + deposit + winnings;

                                        bonusBalance.setText(String.format(Locale.getDefault(), "%.2f", bonus));
                                        depositedBalance.setText(String.format(Locale.getDefault(), "%.2f", deposit));
                                        winningBalance.setText(String.format(Locale.getDefault(), "%.2f", winnings));
                                        totalBalance.setText(String.format(Locale.getDefault(), "%.2f", total));

                                    } else {
                                        setDefaultBalance();
                                    }
                                } else {
                                    setDefaultBalance();
                                }
                            } catch (Exception e) {
                                Toast.makeText(wallet.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                setDefaultBalance();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(wallet.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            setDefaultBalance();
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            setDefaultBalance();
        }

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Buttons click handlers
        txn.setOnClickListener(v -> startActivity(new Intent(wallet.this, transactionsActivity.class)));
        addMoney.setOnClickListener(v -> startActivity(new Intent(wallet.this, AddMoney.class)));
        withdraw.setOnClickListener(v -> startActivity(new Intent(wallet.this, Withdraw.class)));

        // Window insets
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
    }

    private void setDefaultBalance() {
        bonusBalance.setText("0");
        depositedBalance.setText("0");
        winningBalance.setText("0");
        totalBalance.setText("0");
    }
}