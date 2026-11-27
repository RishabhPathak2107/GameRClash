package com.tournaments.gamerclash;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tournaments.gamerclash.Models.txnModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Payment extends AppCompatActivity {

    private ImageView qrImage, backBtn;
    private TextView amountText;
    private EditText utrInput;
    private Button submitBtn;
    private Toolbar toolbar;

    private String amount = "0";
    private String upiId = "", name = "", note = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        backBtn = findViewById(R.id.back_button);
        qrImage = findViewById(R.id.qrImage);
        amountText = findViewById(R.id.amountText);
        utrInput = findViewById(R.id.utrInput);
        submitBtn = findViewById(R.id.submitBtn);

        backBtn.setOnClickListener(v -> finish());

        // Status bar height adjustment for toolbar
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            toolbar.setPadding(0, statusBarHeight, 0, 0);
        }

        note = "Add Money";

        String intentAmount = getIntent().getStringExtra("amount");
        if (intentAmount != null && !intentAmount.isEmpty()) {
            amount = intentAmount;
        }
        amountText.setText("₹" + amount);

        // Load Owner UPI ID and Name from Firebase
        FirebaseDatabase.getInstance().getReference("AppDetails").child("owner")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            upiId = snapshot.child("upiId").getValue(String.class);
                            name = snapshot.child("name").getValue(String.class);

                            if (upiId != null && !upiId.isEmpty()) {
                                generateQR(amount);
                            } else {
                                Toast.makeText(Payment.this, "UPI details incomplete in Firebase", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Payment.this, "UPI details not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Payment.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        submitBtn.setOnClickListener(v -> {
            String utr = utrInput.getText().toString().trim();
            if (utr.isEmpty()) {
                Toast.makeText(Payment.this, "Please enter UTR / Transaction ID", Toast.LENGTH_SHORT).show();
                return;
            }
            submitUtrId(utr);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndAutoFillClipboardUtr();
    }

    private void checkAndAutoFillClipboardUtr() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence text = clipData.getItemAt(0).getText();
                    if (text != null) {
                        String copiedText = text.toString().trim();
                        if (copiedText.matches("^[0-9]{12}$")) {
                            if (utrInput != null && utrInput.getText().toString().trim().isEmpty()) {
                                utrInput.setText(copiedText);
                                Toast.makeText(this, "Auto-filled UTR from Clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void generateQR(String amount) {
        String upiUri = "upi://pay?pa=" + upiId +
                "&pn=" + (name != null ? name : "") +
                "&tn=" + note +
                "&am=" + amount +
                "&cu=INR";

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(writer.encode(upiUri, BarcodeFormat.QR_CODE, 500, 500));
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void submitUtrId(String utr) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        // 1. Check if UTR already used in All_Utr
        dbRef.child("All_Utr").child(utr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot allUtrSnap) {
                if (allUtrSnap.exists()) {
                    Toast.makeText(Payment.this, "This UTR ID has already been used!", Toast.LENGTH_SHORT).show();
                } else {
                    // 2. Check if UTR is pre-approved in utr_ids
                    dbRef.child("utr_ids").child(utr).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot utrIdSnap) {
                            dbRef.child("users").child(uid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    String username = userSnap.exists() && userSnap.getValue(String.class) != null
                                            ? userSnap.getValue(String.class)
                                            : "User";

                                    if (utrIdSnap.exists()) {
                                        // UTR is pre-approved! Extract exact amount from utr_ids/{utr} value
                                        Object val = utrIdSnap.getValue();
                                        double validAmount = 0.0;
                                        try {
                                            if (val instanceof Long) {
                                                validAmount = ((Long) val).doubleValue();
                                            } else if (val instanceof Double) {
                                                validAmount = (Double) val;
                                            } else if (val instanceof String) {
                                                validAmount = Double.parseDouble((String) val);
                                            }
                                        } catch (Exception ignored) {}

                                        if (validAmount <= 0) {
                                            Toast.makeText(Payment.this, "Invalid amount recorded for this UTR", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        final double finalAmount = validAmount;

                                        // Mark UTR as used in All_Utr
                                        dbRef.child("All_Utr").child(utr).setValue(username);

                                        // Add to user deposit balance instantly using utr_ids value
                                        DatabaseReference userRef = dbRef.child("users").child(uid);
                                        userRef.child("deposit").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot depSnap) {
                                                double currentDeposit = 0.0;
                                                if (depSnap.exists() && depSnap.getValue() != null) {
                                                    try {
                                                        if (depSnap.getValue(Double.class) != null) {
                                                            currentDeposit = depSnap.getValue(Double.class);
                                                        } else if (depSnap.getValue(Long.class) != null) {
                                                            currentDeposit = depSnap.getValue(Long.class).doubleValue();
                                                        }
                                                    } catch (Exception ignored) {}
                                                }

                                                userRef.child("deposit").setValue(currentDeposit + finalAmount);

                                                // Save transaction with status "success"
                                                addTransactionToFirebase(uid, String.valueOf(finalAmount), "Add Money", utr, "success");

                                                // Remove from utr_ids
                                                dbRef.child("utr_ids").child(utr).removeValue();

                                                Toast.makeText(Payment.this, (int)(finalAmount) + " added to wallet.", Toast.LENGTH_LONG).show();
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });

                                    } else {
                                        // UTR not found in utr_ids yet (Email/SMS hasn't arrived or invalid UTR)
                                        Toast.makeText(Payment.this, "UTR not found yet! Please try again in 1-2 minutes or contact support.", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Payment.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTransactionToFirebase(String userId, String amount, String reason, String txnId, String status) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        txnModel model = new txnModel(date, reason, amount, txnId, status);
        ref.child(txnId).setValue(model);
    }
}
