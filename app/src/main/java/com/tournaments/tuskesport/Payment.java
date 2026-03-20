package com.tournaments.tuskesport;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class Payment extends AppCompatActivity {

    ImageView qrImage;
    TextView amountText;
    EditText utrInput;
    Button submitBtn;
    String amount;

    String upiId = "", name = "", note = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


        qrImage = findViewById(R.id.qrImage);
        amountText = findViewById(R.id.amountText);
        utrInput = findViewById(R.id.utrInput);
        submitBtn = findViewById(R.id.submitBtn);
        getSupportActionBar().hide();

        note = "Add Money in " + getString(R.string.app_name);  // ✅ moved inside onCreate

        amount = getIntent().getStringExtra("amount");

        if (amount != null) {
            amountText.setText("Pay ₹" + amount);
        }

        // Load UPI ID and Name from Firebase
        FirebaseDatabase.getInstance().getReference("AppDetails").child("owner")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            upiId = snapshot.child("upiId").getValue(String.class);
                            name = snapshot.child("name").getValue(String.class);

                            if (upiId != null && name != null && amount != null) {
                                generateQR(amount);  // generate QR after loading data
                            } else {
                                Toast.makeText(Payment.this, "Incomplete UPI details", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Payment.this, "Owner UPI details not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Payment.this, "Error loading UPI details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        submitBtn.setOnClickListener(v -> {
            String utr = utrInput.getText().toString().trim();

            if (utr.isEmpty()) {
                Toast.makeText(this, "Please enter UTR/Txn ID", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.getValue(String.class);

                        DatabaseReference allUtrRef = FirebaseDatabase.getInstance().getReference("All_Utr");
                        allUtrRef.child(utr).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(Payment.this, "This UTR ID has already been submitted!", Toast.LENGTH_SHORT).show();
                                } else {
                                    allUtrRef.child(utr).setValue(username);
                                    FirebaseDatabase.getInstance().getReference("utr_ids").child(utr).setValue(username);

                                    Toast.makeText(Payment.this, "UTR ID Submitted Successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Payment.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(Payment.this, "Username not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Payment.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void generateQR(String amount) {
        String upiUri = "upi://pay?pa=" + upiId +
                "&pn=" + name +
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
}
