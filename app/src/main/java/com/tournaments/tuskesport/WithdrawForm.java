package com.tournaments.tuskesport;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class WithdrawForm extends AppCompatActivity {

    TextView withdrawAmount, coinValue;
    EditText inputField;
    Button submitBtn;

    FirebaseAuth auth;
    DatabaseReference userRef, withdrawRef;

    Context mContext;

    int amount;
    String currentUsername = "";
    int currentWinnings = 0;
    String type = "UPI";

    boolean isDisabled = false; // 🔥 disabled flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_form);
        getSupportActionBar().hide();

        mContext = this;

        withdrawAmount = findViewById(R.id.withdrawAmount);
        coinValue = findViewById(R.id.coinValue);
        inputField = findViewById(R.id.inputField);
        submitBtn = findViewById(R.id.submitBtn);

        amount = getIntent().getIntExtra("amount", 0);
        type = getIntent().getStringExtra("type");

        withdrawAmount.setText("Amount: ₹" + amount);
        coinValue.setText("Coins: " + amount);

        if (type != null && type.equalsIgnoreCase("UPI")) {
            inputField.setHint("Enter your UPI ID");
        } else {
            inputField.setHint("Enter your Gmail ID");
        }

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(auth.getUid());

        withdrawRef = FirebaseDatabase.getInstance()
                .getReference("WithdrawRequests");

        // 🔥 USER DATA + DISABLED CHECK
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                // 🚫 Disabled check
                Boolean disabled = snapshot.child("disabled").getValue(Boolean.class);
                if (disabled != null && disabled) {
                    isDisabled = true;
                    showSuspendedDialog();
                    return;
                }

                currentUsername = snapshot.child("username").getValue(String.class);

                if (snapshot.child("winnings").getValue() != null) {
                    currentWinnings = snapshot.child("winnings").getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        WithdrawForm.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        submitBtn.setOnClickListener(v -> {
            if (isDisabled) {
                showSuspendedDialog();
            } else {
                submitWithdraw();
            }
        });
    }

    private void submitWithdraw() {
        String input = inputField.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter your " + (type.equalsIgnoreCase("UPI") ? "UPI ID" : "Gmail ID"),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 👉 Validate UPI or Gmail
        if (type.equalsIgnoreCase("UPI")) {
            if (!input.contains("@") || input.startsWith("@") || input.endsWith("@")) {
                Toast.makeText(this, "Enter a valid UPI ID", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (!input.endsWith("@gmail.com") || input.length() <= 10) {
                Toast.makeText(this, "Enter a valid Gmail ID", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (currentWinnings < amount) {
            Toast.makeText(this, "Not enough winnings to withdraw.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "User info not loaded yet. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String txnId = generateRandomTxnId(6);

        withdrawRef.child(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            Toast.makeText(
                                    WithdrawForm.this,
                                    "You already have a pending request.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {

                            withdrawRef.child(currentUsername)
                                    .child(String.valueOf(amount))
                                    .setValue(input)
                                    .addOnSuccessListener(unused -> {

                                        withdrawRef.child(currentUsername)
                                                .child("txnId")
                                                .setValue(txnId);

                                        userRef.child("winnings")
                                                .setValue(currentWinnings - amount);

                                        addTransactionToFirebase(
                                                auth.getUid(),
                                                String.valueOf(amount),
                                                "Withdraw Money",
                                                txnId,
                                                "Pending"
                                        );

                                        Toast.makeText(
                                                WithdrawForm.this,
                                                "Withdraw request submitted successfully.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    WithdrawForm.this,
                                                    "Failed: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                WithdrawForm.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // 🚨 NON-CANCELABLE SUSPENDED POPUP
    private void showSuspendedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account Suspended");
        builder.setMessage("Your ID has been suspended.\nWithdraw is disabled.");

        builder.setCancelable(false);

        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            finishAffinity();
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void addTransactionToFirebase(
            String userId,
            String amount,
            String reason,
            String txnId,
            String status
    ) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        String date = new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());

        long timestamp = System.currentTimeMillis();

        com.tournaments.tuskesport.Models.txnModel model =
                new com.tournaments.tuskesport.Models.txnModel(
                        date,
                        reason,
                        amount,
                        txnId,
                        status
                );

        ref.child(String.valueOf(timestamp))
                .setValue(model);
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
